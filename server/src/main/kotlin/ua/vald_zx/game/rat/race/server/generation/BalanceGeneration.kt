package ua.vald_zx.game.rat.race.server.generation

import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.serialization.json.Json
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import ua.vald_zx.game.rat.race.card.shared.BoardLayer
import ua.vald_zx.game.rat.race.card.shared.GeneratedBalance
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.shared.dreamSlotIds

internal class LlmBalanceGenerator(
    private val chat: ChatCompletion = LlmSettings.balanceChat(),
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun generate(world: BoardGeneration, deckSizes: Map<BoardCardType, Int>): GeneratedBalance {
        var lastError: Throwable? = null
        repeat(MAX_BALANCE_ATTEMPTS) { attempt ->
            val answer = chat.complete(systemPrompt(), userPrompt(world, deckSizes, lastError?.message))
            if (answer == null) {
                lastError = IllegalStateException("LLM did not return a balance")
                balanceLogger.warn("Balance attempt ${attempt + 1} returned no response")
                return@repeat
            }
            val generated = runCatching {
                json.decodeFromString<GeneratedBalance>(answer.jsonObject())
                    .withoutDuplicateOptions()
                    .withBoundedAssetPrices()
            }
                .onFailure {
                    lastError = it
                    balanceLogger.warn("Balance attempt ${attempt + 1} has invalid JSON: ${it.message}")
                }
                .getOrNull() ?: return@repeat
            val valid = runCatching { generated.validate() }
                .onFailure {
                    lastError = it
                    balanceLogger.warn("Balance attempt ${attempt + 1} failed validation: ${it.message}")
                }
                .isSuccess
            if (valid) return generated
        }
        val reason = lastError?.message?.substringBefore('\n').orEmpty()
        throw IllegalStateException(
            "LLM failed to generate a valid balance${reason.takeIf { it.isNotBlank() }?.let { ": $it" }.orEmpty()}",
            lastError,
        )
    }

    private fun systemPrompt() = buildString {
        append("You design economic board games. Build a coherent economy for the described world. ")
        append("Return one JSON object only, without markdown. All values must be positive, diverse integers suited to a long game. ")
        append("Small, medium, and big businesses must form three distinct capital tiers. ")
        append("The sum of the maximum household expense percentages must be below 95. ")
        append("Weights are positive relative card frequencies. Light contextual humor is welcome in company names. ")
        append("Generate unique world-specific companies with stable ASCII ids, short tickers, and natural names in every requested language.")
    }

    private fun userPrompt(
        world: BoardGeneration,
        deckSizes: Map<BoardCardType, Int>,
        previousError: String?,
    ) = buildString {
        val described = listOfNotNull(
            world.theme.ifBlank { null }?.let { "Theme: $it." },
            world.locality.ifBlank { null }?.let { "Location: $it." },
            world.epoch.ifBlank { null }?.let { "Era: $it." },
        )
        if (described.isEmpty()) {
            appendLine("World: an ordinary modern city.")
        } else {
            described.forEach(::appendLine)
        }
        append("World seed: ${world.seed}.\n")
        append("Currency and the scale of salaries, prices, land, share bundles, and profits must fit this world naturally.\n")
        append(GAME_MECHANICS)
        append(boardLayoutPrompt())
        append(tempoPrompt())
        append(deckSizesPrompt(deckSizes))
        append(BALANCE_REQUIREMENTS)
        previousError?.takeIf { it.isNotBlank() }?.let { error ->
            append("Previous response rejected: ${error.substringBefore('\n')}. Fix it in a new complete response.\n")
        }
        append("Return every field in this structure:\n")
        append(BALANCE_SCHEMA)
    }

    private fun String.jsonObject(): String {
        val start = indexOf('{')
        val end = lastIndexOf('}')
        check(start >= 0 && end > start) { "LLM balance is not a JSON object" }
        return substring(start, end + 1)
    }
}

private const val MAX_BALANCE_ATTEMPTS = 3
private const val AVERAGE_STEP = 3.5
private val balanceLogger = KtorSimpleLogger("BalanceLlm")

private fun boardLayoutPrompt() = buildString {
    append("Fixed board layout; account for it in card frequencies and amounts:\n")
    BoardLayer.entries.forEach { layer ->
        val counts = layer.places.groupingBy { it.name }.eachCount().entries
            .sortedBy { it.key }
            .joinToString(", ") { (name, count) -> "$name=$count" }
        append("- ${layer.name}: ${layer.places.size} spaces; $counts.\n")
    }
}

