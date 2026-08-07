package ua.vald_zx.game.rat.race.card.shared

data class FinancialFund(
    val rate: Long,
    val amount: Long,
)

data class FinancialAccount(
    val cash: Long,
    val deposit: Long,
    val debts: List<Debt>,
    val funds: List<FinancialFund>,
) {
    constructor(cash: Long, deposit: Long, loan: Long, funds: List<FinancialFund>, ratePercent: Long) :
            this(cash, deposit, legacyCreditLine(loan, ratePercent), funds)

    val loan: Long
        get() = debts.totalPrincipal()

    fun canAffordVoluntaryPurchase(
        price: Long,
        loanLimit: Long,
        useFunds: Boolean = true,
    ): Boolean {
        if (price <= 0) return false
        val availableWithoutCredit = cash + deposit + if (useFunds) funds.sumOf { it.amount } else 0
        val unusedCredit = (loanLimit - loan).coerceAtLeast(0)
        return unusedCredit > price || availableWithoutCredit > price - unusedCredit
    }
}

const val NEUTRAL_INDEX_PERCENT = 100L

data class FinancialSnapshot(
    val account: FinancialAccount,
    val activeIncome: List<Long>,
    val assetValues: List<Long>,
    val baseExpenses: List<Long>,
    val recurringExpenses: List<Long>,
    val depositRate: Long,
    val loanRate: Long,
    val salaryIncome: List<Long> = emptyList(),
    val priceIndexPercent: Long = NEUTRAL_INDEX_PERCENT,
    val salaryIndexPercent: Long = NEUTRAL_INDEX_PERCENT,
) {
    fun balance(): Long = account.cash + account.deposit + account.funds.sumOf { it.amount }

    fun salaryProfit(): Long = applyIndex(salaryIncome.sum(), salaryIndexPercent)

    fun businessProfit(): Long = applyIndex(activeIncome.sum(), priceIndexPercent)

    fun activeProfit(): Long = salaryProfit() + businessProfit()

    fun passiveProfit(): Long = ((account.deposit / 100.0) * depositRate).toLong()

    fun totalProfit(): Long = activeProfit() + passiveProfit()

    fun creditExpenses(): Long = account.debts.totalInterest()

    fun livingExpenses(): Long = applyIndex(baseExpenses.sum() + recurringExpenses.sum(), priceIndexPercent)

    fun totalExpenses(): Long = livingExpenses() + creditExpenses()

    fun cashFlow(): Long = totalProfit() - totalExpenses()

    fun total(): Long = balance() + assetValues.sum() - account.loan
}

internal fun applyIndex(amount: Long, indexPercent: Long): Long {
    if (indexPercent == NEUTRAL_INDEX_PERCENT) return amount
    return ((amount / 100.0) * indexPercent).toLong()
}

data class PaymentPolicy(
    val useFunds: Boolean = true,
    val loanLimit: Long? = null,
    val creditRatePercent: Long = 0,
    val paydayRatePercent: Long = creditRatePercent,
)

sealed interface PaymentEvent {
    data class DepositWithdrawn(val amount: Long) : PaymentEvent
    data class FundsWithdrawn(val amount: Long) : PaymentEvent
    data class LoanAdded(val amount: Long) : PaymentEvent
    data class PaydayLoanTaken(val amount: Long) : PaymentEvent
    data object LoanLimitExceeded : PaymentEvent
}

data class PaymentResult(
    val account: FinancialAccount,
    val events: List<PaymentEvent>,
)

data class CapitalizationResult(
    val funds: List<FinancialFund>,
    val profit: Long,
)

class MoneyService {
    fun addCash(account: FinancialAccount, amount: Long): FinancialAccount {
        return account.copy(cash = account.cash + amount)
    }

