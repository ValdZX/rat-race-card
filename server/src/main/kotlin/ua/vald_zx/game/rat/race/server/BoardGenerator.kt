package ua.vald_zx.game.rat.race.server

import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import ua.vald_zx.game.rat.race.card.shared.PayerType
import ua.vald_zx.game.rat.race.card.shared.SharesType
import ua.vald_zx.game.rat.race.card.shared.seedFor
import kotlin.random.Random

internal interface CardTextSource {
    fun describe(request: CardTextRequest): String
}

internal data class CardTextRequest(
    val type: BoardCardType,
    val cardId: Int,
    val world: BoardGeneration,
    val price: Long,
    val profit: Long,
)

internal object TemplateTextSource : CardTextSource {
    override fun describe(request: CardTextRequest): String {
        val random = Random(request.world.seedFor(request.type, request.cardId))
        val place = request.world.locality.ifBlank { "місті" }
        val era = request.world.epoch.ifBlank { "наш час" }
        val theme = request.world.theme.ifBlank { "справа" }
        val opener = openers.random(random)
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

    private fun text(type: BoardCardType, id: Int, price: Long, profit: Long) =
        texts.describe(CardTextRequest(type, id, world, price, profit))

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
        return when (random.nextInt(3)) {
            0 -> BoardCard.Chance.RandomJob(text(type, id, price, price), price)
            1 -> BoardCard.Chance.Estate(
                name = world.locality.ifBlank { "Обʼєкт" } + " №" + id,
                description = text(type, id, price, 0),
                price = price,
            )

            else -> BoardCard.Chance.Land(
                name = world.locality.ifBlank { "Ділянка" } + " №" + id,
                description = text(type, id, price, 0),
                price = price,
                area = LAND_AREAS.pick(random),
            )
        }
    }

    private fun eventStore(type: BoardCardType, id: Int, random: Random): BoardCard {
        val price = EVENT_PRICES.pick(random)
        return when (random.nextInt(3)) {
            0 -> BoardCard.EventStore.Land(text(type, id, price, 0), price)
            1 -> BoardCard.EventStore.Estate(text(type, id, price, 0), price)
            else -> BoardCard.EventStore.Shares(
                sharesType = SharesTypes.pick(random),
                description = text(type, id, price, 0),
                price = SHARE_PRICES.pick(random),
            )
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
private val SHARE_PRICES = listOf(5L, 10, 20, 30, 40, 50)
private val LAND_AREAS = listOf(6L, 12, 20, 30, 50, 100)
private val ShopTypes = ua.vald_zx.game.rat.race.card.shared.ShopType.entries
private val PayerTypes = PayerType.entries
private val SharesTypes = SharesType.entries
