package ua.vald_zx.game.rat.race.card.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class FinanceTest {
    private val service = MoneyService()

    @Test
    fun mandatoryPaymentUsesCashDepositLowestRateFundsAndLoanInOrder() {
        val account = FinancialAccount(
            cash = 100,
            deposit = 200,
            loan = 50,
            funds = listOf(
                FinancialFund(rate = 20, amount = 400),
                FinancialFund(rate = 5, amount = 300),
            ),
        )

        val result = service.pay(account, 1_100, PaymentPolicy(loanLimit = 200))

        assertEquals(0, result.account.cash)
        assertEquals(0, result.account.deposit)
        assertTrue(result.account.funds.isEmpty())
        assertEquals(150, result.account.loan)
        assertEquals(
            listOf(
                PaymentEvent.DepositWithdrawn(200),
                PaymentEvent.FundsWithdrawn(700),
                PaymentEvent.LoanAdded(100),
            ),
            result.events,
        )
    }

    @Test
    fun partialFundWithdrawalStartsWithTheLowestRate() {
        val account = FinancialAccount(
            cash = 0,
            deposit = 0,
            loan = 0,
            funds = listOf(
                FinancialFund(rate = 20, amount = 1_000),
                FinancialFund(rate = 5, amount = 1_000),
            ),
        )

        val result = service.pay(account, 1_500, PaymentPolicy())

        assertEquals(listOf(FinancialFund(rate = 20, amount = 500)), result.account.funds)
        assertEquals(0, result.account.loan)
    }

    @Test
    fun fundPurchaseDoesNotLiquidateOtherFunds() {
        val account = FinancialAccount(
            cash = 100,
            deposit = 200,
            loan = 0,
            funds = listOf(FinancialFund(rate = 5, amount = 10_000)),
        )

        val result = service.pay(account, 500, PaymentPolicy(useFunds = false))

        assertEquals(account.funds, result.account.funds)
        assertEquals(200, result.account.loan)
        assertIs<PaymentEvent.LoanAdded>(result.events.last())
    }

    @Test
    fun exactPaymentDoesNotCreateZeroValueCredit() {
        val account = FinancialAccount(cash = 100, deposit = 200, loan = 0, funds = emptyList())

        val result = service.pay(account, 300, PaymentPolicy())

        assertEquals(FinancialAccount(0, 0, 0, emptyList()), result.account)
        assertEquals(listOf(PaymentEvent.DepositWithdrawn(200)), result.events)
    }

    @Test
    fun mandatoryPaymentReportsCreditLimitWithoutRejectingThePayment() {
        val account = FinancialAccount(cash = 0, deposit = 0, loan = 90, funds = emptyList())

        val result = service.pay(account, 20, PaymentPolicy(loanLimit = 100))

        assertEquals(110, result.account.loan)
        assertEquals(
            listOf(PaymentEvent.LoanAdded(20), PaymentEvent.LoanLimitExceeded),
            result.events,
        )
    }

    @Test
    fun snapshotCalculatesBothModesFromTheSameFormulas() {
        val snapshot = FinancialSnapshot(
            account = FinancialAccount(
                cash = 1_000,
                deposit = 10_000,
                loan = 2_000,
                funds = listOf(FinancialFund(10, 3_000)),
            ),
            activeIncome = listOf(4_000, 1_000),
            assetValues = listOf(5_000, 2_000),
            baseExpenses = listOf(500, 300),
            recurringExpenses = listOf(200, 100),
            depositRate = 2,
            loanRate = 10,
        )

        assertEquals(14_000, snapshot.balance())
        assertEquals(5_000, snapshot.activeProfit())
        assertEquals(200, snapshot.passiveProfit())
        assertEquals(200, snapshot.creditExpenses())
        assertEquals(1_300, snapshot.totalExpenses())
        assertEquals(3_900, snapshot.cashFlow())
        assertEquals(19_000, snapshot.total())
    }

    @Test
    fun capitalizationAndRecentChangesAreSharedDomainOperations() {
        val result = service.capitalize(
            funds = listOf(FinancialFund(20, 1_000), FinancialFund(5, 2_000)),
            rateOverride = null,
            baseRate = 20,
        )

        assertEquals(300, result.profit)
        assertEquals(listOf(FinancialFund(20, 3_300)), result.funds)
        assertEquals(listOf(2L, 3L, 4L), appendRecentChange(listOf(1, 2, 3), 10, 14))
        assertEquals(listOf(1L, 2L, 3L), appendRecentChange(listOf(1, 2, 3), 10, 10))
    }

    @Test
    fun clockCanBeControlledWithoutUsingTheSystemClock() {
        val clock = object : GameClock {
            override fun nowEpochMilliseconds(): Long = 1_234
        }

        assertEquals(1_234, clock.nowEpochMilliseconds())
    }
}
