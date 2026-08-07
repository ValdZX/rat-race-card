package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.Serializable

@Serializable
enum class LedgerReason {
    OTHER,
    SALARY,
    BUSINESS_PURCHASE,
    ASSET_PURCHASE,
    CONSUMER_PURCHASE,
    EXPENSE,
    LOAN_REPAYMENT,
    DEPOSIT,
    FUND,
    RISK_INVESTMENT,
    SCAM,
    MARKET_SALE,
    MARKET_CRASH,
    TAX_INSPECTION,
    CORRUPTION,
    DREAM,
    FAMILY,
    TRANSFER,
    BANKRUPTCY,
}

@Serializable
data class LedgerEntry(
    val playerId: String,
    val sequence: Long,
    val atEpochMs: Long,
    val reason: LedgerReason,
    val economyPeriod: Long,
    val priceIndexPercent: Long,
    val cash: Long,
    val deposit: Long,
    val loan: Long,
    val funds: Long,
    val businessValue: Long,
    val shareValue: Long,
    val total: Long,
    val cashFlow: Long,
    val creditExpenses: Long,
    val livingExpenses: Long,
) {
    val realTotal: Long
        get() = if (priceIndexPercent <= 0) total else total * NEUTRAL_INDEX_PERCENT / priceIndexPercent
}

fun Player.ledgerEntry(
    sequence: Long,
    atEpochMs: Long,
    reason: LedgerReason,
    economy: EconomyIndex,
): LedgerEntry {
    val snapshot = financialSnapshot()
    return LedgerEntry(
        playerId = id,
        sequence = sequence,
        atEpochMs = atEpochMs,
        reason = reason,
        economyPeriod = economy.period,
        priceIndexPercent = economy.priceIndexPercent,
        cash = cash,
        deposit = deposit,
        loan = loan,
        funds = funds.sumOf { it.amount },
        businessValue = businesses.sumOf { it.price },
        shareValue = sharesList.sumOf { it.price },
        total = snapshot.total(),
        cashFlow = snapshot.cashFlow(),
        creditExpenses = snapshot.creditExpenses(),
        livingExpenses = snapshot.livingExpenses(),
    )
}

data class LedgerFlow(
    val reason: LedgerReason,
    val inflow: Long,
    val outflow: Long,
) {
    val net: Long
        get() = inflow - outflow
}

data class GameDebrief(
    val playerId: String,
    val entries: List<LedgerEntry>,
) {
    val start: LedgerEntry? = entries.firstOrNull()
    val finish: LedgerEntry? = entries.lastOrNull()

    val flows: List<LedgerFlow> = entries.zipWithNext()
        .groupBy { (_, current) -> current.reason }
        .map { (reason, steps) ->
            val deltas = steps.map { (previous, current) -> current.total - previous.total }
            LedgerFlow(
                reason = reason,
                inflow = deltas.filter { it > 0 }.sum(),
                outflow = -deltas.filter { it < 0 }.sum(),
            )
        }
        .sortedByDescending { it.outflow }

    val totalPaidInterest: Long = entries.filter { it.reason == LedgerReason.SALARY }
        .sumOf { it.creditExpenses }

    val firstNegativeCashFlow: LedgerEntry? = entries.firstOrNull { it.cashFlow < 0 }

    val peakLoan: Long = entries.maxOfOrNull { it.loan } ?: 0

    val inflationLostValue: Long
        get() {
            val last = finish ?: return 0
            return last.total - last.realTotal
        }

    val assetShareAtFinishPercent: Long
        get() {
            val last = finish ?: return 0
            val assets = last.businessValue + last.shareValue
            val worth = assets + last.cash + last.deposit + last.funds
            return if (worth <= 0) 0 else assets * 100 / worth
        }
}

fun LedgerEntry.sameFinancialStateAs(other: LedgerEntry): Boolean =
    reason == other.reason &&
            cash == other.cash &&
            deposit == other.deposit &&
            loan == other.loan &&
            funds == other.funds &&
            businessValue == other.businessValue &&
            shareValue == other.shareValue

const val MAX_LEDGER_ENTRIES = 4_000
