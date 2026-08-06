package ua.vald_zx.game.rat.race.card.shared

data class FinancialFund(
    val rate: Long,
    val amount: Long,
)

data class FinancialAccount(
    val cash: Long,
    val deposit: Long,
    val loan: Long,
    val funds: List<FinancialFund>,
) {
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

data class FinancialSnapshot(
    val account: FinancialAccount,
    val activeIncome: List<Long>,
    val assetValues: List<Long>,
    val baseExpenses: List<Long>,
    val recurringExpenses: List<Long>,
    val depositRate: Long,
    val loanRate: Long,
) {
    fun balance(): Long = account.cash + account.deposit + account.funds.sumOf { it.amount }

    fun activeProfit(): Long = activeIncome.sum()

    fun passiveProfit(): Long = ((account.deposit / 100.0) * depositRate).toLong()

    fun totalProfit(): Long = activeProfit() + passiveProfit()

    fun creditExpenses(): Long = ((account.loan / 100.0) * loanRate).toLong()

    fun totalExpenses(): Long = baseExpenses.sum() + recurringExpenses.sum() + creditExpenses()

    fun cashFlow(): Long = totalProfit() - totalExpenses()

    fun total(): Long = balance() + assetValues.sum() - account.loan
}

data class PaymentPolicy(
    val useFunds: Boolean = true,
    val loanLimit: Long? = null,
)

sealed interface PaymentEvent {
    data class DepositWithdrawn(val amount: Long) : PaymentEvent
    data class FundsWithdrawn(val amount: Long) : PaymentEvent
    data class LoanAdded(val amount: Long) : PaymentEvent
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

        val updatedLoan = account.loan + remaining
        val fundsUsed = fundAmountBefore - funds.sumOf { it.amount }
        val events = buildList {
            if (depositUsed > 0) add(PaymentEvent.DepositWithdrawn(depositUsed))
            if (fundsUsed > 0) add(PaymentEvent.FundsWithdrawn(fundsUsed))
            if (remaining > 0) add(PaymentEvent.LoanAdded(remaining))
            if (remaining > 0 && policy.loanLimit != null && updatedLoan > policy.loanLimit) {
                add(PaymentEvent.LoanLimitExceeded)
            }
        }
        return PaymentResult(
            account = account.copy(
                cash = account.cash - cashUsed,
                deposit = account.deposit - depositUsed,
                funds = funds,
                loan = updatedLoan,
            ),
            events = events,
        )
    }

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
