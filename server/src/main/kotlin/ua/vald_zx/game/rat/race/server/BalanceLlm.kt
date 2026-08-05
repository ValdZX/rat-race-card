package ua.vald_zx.game.rat.race.server

import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.serialization.json.Json
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import ua.vald_zx.game.rat.race.card.shared.BoardLayer
import ua.vald_zx.game.rat.race.card.shared.GeneratedBalance
import ua.vald_zx.game.rat.race.card.shared.Config
import ua.vald_zx.game.rat.race.card.shared.OuterCircleConditions
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.shared.ShopType
import ua.vald_zx.game.rat.race.card.shared.VictoryConditions
import ua.vald_zx.game.rat.race.card.shared.dreamSlotIds
import ua.vald_zx.game.rat.race.card.shared.generatedLocales
import ua.vald_zx.game.rat.race.card.shared.shopTiers

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
                json.decodeFromString<GeneratedBalance>(answer.jsonObject()).withoutDuplicateOptions()
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
        append("Ти геймдизайнер економічної настільної гри. ")
        append("Створи цілісну економічну модель світу й відповідай лише одним JSON-об'єктом без markdown. ")
        append("Усі значення мають бути цілими числами, додатними, різноманітними та придатними для тривалої гри. ")
        append("Малий, середній і великий бізнес повинні утворювати три відчутні рівні капіталу. ")
        append("Сума максимальних побутових відсотків має бути меншою за 95. ")
        append("Ваги визначають відносну частоту типів карток і мають бути більшими за нуль.")
        append(" Гра допускає доречний легкий гумор, тому назви компаній можуть бути дотепними й органічними для світу.")
        append(" Згенеруй унікальні компанії цього світу: стабільний ASCII id, короткий тикер і природні назви всіма вказаними мовами.")
    }

    private fun userPrompt(
        world: BoardGeneration,
        deckSizes: Map<BoardCardType, Int>,
        previousError: String?,
    ) = buildString {
        val described = listOfNotNull(
            world.theme.ifBlank { null }?.let { "Тематика: $it." },
            world.locality.ifBlank { null }?.let { "Місцевість: $it." },
            world.epoch.ifBlank { null }?.let { "Епоха: $it." },
        )
        if (described.isEmpty()) {
            appendLine("Світ: звичайне сучасне місто.")
        } else {
            described.forEach(::appendLine)
        }
        append("Seed світу: ${world.seed}.\n")
        append("Валюта, масштаби зарплат, цін, ділянок, пакетів акцій і прибутків мають природно відповідати цьому світу.\n")
        append(GAME_MECHANICS)
        append(boardLayoutPrompt())
        append(tempoPrompt())
        append(deckSizesPrompt(deckSizes))
        append(BALANCE_REQUIREMENTS)
        previousError?.takeIf { it.isNotBlank() }?.let { error ->
            append("Попередню відповідь відхилено: ${error.substringBefore('\n')}. Виправ цю помилку в новій повній відповіді.\n")
        }
        append("Поверни всі поля цієї структури:\n")
        append(BALANCE_SCHEMA)
    }

    private fun String.jsonObject(): String {
        val start = indexOf('{')
        val end = lastIndexOf('}')
        check(start >= 0 && end > start) { "LLM balance is not a JSON object" }
        return substring(start, end + 1)
    }
}

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

private fun List<Long>.median(): Long = sorted()[size / 2]

