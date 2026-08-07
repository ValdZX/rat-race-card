package ua.vald_zx.game.rat.race.card.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DebtTest {
    private val service = MoneyService()

    @Test
    fun anUnpaidRemainderOpensACreditLineAtTheBoardRate() {
        val account = account(cash = 100)

        val result = service.pay(account, 500, PaymentPolicy(loanLimit = 10_000, creditRatePercent = 10))

        val debt = result.account.debts.single()
        assertEquals(DebtKind.CREDIT_LINE, debt.kind)
        assertEquals(400, debt.principal)
        assertEquals(10, debt.ratePercent)
        assertEquals(400, result.account.loan)
    }

    @Test
    fun spendingPastTheLimitFallsToAPunitivePaydayLender() {
        val account = account(cash = 0)

        val result = service.pay(
            account,
            1_000,
            PaymentPolicy(loanLimit = 600, creditRatePercent = 10, paydayRatePercent = 40),
        )

        val credit = result.account.debts.first { it.kind == DebtKind.CREDIT_LINE }
        val payday = result.account.debts.first { it.kind == DebtKind.PAYDAY }
        assertEquals(600, credit.principal)
        assertEquals(400, payday.principal)
        assertEquals(40, payday.ratePercent)
        assertTrue(result.events.any { it is PaymentEvent.PaydayLoanTaken })
    }

    @Test
    fun theSameDebtSizeCostsMoreAtAWorseRate() {
        val cheap = listOf(Debt("a", DebtKind.CREDIT_LINE, 10_000, 10))
        val dear = listOf(Debt("b", DebtKind.PAYDAY, 10_000, 40))

        assertEquals(1_000, cheap.totalInterest())
        assertEquals(4_000, dear.totalInterest())
    }

    @Test
    fun avalancheAttacksTheHighestRateAndSnowballTheSmallestBalance() {
        val debts = listOf(
            Debt("big-cheap", DebtKind.MORTGAGE, 100_000, 5),
            Debt("small-dear", DebtKind.PAYDAY, 2_000, 40),
            Debt("mid", DebtKind.CREDIT_LINE, 20_000, 10),
        )

        assertEquals("small-dear", debts.avalancheOrder().first().id)
        assertEquals("small-dear", debts.snowballOrder().first().id)
        assertEquals("big-cheap", debts.avalancheOrder().last().id)
        assertEquals("big-cheap", debts.snowballOrder().last().id)
    }

    @Test
    fun avalancheBeatsSnowballWhenTheBigDebtIsTheExpensiveOne() {
        val debts = listOf(
            Debt("small-cheap", DebtKind.CONSUMER, 1_000, 5),
            Debt("big-dear", DebtKind.PAYDAY, 20_000, 40),
        )
        val budget = 1_000L

        val afterAvalanche = debts.repayInOrder(debts.avalancheOrder(), budget).totalInterest()
        val afterSnowball = debts.repayInOrder(debts.snowballOrder(), budget).totalInterest()

        assertTrue(
            afterAvalanche < afterSnowball,
            "лавина має лишити менші відсотки: $afterAvalanche проти $afterSnowball",
        )
    }

    @Test
    fun aFullyRepaidDebtDisappearsInsteadOfLingeringAtZero() {
        val debts = listOf(Debt("a", DebtKind.CREDIT_LINE, 500, 10))

        assertEquals(emptyList(), debts.repay("a", 500))
        assertEquals(emptyList(), debts.repay("a", 900))
    }

    @Test
    fun repaymentNeverExceedsWhatThePlayerHas() {
        val account = account(cash = 100, debts = listOf(Debt("a", DebtKind.CREDIT_LINE, 5_000, 10)))

        val repaid = service.repay(account, "a", 5_000)

        assertEquals(0, repaid.cash)
        assertEquals(4_900, repaid.loan)
    }

    @Test
    fun repayingAnUnknownDebtChangesNothing() {
        val account = account(cash = 1_000, debts = listOf(Debt("a", DebtKind.CREDIT_LINE, 500, 10)))

        assertEquals(account, service.repay(account, "missing", 100))
    }

    @Test
    fun legacySavesBecomeASingleCreditLineWithTheOldRate() {
        val player = Player(
            id = "p",
            boardId = "b",
            attrs = PlayerAttributes(color = 0),
            loan = 7_000,
            config = Config(loadRate = 10),
        )

        val debts = player.resolvedDebts()

        assertEquals(1, debts.size)
        assertEquals(DebtKind.CREDIT_LINE, debts.single().kind)
        assertEquals(7_000, debts.single().principal)
        assertEquals(700, player.creditExpenses(), "стара формула loan × rate має дати той самий результат")
    }

    @Test
    fun theLoanTotalStaysInSyncWithTheDebtList() {
        val player = Player(id = "p", boardId = "b", attrs = PlayerAttributes(color = 0))

        val borrowed = player.withFinancialAccount(
            service.pay(
                player.financialAccount(),
                900,
                PaymentPolicy(loanLimit = 500, creditRatePercent = 10, paydayRatePercent = 40),
            ).account,
        )

        assertEquals(borrowed.debts.totalPrincipal(), borrowed.loan)
        assertEquals(900, borrowed.loan)
    }

    private fun account(
        cash: Long = 0,
        deposit: Long = 0,
        debts: List<Debt> = emptyList(),
    ) = FinancialAccount(cash = cash, deposit = deposit, debts = debts, funds = emptyList())
}
