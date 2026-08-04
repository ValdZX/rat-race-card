package ua.vald_zx.game.rat.race.server

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.test.runTest
import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import ua.vald_zx.game.rat.race.card.shared.GeneratedText
import ua.vald_zx.game.rat.race.card.shared.GenerationQuotaType
import ua.vald_zx.game.rat.race.card.shared.PayerType
import java.net.InetSocketAddress
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LlmTextGeneratorTest {

    private val world = BoardGeneration(enabled = true, theme = "космічна колонія", seed = 7)
    private val cards = BoardGenerator(world, testBalance()).generate(mapOf(BoardCardType.SmallBusiness to 4))
    private val professions = BoardGenerator(world, testBalance()).generateProfessions()

    private class FakeChat(val answer: (String) -> String?) : ChatCompletion {
        val prompts = mutableListOf<String>()

        override suspend fun complete(system: String, user: String): String? {
            prompts += user
            return answer(user)
        }
    }

    private fun answerFor(user: String): String {
        val ids = promptIds(user)
        return ids.joinToString(
            prefix = "```json\n[",
            postfix = "]\n```",
        ) { localizedItem(it) }
    }

    private fun localizedItem(id: String): String =
        """{"id":$id,"uk":{"name":"Назва $id","description":"Опис $id"},"en":{"name":"Name $id","description":"Description $id"}}"""

    private fun promptIds(user: String): List<String> = Regex("^(\\d+)\\. ", RegexOption.MULTILINE)
        .findAll(user)
        .map { it.groupValues[1] }
        .toList()

    @Test
    fun oneRequestCreatesBothLocales() = runTest {
        val chat = FakeChat(::answerFor)
        val generated = LlmTextGenerator(chat).generateComplete(world, cards, professions)

        val ukDeck = generated.getValue("uk").cards.getValue(BoardCardType.SmallBusiness)
        val enDeck = generated.getValue("en").cards.getValue(BoardCardType.SmallBusiness)
        assertEquals((1..4).toSet(), ukDeck.keys)
        assertEquals("Назва 1", ukDeck.getValue(1).name)
        assertEquals("Name 1", enDeck.getValue(1).name)
        assertEquals(professions.map { it.id }.toSet(), generated.getValue("uk").professions.keys)
        assertEquals(professions.map { it.id }.toSet(), generated.getValue("en").professionDescriptions.keys)
    }

    @Test
    fun completeGenerationReportsEveryBatch() = runTest {
        val progress = mutableListOf<Pair<Int, Int>>()
        LlmTextGenerator(FakeChat(::answerFor)).generateComplete(
            world = world,
            cards = cards,
            professions = professions,
        ) { _, completed, total, _ -> progress += completed to total }

        assertEquals(progress.last().second, progress.last().first)
    }

    @Test
    fun theWorldAndBothLanguagesReachThePrompt() = runTest {
        val chat = FakeChat(::answerFor)
        LlmTextGenerator(chat).generateComplete(world, cards, professions)

        assertTrue(chat.prompts.all { "космічна колонія" in it })
        assertTrue(chat.prompts.all { "uk та en" in it })
        assertTrue(chat.prompts.all { "гумор" in it })
    }

    @Test
    fun acceptedNamesReachLaterBatchesWithoutDescriptions() = runTest {
        val cardChat = FakeChat(::answerFor)
        LlmTextGenerator(cardChat, batchSize = 2).generateComplete(world, cards, emptyList())

        assertTrue(cardChat.prompts[1].contains("uk «Назва 1» / en «Name 1»"))
        assertTrue(cardChat.prompts[1].contains("uk «Назва 2» / en «Name 2»"))
        assertTrue(!cardChat.prompts[1].contains("Опис 1"))

        val professionChat = FakeChat(::answerFor)
        LlmTextGenerator(professionChat, batchSize = 2).generateComplete(world, emptyMap(), professions)

        assertTrue(professionChat.prompts[1].contains("uk «Назва 1» / en «Name 1»"))
        assertTrue(professionChat.prompts[1].contains("uk «Назва 2» / en «Name 2»"))
        assertTrue(!professionChat.prompts[1].contains("Опис 1"))
    }

    @Test
    fun generatedShareNamesReachTheCardPrompt() = runTest {
        val chat = FakeChat(::answerFor)
        val shareCards = mapOf(
            BoardCardType.Chance to mapOf<Int, BoardCard>(
                1 to BoardCard.Chance.Shares("", 50, 1_000, "aerolith"),
            ),
        )

        LlmTextGenerator(chat).generateComplete(
            world = world,
            cards = shareCards,
            professions = emptyList(),
            shareNames = mapOf("aerolith" to "Аероліт / Aerolith"),
        )

        assertTrue(chat.prompts.single().contains("Аероліт / Aerolith"))
    }

    @Test
    fun expensePayerMechanicReachesThePrompt() = runTest {
        val chat = FakeChat(::answerFor)
        val expense = BoardCard.Expenses("", "", 700, PayerType.FREE_W_OR_MARRIED_M)

        LlmTextGenerator(chat).generateComplete(
            world = world,
            cards = mapOf(BoardCardType.Expenses to mapOf(1 to expense)),
            professions = emptyList(),
        )

        assertTrue(chat.prompts.single().contains("лише незаміжні жінки та одружені чоловіки"))
    }

    @Test
    fun forcedShareSaleMechanicReachesThePrompt() = runTest {
        val chat = FakeChat(::answerFor)
        val event = BoardCard.EventStore.Shares("aerolith", "", 2, forcedSale = true)

        LlmTextGenerator(chat).generateComplete(
            world = world,
            cards = mapOf(BoardCardType.EventStore to mapOf(1 to event)),
            professions = emptyList(),
            shareNames = mapOf("aerolith" to "Аероліт / Aerolith"),
        )

        assertTrue(chat.prompts.single().contains("примусовий продаж усіх акцій"))
        assertTrue(chat.prompts.single().contains("відмовитися чи продати частину не можна"))
    }

    @Test
    fun aDeadModelFailsWithoutInventingTexts() = runTest {
        var checkpoint: Map<String, GeneratedText> = emptyMap()

        assertFailsWith<IllegalStateException> {
            LlmTextGenerator(FakeChat { null }).generateComplete(
                world = world,
                cards = cards,
                professions = emptyList(),
            ) { texts, _, _, _ -> checkpoint = texts }
        }

        assertTrue(checkpoint.values.all { it.cards.isEmpty() })
    }

    @Test
    fun garbageIsThrownAway() = runTest {
        assertFailsWith<IllegalStateException> {
            LlmTextGenerator(FakeChat { "вибачте, я не можу" })
                .generateComplete(world, cards, emptyList())
        }
    }

    @Test
    fun textsForForeignIdsAreIgnored() = runTest {
        val chat = FakeChat {
            """[{"id":999,"uk":{"name":"чуже","description":"чуже"},"en":{"name":"foreign","description":"foreign"}}]"""
        }

        assertFailsWith<IllegalStateException> {
            LlmTextGenerator(chat).generateComplete(world, cards, emptyList())
        }
    }

    @Test
    fun missingBatchIdsAreGeneratedAgain() = runTest {
        var attempt = 0
        val chat = FakeChat { user ->
            attempt += 1
            val ids = promptIds(user)
            val returnedIds = if (attempt == 1) ids.dropLast(1) else ids
            returnedIds.joinToString(prefix = "[", postfix = "]", transform = ::localizedItem)
        }

        val generated = LlmTextGenerator(chat).generateComplete(world, cards, emptyList())

        assertEquals(
            cards.getValue(BoardCardType.SmallBusiness).keys,
            generated.getValue("uk").cards.getValue(BoardCardType.SmallBusiness).keys,
        )
        assertEquals(2, chat.prompts.size)
        assertEquals(listOf("4"), promptIds(chat.prompts[1]))
    }

    @Test
    fun aMissingCardGetsIndividualRepairAfterBatchRetries() = runTest {
        var attempt = 0
        val chat = FakeChat { user ->
            attempt += 1
            val ids = promptIds(user)
            val returnedIds = if (attempt <= 3) ids.filterNot { it == "4" } else ids
            returnedIds.joinToString(prefix = "[", postfix = "]", transform = ::localizedItem)
        }

        val generated = LlmTextGenerator(chat).generateComplete(world, cards, emptyList())

        assertEquals((1..4).toSet(), generated.getValue("uk").cards.getValue(BoardCardType.SmallBusiness).keys)
        assertEquals(listOf("4"), promptIds(chat.prompts[3]))
    }

    @Test
    fun severalMissingCardsAreRepairedInASmallBatch() = runTest {
        var attempt = 0
        val chat = FakeChat { user ->
            attempt += 1
            val ids = promptIds(user)
            val returnedIds = if (attempt <= 3) ids.filter { it == "1" } else ids
            returnedIds.joinToString(prefix = "[", postfix = "]", transform = ::localizedItem)
        }

        val generated = LlmTextGenerator(chat).generateComplete(world, cards, emptyList())

        assertEquals((1..4).toSet(), generated.getValue("uk").cards.getValue(BoardCardType.SmallBusiness).keys)
        assertEquals(listOf("2", "3", "4"), promptIds(chat.prompts[3]))
        assertEquals(4, chat.prompts.size)
    }

    @Test
    fun nestedSingleRepairObjectIsAccepted() = runTest {
        var attempt = 0
        val chat = FakeChat { user ->
            attempt += 1
            val ids = promptIds(user)
            if (attempt <= 3) {
                ids.filterNot { it == "4" }.joinToString(prefix = "[", postfix = "]", transform = ::localizedItem)
            } else {
                """{"card":${localizedItem("999")}}"""
            }
        }

        val generated = LlmTextGenerator(chat).generateComplete(world, cards, emptyList())

        assertEquals("Назва 999", generated.getValue("uk").cards.getValue(BoardCardType.SmallBusiness).getValue(4).name)
        assertEquals(listOf("4"), promptIds(chat.prompts[3]))
    }

    @Test
    fun aSingleObjectCompletesTheLastMissingId() = runTest {
        var attempt = 0
        val chat = FakeChat { user ->
            attempt += 1
            val ids = promptIds(user)
            if (attempt == 1) {
                ids.dropLast(1).joinToString(prefix = "[", postfix = "]", transform = ::localizedItem)
            } else {
                "```json\n${localizedItem(ids.single())}\n```"
            }
        }

        val generated = LlmTextGenerator(chat).generateComplete(world, cards, emptyList())

        assertEquals(listOf("4"), promptIds(chat.prompts[1]))
        assertEquals("Назва 4", generated.getValue("uk").cards.getValue(BoardCardType.SmallBusiness).getValue(4).name)
    }

    @Test
    fun singleRepairKeepsTheRequestedIdWhenTheModelResetsIt() = runTest {
        var attempt = 0
        val chat = FakeChat { user ->
            attempt += 1
            val ids = promptIds(user)
            if (attempt == 1) {
                ids.dropLast(1).joinToString(prefix = "[", postfix = "]", transform = ::localizedItem)
            } else {
                localizedItem("999")
            }
        }

        val generated = LlmTextGenerator(chat).generateComplete(world, cards, emptyList())

        assertEquals("Назва 999", generated.getValue("uk").cards.getValue(BoardCardType.SmallBusiness).getValue(4).name)
        assertTrue(999 !in generated.getValue("uk").cards.getValue(BoardCardType.SmallBusiness))
    }

    @Test
    fun numericStringIdsAreAccepted() = runTest {
        val chat = FakeChat { user ->
            promptIds(user).joinToString(prefix = "[", postfix = "]") { id ->
                localizedItem(id).replace("\"id\":$id", "\"id\":\"$id\"")
            }
        }

        val generated = LlmTextGenerator(chat).generateComplete(world, cards, emptyList())

        assertEquals((1..4).toSet(), generated.getValue("en").cards.getValue(BoardCardType.SmallBusiness).keys)
    }

    @Test
    fun completeItemsFromATruncatedResponseAreKept() = runTest {
        var attempt = 0
        val chat = FakeChat { user ->
            attempt += 1
            val ids = promptIds(user)
            if (attempt == 1) {
                "[${localizedItem(ids[0])},${localizedItem(ids[1])},{\"id\":${ids[2]},\"uk\":"
            } else {
                ids.joinToString(prefix = "[", postfix = "]", transform = ::localizedItem)
            }
        }

        val generated = LlmTextGenerator(chat).generateComplete(world, cards, emptyList())

        assertEquals(listOf("3", "4"), promptIds(chat.prompts[1]))
        assertEquals((1..4).toSet(), generated.getValue("uk").cards.getValue(BoardCardType.SmallBusiness).keys)
    }

    @Test
    fun rejectedDuplicateIsIncludedInTheRetryPrompt() = runTest {
        var attempt = 0
        val chat = FakeChat { user ->
            attempt += 1
            val ids = promptIds(user)
            ids.joinToString(prefix = "[", postfix = "]") { id ->
                if (attempt == 1 && id == "4") localizedItem("1").replace("\"id\":1", "\"id\":4")
                else localizedItem(id)
            }
        }

        val generated = LlmTextGenerator(chat).generateComplete(world, cards, emptyList())

        assertEquals(listOf("4"), promptIds(chat.prompts[1]))
        assertTrue(chat.prompts[1].contains("Назва 1"))
        assertEquals("Назва 4", generated.getValue("uk").cards.getValue(BoardCardType.SmallBusiness).getValue(4).name)
    }

    @Test
    fun longDecksAreAskedInLargerBatches() = runTest {
        val chat = FakeChat(::answerFor)
        val bigDeck = BoardGenerator(world, testBalance()).generate(mapOf(BoardCardType.Shopping to 50))

        val generated = LlmTextGenerator(chat).generateComplete(world, bigDeck, emptyList())

        assertEquals(3, chat.prompts.size)
        assertEquals((1..50).toSet(), generated.getValue("en").cards.getValue(BoardCardType.Shopping).keys)
    }

    @Test
    fun completedBatchesResumeWithoutAnotherRequest() = runTest {
        val firstChat = object : ChatCompletion {
            var calls = 0

            override suspend fun complete(system: String, user: String): String? {
                calls += 1
                if (calls == 2) throw LlmRateLimitException("test", "model", 120_000)
                return answerFor(user)
            }
        }
        var checkpoint: Map<String, GeneratedText> = emptyMap()
        assertFailsWith<LlmRateLimitException> {
            LlmTextGenerator(firstChat, batchSize = 2).generateComplete(
                world = world,
                cards = cards,
                professions = emptyList(),
            ) { texts, _, _, _ -> checkpoint = texts }
        }

        val resumedChat = FakeChat(::answerFor)
        val generated = LlmTextGenerator(resumedChat, batchSize = 2).generateComplete(
            world = world,
            cards = cards,
            professions = emptyList(),
            existingTexts = checkpoint,
        )

        assertEquals(listOf("3", "4"), promptIds(resumedChat.prompts.single()))
        assertEquals((1..4).toSet(), generated.getValue("uk").cards.getValue(BoardCardType.SmallBusiness).keys)
    }

    @Test
    fun fallbackIsUsedAfterRateLimit() = runTest {
        val chat = FallbackChatCompletion(
            listOf(
                ChatCompletion { _, _ -> throw LlmRateLimitException("primary", "model", 120_000) },
                ChatCompletion { _, _ -> "fallback answer" },
            )
        )

        assertEquals("fallback answer", chat.complete("system", "user"))
    }

    @Test
    fun fallbackIsUsedWhenProviderIsUnavailable() = runTest {
        val chat = FallbackChatCompletion(
            listOf(
                ChatCompletion { _, _ -> throw LlmProviderException("primary is unavailable") },
                ChatCompletion { _, _ -> "fallback answer" },
            )
        )

        assertEquals("fallback answer", chat.complete("system", "user"))
    }

    @Test
    fun preciseRateLimitWaitsUntilTheSameRequestSucceeds() = runTest {
        val requests = AtomicInteger()
        val server = HttpServer.create(InetSocketAddress(0), 0).apply {
            createContext("/chat") { exchange ->
                val attempt = requests.incrementAndGet()
                val body = if (attempt <= 10) {
                    exchange.responseHeaders.add("Retry-After", "0.001")
                    "{}"
                } else {
                    """{"choices":[{"message":{"content":"completed"}}]}"""
                }
                val bytes = body.encodeToByteArray()
                exchange.sendResponseHeaders(if (attempt <= 10) 429 else 200, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
            start()
        }
        val retries = mutableListOf<LlmRetryWait>()
        try {
            val chat = HttpChatCompletion(
                provider = LlmProviderSettings(
                    name = "test",
                    url = "http://127.0.0.1:${server.address.port}/chat",
                    key = "key",
                    balanceModel = "model",
                    textModel = "model",
                ),
                model = "model",
                onRetry = { retries += it },
                wait = {},
            )

            assertEquals("completed", chat.complete("system", "user"))
            assertEquals(11, requests.get())
            assertEquals(10, retries.size)
            assertTrue(retries.all { it.delayMillis > 0 })
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun googleRetryInfoWaitsUntilTheSameRequestSucceeds() = runTest {
        val requests = AtomicInteger()
        val server = HttpServer.create(InetSocketAddress(0), 0).apply {
            createContext("/chat") { exchange ->
                val attempt = requests.incrementAndGet()
                val body = if (attempt <= 10) {
                    """{"error":{"code":429,"message":"Quota exceeded. Please retry in 0.001s.","status":"RESOURCE_EXHAUSTED","details":[{"@type":"type.googleapis.com/google.rpc.QuotaFailure","violations":[{"quotaMetric":"generativelanguage.googleapis.com/generate_content_free_tier_requests","quotaId":"GenerateRequestsPerMinutePerProjectPerModel-FreeTier","quotaValue":"20"}]},{"@type":"type.googleapis.com/google.rpc.RetryInfo","retryDelay":"0.001s"}]}}"""
                } else {
                    """{"choices":[{"message":{"content":"completed"}}]}"""
                }
                val bytes = body.encodeToByteArray()
                exchange.sendResponseHeaders(if (attempt <= 10) 429 else 200, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
            start()
        }
        val retries = mutableListOf<LlmRetryWait>()
        try {
            val chat = HttpChatCompletion(
                provider = LlmProviderSettings(
                    name = "gemini",
                    url = "http://127.0.0.1:${server.address.port}/chat",
                    key = "key",
                    balanceModel = "model",
                    textModel = "model",
                ),
                model = "model",
                onRetry = { retries += it },
                wait = {},
            )

            assertEquals("completed", chat.complete("system", "user"))
            assertEquals(11, requests.get())
            assertEquals(10, retries.size)
            assertTrue(retries.all { it.provider == "gemini" && it.delayMillis > 0 })
            assertTrue(retries.all { it.quota?.type == GenerationQuotaType.REQUESTS_PER_MINUTE })
            assertTrue(retries.all { retry ->
                retry.quota?.let { it.limit == 20L && it.used == 20L } == true
            })
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun dailyQuotaWaitsUntilThePacificReset() = runTest {
        val requests = AtomicInteger()
        val server = HttpServer.create(InetSocketAddress(0), 0).apply {
            createContext("/chat") { exchange ->
                val attempt = requests.incrementAndGet()
                val body = if (attempt == 1) {
                    """{"error":{"code":429,"message":"Quota exceeded. Please retry in 4s.","status":"RESOURCE_EXHAUSTED","details":[{"@type":"type.googleapis.com/google.rpc.QuotaFailure","violations":[{"quotaMetric":"generativelanguage.googleapis.com/generate_content_free_tier_requests","quotaId":"GenerateRequestsPerDayPerProjectPerModel-FreeTier","quotaValue":"250"}]},{"@type":"type.googleapis.com/google.rpc.RetryInfo","retryDelay":"4s"}]}}"""
                } else {
                    """{"choices":[{"message":{"content":"completed"}}]}"""
                }
                val bytes = body.encodeToByteArray()
                exchange.sendResponseHeaders(if (attempt == 1) 429 else 200, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
            start()
        }
        val retries = mutableListOf<LlmRetryWait>()
        val now = Instant.parse("2026-08-05T12:00:00Z").toEpochMilli()
        try {
            val chat = HttpChatCompletion(
                provider = LlmProviderSettings(
                    name = "gemini",
                    url = "http://127.0.0.1:${server.address.port}/chat",
                    key = "key",
                    balanceModel = "daily-model",
                    textModel = "daily-model",
                ),
                model = "daily-model",
                onRetry = { retries += it },
                wait = {},
                quotaTracker = LlmQuotaTracker { now },
                nowEpochMs = { now },
            )

            assertEquals("completed", chat.complete("system", "user"))
            assertEquals(2, requests.get())
            assertEquals(1, retries.size)
            assertTrue(retries.single().delayMillis > 18 * 60 * 60 * 1_000)
            assertEquals(
                GenerationQuotaType.REQUESTS_PER_DAY,
                retries.single().quota?.type,
            )
            assertEquals(250, retries.single().quota?.limit)
        } finally {
            server.stop(0)
        }
    }
}
