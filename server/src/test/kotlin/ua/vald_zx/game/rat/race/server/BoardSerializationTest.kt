package ua.vald_zx.game.rat.race.server

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import ua.vald_zx.game.rat.race.card.shared.BoardLayer
import ua.vald_zx.game.rat.race.card.shared.CardLink
import ua.vald_zx.game.rat.race.card.shared.CardText
import ua.vald_zx.game.rat.race.card.shared.GeneratedText
import ua.vald_zx.game.rat.race.card.shared.cardOrNull
import ua.vald_zx.game.rat.race.card.shared.placesOf
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
    fun theGeneratedWorldSurvivesTheWire() {
        val world = BoardGeneration(enabled = true, theme = "тема", locality = "місто", epoch = "епоха", seed = 7)
        val generator = BoardGenerator(world)
        val generated = board(generator.generate(BoardCardType.entries.associateWith { 5 })).copy(
            generatedProfessions = generator.generateProfessions(),
            generatedPlaces = generator.generatePlaces(),
            generatedTexts = mapOf(
                "uk" to GeneratedText(
                    cards = mapOf(BoardCardType.Shopping to mapOf(1 to CardText("Назва", "Опис"))),
                    professions = mapOf(1 to "Пілотеса"),
                ),
            ),
        )

        val decoded = json.decodeFromString(Board.serializer(), json.encodeToString(Board.serializer(), generated))

        assertEquals(generated.generatedProfessions, decoded.generatedProfessions)
        assertEquals(generated.generatedPlaces, decoded.generatedPlaces)
        assertEquals(generated.generatedTexts, decoded.generatedTexts)
        BoardLayer.entries.forEach { layer ->
            assertEquals(generated.placesOf(layer), decoded.placesOf(layer), "коло $layer не пережило JSON")
            assertEquals(layer.places.size, decoded.placesOf(layer).size)
        }
    }

    @Test
    fun localizedTextReplacesTheGeneratedOne() {
        val generator = BoardGenerator(BoardGeneration(enabled = true, theme = "тема", seed = 7))
        val generated = board(generator.generate(mapOf(BoardCardType.Shopping to 3))).copy(
            generatedTexts = mapOf(
                "uk" to GeneratedText(
                    cards = mapOf(BoardCardType.Shopping to mapOf(1 to CardText(description = "Український опис"))),
                ),
                "en" to GeneratedText(
                    cards = mapOf(BoardCardType.Shopping to mapOf(1 to CardText(description = "English text"))),
                ),
            ),
        )
        val link = CardLink(BoardCardType.Shopping, 1)

        val ukrainian = generated.cardOrNull(link, "uk") as BoardCard.Shopping
        val english = generated.cardOrNull(link, "en-US") as BoardCard.Shopping
        val unknown = generated.cardOrNull(link, "de") as BoardCard.Shopping
        val untouched = generated.cardOrNull(CardLink(BoardCardType.Shopping, 2), "uk") as BoardCard.Shopping

        assertEquals("Український опис", ukrainian.description)
        assertEquals("English text", english.description)
        assertEquals("Український опис", unknown.description)
        assertEquals(ukrainian.price, english.price)
        assertTrue(untouched.description.isNotBlank(), "картка без перекладу лишилась без тексту")
    }

    @Test
    fun aBoardWithoutGenerationStaysSmall() {
        val encoded = json.encodeToString(Board.serializer(), board(emptyMap()))
        val decoded = json.decodeFromString(Board.serializer(), encoded)
        assertTrue(decoded.generatedCards.isEmpty())
    }
}
