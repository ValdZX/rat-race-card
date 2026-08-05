package ua.vald_zx.game.rat.race.server

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.test.runTest
import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import ua.vald_zx.game.rat.race.card.shared.GeneratedText
import ua.vald_zx.game.rat.race.card.shared.GenerationQuotaType
import ua.vald_zx.game.rat.race.card.shared.PayerType
import ua.vald_zx.game.rat.race.card.shared.ShopType
import java.net.InetSocketAddress
import java.time.Instant
import java.util.Collections
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
        val prompts: MutableList<String> = Collections.synchronizedList(mutableListOf())

        override suspend fun complete(system: String, user: String): String? {
            prompts += user
            return answer(user)
        }
    }

    private fun poolMember(name: String, completion: ChatCompletion) =
        LlmProviderPool.Member(provider = name, model = "model", completion = completion)

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
    fun everyDeckPromptExplainsWhatTheVisibleTextMustContain() = runTest {
        val representativeCards = listOf(
            BoardCardType.SmallBusiness to BoardCard.SmallBusiness("", "", 100, 10),
            BoardCardType.Shopping to BoardCard.Shopping("", 100, ShopType.AUTO, ""),
            BoardCardType.Chance to BoardCard.Chance.RandomJob("", 100),
            BoardCardType.EventStore to BoardCard.EventStore.Announcement(""),
            BoardCardType.Deputy to BoardCard.Deputy("", false),
        )

        val prompts = representativeCards.map { (type, card) ->
            val chat = FakeChat(::answerFor)
            LlmTextGenerator(chat).generateComplete(
                world = world,
                cards = mapOf(type to mapOf(1 to card)),
                professions = emptyList(),
            )
            chat.prompts.single()
        }.joinToString("\n")

        assertTrue(prompts.contains("Name відображається гравцю як головний заголовок"))
        assertTrue(prompts.contains("description пояснює, чим він займається і звідки отримує дохід"))
        assertTrue(prompts.contains("description пояснює, що саме купують і навіщо"))
        assertTrue(prompts.contains("можливість, підробіток, актив або корупційну схему"))
        assertTrue(prompts.contains("конкретним заголовком ринкової новини"))
        assertTrue(prompts.contains("посадовця, роль або скандал"))
        assertTrue(prompts.contains("не додавай нових чисел, умов або наслідків"))
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
        assertTrue(chat.prompts.single().contains("name коротко й конкретно називає предмет оплати"))
        assertTrue(chat.prompts.single().contains("не повторюй їх і не замінюй ними причину витрати"))
    }

    @Test
    fun mechanicalExpenseTextIsRegeneratedWithASpecificPurpose() = runTest {
        var attempt = 0
        val chat = FakeChat { user ->
            attempt += 1
            val id = promptIds(user).single()
            if (attempt == 1) {
                """[{"id":$id,"uk":{"name":"Обов'язкова витрата","description":"Обов'язкова витрата 4000 лише для гравців за умовою: платять усі без винятку."},"en":{"name":"Mandatory expense","description":"Mandatory expense for all players. Everyone pays without exception."}}]"""
            } else {
                """[{"id":$id,"uk":{"name":"Ремонт купола","description":"Метеорит залишив тріщину в захисному куполі колонії."},"en":{"name":"Dome repair","description":"A meteorite cracked the colony's protective dome."}}]"""
            }
        }
        val expense = BoardCard.Expenses("", "", 4_000, PayerType.ALL)

        val generated = LlmTextGenerator(chat).generateComplete(
            world = world,
            cards = mapOf(BoardCardType.Expenses to mapOf(1 to expense)),
            professions = emptyList(),
        )

        assertEquals(2, chat.prompts.size)
        assertEquals(
            "Ремонт купола",
            generated.getValue("uk").cards.getValue(BoardCardType.Expenses).getValue(1).name,
        )
    }

    @Test
    fun genericCategoryTitleIsRegeneratedForOtherDecks() = runTest {
        var attempt = 0
        val chat = FakeChat { user ->
            attempt += 1
            val id = promptIds(user).single()
            if (attempt == 1) {
                """[{"id":$id,"uk":{"name":"Покупка","description":"Гравець купує актив."},"en":{"name":"Shopping","description":"The player buys an asset."}}]"""
            } else {
                """[{"id":$id,"uk":{"name":"Місячний всюдихід","description":"Маневрений транспорт для поїздок між куполами."},"en":{"name":"Lunar rover","description":"Agile transport for trips between colony domes."}}]"""
            }
        }
        val shopping = BoardCard.Shopping(
            description = "",
            price = 4_000,
            shopType = ShopType.AUTO,
            credit = "",
        )

        val generated = LlmTextGenerator(chat).generateComplete(
            world = world,
            cards = mapOf(BoardCardType.Shopping to mapOf(1 to shopping)),
            professions = emptyList(),
        )

        assertEquals(2, chat.prompts.size)
        assertEquals(
            "Місячний всюдихід",
            generated.getValue("uk").cards.getValue(BoardCardType.Shopping).getValue(1).name,
        )
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
                if (attempt == 1 && id == "4") {
                    localizedItem(id).replace("Назва 4", "Назва 1").replace("Name 4", "Name 1")
                }
                else localizedItem(id)
            }
        }

        val generated = LlmTextGenerator(chat).generateComplete(world, cards, emptyList())

        assertEquals(listOf("4"), promptIds(chat.prompts[1]))
        assertTrue(chat.prompts[1].contains("Назва 1"))
        assertEquals("Назва 4", generated.getValue("uk").cards.getValue(BoardCardType.SmallBusiness).getValue(4).name)
    }

    @Test
    fun repeatedNamesAreRegeneratedForEveryCardType() = runTest {
        var attempt = 0
        val chat = FakeChat { user ->
            attempt += 1
            promptIds(user).joinToString(prefix = "[", postfix = "]") { id ->
                val suffix = if (attempt == 1) "" else " $id"
                """{"id":$id,"uk":{"name":"Акції Аероліту$suffix","description":"Пропозиція $id"},""" +
                        """"en":{"name":"Aerolith shares$suffix","description":"Offer $id"}}"""
            }
        }
        val shareCards = mapOf(
            BoardCardType.Chance to mapOf<Int, BoardCard>(
                67 to BoardCard.Chance.Shares("", 50, 1_000, "aerolith"),
                68 to BoardCard.Chance.Shares("", 75, 500, "aerolith"),
            ),
        )

        val generated = LlmTextGenerator(chat).generateComplete(world, shareCards, emptyList())

        assertEquals(setOf(67, 68), generated.getValue("uk").cards.getValue(BoardCardType.Chance).keys)
        assertEquals(listOf("68"), promptIds(chat.prompts[1]))
        assertEquals(2, chat.prompts.size)
    }

    @Test
    fun repeatedDescriptionsAreAcceptedForMechanicallyIdenticalCards() = runTest {
        val chat = FakeChat { user ->
            promptIds(user).joinToString(prefix = "[", postfix = "]") { id ->
                """{"id":$id,"uk":{"name":"Тихий ринок $id","description":"На ринку тихий день."},""" +
                        """"en":{"name":"Quiet market $id","description":"A quiet day at the market."}}"""
            }
        }
        val eventCards = mapOf(
            BoardCardType.EventStore to mapOf<Int, BoardCard>(
                59 to BoardCard.EventStore.Announcement(""),
                64 to BoardCard.EventStore.Announcement(""),
                70 to BoardCard.EventStore.Announcement(""),
                72 to BoardCard.EventStore.Announcement(""),
            ),
        )

        val generated = LlmTextGenerator(chat).generateComplete(world, eventCards, emptyList())

        assertEquals(
            setOf(59, 64, 70, 72),
            generated.getValue("uk").cards.getValue(BoardCardType.EventStore).keys,
        )
        assertEquals(1, chat.prompts.size)
    }

    @Test
    fun repeatedDescriptionsAreRejectedForDifferentCards() = runTest {
        var attempt = 0
        val chat = FakeChat { user ->
            attempt += 1
            promptIds(user).joinToString(prefix = "[", postfix = "]") { id ->
                val suffix = if (attempt == 1) "" else " $id"
                """{"id":$id,"uk":{"name":"Продаж землі $id","description":"Ціна землі змінилася$suffix."},""" +
                        """"en":{"name":"Land sale $id","description":"The land price changed$suffix."}}"""
            }
        }
        val eventCards = mapOf(
            BoardCardType.EventStore to mapOf<Int, BoardCard>(
                1 to BoardCard.EventStore.Land("", 100),
                2 to BoardCard.EventStore.Land("", 200),
            ),
        )

        LlmTextGenerator(chat).generateComplete(world, eventCards, emptyList())

        assertEquals(listOf("2"), promptIds(chat.prompts[1]))
        assertEquals(2, chat.prompts.size)
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
    fun theSameNameInAnotherDeckIsAccepted() = runTest {
        val chat = FakeChat { user ->
            val deck = if ("малий бізнес" in user) "small" else "medium"
            promptIds(user).joinToString(prefix = "[", postfix = "]") { id ->
                """{"id":$id,"uk":{"name":"Спільна назва","description":"Опис $deck"},""" +
                        """"en":{"name":"Shared name","description":"Description $deck"}}"""
            }
        }
        val twoDecks = BoardGenerator(world, testBalance()).generate(
            mapOf(BoardCardType.SmallBusiness to 1, BoardCardType.MediumBusiness to 1)
        )

        val generated = LlmTextGenerator(chat).generateComplete(world, twoDecks, emptyList())

        val uk = generated.getValue("uk").cards
        assertEquals("Спільна назва", uk.getValue(BoardCardType.SmallBusiness).getValue(1).name)
        assertEquals("Спільна назва", uk.getValue(BoardCardType.MediumBusiness).getValue(1).name)
    }

    @Test
    fun theNextProviderAnswersWhenTheFirstIsRateLimited() = runTest {
        val pool = LlmProviderPool(
            members = listOf(
                poolMember("primary", ChatCompletion { _, _ ->
                    throw LlmRateLimitException("primary", "model", 120_000)
                }),
                poolMember("fallback", ChatCompletion { _, _ -> "fallback answer" }),
            ),
            wait = {},
        )

        assertEquals("fallback answer", pool.complete("system", "user"))
    }

    @Test
    fun consecutiveRequestsRotateAcrossProviders() = runTest {
        val pool = LlmProviderPool(
            members = listOf("groq", "cerebras", "mistral", "openrouter").map { provider ->
                poolMember(provider, ChatCompletion { _, _ -> provider })
            },
        )

        val answers = List(8) { pool.complete("system", "user") }

        assertEquals(
            listOf("groq", "cerebras", "mistral", "openrouter", "groq", "cerebras", "mistral", "openrouter"),
            answers,
        )
    }

    @Test
    fun anUnavailableProviderIsSkipped() = runTest {
        val pool = LlmProviderPool(
            members = listOf(
                poolMember("primary", ChatCompletion { _, _ ->
                    throw LlmProviderException("primary is unavailable")
                }),
                poolMember("fallback", ChatCompletion { _, _ -> "fallback answer" }),
            ),
            wait = {},
        )

        assertEquals("fallback answer", pool.complete("system", "user"))
    }

    @Test
    fun aRateLimitedProviderIsNotCalledAgainWhileItCoolsDown() = runTest {
        var now = 0L
        val primaryCalls = AtomicInteger()
        val fallbackCalls = AtomicInteger()
        val pool = LlmProviderPool(
            members = listOf(
                poolMember("primary", ChatCompletion { _, _ ->
                    primaryCalls.incrementAndGet()
                    throw LlmRateLimitException("primary", "model", 60_000)
                }),
                poolMember("fallback", ChatCompletion { _, _ ->
                    fallbackCalls.incrementAndGet()
                    "answer"
                }),
            ),
            nowEpochMs = { now },
            wait = { now += it },
        )

        repeat(3) { assertEquals("answer", pool.complete("system", "user")) }

        assertEquals(1, primaryCalls.get())
        assertEquals(3, fallbackCalls.get())
    }

    @Test
    fun aDailyQuotaFailsTheRequestInsteadOfWaiting() = runTest {
        var now = 0L
        var waited = 0L
        val pool = LlmProviderPool(
            members = listOf(
                poolMember("only", ChatCompletion { _, _ ->
                    throw LlmRateLimitException("only", "model", 20 * 60 * 60 * 1_000)
                }),
            ),
            nowEpochMs = { now },
            wait = { waited += it; now += it },
        )

        assertFailsWith<LlmRateLimitException> { pool.complete("system", "user") }
        assertEquals(0, waited)
    }

    @Test
    fun aMinuteQuotaIsWaitedOutWhenNoProviderIsFree() = runTest {
        var now = 0L
        var waited = 0L
        var attempt = 0
        val pool = LlmProviderPool(
            members = listOf(
                poolMember("only", ChatCompletion { _, _ ->
                    attempt += 1
                    if (attempt == 1) throw LlmRateLimitException("only", "model", 30_000)
                    "answer"
                }),
            ),
            nowEpochMs = { now },
            wait = { waited += it; now += it },
        )

        assertEquals("answer", pool.complete("system", "user"))
        assertEquals(30_000, waited)
    }

    @Test
    fun aRateLimitIsReportedInsteadOfBeingWaitedOut() = runTest {
        val requests = AtomicInteger()
        val server = rateLimitedServer { requests.incrementAndGet() }
        try {
            val chat = HttpChatCompletion(
                provider = testProvider(server.address.port, "test"),
                model = "model",
            )

            val failure = assertFailsWith<LlmRateLimitException> { chat.complete("system", "user") }

            assertEquals(1, requests.get())
            assertTrue(failure.retryAfterMillis > 0)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun googleQuotaDetailsTravelWithTheRateLimit() = runTest {
        val server = quotaServer(
            """{"quotaMetric":"generativelanguage.googleapis.com/generate_content_free_tier_requests","quotaId":"GenerateRequestsPerMinutePerProjectPerModel-FreeTier","quotaValue":"20"}""",
            "0.001s",
        )
        val quotas = mutableListOf<LlmQuotaSnapshot>()
        try {
            val chat = HttpChatCompletion(
                provider = testProvider(server.address.port, "gemini"),
                model = "model",
                onQuota = { quotas += it },
            )

            val failure = assertFailsWith<LlmRateLimitException> { chat.complete("system", "user") }

            assertEquals(GenerationQuotaType.REQUESTS_PER_MINUTE, failure.quota?.type)
            assertEquals(20L, failure.quota?.limit)
            assertEquals(1, quotas.size)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun aDailyQuotaCoolsDownUntilThePacificReset() = runTest {
        val server = quotaServer(
            """{"quotaMetric":"generativelanguage.googleapis.com/generate_content_free_tier_requests","quotaId":"GenerateRequestsPerDayPerProjectPerModel-FreeTier","quotaValue":"250"}""",
            "4s",
        )
        val now = Instant.parse("2026-08-05T12:00:00Z").toEpochMilli()
        try {
            val chat = HttpChatCompletion(
                provider = testProvider(server.address.port, "gemini"),
                model = "daily-model",
                quotaTracker = LlmQuotaTracker { now },
                nowEpochMs = { now },
            )

            val failure = assertFailsWith<LlmRateLimitException> { chat.complete("system", "user") }

            assertEquals(GenerationQuotaType.REQUESTS_PER_DAY, failure.quota?.type)
            assertEquals(250L, failure.quota?.limit)
            assertTrue(failure.retryAfterMillis > 18 * 60 * 60 * 1_000)
        } finally {
            server.stop(0)
        }
    }

    private fun testProvider(port: Int, name: String) = LlmProviderSettings(
        name = name,
        url = "http://127.0.0.1:$port/chat",
        key = "key",
        balanceModel = "model",
        textModel = "model",
    )

    private fun rateLimitedServer(onRequest: () -> Unit): HttpServer =
        HttpServer.create(InetSocketAddress(0), 0).apply {
            createContext("/chat") { exchange ->
                onRequest()
                exchange.responseHeaders.add("Retry-After", "0.001")
                val bytes = "{}".encodeToByteArray()
                exchange.sendResponseHeaders(429, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
            start()
        }

    private fun quotaServer(violation: String, retryDelay: String): HttpServer =
        HttpServer.create(InetSocketAddress(0), 0).apply {
            createContext("/chat") { exchange ->
                val body = """{"error":{"code":429,"message":"Quota exceeded.","status":"RESOURCE_EXHAUSTED",""" +
                        """"details":[{"@type":"type.googleapis.com/google.rpc.QuotaFailure",""" +
                        """"violations":[$violation]},""" +
                        """{"@type":"type.googleapis.com/google.rpc.RetryInfo","retryDelay":"$retryDelay"}]}}"""
                val bytes = body.encodeToByteArray()
                exchange.sendResponseHeaders(429, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
            start()
        }
}
