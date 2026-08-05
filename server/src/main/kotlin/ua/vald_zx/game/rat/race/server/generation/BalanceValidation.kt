package ua.vald_zx.game.rat.race.server.generation

import ua.vald_zx.game.rat.race.card.shared.Config
import ua.vald_zx.game.rat.race.card.shared.GeneratedBalance
import ua.vald_zx.game.rat.race.card.shared.OuterCircleConditions
import ua.vald_zx.game.rat.race.card.shared.ShopType
import ua.vald_zx.game.rat.race.card.shared.VictoryConditions
import ua.vald_zx.game.rat.race.card.shared.dreamSlotIds
import ua.vald_zx.game.rat.race.card.shared.generatedLocales
import ua.vald_zx.game.rat.race.card.shared.shopTiers

internal fun GeneratedBalance.validate() {
    salaries.requireAmounts("salaries", 10)
    rentPercentages.requirePercentages("rentPercentages")
    foodPercentages.requirePercentages("foodPercentages")
    clothPercentages.requirePercentages("clothPercentages")
    transportPercentages.requirePercentages("transportPercentages")
    phonePercentages.requirePercentages("phonePercentages")
    check(
        rentPercentages.max() + foodPercentages.max() + clothPercentages.max() +
                transportPercentages.max() + phonePercentages.max() < 95
    ) { "Profession expenses leave no positive cash flow" }
    val poorestSalary = salaries.min()
    val heaviestExpenses = listOf(
        rentPercentages,
        foodPercentages,
        clothPercentages,
        transportPercentages,
        phonePercentages,
    ).sumOf { percentages -> poorestSalary.expenseShare(percentages.max()) }
    check(poorestSalary > heaviestExpenses) {
        "The lowest salary $poorestSalary does not cover its $heaviestExpenses of expenses"
    }

    smallBusinessPrices.requireAmounts("smallBusinessPrices")
    mediumBusinessPrices.requireAmounts("mediumBusinessPrices")
    bigBusinessPrices.requireAmounts("bigBusinessPrices")
    check(smallBusinessPrices.min() < mediumBusinessPrices.min()) { "Small and medium business tiers overlap" }
    check(mediumBusinessPrices.min() < bigBusinessPrices.min()) { "Medium and big business tiers overlap" }
    smallBusinessReturnPercentages.requireRange("smallBusinessReturnPercentages", 1L..200L)
    mediumBusinessReturnPercentages.requireRange("mediumBusinessReturnPercentages", 1L..100L)
    bigBusinessReturnPercentages.requireRange("bigBusinessReturnPercentages", 1L..60L)

    check(shoppingPrices.keys == ShopType.entries.toSet()) { "shoppingPrices misses a shop type" }
    check(shopWeights.keys == ShopType.entries.toSet()) { "shopWeights misses a shop type" }
    shoppingPrices.forEach { (type, prices) -> prices.requireAmounts("shoppingPrices[$type]") }
    shopWeights.values.toList().requireRange("shopWeights", 1..10_000)
    val tierMedians = shopTiers.map { type -> shoppingPrices.getValue(type).median() }
    check(tierMedians.zipWithNext().all { (cheaper, dearer) -> cheaper < dearer }) {
        "Shop tiers must grow in price in the order ${shopTiers.joinToString()}"
    }

    expensePrices.requireAmounts("expensePrices")
    randomJobProfits.requireAmounts("randomJobProfits")
    estatePrices.requireAmounts("estatePrices")
    estateSalePercentages.requireRange("estateSalePercentages", 30L..180L)
    val dearestEstateSale = estatePrices.max() * estateSalePercentages.max() / 100
    check(dearestEstateSale <= estatePrices.min() * MAX_ASSET_SPREAD) {
        "Estate speculation is too profitable: $dearestEstateSale against ${estatePrices.min()} to buy"
    }
    check(shares.size >= 6) { "shares has too few instruments" }
    check(shares.map { it.id }.distinct().size == shares.size) { "shares contains duplicate ids" }
    check(shares.map { it.ticker.lowercase() }.distinct().size == shares.size) { "shares contains duplicate tickers" }
    shares.forEach { share ->
        check(share.id.matches(SHARE_ID_PATTERN)) { "share id ${share.id} is invalid" }
        check(share.ticker.length in 2..8 && share.ticker.none(Char::isWhitespace)) {
            "share ticker ${share.ticker} is invalid"
        }
        check(generatedLocales.all { share.names[it]?.length in 2..80 }) {
            "share ${share.id} has incomplete names"
        }
    }
    generatedLocales.forEach { locale ->
        check(shares.map { it.names.getValue(locale).lowercase() }.distinct().size == shares.size) {
            "shares contains duplicate $locale names"
        }
    }
    sharePrices.requireAmounts("sharePrices")
    forcedShareSalePrices.requireAmounts("forcedShareSalePrices")
    check(forcedShareSalePrices.max() < sharePrices.min()) {
        "Forced share sale prices must be below every regular share price"
    }
    shareCounts.requireAmounts("shareCounts")
    businessExtensionProfits.requireAmounts("businessExtensionProfits")
    landAreas.requireAmounts("landAreas")
    landPricePerUnit.requireAmounts("landPricePerUnit")
    eventLandPricePercentages.requireRange("eventLandPricePercentages", 30L..180L)
    val dearestLandSale = landPricePerUnit.max() * eventLandPricePercentages.max() / 100
    check(dearestLandSale <= landPricePerUnit.min() * MAX_ASSET_SPREAD) {
        "Land speculation is too profitable: $dearestLandSale per unit against ${landPricePerUnit.min()} to buy"
    }
    corruptBusinessPrices.requireAmounts("corruptBusinessPrices")
    corruptBusinessReturnPercentages.requireRange("corruptBusinessReturnPercentages", 1L..500L)
    corruptOneTimeReturnPercentages.requireRange("corruptOneTimeReturnPercentages", 100L..5_000L)
    corruptBusinessDeputies.requireRange("corruptBusinessDeputies", 1..20)
    corruptLandPricePerUnit.requireAmounts("corruptLandPricePerUnit")
    corruptLandAreas.requireAmounts("corruptLandAreas")
    corruptLandDeputies.requireRange("corruptLandDeputies", 1..20)
    check(corruptDeputyPercentage in 1..99) { "corruptDeputyPercentage is invalid" }
    check(corruptOneTimePercentage in 1..99) { "corruptOneTimePercentage is invalid" }
    check(forcedShareSalePercentage in 5..40) { "forcedShareSalePercentage is invalid" }
    check(strayAnimalPercentage in 1..30) { "strayAnimalPercentage is invalid" }
    check(depositRate in 1..20) { "depositRate is invalid" }
    check(loanRate in 1..50) { "loanRate is invalid" }
    check(listOf(
        babyRecurringCost,
        carRecurringCost,
        apartmentRecurringCost,
        houseRecurringCost,
        yachtRecurringCost,
        planeRecurringCost,
        animalRecurringCost,
        marriageCost,
        childBenefit,
    ).all { it in 1..MAX_GENERATED_AMOUNT }) { "player economy values are invalid" }
    check(loanLimit in 1..MAX_GENERATED_AMOUNT) { "loanLimit is invalid" }
    check(businessLimit in 1..1_000) { "businessLimit is invalid" }
    check(outerCircleMinimumCashFlow in 1..MAX_GENERATED_AMOUNT) {
        "outerCircleMinimumCashFlow is invalid"
    }
    check(outerCircleMinimumAccountBalance in 1..MAX_GENERATED_AMOUNT) {
        "outerCircleMinimumAccountBalance is invalid"
    }
    check(victoryMinimumAccountBalance in 1..MAX_GENERATED_AMOUNT) {
        "victoryMinimumAccountBalance is invalid"
    }
    check(victoryMinimumAccountBalance > outerCircleMinimumAccountBalance) {
        "Victory balance must exceed the outer-circle balance"
    }
    check(dreamMinPrice in 1..MAX_GENERATED_AMOUNT) { "dreamMinPrice is invalid" }
    check(dreamMaxPrice in 1..MAX_GENERATED_AMOUNT) { "dreamMaxPrice is invalid" }
    check(dreamMaxPrice - dreamMinPrice >= dreamSlotIds.size) {
        "Dream prices leave no room for ${dreamSlotIds.size} distinct dreams"
    }
    check(dreamMinPrice <= victoryMinimumAccountBalance) {
        "The cheapest dream costs more than the victory balance"
    }

    listOf(
        chanceWeights.randomJob,
        chanceWeights.estate,
        chanceWeights.land,
        chanceWeights.shares,
        chanceWeights.corruptBusiness,
        chanceWeights.corruptLand,
        eventWeights.land,
        eventWeights.estate,
        eventWeights.shares,
        eventWeights.businessExtending,
        eventWeights.reelection,
        eventWeights.announcement,
    ).requireRange("card weights", 1..10_000)
}

