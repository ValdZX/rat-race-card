package ua.vald_zx.game.rat.race.server

import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import ua.vald_zx.game.rat.race.card.shared.BoardLayer
import ua.vald_zx.game.rat.race.card.shared.Gender
import ua.vald_zx.game.rat.race.card.shared.PayerType
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.shared.ProfessionCard
import ua.vald_zx.game.rat.race.card.shared.SharesType
import ua.vald_zx.game.rat.race.card.shared.code
import ua.vald_zx.game.rat.race.card.shared.seedFor
import kotlin.random.Random

internal interface CardTextSource {
    fun describe(request: CardTextRequest): String
}

internal enum class CardKind {
    DEFAULT,
    CORRUPT,
    REELECTION,
    ANNOUNCEMENT,
}

internal data class CardTextRequest(
    val type: BoardCardType,
    val cardId: Int,
    val world: BoardGeneration,
    val price: Long,
    val profit: Long,
    val kind: CardKind = CardKind.DEFAULT,
)

internal object TemplateTextSource : CardTextSource {
    override fun describe(request: CardTextRequest): String {
        val random = Random(request.world.seedFor(request.type, request.cardId))
        val place = request.world.locality.ifBlank { "місті" }
        val era = request.world.epoch.ifBlank { "наш час" }
        val theme = request.world.theme.ifBlank { "справа" }
        val opener = openers.random(random)
        when (request.kind) {
            CardKind.CORRUPT ->
                return "$opener у $place без потрібних людей до $theme не підступитись. Ціна питання — ${request.price}. Епоха: $era."

            CardKind.REELECTION ->
                return "$opener у $place оголошено перевибори. Підкуплені депутати повертаються у гру. Епоха: $era."

            CardKind.ANNOUNCEMENT ->
                return "$opener у $place призначено нового високопосадовця, і правила гри навколо $theme змінюються. Епоха: $era."

            CardKind.DEFAULT -> Unit
        }
        return when (request.type) {
            BoardCardType.SmallBusiness,
            BoardCardType.MediumBusiness,
            BoardCardType.BigBusiness,
            -> "$opener у $place відкривається $theme. Вкладення ${request.price}, дохід ${request.profit}. Епоха: $era."

            BoardCardType.Shopping ->
                "$opener у $place пропонують придбати $theme за ${request.price}. Епоха: $era."

            BoardCardType.Expenses ->
                "$opener у $place стається непередбачена витрата на $theme — ${request.price}. Епоха: $era."

            BoardCardType.Chance ->
                "$opener у $place трапляється нагода вкластися в $theme за ${request.price}. Епоха: $era."

            BoardCardType.EventStore ->
                "$opener ринок $theme у $place змінюється. Ціна дня — ${request.price}. Епоха: $era."

            BoardCardType.Deputy ->
                "$opener у $place до вас приходить посадовець, дотичний до $theme. Епоха: $era."
        }
    }

    private val openers = listOf(
        "Кажуть, що",
        "Так сталося, що",
        "Одного ранку",
        "Подейкують, ніби",
        "Раптово",
    )

    private fun List<String>.random(random: Random) = this[random.nextInt(size)]
}