private fun GeneratedBalance.withoutDuplicateOptions(): GeneratedBalance = copy(
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

private fun List<Long>.requireAmounts(name: String, minimumSize: Int = 3) {
    requireRange(name, 1L..MAX_GENERATED_AMOUNT, minimumSize)
}

private fun List<Long>.requirePercentages(name: String) {
    requireRange(name, 1L..60L, 3)
}

private fun List<Long>.requireRange(name: String, range: LongRange, minimumSize: Int = 3) {
    check(size >= minimumSize) { "$name has too few values" }
    check(distinct().size == size) { "$name contains duplicates" }
    check(all { it in range }) { "$name contains an out-of-range value" }
}

private fun List<Int>.requireRange(name: String, range: IntRange) {
    check(isNotEmpty() && all { it in range }) { "$name contains an out-of-range value" }
}

private const val MAX_BALANCE_ATTEMPTS = 3
private const val AVERAGE_STEP = 3.5
private const val MAX_ASSET_SPREAD = 3
private const val MAX_GENERATED_AMOUNT = 1_000_000_000L
private val SHARE_ID_PATTERN = Regex("[a-z][a-z0-9_-]{1,31}")
private val balanceLogger = KtorSimpleLogger("BalanceLlm")

private fun boardLayoutPrompt() = buildString {
    append("Фіксована структура поля, яку треба врахувати в частотах і сумах карток:\n")
    BoardLayer.entries.forEach { layer ->
        val counts = layer.places.groupingBy { it.name }.eachCount().entries
            .sortedBy { it.key }
            .joinToString(", ") { (name, count) -> "$name=$count" }
        append("- ${layer.name}: ${layer.places.size} клітинок; $counts.\n")
    }
}

private fun tempoPrompt() = buildString {
    append("Темп гри, від якого залежить, чи окупляться ціни:\n")
    append("- Гравець кидає один кубик 1..6, тобто в середньому проходить $AVERAGE_STEP клітинки за хід.\n")
    BoardLayer.entries.forEach { layer ->
        val salaryCount = layer.places.count { it == PlaceType.Salary }
        val movesPerLap = (layer.places.size / AVERAGE_STEP).toInt()
        val movesPerSalary = if (salaryCount > 0) (layer.places.size / salaryCount / AVERAGE_STEP).toInt() else 0
        append("- ${layer.name}: повне коло за ~$movesPerLap ходів")
        if (movesPerSalary > 0) append(", зарплата приблизно кожні $movesPerSalary ходів")
        append(".\n")
    }
    append("- Гравець починає з нульовою готівкою, перший дохід — це зарплата.\n")
    append("- Стартові ціни мають бути досяжними за кілька зарплат, інакше перші ходи будуть порожніми.\n")
}

private fun deckSizesPrompt(deckSizes: Map<BoardCardType, Int>) = buildString {
    if (deckSizes.isEmpty()) return@buildString
    append("Розміри колод цього столу — від них залежить, скільки разів ефект трапиться за партію:\n")
    deckSizes.entries.sortedBy { it.key.name }.forEach { (type, size) ->
        append("- ${type.name}: $size карток.\n")
    }
}

private val GAME_MECHANICS = """
Механіки, з якими баланс має бути узгоджений:
- На внутрішньому колі гравець отримує зарплату, купує малий/середній бізнес, землю, нерухомість, речі й акції та сплачує загальні або умовні витрати.
- Професія задає стать, зарплату й регулярні витрати; стать, шлюб, діти та володіння активами впливають на події й на те, хто платить умовну витрату.
- Акції купують пакетами на картках Chance. Звичайна ринкова подія дозволяє продати будь-яку кількість або відмовитися.
- Частина ринкових подій примусово продає всі акції вказаної компанії кожного власника без права відмовитися. forcedShareSalePrices завжди нижчі за будь-яку ціну купівлі з sharePrices; це збиток, але не обнулення ціни.
- Корупційні угоди потребують депутатів. Корупційний бізнес дає або регулярний прибуток, або одну разову виплату, але не обидва ефекти одночасно.
- Розширення бізнесу збільшує регулярний прибуток одного малого бізнесу. Перевибори змінюють склад депутатів.
- Депозит дає depositRate відсотків доходу, кредит створює loanRate відсотків витрат. Діти, автомобілі, квартири, будинки, яхти й літаки додають відповідні регулярні витрати.
- Клітинка шлюбу одружує гравця; чоловік сплачує marriageCost. Клітинка дитини додає дитину жінці або одруженому чоловіку, дає childBenefit і надалі збільшує регулярні витрати.
- Розлучення знімає шлюб; чоловік також втрачає половину готівки й депозиту та дітей. Банкрутство забирає випадковий бізнес, відставка — роботу, відпочинок — два ходи.
- Прохід повз Salary виплачує поточний грошовий потік. На клітинках Salary доступні фонди зі ставками 20%, 15%, 10% або 5%; на Start фонди капіталізуються, а при точному попаданні ставка дорівнює 30%.
- Desire пропонує купити ще не придбану мрію; придбані мрії спільні для дошки й можуть бути умовою перемоги. TaxInspection наразі не має прямого фінансового ефекту.
- Ризикові інвестиції мають фіксовані виплати: середній ризик x2, високий ризик x6. Ці множники врахуй у загальній складності економіки.
- Перехід на зовнішнє коло та перемога залежать від згенерованих фінансових і майнових умов; значення мають робити шлях складним, але досяжним.
""".trimIndent() + "\n"

private val BALANCE_REQUIREMENTS = """
Обов'язкові обмеження:
- salaries: щонайменше 10 унікальних сум.
- shares: щонайменше 6 унікальних компаній; усі id, ticker, uk та en назви унікальні.
- Кожен share id відповідає regex [a-z][a-z0-9_-]{1,31}, використовує лише малі латинські літери, цифри, _ або -.
- Кожен ticker має 2..8 символів без пробілів.
- Кожен інший числовий масив: щонайменше 3 унікальні значення.
- shoppingPrices і shopWeights мають ключі ANIMAL, AUTO, APARTMENT, HOUSE, YACHT, FLY — усі шість обов'язкові.
- Медіани shoppingPrices мають строго зростати саме в порядку ANIMAL < AUTO < APARTMENT < HOUSE < YACHT < FLY.
- Тварину купують у крамниці як помітну, але найдешевшу з покупок; вона додає animalRecurringCost щоходу.
- landPricePerUnit — ціна ОДНІЄЇ одиниці площі. Загальна ціна ділянки = площа × ця ціна, тож вона має бути малою поряд із цінами бізнесів.
- eventLandPricePercentages: 30..180 — за скільки відсотків від landPricePerUnit ринок викуповує одиницю площі. Значення нижче 100 — збиток власника, вище 100 — вигідний продаж.
- Продавати землю необов'язково, тож гравець завжди дочекається найкращої ціни: найдорожчий продаж (max landPricePerUnit × max eventLandPricePercentages) не може перевищувати найдешевшу купівлю більш ніж утричі. Тримай landPricePerUnit у вузькому діапазоні.
- estatePrices — ціна одного об'єкта нерухомості на картці випадку. estateSalePercentages: 30..180 — за скільки відсотків від тієї ж бази ринкова подія викуповує об'єкт.
- Те саме правило втричі діє й для нерухомості: max estatePrices × max estateSalePercentages не може перевищувати min estatePrices більш ніж утричі.
- randomJobProfits — разовий дохід від підробітку. Це дохід, а не ціна активу: тримай його помітно нижчим за estatePrices, інакше випадкова робота знецінить нерухомість.
- corruptLandPricePerUnit працює так само для корупційних ділянок.
- strayAnimalPercentage: 1..30 — частка карток витрат, де гравець підбирає бродячу або врятовану тварину: він платить за неї й отримує тварину.
- dreamMinPrice і dreamMaxPrice — межі цін ${dreamSlotIds.size} мрій; сервер рівномірно розподілить решту між ними.
- dreamMinPrice не може перевищувати victoryMinimumAccountBalance, інакше мрію неможливо купити, а вона є умовою перемоги.
- forcedShareSalePrices: щонайменше 3 унікальні додатні ціни; кожна нижча за найменше значення sharePrices.
- rentPercentages: 10..30; foodPercentages: 5..20; clothPercentages: 2..10; transportPercentages: 2..15; phonePercentages: 1..5.
- smallBusinessReturnPercentages: 1..200; mediumBusinessReturnPercentages: 1..100; bigBusinessReturnPercentages: 1..60.
- corruptBusinessReturnPercentages: 1..500; corruptOneTimeReturnPercentages: 100..5000.
- corruptBusinessDeputies і corruptLandDeputies: 1..20.
- corruptDeputyPercentage і corruptOneTimePercentage: 1..99.
- forcedShareSalePercentage: 5..40; примусові продажі мають бути відчутними, але не домінувати в колоді.
- depositRate: 1..20; loanRate: 1..50.
- Найменша зарплата має покривати свої витрати: сума rent+food+cloth+transport+phone від неї рахується у відсотках і округлюється до десятків, тож надто дрібні зарплати відхиляються.
- babyRecurringCost, carRecurringCost, apartmentRecurringCost, houseRecurringCost, yachtRecurringCost, planeRecurringCost, animalRecurringCost, marriageCost і childBenefit — додатні суми, узгоджені із зарплатами та цінами активів.
- Усі суми та кількості додатні й не перевищують 1000000000.
- Усі ваги карток: 1..10000.
- loanLimit, businessLimit, умови переходу на зовнішнє коло та перемоги мають відповідати масштабу цієї економіки.
- businessLimit: 1..1000.
- victoryMinimumAccountBalance має бути більшим за outerCircleMinimumAccountBalance.
- Булеві умови володіння й бонус руху обери так, щоб партія була різноманітною, але прохідною.
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
  "shares":[{"id":"ascii_id","ticker":"CODE","names":{"uk":"Назва","en":"Name"}}],
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
