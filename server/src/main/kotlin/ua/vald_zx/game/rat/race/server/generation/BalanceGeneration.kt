package ua.vald_zx.game.rat.race.server.generation

import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import ua.vald_zx.game.rat.race.card.shared.BoardLayer
import ua.vald_zx.game.rat.race.card.shared.GeneratedBalance
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.shared.dreamSlotIds

internal data class GeneratedSalaryScale(
    val salaries: List<Long>,
    val currency: String,
)

internal fun llmBalanceGenerator(
    onUsage: suspend (LlmTokenUsage) -> Unit = {},
    onQuota: suspend (LlmQuotaSnapshot) -> Unit = {},
    onRetry: suspend (LlmRetryWait) -> Unit = {},
    onFailed: suspend () -> Unit = {},
) = LlmBalanceGenerator(
    chat = LlmSettings.balanceChat(balanceResponseFormat, onUsage, onQuota, onRetry, onFailed),
    scaleChat = LlmSettings.balanceChat(salaryScaleResponseFormat, onUsage, onQuota, onRetry, onFailed),
)

internal class LlmBalanceGenerator(
    private val chat: ChatCompletion,
    private val scaleChat: ChatCompletion = chat,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun generate(world: BoardGeneration, deckSizes: Map<BoardCardType, Int>): GeneratedBalance =
        generateEconomy(world, deckSizes, generateSalaryScale(world))

    private suspend fun generateSalaryScale(world: BoardGeneration): GeneratedSalaryScale {
        var lastError: Throwable? = null
        repeat(MAX_SCALE_ATTEMPTS) { attempt ->
            val answer = scaleChat.complete(SCALE_SYSTEM_PROMPT, scaleUserPrompt(world, lastError?.message))
            if (answer == null) {
                lastError = IllegalStateException("LLM did not return a salary scale")
                balanceLogger.warn("Salary scale attempt ${attempt + 1} returned no response")
                return@repeat
            }
            val scale = runCatching {
                json.parseToJsonElement(answer.jsonObject()).jsonObject.toSalaryScale().also { it.validate() }
            }
                .onFailure {
                    lastError = it
                    balanceLogger.warn("Salary scale attempt ${attempt + 1} rejected: ${it.message}")
                }
                .getOrNull() ?: return@repeat
            return scale
        }
        throw generationFailure("salary scale", lastError)
    }

    private suspend fun generateEconomy(
        world: BoardGeneration,
        deckSizes: Map<BoardCardType, Int>,
        scale: GeneratedSalaryScale,
    ): GeneratedBalance {
        var lastError: Throwable? = null
        repeat(MAX_BALANCE_ATTEMPTS) { attempt ->
            val answer = chat.complete(SYSTEM_PROMPT, economyUserPrompt(world, deckSizes, scale, lastError?.message))
            if (answer == null) {
                lastError = IllegalStateException("LLM did not return a balance")
                balanceLogger.warn("Balance attempt ${attempt + 1} returned no response")
                return@repeat
            }
            val generated = runCatching {
                val response = json.parseToJsonElement(answer.jsonObject()).jsonObject
                val anchored = JsonObject(response + scale.asJsonFields())
                val missingFields = generatedBalanceFields - anchored.keys
                check(missingFields.isEmpty()) {
                    "Balance response misses ${missingFields.sorted().joinToString()}"
                }
                json.decodeFromJsonElement<GeneratedBalance>(anchored)
                    .withoutDuplicateOptions()
                    .withBoundedAssetPrices()
                    .withBusinessTiersOnSalaryScale()
                    .withCorruptLandAreasAboveRegular()
                    .withProfitableCorruptLandSale()
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
        throw generationFailure("balance", lastError)
    }

    private fun generationFailure(what: String, cause: Throwable?): IllegalStateException {
        val reason = cause?.message?.substringBefore('\n').orEmpty()
        return IllegalStateException(
            "LLM failed to generate a valid $what${reason.takeIf { it.isNotBlank() }?.let { ": $it" }.orEmpty()}",
            cause,
        )
    }

    private fun scaleUserPrompt(world: BoardGeneration, previousError: String?) = buildString {
        append(SCALE_PROMPT_PREFIX)
        append(balanceWorldPrompt(world))
        append(previousErrorPrompt(previousError))
        append("Return the salary scale of this world as the JSON object described above.\n")
    }

    private fun economyUserPrompt(
        world: BoardGeneration,
        deckSizes: Map<BoardCardType, Int>,
        scale: GeneratedSalaryScale,
        previousError: String?,
    ) = buildString {
        append(ECONOMY_PROMPT_PREFIX)
        append(balanceWorldPrompt(world))
        append(salaryScalePrompt(scale))
        append(deckSizesPrompt(deckSizes))
        append(previousErrorPrompt(previousError))
        append("Return the economy of this world as the JSON object described above, with every field present.\n")
    }

    private fun previousErrorPrompt(previousError: String?) =
        previousError?.takeIf { it.isNotBlank() }?.let { error ->
            "Previous response rejected: ${error.substringBefore('\n')}. Fix it in a new complete response.\n"
        }.orEmpty()

    private fun String.jsonObject(): String {
        val start = indexOf('{')
        val end = lastIndexOf('}')
        check(start in 0..<end) { "LLM balance is not a JSON object" }
        return substring(start, end + 1)
    }
}

private fun JsonObject.toSalaryScale(): GeneratedSalaryScale {
    val salaries = checkNotNull(this["salaries"]) { "Salary scale response misses salaries" }
        .jsonArray.map { it.jsonPrimitive.long }
    val currency = checkNotNull(this["currency"]?.jsonPrimitive?.contentOrNull) {
        "Salary scale response misses currency"
    }
    return GeneratedSalaryScale(salaries.distinct().sorted(), currency)
}

private fun GeneratedSalaryScale.validate() {
    salaries.requireAmounts("salaries", MIN_SALARY_COUNT)
    currency.requireCurrency()
}

private fun GeneratedSalaryScale.asJsonFields() = mapOf(
    "salaries" to JsonArray(salaries.map(::JsonPrimitive)),
    "currency" to JsonPrimitive(currency),
)

private val SCALE_PROMPT_PREFIX: String by lazy {
    buildString {
        append(SCALE_REQUIREMENTS)
        append("Return exactly this structure:\n")
        appendLine(SCALE_SCHEMA)
    }
}

private val ECONOMY_PROMPT_PREFIX: String by lazy {
    buildString {
        append(GAME_MECHANICS)
        append(boardLayoutPrompt())
        append(tempoPrompt())
        append(BALANCE_REQUIREMENTS)
        append("Return every field in this structure:\n")
        appendLine(BALANCE_SCHEMA)
    }
}

private fun balanceWorldPrompt(world: BoardGeneration) = buildString {
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
}

private fun salaryScalePrompt(scale: GeneratedSalaryScale) = buildString {
    val unit = scale.salaries.median()
    append("The salary scale of this world is already fixed; price everything against it:\n")
    append("- Salaries: ${scale.salaries.sorted().joinToString()} ${scale.currency}.\n")
    append("- One unit is the median salary: $unit ${scale.currency}. Every price is a round multiple of it.\n")
    append("- smallBusinessPrices: at most ${unit * SMALL_BUSINESS_MAX_UNITS}, ")
    append("the cheapest at most ${unit * SMALL_BUSINESS_ENTRY_UNITS} so the first business is reachable early.\n")
    append("- mediumBusinessPrices: ${unit * MEDIUM_BUSINESS_MIN_UNITS}..${unit * MEDIUM_BUSINESS_MAX_UNITS}, ")
    append("the cheapest strictly above the dearest small business.\n")
    append("- bigBusinessPrices: ${unit * BIG_BUSINESS_MIN_UNITS}..${unit * BIG_BUSINESS_MAX_UNITS}, ")
    append("the cheapest strictly above the cheapest medium business.\n")
    append("- The lowest salary ${scale.salaries.min()} must still cover its own rent, food, cloth, ")
    append("transport, and phone percentages.\n")
}

private const val MAX_SCALE_ATTEMPTS = 3
private const val MAX_BALANCE_ATTEMPTS = 3
private const val AVERAGE_STEP = 3.5
private val balanceLogger = KtorSimpleLogger("BalanceLlm")
private val generatedBalanceFields = setOf(
    "shares",
    "forcedShareSalePrices",
    "forcedShareSalePercentage",
    "corruptBusinessSalePercentages",
    "corruptLandSalePercentages",
    "strayAnimalPercentage",
    "scamPrices",
    "scamPromisedReturnPercentages",
    "scamSuccessPercentage",
    "crashSectorDropPercentages",
    "crashMarketDropPercentages",
    "depositRate",
    "loanRate",
    "paydayRate",
    "babyRecurringCost",
    "carRecurringCost",
    "apartmentRecurringCost",
    "houseRecurringCost",
    "yachtRecurringCost",
    "planeRecurringCost",
    "animalRecurringCost",
    "marriageCost",
    "childBenefit",
    "deputyCardPrice",
    "taxInspectionBribePercentage",
    "mediumRiskMultiplier",
    "highRiskMultiplier",
    "salaryFundRates",
    "fundBaseRate",
    "fundStartRate",
    "restTurnCount",
    "divorceAssetRetentionPercentage",
    "carMovementBonus",
    "planeMovementBonus",
    "dreamMinPrice",
    "dreamMaxPrice",
    "loanLimit",
    "businessLimit",
    "transportMovementBonusEnabled",
    "outerCircleMinimumCashFlow",
    "outerCircleMinimumAccountBalance",
    "outerCircleApartmentRequired",
    "outerCircleCarRequired",
    "victoryMinimumAccountBalance",
    "victoryDreamRequired",
    "victoryPlaneRequired",
    "victoryEstateRequired",
)

private val SCALE_SYSTEM_PROMPT = buildString {
    append("You design economic board games. Choose the money scale of the described world. ")
    append("Return one JSON object only, without markdown. ")
    append("Salary is the player's recurring income and the unit every other price will be derived from.")
}

private val SYSTEM_PROMPT = buildString {
    append("You design economic board games. Build a coherent economy for the described world. ")
    append("Return one JSON object only, without markdown. All values must be positive, diverse integers suited to a long game. ")
    append("Small, medium, and big businesses must form three distinct capital tiers. ")
    append("The sum of the maximum household expense percentages must be below 95. ")
    append("Weights are positive relative card frequencies. Light contextual humor is welcome in company names. ")
    append("Generate unique world-specific companies with stable ASCII ids, short tickers, and natural names in every requested language.")
}

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
}

private fun deckSizesPrompt(deckSizes: Map<BoardCardType, Int>) = buildString {
    if (deckSizes.isEmpty()) return@buildString
    append("Deck sizes for this board; they determine how often effects occur:\n")
    deckSizes.entries.sortedBy { it.key.name }.forEach { (type, size) ->
        append("- ${type.name}: $size cards.\n")
    }
}

private val SCALE_REQUIREMENTS = """
Players earn a salary once per lap and start with no cash. Every other price in this economy will be derived from these salaries, so their scale must fit this world naturally.
Required constraints:
- salaries: at least $MIN_SALARY_COUNT unique positive amounts, from a modest profession to a wealthy one, all in the same currency.
- The lowest salary must comfortably cover its own rent, food, cloth, transport, and phone, which together take up to 90 percent of it, so keep it well above a handful of coins.
- currency: the short symbol or abbreviation money is written with in this world, 1..$MAX_CURRENCY_LENGTH characters, matching the era and the location. Prefer a real symbol where the setting has one (${'$'}, €, £, ₴, ¥); invent a fitting short one for a fictional or historical setting. No digits, no spaces, no explanatory words.
""".trimIndent() + "\n"

private val SCALE_SCHEMA = """
{"salaries":[...],"currency":"..."}
""".trimIndent()

private val GAME_MECHANICS = """
Mechanics the balance must support:
- On the inner circle, players receive salary; buy small/medium businesses, land, real estate, goods, and shares; and pay general or conditional expenses.
- A profession defines gender, salary, and recurring expenses. Gender, marriage, children, and owned assets affect events and conditional payers.
- Chance cards sell shares in bundles. A regular market event lets owners sell any quantity or decline.
- Some market events force every owner to sell all shares of one company at forcedShareSalePrices: a loss, but never zero.
- Corrupt deals from Chance require deputies and are substantially better than regular assets. A corrupt business yields either recurring profit or one immediate payout, never both. Market corruption events let owners sell previously acquired recurring corrupt businesses or corrupt land at a profit; they never sell a new asset to the player.
- Business extension raises one small business's recurring profit. Reelection changes the deputies.
- Every company belongs to one sector. A market crash event marks down every holding at once: companies in the struck sector lose crashSectorDropPercentages, all other companies lose the smaller crashMarketDropPercentages. Nobody may opt out, so a concentrated portfolio is punished and a spread one survives.
- A Chance scam promises an implausible immediate return for an upfront payment, but pays out only scamSuccessPercentage of the time. It must be a losing bet on average; players are meant to learn to refuse it.
- Deposits yield depositRate percent income; the credit line adds loanRate percent expense. Anything borrowed past loanLimit becomes a payday loan at paydayRate, which must be clearly punitive: at least twice loanRate.
- Children, cars, apartments, houses, yachts, and planes add their recurring costs.
- Marriage marries the player; a man pays marriageCost. Child adds a child to a woman or married man, pays childBenefit, and raises recurring expenses.
- Divorce ends marriage; a man keeps divorceAssetRetentionPercentage of his cash and deposit but loses his children. Bankruptcy removes a random business, resignation removes the job, and rest skips restTurnCount turns.
- Passing Salary pays current cash flow. Salary spaces offer the descending salaryFundRates. Start capitalizes funds at their own rates into fundBaseRate; landing exactly uses fundStartRate.
- Desire offers an unowned dream; purchased dreams are board-wide and may be required to win. TaxInspection charges exactly taxInspectionBribePercentage of total wealth when the player owns at least one corrupt business or corrupt land; otherwise it has no financial effect.
- Risk investments pay mediumRiskMultiplier and highRiskMultiplier. Buying a deputy costs deputyCardPrice. Account for them in overall difficulty.
- Cars and planes add carMovementBonus and planeMovementBonus steps when transport bonuses are enabled.
- Generated financial and ownership conditions control outer-circle entry and victory. Make the path challenging but achievable.
""".trimIndent() + "\n"

private val BALANCE_REQUIREMENTS = """
Required constraints:
- shares: at least 6 unique companies; every id, ticker, uk name, and en name is unique.
- Every share carries a sector from exactly this list: core.energy, core.industry, core.consumer, core.realty, core.agro, core.tech. Use at least 3 different sectors, and never put all but one company in the same sector.
- scamPromisedReturnPercentages: 150..900, the promised payout as a percentage of the amount staked. scamSuccessPercentage: 1..20.
- The best case max(scamPromisedReturnPercentages) × scamSuccessPercentage must stay below 10000, so fraud loses money on average.
- min(scamPrices) must be below max(sharePrices), so the offer looks affordable next to honest instruments.
- crashSectorDropPercentages: 30..80. crashMarketDropPercentages: 1..30 and strictly below every crashSectorDropPercentages value.
- Each share id matches [a-z][a-z0-9_-]{1,31}: lowercase ASCII letters, digits, _ or - only.
- Each ticker has 2..8 non-whitespace characters.
- Every other numeric array has at least 3 unique values.
- shoppingPrices and shopWeights contain all six keys: ANIMAL, AUTO, APARTMENT, HOUSE, YACHT, FLY.
- shoppingPrices medians strictly increase in this order: ANIMAL < AUTO < APARTMENT < HOUSE < YACHT < FLY.
- An animal is a meaningful but cheapest shop purchase and adds animalRecurringCost each turn.
- landPricePerUnit is the price of ONE area unit. Total land price = area × unit price, so keep it low relative to businesses.
- estatePrices is one Chance-card property's purchase price. corruptLandPricePerUnit works like landPricePerUnit.
- eventLandPricePercentages and estateSalePercentages: 30..180, the market sale price as a percentage of the purchase price. Below 100 is a loss; above 100 is profitable.
- Keep both price bands narrow: within landPricePerUnit and within estatePrices, the dearest is at most three times the cheapest.
- randomJobProfits is immediate income, not an asset price. Keep it well below estatePrices so random jobs do not trivialize real estate.
- Every corruptLandAreas value is larger than every landAreas value.
- corruptBusinessSalePercentages: 200..1000. A market event pays this percentage of the selected corrupt business's purchase price.
- corruptLandSalePercentages: 150..1000, and the lowest must pay at least twice the highest corruptLandPricePerUnit.
- strayAnimalPercentage: 1..30, the share of expense cards where the player pays for and adopts a stray or rescued animal.
- dreamMinPrice and dreamMaxPrice bound ${dreamSlotIds.size} dreams; the server distributes the others evenly between them.
- dreamMinPrice <= victoryMinimumAccountBalance, otherwise a required dream may be unaffordable.
- forcedShareSalePrices: at least 3 unique positive prices, each below min sharePrices.
- rentPercentages: 10..30; foodPercentages: 5..20; clothPercentages: 2..10; transportPercentages: 2..15; phonePercentages: 1..5.
- smallBusinessReturnPercentages: 1..200; mediumBusinessReturnPercentages: 1..100; bigBusinessReturnPercentages: 1..60.
- corruptBusinessReturnPercentages: 1..500, and its minimum must be at least twice the maximum bigBusinessReturnPercentages. corruptOneTimeReturnPercentages: 300..5000.
- corruptBusinessDeputies and corruptLandDeputies: 1..20.
- corruptDeputyPercentage and corruptOneTimePercentage: 1..99.
- forcedShareSalePercentage: 5..40; forced sales should matter without dominating the deck.
- depositRate: 1..20; loanRate: 1..50.
- babyRecurringCost, carRecurringCost, apartmentRecurringCost, houseRecurringCost, yachtRecurringCost, planeRecurringCost, animalRecurringCost, marriageCost, childBenefit, and deputyCardPrice are positive amounts consistent with salaries and asset prices.
- taxInspectionBribePercentage is exactly 20.
- mediumRiskMultiplier: 2..20; highRiskMultiplier: 2..20 and greater than mediumRiskMultiplier.
- salaryFundRates contains exactly four strictly descending rates in 1..100; fundBaseRate and fundStartRate: 1..100.
- restTurnCount: 1..10; divorceAssetRetentionPercentage: 10..90.
- carMovementBonus: 0..6; planeMovementBonus: carMovementBonus..12.
- All amounts and counts are positive and <= 1000000000.
- All card weights: 1..10000.
- loanLimit, businessLimit, outer-circle conditions, and victory conditions must fit this economy's scale.
- businessLimit: 1..1000.
- victoryMinimumAccountBalance > outerCircleMinimumAccountBalance.
- Choose ownership booleans and the movement bonus for a varied but achievable game.
""".trimIndent() + "\n"

private val BALANCE_SCHEMA = """
{
  "rentPercentages":[...], "foodPercentages":[...], "clothPercentages":[...],
  "transportPercentages":[...], "phonePercentages":[...],
  "smallBusinessPrices":[...], "smallBusinessReturnPercentages":[...],
  "mediumBusinessPrices":[...], "mediumBusinessReturnPercentages":[...],
  "bigBusinessPrices":[...], "bigBusinessReturnPercentages":[...],
  "shoppingPrices":{"ANIMAL":[...],"AUTO":[...],"APARTMENT":[...],"HOUSE":[...],"YACHT":[...],"FLY":[...]},
  "shopWeights":{"ANIMAL":10,"AUTO":30,"APARTMENT":25,"HOUSE":20,"YACHT":10,"FLY":5},
  "expensePrices":[...], "randomJobProfits":[...],
  "estatePrices":[...], "estateSalePercentages":[...],
  "shares":[{"id":"ascii_id","ticker":"CODE","names":{"uk":"...","en":"..."},"sector":"core.energy"}],
  "sharePrices":[...], "forcedShareSalePrices":[...], "shareCounts":[...],
  "businessExtensionProfits":[...], "landAreas":[...],
  "landPricePerUnit":[...], "eventLandPricePercentages":[...],
  "corruptBusinessPrices":[...], "corruptBusinessReturnPercentages":[...],
  "corruptOneTimeReturnPercentages":[...], "corruptBusinessSalePercentages":[...], "corruptBusinessDeputies":[...],
  "corruptLandPricePerUnit":[...], "corruptLandAreas":[...], "corruptLandSalePercentages":[...], "corruptLandDeputies":[...],
  "corruptDeputyPercentage":49, "corruptOneTimePercentage":30, "forcedShareSalePercentage":20,
  "strayAnimalPercentage":10,
  "scamPrices":[...], "scamPromisedReturnPercentages":[...], "scamSuccessPercentage":10,
  "crashSectorDropPercentages":[...], "crashMarketDropPercentages":[...],
  "depositRate":2, "loanRate":10, "paydayRate":30,
  "babyRecurringCost":300, "carRecurringCost":600, "apartmentRecurringCost":200,
  "houseRecurringCost":1000, "yachtRecurringCost":1500, "planeRecurringCost":5000,
  "animalRecurringCost":100, "marriageCost":5000, "childBenefit":1000,
  "deputyCardPrice":50000, "taxInspectionBribePercentage":20, "mediumRiskMultiplier":2, "highRiskMultiplier":6,
  "salaryFundRates":[20,15,10,5], "fundBaseRate":20, "fundStartRate":30,
  "restTurnCount":2, "divorceAssetRetentionPercentage":50,
  "carMovementBonus":1, "planeMovementBonus":2,
  "dreamMinPrice":1000000, "dreamMaxPrice":20000000,
  "chanceWeights":{"randomJob":30,"estate":25,"land":30,"shares":35,"corruptBusiness":13,"corruptLand":5,"scam":8},
  "eventWeights":{"land":26,"estate":15,"shares":45,"businessExtending":20,"reelection":2,"announcement":4,"corruptBusiness":13,"corruptLand":5,"marketCrash":6},
  "loanLimit":10000, "businessLimit":10, "transportMovementBonusEnabled":true,
  "outerCircleMinimumCashFlow":50000, "outerCircleMinimumAccountBalance":200000,
  "outerCircleApartmentRequired":true, "outerCircleCarRequired":true,
  "victoryMinimumAccountBalance":10000000, "victoryDreamRequired":true,
  "victoryPlaneRequired":true, "victoryEstateRequired":true
}
""".trimIndent()
