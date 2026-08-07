package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.TrackDefinition
import ua.vald_zx.game.rat.race.card.shared.TrackId
import ua.vald_zx.game.rat.race.card.shared.TrackVisualHint
import ua.vald_zx.game.rat.race.card.shared.toCellInstance
import ua.vald_zx.game.rat.race.card.shared.toPlaceType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrackLayoutEngineTest {
    private val tracks = listOf(
        track("inner", 0, 17, 28, 18),
        track("outer", 1, 23, 30, 20),
        track("elite", 2, 13, 24, 16),
    )

    @Test
    fun rendersThreeNestedTracksInLandscape() {
        verify(TrackLayoutEngine().layout(tracks, TrackViewport(1_200f, 800f), portrait = false))
    }

    @Test
    fun rendersThreeNestedTracksInPortrait() {
        verify(TrackLayoutEngine().layout(tracks, TrackViewport(800f, 1_200f), portrait = true))
    }

    @Test
    fun boardRendererConsumesEveryDynamicTrackInBothOrientations() {
        val layers = BoardLayers(
            layers = tracks.associate { track ->
                track.id to BoardRoute(
                    horizontalCells = track.visual.horizontalCells,
                    verticalCells = track.visual.verticalCells,
                    places = track.cells.map { it.toPlaceType() },
                )
            },
            order = tracks.associate { it.id to it.order },
        )

        val landscape = calculateBoardLayout(DpSize(1_200.dp, 800.dp), false, layers)
        val portrait = calculateBoardLayout(DpSize(800.dp, 1_200.dp), true, layers)

        assertEquals(tracks.map { it.id }.toSet(), landscape?.routes?.map { it.trackId }?.toSet())
        assertEquals(tracks.map { it.id }.toSet(), portrait?.routes?.map { it.trackId }?.toSet())
        assertEquals(3, landscape?.routes?.size)
        assertEquals(3, portrait?.routes?.size)
    }

    @Test
    fun rendererUsesDeckDefinitionsProvidedByFeatureRuntime() {
        val coreDecks = listOf(BoardCardType.Chance, BoardCardType.Expenses, BoardCardType.Shopping)

        val layout = calculateBoardLayout(
            boardSize = DpSize(1_200.dp, 800.dp),
            isVertical = false,
            deckTypes = coreDecks,
        )

        assertEquals(coreDecks.toSet(), layout?.cardDecks?.slots?.map { it.type }?.toSet())
        assertEquals(coreDecks.size * 2, layout?.cardDecks?.slots?.size)
    }

    private fun verify(frames: List<TrackFrame>) {
        assertEquals(listOf("elite", "outer", "inner"), frames.map { it.trackId.value })
        assertEquals(tracks.sumOf { it.cells.size }, frames.sumOf { it.cells.size })
        frames.zipWithNext().forEach { (outside, inside) ->
            assertTrue(inside.width < outside.width)
            assertTrue(inside.height < outside.height)
            assertTrue(inside.left >= outside.left)
            assertTrue(inside.top >= outside.top)
        }
        frames.forEach { frame ->
            frame.cells.forEach { point ->
                assertTrue(point.x in frame.left..frame.left + frame.width)
                assertTrue(point.y in frame.top..frame.top + frame.height)
            }
        }
    }

    private fun track(id: String, order: Int, count: Int, horizontal: Int, vertical: Int) = TrackDefinition(
        id = TrackId(id),
        order = order,
        cells = List(count) { PlaceType.Start.toCellInstance("$id-$it") },
        visual = TrackVisualHint(horizontal, vertical),
    )
}
