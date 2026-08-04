package ua.vald_zx.game.rat.race.server

import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import ua.vald_zx.game.rat.race.card.shared.CardText
import ua.vald_zx.game.rat.race.card.shared.GeneratedText
import ua.vald_zx.game.rat.race.card.shared.Gender
import ua.vald_zx.game.rat.race.card.shared.ProfessionCard
import ua.vald_zx.game.rat.race.server.data.Env
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

private val llmLogger = KtorSimpleLogger("CardTextLlm")

internal object LlmSettings {
    val url: String get() = Env["LLM_API_URL"] ?: "https://api.groq.com/openai/v1/chat/completions"
    val key: String get() = Env["LLM_API_KEY"].orEmpty()
    val model: String get() = Env["LLM_MODEL"] ?: "llama-3.3-70b-versatile"
    val enabled: Boolean get() = key.isNotBlank()
}

internal fun interface ChatCompletion {
    suspend fun complete(system: String, user: String): String?
}

internal object HttpChatCompletion : ChatCompletion {

    private val client: HttpClient by lazy {
        HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build()
    }

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun complete(system: String, user: String): String? = withContext(Dispatchers.IO) {
        val payload = buildJsonObject {
            put("model", LlmSettings.model)
            put("temperature", 0.9)
            put("messages", buildJsonArray {
                add(message("system", system))
                add(message("user", user))
            })
        }
        val request = HttpRequest.newBuilder(URI.create(LlmSettings.url))
            .timeout(Duration.ofSeconds(120))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer ${LlmSettings.key}")
            .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), Charsets.UTF_8))
            .build()
        runCatching {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString(Charsets.UTF_8))
            if (response.statusCode() !in 200..299) {
                llmLogger.warn("LLM answered ${response.statusCode()}: ${response.body().take(300)}")
                return@runCatching null
            }
            json.parseToJsonElement(response.body())
                .jsonObject["choices"]?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("message")?.jsonObject?.get("content")
                ?.jsonPrimitive?.contentOrNull
        }.onFailure { llmLogger.warn("LLM call failed: ${it.message}") }.getOrNull()
    }

    private fun message(role: String, content: String) = buildJsonObject {
        put("role", role)
        put("content", content)
    }
}

