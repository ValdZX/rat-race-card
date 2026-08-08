package ua.vald_zx.game.rat.race.server.generation

import ua.vald_zx.game.rat.race.card.shared.ShareSectors
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
    val medianSalary = salaries.median()
    check(smallBusinessPrices.min() <= medianSalary * SMALL_BUSINESS_ENTRY_UNITS) {
        "The cheapest small business ${smallBusinessPrices.min()} is above " +
                "${medianSalary * SMALL_BUSINESS_ENTRY_UNITS} " +
                "($SMALL_BUSINESS_ENTRY_UNITS × median salary $medianSalary)"
    }
    check(smallBusinessPrices.max() <= medianSalary * SMALL_BUSINESS_MAX_UNITS) {
        "The dearest small business ${smallBusinessPrices.max()} is above " +
                "${medianSalary * SMALL_BUSINESS_MAX_UNITS} " +
                "($SMALL_BUSINESS_MAX_UNITS × median salary $medianSalary)"
    }
    check(smallBusinessPrices.max() < mediumBusinessPrices.min()) {
        "Small and medium business tiers overlap: the dearest small business ${smallBusinessPrices.max()} " +
                "is not below the cheapest medium business ${mediumBusinessPrices.min()}"
    }
    check(mediumBusinessPrices.min() >= medianSalary * MEDIUM_BUSINESS_MIN_UNITS) {
        "The cheapest medium business ${mediumBusinessPrices.min()} is below " +
                "${medianSalary * MEDIUM_BUSINESS_MIN_UNITS} " +
                "($MEDIUM_BUSINESS_MIN_UNITS × median salary $medianSalary)"
    }
    check(mediumBusinessPrices.max() <= medianSalary * MEDIUM_BUSINESS_MAX_UNITS) {
        "The dearest medium business ${mediumBusinessPrices.max()} is above " +
                "${medianSalary * MEDIUM_BUSINESS_MAX_UNITS} " +
                "($MEDIUM_BUSINESS_MAX_UNITS × median salary $medianSalary)"
    }
    check(mediumBusinessPrices.min() < bigBusinessPrices.min()) {
        "Medium and big business tiers overlap: the cheapest medium business ${mediumBusinessPrices.min()} " +
                "is not below the cheapest big business ${bigBusinessPrices.min()}"
    }
    check(bigBusinessPrices.min() >= medianSalary * BIG_BUSINESS_MIN_UNITS) {
        "The cheapest big business ${bigBusinessPrices.min()} is below " +
                "${medianSalary * BIG_BUSINESS_MIN_UNITS} " +
                "($BIG_BUSINESS_MIN_UNITS × median salary $medianSalary)"
    }
    check(bigBusinessPrices.max() <= medianSalary * BIG_BUSINESS_MAX_UNITS) {
        "The dearest big business ${bigBusinessPrices.max()} is above " +
                "${medianSalary * BIG_BUSINESS_MAX_UNITS} " +
                "($BIG_BUSINESS_MAX_UNITS × median salary $medianSalary)"
    }
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
    check(shares.all { it.sector in ShareSectors.all }) {
        "shares must use known sectors: ${ShareSectors.all.joinToString()}"
    }
    check(shares.map { it.sector }.distinct().size >= MIN_SHARE_SECTORS) {
        "shares must span at least $MIN_SHARE_SECTORS sectors so diversification matters"
    }
    check(shares.groupingBy { it.sector }.eachCount().values.none { it > shares.size - 2 }) {
        "one sector holds almost every company, so a crash cannot be correlated"
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
    check(corruptBusinessReturnPercentages.min() >= bigBusinessReturnPercentages.max() * 2) {
        "Corrupt recurring business must return at least twice the best regular big business rate"
    }
    corruptOneTimeReturnPercentages.requireRange("corruptOneTimeReturnPercentages", 300L..5_000L)
    corruptBusinessSalePercentages.requireRange("corruptBusinessSalePercentages", 200L..1_000L)
    corruptBusinessDeputies.requireRange("corruptBusinessDeputies", 1..20)
    corruptLandPricePerUnit.requireAmounts("corruptLandPricePerUnit")
    corruptLandAreas.requireAmounts("corruptLandAreas")
    check(landAreas.max() < corruptLandAreas.min()) {
        "Corrupt land areas must be larger than regular land areas"
    }
    corruptLandSalePercentages.requireRange("corruptLandSalePercentages", 150L..1_000L)
    val cheapestCorruptLandSale = corruptLandPricePerUnit.min() * corruptLandSalePercentages.min() / 100
    check(cheapestCorruptLandSale >= corruptLandPricePerUnit.max() * 2) {
        "Corrupt land sale must pay at least twice the highest corrupt land purchase price"
    }
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
        deputyCardPrice,
    ).all { it in 1..MAX_GENERATED_AMOUNT }) { "player economy values are invalid" }
    check(currency.length in 1..MAX_CURRENCY_LENGTH) {
        "currency \"$currency\" must be 1..$MAX_CURRENCY_LENGTH characters"
    }
    check(currency.none { it.isWhitespace() || it.isDigit() }) {
        "currency \"$currency\" must not contain digits or whitespace"
    }
    check(taxInspectionBribePercentage == 20L) { "taxInspectionBribePercentage must be 20" }
    check(mediumRiskMultiplier in 2..20) { "mediumRiskMultiplier is invalid" }
    check(highRiskMultiplier in 2..20 && highRiskMultiplier > mediumRiskMultiplier) {
        "highRiskMultiplier is invalid"
    }
    check(salaryFundRates.size == 4 && salaryFundRates.all { it in 1..100 }) {
        "salaryFundRates are invalid"
    }
    check(salaryFundRates.zipWithNext().all { (higher, lower) -> higher > lower }) {
        "salaryFundRates must decrease towards Start"
    }
    check(fundBaseRate in 1..100) { "fundBaseRate is invalid" }
    check(fundStartRate in 1..100) { "fundStartRate is invalid" }
    check(restTurnCount in 1..10) { "restTurnCount is invalid" }
    check(divorceAssetRetentionPercentage in 10..90) {
        "divorceAssetRetentionPercentage is invalid"
    }
    check(carMovementBonus in 0..6) { "carMovementBonus is invalid" }
    check(planeMovementBonus in carMovementBonus..12) { "planeMovementBonus is invalid" }
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

    check(paydayRate >= loanRate * 2) {
        "The payday rate $paydayRate must be at least twice the credit line rate $loanRate to be a real trap"
    }

    scamPrices.requireAmounts("scamPrices")
    scamPromisedReturnPercentages.requireRange("scamPromisedReturnPercentages", MIN_SCAM_RETURN..MAX_SCAM_RETURN)
    check(scamSuccessPercentage in 1..MAX_SCAM_SUCCESS) {
        "scamSuccessPercentage must be between 1 and $MAX_SCAM_SUCCESS so a scam stays a losing bet"
    }
    val bestScamExpectation = scamPromisedReturnPercentages.max() * scamSuccessPercentage
    check(bestScamExpectation < 100 * 100) {
        "The best scam returns $bestScamExpectation per 10000 staked, so investing in fraud pays off on average"
    }
    check(scamPrices.min() < sharePrices.max()) {
        "Scams cost more than any honest instrument, so nobody can be tempted"
    }

    crashSectorDropPercentages.requireRange("crashSectorDropPercentages", MIN_CRASH_DROP..MAX_CRASH_DROP)
    crashMarketDropPercentages.requireRange("crashMarketDropPercentages", 1L..MIN_CRASH_DROP)
    check(crashMarketDropPercentages.max() < crashSectorDropPercentages.min()) {
        "A crash must hit its own sector harder than the rest of the market"
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
        eventWeights.corruptBusiness,
        eventWeights.corruptLand,
        chanceWeights.scam,
        eventWeights.marketCrash,
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
    paydayRate = paydayRate,
    babyCost = babyRecurringCost,
    carCost = carRecurringCost,
    apartmentCost = apartmentRecurringCost,
    cottageCost = houseRecurringCost,
    yachtCost = yachtRecurringCost,
    flightCost = planeRecurringCost,
    animalCost = animalRecurringCost,
    fundBaseRate = fundBaseRate,
    fundStartRate = fundStartRate,
    marriageCost = marriageCost,
    childBenefit = childBenefit,
    deputyCardPrice = deputyCardPrice,
    taxInspectionBribePercentage = taxInspectionBribePercentage,
    mediumRiskMultiplier = mediumRiskMultiplier,
    highRiskMultiplier = highRiskMultiplier,
    salaryFundRates = salaryFundRates,
    restTurnCount = restTurnCount,
    divorceAssetRetentionPercentage = divorceAssetRetentionPercentage,
    carMovementBonus = carMovementBonus,
    planeMovementBonus = planeMovementBonus,
)

