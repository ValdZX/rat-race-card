package ua.vald_zx.game.rat.race.server

import kotlinx.coroutines.test.runTest
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LlmTextGeneratorTest {

    private val world = BoardGeneration(enabled = true, theme = "космічна колонія", seed = 7)
    private val cards = BoardGenerator(world).generate(mapOf(BoardCardType.SmallBusiness to 4))
    private val professions = BoardGenerator(world).generateProfessions()

    private class FakeChat(val answer: (String) -> String?) : ChatCompletion {
        val prompts = mutableListOf<String>()
        override suspend fun complete(system: String, user: String): String? {
            prompts += user
            return answer(user)
        }
    }

    private fun answerFor(user: String): String {
        val ids = Regex("^(\\d+)\\. ", RegexOption.MULTILINE).findAll(user).map { it.groupValues[1] }
        return ids.joinToString(
            prefix = "```json\n[",
            postfix = "]\n```",
        ) { """{"id":$it,"name":"Назва $it","description":"Опис $it"}""" }
    }

    @Test
    fun answersBecomeCardTexts() = runTest {
        val generated = LlmTextGenerator(FakeChat(::answerFor)).localize(world, cards, professions, "uk")
        val deck = generated.cards.getValue(BoardCardType.SmallBusiness)
        assertEquals((1..4).toSet(), deck.keys)
        assertEquals("Назва 1", deck.getValue(1).name)
        assertEquals("Опис 1", deck.getValue(1).description)
        assertEquals(professions.map { it.id }.toSet(), generated.professions.keys)
    }

    @Test
    fun theWorldAndTheLanguageReachThePrompt() = runTest {
        val chat = FakeChat(::answerFor)
        LlmTextGenerator(chat).localize(world, cards, professions, "en")
        assertTrue(chat.prompts.all { "космічна колонія" in it }, "світ не потрапив у запит")
        assertTrue(chat.prompts.all { "англійська" in it }, "мова не потрапила у запит")
    }

    @Test
    fun aDeadModelLeavesTheGeneratedTextsEmpty() = runTest {
        val generated = LlmTextGenerator(FakeChat { null }).localize(world, cards, professions, "uk")
        assertTrue(generated.cards.isEmpty(), "нежива модель підмінила тексти")
        assertTrue(generated.professions.isEmpty())
    }

    @Test
    fun garbageIsThrownAway() = runTest {
        val generated = LlmTextGenerator(FakeChat { "вибачте, я не можу" })
            .localize(world, cards, professions, "uk")
        assertTrue(generated.cards.isEmpty(), "сміття потрапило в картки")
    }

    @Test
    fun textsForForeignIdsAreIgnored() = runTest {
        val chat = FakeChat { """[{"id":999,"name":"чуже","description":"чуже"}]""" }
        val generated = LlmTextGenerator(chat).localize(world, cards, professions, "uk")
        assertTrue(generated.cards.isEmpty(), "картка з чужим id потрапила в колоду")
    }

    @Test
    fun longDecksAreAskedInBatches() = runTest {
        val chat = FakeChat(::answerFor)
        val bigDeck = BoardGenerator(world).generate(mapOf(BoardCardType.Shopping to 30))
        val generated = LlmTextGenerator(chat, batchSize = 12)
            .localize(world, bigDeck, emptyList(), "uk")
        assertEquals(3, chat.prompts.size, "колода з 30 карток пішла не трьома запитами")
        assertEquals((1..30).toSet(), generated.cards.getValue(BoardCardType.Shopping).keys)
    }
}
