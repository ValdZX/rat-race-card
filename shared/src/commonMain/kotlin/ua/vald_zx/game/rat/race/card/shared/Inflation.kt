package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.Serializable

@Serializable
data class InflationSettings(
    val enabled: Boolean = false,
    val periodRatePercent: Long = 0,
    val salaryIndexationPercent: Long = 50,
) {
    fun validate(): ValidationResult {
        val errors = buildList {
            if (periodRatePercent < 0) add("Inflation rate must not be negative")
            if (periodRatePercent > MAX_PERIOD_RATE_PERCENT) {
                add("Inflation rate must not exceed $MAX_PERIOD_RATE_PERCENT% per period")
            }
            if (salaryIndexationPercent !in 0..MAX_SALARY_INDEXATION_PERCENT) {
                add("Salary indexation must be between 0 and $MAX_SALARY_INDEXATION_PERCENT%")
            }
        }
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }

    companion object {
        const val MAX_PERIOD_RATE_PERCENT = 50L
        const val MAX_SALARY_INDEXATION_PERCENT = 100L
    }
}

@Serializable
data class EconomyIndex(
    val period: Long = 0,
    val priceIndexPercent: Long = NEUTRAL_INDEX_PERCENT,
    val salaryIndexPercent: Long = NEUTRAL_INDEX_PERCENT,
) {
    val cumulativeInflationPercent: Long
        get() = priceIndexPercent - NEUTRAL_INDEX_PERCENT

    val salaryLagPercent: Long
        get() = priceIndexPercent - salaryIndexPercent
}

fun EconomyIndex.advanced(settings: InflationSettings): EconomyIndex {
    if (!settings.enabled || settings.periodRatePercent <= 0) return this
    val salaryRate = settings.periodRatePercent * settings.salaryIndexationPercent / 100
    return EconomyIndex(
        period = period + 1,
        priceIndexPercent = grow(priceIndexPercent, settings.periodRatePercent),
        salaryIndexPercent = grow(salaryIndexPercent, salaryRate),
    )
}

fun EconomyIndex.indexPrice(amount: Long): Long = applyIndex(amount, priceIndexPercent)

private fun grow(indexPercent: Long, ratePercent: Long): Long {
    if (ratePercent <= 0) return indexPercent
    val grown = ((indexPercent / 100.0) * (100 + ratePercent)).toLong()
    return grown.coerceAtLeast(indexPercent + 1)
}

fun Config.withEconomyIndex(index: EconomyIndex): Config = copy(
    priceIndexPercent = index.priceIndexPercent,
    salaryIndexPercent = index.salaryIndexPercent,
)

data class EconomyLap(val board: Board, val completedPeriod: Boolean)

fun Board.registerStartLap(playerId: String, activePlayerIds: Set<String>): EconomyLap {
    if (!inflation.enabled) return EconomyLap(this, completedPeriod = false)
    val passed = economyLapPlayerIds + playerId
    val everyoneLapped = activePlayerIds.isNotEmpty() && passed.containsAll(activePlayerIds)
    if (!everyoneLapped) return EconomyLap(copy(economyLapPlayerIds = passed), completedPeriod = false)
    val advanced = advanceEconomy().copy(economyLapPlayerIds = emptySet())
    return EconomyLap(advanced, completedPeriod = advanced.economy != economy)
}

fun Board.advanceEconomy(): Board {
    val advanced = economy.advanced(inflation)
    if (advanced == economy) return this
    val rate = inflation.periodRatePercent
    return copy(
        economy = advanced,
        outerCircleConditions = outerCircleConditions.copy(
            minimumCashFlow = raise(outerCircleConditions.minimumCashFlow, rate),
            minimumAccountBalance = raise(outerCircleConditions.minimumAccountBalance, rate),
        ),
        victoryConditions = victoryConditions.copy(
            minimumAccountBalance = raise(victoryConditions.minimumAccountBalance, rate),
        ),
    )
}

private fun raise(amount: Long, ratePercent: Long): Long {
    if (amount <= 0 || ratePercent <= 0) return amount
    return ((amount / 100.0) * (100 + ratePercent)).toLong()
}
