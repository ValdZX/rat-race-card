package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.screen.board.calculateBoardLayout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CellFocusTest {

    private val board = DpSize(960.dp, 700.dp)
    private val density = Density(1f)
    private val boardPx = IntSize(board.width.value.toInt(), board.height.value.toInt())
    private val layout = calculateBoardLayout(board, isVertical = false)!!
    private val routes = listOf(layout.innerRoute, layout.outerRoute)

    private fun focusAt(point: Offset, held: Pair<Int, Int>?) = with(density) {
        routes.firstNotNullOfOrNull { heldCell(it, point, boardPx, held) }
            ?: routes.firstNotNullOfOrNull { cellUnder(it, point, boardPx) }
    }

    private fun centerOf(index: Int): Offset {
        val route = layout.outerRoute
        val place = route.places.first { it.index == index }.place
        return Offset(
            x = (board.width - route.size.width).value / 2 + place.offset.x.value + place.size.width.value / 2,
            y = (board.height - route.size.height).value / 2 + place.offset.y.value + place.size.height.value / 2,
        )
    }

    @Test
    fun pointerOverACellFocusesIt() {
        val target = layout.outerRoute.layer.level to 5
        assertEquals(target, focusAt(centerOf(5), held = null))
    }

    @Test
    fun holdingFocusNeverBouncesBetweenCells() {
        val step = 7f
        var x = 0f
        while (x < board.width.value) {
            var y = 0f
            while (y < board.height.value) {
                val point = Offset(x, y)
                val settled = focusAt(point, held = null)
                assertEquals(
                    settled,
                    focusAt(point, held = settled),
                    "фокус перекидається сам на себе в точці ($x, $y) — саме так виникає блимання",
                )
                y += step
            }
            x += step
        }
    }

    @Test
    fun focusSurvivesWhereTheTokenSteppedAside() {
        val route = layout.outerRoute
        val index = route.places.first { it.place.type.name == "Bankruptcy" }.index
        val place = route.places.first { it.index == index }.place
        val held = route.layer.level to index
        val float = tokenFloat(route, place, expandedCellBox(route))
        val parked = centerOf(index) + Offset(float.x.value, float.y.value)

        assertNotNull(focusAt(parked, held = held), "фішка відійшла туди, де фокус уже згас")
        assertEquals(held, focusAt(parked, held = held))
    }

    @Test
    fun pointerOnTheBareBackgroundClearsFocus() {
        val center = Offset(board.width.value / 2, board.height.value / 2)
        assertNull(focusAt(center, held = null))
    }

    @Test
    fun tapHoldsFocusUntilItsOwnRelease() {
        val focus = CellFocus()
        val cell = 1 to 7

        focus.tap(cell)
        assertEquals(cell, focus.key)

        focus.hover(null)
        assertEquals(cell, focus.key, "тап має триматись сам по собі, а не залежати від курсора")

        focus.releaseTap()
        assertNull(focus.key)
    }

    @Test
    fun hoverWinsOverAHeldTap() {
        val focus = CellFocus()
        focus.tap(1 to 7)
        focus.hover(1 to 9)
        assertEquals(1 to 9, focus.key)
    }
}
