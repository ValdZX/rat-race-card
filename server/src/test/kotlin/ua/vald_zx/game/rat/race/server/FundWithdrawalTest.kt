package ua.vald_zx.game.rat.race.server

import ua.vald_zx.game.rat.race.card.shared.FinancialAccount
import ua.vald_zx.game.rat.race.card.shared.FinancialFund
import ua.vald_zx.game.rat.race.card.shared.PaymentPolicy
import ua.vald_zx.game.rat.race.card.shared.sharedMoneyService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FundWithdrawalTest {

    @Test
    fun partialWithdrawalKeepsTheRemainder() {
        val funds = listOf(FinancialFund(rate = 10, amount = 2_000))

        assertEquals(listOf(FinancialFund(rate = 10, amount = 1_000)), withdraw(funds, 1_000)?.funds)
    }

    @Test
    fun exactWithdrawalRemovesTheFund() {
        val funds = listOf(FinancialFund(rate = 10, amount = 1_000))

        assertEquals(emptyList(), withdraw(funds, 1_000)?.funds)
    }

    @Test
    fun withdrawalUsesTheLowestRateFirst() {
        val funds = listOf(FinancialFund(rate = 20, amount = 1_000), FinancialFund(rate = 5, amount = 1_000))

        assertEquals(listOf(FinancialFund(rate = 20, amount = 500)), withdraw(funds, 1_500)?.funds)
    }

    @Test
    fun insufficientFundsDoNotProduceAPartialResult() {
        val funds = listOf(FinancialFund(rate = 10, amount = 500))

        assertNull(withdraw(funds, 1_000))
    }

    private fun withdraw(funds: List<FinancialFund>, amount: Long): FinancialAccount? {
        val result = sharedMoneyService.pay(
            account = FinancialAccount(cash = 0, deposit = 0, loan = 0, funds = funds),
            amount = amount,
            policy = PaymentPolicy(),
        )
        return result.account.takeIf { it.loan == 0L }
    }
}
