package ua.vald_zx.game.rat.race.server

import ua.vald_zx.game.rat.race.card.shared.Fund
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FundWithdrawalTest {

    @Test
    fun partialWithdrawalKeepsTheRemainder() {
        val funds = listOf(Fund(rate = 10, amount = 2_000))

        assertEquals(listOf(Fund(rate = 10, amount = 1_000)), funds.withdrawFunds(1_000))
    }

    @Test
    fun exactWithdrawalRemovesTheFund() {
        val funds = listOf(Fund(rate = 10, amount = 1_000))

        assertEquals(emptyList(), funds.withdrawFunds(1_000))
    }

    @Test
    fun withdrawalUsesTheLowestRateFirst() {
        val funds = listOf(Fund(rate = 20, amount = 1_000), Fund(rate = 5, amount = 1_000))

        assertEquals(listOf(Fund(rate = 20, amount = 500)), funds.withdrawFunds(1_500))
    }

    @Test
    fun insufficientFundsDoNotProduceAPartialResult() {
        val funds = listOf(Fund(rate = 10, amount = 500))

        assertNull(funds.withdrawFunds(1_000))
    }
}