internal class BoardGenerator(
    private val world: BoardGeneration,
    private val texts: CardTextSource = TemplateTextSource,
) {

    fun generate(decks: Map<BoardCardType, Int>): Map<BoardCardType, Map<Int, BoardCard>> {
        if (!world.enabled) return emptyMap()
        return decks.mapValues { (type, size) ->
            (1..size).associateWith { id -> card(type, id) }
        }
    }

    fun generateProfessions(): List<ProfessionCard> {
        if (!world.enabled) return emptyList()
        return Gender.entries.flatMapIndexed { genderIndex, gender ->
            (1..PROFESSIONS_PER_GENDER).map { index ->
                profession(gender, genderIndex * PROFESSIONS_PER_GENDER + index)
            }
        }
    }

    fun generatePlaces(): Map<BoardLayer, List<String>> {
        if (!world.enabled) return emptyMap()
        return BoardLayer.entries.associateWith { layer -> shuffledPlaces(layer).map { it.code() } }
    }

    private fun profession(gender: Gender, id: Int): ProfessionCard {
        val random = Random(world.seedFor("profession", id))
        val salary = SALARIES.pick(random)
        val rent = salary.share(RENT_SHARES.pick(random))
        val food = salary.share(FOOD_SHARES.pick(random))
        val cloth = salary.share(CLOTH_SHARES.pick(random))
        val transport = salary.share(TRANSPORT_SHARES.pick(random))
        val phone = salary.share(PHONE_SHARES.pick(random))
        val title = if (gender == Gender.FEMALE) "Фахівчиня №$id" else "Фахівець №$id"
        val theme = world.theme.ifBlank { null }?.let { " ($it)" }.orEmpty()
        return ProfessionCard(
            id = id,
            name = title + theme,
            salary = salary,
            rent = rent,
            food = food,
            cloth = cloth,
            transport = transport,
            phone = phone,
            gender = gender,
        )
    }

    private fun shuffledPlaces(layer: BoardLayer): List<PlaceType> {
        val random = Random(world.seedFor("layout", layer.level))
        val places = layer.places
        val shuffled = places.toMutableList()

        val movableIndices = places.indices.filter { places[it].isMovable() }
        val movable = movableIndices.map { places[it] }.shuffled(random)
        movableIndices.forEachIndexed { slot, index -> shuffled[index] = movable[slot] }

        val desireIndices = places.indices.filter { places[it] is PlaceType.Desire }
        val desires = desireIndices.map { places[it] }.shuffled(random)
        desireIndices.forEachIndexed { slot, index -> shuffled[index] = desires[slot] }

        return shuffled
    }

    private fun PlaceType.isMovable(): Boolean =
        !isBig && this != PlaceType.Start && this !is PlaceType.Desire

    private fun Long.share(percent: Long): Long = (this * percent / 100 / 10).coerceAtLeast(1) * 10

    private fun card(type: BoardCardType, id: Int): BoardCard {
        val random = Random(world.seedFor(type, id))
        return when (type) {
            BoardCardType.SmallBusiness -> business(type, id, random, SMALL_PRICES, SMALL_RATES)
            BoardCardType.MediumBusiness -> business(type, id, random, MEDIUM_PRICES, MEDIUM_RATES)
            BoardCardType.BigBusiness -> business(type, id, random, BIG_PRICES, BIG_RATES)
            BoardCardType.Shopping -> shopping(type, id, random)
            BoardCardType.Expenses -> expenses(type, id, random)
            BoardCardType.Chance -> chance(type, id, random)
            BoardCardType.EventStore -> eventStore(type, id, random)
            BoardCardType.Deputy -> deputy(type, id, random)
        }
    }

    private fun text(
        type: BoardCardType,
        id: Int,
        price: Long,
        profit: Long,
        kind: CardKind = CardKind.DEFAULT,
    ) = texts.describe(CardTextRequest(type, id, world, price, profit, kind))

    private fun business(
        type: BoardCardType,
        id: Int,
        random: Random,
        prices: List<Long>,
        rates: List<Long>,
    ): BoardCard {
        val price = prices.pick(random)
        val profit = (price * rates.pick(random) / 100).coerceAtLeast(1)
        val description = text(type, id, price, profit)
        val name = world.theme.ifBlank { "Бізнес" } + " №" + id
        return when (type) {
            BoardCardType.SmallBusiness -> BoardCard.SmallBusiness(name, description, price, profit)
            BoardCardType.MediumBusiness -> BoardCard.MediumBusiness(name, description, price, profit)
            else -> BoardCard.BigBusiness(name, description, price, profit)
        }
    }

    private fun shopping(type: BoardCardType, id: Int, random: Random): BoardCard {
        val price = SHOPPING_PRICES.pick(random)
        return BoardCard.Shopping(
            description = text(type, id, price, 0),
            price = price,
            credit = "",
            shopType = ShopTypes.pick(random),
        )
    }

    private fun expenses(type: BoardCardType, id: Int, random: Random): BoardCard {
        val price = EXPENSE_PRICES.pick(random)
        return BoardCard.Expenses(
            description = text(type, id, price, 0),
            priceTitle = "",
            price = price,
            payer = PayerTypes.pick(random),
        )
    }

    private fun chance(type: BoardCardType, id: Int, random: Random): BoardCard {
        val price = CHANCE_PRICES.pick(random)
        return when (CHANCE_KINDS.pick(random)) {
            ChanceKind.RANDOM_JOB -> BoardCard.Chance.RandomJob(text(type, id, price, price), price)
            ChanceKind.ESTATE -> BoardCard.Chance.Estate(
                name = world.locality.ifBlank { "Обʼєкт" } + " №" + id,
                description = text(type, id, price, 0),
                price = price,
            )

            ChanceKind.LAND -> BoardCard.Chance.Land(
                name = world.locality.ifBlank { "Ділянка" } + " №" + id,
                description = text(type, id, price, 0),
                price = price,
                area = LAND_AREAS.pick(random),
            )

            ChanceKind.SHARES -> BoardCard.Chance.Shares(
                description = text(type, id, price, 0),
                price = SHARE_PRICES.pick(random),
                maxCount = SHARE_COUNTS.pick(random),
                sharesType = SharesTypes.pick(random),
            )

            ChanceKind.CORRUPT_BUSINESS -> corruptBusiness(type, id, random)
            ChanceKind.CORRUPT_LAND -> {
                val corruptPrice = CORRUPT_LAND_PRICES.pick(random)
                BoardCard.Chance.CorruptLand(
                    description = text(type, id, corruptPrice, 0, CardKind.CORRUPT),
                    price = corruptPrice,
                    area = CORRUPT_LAND_AREAS.pick(random),
                    deputies = CORRUPT_LAND_DEPUTIES.pick(random),
                )
            }
        }
    }

    private fun corruptBusiness(type: BoardCardType, id: Int, random: Random): BoardCard {
        val price = CORRUPT_BUSINESS_PRICES.pick(random)
        val oneTime = random.nextInt(100) < CORRUPT_ONE_TIME_PERCENT
        val profit = if (oneTime) 0 else price * CORRUPT_BUSINESS_RATES.pick(random) / 100
        val oneTimeProfit = if (oneTime) price * CORRUPT_ONE_TIME_RATES.pick(random) / 100 else 0
        return BoardCard.Chance.CorruptBusiness(
            description = text(type, id, price, profit + oneTimeProfit, CardKind.CORRUPT),
            price = price,
            profit = profit,
            oneTimeProfit = oneTimeProfit,
            deputies = CORRUPT_BUSINESS_DEPUTIES.pick(random),
        )
    }

    private fun eventStore(type: BoardCardType, id: Int, random: Random): BoardCard {
        val price = EVENT_PRICES.pick(random)
        return when (EVENT_KINDS.pick(random)) {
            EventKind.LAND -> BoardCard.EventStore.Land(text(type, id, price, 0), price)
            EventKind.ESTATE -> BoardCard.EventStore.Estate(text(type, id, price, 0), price)
            EventKind.SHARES -> BoardCard.EventStore.Shares(
                sharesType = SharesTypes.pick(random),
                description = text(type, id, price, 0),
                price = SHARE_PRICES.pick(random),
            )

            EventKind.BUSINESS_EXTENDING -> BoardCard.EventStore.BusinessExtending(
                description = text(type, id, price, price),
                profit = EXTENDING_PROFITS.pick(random),
            )

            EventKind.REELECTION ->
                BoardCard.EventStore.Reelection(text(type, id, 0, 0, CardKind.REELECTION))

            EventKind.ANNOUNCEMENT ->
                BoardCard.EventStore.Announcement(text(type, id, 0, 0, CardKind.ANNOUNCEMENT))
        }
    }

    private fun deputy(type: BoardCardType, id: Int, random: Random): BoardCard {
        val corrupt = random.nextInt(100) < CORRUPT_PERCENT
        return BoardCard.Deputy(
            description = if (corrupt) "" else text(type, id, 0, 0),
            corrupt = corrupt,
        )
    }

    private fun <T> List<T>.pick(random: Random): T = this[random.nextInt(size)]

}