internal fun GeneratedBalance.outerCircleConditions() = OuterCircleConditions(
    minimumCashFlow = outerCircleMinimumCashFlow,
    apartmentRequired = outerCircleApartmentRequired,
    carRequired = outerCircleCarRequired,
    minimumAccountBalance = outerCircleMinimumAccountBalance,
)

internal fun GeneratedBalance.victoryConditions() = VictoryConditions(
    dreamRequired = victoryDreamRequired,
    planeRequired = victoryPlaneRequired,
    estateRequired = victoryEstateRequired,
    minimumAccountBalance = victoryMinimumAccountBalance,
)

internal fun GeneratedBalance.playerConfig() = Config(
    depositRate = depositRate,
    loadRate = loanRate,
    babyCost = babyRecurringCost,
    carCost = carRecurringCost,
    apartmentCost = apartmentRecurringCost,
    cottageCost = houseRecurringCost,
    yachtCost = yachtRecurringCost,
    flightCost = planeRecurringCost,
    animalCost = animalRecurringCost,
    marriageCost = marriageCost,
)

internal fun List<Long>.median(): Long = sorted()[size / 2]

internal fun GeneratedBalance.withoutDuplicateOptions(): GeneratedBalance = copy(
    salaries = salaries.distinct(),
    rentPercentages = rentPercentages.distinct(),
    foodPercentages = foodPercentages.distinct(),
    clothPercentages = clothPercentages.distinct(),
    transportPercentages = transportPercentages.distinct(),
    phonePercentages = phonePercentages.distinct(),
    smallBusinessPrices = smallBusinessPrices.distinct(),
    smallBusinessReturnPercentages = smallBusinessReturnPercentages.distinct(),
    mediumBusinessPrices = mediumBusinessPrices.distinct(),
    mediumBusinessReturnPercentages = mediumBusinessReturnPercentages.distinct(),
    bigBusinessPrices = bigBusinessPrices.distinct(),
    bigBusinessReturnPercentages = bigBusinessReturnPercentages.distinct(),
    shoppingPrices = shoppingPrices.mapValues { (_, prices) -> prices.distinct() },
    expensePrices = expensePrices.distinct(),
    randomJobProfits = randomJobProfits.distinct(),
    estatePrices = estatePrices.distinct(),
    estateSalePercentages = estateSalePercentages.distinct(),
    sharePrices = sharePrices.distinct(),
    forcedShareSalePrices = forcedShareSalePrices.distinct(),
    shareCounts = shareCounts.distinct(),
    businessExtensionProfits = businessExtensionProfits.distinct(),
    landAreas = landAreas.distinct(),
    landPricePerUnit = landPricePerUnit.distinct(),
    eventLandPricePercentages = eventLandPricePercentages.distinct(),
    corruptBusinessPrices = corruptBusinessPrices.distinct(),
    corruptBusinessReturnPercentages = corruptBusinessReturnPercentages.distinct(),
    corruptOneTimeReturnPercentages = corruptOneTimeReturnPercentages.distinct(),
    corruptLandPricePerUnit = corruptLandPricePerUnit.distinct(),
    corruptLandAreas = corruptLandAreas.distinct(),
)

