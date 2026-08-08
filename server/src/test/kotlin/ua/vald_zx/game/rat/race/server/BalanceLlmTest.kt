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
        assertEquals(2, prompts.size)
        val (scalePrompt, economyPrompt) = prompts
        assertTrue(scalePrompt.contains("підводна цивілізація"))
        assertTrue(scalePrompt.contains("Маріанська западина"))
        assertTrue(scalePrompt.contains("2600 рік"))
        assertTrue(scalePrompt.contains("salaries"))
        assertTrue(scalePrompt.contains("currency"))
        assertTrue(economyPrompt.contains("підводна цивілізація"))
        assertTrue(economyPrompt.contains("shares"))
        assertTrue(economyPrompt.contains("forcedShareSalePrices"))
        assertTrue(economyPrompt.contains("force every owner to sell all shares"))
        assertTrue(economyPrompt.contains("INNER:"))
        assertTrue(economyPrompt.contains("OUTER:"))
        assertTrue(economyPrompt.contains("victoryMinimumAccountBalance"))
        assertTrue(economyPrompt.contains("childBenefit"))
        assertTrue(economyPrompt.contains("deputyCardPrice"))
        assertTrue(economyPrompt.contains("taxInspectionBribePercentage"))
        assertTrue(economyPrompt.contains("Market corruption events let owners sell"))
        assertTrue(economyPrompt.contains("corruptBusinessSalePercentages"))
        assertTrue(economyPrompt.contains("corruptLandSalePercentages"))
        assertTrue(economyPrompt.contains("salaryFundRates"))
    }

    @Test
    fun theEconomyPromptCarriesTheSalaryScaleAsLiteralAmounts() = runTest {
        val prompts = mutableListOf<String>()
        val chat = ChatCompletion { _, user ->
            prompts += user
            balanceJson.encodeToString(testBalance())
        }

        LlmBalanceGenerator(chat).generate(world, deckSizes)

        val economyPrompt = prompts.last()
        assertTrue(economyPrompt.contains("One unit is the median salary: 700"), economyPrompt)
        assertTrue(economyPrompt.contains("smallBusinessPrices: at most 14000, the cheapest at most 1400"), economyPrompt)
        assertTrue(economyPrompt.contains("mediumBusinessPrices: 10500..1050000"), economyPrompt)
        assertTrue(economyPrompt.contains("bigBusinessPrices: 350000..10500000"), economyPrompt)
        assertTrue(!economyPrompt.contains("\"salaries\":[...]"), "the economy call no longer asks for salaries")
    }

    @Test
    fun everythingWorldSpecificComesAfterTheReusableStaticPrefix() = runTest {
        val prompts = mutableListOf<String>()
        val chat = ChatCompletion { _, user ->
            prompts += user
            balanceJson.encodeToString(testBalance())
        }
        val otherWorld = world.copy(theme = "місто на кільцях Сатурна", locality = "Титан", seed = 7)

        LlmBalanceGenerator(chat).generate(world, deckSizes)
        LlmBalanceGenerator(chat).generate(otherWorld, deckSizes)

        val (firstScale, firstEconomy, secondScale, secondEconomy) = prompts
        assertTrue(firstEconomy.startsWith("Mechanics the balance must support:"), firstEconomy.take(80))
        assertTrue(
            firstEconomy.indexOf("Theme:") > firstEconomy.indexOf("\"victoryEstateRequired\""),
            "the world description must follow the schema, not precede it",
        )
        val sharedEconomyPrefix = firstEconomy.commonPrefixWith(secondEconomy)
        val sharedScalePrefix = firstScale.commonPrefixWith(secondScale)
        assertTrue(sharedEconomyPrefix.length > 9_000, "shared economy prefix: ${sharedEconomyPrefix.length}")
        assertTrue(sharedScalePrefix.length > 800, "shared scale prefix: ${sharedScalePrefix.length}")
    }

    @Test
    fun aSalaryScaleWithTooFewOptionsIsRegenerated() = runTest {
        var attempts = 0
        val chat = ChatCompletion { _, _ ->
            attempts += 1
            if (attempts == 1) """{"salaries":[300,400,500],"currency":"₴"}"""
            else balanceJson.encodeToString(testBalance())
        }

        assertEquals(testBalance(), LlmBalanceGenerator(chat).generate(world, deckSizes))
        assertEquals(3, attempts)
    }

    @Test
    fun theEconomyAnswerCannotOverrideTheFixedSalaryScale() = runTest {
        val scaleSalaries = testBalance().salaries
        var attempts = 0
        val chat = ChatCompletion { _, _ ->
            attempts += 1
            if (attempts == 1) {
                """{"salaries":${scaleSalaries.joinToString(",", "[", "]")},"currency":"₴"}"""
            } else {
                balanceJson.encodeToString(
                    testBalance().copy(salaries = listOf(1, 2, 3), currency = "€")
                )
            }
        }

        val generated = LlmBalanceGenerator(chat).generate(world, deckSizes)

        assertEquals(scaleSalaries, generated.salaries)
        assertEquals("₴", generated.currency)
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
            if (attempts == 2) "{}" else balanceJson.encodeToString(testBalance())
        }

        assertEquals(testBalance(), LlmBalanceGenerator(chat).generate(world, deckSizes))
        assertEquals(3, attempts)
    }

    @Test
    fun rejectedBalanceReasonReachesTheNextAttempt() = runTest {
        val prompts = mutableListOf<String>()
        val invalid = testBalance().copy(shares = testBalance().shares.take(5))
        val chat = ChatCompletion { _, user ->
            prompts += user
            balanceJson.encodeToString(if (prompts.size == 2) invalid else testBalance())
        }

        assertEquals(testBalance(), LlmBalanceGenerator(chat).generate(world, deckSizes))
        assertTrue(prompts[2].contains("shares has too few instruments"))
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
    fun corruptLandAreasOverlappingRegularOnesAreLiftedInsteadOfRejected() = runTest {
        val overlapping = testBalance().copy(
            landAreas = listOf(6, 12, 20, 30, 50, 100),
            corruptLandAreas = listOf(40, 60, 100, 300, 500),
        )
        assertFailsWith<IllegalStateException> { overlapping.validate() }
        val chat = ChatCompletion { _, _ -> balanceJson.encodeToString(overlapping) }

        val generated = LlmBalanceGenerator(chat).generate(world, deckSizes)

        assertEquals(listOf(101L, 102L, 103L, 300L, 500L), generated.corruptLandAreas)
        generated.validate()
    }

    @Test
    fun corruptLandAreasAlreadyAboveRegularOnesAreLeftUntouched() {
        val balance = testBalance()

        assertEquals(balance.corruptLandAreas, balance.withCorruptLandAreasAboveRegular().corruptLandAreas)
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
    fun anUnprofitableCorruptLandSaleIsLiftedInsteadOfRejected() = runTest {
        val unprofitable = testBalance().copy(corruptLandSalePercentages = listOf(150, 160, 170))
        assertFailsWith<IllegalStateException> { unprofitable.validate() }
        val chat = ChatCompletion { _, _ -> balanceJson.encodeToString(unprofitable) }

        val generated = LlmBalanceGenerator(chat).generate(world, deckSizes)

        assertEquals(testBalance().corruptLandPricePerUnit, generated.corruptLandPricePerUnit)
        assertEquals(listOf(427L, 428L, 429L), generated.corruptLandSalePercentages)
        generated.validate()
    }

    @Test
    fun aCorruptLandPriceBandTooWideForAnyProfitableSaleIsNarrowed() = runTest {
        val wide = testBalance().copy(
            corruptLandPricePerUnit = listOf(1_000, 5_000, 9_000),
            corruptLandSalePercentages = listOf(200, 300, 400),
        )
        assertFailsWith<IllegalStateException> { wide.validate() }
        val chat = ChatCompletion { _, _ -> balanceJson.encodeToString(wide) }

        val generated = LlmBalanceGenerator(chat).generate(world, deckSizes)

        assertEquals(listOf(1_000L, 2_995L, 4_990L), generated.corruptLandPricePerUnit)
        assertEquals(listOf(998L, 999L, 1_000L), generated.corruptLandSalePercentages)
        generated.validate()
    }

    @Test
    fun aProfitableCorruptLandSaleIsLeftUntouched() {
        val balance = testBalance()

        assertEquals(balance, balance.withProfitableCorruptLandSale())
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
            if (attempts <= 2) withoutChildBenefit else complete.toString()
        }

        assertEquals(testBalance(), LlmBalanceGenerator(chat).generate(world, deckSizes))
        assertEquals(3, attempts)
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