private const val CORRUPT_PERCENT = 49
private const val PROFESSIONS_PER_GENDER = 30

private val SALARIES = listOf(300L, 350, 400, 450, 500, 600, 700, 800, 1000, 1200, 1500, 2000, 2500)
private val RENT_SHARES = listOf(20L, 25, 30, 35)
private val FOOD_SHARES = listOf(15L, 20, 25, 30)
private val CLOTH_SHARES = listOf(3L, 5, 8, 10)
private val TRANSPORT_SHARES = listOf(3L, 5, 6, 8)
private val PHONE_SHARES = listOf(1L, 2, 3, 4)

private val SMALL_PRICES = listOf(200L, 300, 500, 600, 800, 1000, 1200, 1500, 2000, 3000, 4000, 8000)
private val SMALL_RATES = listOf(12L, 15, 20, 22, 25, 27, 30, 33, 38, 40, 45, 47, 50, 60, 83, 100)
private val MEDIUM_PRICES = listOf(50000L, 60000, 75000, 80000, 100000, 120000, 150000, 200000, 225000, 250000, 300000, 350000, 400000, 450000, 500000, 600000, 700000, 950000)
private val MEDIUM_RATES = listOf(3L, 4, 5, 6, 7, 8, 10)
private val BIG_PRICES = listOf(700000L, 800000, 900000, 1000000, 1200000, 1300000, 1400000, 1500000, 1600000, 1700000, 1800000, 2000000, 2200000, 2300000, 2400000, 2500000, 2600000, 2700000, 2800000, 3000000, 3500000, 4000000, 5000000, 6000000, 8000000)
private val BIG_RATES = listOf(6L, 7, 8, 9, 10)
private val SHOPPING_PRICES = listOf(500L, 1_000, 2_000, 5_000, 10_000, 20_000, 50_000, 100_000)
private val EXPENSE_PRICES = listOf(50L, 100, 150, 200, 300, 500, 800, 1_000, 2_000)
private val CHANCE_PRICES = listOf(1_000L, 5_000, 10_000, 20_000, 45_000, 60_000)
private val EVENT_PRICES = listOf(20_000L, 30_000, 40_000, 50_000, 60_000, 75_000)
private val SHARE_PRICES = listOf(5L, 10, 20, 30, 40, 50, 70, 75, 80, 90, 300, 400, 500)
private val SHARE_COUNTS = listOf(700L, 1000, 1500, 1800, 2000, 3000, 4000)
private val EXTENDING_PROFITS = listOf(100L, 200, 300, 500, 1000, 2000, 5000)
private val CORRUPT_BUSINESS_PRICES = listOf(200000L, 250000, 300000, 500000, 600000, 800000, 1000000)
private val CORRUPT_BUSINESS_RATES = listOf(40L, 50, 60, 65, 70)
private val CORRUPT_ONE_TIME_RATES = listOf(500L, 750, 800, 1000)
private val CORRUPT_BUSINESS_DEPUTIES = listOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 3)
private val CORRUPT_LAND_PRICES = listOf(400000L, 800000, 1000000, 1500000)
private val CORRUPT_LAND_AREAS = listOf(200L, 300, 400, 500, 600)
private val CORRUPT_LAND_DEPUTIES = listOf(2, 2, 3, 3, 4)
private const val CORRUPT_ONE_TIME_PERCENT = 30

