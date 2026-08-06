package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import ua.vald_zx.game.rat.race.card.screen.board.boardLayersOf
import ua.vald_zx.game.rat.race.card.screen.board.calculateBoardLayout
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.BoardLayer
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.shared.code
import ua.vald_zx.game.rat.race.card.shared.defaultTrackDefinition
import ua.vald_zx.game.rat.race.card.shared.placesOf
import ua.vald_zx.game.rat.race.card.shared.toCellInstance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeneratedBoardLayoutTest {

    private val swapped = BoardLayer.INNER.places.toMutableList().also { places ->
        val chance = places.indexOf(PlaceType.Chance)
        val store = places.indexOf(PlaceType.Store)
        places[chance] = PlaceType.Store
        places[store] = PlaceType.Chance
    }

    private fun board(generated: Boolean) = Board(
        id = "b",
        name = "b",
        loanLimit = 0,
        businessLimit = 0,
        createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
        cards = emptyMap(),
        generatedPlaces = if (generated) mapOf(BoardLayer.INNER to swapped.map { it.code() }) else emptyMap(),
    )

    @Test
    fun theTrackFollowsTheGeneratedOrder() {
        val layout = calculateBoardLayout(
            boardSize = DpSize(900.dp, 620.dp),
            isVertical = false,
            layers = boardLayersOf(board(generated = true)),
        )
        requireNotNull(layout)
        swapped.forEachIndexed { index, place ->
            val drawn = layout.innerRoute.places.first { it.index == index }.place.type
            assertEquals(place, drawn, "клітинка $index намальована не з розкладки дошки")
        }
        assertTrue(swapped != BoardLayer.INNER.places, "перевірка нічого не міняє")
    }

    @Test
    fun aBoardWithoutGenerationKeepsTheStaticTrack() {
        val plain = board(generated = false)
        assertEquals(BoardLayer.INNER.places, plain.placesOf(BoardLayer.INNER))
        assertEquals(BoardLayer.OUTER.places, plain.placesOf(BoardLayer.OUTER))
    }

    @Test
    fun aBrokenLayoutFallsBackToTheStaticTrack() {
        val broken = board(generated = true).copy(
            generatedPlaces = mapOf(BoardLayer.INNER to listOf("Chance", "тут-такого-немає")),
        )
        assertEquals(BoardLayer.INNER.places, broken.placesOf(BoardLayer.INNER))
    }

    @Test
    fun routeGeometryComesFromTrackDefinition() {
        val definition = BoardLayer.INNER.defaultTrackDefinition().copy(
            cells = swapped.mapIndexed { index, place -> place.toCellInstance("inner-$index") },
            visual = ua.vald_zx.game.rat.race.card.shared.TrackVisualHint(30, 20),
        )
        val route = boardLayersOf(
            board(generated = false).copy(trackDefinitions = mapOf(BoardLayer.INNER to definition)),
        ).layers.getValue(BoardLayer.INNER)

        assertEquals(30, route.horizontalCells)
        assertEquals(20, route.verticalCells)
        assertEquals(swapped, route.places)
    }
}
