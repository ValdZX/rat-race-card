package ua.vald_zx.game.rat.race.server

import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import ua.vald_zx.game.rat.race.card.shared.CardText
import ua.vald_zx.game.rat.race.card.shared.Dream
import ua.vald_zx.game.rat.race.card.shared.GeneratedText
import ua.vald_zx.game.rat.race.card.shared.GenerationQuotaType
import ua.vald_zx.game.rat.race.card.shared.Gender
import ua.vald_zx.game.rat.race.card.shared.PayerType
import ua.vald_zx.game.rat.race.card.shared.ProfessionCard
import ua.vald_zx.game.rat.race.card.shared.ShopType
import ua.vald_zx.game.rat.race.card.shared.generatedLocales
import ua.vald_zx.game.rat.race.server.data.Env
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

private val llmLogger = KtorSimpleLogger("CardTextLlm")
private val llmQuotaTracker = LlmQuotaTracker()
private val llmHttpClient: HttpClient by lazy {
    HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build()
}

internal object LlmSettings {
    val providers: List<LlmProviderSettings> by lazy { configuredLlmProviders { Env[it] } }

    val enabled: Boolean get() = providers.isNotEmpty()

    fun logConfiguration() {
        if (providers.isEmpty()) {
            llmLogger.warn("No LLM provider is configured, board generation is unavailable")
            return
        }
        llmLogger.info(
            "LLM pool of ${providers.size}: " + providers.joinToString {
                "${it.name} balance=${it.balanceModel} text=${it.textModel}"
            }
        )
    }

    fun balanceChat(
        onUsage: suspend (LlmTokenUsage) -> Unit = {},
        onQuota: suspend (LlmQuotaSnapshot) -> Unit = {},
        onRetry: suspend (LlmRetryWait) -> Unit = {},
    ): ChatCompletion = pool({ it.balanceModel }, onUsage, onQuota, onRetry)

    fun textChat(
        onUsage: suspend (LlmTokenUsage) -> Unit = {},
        onQuota: suspend (LlmQuotaSnapshot) -> Unit = {},
        onRetry: suspend (LlmRetryWait) -> Unit = {},
    ): ChatCompletion = pool({ it.textModel }, onUsage, onQuota, onRetry)

    private fun pool(
        model: (LlmProviderSettings) -> String,
        onUsage: suspend (LlmTokenUsage) -> Unit,
        onQuota: suspend (LlmQuotaSnapshot) -> Unit,
        onRetry: suspend (LlmRetryWait) -> Unit,
    ): ChatCompletion = LlmProviderPool(
        members = providers.map { provider ->
            LlmProviderPool.Member(
                provider = provider.name,
                model = model(provider),
                completion = HttpChatCompletion(
                    provider = provider,
                    model = model(provider),
                    onUsage = onUsage,
                    onQuota = onQuota,
                ),
            )
        },
        onRetry = onRetry,
    )

}

internal fun configuredLlmProviders(
    configuration: (String) -> String?,
): List<LlmProviderSettings> = buildList {
    (listOf(PRIMARY_LLM_PROVIDER) + FREE_LLM_PROVIDERS).mapNotNullTo(this) { defaults ->
        provider(configuration, defaults)
    }
    (1..MAX_LLM_FALLBACKS).mapNotNullTo(this) { index ->
        provider(
            configuration,
            LlmProviderDefaults(
                prefix = "LLM_FALLBACK_${index}_",
                apiKey = "LLM_FALLBACK_${index}_API_KEY",
                name = "fallback-$index",
            ),
        )
    }
}

private fun provider(
    configuration: (String) -> String?,
    defaults: LlmProviderDefaults,
): LlmProviderSettings? {
    val key = configuration(defaults.apiKey).orEmpty()
    if (key.isBlank()) return null
    val model = configuration("${defaults.prefix}MODEL")
    val url = configuration("${defaults.prefix}API_URL") ?: defaults.url
    val balanceModel = configuration("${defaults.prefix}BALANCE_MODEL") ?: model ?: defaults.balanceModel
    val textModel = configuration("${defaults.prefix}TEXT_MODEL") ?: model ?: defaults.textModel
    val missing = buildList {
        if (url == null) add("${defaults.prefix}API_URL")
        if (balanceModel == null) add("${defaults.prefix}BALANCE_MODEL or ${defaults.prefix}MODEL")
        if (textModel == null) add("${defaults.prefix}TEXT_MODEL or ${defaults.prefix}MODEL")
    }
    if (missing.isNotEmpty()) {
        llmLogger.warn(
            "${defaults.apiKey} is set but the provider is ignored, missing: ${missing.joinToString()}"
        )
        return null
    }
    return LlmProviderSettings(
        name = configuration("${defaults.prefix}PROVIDER_NAME") ?: defaults.name,
        url = requireNotNull(url),
        key = key,
        balanceModel = requireNotNull(balanceModel),
        textModel = requireNotNull(textModel),
    )
}

private data class LlmProviderDefaults(
    val prefix: String,
    val apiKey: String,
    val name: String,
    val url: String? = null,
    val balanceModel: String? = null,
    val textModel: String? = null,
)

private val PRIMARY_LLM_PROVIDER = LlmProviderDefaults(
    prefix = "LLM_",
    apiKey = "LLM_API_KEY",
    name = "gemini",
    url = "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions",
    balanceModel = "gemini-3.6-flash",
    textModel = "gemini-3.5-flash-lite",
)

private val FREE_LLM_PROVIDERS = listOf(
    LlmProviderDefaults(
        prefix = "GROQ_",
        apiKey = "GROQ_API_KEY",
        name = "groq",
        url = "https://api.groq.com/openai/v1/chat/completions",
        balanceModel = "openai/gpt-oss-120b",
        textModel = "openai/gpt-oss-20b",
    ),
    LlmProviderDefaults(
        prefix = "CEREBRAS_",
        apiKey = "CEREBRAS_API_KEY",
        name = "cerebras",
        url = "https://api.cerebras.ai/v1/chat/completions",
        balanceModel = "gpt-oss-120b",
        textModel = "gpt-oss-120b",
    ),
    LlmProviderDefaults(
        prefix = "MISTRAL_",
        apiKey = "MISTRAL_API_KEY",
        name = "mistral",
        url = "https://api.mistral.ai/v1/chat/completions",
        balanceModel = "mistral-small-latest",
        textModel = "mistral-small-latest",
    ),
    LlmProviderDefaults(
        prefix = "OPENROUTER_",
        apiKey = "OPENROUTER_API_KEY",
        name = "openrouter",
        url = "https://openrouter.ai/api/v1/chat/completions",
        balanceModel = "openrouter/free",
        textModel = "openrouter/free",
    ),
)

internal data class LlmProviderSettings(
    val name: String,
    val url: String,
    val key: String,
    val balanceModel: String,
    val textModel: String,
)

internal data class LlmTokenUsage(
    val input: Long,
    val output: Long,
    val total: Long,
    val quota: LlmQuotaSnapshot? = null,
)

internal data class LlmRetryWait(
    val provider: String,
    val model: String,
    val delayMillis: Long,
    val quota: LlmQuotaSnapshot? = null,
)

internal fun interface ChatCompletion {
    suspend fun complete(system: String, user: String): String?
}