    fun pay(account: FinancialAccount, amount: Long, policy: PaymentPolicy): PaymentResult {
        require(amount >= 0) { "Payment must not be negative" }
        if (amount == 0L) return PaymentResult(account, emptyList())

        var remaining = amount
        val cashUsed = minOf(account.cash, remaining)
        remaining -= cashUsed
        val depositUsed = minOf(account.deposit, remaining)
        remaining -= depositUsed

        var funds = account.funds
        val fundAmountBefore = funds.sumOf { it.amount }
        if (policy.useFunds && remaining > 0) {
            val withdrawal = withdrawFunds(funds, remaining)
            funds = withdrawal.funds
            remaining = withdrawal.remaining
        }

        val borrowed = borrow(account.debts, remaining, policy)
        val updatedLoan = borrowed.debts.totalPrincipal()
        val fundsUsed = fundAmountBefore - funds.sumOf { it.amount }
        val events = buildList {
            if (depositUsed > 0) add(PaymentEvent.DepositWithdrawn(depositUsed))
            if (fundsUsed > 0) add(PaymentEvent.FundsWithdrawn(fundsUsed))
            if (remaining > 0) add(PaymentEvent.LoanAdded(remaining))
            if (borrowed.paydayTaken > 0) add(PaymentEvent.PaydayLoanTaken(borrowed.paydayTaken))
            if (remaining > 0 && policy.loanLimit != null && updatedLoan > policy.loanLimit) {
                add(PaymentEvent.LoanLimitExceeded)
            }
        }
        return PaymentResult(
            account = account.copy(
                cash = account.cash - cashUsed,
                deposit = account.deposit - depositUsed,
                funds = funds,
                debts = borrowed.debts,
            ),
            events = events,
        )
    }

    fun repay(account: FinancialAccount, debtId: String, amount: Long): FinancialAccount {
        require(amount >= 0) { "Repayment must not be negative" }
        val debt = account.debts.firstOrNull { it.id == debtId } ?: return account
        val paid = minOf(amount, debt.principal, account.cash + account.deposit)
        if (paid <= 0) return account
        val fromCash = minOf(account.cash, paid)
        return account.copy(
            cash = account.cash - fromCash,
            deposit = account.deposit - (paid - fromCash),
            debts = account.debts.repay(debtId, paid),
        )
    }

    private fun borrow(debts: List<Debt>, amount: Long, policy: PaymentPolicy): Borrowing {
        if (amount <= 0) return Borrowing(debts, 0)
        val limit = policy.loanLimit
        val creditRoom = if (limit == null) amount else (limit - debts.totalPrincipal()).coerceIn(0, amount)
        val onCredit = if (limit == null) amount else creditRoom
        val onPayday = amount - onCredit
        val withCredit = debts.borrow(
            id = CREDIT_LINE_DEBT_ID,
            kind = DebtKind.CREDIT_LINE,
            amount = onCredit,
            ratePercent = policy.creditRatePercent,
        )
        return Borrowing(
            debts = withCredit.borrow(
                id = PAYDAY_DEBT_ID,
                kind = DebtKind.PAYDAY,
                amount = onPayday,
                ratePercent = policy.paydayRatePercent,
            ),
            paydayTaken = onPayday,
        )
    }

    private data class Borrowing(val debts: List<Debt>, val paydayTaken: Long)

    fun capitalize(
        funds: List<FinancialFund>,
        rateOverride: Long?,
        baseRate: Long,
    ): CapitalizationResult {
        if (funds.isEmpty()) return CapitalizationResult(emptyList(), 0)
        val profit = funds.sumOf { fund ->
            ((rateOverride ?: fund.rate) / 100.0 * fund.amount).toLong()
        }
        val total = funds.sumOf { it.amount } + profit
        return CapitalizationResult(listOf(FinancialFund(baseRate, total)), profit)
    }

    private fun withdrawFunds(funds: List<FinancialFund>, amount: Long): FundWithdrawal {
        var remaining = amount
        var updated = funds
        for (fund in funds.sortedBy { it.rate }) {
            if (remaining == 0L) break
            if (fund.amount <= remaining) {
                remaining -= fund.amount
                updated = updated.remove(fund)
            } else {
                updated = updated.replace(fund, fund.copy(amount = fund.amount - remaining))
                remaining = 0
            }
        }
        return FundWithdrawal(updated, remaining)
    }

    private data class FundWithdrawal(
        val funds: List<FinancialFund>,
        val remaining: Long,
    )
}

fun appendRecentChange(changes: List<Long>, previous: Long, current: Long): List<Long> {
    return if (current == previous) changes else (changes + (current - previous)).takeLast(3)
}

val sharedMoneyService = MoneyService()