internal class LlmTextGenerator(
    private val chat: ChatCompletion = HttpChatCompletion,
    private val batchSize: Int = 12,
    concurrentRequests: Int = 4,
) {

    private val json = Json { ignoreUnknownKeys = true }
    private val gate = Semaphore(concurrentRequests)

    private suspend fun ask(system: String, user: String): String? =
        gate.withPermit { chat.complete(system, user) }

    suspend fun localize(
        world: BoardGeneration,
        cards: Map<BoardCardType, Map<Int, BoardCard>>,
        professions: List<ProfessionCard>,
        locale: String,
    ): GeneratedText = coroutineScope {
        val decks = cards.map { (type, deck) ->
            async { type to deckTexts(world, locale, type, deck) }
        }
        val names = async { professionTexts(world, locale, professions) }
        GeneratedText(
            cards = decks.awaitAll().toMap().filterValues { it.isNotEmpty() },
            professions = names.await(),
        )
    }

    private suspend fun deckTexts(
        world: BoardGeneration,
        locale: String,
        type: BoardCardType,
        deck: Map<Int, BoardCard>,
    ): Map<Int, CardText> {
        val texts = mutableMapOf<Int, CardText>()
        deck.entries.sortedBy { it.key }.chunked(batchSize).forEach { batch ->
            val briefs = batch.joinToString("\n") { (id, card) -> "$id. ${card.brief()}" }
            val answer = ask(
                system = systemPrompt(locale),
                user = deckPrompt(world, locale, type, briefs),
            ) ?: return@forEach
            answer.parseItems().forEach { (id, text) ->
                if (batch.any { it.key == id }) texts[id] = text
            }
        }
        return texts
    }

    private suspend fun professionTexts(
        world: BoardGeneration,
        locale: String,
        professions: List<ProfessionCard>,
    ): Map<Int, String> {
        val names = mutableMapOf<Int, String>()
        professions.chunked(batchSize).forEach { batch ->
            val briefs = batch.joinToString("\n") { card ->
                "${card.id}. ${if (card.gender == Gender.FEMALE) "жінка" else "чоловік"}, зарплата ${card.salary}"
            }
            val answer = ask(
                system = systemPrompt(locale),
                user = professionPrompt(world, locale, briefs),
            ) ?: return@forEach
            answer.parseItems().forEach { (id, text) ->
                val name = text.name.ifBlank { text.description }
                if (name.isNotBlank() && batch.any { it.id == id }) names[id] = name
            }
        }
        return names
    }

    private fun systemPrompt(locale: String) = buildString {
        append("Ти пишеш тексти карток для настільної економічної гри. ")
        append("Відповідай виключно JSON-масивом об'єктів {\"id\":число,\"name\":рядок,\"description\":рядок}, ")
        append("без пояснень і без markdown. ")
        append("Усі тексти мовою: ${languageName(locale)}.")
    }

    private fun deckPrompt(
        world: BoardGeneration,
        locale: String,
        type: BoardCardType,
        briefs: String,
    ) = buildString {
        append(worldPrompt(world))
        append("Придумай назву та опис (до 140 символів) для кожної картки колоди «${deckName(type)}». ")
        append("Не змінюй числа, вони вже надруковані на картці. ")
        append("Мова відповіді: ${languageName(locale)}.\n")
        append("Картки:\n")
        append(briefs)
    }

    private fun professionPrompt(world: BoardGeneration, locale: String, briefs: String) = buildString {
        append(worldPrompt(world))
        append("Придумай назву професії для кожного рядка. ")
        append("Назва має бути у формі, що відповідає статі, і пасувати до розміру зарплати. ")
        append("У полі name повертай назву професії, description лишай порожнім. ")
        append("Мова відповіді: ${languageName(locale)}.\n")
        append("Професії:\n")
        append(briefs)
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

    private fun String.parseItems(): Map<Int, CardText> {
        val start = indexOf('[')
        val end = lastIndexOf(']')
        if (start < 0 || end <= start) return emptyMap()
        val array = runCatching { json.parseToJsonElement(substring(start, end + 1)) as? JsonArray }
            .onFailure { llmLogger.warn("LLM answered unparsable JSON: ${it.message}") }
            .getOrNull() ?: return emptyMap()
        return array.mapNotNull { element ->
            val item = element as? JsonObject ?: return@mapNotNull null
            val id = item["id"]?.jsonPrimitive?.intOrNull ?: return@mapNotNull null
            val text = CardText(
                name = item["name"]?.jsonPrimitive?.contentOrNull.orEmpty().trim(),
                description = item["description"]?.jsonPrimitive?.contentOrNull.orEmpty().trim(),
            )
            if (text.name.isBlank() && text.description.isBlank()) null else id to text
        }.toMap()
    }
}

private fun languageName(locale: String) = when (locale.take(2)) {
    "uk" -> "українська"
    "en" -> "англійська"
    else -> locale
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

private fun BoardCard.brief(): String = when (this) {
    is BoardCard.SmallBusiness -> "бізнес, ціна $price, дохід $profit"
    is BoardCard.MediumBusiness -> "бізнес, ціна $price, дохід $profit"
    is BoardCard.BigBusiness -> "бізнес, ціна $price, дохід $profit"
    is BoardCard.Shopping -> "покупка ${shopType.name}, ціна $price"
    is BoardCard.Expenses -> "витрата $price"
    is BoardCard.Deputy -> if (corrupt) "продажний посадовець" else "чесний посадовець"
    is BoardCard.EventStore.Shares -> "акції ${sharesType.name}, ціна $price"
    is BoardCard.EventStore.Land -> "зміна ціни землі, $price"
    is BoardCard.EventStore.Estate -> "зміна ціни нерухомості, $price"
    is BoardCard.EventStore.BusinessExtending -> "розширення бізнесу, дохід $profit"
    is BoardCard.EventStore.Reelection -> "перевибори"
    is BoardCard.EventStore.Announcement -> "оголошення"
    is BoardCard.Chance.RandomJob -> "підробіток, дохід $profit"
    is BoardCard.Chance.Land -> "земля $area соток, ціна $price"
    is BoardCard.Chance.Estate -> "нерухомість, ціна $price"
    is BoardCard.Chance.Shares -> "акції ${sharesType.name}, ціна $price"
    is BoardCard.Chance.CorruptBusiness -> "корупційний бізнес, ціна $price, дохід $profit, депутатів $deputies"
    is BoardCard.Chance.CorruptLand -> "корупційна земля $area соток, ціна $price, депутатів $deputies"
}