internal class LlmProviderPool(
    private val members: List<Member>,
    private val maxWaitMillis: Long = MAX_POOL_WAIT_MILLIS,
    private val onRetry: suspend (LlmRetryWait) -> Unit = {},
    private val nowEpochMs: () -> Long = System::currentTimeMillis,
    private val wait: suspend (Long) -> Unit = { delay(it) },
) : ChatCompletion {

    private val nextMemberIndex = AtomicInteger()

    internal class Member(
        val provider: String,
        val model: String,
        val completion: ChatCompletion,
    ) {
        internal val slot = Semaphore(1)
        internal val cooldown = AtomicReference<Cooldown?>(null)

        val name: String get() = "$provider/$model"

        fun availableAt(now: Long): Long = cooldown.get()
            ?.takeIf { it.untilEpochMs > now }
            ?.untilEpochMs
            ?: now
    }

    override suspend fun complete(system: String, user: String): String? {
        val exhausted = mutableSetOf<Member>()
        var waitedMillis = 0L
        var lastFailure: LlmProviderException? = null
        while (true) {
            val candidates = members.filterNot { it in exhausted }
            if (candidates.isEmpty()) break
            val now = nowEpochMs()
            val ready = candidates.filter { it.availableAt(now) <= now }.inRotationOrder()
            if (ready.isEmpty()) {
                val soonest = candidates.minBy { it.availableAt(now) }
                val delayMillis = (soonest.availableAt(now) - now).coerceAtLeast(0)
                if (waitedMillis + delayMillis > maxWaitMillis) break
                waitedMillis += delayMillis
                llmLogger.warn("Every LLM provider is cooling down, waiting ${delayMillis}ms for ${soonest.name}")
                onRetry(LlmRetryWait(soonest.provider, soonest.model, delayMillis, soonest.cooldown.get()?.quota))
                wait(delayMillis)
                continue
            }
            val member = ready.firstOrNull { it.slot.tryAcquire() }
                ?: ready.first().also { it.slot.acquire() }
            var coolingDown = false
            val answer = try {
                member.completion.complete(system, user)
            } catch (rateLimit: LlmRateLimitException) {
                coolingDown = true
                member.cooldown.set(
                    Cooldown(nowEpochMs() + rateLimit.retryAfterMillis, rateLimit.quota)
                )
                lastFailure = rateLimit
                llmLogger.warn(
                    "${member.name} is rate limited, cooling it down for ${rateLimit.retryAfterMillis}ms"
                )
                null
            } catch (failure: LlmProviderException) {
                lastFailure = failure
                llmLogger.warn("${failure.message}; excluding ${member.name} from this request")
                null
            } finally {
                member.slot.release()
            }
            if (answer != null) return answer
            if (!coolingDown) exhausted += member
        }
        lastFailure?.let { throw it }
        return null
    }

    private fun List<Member>.inRotationOrder(): List<Member> {
        if (size < 2) return this
        val start = Math.floorMod(nextMemberIndex.getAndIncrement(), size)
        return drop(start) + take(start)
    }
}

internal data class Cooldown(
    val untilEpochMs: Long,
    val quota: LlmQuotaSnapshot?,
)

internal class HttpChatCompletion(
    private val provider: LlmProviderSettings,
    private val model: String,
    private val onUsage: suspend (LlmTokenUsage) -> Unit = {},
    private val onQuota: suspend (LlmQuotaSnapshot) -> Unit = {},
    private val wait: suspend (Long) -> Unit = { delay(it) },
    private val quotaTracker: LlmQuotaTracker = llmQuotaTracker,
    private val nowEpochMs: () -> Long = System::currentTimeMillis,
    private val client: HttpClient = llmHttpClient,
) : ChatCompletion {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun complete(system: String, user: String): String? = withContext(Dispatchers.IO) {
        val payload = buildJsonObject {
            put("model", model)
            put("max_tokens", MAX_LLM_COMPLETION_TOKENS)
            put("messages", buildJsonArray {
                add(message("system", system))
                add(message("user", user))
            })
        }
        var failedAttempts = 0
        while (true) {
            val request = HttpRequest.newBuilder(URI.create(provider.url))
                .timeout(Duration.ofSeconds(120))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer ${provider.key}")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), Charsets.UTF_8))
                .build()
            val response = runCatching {
                client.send(request, HttpResponse.BodyHandlers.ofString(Charsets.UTF_8))
            }.onFailure {
                llmLogger.warn("LLM call failed: ${it.message}")
            }.getOrNull()
            if (response == null) {
                failedAttempts += 1
                if (failedAttempts >= MAX_LLM_REQUEST_ATTEMPTS) return@withContext null
                continue
            }
            if (response.statusCode() == 429) {
                val reportedRetry = response.retryDelay()
                val quota = response.reportedQuota()?.let { reported ->
                    quotaTracker.recordLimit(
                        provider = provider.name,
                        model = model,
                        type = reported.type,
                        limit = reported.limit,
                        retryDelayMillis = reportedRetry,
                    )
                }
                quota?.let { onQuota(it) }
                val retryAfterMillis = quota?.takeIf { it.type.isDailyQuota }
                    ?.let { maxOf(reportedRetry, it.resetAtEpochMs - nowEpochMs()) }
                    ?: reportedRetry
                throw LlmRateLimitException(provider.name, model, retryAfterMillis, quota)
            }
            if (response.statusCode() in 500..599) {
                val message = response.errorMessage()
                failedAttempts += 1
                if (failedAttempts < MAX_LLM_UNAVAILABLE_ATTEMPTS) {
                    val waitMillis = UNAVAILABLE_RETRY_DELAY_MILLIS * failedAttempts
                    llmLogger.warn(
                        "${provider.name}/$model temporarily unavailable: $message; retrying in ${waitMillis}ms"
                    )
                    wait(waitMillis)
                    continue
                }
                throw LlmProviderException("${provider.name}/$model is unavailable: $message")
            }
            if (response.statusCode() !in 200..299) {
                throw LlmProviderException(
                    "${provider.name}/$model answered ${response.statusCode()}: ${response.errorMessage()}"
                )
            }
            val responseObject = json.parseToJsonElement(response.body()).jsonObject
            responseObject.tokenUsageOrNull()?.let { usage ->
                val quota = quotaTracker.recordSuccess(provider.name, model, usage.input)
                onUsage(usage.copy(quota = quota))
            }
            val choice = responseObject["choices"]?.jsonArray?.firstOrNull()?.jsonObject
                ?: throw LlmProviderException("${provider.name}/$model returned no choices")
            return@withContext choice["message"]?.jsonObject?.get("content")
                ?.jsonPrimitive?.contentOrNull
                ?: throw LlmProviderException(
                    "${provider.name}/$model returned no text (${choice["finish_reason"]?.jsonPrimitive?.contentOrNull ?: "unknown reason"})"
                )
        }
        @Suppress("UNREACHABLE_CODE")
        return@withContext null
    }

    private fun message(role: String, content: String) = buildJsonObject {
        put("role", role)
        put("content", content)
    }

    private fun HttpResponse<String>.retryDelay(): Long {
        val headerSeconds = headers().firstValue("Retry-After").orElse(null)?.toDoubleOrNull()
        val responseBody = body()
        val bodyDelay = RETRY_INFO_DELAY_PATTERN.find(responseBody)?.groupValues?.get(1)?.retryDurationMillis()
            ?: RETRY_MESSAGE_DELAY_PATTERN.find(responseBody)?.groupValues?.get(1)?.retryDurationMillis()
        val value = headerSeconds?.times(1_000)?.toLong() ?: bodyDelay ?: DEFAULT_RETRY_DELAY_MILLIS
        return value.coerceIn(MIN_RETRY_DELAY_MILLIS, MAX_REPORTED_RETRY_DELAY_MILLIS) + RETRY_DELAY_BUFFER_MILLIS
    }

    private fun HttpResponse<String>.errorMessage(): String =
        ERROR_MESSAGE_PATTERN.find(body())?.groupValues?.get(1)
            ?: body().take(MAX_ERROR_MESSAGE_LENGTH).ifBlank { "empty response" }

    private fun HttpResponse<String>.reportedQuota(): ReportedQuota? {
        val responseObject = runCatching { json.parseToJsonElement(body()).jsonObject }.getOrNull() ?: return null
        val details = runCatching {
            responseObject["error"]?.jsonObject?.get("details")?.jsonArray
        }.getOrNull() ?: return null
        return details.flatMap { detail ->
            runCatching { detail.jsonObject["violations"]?.jsonArray.orEmpty() }.getOrDefault(emptyList())
        }.mapNotNull { violation ->
            val value = runCatching { violation.jsonObject }.getOrNull() ?: return@mapNotNull null
            val quotaId = value["quotaId"]?.jsonPrimitive?.contentOrNull.orEmpty()
            val metric = value["quotaMetric"]?.jsonPrimitive?.contentOrNull.orEmpty()
            val limit = value["quotaValue"]?.jsonPrimitive?.longOrNull ?: return@mapNotNull null
            ReportedQuota(quotaType("$quotaId $metric"), limit)
        }.maxByOrNull { it.type.priority }
    }

    private fun JsonObject.tokenUsageOrNull(): LlmTokenUsage? {
        val usage = runCatching { get("usage")?.jsonObject }.getOrNull() ?: return null
        val input = usage["prompt_tokens"]?.jsonPrimitive?.longOrNull ?: 0
        val output = usage["completion_tokens"]?.jsonPrimitive?.longOrNull ?: 0
        val total = usage["total_tokens"]?.jsonPrimitive?.longOrNull ?: input + output
        return LlmTokenUsage(input, output, total).takeIf { it.total > 0 }
    }
}