internal fun List<Long>.median(): Long {
    val sorted = sorted()
    val middle = size / 2
    return if (size % 2 == 1) sorted[middle] else (sorted[middle - 1] + sorted[middle]) / 2
}

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
    corruptBusinessSalePercentages = corruptBusinessSalePercentages.distinct(),
    corruptLandPricePerUnit = corruptLandPricePerUnit.distinct(),
    corruptLandAreas = corruptLandAreas.distinct(),
    corruptLandSalePercentages = corruptLandSalePercentages.distinct(),
)

internal fun GeneratedBalance.withBoundedAssetPrices(): GeneratedBalance = copy(
    estatePrices = estatePrices.withinSaleSpread(estateSalePercentages),
    landPricePerUnit = landPricePerUnit.withinSaleSpread(eventLandPricePercentages),
)

internal fun GeneratedBalance.withBusinessTiersOnSalaryScale(): GeneratedBalance {
    if (salaries.isEmpty()) return this
    val unit = salaries.median()
    if (unit <= 0) return this

    val smallFloor = minOf(smallBusinessPrices.minOrNull() ?: 1, unit * SMALL_BUSINESS_ENTRY_UNITS)
        .coerceAtLeast(1)
    val small = smallBusinessPrices.redistributedInto(smallFloor, unit * SMALL_BUSINESS_MAX_UNITS)

    val mediumFloor = maxOf(unit * MEDIUM_BUSINESS_MIN_UNITS, (small.maxOrNull() ?: 0) + 1)
    val medium = mediumBusinessPrices.redistributedInto(mediumFloor, unit * MEDIUM_BUSINESS_MAX_UNITS)

    val bigFloor = maxOf(unit * BIG_BUSINESS_MIN_UNITS, (medium.minOrNull() ?: 0) + 1)
    val big = bigBusinessPrices.redistributedInto(bigFloor, unit * BIG_BUSINESS_MAX_UNITS)

    return copy(
        smallBusinessPrices = small,
        mediumBusinessPrices = medium,
        bigBusinessPrices = big,
    )
}

