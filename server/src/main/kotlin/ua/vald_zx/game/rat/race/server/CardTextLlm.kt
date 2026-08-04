package ua.vald_zx.game.rat.race.server

import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

private val llmLogger = KtorSimpleLogger("CardTextLlm")
private val llmQuotaTracker = LlmQuotaTracker()

internal object LlmSettings {
    val providers: List<LlmProviderSettings>
        get() = buildList {
            provider(
                prefix = "LLM_",
                defaultName = "gemini",
                defaultUrl = "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions",
                defaultBalanceModel = "gemini-3.6-flash",
                defaultTextModel = "gemini-3.5-flash-lite",
            )?.let(::add)
            (1..MAX_LLM_FALLBACKS).mapNotNullTo(this) { index ->
                provider(prefix = "LLM_FALLBACK_${index}_", defaultName = "fallback-$index")
            }
        }

    val enabled: Boolean get() = providers.isNotEmpty()

    fun balanceChat(
        onUsage: suspend (LlmTokenUsage) -> Unit = {},
        onRetry: suspend (LlmRetryWait) -> Unit = {},
    ): ChatCompletion = fallbackChat({ it.balanceModel }, onUsage, onRetry)

    fun textChat(
        onUsage: suspend (LlmTokenUsage) -> Unit = {},
        onRetry: suspend (LlmRetryWait) -> Unit = {},
    ): ChatCompletion = fallbackChat({ it.textModel }, onUsage, onRetry)

    private fun fallbackChat(
        model: (LlmProviderSettings) -> String,
        onUsage: suspend (LlmTokenUsage) -> Unit,
        onRetry: suspend (LlmRetryWait) -> Unit,
    ): ChatCompletion {
        val configured = providers
        return FallbackChatCompletion(
            configured.mapIndexed { index, provider ->
                HttpChatCompletion(
                    provider = provider,
                    model = model(provider),
                    retryRateLimits = index == configured.lastIndex,
                    onUsage = onUsage,
                    onRetry = onRetry,
                )
            }
        )
    }

    private fun provider(
        prefix: String,
        defaultName: String,
        defaultUrl: String? = null,
        defaultBalanceModel: String? = null,
        defaultTextModel: String? = null,
    ): LlmProviderSettings? {
        val key = Env["${prefix}API_KEY"].orEmpty()
        if (key.isBlank()) return null
        val model = Env["${prefix}MODEL"]
        val url = Env["${prefix}API_URL"] ?: defaultUrl ?: return null
        val balanceModel = Env["${prefix}BALANCE_MODEL"] ?: model ?: defaultBalanceModel ?: return null
        val textModel = Env["${prefix}TEXT_MODEL"] ?: model ?: defaultTextModel ?: return null
        return LlmProviderSettings(
            name = Env["${prefix}PROVIDER_NAME"] ?: defaultName,
            url = url,
            key = key,
            balanceModel = balanceModel,
            textModel = textModel,
        )
    }
}

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

internal class FallbackChatCompletion(
    private val completions: List<ChatCompletion>,
) : ChatCompletion {
    override suspend fun complete(system: String, user: String): String? {
        var providerFailure: LlmProviderException? = null
        completions.forEachIndexed { index, completion ->
            val answer = try {
                completion.complete(system, user)
            } catch (error: LlmProviderException) {
                providerFailure = error
                if (index < completions.lastIndex) {
                    llmLogger.warn("${error.message}; switching to fallback ${index + 2}")
                }
                null
            }
            if (answer != null) return answer
        }
        providerFailure?.let { throw it }
        return null
    }
}

