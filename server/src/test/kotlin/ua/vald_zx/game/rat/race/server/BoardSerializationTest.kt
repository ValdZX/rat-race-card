package ua.vald_zx.game.rat.race.server

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BoardSerializationTest {

    private val json = Json

    private fun board(generated: Map<BoardCardType, Map<Int, BoardCard>>) = Board(
        id = "b",
        name = "b",
        loanLimit = 0,
        businessLimit = 0,
        createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
        cards = emptyMap(),
        generation = BoardGeneration(enabled = true, theme = "тема", seed = 7),
        generatedCards = generated,
    )

    @Test
    fun everyGeneratedCardKindSurvivesTheWire() {
        val generated = BoardGenerator(
            BoardGeneration(enabled = true, theme = "тема", locality = "місто", epoch = "епоха", seed = 7)
        ).generate(BoardCardType.entries.associateWith { 30 })

        val kinds = generated.values.flatMap { it.values }.map { it::class.simpleName }.toSet()
        assertTrue(kinds.size > 5, "згенеровано замало різних типів карток для перевірки: $kinds")

        val encoded = json.encodeToString(Board.serializer(), board(generated))
        val decoded = json.decodeFromString(Board.serializer(), encoded)
        assertEquals(generated, decoded.generatedCards)
    }

    @Test
    fun theDiscriminatorDoesNotEatTheCardType() {
        val card: BoardCard = BoardCard.Chance.Estate("дім", "опис", 1000)
        val encoded = json.encodeToString(BoardCard.serializer(), card)
        assertTrue("cardType" in encoded, "поле type зникло з JSON: $encoded")
        assertEquals(card, json.decodeFromString(BoardCard.serializer(), encoded))
    }

    @Test
    fun aBoardWithoutGenerationStaysSmall() {
        val encoded = json.encodeToString(Board.serializer(), board(emptyMap()))
        val decoded = json.decodeFromString(Board.serializer(), encoded)
        assertTrue(decoded.generatedCards.isEmpty())
    }
}
