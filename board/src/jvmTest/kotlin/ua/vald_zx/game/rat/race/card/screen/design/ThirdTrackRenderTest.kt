package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.datetime.LocalDateTime
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.screen.board.boardLayersOf
import ua.vald_zx.game.rat.race.card.screen.board.calculateBoardLayout
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.CoreTrackIds
import ua.vald_zx.game.rat.race.card.shared.TrackDefinition
import ua.vald_zx.game.rat.race.card.shared.TrackId
import ua.vald_zx.game.rat.race.card.shared.TrackTransition
import ua.vald_zx.game.rat.race.card.shared.TrackVisualHint
import ua.vald_zx.game.rat.race.card.shared.defaultTrackDefinition
import ua.vald_zx.game.rat.race.card.shared.BoardLayer
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.shared.toCellInstance
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val eliteTrackId = TrackId("elite")

private fun eliteTrack(): TrackDefinition {
    val places = listOf(
        PlaceType.Start,
        PlaceType.Chance,
        PlaceType.BigBusiness,
        PlaceType.Salary,
        PlaceType.Store,
        PlaceType.Expenses,
        PlaceType.Rest,
        PlaceType.Chance,
        PlaceType.Shopping,
        PlaceType.Salary,
    )
    return TrackDefinition(
        id = eliteTrackId,
        order = 2,
        cells = places.mapIndexed { index, place -> place.toCellInstance("elite-$index") },
        visual = TrackVisualHint(horizontalCells = 22, verticalCells = 16),
    )
}

private fun threeTrackBoard() = Board(
    id = "three-tracks",
    name = "three-tracks",
    loanLimit = 0,
    businessLimit = 0,
    createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
    cards = emptyMap(),
    tracks = listOf(
        BoardLayer.INNER.defaultTrackDefinition(),
        BoardLayer.OUTER.defaultTrackDefinition(),
        eliteTrack(),
    ),
    transitions = listOf(
        TrackTransition("inner-outer", CoreTrackIds.Inner, CoreTrackIds.Outer),
        TrackTransition("outer-elite", CoreTrackIds.Outer, eliteTrackId),
    ),
)

@OptIn(ExperimentalTestApi::class)
class ThirdTrackRenderTest {

    @Test
    fun layoutBuildsEveryTrackWithoutLegacyLayerMapping() {
        val layers = boardLayersOf(threeTrackBoard())
        val layout = calculateBoardLayout(DpSize(900.dp, 620.dp), isVertical = false, layers = layers)
        requireNotNull(layout)

        assertEquals(
            listOf(eliteTrackId, CoreTrackIds.Outer, CoreTrackIds.Inner),
            layout.routes.map { it.trackId },
            "треки розкладаються від зовнішнього до внутрішнього незалежно від їх кількості",
        )
        assertEquals(
            eliteTrack().cells.size,
            layout.routes.first().cellCount,
            "третій трек зберігає власну кількість клітинок",
        )
        assertTrue(
            layout.routes.first().isOutermost,
            "новий трек стає зовнішнім за своїм order",
        )
    }

    @Test
    fun everyCellOfThirdTrackStaysInsideItsFrame() {
        val layers = boardLayersOf(threeTrackBoard())
        listOf(
            DpSize(900.dp, 620.dp) to false,
            DpSize(620.dp, 900.dp) to true,
        ).forEach { (size, isVertical) ->
            val layout = calculateBoardLayout(size, isVertical = isVertical, layers = layers)
            requireNotNull(layout)
            layout.routes.forEach { route ->
                route.places.forEach { (index, place) ->
                    assertTrue(
                        place.offset.x >= 0.dp && place.offset.y >= 0.dp,
                        "клітинка $index виїхала за верхній/лівий край ${route.trackId.value}",
                    )
                    assertTrue(
                        place.offset.x + place.size.width <= route.size.width + 0.5.dp &&
                                place.offset.y + place.size.height <= route.size.height + 0.5.dp,
                        "клітинка $index виїхала за правий/нижній край ${route.trackId.value}",
                    )
                }
            }
        }
    }

    @Test
    fun allThreeTracksRender() = runComposeUiTest {
        val layers = boardLayersOf(threeTrackBoard())
        val layout = calculateBoardLayout(DpSize(760.dp, 520.dp), isVertical = false, layers = layers)!!
        setContent {
            AppTheme(forceDark = true) {
                Box(
                    Modifier
                        .size(760.dp, 520.dp)
                        .background(Color.Black)
                        .testTag("board")
                ) {
                    layout.routes.forEach { route ->
                        DesignTrackForTest(
                            route,
                            if (route.trackId == eliteTrackId) CellSurface.Tile else CellSurface.Engraved,
                        )
                    }
                }
            }
        }
        waitForIdle()
        onNodeWithTag("board").assertIsDisplayed()
    }
}
