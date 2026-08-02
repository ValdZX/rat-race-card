package ua.vald_zx.game.rat.race.card.screen.design

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CellFocusTest {

    private val cell = 1 to 7

    @Test
    fun hoverSurvivesTheGapBetweenCellAndToken() {
        val focus = CellFocus()

        focus.set(cell, focused = true, source = FocusSource.Cell)
        assertEquals(cell, focus.key)

        focus.set(cell, focused = false, source = FocusSource.Cell)
        assertEquals(cell, focus.key, "фокус згас одразу, не давши фішці перехопити")

        focus.set(cell, focused = true, source = FocusSource.Cell)
        focus.clearHover()
        assertEquals(cell, focus.key, "відкладене згасання спрацювало після перехоплення")
    }

    @Test
    fun hoverFadesWhenNobodyPicksItUp() {
        val focus = CellFocus()
        focus.set(cell, focused = true, source = FocusSource.Cell)
        focus.set(cell, focused = false, source = FocusSource.Cell)

        focus.clearHover()
        assertNull(focus.key, "нікуди не перейшли — фокус мав згаснути")
    }

    @Test
    fun focusRemembersWhoStartedIt() {
        val focus = CellFocus()

        focus.set(cell, focused = true, source = FocusSource.Token)
        assertEquals(FocusSource.Token, focus.source)

        focus.set(cell, focused = true, source = FocusSource.Cell)
        assertEquals(cell, focus.key)
        assertEquals(FocusSource.Cell, focus.source)
    }

    @Test
    fun tapHoldsUntilItsOwnRelease() {
        val focus = CellFocus()
        focus.set(cell, focused = true, source = FocusSource.Tap)

        focus.set(cell, focused = false, source = FocusSource.Cell)
        focus.clearHover()
        assertEquals(cell, focus.key)

        focus.releaseTap()
        assertNull(focus.key)
    }
}
