package ua.vald_zx.game.rat.race.server

import ua.vald_zx.game.rat.race.server.generation.MEDIUM_BUSINESS_MIN_UNITS
import ua.vald_zx.game.rat.race.server.generation.median
import ua.vald_zx.game.rat.race.server.generation.redistributedInto
import ua.vald_zx.game.rat.race.server.generation.validate
import ua.vald_zx.game.rat.race.server.generation.withBusinessTiersOnSalaryScale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BusinessTierScaleTest {

    @Test
    fun medianOfAnEvenListAveragesTheTwoMiddleValues() {
        val salaries = listOf(2_000L, 2_500, 3_000, 3_500, 4_000, 5_000, 6_000, 8_000, 10_000, 15_000)

        assertEquals(4_500, salaries.median(), "медіана парного списку — середнє двох центральних")
    }

    @Test
    fun medianOfAnOddListIsTheMiddleValue() {
        assertEquals(700, testBalance().salaries.median())
    }

    @Test
    fun aMediumTierBelowTheSalaryScaleIsLiftedInsteadOfRejected() {
        val salaries = listOf(2_000L, 2_500, 3_000, 3_500, 4_000, 5_000, 6_000, 8_000, 10_000, 15_000)
        val unit = salaries.median()
        val broken = testBalance().copy(
            salaries = salaries,
            smallBusinessPrices = listOf(4_000, 9_000, 18_000, 30_000, 45_000, 60_000),
            mediumBusinessPrices = listOf(62_000, 120_000, 260_000, 500_000, 900_000, 1_400_000),
            bigBusinessPrices = listOf(2_400_000, 4_000_000, 8_000_000, 20_000_000, 40_000_000, 60_000_000),
        )
        assertTrue(
            broken.mediumBusinessPrices.min() < unit * MEDIUM_BUSINESS_MIN_UNITS,
            "фікстура має відтворювати саме цю відмову",
        )
        assertFailsWith<IllegalStateException> { broken.validate() }

        val repaired = broken.withBusinessTiersOnSalaryScale()

        repaired.validate()
        assertTrue(repaired.mediumBusinessPrices.min() >= unit * MEDIUM_BUSINESS_MIN_UNITS)
        assertTrue(repaired.smallBusinessPrices.max() < repaired.mediumBusinessPrices.min())
    }

    @Test
    fun aBalanceAlreadyOnScaleIsLeftUntouched() {
        val balance = testBalance()

        val repaired = balance.withBusinessTiersOnSalaryScale()

        assertEquals(balance.smallBusinessPrices, repaired.smallBusinessPrices)
        assertEquals(balance.mediumBusinessPrices, repaired.mediumBusinessPrices)
        assertEquals(balance.bigBusinessPrices, repaired.bigBusinessPrices)
    }

    @Test
    fun redistributionKeepsValuesDistinctAndAscending() {
        val redistributed = listOf(5L, 7, 9, 11, 13, 15).redistributedInto(100, 400)

        assertEquals(6, redistributed.size)
        assertEquals(redistributed.sorted(), redistributed)
        assertEquals(redistributed.distinct().size, redistributed.size)
        assertTrue(redistributed.first() >= 100 && redistributed.last() <= 400)
    }

    @Test
    fun theRejectionMessageCarriesBothTheActualAndTheRequiredPrice() {
        val salaries = listOf(2_000L, 2_500, 3_000, 3_500, 4_000, 5_000, 6_000, 8_000, 10_000, 15_000)
        val broken = testBalance().copy(
            salaries = salaries,
            smallBusinessPrices = listOf(4_000, 9_000, 18_000, 30_000, 45_000, 60_000),
            mediumBusinessPrices = listOf(62_000, 120_000, 260_000, 500_000, 900_000, 1_400_000),
            bigBusinessPrices = listOf(2_400_000, 4_000_000, 8_000_000, 20_000_000, 40_000_000, 60_000_000),
        )

        val message = assertFailsWith<IllegalStateException> { broken.validate() }.message.orEmpty()

        assertTrue(message.contains("62000"), "повідомлення має містити поточну ціну: $message")
        assertTrue(message.contains("67500"), "повідомлення має містити потрібний поріг: $message")
    }
}
