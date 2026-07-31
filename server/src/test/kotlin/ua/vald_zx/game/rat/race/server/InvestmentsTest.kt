package ua.vald_zx.game.rat.race.server

import ua.vald_zx.game.rat.race.card.shared.BoardLayer
import ua.vald_zx.game.rat.race.card.shared.Fund
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.shared.capitalize
import ua.vald_zx.game.rat.race.card.shared.fundRateAtSalary
import ua.vald_zx.game.rat.race.card.shared.nextPositionOf
import kotlin.test.Test
import kotlin.test.assertEquals

class InvestmentsTest {

    @Test
    fun fundRateDropsAsSalaryCellGetsCloserToStart() {
        assertEquals(20L, BoardLayer.INNER.fundRateAtSalary(24))
        assertEquals(15L, BoardLayer.INNER.fundRateAtSalary(39))
        assertEquals(10L, BoardLayer.INNER.fundRateAtSalary(63))
        assertEquals(5L, BoardLayer.INNER.fundRateAtSalary(0))
    }

    @Test
    fun outerCircleUsesTheSameRanking() {
        assertEquals(20L, BoardLayer.OUTER.fundRateAtSalary(22))
        assertEquals(15L, BoardLayer.OUTER.fundRateAtSalary(37))
        assertEquals(10L, BoardLayer.OUTER.fundRateAtSalary(59))
        assertEquals(5L, BoardLayer.OUTER.fundRateAtSalary(0))
    }

    @Test
    fun everySalaryCellGetsARate() {
        BoardLayer.entries.forEach { layer ->
            val salaryCells = layer.places.indices.filter { layer.places[it] == PlaceType.Salary }
            val rates = salaryCells.map { layer.fundRateAtSalary(it) }
            assertEquals(listOf(20L, 15L, 10L, 5L).sorted(), rates.sorted(), "layer $layer")
        }
    }

    @Test
    fun debugJumpFindsTheNextSalaryCellAhead() {
        assertEquals(24, BoardLayer.INNER.nextPositionOf(PlaceType.Salary, from = 0))
        assertEquals(39, BoardLayer.INNER.nextPositionOf(PlaceType.Salary, from = 24))
        assertEquals(22, BoardLayer.OUTER.nextPositionOf(PlaceType.Salary, from = 0))
    }

    @Test
    fun debugJumpWrapsAroundTheBoard() {
        assertEquals(0, BoardLayer.INNER.nextPositionOf(PlaceType.Salary, from = 63))
        assertEquals(0, BoardLayer.OUTER.nextPositionOf(PlaceType.Salary, from = 59))
    }

    @Test
    fun passingStartCapitalizesEachFundAtItsOwnRate() {
        val funds = listOf(Fund(rate = 20, amount = 1000), Fund(rate = 5, amount = 2000))
        val (newFunds, profit) = funds.capitalize(rateOverride = null)

        assertEquals(300L, profit)
        assertEquals(listOf(Fund(rate = 20, amount = 3300)), newFunds)
    }

    @Test
    fun landingOnStartCapitalizesEverythingAtThirtyPercent() {
        val funds = listOf(Fund(rate = 20, amount = 1000), Fund(rate = 5, amount = 2000))
        val (newFunds, profit) = funds.capitalize(rateOverride = 30)

        assertEquals(900L, profit)
        assertEquals(listOf(Fund(rate = 20, amount = 3900)), newFunds)
    }

    @Test
    fun emptyFundsCapitalizeToNothing() {
        val (newFunds, profit) = emptyList<Fund>().capitalize(rateOverride = 30)
        assertEquals(0L, profit)
        assertEquals(emptyList(), newFunds)
    }
}