private fun tempoPrompt() = buildString {
    append("Game pace, which determines whether prices can pay off:\n")
    append("- One 1..6 die moves a player $AVERAGE_STEP spaces per turn on average.\n")
    BoardLayer.entries.forEach { layer ->
        val salaryCount = layer.places.count { it == PlaceType.Salary }
        val movesPerLap = (layer.places.size / AVERAGE_STEP).toInt()
        val movesPerSalary = if (salaryCount > 0) (layer.places.size / salaryCount / AVERAGE_STEP).toInt() else 0
        append("- ${layer.name}: one lap takes ~$movesPerLap turns")
        if (movesPerSalary > 0) append(", salary roughly every $movesPerSalary turns")
        append(".\n")
    }
    append("- Players start with no cash; salary is their first income.\n")
    append("- Entry prices must be reachable within a few salaries so early turns are meaningful.\n")
}

private fun deckSizesPrompt(deckSizes: Map<BoardCardType, Int>) = buildString {
    if (deckSizes.isEmpty()) return@buildString
    append("Deck sizes for this board; they determine how often effects occur:\n")
    deckSizes.entries.sortedBy { it.key.name }.forEach { (type, size) ->
        append("- ${type.name}: $size cards.\n")
    }
}

private val GAME_MECHANICS = """
Mechanics the balance must support:
- On the inner circle, players receive salary; buy small/medium businesses, land, real estate, goods, and shares; and pay general or conditional expenses.
- A profession defines gender, salary, and recurring expenses. Gender, marriage, children, and owned assets affect events and conditional payers.
- Chance cards sell shares in bundles. A regular market event lets owners sell any quantity or decline.
- Some market events force every owner to sell all shares of one company. forcedShareSalePrices are always below every sharePrices purchase price: a loss, but never zero.
- Corrupt deals require deputies. A corrupt business yields either recurring profit or one immediate payout, never both.
- Business extension raises one small business's recurring profit. Reelection changes the deputies.
- Deposits yield depositRate percent income; loans add loanRate percent expense. Children, cars, apartments, houses, yachts, and planes add their recurring costs.
- Marriage marries the player; a man pays marriageCost. Child adds a child to a woman or married man, pays childBenefit, and raises recurring expenses.
- Divorce ends marriage; a man also loses half his cash and deposit plus his children. Bankruptcy removes a random business, resignation removes the job, and rest skips two turns.
- Passing Salary pays current cash flow. Salary spaces offer funds at 20%, 15%, 10%, or 5%. Start capitalizes funds; landing exactly gives 30%.
- Desire offers an unowned dream; purchased dreams are board-wide and may be required to win. TaxInspection currently has no direct financial effect.
- Risk investments have fixed payouts: medium risk x2, high risk x6. Account for them in overall difficulty.
- Generated financial and ownership conditions control outer-circle entry and victory. Make the path challenging but achievable.
""".trimIndent() + "\n"

private val BALANCE_REQUIREMENTS = """
Required constraints:
- salaries: at least 10 unique amounts.
- shares: at least 6 unique companies; every id, ticker, uk name, and en name is unique.
- Each share id matches [a-z][a-z0-9_-]{1,31}: lowercase ASCII letters, digits, _ or - only.
- Each ticker has 2..8 non-whitespace characters.
- Every other numeric array has at least 3 unique values.
- shoppingPrices and shopWeights contain all six keys: ANIMAL, AUTO, APARTMENT, HOUSE, YACHT, FLY.
- shoppingPrices medians strictly increase in this order: ANIMAL < AUTO < APARTMENT < HOUSE < YACHT < FLY.
- An animal is a meaningful but cheapest shop purchase and adds animalRecurringCost each turn.
- landPricePerUnit is the price of ONE area unit. Total land price = area × unit price, so keep it low relative to businesses.
- eventLandPricePercentages: 30..180, the market sale price as a percentage of landPricePerUnit. Below 100 is a loss; above 100 is profitable.
- Land sales are optional, so max landPricePerUnit × max eventLandPricePercentages / 100 <= min landPricePerUnit × 3. Keep unit prices narrow.
- estatePrices is one Chance-card property's purchase price. estateSalePercentages: 30..180, the market sale price as a percentage of the same base.
- Real estate follows the same cap: max estatePrices × max estateSalePercentages / 100 <= min estatePrices × 3. Example: if min price is 20000 and max sale percentage is 140, max price is 42857.
- randomJobProfits is immediate income, not an asset price. Keep it well below estatePrices so random jobs do not trivialize real estate.
- corruptLandPricePerUnit works the same way for corrupt land.
- strayAnimalPercentage: 1..30, the share of expense cards where the player pays for and adopts a stray or rescued animal.
- dreamMinPrice and dreamMaxPrice bound ${dreamSlotIds.size} dreams; the server distributes the others evenly between them.
- dreamMinPrice <= victoryMinimumAccountBalance, otherwise a required dream may be unaffordable.
- forcedShareSalePrices: at least 3 unique positive prices, each below min sharePrices.
- rentPercentages: 10..30; foodPercentages: 5..20; clothPercentages: 2..10; transportPercentages: 2..15; phonePercentages: 1..5.
- smallBusinessReturnPercentages: 1..200; mediumBusinessReturnPercentages: 1..100; bigBusinessReturnPercentages: 1..60.
- corruptBusinessReturnPercentages: 1..500; corruptOneTimeReturnPercentages: 100..5000.
- corruptBusinessDeputies and corruptLandDeputies: 1..20.
- corruptDeputyPercentage and corruptOneTimePercentage: 1..99.
- forcedShareSalePercentage: 5..40; forced sales should matter without dominating the deck.
- depositRate: 1..20; loanRate: 1..50.
- The lowest salary must cover rent+food+cloth+transport+phone, calculated as percentages and rounded to tens; tiny salaries may fail.
- babyRecurringCost, carRecurringCost, apartmentRecurringCost, houseRecurringCost, yachtRecurringCost, planeRecurringCost, animalRecurringCost, marriageCost, and childBenefit are positive amounts consistent with salaries and asset prices.
- All amounts and counts are positive and <= 1000000000.
- All card weights: 1..10000.
- loanLimit, businessLimit, outer-circle conditions, and victory conditions must fit this economy's scale.
- businessLimit: 1..1000.
- victoryMinimumAccountBalance > outerCircleMinimumAccountBalance.
- Choose ownership booleans and the movement bonus for a varied but achievable game.
""".trimIndent() + "\n"