internal fun GeneratedBalance.withBoundedAssetPrices(): GeneratedBalance = copy(
    estatePrices = estatePrices.withinSaleSpread(estateSalePercentages),
    landPricePerUnit = landPricePerUnit.withinSaleSpread(eventLandPricePercentages),
)

internal fun List<Long>.withinSaleSpread(salePercentages: List<Long>): List<Long> {
    if (size < 2 || salePercentages.isEmpty()) return this
    val sortedPrices = sorted()
    val cheapestPrice = sortedPrices.first()
    val highestSalePercentage = salePercentages.max()
    val highestPrice = cheapestPrice * MAX_ASSET_SPREAD * 100 / highestSalePercentage
    if (sortedPrices.last() <= highestPrice || highestPrice - cheapestPrice < lastIndex) return this
    return sortedPrices.mapIndexed { index, _ ->
        cheapestPrice + (highestPrice - cheapestPrice) * index / lastIndex
    }
}

internal fun List<Long>.requireAmounts(name: String, minimumSize: Int = 3) {
    requireRange(name, 1L..MAX_GENERATED_AMOUNT, minimumSize)
}

internal fun List<Long>.requirePercentages(name: String) {
    requireRange(name, 1L..60L, 3)
}

internal fun List<Long>.requireRange(name: String, range: LongRange, minimumSize: Int = 3) {
    check(size >= minimumSize) { "$name has too few values" }
    check(distinct().size == size) { "$name contains duplicates" }
    check(all { it in range }) { "$name contains an out-of-range value" }
}

internal fun List<Int>.requireRange(name: String, range: IntRange) {
    check(isNotEmpty() && all { it in range }) { "$name contains an out-of-range value" }
}

internal const val MAX_ASSET_SPREAD = 3
internal const val MAX_GENERATED_AMOUNT = 1_000_000_000L
internal val SHARE_ID_PATTERN = Regex("[a-z][a-z0-9_-]{1,31}")