internal class HttpChatCompletion(
    private val provider: LlmProviderSettings,
    private val model: String,
    private val retryRateLimits: Boolean = true,
    private val onUsage: suspend (LlmTokenUsage) -> Unit = {},
    private val onRetry: suspend (LlmRetryWait) -> Unit = {},
    private val wait: suspend (Long) -> Unit = { delay(it) },
    private val quotaTracker: LlmQuotaTracker = llmQuotaTracker,
    private val nowEpochMs: () -> Long = System::currentTimeMillis,
) : ChatCompletion {

    private val client: HttpClient by lazy {
        HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build()
    }

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun complete(system: String, user: String): String? = withContext(Dispatchers.IO) {
        val payload = buildJsonObject {
            put("model", model)
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
                val reportedQuota = response.reportedQuota()
                val quota = reportedQuota?.let { reported ->
                    quotaTracker.recordLimit(
                        provider = provider.name,
                        model = model,
                        type = reported.type,
                        limit = reported.limit,
                        retryDelayMillis = reportedRetry.millis,
                    )
                }
                val retry = quota?.takeIf { it.type.isDailyQuota }?.let { dailyQuota ->
                    RetryDelay(
                        millis = maxOf(reportedRetry.millis, dailyQuota.resetAtEpochMs - nowEpochMs()),
                        precise = true,
                    )
                } ?: reportedRetry
                if (!retryRateLimits) {
                    throw LlmRateLimitException(provider.name, model, retry.millis)
                }
                if (!retry.precise) {
                    failedAttempts += 1
                    if (failedAttempts >= MAX_LLM_REQUEST_ATTEMPTS) {
                        throw LlmRateLimitException(provider.name, model, retry.millis)
                    }
                }
                llmLogger.warn("${provider.name}/$model rate limit reached, retrying in ${retry.millis}ms")
                onRetry(LlmRetryWait(provider.name, model, retry.millis, quota))
                wait(retry.millis)
                continue
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

    private fun HttpResponse<String>.retryDelay(): RetryDelay {
        val headerSeconds = headers().firstValue("Retry-After").orElse(null)?.toDoubleOrNull()
        val responseBody = body()
        val bodyDelay = RETRY_INFO_DELAY_PATTERN.find(responseBody)?.groupValues?.get(1)?.retryDurationMillis()
            ?: RETRY_MESSAGE_DELAY_PATTERN.find(responseBody)?.groupValues?.get(1)?.retryDurationMillis()
        val value = headerSeconds?.times(1_000)?.toLong() ?: bodyDelay ?: DEFAULT_RETRY_DELAY_MILLIS
        return RetryDelay(
            millis = value.coerceIn(MIN_RETRY_DELAY_MILLIS, MAX_REPORTED_RETRY_DELAY_MILLIS) +
                    RETRY_DELAY_BUFFER_MILLIS,
            precise = headerSeconds != null || bodyDelay != null,
        )
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

private data class RetryDelay(
    val millis: Long,
    val precise: Boolean,
)

private data class ReportedQuota(
    val type: GenerationQuotaType,
    val limit: Long,
)

internal open class LlmProviderException(message: String) : IllegalStateException(message)

internal class LlmRateLimitException(
    provider: String,
    model: String,
    retryAfterMillis: Long,
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
    ): Int = deckSizes.sumOf(::batches) + batches(professionCount)

    suspend fun generateComplete(
        world: BoardGeneration,
        cards: Map<BoardCardType, Map<Int, BoardCard>>,
        professions: List<ProfessionCard>,
        existingTexts: Map<String, GeneratedText> = emptyMap(),
        shareNames: Map<String, String> = emptyMap(),
        onCheckpoint: suspend (
            texts: Map<String, GeneratedText>,
            completed: Int,
            total: Int,
            detail: String,
        ) -> Unit = { _, _, _, _ -> },
    ): Map<String, GeneratedText> {
        val texts = generatedLocales.associateWith { existingTexts[it] ?: GeneratedText() }.toMutableMap()
        val cardBatches = cards.entries.flatMap { (type, deck) ->
            deck.entries.sortedBy { it.key }.chunked(batchSize).mapIndexed { index, batch ->
                CardBatch(type, index, batches(deck.size), batch)
            }
        }
        val professionBatches = professions.chunked(batchSize).mapIndexed { index, batch ->
            ProfessionBatch(index, batches(professions.size), batch)
        }
        val total = cardBatches.size + professionBatches.size
        var completed = cardBatches.count { batch -> texts.hasCards(batch.type, batch.items.map { it.key }) } +
                professionBatches.count { batch -> texts.hasProfessions(batch.items.map { it.id }) }
        onCheckpoint(texts.snapshot(), completed, total, "")

        cardBatches.forEach { batch ->
            if (texts.hasCards(batch.type, batch.items.map { it.key })) return@forEach
            val detail = "${batch.type.name} ${batch.index + 1}/${batch.count}"
            val rejected = mutableMapOf<Int, MutableList<Map<String, CardText>>>()
            repeat(MAX_TEXT_BATCH_ATTEMPTS) { attempt ->
                val missing = batch.items.filterNot { texts.hasCard(batch.type, it.key) }
                if (missing.isEmpty()) return@repeat
                val briefs = missing.joinToString("\n") { (id, card) -> "$id. ${card.brief(shareNames)}" }
                val answer = chat.complete(
                    system = systemPrompt(),
                    user = deckPrompt(
                        world = world,
                        type = batch.type,
                        briefs = briefs,
                        acceptedNames = texts.cardNameContext(batch.type),
                        attempt = attempt,
                        rejected = rejected,
                    ),
                ) ?: return@repeat
                val candidates = answer.parseLocalizedItems()
                texts.acceptCards(batch.type, candidates, missing.map { it.key }.toSet())
                missing.filterNot { texts.hasCard(batch.type, it.key) }.forEach { (id) ->
                    candidates[id]?.let { candidate -> rejected.getOrPut(id, ::mutableListOf) += candidate }
                }
                if (!texts.hasCards(batch.type, batch.items.map { it.key })) {
                    onCheckpoint(texts.snapshot(), completed, total, detail)
                }
            }
            repeat(MAX_REPAIR_BATCH_ATTEMPTS) { repairAttempt ->
                val missing = batch.items.filterNot { texts.hasCard(batch.type, it.key) }
                if (missing.isEmpty()) return@repeat
                missing.chunked(REPAIR_BATCH_SIZE).forEach repairBatchLoop@{ repairBatch ->
                    val briefs = repairBatch.joinToString("\n") { (id, card) ->
                        "$id. ${card.brief(shareNames)}"
                    }
                    val answer = chat.complete(
                        system = systemPrompt(),
                        user = deckPrompt(
                            world = world,
                            type = batch.type,
                            briefs = briefs,
                            acceptedNames = texts.cardNameContext(batch.type),
                            attempt = MAX_TEXT_BATCH_ATTEMPTS + repairAttempt,
                            rejected = rejected,
                        ),
                    ) ?: return@repairBatchLoop
                    val expectedIds = repairBatch.map { it.key }.toSet()
                    val candidates = answer.parseLocalizedItems(
                        expectedSingleId = expectedIds.singleOrNull(),
                    )
                    texts.acceptCards(batch.type, candidates, expectedIds)
                    repairBatch.filterNot { texts.hasCard(batch.type, it.key) }.forEach { (id) ->
                        candidates[id]?.let { candidate -> rejected.getOrPut(id, ::mutableListOf) += candidate }
                    }
                    onCheckpoint(texts.snapshot(), completed, total, detail)
                }
            }
            batch.items.filterNot { texts.hasCard(batch.type, it.key) }.forEach { missingCard ->
                repeat(MAX_SINGLE_ITEM_ATTEMPTS) { repairAttempt ->
                    if (texts.hasCard(batch.type, missingCard.key)) return@repeat
                    val id = missingCard.key
                    val answer = chat.complete(
                        system = systemPrompt(),
                        user = deckPrompt(
                            world = world,
                            type = batch.type,
                            briefs = "$id. ${missingCard.value.brief(shareNames)}",
                            acceptedNames = texts.cardNameContext(batch.type),
                            attempt = MAX_TEXT_BATCH_ATTEMPTS + repairAttempt,
                            rejected = rejected,
                        ),
                    ) ?: return@repeat
                    val candidates = answer.parseLocalizedItems(expectedSingleId = id)
                    texts.acceptCards(batch.type, candidates, setOf(id))
                    if (!texts.hasCard(batch.type, id)) {
                        candidates[id]?.let { candidate ->
                            rejected.getOrPut(id, ::mutableListOf) += candidate
                            llmLogger.warn("Rejected duplicate localized card text for ${batch.type} $id")
                        } ?: llmLogger.warn("LLM repair response has no usable localized card text for ${batch.type} $id")
                    }
                    onCheckpoint(texts.snapshot(), completed, total, detail)
                }
            }
            val missingIds = batch.items.map { it.key }.filterNot { texts.hasCard(batch.type, it) }
            check(missingIds.isEmpty()) {
                "Incomplete uk/en texts for ${batch.type}: ${missingIds.take(MAX_ERROR_IDS)}"
            }
            completed += 1
            onCheckpoint(texts.snapshot(), completed, total, detail)
        }

        professionBatches.forEach { batch ->
            if (texts.hasProfessions(batch.items.map { it.id })) return@forEach
            val detail = "professions ${batch.index + 1}/${batch.count}"
            val rejected = mutableMapOf<Int, MutableList<Map<String, CardText>>>()
            repeat(MAX_TEXT_BATCH_ATTEMPTS) { attempt ->
                val missing = batch.items.filterNot { texts.hasProfession(it.id) }
                if (missing.isEmpty()) return@repeat
                val briefs = missing.joinToString("\n") { card ->
                    "${card.id}. ${if (card.gender == Gender.FEMALE) "жінка" else "чоловік"}, зарплата ${card.salary}"
                }
                val answer = chat.complete(
                    system = systemPrompt(),
                    user = professionPrompt(
                        world = world,
                        briefs = briefs,
                        acceptedNames = texts.professionNameContext(),
                        attempt = attempt,
                        rejected = rejected,
                    ),
                ) ?: return@repeat
                val candidates = answer.parseLocalizedItems()
                texts.acceptProfessions(candidates, missing.map { it.id }.toSet())
                missing.filterNot { texts.hasProfession(it.id) }.forEach { profession ->
                    candidates[profession.id]?.let { candidate ->
                        rejected.getOrPut(profession.id, ::mutableListOf) += candidate
                    }
                }
                if (!texts.hasProfessions(batch.items.map { it.id })) {
                    onCheckpoint(texts.snapshot(), completed, total, detail)
                }
            }
            repeat(MAX_REPAIR_BATCH_ATTEMPTS) { repairAttempt ->
                val missing = batch.items.filterNot { texts.hasProfession(it.id) }
                if (missing.isEmpty()) return@repeat
                missing.chunked(REPAIR_BATCH_SIZE).forEach repairBatchLoop@{ repairBatch ->
                    val briefs = repairBatch.joinToString("\n") { profession ->
                        "${profession.id}. ${if (profession.gender == Gender.FEMALE) "жінка" else "чоловік"}, зарплата ${profession.salary}"
                    }
                    val answer = chat.complete(
                        system = systemPrompt(),
                        user = professionPrompt(
                            world = world,
                            briefs = briefs,
                            acceptedNames = texts.professionNameContext(),
                            attempt = MAX_TEXT_BATCH_ATTEMPTS + repairAttempt,
                            rejected = rejected,
                        ),
                    ) ?: return@repairBatchLoop
                    val expectedIds = repairBatch.map { it.id }.toSet()
                    val candidates = answer.parseLocalizedItems(
                        expectedSingleId = expectedIds.singleOrNull(),
                    )
                    texts.acceptProfessions(candidates, expectedIds)
                    repairBatch.filterNot { texts.hasProfession(it.id) }.forEach { profession ->
                        candidates[profession.id]?.let { candidate ->
                            rejected.getOrPut(profession.id, ::mutableListOf) += candidate
                        }
                    }
                    onCheckpoint(texts.snapshot(), completed, total, detail)
                }
            }
            batch.items.filterNot { texts.hasProfession(it.id) }.forEach { missingProfession ->
                repeat(MAX_SINGLE_ITEM_ATTEMPTS) { repairAttempt ->
                    if (texts.hasProfession(missingProfession.id)) return@repeat
                    val answer = chat.complete(
                        system = systemPrompt(),
                        user = professionPrompt(
                            world = world,
                            briefs = "${missingProfession.id}. ${if (missingProfession.gender == Gender.FEMALE) "жінка" else "чоловік"}, зарплата ${missingProfession.salary}",
                            acceptedNames = texts.professionNameContext(),
                            attempt = MAX_TEXT_BATCH_ATTEMPTS + repairAttempt,
                            rejected = rejected,
                        ),
                    ) ?: return@repeat
                    val candidates = answer.parseLocalizedItems(expectedSingleId = missingProfession.id)
                    texts.acceptProfessions(candidates, setOf(missingProfession.id))
                    if (!texts.hasProfession(missingProfession.id)) {
                        candidates[missingProfession.id]?.let { candidate ->
                            rejected.getOrPut(missingProfession.id, ::mutableListOf) += candidate
                            llmLogger.warn("Rejected duplicate localized profession ${missingProfession.id}")
                        } ?: llmLogger.warn(
                            "LLM repair response has no usable localized profession ${missingProfession.id}"
                        )
                    }
                    onCheckpoint(texts.snapshot(), completed, total, detail)
                }
            }
            val missingIds = batch.items.map { it.id }.filterNot { texts.hasProfession(it) }
            check(missingIds.isEmpty()) { "Incomplete uk/en professions: ${missingIds.take(MAX_ERROR_IDS)}" }
            completed += 1
            onCheckpoint(texts.snapshot(), completed, total, detail)
        }

        val generated = texts.snapshot()
        generatedLocales.forEach { locale -> validateComplete(locale, generated.getValue(locale), cards, professions) }
        return generated
    }

    private fun validateComplete(
        locale: String,
        generated: GeneratedText,
        cards: Map<BoardCardType, Map<Int, BoardCard>>,
        professions: List<ProfessionCard>,
    ) {
        cards.forEach { (type, deck) ->
            val texts = generated.cards[type].orEmpty()
            check(texts.keys == deck.keys) { "Incomplete $locale texts for $type" }
            check(texts.values.all { it.name.isNotBlank() }) { "Empty $locale name for $type" }
            check(texts.values.all { it.description.isNotBlank() }) { "Empty $locale description for $type" }
            check(texts.values.map { it.name.lowercase() }.distinct().size == texts.size) {
                "Repeated $locale names for $type"
            }
            check(texts.values.map { it.description }.distinct().size == texts.size) {
                "Repeated $locale descriptions for $type"
            }
        }
        val cardIdentities = generated.cards.values
            .flatMap { it.values }
            .map { text -> text.name.lowercase() to text.description.lowercase() }
        check(cardIdentities.distinct().size == cardIdentities.size) { "Repeated $locale cards" }
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
        append("Не повторюй сюжети, назви чи описи. Механіка в кожному рядку є точною й обов'язковою: відобрази її в описі, не додавай нових чисел, умов або наслідків. ")
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
        append("Назва має відповідати статі, світу та розміру зарплати. ")
        append("Стать є ігровою ознакою: вона впливає на шлюб, дітей, розлучення й умовні витрати, тому не роби професію гендерно суперечливою. ")
        append("Опис до 140 символів має пояснювати роль професії у вказаних темі, місцевості й епосі. ")
        append("Не повторюй назви чи описи. ")
        append("Поверни рівно по одному об'єкту для кожного переданого ID. ")
        append("Поля uk та en мають передавати ту саму професію відповідними мовами.\n")
        appendAcceptedNames(acceptedNames)
        appendRetryInstructions(attempt, rejected)
        append("Професії:\n")
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
    ) {
        candidates.forEach { (id, localized) ->
            if (id !in allowedIds) return@forEach
            val unique = generatedLocales.all { locale ->
                localized.getValue(locale).isUniqueCard(this, locale, type, id)
            }
            if (!unique) return@forEach
            generatedLocales.forEach { locale -> putCard(locale, type, id, localized.getValue(locale)) }
        }
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
    ): Boolean = texts.getValue(locale).cards.none { (existingType, deck) ->
        deck.any { (existingId, existing) ->
            (existingType != type || existingId != id) && existing.collidesWith(this)
        }
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

    private fun Map<String, GeneratedText>.hasCard(type: BoardCardType, id: Int): Boolean =
        generatedLocales.all { locale -> this[locale]?.cards?.get(type)?.get(id)?.isUsable() == true }

    private fun Map<String, GeneratedText>.hasCards(type: BoardCardType, ids: List<Int>): Boolean =
        ids.all { hasCard(type, it) }

    private fun Map<String, GeneratedText>.hasProfession(id: Int): Boolean = generatedLocales.all { locale ->
        this[locale]?.professions?.get(id).orEmpty().isNotBlank() &&
                this[locale]?.professionDescriptions?.get(id).orEmpty().isNotBlank()
    }

    private fun Map<String, GeneratedText>.hasProfessions(ids: List<Int>): Boolean = ids.all { hasProfession(it) }

    private fun Map<String, GeneratedText>.snapshot(): Map<String, GeneratedText> =
        generatedLocales.associateWith { getValue(it) }

    private fun Map<String, GeneratedText>.cardNameContext(preferredType: BoardCardType): List<String> {
        val orderedTypes = listOf(preferredType) + BoardCardType.entries.filterNot { it == preferredType }
        return orderedTypes.flatMap { type ->
            val ids = generatedLocales.flatMap { locale ->
                this[locale]?.cards?.get(type)?.keys.orEmpty()
            }.distinct().sorted()
            ids.mapNotNull { id ->
                localizedNameContext { locale -> this[locale]?.cards?.get(type)?.get(id)?.name }
                    ?.let { names -> "${type.name}: $names" }
            }
        }.take(MAX_ACCEPTED_CARD_NAMES_IN_PROMPT)
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
)

private data class ProfessionBatch(
    val index: Int,
    val count: Int,
    val items: List<ProfessionCard>,
)

private fun CardText.isUsable(): Boolean = name.isNotBlank() && description.isNotBlank()

private fun CardText.collidesWith(other: CardText): Boolean =
    name.normalized() == other.name.normalized() || description.normalized() == other.description.normalized()

private fun String.normalized(): String = trim().lowercase()

private const val MAX_LLM_REQUEST_ATTEMPTS = 8
private const val MAX_LLM_UNAVAILABLE_ATTEMPTS = 3
private const val UNAVAILABLE_RETRY_DELAY_MILLIS = 2_000L
private const val MAX_ERROR_MESSAGE_LENGTH = 300
private const val MAX_LLM_FALLBACKS = 3
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
    is BoardCard.Expenses -> "обов'язкова витрата $price лише для гравців за умовою: ${payer.promptRule()}"
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