private val BALANCE_SCHEMA = """
{
  "salaries":[...],
  "rentPercentages":[...], "foodPercentages":[...], "clothPercentages":[...],
  "transportPercentages":[...], "phonePercentages":[...],
  "smallBusinessPrices":[...], "smallBusinessReturnPercentages":[...],
  "mediumBusinessPrices":[...], "mediumBusinessReturnPercentages":[...],
  "bigBusinessPrices":[...], "bigBusinessReturnPercentages":[...],
  "shoppingPrices":{"ANIMAL":[...],"AUTO":[...],"APARTMENT":[...],"HOUSE":[...],"YACHT":[...],"FLY":[...]},
  "shopWeights":{"ANIMAL":10,"AUTO":30,"APARTMENT":25,"HOUSE":20,"YACHT":10,"FLY":5},
  "expensePrices":[...], "randomJobProfits":[...],
  "estatePrices":[...], "estateSalePercentages":[...],
  "shares":[{"id":"ascii_id","ticker":"CODE","names":{"uk":"...","en":"..."}}],
  "sharePrices":[...], "forcedShareSalePrices":[...], "shareCounts":[...],
  "businessExtensionProfits":[...], "landAreas":[...],
  "landPricePerUnit":[...], "eventLandPricePercentages":[...],
  "corruptBusinessPrices":[...], "corruptBusinessReturnPercentages":[...],
  "corruptOneTimeReturnPercentages":[...], "corruptBusinessDeputies":[...],
  "corruptLandPricePerUnit":[...], "corruptLandAreas":[...], "corruptLandDeputies":[...],
  "corruptDeputyPercentage":49, "corruptOneTimePercentage":30, "forcedShareSalePercentage":20,
  "strayAnimalPercentage":10,
  "depositRate":2, "loanRate":10,
  "babyRecurringCost":300, "carRecurringCost":600, "apartmentRecurringCost":200,
  "houseRecurringCost":1000, "yachtRecurringCost":1500, "planeRecurringCost":5000,
  "animalRecurringCost":100, "marriageCost":5000, "childBenefit":1000,
  "dreamMinPrice":1000000, "dreamMaxPrice":20000000,
  "chanceWeights":{"randomJob":30,"estate":25,"land":30,"shares":35,"corruptBusiness":13,"corruptLand":5},
  "eventWeights":{"land":26,"estate":15,"shares":45,"businessExtending":20,"reelection":2,"announcement":4},
  "loanLimit":10000, "businessLimit":10, "transportMovementBonusEnabled":true,
  "outerCircleMinimumCashFlow":50000, "outerCircleMinimumAccountBalance":200000,
  "outerCircleApartmentRequired":true, "outerCircleCarRequired":true,
  "victoryMinimumAccountBalance":10000000, "victoryDreamRequired":true,
  "victoryPlaneRequired":true, "victoryEstateRequired":true
}
""".trimIndent()
