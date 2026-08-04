package ua.vald_zx.game.rat.race.server

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import ua.vald_zx.game.rat.race.card.shared.OuterCircleConditions
import ua.vald_zx.game.rat.race.card.shared.VictoryConditions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BalanceLlmTest {
    private val world = BoardGeneration(
        enabled = true,
        theme = "підводна цивілізація",
        locality = "Маріанська западина",
        epoch = "2600 рік",
        seed = 41,
    )

    @Test
    fun llmCreatesTheWholeEconomicModelForTheWorld() = runTest {
        val prompts = mutableListOf<String>()
        val chat = ChatCompletion { _, user ->
            prompts += user
            Json.encodeToString(testBalance())
        }

        val generated = LlmBalanceGenerator(chat).generate(world)

        assertEquals(testBalance(), generated)
        assertTrue(prompts.single().contains("підводна цивілізація"))
        assertTrue(prompts.single().contains("Маріанська западина"))
        assertTrue(prompts.single().contains("2600 рік"))
        assertTrue(prompts.single().contains("shares"))
        assertTrue(prompts.single().contains("forcedShareSalePrices"))
        assertTrue(prompts.single().contains("примусово продає всі акції"))
        assertTrue(prompts.single().contains("INNER:"))
        assertTrue(prompts.single().contains("OUTER:"))
        assertTrue(prompts.single().contains("victoryMinimumAccountBalance"))
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
    }

    @Test
    fun invalidLlmAnswerIsRegenerated() = runTest {
        var attempts = 0
        val chat = ChatCompletion { _, _ ->
            attempts += 1
            if (attempts == 1) "{}" else Json.encodeToString(testBalance())
        }

        assertEquals(testBalance(), LlmBalanceGenerator(chat).generate(world))
        assertEquals(2, attempts)
    }

    @Test
    fun rejectedBalanceReasonReachesTheNextAttempt() = runTest {
        val prompts = mutableListOf<String>()
        val invalid = testBalance().copy(shares = testBalance().shares.take(5))
        val chat = ChatCompletion { _, user ->
            prompts += user
            Json.encodeToString(if (prompts.size == 1) invalid else testBalance())
        }

        assertEquals(testBalance(), LlmBalanceGenerator(chat).generate(world))
        assertTrue(prompts[1].contains("shares has too few instruments"))
    }

    @Test
    fun repeatedNumericOptionsAreNormalized() = runTest {
        val repeated = testBalance().copy(
            corruptOneTimeReturnPercentages = listOf(500, 750, 750, 800, 1_000),
        )
        val chat = ChatCompletion { _, _ -> Json.encodeToString(repeated) }

        val generated = LlmBalanceGenerator(chat).generate(world)

        assertEquals(listOf(500L, 750L, 800L, 1_000L), generated.corruptOneTimeReturnPercentages)
    }

    @Test
    fun repeatedShareIdsAreRejected() {
        val repeated = testBalance().shares.first()
        val invalid = testBalance().copy(shares = testBalance().shares + repeated)

        assertFailsWith<IllegalStateException> { invalid.validate() }
    }
}