private data class ReportedQuota(
    val type: GenerationQuotaType,
    val limit: Long,
)

internal open class LlmProviderException(message: String) : IllegalStateException(message)

internal class LlmRateLimitException(
    provider: String,
    model: String,
    val retryAfterMillis: Long,
    val quota: LlmQuotaSnapshot? = null,
) : LlmProviderException(
    "LLM rate limit reached for $provider/$model. Try again in ${retryAfterMillis.retryDelayText()}",
)

internal class LlmTextGenerator(
    private val chat: ChatCompletion = LlmSettings.textChat(),
    private val batchSize: Int = 24,
) {

    private val json = Json { ignoreUnknownKeys = true }

    fun workUnits(
        deckSizes: Collection<Int>,
        professionCount: Int,
        dreamCount: Int,
    ): Int = deckSizes.sumOf(::batches) + batches(professionCount) + batches(dreamCount)

    suspend fun generateComplete(
        world: BoardGeneration,
        cards: Map<BoardCardType, Map<Int, BoardCard>>,
        professions: List<ProfessionCard>,
        dreams: List<Dream> = emptyList(),
        existingTexts: Map<String, GeneratedText> = emptyMap(),
        shareNames: Map<String, String> = emptyMap(),
        onCheckpoint: suspend (
            texts: Map<String, GeneratedText>,
            completed: Int,
            total: Int,
            detail: String,
        ) -> Unit = { _, _, _, _ -> },
    ): Map<String, GeneratedText> {
        val store = Texts(existingTexts)
        val cardBatches = cards.mapValues { (type, deck) ->
            val nameBearingIds = deck.filterValues { it.usesGeneratedName() }.keys
            deck.entries.sortedBy { it.key }.chunked(batchSize).mapIndexed { index, items ->
                CardBatch(type, index, batches(deck.size), items, deck, nameBearingIds)
            }
        }
        val professionBatches = professions.chunked(batchSize).mapIndexed { index, items ->
            ProfessionBatch(index, batches(professions.size), items)
        }
        val dreamBatches = dreams.withIndex().toList().chunked(batchSize).mapIndexed { index, items ->
            DreamBatch(index, batches(dreams.size), items)
        }
        val restored = store.snapshot()
        val total = cardBatches.values.sumOf { it.size } + professionBatches.size + dreamBatches.size
        val done = cardBatches.values.flatten()
            .count { batch -> restored.hasCards(batch.type, batch.items) } +
                professionBatches.count { batch -> restored.hasProfessions(batch.items.map { it.id }) } +
                dreamBatches.count { batch -> restored.hasDreams(batch.items.map { it.value.id }) }
        val progress = Progress(total, done, store, onCheckpoint)
        progress.report("")

        coroutineScope {
            val deckJobs = cardBatches.values.map { batchesOfDeck ->
                async { batchesOfDeck.forEach { generateCardBatch(world, it, shareNames, store, progress) } }
            }
            val professionJob = async {
                professionBatches.forEach { generateProfessionBatch(world, it, store, progress) }
            }
            val dreamJob = async {
                dreamBatches.forEach { generateDreamBatch(world, it, store, progress) }
            }
            (deckJobs + professionJob + dreamJob).awaitAll()
        }

        val generated = store.snapshot()
        generatedLocales.forEach { locale ->
            validateComplete(locale, generated.getValue(locale), cards, professions, dreams)
        }
        progress.finish()
        return generated
    }

    private suspend fun generateCardBatch(
        world: BoardGeneration,
        batch: CardBatch,
        shareNames: Map<String, String>,
        store: Texts,
        progress: Progress,
    ) {
        val ids = batch.items.map { it.key }
        if (store.snapshot().hasCards(batch.type, batch.items)) return
        val detail = "${batch.type.name} ${batch.index + 1}/${batch.count}"
        val rejected = mutableMapOf<Int, MutableList<Map<String, CardText>>>()

        repeat(MAX_TEXT_BATCH_ATTEMPTS) { attempt ->
            val current = store.snapshot()
            val missing = batch.items.filterNot { current.hasCard(batch.type, it.key, it.value) }
            if (missing.isEmpty()) return@repeat
            val expectedIds = missing.map { it.key }.toSet()
            val answer = chat.complete(
                system = systemPrompt(),
                user = deckPrompt(
                    world = world,
                    type = batch.type,
                    briefs = missing.joinToString("\n") { (id, card) -> "$id. ${card.brief(shareNames)}" },
                    acceptedNames = current.cardNameContext(batch.type, batch.nameBearingIds),
                    attempt = attempt,
                    rejected = rejected.filterKeys { it in expectedIds },
                ),
            ) ?: return@repeat
            val candidates = answer.parseLocalizedItems()
            val updated = store.acceptCards(
                batch.type,
                candidates,
                expectedIds,
                batch.cardsById,
                batch.nameBearingIds,
            )
            missing.filterNot { updated.hasCard(batch.type, it.key, it.value) }.forEach { (id) ->
                candidates[id]?.let { candidate -> rejected.getOrPut(id, ::mutableListOf) += candidate }
            }
            if (!updated.hasCards(batch.type, batch.items)) progress.report(detail)
        }

        repeat(MAX_REPAIR_BATCH_ATTEMPTS) { repairAttempt ->
            val missing = store.snapshot().let { current ->
                batch.items.filterNot { current.hasCard(batch.type, it.key, it.value) }
            }
            if (missing.isEmpty()) return@repeat
            missing.chunked(REPAIR_BATCH_SIZE).forEach repairBatchLoop@{ repairBatch ->
                val expectedIds = repairBatch.map { it.key }.toSet()
                val answer = chat.complete(
                    system = systemPrompt(),
                    user = deckPrompt(
                        world = world,
                        type = batch.type,
                        briefs = repairBatch.joinToString("\n") { (id, card) -> "$id. ${card.brief(shareNames)}" },
                        acceptedNames = store.snapshot().cardNameContext(batch.type, batch.nameBearingIds),
                        attempt = MAX_TEXT_BATCH_ATTEMPTS + repairAttempt,
                        rejected = rejected.filterKeys { it in expectedIds },
                    ),
                ) ?: return@repairBatchLoop
                val candidates = answer.parseLocalizedItems(expectedSingleId = expectedIds.singleOrNull())
                val updated = store.acceptCards(
                    batch.type,
                    candidates,
                    expectedIds,
                    batch.cardsById,
                    batch.nameBearingIds,
                )
                repairBatch.filterNot { updated.hasCard(batch.type, it.key, it.value) }.forEach { (id) ->
                    candidates[id]?.let { candidate -> rejected.getOrPut(id, ::mutableListOf) += candidate }
                }
                progress.report(detail)
            }
        }

        val stillMissing = store.snapshot().let { current ->
            batch.items.filterNot { current.hasCard(batch.type, it.key, it.value) }
        }
        stillMissing.forEach { missingCard ->
            val id = missingCard.key
            repeat(MAX_SINGLE_ITEM_ATTEMPTS) { repairAttempt ->
                if (store.snapshot().hasCard(batch.type, id, missingCard.value)) return@repeat
                val answer = chat.complete(
                    system = systemPrompt(),
                    user = deckPrompt(
                        world = world,
                        type = batch.type,
                        briefs = "$id. ${missingCard.value.brief(shareNames)}",
                        acceptedNames = store.snapshot().cardNameContext(batch.type, batch.nameBearingIds),
                        attempt = MAX_TEXT_BATCH_ATTEMPTS + repairAttempt,
                        rejected = rejected.filterKeys { it == id },
                    ),
                ) ?: return@repeat
                val candidates = answer.parseLocalizedItems(expectedSingleId = id)
                val updated = store.acceptCards(
                    batch.type,
                    candidates,
                    setOf(id),
                    batch.cardsById,
                    batch.nameBearingIds,
                )
                if (!updated.hasCard(batch.type, id, missingCard.value)) {
                    candidates[id]?.let { candidate ->
                        rejected.getOrPut(id, ::mutableListOf) += candidate
                        llmLogger.warn("Rejected duplicate localized card text for ${batch.type} $id")
                    } ?: llmLogger.warn(
                        "LLM repair response has no usable localized card text for ${batch.type} $id"
                    )
                }
                progress.report(detail)
            }
        }

        val missingIds = store.snapshot().let { current ->
            batch.items.filterNot { current.hasCard(batch.type, it.key, it.value) }.map { it.key }
        }
        check(missingIds.isEmpty()) {
            "Incomplete uk/en texts for ${batch.type}: ${missingIds.take(MAX_ERROR_IDS)}"
        }
        progress.complete(detail)
    }

    private suspend fun generateProfessionBatch(
        world: BoardGeneration,
        batch: ProfessionBatch,
        store: Texts,
        progress: Progress,
    ) {
        val ids = batch.items.map { it.id }
        if (store.snapshot().hasProfessions(ids)) return
        val detail = "professions ${batch.index + 1}/${batch.count}"
        val rejected = mutableMapOf<Int, MutableList<Map<String, CardText>>>()

        repeat(MAX_TEXT_BATCH_ATTEMPTS) { attempt ->
            val current = store.snapshot()
            val missing = batch.items.filterNot { current.hasProfession(it.id) }
            if (missing.isEmpty()) return@repeat
            val expectedIds = missing.map { it.id }.toSet()
            val answer = chat.complete(
                system = systemPrompt(),
                user = professionPrompt(
                    world = world,
                    briefs = missing.joinToString("\n", transform = ::professionBrief),
                    acceptedNames = current.professionNameContext(),
                    attempt = attempt,
                    rejected = rejected.filterKeys { it in expectedIds },
                ),
            ) ?: return@repeat
            val candidates = answer.parseLocalizedItems()
            val updated = store.acceptProfessions(candidates, expectedIds)
            missing.filterNot { updated.hasProfession(it.id) }.forEach { profession ->
                candidates[profession.id]?.let { candidate ->
                    rejected.getOrPut(profession.id, ::mutableListOf) += candidate
                }
            }
            if (!updated.hasProfessions(ids)) progress.report(detail)
        }

        repeat(MAX_REPAIR_BATCH_ATTEMPTS) { repairAttempt ->
            val missing = store.snapshot().let { current ->
                batch.items.filterNot { current.hasProfession(it.id) }
            }
            if (missing.isEmpty()) return@repeat
            missing.chunked(REPAIR_BATCH_SIZE).forEach repairBatchLoop@{ repairBatch ->
                val expectedIds = repairBatch.map { it.id }.toSet()
                val answer = chat.complete(
                    system = systemPrompt(),
                    user = professionPrompt(
                        world = world,
                        briefs = repairBatch.joinToString("\n", transform = ::professionBrief),
                        acceptedNames = store.snapshot().professionNameContext(),
                        attempt = MAX_TEXT_BATCH_ATTEMPTS + repairAttempt,
                        rejected = rejected.filterKeys { it in expectedIds },
                    ),
                ) ?: return@repairBatchLoop
                val candidates = answer.parseLocalizedItems(expectedSingleId = expectedIds.singleOrNull())
                val updated = store.acceptProfessions(candidates, expectedIds)
                repairBatch.filterNot { updated.hasProfession(it.id) }.forEach { profession ->
                    candidates[profession.id]?.let { candidate ->
                        rejected.getOrPut(profession.id, ::mutableListOf) += candidate
                    }
                }
                progress.report(detail)
            }
        }

        val stillMissing = store.snapshot().let { current ->
            batch.items.filterNot { current.hasProfession(it.id) }
        }
        stillMissing.forEach { missingProfession ->
            val id = missingProfession.id
            repeat(MAX_SINGLE_ITEM_ATTEMPTS) { repairAttempt ->
                if (store.snapshot().hasProfession(id)) return@repeat
                val answer = chat.complete(
                    system = systemPrompt(),
                    user = professionPrompt(
                        world = world,
                        briefs = professionBrief(missingProfession),
                        acceptedNames = store.snapshot().professionNameContext(),
                        attempt = MAX_TEXT_BATCH_ATTEMPTS + repairAttempt,
                        rejected = rejected.filterKeys { it == id },
                    ),
                ) ?: return@repeat
                val candidates = answer.parseLocalizedItems(expectedSingleId = id)
                val updated = store.acceptProfessions(candidates, setOf(id))
                if (!updated.hasProfession(id)) {
                    candidates[id]?.let { candidate ->
                        rejected.getOrPut(id, ::mutableListOf) += candidate
                        llmLogger.warn("Rejected duplicate localized profession $id")
                    } ?: llmLogger.warn("LLM repair response has no usable localized profession $id")
                }
                progress.report(detail)
            }
        }

        val missingIds = store.snapshot().let { current -> ids.filterNot { current.hasProfession(it) } }
        check(missingIds.isEmpty()) { "Incomplete uk/en professions: ${missingIds.take(MAX_ERROR_IDS)}" }
        progress.complete(detail)
    }

    private suspend fun generateDreamBatch(
        world: BoardGeneration,
        batch: DreamBatch,
        store: Texts,
        progress: Progress,
    ) {
        val ids = batch.items.map { it.value.id }
        if (store.snapshot().hasDreams(ids)) return
        val detail = "dreams ${batch.index + 1}/${batch.count}"
        val slots = batch.items.associate { (index, dream) -> index + 1 to dream }
        val rejected = mutableMapOf<Int, MutableList<Map<String, CardText>>>()

        repeat(MAX_TEXT_BATCH_ATTEMPTS + MAX_REPAIR_BATCH_ATTEMPTS) { attempt ->
            val current = store.snapshot()
            val missing = slots.filterValues { !current.hasDream(it.id) }
            if (missing.isEmpty()) return@repeat
            val answer = chat.complete(
                system = systemPrompt(),
                user = dreamPrompt(
                    world = world,
                    briefs = missing.entries.joinToString("\n") { (slot, dream) ->
                        "$slot. мрія вартістю ${dream.price}"
                    },
                    acceptedNames = current.dreamNameContext(),
                    attempt = attempt,
                    rejected = rejected.filterKeys { it in missing.keys },
                ),
            ) ?: return@repeat
            val candidates = answer.parseLocalizedItems(expectedSingleId = missing.keys.singleOrNull())
            val updated = store.acceptDreams(candidates, slots)
            missing.filterValues { !updated.hasDream(it.id) }.forEach { (slot) ->
                candidates[slot]?.let { candidate -> rejected.getOrPut(slot, ::mutableListOf) += candidate }
            }
            progress.report(detail)
        }

        val missingIds = store.snapshot().let { current -> ids.filterNot { current.hasDream(it) } }
        check(missingIds.isEmpty()) { "Incomplete uk/en dreams: ${missingIds.take(MAX_ERROR_IDS)}" }
        progress.complete(detail)
    }

    private fun professionBrief(card: ProfessionCard): String =
        "${card.id}. ${if (card.gender == Gender.FEMALE) "жінка" else "чоловік"}, зарплата ${card.salary}"

    private inner class Texts(existing: Map<String, GeneratedText>) {
        private val lock = Mutex()
        private var texts: Map<String, GeneratedText> =
            generatedLocales.associateWith { existing[it] ?: GeneratedText() }

        suspend fun snapshot(): Map<String, GeneratedText> = lock.withLock { texts }

        suspend fun acceptCards(
            type: BoardCardType,
            candidates: Map<Int, Map<String, CardText>>,
            allowedIds: Set<Int>,
            cardsById: Map<Int, BoardCard>,
            nameBearingIds: Set<Int>,
        ): Map<String, GeneratedText> = lock.withLock {
            texts = texts.toMutableMap().apply {
                acceptCards(type, candidates, allowedIds, cardsById, nameBearingIds)
            }
            texts
        }

        suspend fun acceptProfessions(
            candidates: Map<Int, Map<String, CardText>>,
            allowedIds: Set<Int>,
        ): Map<String, GeneratedText> = lock.withLock {
            texts = texts.toMutableMap().apply { acceptProfessions(candidates, allowedIds) }
            texts
        }

        suspend fun acceptDreams(
            candidates: Map<Int, Map<String, CardText>>,
            slots: Map<Int, Dream>,
        ): Map<String, GeneratedText> = lock.withLock {
            texts = texts.toMutableMap().apply { acceptDreams(candidates, slots) }
            texts
        }
    }

    private inner class Progress(
        private val total: Int,
        completedBatches: Int,
        private val store: Texts,
        private val onCheckpoint: suspend (Map<String, GeneratedText>, Int, Int, String) -> Unit,
    ) {
        private val completed = AtomicInteger(completedBatches)
        private val lock = Mutex()

        suspend fun report(detail: String) = lock.withLock {
            onCheckpoint(store.snapshot(), completed.get(), total, detail)
        }

        suspend fun complete(detail: String) {
            completed.incrementAndGet()
            report(detail)
        }

        suspend fun finish() = lock.withLock {
            onCheckpoint(store.snapshot(), total, total, "")
        }
    }

    private fun validateComplete(
        locale: String,
        generated: GeneratedText,
        cards: Map<BoardCardType, Map<Int, BoardCard>>,
        professions: List<ProfessionCard>,
        dreams: List<Dream>,
    ) {
        cards.forEach { (type, deck) ->
            val texts = generated.cards[type].orEmpty()
            check(texts.keys == deck.keys) { "Incomplete $locale texts for $type" }
            check(texts.values.all { it.name.isNotBlank() }) { "Empty $locale name for $type" }
            check(texts.values.all { it.description.isNotBlank() }) { "Empty $locale description for $type" }
            val namedTexts = texts.filterKeys { deck.getValue(it).usesGeneratedName() }
            check(namedTexts.values.map { it.name.normalized() }.distinct().size == namedTexts.size) {
                "Repeated $locale names for $type"
            }
            val repeatedDescriptions = texts.entries.groupBy { it.value.description.normalized() }
                .values
                .filter { it.size > 1 }
            check(repeatedDescriptions.all { repeated ->
                repeated.map { (id) -> deck.getValue(id) }.distinct().size == 1
            }) {
                "Repeated $locale descriptions for different $type cards"
            }
        }
        val localizedCards = generated.cards.flatMap { (type, deck) ->
            deck.map { (id, text) -> Triple(type, id, text.description.normalized()) }
        }
        val repeatedDescriptions = localizedCards.groupBy { it.third }.values.filter { it.size > 1 }
        check(repeatedDescriptions.all { repeated ->
            repeated.map { (type, id) -> cards.getValue(type).getValue(id) }.distinct().size == 1
        }) { "Repeated $locale descriptions for different cards" }
        check(generated.professions.keys == professions.map { it.id }.toSet()) {
            "Incomplete $locale profession names"
        }
        check(generated.professionDescriptions.keys == professions.map { it.id }.toSet()) {
            "Incomplete $locale profession descriptions"
        }
        check(generated.professions.values.all { it.isNotBlank() }) { "Empty $locale profession name" }
        check(generated.professionDescriptions.values.all { it.isNotBlank() }) {
            "Empty $locale profession description"
        }
        check(generated.professions.values.map { it.lowercase() }.distinct().size == professions.size) {
            "Repeated $locale profession names"
        }
        check(generated.professionDescriptions.values.map { it.lowercase() }.distinct().size == professions.size) {
            "Repeated $locale profession descriptions"
        }
        check(generated.dreams.keys == dreams.map { it.id }.toSet()) { "Incomplete $locale dreams" }
        check(generated.dreams.values.all { it.isUsable() }) { "Empty $locale dream text" }
        check(generated.dreams.values.map { it.name.normalized() }.distinct().size == dreams.size) {
            "Repeated $locale dream names"
        }
        check(generated.dreams.values.map { it.description.normalized() }.distinct().size == dreams.size) {
            "Repeated $locale dream descriptions"
        }
    }

    private fun systemPrompt() = buildString {
        append("Ти пишеш тексти карток для настільної економічної гри. ")
        append("Відповідай виключно JSON-масивом об'єктів ")
        append("{\"id\":число,\"uk\":{\"name\":рядок,\"description\":рядок},")
        append("\"en\":{\"name\":рядок,\"description\":рядок}}, ")
        append("без пояснень і без markdown. ")
        append("Для кожного ID обов'язково поверни природні українські та англійські тексти.")
    }

    private fun deckPrompt(
        world: BoardGeneration,
        type: BoardCardType,
        briefs: String,
        acceptedNames: List<String>,
        attempt: Int,
        rejected: Map<Int, List<Map<String, CardText>>>,
    ) = buildString {
        append(worldPrompt(world))
        append("Створи унікальну назву та унікальний опис (до 140 символів) для кожної картки колоди «${deckName(type)}». ")
        append("У цій грі доречний легкий дотепний гумор, пов'язаний зі світом і ситуацією картки. ")
        append("Name відображається гравцю як головний заголовок, тому він має самостійно й конкретно називати об'єкт, персонажа або подію, а не лише тип колоди. ")
        append("Не повторюй сюжети, назви чи описи. ")
        when (type) {
            BoardCardType.SmallBusiness,
            BoardCardType.MediumBusiness,
            BoardCardType.BigBusiness -> {
                append("Назва конкретно називає бізнес, а description пояснює, чим він займається і звідки отримує дохід у цьому світі. ")
                append("Ціну й дохід інтерфейс показує окремо; не роби опис лише переказом цих чисел. ")
            }

            BoardCardType.Shopping -> {
                append("Назва конкретно називає придбання, а description пояснює, що саме купують і навіщо воно потрібне у цьому світі. ")
                append("Тип активу й ціну інтерфейс показує окремо; не підміняй ними сюжет покупки. ")
            }

            BoardCardType.Expenses -> {
                append("Для витрат name коротко й конкретно називає предмет оплати, послугу або подію. ")
                append("Description пояснює, що сталося і за що сплачують. ")
                append("Сума та умова платника вже показані на картці окремо: не повторюй їх і не замінюй ними причину витрати. ")
                append("Для картки з твариною обов'язково поясни порятунок або прихисток тварини та подальше утримання. ")
            }

            BoardCardType.Chance -> {
                append("Назва конкретно називає можливість, підробіток, актив або корупційну схему. ")
                append("Description спочатку пояснює ситуацію, а потім природно передає точний ефект із рядка. ")
            }

            BoardCardType.EventStore -> {
                append("Назва є конкретним заголовком ринкової новини, а description пояснює її причину та точний вплив на гравців або активи. ")
            }

            BoardCardType.Deputy -> {
                append("Назва конкретно називає посадовця, роль або скандал, а description пояснює ситуацію та чесний чи корупційний наслідок. ")
            }
        }
        append("Механіка в кожному рядку є точною й обов'язковою: не додавай нових чисел, умов або наслідків. ")
        append("Поверни рівно по одному об'єкту для кожного переданого ID. ")
        append("Поля uk та en мають передавати той самий зміст відповідними мовами.\n")
        appendAcceptedNames(acceptedNames)
        appendRetryInstructions(attempt, rejected)
        append("Картки:\n")
        append(briefs)
    }

    private fun professionPrompt(
        world: BoardGeneration,
        briefs: String,
        acceptedNames: List<String>,
        attempt: Int,
        rejected: Map<Int, List<Map<String, CardText>>>,
    ) = buildString {
        append(worldPrompt(world))
        append("Створи унікальну назву й опис професії для кожного рядка. ")
        append("У цій грі доречний легкий дотепний гумор, пов'язаний із професією та світом. ")
        append("Name відображається як заголовок картки й конкретно називає професію або посаду, а не категорію «Професія». ")
        append("Назва має відповідати статі, світу та розміру зарплати. ")
        append("Стать є ігровою ознакою: вона впливає на шлюб, дітей, розлучення й умовні витрати, тому не роби професію гендерно суперечливою. ")
        append("Опис до 140 символів має пояснювати щоденну роль і джерело зарплати у вказаних темі, місцевості й епосі. ")
        append("Не повторюй назви чи описи. ")
        append("Поверни рівно по одному об'єкту для кожного переданого ID. ")
        append("Поля uk та en мають передавати ту саму професію відповідними мовами.\n")
        appendAcceptedNames(acceptedNames)
        appendRetryInstructions(attempt, rejected)
        append("Професії:\n")
        append(briefs)
    }

    private fun dreamPrompt(
        world: BoardGeneration,
        briefs: String,
        acceptedNames: List<String>,
        attempt: Int,
        rejected: Map<Int, List<Map<String, CardText>>>,
    ) = buildString {
        append(worldPrompt(world))
        append("Створи унікальну назву та опис великої мрії для кожного рядка. ")
        append("Мрія — це не актив і не бізнес, а те, заради чого гравець виходить із щурячих перегонів: ")
        append("вчинок, місце або справа життя, доречні цьому світу. ")
        append("Name відображається як заголовок і конкретно називає мету, а не категорію «Мрія». ")
        append("Ціна показує масштаб мрії: чим дорожча, тим величніша. ")
        append("Опис до 140 символів пояснює, що саме гравець здійснює або створює, не повторюючи ціну. ")
        append("Не повторюй назви чи описи. ")
        append("Поверни рівно по одному об'єкту для кожного переданого ID. ")
        append("Поля uk та en мають передавати ту саму мрію відповідними мовами.\n")
        appendAcceptedNames(acceptedNames)
        appendRetryInstructions(attempt, rejected)
        append("Мрії:\n")
        append(briefs)
    }

    private fun StringBuilder.appendAcceptedNames(names: List<String>) {
        if (names.isEmpty()) return
        append("Уже використані назви, які не можна повторювати:\n")
        names.forEach { name -> append("- $name\n") }
    }

    private fun StringBuilder.appendRetryInstructions(
        attempt: Int,
        rejected: Map<Int, List<Map<String, CardText>>>,
    ) {
        if (attempt == 0) return
        append("Це повторна спроба: попередня відповідь не пройшла перевірку або не містила всі ID. ")
        append("Поверни всі передані нижче ID й створи для них нові варіанти. ")
        append("Скопіюй кожен числовий ID без змін; не замінюй його на 1 і не пропускай поля uk/en, name або description.\n")
        if (rejected.isEmpty()) return
        append("Не повторюй ці відхилені назви й описи:\n")
        rejected.forEach { (id, attempts) ->
            attempts.takeLast(MAX_REJECTED_TEXTS_IN_PROMPT).forEach { localized ->
                append("$id: ")
                append(generatedLocales.joinToString("; ") { locale ->
                    val text = localized.getValue(locale)
                    "$locale «${text.name}» — «${text.description}»"
                })
                append('\n')
            }
        }
    }

    private fun worldPrompt(world: BoardGeneration) = buildString {
        append("Світ гри: ")
        append(listOfNotNull(
            world.theme.ifBlank { null }?.let { "тематика — $it" },
            world.locality.ifBlank { null }?.let { "місцевість — $it" },
            world.epoch.ifBlank { null }?.let { "епоха — $it" },
        ).joinToString(", ").ifBlank { "звичайне сучасне місто" })
        append(".\n")
    }

    private fun String.parseLocalizedItems(expectedSingleId: Int? = null): Map<Int, Map<String, CardText>> {
        val items = completeJsonObjects()
        if (items.isEmpty()) llmLogger.warn("LLM answer contains no complete JSON items")
        val localizedItems = items.mapNotNull { item ->
            item.localizedTextOrNull()?.let { localized -> item to localized }
        }
        val parsed = localizedItems.mapNotNull { (item, localized) ->
            val id = item["id"]?.jsonPrimitive?.let { value ->
                value.intOrNull ?: value.contentOrNull?.toIntOrNull()
            } ?: return@mapNotNull null
            id to localized
        }.toMap()
        if (expectedSingleId == null || expectedSingleId in parsed || localizedItems.size != 1) return parsed
        val localized = localizedItems.single().second
        return parsed + (expectedSingleId to localized)
    }

    private fun String.completeJsonObjects(): List<JsonObject> {
        val arrayStart = indexOf('[')
        val scanStart = if (arrayStart >= 0) arrayStart + 1 else 0
        val objects = mutableListOf<JsonObject>()
        val objectStarts = mutableListOf<Int>()
        var insideString = false
        var escaped = false
        for (index in scanStart until length) {
            val character = this[index]
            if (insideString) {
                when {
                    escaped -> escaped = false
                    character == '\\' -> escaped = true
                    character == '"' -> insideString = false
                }
                continue
            }
            when (character) {
                '"' -> insideString = true
                '{' -> objectStarts += index
                '}' -> if (objectStarts.isNotEmpty()) {
                    val objectStart = objectStarts.removeLast()
                    runCatching { json.parseToJsonElement(substring(objectStart, index + 1)) as? JsonObject }
                        .onFailure { llmLogger.warn("LLM answered an invalid JSON item: ${it.message}") }
                        .getOrNull()
                        ?.let(objects::add)
                }
                ']' -> if (arrayStart >= 0 && objectStarts.isEmpty()) break
            }
        }
        return objects
    }

    private fun JsonObject.localizedTextOrNull(): Map<String, CardText>? {
        val localized = mutableMapOf<String, CardText>()
        generatedLocales.forEach { locale ->
            val item = this[locale] as? JsonObject ?: return null
            val text = CardText(
                name = item["name"]?.jsonPrimitive?.contentOrNull.orEmpty().trim(),
                description = item["description"]?.jsonPrimitive?.contentOrNull.orEmpty().trim(),
            )
            if (!text.isUsable()) return null
            localized[locale] = text
        }
        return localized
    }

    private fun MutableMap<String, GeneratedText>.acceptCards(
        type: BoardCardType,
        candidates: Map<Int, Map<String, CardText>>,
        allowedIds: Set<Int>,
        cardsById: Map<Int, BoardCard>,
        nameBearingIds: Set<Int>,
    ) {
        candidates.forEach { (id, localized) ->
            if (id !in allowedIds) return@forEach
            val card = cardsById[id] ?: return@forEach
            val suitable = generatedLocales.all { locale ->
                localized.getValue(locale).isSuitableFor(card, locale)
            }
            if (!suitable) return@forEach
            val unique = generatedLocales.all { locale ->
                localized.getValue(locale).isUniqueCard(
                    this,
                    locale,
                    type,
                    id,
                    cardsById,
                    nameBearingIds,
                )
            }
            if (!unique) return@forEach
            generatedLocales.forEach { locale -> putCard(locale, type, id, localized.getValue(locale)) }
        }
    }

    private fun MutableMap<String, GeneratedText>.acceptDreams(
        candidates: Map<Int, Map<String, CardText>>,
        slots: Map<Int, Dream>,
    ) {
        candidates.forEach { (slot, localized) ->
            val dream = slots[slot] ?: return@forEach
            val unique = generatedLocales.all { locale ->
                localized.getValue(locale).isUniqueDream(this, locale, dream.id)
            }
            if (!unique) return@forEach
            generatedLocales.forEach { locale ->
                val generated = getValue(locale)
                this[locale] = generated.copy(dreams = generated.dreams + (dream.id to localized.getValue(locale)))
            }
        }
    }

    private fun CardText.isUniqueDream(
        texts: Map<String, GeneratedText>,
        locale: String,
        id: String,
    ): Boolean = texts.getValue(locale).dreams.none { (existingId, existing) ->
        existingId != id && existing.collidesWith(this)
    }

    private fun MutableMap<String, GeneratedText>.acceptProfessions(
        candidates: Map<Int, Map<String, CardText>>,
        allowedIds: Set<Int>,
    ) {
        candidates.forEach { (id, localized) ->
            if (id !in allowedIds) return@forEach
            val unique = generatedLocales.all { locale ->
                localized.getValue(locale).isUniqueProfession(this, locale, id)
            }
            if (!unique) return@forEach
            generatedLocales.forEach { locale -> putProfession(locale, id, localized.getValue(locale)) }
        }
    }

    private fun CardText.isUniqueCard(
        texts: Map<String, GeneratedText>,
        locale: String,
        type: BoardCardType,
        id: Int,
        cardsById: Map<Int, BoardCard>,
        nameBearingIds: Set<Int>,
    ): Boolean {
        val decks = texts.getValue(locale).cards
        val uniqueName = id !in nameBearingIds || decks[type].orEmpty().none { (existingId, existing) ->
            existingId != id && existingId in nameBearingIds && existing.name.normalized() == name.normalized()
        }
        val uniqueDescription = decks.none { (existingType, deck) ->
            deck.any { (existingId, existing) ->
                val sameCard = existingType == type && existingId == id
                val sameMechanics = existingType == type && cardsById[existingId] == cardsById[id]
                !sameCard && !sameMechanics && existing.description.normalized() == description.normalized()
            }
        }
        return uniqueName && uniqueDescription
    }

    private fun CardText.isUniqueProfession(
        texts: Map<String, GeneratedText>,
        locale: String,
        id: Int,
    ): Boolean {
        val generated = texts.getValue(locale)
        return generated.professions.none { (existingId, name) ->
            existingId != id && name.normalized() == this.name.normalized()
        } && generated.professionDescriptions.none { (existingId, description) ->
            existingId != id && description.normalized() == this.description.normalized()
        }
    }

    private fun MutableMap<String, GeneratedText>.putCard(
        locale: String,
        type: BoardCardType,
        id: Int,
        text: CardText,
    ) {
        val generated = getValue(locale)
        this[locale] = generated.copy(
            cards = generated.cards + (type to (generated.cards[type].orEmpty() + (id to text)))
        )
    }

    private fun MutableMap<String, GeneratedText>.putProfession(locale: String, id: Int, text: CardText) {
        val generated = getValue(locale)
        this[locale] = generated.copy(
            professions = generated.professions + (id to text.name),
            professionDescriptions = generated.professionDescriptions + (id to text.description),
        )
    }

    private fun Map<String, GeneratedText>.hasCard(type: BoardCardType, id: Int, card: BoardCard): Boolean =
        generatedLocales.all { locale ->
            this[locale]?.cards?.get(type)?.get(id)?.isSuitableFor(card, locale) == true
        }

    private fun Map<String, GeneratedText>.hasCards(
        type: BoardCardType,
        cards: List<Map.Entry<Int, BoardCard>>,
    ): Boolean = cards.all { hasCard(type, it.key, it.value) }

    private fun Map<String, GeneratedText>.hasProfession(id: Int): Boolean = generatedLocales.all { locale ->
        this[locale]?.professions?.get(id).orEmpty().isNotBlank() &&
                this[locale]?.professionDescriptions?.get(id).orEmpty().isNotBlank()
    }

    private fun Map<String, GeneratedText>.hasProfessions(ids: List<Int>): Boolean = ids.all { hasProfession(it) }

    private fun Map<String, GeneratedText>.hasDream(id: String): Boolean = generatedLocales.all { locale ->
        this[locale]?.dreams?.get(id)?.isUsable() == true
    }

    private fun Map<String, GeneratedText>.hasDreams(ids: List<String>): Boolean = ids.all { hasDream(it) }

    private fun Map<String, GeneratedText>.dreamNameContext(): List<String> {
        val ids = generatedLocales.flatMap { locale -> this[locale]?.dreams?.keys.orEmpty() }.distinct().sorted()
        return ids.mapNotNull { id ->
            localizedNameContext { locale -> this[locale]?.dreams?.get(id)?.name }
        }.takeLast(MAX_ACCEPTED_PROFESSION_NAMES_IN_PROMPT)
    }

    private fun Map<String, GeneratedText>.cardNameContext(
        type: BoardCardType,
        nameBearingIds: Set<Int>,
    ): List<String> {
        val ids = generatedLocales.flatMap { locale ->
            this[locale]?.cards?.get(type)?.keys.orEmpty()
        }.distinct().filter { it in nameBearingIds }.sorted()
        return ids.mapNotNull { id ->
            localizedNameContext { locale -> this[locale]?.cards?.get(type)?.get(id)?.name }
        }.takeLast(MAX_ACCEPTED_CARD_NAMES_IN_PROMPT)
    }

    private fun Map<String, GeneratedText>.professionNameContext(): List<String> {
        val ids = generatedLocales.flatMap { locale -> this[locale]?.professions?.keys.orEmpty() }
            .distinct()
            .sorted()
        return ids.mapNotNull { id ->
            localizedNameContext { locale -> this[locale]?.professions?.get(id) }
        }.take(MAX_ACCEPTED_PROFESSION_NAMES_IN_PROMPT)
    }

    private fun localizedNameContext(name: (String) -> String?): String? {
        val localized = generatedLocales.mapNotNull { locale ->
            name(locale)?.takeIf(String::isNotBlank)?.let { "$locale «$it»" }
        }
        return localized.takeIf { it.size == generatedLocales.size }?.joinToString(" / ")
    }

    private fun batches(size: Int): Int = if (size == 0) 0 else (size + batchSize - 1) / batchSize
}

