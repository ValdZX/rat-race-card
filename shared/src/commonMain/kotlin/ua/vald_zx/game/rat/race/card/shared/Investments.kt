package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.Serializable

const val HIGH_RISK_MULTIPLIER = 6L
const val MEDIUM_RISK_MULTIPLIER = 2L
const val START_LANDED_RATE = 30L
const val FUND_BASE_RATE = 20L

private val salaryFundRates = listOf(20L, 15L, 10L, 5L)

@Serializable
data class StartCapitalization(
    val position: Int,
    val landed: Boolean,
)

@Serializable
data class InvestmentOutcome(
    val dice: Int,
    val stake: Long,
    val payout: Long,
)

fun BoardLayer.fundRateAtSalary(position: Int): Long {
    val startIndex = places.indexOf(PlaceType.Start)
    if (startIndex < 0 || places.getOrNull(position) != PlaceType.Salary) {
        return salaryFundRates.last()
    }
    val rank = places.indices
        .filter { places[it] == PlaceType.Salary }
        .sortedByDescending { it.stepsTo(startIndex, places.size) }
        .indexOf(position)
    return salaryFundRates.getOrElse(rank) { salaryFundRates.last() }
}

private fun Int.stepsTo(target: Int, cellCount: Int): Int = ((target - this) + cellCount) % cellCount

fun List<Fund>.capitalize(rateOverride: Long?): Pair<List<Fund>, Long> {
    if (isEmpty()) return this to 0L
    val profit = sumOf { fund ->
        ((rateOverride ?: fund.rate) / 100.0 * fund.amount).toLong()
    }
    val total = sumOf { it.amount } + profit
    return listOf(Fund(rate = FUND_BASE_RATE, amount = total)) to profit
}
