package ua.vald_zx.game.rat.race.card.screen.design

import ua.vald_zx.game.rat.race.card.shared.PlayerStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerStatusProgressTest {

    @Test
    fun ladderFillsFromOneDotAtTheBottomToFiveAtTheTop() {
        assertEquals(1, PlayerStatus.EMPLOYEE.progressFilled())
        assertEquals(1, PlayerStatus.BROKE.progressFilled())
        assertEquals(5, PlayerStatus.FINALIST.progressFilled())
        assertEquals(4, PlayerStatus.MULTIMILLIONAIRE.progressFilled())
    }

    @Test
    fun ladderIsMonotonicAndNeverEmpty() {
        val values = PlayerStatus.entries.map { it.progressFilled() }
        assertEquals(values.sortedDescending(), values, "прогрес має спадати від вершини до низу")
        assertEquals(1, values.min())
        assertEquals(5, values.max())
    }
}