private enum class ChanceKind { RANDOM_JOB, ESTATE, LAND, SHARES, CORRUPT_BUSINESS, CORRUPT_LAND }

private val CHANCE_KINDS =
    List(30) { ChanceKind.RANDOM_JOB } +
            List(25) { ChanceKind.ESTATE } +
            List(30) { ChanceKind.LAND } +
            List(35) { ChanceKind.SHARES } +
            List(13) { ChanceKind.CORRUPT_BUSINESS } +
            List(5) { ChanceKind.CORRUPT_LAND }

private enum class EventKind { LAND, ESTATE, SHARES, BUSINESS_EXTENDING, REELECTION, ANNOUNCEMENT }

private val EVENT_KINDS =
    List(26) { EventKind.LAND } +
            List(15) { EventKind.ESTATE } +
            List(45) { EventKind.SHARES } +
            List(20) { EventKind.BUSINESS_EXTENDING } +
            List(2) { EventKind.REELECTION } +
            List(4) { EventKind.ANNOUNCEMENT }
private val LAND_AREAS = listOf(6L, 12, 20, 30, 50, 100)
private val ShopTypes = ua.vald_zx.game.rat.race.card.shared.ShopType.entries
private val PayerTypes = PayerType.entries
private val SharesTypes = SharesType.entries
