package ua.vald_zx.game.rat.race.server

import ua.vald_zx.game.rat.race.server.generation.BoardGenerator

import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BoardGeneratorTest {

    private val decks = BoardCardType.entries.associateWith { 40 }

    private fun world(seed: Long = 42, theme: String = "космічна колонія") = BoardGeneration(
        enabled = true,
        theme = theme,
        locality = "Марсі",
        epoch = "2140 рік",
        seed = seed,
    )

    @Test
    fun theSameSeedRebuildsTheSameDeck() {
        val first = BoardGenerator(world(), testBalance()).generate(decks)
        val second = BoardGenerator(world(), testBalance()).generate(decks)
        assertEquals(first, second, "колода не відтворюється з того самого seed")
    }

    @Test
    fun anotherSeedGivesAnotherDeck() {
        val first = BoardGenerator(world(seed = 1), testBalance()).generate(decks)
        val second = BoardGenerator(world(seed = 2), testBalance()).generate(decks)
        assertTrue(first != second, "seed ні на що не впливає")
    }

    @Test
    fun switchingGenerationOffLeavesTheStaticDecks() {
        val off = BoardGenerator(world().copy(enabled = false), testBalance()).generate(decks)
        assertTrue(off.isEmpty(), "вимкнена генерація все одно підмінила колоду")
    }

    @Test
    fun everyRequestedCardIsBuilt() {
        val generated = BoardGenerator(world(), testBalance()).generate(decks)
        decks.forEach { (type, size) ->
            assertEquals(size, generated.getValue(type).size, "колода $type неповна")
            assertEquals((1..size).toSet(), generated.getValue(type).keys)
        }
    }

    @Test
    fun generatedExpensesHaveVisiblePriceTitles() {
        val expenses = BoardGenerator(world(), testBalance())
            .generate(mapOf(BoardCardType.Expenses to 40))
            .getValue(BoardCardType.Expenses)
            .values
            .filterIsInstance<BoardCard.Expenses>()

        assertTrue(expenses.isNotEmpty())
        expenses.forEach { card ->
            assertTrue(card.priceTitle.isNotBlank())
            assertEquals(card.price.toString(), card.priceTitle.filter(Char::isDigit))
        }
    }

    @Test
    fun shareCardsUseOnlyTheGeneratedMarket() {
        val balance = testBalance()
        val generated = BoardGenerator(world(), balance).generate(decks)
        val shareIds = generated.values.flatMap { it.values }.mapNotNull { card ->
            when (card) {
                is BoardCard.Chance.Shares -> card.sharesType
                is BoardCard.EventStore.Shares -> card.sharesType
                else -> null
            }
        }

        assertTrue(shareIds.isNotEmpty())
        assertTrue(shareIds.all { id -> balance.shares.any { it.id == id } })
    }

    @Test
    fun generatedMarketContainsUnfavorableForcedShareSales() {
        val balance = testBalance()
        val events = BoardGenerator(world(), balance)
            .generate(mapOf(BoardCardType.EventStore to 200))
            .getValue(BoardCardType.EventStore)
            .values
            .filterIsInstance<BoardCard.EventStore.Shares>()
        val forced = events.filter { it.forcedSale }

        assertTrue(forced.isNotEmpty())
        assertTrue(forced.all { it.price in balance.forcedShareSalePrices })
        assertTrue(forced.all { it.price < balance.sharePrices.min() })
        assertTrue(events.filterNot { it.forcedSale }.all { it.price in balance.sharePrices })
    }

    @Test
    fun mechanicsContainNoTemplateText() {
        val generated = BoardGenerator(world(), testBalance()).generate(decks)
        generated.values.flatMap { it.values }.forEach { card ->
            val text = when (card) {
                is BoardCard.SmallBusiness -> card.name + card.description
                is BoardCard.MediumBusiness -> card.name + card.description
                is BoardCard.BigBusiness -> card.name + card.description
                is BoardCard.Shopping -> card.description
                is BoardCard.Expenses -> card.description
                is BoardCard.Deputy -> card.description
                is BoardCard.EventStore.Shares -> card.description
                is BoardCard.EventStore.Land -> card.description
                is BoardCard.EventStore.Estate -> card.description
                is BoardCard.EventStore.BusinessExtending -> card.description
                is BoardCard.EventStore.Reelection -> card.description
                is BoardCard.EventStore.Announcement -> card.description
                is BoardCard.Chance.RandomJob -> card.description
                is BoardCard.Chance.Land -> card.name + card.description
                is BoardCard.Chance.Estate -> card.name + card.description
                is BoardCard.Chance.Shares -> card.description
                is BoardCard.Chance.CorruptBusiness -> card.description
                is BoardCard.Chance.CorruptLand -> card.description
            }
            assertTrue(text.isBlank(), "генератор механіки повернув шаблонний текст")
        }
    }

    @Test
    fun businessesStayInsideTheBandsFromTheSpec() {
        val balance = testBalance()
        val generated = BoardGenerator(world(), balance).generate(decks)
        val bands = mapOf(
            BoardCardType.SmallBusiness to (
                    balance.smallBusinessPrices.min()..balance.smallBusinessPrices.max() to
                            balance.smallBusinessReturnPercentages.min().toInt()..balance.smallBusinessReturnPercentages.max().toInt()
                    ),
            BoardCardType.MediumBusiness to (
                    balance.mediumBusinessPrices.min()..balance.mediumBusinessPrices.max() to
                            balance.mediumBusinessReturnPercentages.min().toInt()..balance.mediumBusinessReturnPercentages.max().toInt()
                    ),
            BoardCardType.BigBusiness to (
                    balance.bigBusinessPrices.min()..balance.bigBusinessPrices.max() to
                            balance.bigBusinessReturnPercentages.min().toInt()..balance.bigBusinessReturnPercentages.max().toInt()
                    ),
        )
        bands.forEach { (type, band) ->
            val (prices, rates) = band
            generated.getValue(type).values.forEach { card ->
                val (price, profit) = when (card) {
                    is BoardCard.SmallBusiness -> card.price to card.profit
                    is BoardCard.MediumBusiness -> card.price to card.profit
                    is BoardCard.BigBusiness -> card.price to card.profit
                    else -> error("не бізнес")
                }
                assertTrue(price in prices, "$type: ціна $price поза діапазоном ТЗ $prices")
                val rate = (profit * 100 / price).toInt()
                assertTrue(rate in rates, "$type: дохідність $rate% поза діапазоном ТЗ $rates")
                assertTrue(profit > 0, "$type за $price не приносить нічого")
            }
        }
    }

    @Test
    fun corruptionSurvivesGeneration() {
        val bigDecks = BoardCardType.entries.associateWith { 200 }
        val chance = BoardGenerator(world(), testBalance()).generate(bigDecks).getValue(BoardCardType.Chance).values
        val corruptBusiness = chance.filterIsInstance<BoardCard.Chance.CorruptBusiness>()
        val corruptLand = chance.filterIsInstance<BoardCard.Chance.CorruptLand>()

        assertTrue(corruptBusiness.isNotEmpty(), "у згенерованій колоді немає корупційного бізнесу")
        assertTrue(corruptLand.isNotEmpty(), "у згенерованій колоді немає корупційної землі")
        corruptBusiness.forEach { card ->
            assertTrue(card.deputies > 0, "корупційний бізнес без депутатів: $card")
            assertTrue(card.profit > 0 || card.oneTimeProfit > 0, "корупційний бізнес без вигоди: $card")
        }
        corruptLand.forEach { card ->
            assertTrue(card.deputies > 0, "корупційна земля без депутатів: $card")
            assertTrue(card.area > 0, "корупційна земля без площі: $card")
        }
    }

    @Test
    fun marketEventsKeepTheirRareCards() {
        val events = BoardGenerator(world(), testBalance()).generate(BoardCardType.entries.associateWith { 200 })
            .getValue(BoardCardType.EventStore)
            .values
        assertTrue(
            events.any { it is BoardCard.EventStore.Reelection },
            "перевибори зникли зі згенерованої колоди подій",
        )
        assertTrue(
            events.any { it is BoardCard.EventStore.BusinessExtending },
            "розширення бізнесу зникло зі згенерованої колоди подій",
        )
        assertTrue(
            events.any { it is BoardCard.EventStore.Announcement },
            "оголошення зникли зі згенерованої колоди подій",
        )
    }

    @Test
    fun deputiesStayCloseToHalfCorrupt() {
        val deputies = BoardGenerator(world(), testBalance()).generate(BoardCardType.entries.associateWith { 200 })
            .getValue(BoardCardType.Deputy)
            .values
            .filterIsInstance<BoardCard.Deputy>()
        val corrupt = deputies.count { it.corrupt }
        assertTrue(corrupt in 70..130, "продажних депутатів $corrupt з ${deputies.size} — шанс поїхав")
    }
}
