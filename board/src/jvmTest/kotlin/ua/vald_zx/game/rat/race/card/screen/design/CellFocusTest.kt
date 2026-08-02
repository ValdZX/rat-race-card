package ua.vald_zx.game.rat.race.card.screen.design

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Клітинка й фішка, що стоїть на ній, — два різні носії наведення. Курсор
 * переходить з одного на другий через мить порожнечі, і саме вона давала
 * блимання: фокус гаснув, клітинка згорталась, фішка їхала назад під курсор.
 */
class CellFocusTest {

    private val cell = 1 to 7

    @Test
    fun hoverSurvivesTheGapBetweenCellAndToken() {
        val focus = CellFocus()

        focus.set(cell, focused = true, source = FocusSource.Cell)
        assertEquals(cell, focus.key)

        // Вказівник зійшов з клітинки — фокус ще тримається.
        focus.set(cell, focused = false, source = FocusSource.Cell)
        assertEquals(cell, focus.key, "фокус згас одразу, не давши фішці перехопити")

        // Його перехопила фішка тієї ж клітинки.
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

    /** Фішка, з якої почалось наведення, не має тікати з-під курсора. */
    @Test
    fun focusRemembersWhoStartedIt() {
        val focus = CellFocus()

        focus.set(cell, focused = true, source = FocusSource.Token)
        assertEquals(FocusSource.Token, focus.source)

        // Перехід на саму клітинку джерело міняє, ключ — ні.
        focus.set(cell, focused = true, source = FocusSource.Cell)
        assertEquals(cell, focus.key)
        assertEquals(FocusSource.Cell, focus.source)
    }

    @Test
    fun tapHoldsUntilItsOwnRelease() {
        val focus = CellFocus()
        focus.set(cell, focused = true, source = FocusSource.Tap)

        // Наведення поруч не гасить тап.
        focus.set(cell, focused = false, source = FocusSource.Cell)
        focus.clearHover()
        assertEquals(cell, focus.key)

        focus.releaseTap()
        assertNull(focus.key)
    }
}