internal fun List<Long>.redistributedInto(lowest: Long, highest: Long): List<Long> {
    if (size < 2 || highest - lowest < lastIndex) return this
    val sorted = sorted()
    val first = sorted.first().coerceIn(lowest, highest - lastIndex)
    val last = sorted.last().coerceIn(first + lastIndex, highest)
    if (first == sorted.first() && last == sorted.last()) return this
    return List(size) { index -> first + (last - first) * index / lastIndex }
}

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

internal const val MAX_CURRENCY_LENGTH = 4
internal const val SMALL_BUSINESS_ENTRY_UNITS = 2L
internal const val SMALL_BUSINESS_MAX_UNITS = 20L
internal const val MEDIUM_BUSINESS_MIN_UNITS = 15L
internal const val MEDIUM_BUSINESS_MAX_UNITS = 1_500L
internal const val BIG_BUSINESS_MIN_UNITS = 500L
internal const val BIG_BUSINESS_MAX_UNITS = 15_000L
internal const val MIN_SHARE_SECTORS = 3
internal const val MIN_SCAM_RETURN = 150L
internal const val MAX_SCAM_RETURN = 900L
internal const val MAX_SCAM_SUCCESS = 20
internal const val MIN_CRASH_DROP = 30L
internal const val MAX_CRASH_DROP = 80L
internal const val MAX_ASSET_SPREAD = 3
internal const val MAX_GENERATED_AMOUNT = 1_000_000_000L
internal val SHARE_ID_PATTERN = Regex("[a-z][a-z0-9_-]{1,31}")
