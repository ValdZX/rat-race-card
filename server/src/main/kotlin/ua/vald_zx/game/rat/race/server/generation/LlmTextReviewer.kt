package ua.vald_zx.game.rat.race.server.generation

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import ua.vald_zx.game.rat.race.card.shared.CardText
import ua.vald_zx.game.rat.race.card.shared.generatedLocales

internal fun interface TextReviewer {
    suspend fun rejectedIds(kind: String, candidates: Map<Int, Map<String, CardText>>): Set<Int>
}

internal object AcceptAllTextReviewer : TextReviewer {
    override suspend fun rejectedIds(
        kind: String,
        candidates: Map<Int, Map<String, CardText>>,
    ): Set<Int> = emptySet()
}

internal class LlmTextReviewer(
    private val chat: ChatCompletion,
) : TextReviewer {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun rejectedIds(
        kind: String,
        candidates: Map<Int, Map<String, CardText>>,
    ): Set<Int> {
        if (candidates.isEmpty()) return emptySet()
        val answer = runCatching {
            chat.complete(
                system = REVIEW_SYSTEM_PROMPT,
                user = reviewPrompt(kind, candidates),
            )
        }.onFailure { failure ->
            llmLogger.warn("LLM text review failed open: ${failure.message}")
        }.getOrNull() ?: return emptySet()
        val review = runCatching {
            val start = answer.indexOf('{')
            val end = answer.lastIndexOf('}')
            check(start >= 0 && end > start) { "review is not a JSON object" }
            json.parseToJsonElement(answer.substring(start, end + 1)).jsonObject
        }.onFailure { failure ->
            llmLogger.warn("LLM text review returned invalid JSON and failed open: ${failure.message}")
        }.getOrNull() ?: return emptySet()
        val invalidIds = runCatching {
            val values = checkNotNull(review["invalidIds"]) { "invalidIds is missing" }.jsonArray
            values.mapNotNull { value ->
                value.jsonPrimitive.intOrNull ?: value.jsonPrimitive.contentOrNull?.toIntOrNull()
            }.toSet()
        }.onFailure { failure ->
            llmLogger.warn("LLM text review omitted invalidIds and failed open: ${failure.message}")
        }.getOrNull() ?: return emptySet()
        val rejected = invalidIds.intersect(candidates.keys)
        if (rejected.isNotEmpty()) {
            llmLogger.warn("LLM text reviewer rejected $kind IDs: ${rejected.sorted()}")
        }
        return rejected
    }

    private fun reviewPrompt(
        kind: String,
        candidates: Map<Int, Map<String, CardText>>,
    ): String = buildString {
        append("Content type: $kind. Review every object independently.\n")
        append("Candidates:\n")
        append(buildJsonArray {
            candidates.toSortedMap().forEach { (id, localized) ->
                add(buildJsonObject {
                    put("id", id)
                    generatedLocales.forEach { locale ->
                        val text = localized.getValue(locale)
                        put(locale, buildJsonObject {
                            put("name", text.name)
                            put("description", text.description)
                        })
                    }
                })
            }
        })
    }
}

private const val REVIEW_SYSTEM_PROMPT = """You are a strict bilingual editor for an economic board game.
Review each candidate independently. Reject an ID when Ukrainian is Russian, mixed with Russian, unnatural,
or meaningless; when English is unnatural or meaningless; or when uk and en convey materially different meanings.
Names must identify something specific and descriptions must describe a coherent situation. Reject only confident
quality problems, not harmless stylistic preferences. Return only JSON: {"invalidIds":[number],"reasons":{"number":"short reason"}}."""