private data class CardBatch(
    val type: BoardCardType,
    val index: Int,
    val count: Int,
    val items: List<Map.Entry<Int, BoardCard>>,
    val cardsById: Map<Int, BoardCard>,
    val nameBearingIds: Set<Int>,
)

private data class ProfessionBatch(
    val index: Int,
    val count: Int,
    val items: List<ProfessionCard>,
)

private data class DreamBatch(
    val index: Int,
    val count: Int,
    val items: List<IndexedValue<Dream>>,
)

private fun CardText.isUsable(): Boolean = name.isNotBlank() && description.isNotBlank()

private fun CardText.isSuitableFor(card: BoardCard, locale: String): Boolean =
    isUsable() && hasSpecificName(locale) &&
            (card !is BoardCard.Expenses || expensePurposeWords(locale).isNotEmpty())

private fun CardText.hasSpecificName(locale: String): Boolean {
    val genericNames = if (locale.startsWith("en")) GENERIC_EN_CARD_NAMES else GENERIC_UK_CARD_NAMES
    return name.qualityNormalized() !in genericNames
}

private fun CardText.expensePurposeWords(locale: String): List<String> {
    val ignored = if (locale.startsWith("en")) EN_EXPENSE_MECHANIC_WORDS else UK_EXPENSE_MECHANIC_WORDS
    return EXPENSE_WORD_PATTERN.findAll("$name $description".lowercase().replace("'", "").replace("’", ""))
        .map { it.value }
        .filterNot { it in ignored }
        .toList()
}

