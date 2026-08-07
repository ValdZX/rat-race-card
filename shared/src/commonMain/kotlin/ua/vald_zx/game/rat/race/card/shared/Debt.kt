package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.Serializable

@Serializable
enum class DebtKind {
    CREDIT_LINE,
    PAYDAY,
    MORTGAGE,
    CONSUMER,
}

@Serializable
data class Debt(
    val id: String,
    val kind: DebtKind,
    val principal: Long,
    val ratePercent: Long,
) {
    val interestPerPeriod: Long
        get() = ((principal / 100.0) * ratePercent).toLong()
}

const val CREDIT_LINE_DEBT_ID = "credit-line"
const val PAYDAY_DEBT_ID = "payday"

fun List<Debt>.totalPrincipal(): Long = sumOf { it.principal }

fun List<Debt>.totalInterest(): Long = sumOf { it.interestPerPeriod }

fun List<Debt>.avalancheOrder(): List<Debt> = sortedWith(
    compareByDescending<Debt> { it.ratePercent }.thenBy { it.principal },
)

fun List<Debt>.snowballOrder(): List<Debt> = sortedWith(
    compareBy<Debt> { it.principal }.thenByDescending { it.ratePercent },
)

fun List<Debt>.borrow(id: String, kind: DebtKind, amount: Long, ratePercent: Long): List<Debt> {
    if (amount <= 0) return this
    val existing = firstOrNull { it.id == id }
    return if (existing == null) {
        this + Debt(id, kind, amount, ratePercent)
    } else {
        map { debt ->
            if (debt.id == id) debt.copy(principal = debt.principal + amount, ratePercent = ratePercent) else debt
        }
    }
}

fun List<Debt>.repay(id: String, amount: Long): List<Debt> {
    if (amount <= 0) return this
    return mapNotNull { debt ->
        when {
            debt.id != id -> debt
            debt.principal <= amount -> null
            else -> debt.copy(principal = debt.principal - amount)
        }
    }
}

fun List<Debt>.repayInOrder(order: List<Debt>, amount: Long): List<Debt> {
    var remaining = amount
    var current = this
    order.forEach { target ->
        if (remaining <= 0) return current
        val live = current.firstOrNull { it.id == target.id } ?: return@forEach
        val paid = minOf(live.principal, remaining)
        current = current.repay(live.id, paid)
        remaining -= paid
    }
    return current
}

fun legacyCreditLine(loan: Long, ratePercent: Long): List<Debt> =
    if (loan <= 0) emptyList() else listOf(Debt(CREDIT_LINE_DEBT_ID, DebtKind.CREDIT_LINE, loan, ratePercent))

fun List<Debt>.resolved(legacyLoan: Long, ratePercent: Long): List<Debt> =
    ifEmpty { legacyCreditLine(legacyLoan, ratePercent) }
