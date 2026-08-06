package ua.vald_zx.game.rat.race.server

import kotlinx.datetime.LocalDateTime
import ua.vald_zx.game.rat.race.card.shared.*
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ServerPurchaseGuardTest {
    @Test
    fun activePlayerCanUseCashDepositFundsAndUnusedCredit() {
        val player = player(
            cash = 1_000,
            deposit = 2_000,
            loan = 1_000,
            funds = listOf(Fund(rate = 10, amount = 3_000)),
        )

        assertTrue(board().canMakeVoluntaryPurchase(player, 9_999))
    }

    @Test
    fun purchaseCannotConsumeTheLastAvailableUnitOrExceedCredit() {
        val player = player(cash = 5_000, loan = 2_000)

        assertTrue(board().canMakeVoluntaryPurchase(player, 7_999))
        assertFalse(board().canMakeVoluntaryPurchase(player, 8_000))
        assertFalse(board().canMakeVoluntaryPurchase(player, 8_001))
    }

    @Test
    fun inactiveRemovedAndOutOfTurnPlayersCannotBuy() {
        val board = board()

        assertFalse(board.canMakeVoluntaryPurchase(player(isInactive = true), 1))
        assertFalse(board.canMakeVoluntaryPurchase(player().copy(id = "removed"), 1))
        assertFalse(board.copy(activePlayerId = "other").canMakeVoluntaryPurchase(player(), 1))
    }

    @Test
    fun nonPositivePricesAreNeverVoluntaryPurchases() {
        assertFalse(board().canMakeVoluntaryPurchase(player(), 0))
        assertFalse(board().canMakeVoluntaryPurchase(player(), -1))
    }

    @Test
    fun businessLimitMatchesTheExistingClientBoundary() {
        val businesses = (1..3).map { index -> business(index) }

        assertTrue(board(businessLimit = 3).canBuyBusiness(player(businesses = businesses), 1))
        assertFalse(board(businessLimit = 3).canBuyBusiness(player(businesses = businesses + business(4)), 1))
    }

    @Test
    fun buyingAFundCannotLiquidateAnExistingFund() {
        val player = player(cash = 1_000, funds = listOf(Fund(rate = 5, amount = 100_000)))

        assertTrue(board().canMakeVoluntaryPurchase(player, 50_000))
        assertFalse(board().canBuyWithCashAndDeposit(player, 50_000))
    }

    @Test
    fun purchaseMustBelongToTheCurrentlyOpenCardScenario() {
        assertFalse(board().copy(canTakeCard = listOf(BoardCardType.Shopping)).isResolvingCard(BoardCardType.Shopping))
        assertTrue(
            board().copy(takenCard = CardLink(BoardCardType.Chance, 7))
                .isResolvingCard(BoardCardType.Chance)
        )
        assertFalse(board().isResolvingCard(BoardCardType.Shopping))
    }

    private fun board(businessLimit: Long = 3) = Board(
        id = "board",
        name = "Board",
        loanLimit = 5_000,
        businessLimit = businessLimit,
        createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
        cards = emptyMap(),
        playerIds = setOf("player"),
        activePlayerId = "player",
    )

    private fun player(
        cash: Long = 10_000,
        deposit: Long = 0,
        loan: Long = 0,
        funds: List<Fund> = emptyList(),
        businesses: List<Business> = emptyList(),
        isInactive: Boolean = false,
    ) = Player(
        id = "player",
        boardId = "board",
        attrs = PlayerAttributes(color = 0),
        cash = cash,
        deposit = deposit,
        loan = loan,
        funds = funds,
        businesses = businesses,
        isInactive = isInactive,
    )

    private fun business(index: Int) = Business(
        type = BusinessType.SMALL,
        name = "Business $index",
        price = 1,
        profit = 1,
    )
}