private fun String.qualityNormalized(): String = lowercase()
    .replace("'", "")
    .replace("’", "")
    .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
    .trim()

private fun CardText.collidesWith(other: CardText): Boolean =
    name.normalized() == other.name.normalized() || description.normalized() == other.description.normalized()

private fun String.normalized(): String = trim().lowercase()

private fun BoardCard.usesGeneratedName(): Boolean = true

private const val MAX_LLM_REQUEST_ATTEMPTS = 8
private const val MAX_LLM_UNAVAILABLE_ATTEMPTS = 3
private const val MAX_LLM_COMPLETION_TOKENS = 6_000
private const val UNAVAILABLE_RETRY_DELAY_MILLIS = 2_000L
private const val MAX_ERROR_MESSAGE_LENGTH = 300
private const val MAX_LLM_FALLBACKS = 3
private const val MAX_POOL_WAIT_MILLIS = 5 * 60 * 1_000L
private const val MAX_TEXT_BATCH_ATTEMPTS = 3
private const val MAX_REPAIR_BATCH_ATTEMPTS = 4
private const val REPAIR_BATCH_SIZE = 4
private const val MAX_SINGLE_ITEM_ATTEMPTS = 8
private const val MAX_REJECTED_TEXTS_IN_PROMPT = 2
private const val MAX_ACCEPTED_CARD_NAMES_IN_PROMPT = 150
private const val MAX_ACCEPTED_PROFESSION_NAMES_IN_PROMPT = 100
private const val MAX_ERROR_IDS = 8
private const val DEFAULT_RETRY_DELAY_MILLIS = 3_000L
private const val MIN_RETRY_DELAY_MILLIS = 500L
private const val MAX_REPORTED_RETRY_DELAY_MILLIS = 24 * 60 * 60 * 1_000L
private const val RETRY_DELAY_BUFFER_MILLIS = 300L
private val RETRY_INFO_DELAY_PATTERN = Regex(
    "\\\"retryDelay\\\"\\s*:\\s*\\\"((?:[\\d.]+(?:ms|[hms]))+)\\\"",
    RegexOption.IGNORE_CASE,
)
private val RETRY_MESSAGE_DELAY_PATTERN = Regex(
    "(?:please\\s+)?(?:try\\s+again|retry)\\s+in\\s+((?:[\\d.]+(?:ms|[hms]))+)",
    RegexOption.IGNORE_CASE,
)
private val ERROR_MESSAGE_PATTERN = Regex("\\\"message\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"")
private val RETRY_DURATION_PART_PATTERN = Regex("([\\d.]+)(ms|[hms])", RegexOption.IGNORE_CASE)
private val EXPENSE_WORD_PATTERN = Regex("\\p{L}+")
private val UK_EXPENSE_MECHANIC_WORDS = setOf(
    "без", "винятку", "всі", "витрата", "витрати", "гравець", "гравці", "гравців", "для", "за",
    "заплатити", "кожен", "лише", "має", "обовязкова", "обовязковий", "оплата", "платіж", "платять",
    "повинен", "повинні", "сплата", "сплатити", "сплачують", "сума", "суму", "тільки", "умовою", "усі",
    "хто",
)
private val EN_EXPENSE_MECHANIC_WORDS = setOf(
    "all", "amount", "everyone", "expense", "for", "mandatory", "must", "only", "pay", "pays", "payment",
    "players", "required", "the", "those", "who", "without",
)
private val GENERIC_UK_CARD_NAMES = setOf(
    "акції", "бізнес", "великий бізнес", "випадковий заробіток", "випадок", "витрати", "депутат", "депутати",
    "земля", "корупційна земля", "корупційний бізнес", "малий бізнес", "нерухомість", "обовязкова витрата",
    "подія", "подія ринку", "покупка", "перевибори", "ринкова новина", "ринкова подія", "розширення бізнесу",
    "середній бізнес", "шанс",
)
private val GENERIC_EN_CARD_NAMES = setOf(
    "big business", "business", "chance", "corrupt business", "corrupt land", "deputies", "deputy", "event",
    "expenses", "land", "mandatory expense", "market event", "market news", "medium business", "random job",
    "real estate", "reelection", "shares", "shopping", "small business",
)

private fun quotaType(value: String): GenerationQuotaType {
    val normalized = value.lowercase().filter(Char::isLetterOrDigit)
    return when {
        "inputtoken" in normalized && "perday" in normalized -> GenerationQuotaType.INPUT_TOKENS_PER_DAY
        "request" in normalized && "perday" in normalized -> GenerationQuotaType.REQUESTS_PER_DAY
        "inputtoken" in normalized && "perminute" in normalized -> GenerationQuotaType.INPUT_TOKENS_PER_MINUTE
        "request" in normalized && "perminute" in normalized -> GenerationQuotaType.REQUESTS_PER_MINUTE
        "spend" in normalized -> GenerationQuotaType.SPEND_PER_TEN_MINUTES
        else -> GenerationQuotaType.UNKNOWN
    }
}

private val GenerationQuotaType.priority: Int
    get() = when (this) {
        GenerationQuotaType.REQUESTS_PER_DAY,
        GenerationQuotaType.INPUT_TOKENS_PER_DAY -> 3

        GenerationQuotaType.SPEND_PER_TEN_MINUTES -> 2
        GenerationQuotaType.REQUESTS_PER_MINUTE,
        GenerationQuotaType.INPUT_TOKENS_PER_MINUTE -> 1

        GenerationQuotaType.UNKNOWN -> 0
    }

private val GenerationQuotaType.isDailyQuota: Boolean
    get() = this == GenerationQuotaType.REQUESTS_PER_DAY || this == GenerationQuotaType.INPUT_TOKENS_PER_DAY

private fun String.retryDurationMillis(): Long = RETRY_DURATION_PART_PATTERN.findAll(this).sumOf { match ->
    val amount = match.groupValues[1].toDoubleOrNull() ?: return@sumOf 0L
    when (match.groupValues[2].lowercase()) {
        "h" -> amount.times(60 * 60 * 1_000).toLong()
        "m" -> amount.times(60 * 1_000).toLong()
        "s" -> amount.times(1_000).toLong()
        else -> amount.toLong()
    }
}

private fun Long.retryDelayText(): String = when {
    this >= 60 * 60 * 1_000 -> "${(this + 60 * 60 * 1_000 - 1) / (60 * 60 * 1_000)} h"
    this >= 60 * 1_000 -> "${(this + 60 * 1_000 - 1) / (60 * 1_000)} min"
    else -> "${(this + 1_000 - 1) / 1_000} s"
}

private fun deckName(type: BoardCardType) = when (type) {
    BoardCardType.SmallBusiness -> "малий бізнес"
    BoardCardType.MediumBusiness -> "середній бізнес"
    BoardCardType.BigBusiness -> "великий бізнес"
    BoardCardType.Shopping -> "покупки"
    BoardCardType.Expenses -> "витрати"
    BoardCardType.Chance -> "випадок"
    BoardCardType.EventStore -> "події ринку"
    BoardCardType.Deputy -> "депутати"
}

private fun BoardCard.brief(shareNames: Map<String, String>): String = when (this) {
    is BoardCard.SmallBusiness -> "можна купити малий бізнес за $price; він додає регулярний дохід $profit"
    is BoardCard.MediumBusiness -> "можна купити середній бізнес за $price; він додає регулярний дохід $profit"
    is BoardCard.BigBusiness -> "можна купити великий бізнес за $price; він додає регулярний дохід $profit"
    is BoardCard.Shopping -> "можна купити ${shopType.promptAsset()} за $price; покупка додає один такий актив"
    is BoardCard.Expenses -> if (grantsAnimal) {
        "гравець підбирає бродячу або врятовану тварину: платить $price і назавжди отримує домашню тварину, за яку далі щоходу йдуть витрати на утримання"
    } else {
        "обов'язкова витрата $price лише для гравців за умовою: ${payer.promptRule()}"
    }
    is BoardCard.Deputy -> if (corrupt) {
        "продажний посадовець приєднується до гравця як депутат"
    } else {
        "чесний посадовець не приєднується до гравця"
    }
    is BoardCard.EventStore.Shares -> if (forcedSale) {
        "примусовий продаж усіх акцій ${shareNames[sharesType] ?: sharesType} кожним власником за невигідною ціною $price; відмовитися чи продати частину не можна"
    } else {
        "власники акцій ${shareNames[sharesType] ?: sharesType} можуть продати будь-яку кількість за ціною $price за акцію або відмовитися"
    }
    is BoardCard.EventStore.Land -> "власники землі можуть продати будь-яку площу за $price за одиницю площі або відмовитися"
    is BoardCard.EventStore.Estate -> "власники нерухомості можуть продати вибрані об'єкти по $price за об'єкт або відмовитися"
    is BoardCard.EventStore.BusinessExtending -> "активний гравець збільшує регулярний дохід одного випадкового малого бізнесу на $profit; без малого бізнесу ефекту немає"
    is BoardCard.EventStore.Reelection -> "перевибори: усі гравці втрачають усіх депутатів"
    is BoardCard.EventStore.Announcement -> "ринкова новина без прямої фінансової дії"
    is BoardCard.Chance.RandomJob -> "гравець одразу отримує разовий дохід $profit"
    is BoardCard.Chance.Land -> "можна купити землю площею $area за $price"
    is BoardCard.Chance.Estate -> "можна купити один об'єкт нерухомості за $price"
    is BoardCard.Chance.Shares -> "можна купити до $maxCount акцій ${shareNames[sharesType] ?: sharesType} за ціною $price за акцію"
    is BoardCard.Chance.CorruptBusiness -> if (oneTimeProfit > 0) {
        "можна витратити $deputies депутатів і $price та одразу отримати разову виплату $oneTimeProfit; регулярного доходу немає"
    } else {
        "можна витратити $deputies депутатів і $price та отримати корупційний бізнес із регулярним доходом $profit"
    }
    is BoardCard.Chance.CorruptLand -> "можна витратити $deputies депутатів і $price та отримати корупційну землю площею $area"
}

private fun ShopType.promptAsset(): String = when (this) {
    ShopType.AUTO -> "автомобіль"
    ShopType.HOUSE -> "будинок"
    ShopType.APARTMENT -> "квартиру"
    ShopType.YACHT -> "яхту"
    ShopType.FLY -> "літак"
    ShopType.ANIMAL -> "домашню тварину"
}

private fun PayerType.promptRule(): String = when (this) {
    PayerType.ALL -> "платять усі"
    PayerType.FREE_W_OR_MARRIED_M -> "платять лише незаміжні жінки та одружені чоловіки"
    PayerType.AUTO_OWNER -> "платять лише власники автомобілів"
    PayerType.MEN -> "платять лише чоловіки"
    PayerType.PARENT -> "платять лише гравці з дітьми"
    PayerType.MARRIED_M -> "платять лише одружені чоловіки"
    PayerType.APARTMENT_OWNER -> "платять лише власники квартир"
    PayerType.APARTMENT_OR_HOUSE_OWNER -> "платять лише власники квартир або будинків"
    PayerType.ANIMAL_OWNER -> "платять лише власники тварин"
}
