package ua.vald_zx.game.rat.race.card.shared

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class BoardCurrencyTest {

    private fun board(balance: GeneratedBalance? = null) = Board(
        id = "b",
        name = "b",
        loanLimit = 0,
        businessLimit = 0,
        createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
        cards = emptyMap(),
        generatedBalance = balance,
    )

    @Test
    fun aBoardWithoutAGeneratedEconomyUsesTheDollar() {
        assertEquals("$", board().currency)
    }

    @Test
    fun aGeneratedBoardUsesTheCurrencyOfItsWorld() {
        assertEquals("₴", board(balance(currency = "₴")).currency)
    }

    @Test
    fun aBlankGeneratedCurrencyFallsBackToTheDefault() {
        assertEquals("$", board(balance(currency = "   ")).currency)
    }

    private fun balance(currency: String) = GeneratedBalance(
        salaries = listOf(1),
        rentPercentages = listOf(1),
        foodPercentages = listOf(1),
        clothPercentages = listOf(1),
        transportPercentages = listOf(1),
        phonePercentages = listOf(1),
        smallBusinessPrices = listOf(1),
        smallBusinessReturnPercentages = listOf(1),
        mediumBusinessPrices = listOf(1),
        mediumBusinessReturnPercentages = listOf(1),
        bigBusinessPrices = listOf(1),
        bigBusinessReturnPercentages = listOf(1),
        shoppingPrices = emptyMap(),
        shopWeights = emptyMap(),
        expensePrices = listOf(1),
        randomJobProfits = listOf(1),
        estatePrices = listOf(1),
        estateSalePercentages = listOf(1),
        sharePrices = listOf(1),
        shareCounts = listOf(1),
        businessExtensionProfits = listOf(1),
        landAreas = listOf(1),
        landPricePerUnit = listOf(1),
        eventLandPricePercentages = listOf(1),
        corruptBusinessPrices = listOf(1),
        corruptBusinessReturnPercentages = listOf(1),
        corruptOneTimeReturnPercentages = listOf(1),
        corruptBusinessDeputies = listOf(1),
        corruptLandPricePerUnit = listOf(1),
        corruptLandAreas = listOf(1),
        corruptLandDeputies = listOf(1),
        corruptDeputyPercentage = 1,
        corruptOneTimePercentage = 1,
        chanceWeights = GeneratedChanceWeights(1, 1, 1, 1, 1, 1),
        eventWeights = GeneratedEventWeights(1, 1, 1, 1, 1, 1),
        currency = currency,
    )
}
