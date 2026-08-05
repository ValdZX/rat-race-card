package ua.vald_zx.game.rat.race.server

import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import ua.vald_zx.game.rat.race.card.shared.PayerType
import ua.vald_zx.game.rat.race.card.shared.ShopType
import ua.vald_zx.game.rat.race.card.shared.dreamSlotIds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeneratedEconomyTest {

    private val balance = testBalance()

    private fun world(seed: Long = 42) = BoardGeneration(
        enabled = true,
        theme = "космічна колонія",
        locality = "Марсі",
        epoch = "2140 рік",
        seed = seed,
    )

    private fun deck(type: BoardCardType, size: Int = 200) =
        BoardGenerator(world(), balance).generate(mapOf(type to size)).getValue(type).values

    @Test
    fun landIsAlwaysPricedByItsArea() {
        val lands = deck(BoardCardType.Chance).filterIsInstance<BoardCard.Chance.Land>()

        assertTrue(lands.isNotEmpty(), "у колоді випадків немає землі")
        lands.forEach { land ->
            assertEquals(0, land.price % land.area, "ціна ${land.price} не ділиться на площу ${land.area}")
            assertTrue(
                land.price / land.area in balance.landPricePerUnit,
                "ціна одиниці ${land.price / land.area} не зі списку landPricePerUnit",
            )
        }
    }

    @Test
    fun sellingLandCannotBeatBuyingItByMoreThanTheMarketBand() {
        val marketPrices = deck(BoardCardType.EventStore)
            .filterIsInstance<BoardCard.EventStore.Land>()
            .map { it.price }
        val cheapestPurchase = balance.landPricePerUnit.min()
        val dearestSale = balance.landPricePerUnit.max() * balance.eventLandPricePercentages.max() / 100

        assertTrue(marketPrices.isNotEmpty(), "у колоді подій немає землі")
        marketPrices.forEach { price ->
            assertTrue(price <= dearestSale, "ринок купує одиницю за $price, дорожче за стелю $dearestSale")
        }
        assertTrue(
            dearestSale < cheapestPurchase * ARBITRAGE_LIMIT,
            "найкращий продаж $dearestSale надто відірваний від найдешевшої купівлі $cheapestPurchase",
        )
    }

    @Test
    fun sellingEstateCannotBeatBuyingItByMoreThanTheMarketBand() {
        val purchases = deck(BoardCardType.Chance)
            .filterIsInstance<BoardCard.Chance.Estate>()
            .map { it.price }
        val sales = deck(BoardCardType.EventStore)
            .filterIsInstance<BoardCard.EventStore.Estate>()
            .map { it.price }

        assertTrue(purchases.isNotEmpty(), "у колоді випадків немає нерухомості")
        assertTrue(sales.isNotEmpty(), "у колоді подій немає нерухомості")
        purchases.forEach { price ->
            assertTrue(price in balance.estatePrices, "об'єкт за $price куплений не за згенерованою ціною")
        }
        val dearestSale = balance.estatePrices.max() * balance.estateSalePercentages.max() / 100
        sales.forEach { price ->
            assertTrue(price <= dearestSale, "ринок викуповує об'єкт за $price, дорожче за стелю $dearestSale")
        }
        assertTrue(
            dearestSale < purchases.min() * ARBITRAGE_LIMIT,
            "найкращий продаж $dearestSale надто відірваний від найдешевшої купівлі ${purchases.min()}",
        )
    }

    @Test
    fun aRandomJobPaysLessThanAnEstateCosts() {
        val jobs = deck(BoardCardType.Chance)
            .filterIsInstance<BoardCard.Chance.RandomJob>()
            .map { it.profit }

        assertTrue(jobs.isNotEmpty(), "у колоді випадків немає підробітків")
        jobs.forEach { profit ->
            assertTrue(profit in balance.randomJobProfits, "підробіток $profit не з пулу доходів")
        }
        assertTrue(
            jobs.max() < balance.estatePrices.min(),
            "разовий підробіток ${jobs.max()} перекриває найдешевшу нерухомість ${balance.estatePrices.min()}",
        )
    }

    @Test
    fun corruptLandIsPricedByItsAreaToo() {
        val lands = deck(BoardCardType.Chance).filterIsInstance<BoardCard.Chance.CorruptLand>()

        assertTrue(lands.isNotEmpty(), "у колоді випадків немає корупційної землі")
        lands.forEach { land ->
            assertTrue(
                land.price / land.area in balance.corruptLandPricePerUnit,
                "корупційна ціна одиниці ${land.price / land.area} не зі списку",
            )
        }
    }

    @Test
    fun everyPurchaseCostsWhatItsKindCosts() {
        val purchases = deck(BoardCardType.Shopping).filterIsInstance<BoardCard.Shopping>()

        assertTrue(purchases.isNotEmpty())
        purchases.forEach { purchase ->
            assertTrue(
                purchase.price in balance.shoppingPrices.getValue(purchase.shopType),
                "${purchase.shopType} за ${purchase.price} — ціна не з діапазону свого типу",
            )
        }
    }

    @Test
    fun aPlaneNeverCostsLessThanACar() {
        val purchases = deck(BoardCardType.Shopping).filterIsInstance<BoardCard.Shopping>()
        val dearestCar = purchases.filter { it.shopType == ShopType.AUTO }.maxOf { it.price }
        val cheapestPlane = purchases.filter { it.shopType == ShopType.FLY }.minOf { it.price }

        assertTrue(cheapestPlane > dearestCar, "літак за $cheapestPlane дешевший за авто за $dearestCar")
    }

    @Test
    fun everyKindOfPurchaseAppearsInALargeDeck() {
        val kinds = deck(BoardCardType.Shopping)
            .filterIsInstance<BoardCard.Shopping>()
            .map { it.shopType }
            .toSet()

        assertEquals(ShopType.entries.toSet(), kinds, "у великій колоді покупок не всі типи активів")
    }

    @Test
    fun aRescuedAnimalIsPaidForByWhoeverDrewIt() {
        val expenses = deck(BoardCardType.Expenses).filterIsInstance<BoardCard.Expenses>()
        val rescues = expenses.filter { it.grantsAnimal }

        assertTrue(rescues.isNotEmpty(), "у колоді витрат немає жодної врятованої тварини")
        assertTrue(rescues.size < expenses.size, "уся колода витрат складається з тварин")
        rescues.forEach { rescue ->
            assertEquals(PayerType.ALL, rescue.payer, "бродячу тварину не може забрати лише частина гравців")
        }
    }

    @Test
    fun dreamsFillTheGeneratedPriceRange() {
        val dreams = BoardGenerator(world(), balance).generateDreams()

        assertEquals(dreamSlotIds, dreams.map { it.id }, "мрії згенеровані не для тих слотів")
        assertEquals(balance.dreamMinPrice, dreams.first().price)
        assertTrue(dreams.last().price <= balance.dreamMaxPrice)
        assertTrue(
            dreams.zipWithNext().all { (cheaper, dearer) -> cheaper.price < dearer.price },
            "ціни мрій не зростають",
        )
    }

    @Test
    fun theCheapestDreamStaysWithinTheVictoryBalance() {
        val dreams = BoardGenerator(world(), balance).generateDreams()

        assertTrue(
            dreams.first().price <= balance.victoryMinimumAccountBalance,
            "найдешевшу мрію неможливо купити, а вона є умовою перемоги",
        )
    }

    private companion object {
        const val ARBITRAGE_LIMIT = 3
    }
}
