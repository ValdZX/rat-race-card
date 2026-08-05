package ua.vald_zx.game.rat.race.server

import ua.vald_zx.game.rat.race.card.shared.*
import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerRecentChangesTest {
    private val previous = Player(
        id = "player",
        boardId = "board",
        attrs = PlayerAttributes(color = 0),
        card = PlayerCard(food = 500),
        cash = 10_000,
        loan = 2_000,
    )

    @Test
    fun cashFlowIncreaseIsStoredWithPositiveSign() {
        val changed = previous.copy(card = previous.card.copy(food = 300))

        assertEquals(listOf(200L), changed.withRecentChanges(previous).lastCashFlows)
    }

    @Test
    fun cashFlowDecreaseIsStoredWithNegativeSign() {
        val changed = previous.copy(card = previous.card.copy(food = 800))

        assertEquals(listOf(-300L), changed.withRecentChanges(previous).lastCashFlows)
    }

    @Test
    fun loanChangesAreStoredAndLimitedToThreeEntries() {
        val changed = previous.copy(
            loan = 3_500,
            lastLoans = listOf(500, -200, 1_000),
        )

        assertEquals(listOf(-200L, 1_000L, 1_500L), changed.withRecentChanges(previous).lastLoans)
    }
}
