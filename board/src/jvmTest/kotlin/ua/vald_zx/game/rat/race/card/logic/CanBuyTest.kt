package ua.vald_zx.game.rat.race.card.logic

import kotlinx.datetime.LocalDateTime
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.Business
import ua.vald_zx.game.rat.race.card.shared.BusinessType
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CanBuyTest {

    private fun state(
        cash: Long = 10_000,
        loan: Long = 0,
        loanLimit: Long = 5_000,
        businessLimit: Long = 3,
        businesses: List<Business> = emptyList(),
        isProgress: Boolean = false,
    ): BoardState {
        val player = Player(
            id = "me",
            boardId = "b",
            attrs = PlayerAttributes(0, 0),
            cash = cash,
            loan = loan,
            businesses = businesses,
        )
        val board = Board(
            id = "b",
            name = "b",
            loanLimit = loanLimit,
            businessLimit = businessLimit,
            createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
            cards = emptyMap(),
            playerIds = setOf("me"),
            activePlayerId = "me",
        )
        return BoardState(isProgress, board, player)
    }

    @Test
    fun purchaseWithinCashAndLoanLimitIsAllowed() {
        assertTrue(state().canBuy(14_000))
    }

    @Test
    fun purchaseThatDrainsEverythingIsBlocked() {
        assertFalse(state().canBuy(15_000), "покупка з'їдає весь резерв — гравець одразу банкрут")
        assertFalse(state().canBuy(20_000))
    }

    @Test
    fun existingLoanShrinksWhatIsAffordable() {
        assertTrue(state(loan = 4_000).canBuy(10_000))
        assertFalse(state(loan = 4_000).canBuy(11_000))
    }

    @Test
    fun purchaseIsBlockedWhileARequestIsInFlight() {
        assertFalse(state(isProgress = true).canBuy(100))
    }

    @Test
    fun purchaseIsBlockedWhenItIsNotYourTurn() {
        val idle = state().let { it.copy(board = it.board.copy(activePlayerId = "someone-else")) }
        assertFalse(idle.canBuy(100))
    }

    @Test
    fun businessNeedsBothMoneyAndAFreeSlot() {
        val full = (1..4).map { Business(type = BusinessType.SMALL, name = "b$it", price = 1, profit = 1) }
        assertTrue(state().canBuyBusiness(1_000))
        assertFalse(state(businesses = full).canBuyBusiness(1_000), "ліміт бізнесів вичерпано")
        assertFalse(state().canBuyBusiness(20_000), "грошей не вистачає")
    }
}
