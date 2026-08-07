package ua.vald_zx.game.rat.race.server

import ua.vald_zx.game.rat.race.server.generation.*

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import ua.vald_zx.game.rat.race.card.shared.OuterCircleConditions
import ua.vald_zx.game.rat.race.card.shared.ShopType
import ua.vald_zx.game.rat.race.card.shared.VictoryConditions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BalanceLlmTest {
    private val balanceJson = Json { encodeDefaults = true }
    private val world = BoardGeneration(
        enabled = true,
        theme = "підводна цивілізація",
        locality = "Маріанська западина",
        epoch = "2600 рік",
        seed = 41,
    )
    private val deckSizes = BoardCardType.entries.associateWith { 24 }

    @Test
    fun llmCreatesTheWholeEconomicModelForTheWorld() = runTest {
        val prompts = mutableListOf<String>()
        val chat = ChatCompletion { _, user ->
            prompts += user
            balanceJson.encodeToString(testBalance())
        }

        val generated = LlmBalanceGenerator(chat).generate(world, deckSizes)

        assertEquals(testBalance(), generated)
        assertTrue(prompts.single().contains("підводна цивілізація"))
        assertTrue(prompts.single().contains("Маріанська западина"))
        assertTrue(prompts.single().contains("2600 рік"))
        assertTrue(prompts.single().contains("shares"))
        assertTrue(prompts.single().contains("forcedShareSalePrices"))
        assertTrue(prompts.single().contains("force every owner to sell all shares"))
        assertTrue(prompts.single().contains("INNER:"))
        assertTrue(prompts.single().contains("OUTER:"))
        assertTrue(prompts.single().contains("victoryMinimumAccountBalance"))
        assertTrue(prompts.single().contains("childBenefit"))
        assertTrue(prompts.single().contains("deputyCardPrice"))
        assertTrue(prompts.single().contains("taxInspectionBribePercentage"))
        assertTrue(prompts.single().contains("Market corruption events let owners sell"))
        assertTrue(prompts.single().contains("corruptBusinessSalePercentages"))
        assertTrue(prompts.single().contains("corruptLandSalePercentages"))
        assertTrue(prompts.single().contains("salaryFundRates"))
    }

    @Test
    fun invalidEconomyIsRejected() {
        val invalid = testBalance().copy(
            rentPercentages = listOf(50, 55, 60),
            foodPercentages = listOf(40, 45, 50),
        )

        assertFailsWith<IllegalStateException> { invalid.validate() }
    }

    @Test
    fun forcedSaleMustAlwaysBeBelowRegularSharePrices() {
        val invalid = testBalance().copy(forcedShareSalePrices = listOf(4, 5, 6))

        assertFailsWith<IllegalStateException> { invalid.validate() }
    }

    @Test
    fun generatedRulesBecomeBoardConditions() {
        val balance = testBalance()

        assertEquals(
            OuterCircleConditions(
                minimumCashFlow = 50_000,
                minimumAccountBalance = 200_000,
                apartmentRequired = true,
                carRequired = true,
            ),
            balance.outerCircleConditions(),
        )
        assertEquals(
            VictoryConditions(
                minimumAccountBalance = 10_000_000,
                dreamRequired = true,
                planeRequired = true,
                estateRequired = true,
            ),
            balance.victoryConditions(),
        )
        assertEquals(balance.depositRate, balance.playerConfig().depositRate)
        assertEquals(balance.loanRate, balance.playerConfig().loadRate)
        assertEquals(balance.babyRecurringCost, balance.playerConfig().babyCost)
        assertEquals(balance.marriageCost, balance.playerConfig().marriageCost)
        assertEquals(balance.childBenefit, balance.playerConfig().childBenefit)
        assertEquals(balance.deputyCardPrice, balance.playerConfig().deputyCardPrice)
        assertEquals(
            balance.taxInspectionBribePercentage,
            balance.playerConfig().taxInspectionBribePercentage,
        )
        assertEquals(balance.mediumRiskMultiplier, balance.playerConfig().mediumRiskMultiplier)
        assertEquals(balance.highRiskMultiplier, balance.playerConfig().highRiskMultiplier)
        assertEquals(balance.salaryFundRates, balance.playerConfig().salaryFundRates)
        assertEquals(balance.fundBaseRate, balance.playerConfig().fundBaseRate)
        assertEquals(balance.fundStartRate, balance.playerConfig().fundStartRate)
        assertEquals(balance.restTurnCount, balance.playerConfig().restTurnCount)
        assertEquals(
            balance.divorceAssetRetentionPercentage,
            balance.playerConfig().divorceAssetRetentionPercentage,
        )
        assertEquals(balance.carMovementBonus, balance.playerConfig().carMovementBonus)
        assertEquals(balance.planeMovementBonus, balance.playerConfig().planeMovementBonus)
    }

    @Test
    fun invalidLlmAnswerIsRegenerated() = runTest {
        var attempts = 0
        val chat = ChatCompletion { _, _ ->
            attempts += 1
            if (attempts == 1) "{}" else balanceJson.encodeToString(testBalance())
        }

        assertEquals(testBalance(), LlmBalanceGenerator(chat).generate(world, deckSizes))
        assertEquals(2, attempts)
    }

    @Test
    fun rejectedBalanceReasonReachesTheNextAttempt() = runTest {
        val prompts = mutableListOf<String>()
        val invalid = testBalance().copy(shares = testBalance().shares.take(5))
        val chat = ChatCompletion { _, user ->
            prompts += user
            balanceJson.encodeToString(if (prompts.size == 1) invalid else testBalance())
        }

        assertEquals(testBalance(), LlmBalanceGenerator(chat).generate(world, deckSizes))
        assertTrue(prompts[1].contains("shares has too few instruments"))
    }

    @Test
    fun repeatedNumericOptionsAreNormalized() = runTest {
        val repeated = testBalance().copy(
            corruptOneTimeReturnPercentages = listOf(500, 750, 750, 800, 1_000),
        )
        val chat = ChatCompletion { _, _ -> balanceJson.encodeToString(repeated) }

        val generated = LlmBalanceGenerator(chat).generate(world, deckSizes)

        assertEquals(listOf(500L, 750L, 800L, 1_000L), generated.corruptOneTimeReturnPercentages)
    }

    @Test
    fun overlyWideAssetPriceBandsAreNormalized() = runTest {
        val wide = testBalance().copy(
            estatePrices = listOf(20_000, 60_000, 120_000),
            estateSalePercentages = listOf(80, 110, 140),
            landPricePerUnit = listOf(1_000, 4_000, 8_000),
            eventLandPricePercentages = listOf(60, 100, 150),
        )
        val chat = ChatCompletion { _, _ -> balanceJson.encodeToString(wide) }

        val generated = LlmBalanceGenerator(chat).generate(world, deckSizes)

        assertEquals(listOf(20_000L, 31_428L, 42_857L), generated.estatePrices)
        assertEquals(listOf(1_000L, 1_500L, 2_000L), generated.landPricePerUnit)
        generated.validate()
    }

    @Test
    fun aSalaryThatCannotCoverItsOwnExpensesIsRejected() {
        val invalid = testBalance().copy(salaries = testBalance().salaries + 40)

        assertFailsWith<IllegalStateException> { invalid.validate() }
    }

    @Test
    fun shopTiersOutOfOrderAreRejected() {
        val prices = testBalance().shoppingPrices.toMutableMap()
        prices[ShopType.FLY] = listOf(100, 200, 300)
        val invalid = testBalance().copy(shoppingPrices = prices)

        assertFailsWith<IllegalStateException> { invalid.validate() }
    }

    @Test
    fun aMissingShopKindIsRejected() {
        val invalid = testBalance().copy(shoppingPrices = testBalance().shoppingPrices - ShopType.ANIMAL)

        assertFailsWith<IllegalStateException> { invalid.validate() }
    }

    @Test
    fun landThatResellsForTooMuchIsRejected() {
        val invalid = testBalance().copy(landPricePerUnit = listOf(100, 1_000, 5_000))

        assertFailsWith<IllegalStateException> { invalid.validate() }
    }

    @Test
    fun estateThatResellsForTooMuchIsRejected() {
        val invalid = testBalance().copy(estatePrices = listOf(1_000, 40_000, 90_000))

        assertFailsWith<IllegalStateException> { invalid.validate() }
    }

    @Test
    fun corruptLandMarketMustAlwaysBeProfitable() {
        val invalid = testBalance().copy(corruptLandSalePercentages = listOf(150, 160, 170))

        assertFailsWith<IllegalStateException> { invalid.validate() }
    }

    @Test
    fun corruptBusinessMustBeatRegularBigBusiness() {
        val invalid = testBalance().copy(corruptBusinessReturnPercentages = listOf(10, 15, 20))

        assertFailsWith<IllegalStateException> { invalid.validate() }
    }

    @Test
    fun taxInspectionBribeIsFixedAtTwentyPercent() {
        val invalid = testBalance().copy(taxInspectionBribePercentage = 25)

        assertFailsWith<IllegalStateException> { invalid.validate() }
    }

    @Test
    fun anUnaffordableCheapestDreamIsRejected() {
        val invalid = testBalance().copy(dreamMinPrice = testBalance().victoryMinimumAccountBalance + 1)

        assertFailsWith<IllegalStateException> { invalid.validate() }
    }

    @Test
    fun omittedChildBenefitIsRegeneratedInsteadOfUsingTheLegacyDefault() = runTest {
        var attempts = 0
        val complete = balanceJson.encodeToJsonElement(testBalance()).jsonObject
        val withoutChildBenefit = JsonObject(complete - "childBenefit").toString()
        val chat = ChatCompletion { _, _ ->
            attempts += 1
            if (attempts == 1) withoutChildBenefit else complete.toString()
        }

        assertEquals(testBalance(), LlmBalanceGenerator(chat).generate(world, deckSizes))
        assertEquals(2, attempts)
    }

    @Test
    fun repeatedShareIdsAreRejected() {
        val repeated = testBalance().shares.first()
        val invalid = testBalance().copy(shares = testBalance().shares + repeated)

        assertFailsWith<IllegalStateException> { invalid.validate() }
    }

    @Test
    fun smallBusinessesTooExpensiveForTheSalaryScaleAreRejected() {
        val invalid = testBalance().copy(smallBusinessPrices = listOf(10_000, 20_000, 40_000))

        assertFailsWith<IllegalStateException> { invalid.validate() }
    }

    @Test
    fun mediumBusinessesTooExpensiveForTheSalaryScaleAreRejected() {
        val invalid = testBalance().copy(mediumBusinessPrices = listOf(500_000, 800_000, 1_200_000))

        assertFailsWith<IllegalStateException> { invalid.validate() }
    }
}
