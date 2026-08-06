package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.Serializable

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

fun List<PlaceType>.fundRateAtSalary(position: Int, salaryFundRates: List<Long>): Long {
    require(salaryFundRates.isNotEmpty())
    val startIndex = indexOf(PlaceType.Start)
    if (startIndex < 0 || getOrNull(position) != PlaceType.Salary) {
        return salaryFundRates.last()
    }
    val rank = indices
        .filter { this[it] == PlaceType.Salary }
        .sortedByDescending { it.stepsTo(startIndex, size) }
        .indexOf(position)
    return salaryFundRates.getOrElse(rank) { salaryFundRates.last() }
}

fun BoardLayer.fundRateAtSalary(position: Int, salaryFundRates: List<Long>): Long =
    places.fundRateAtSalary(position, salaryFundRates)

private fun Int.stepsTo(target: Int, cellCount: Int): Int = ((target - this) + cellCount) % cellCount

fun List<Fund>.capitalize(rateOverride: Long?, baseRate: Long): Pair<List<Fund>, Long> {
    val result = sharedMoneyService.capitalize(
        funds = map { FinancialFund(it.rate, it.amount) },
        rateOverride = rateOverride,
        baseRate = baseRate,
    )
    return result.funds.map { Fund(it.rate, it.amount) } to result.profit
}
