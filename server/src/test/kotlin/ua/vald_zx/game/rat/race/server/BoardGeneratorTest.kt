package ua.vald_zx.game.rat.race.server

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
        val first = BoardGenerator(world()).generate(decks)
        val second = BoardGenerator(world()).generate(decks)
        assertEquals(first, second, "колода не відтворюється з того самого seed")
    }

    @Test
    fun anotherSeedGivesAnotherDeck() {
        val first = BoardGenerator(world(seed = 1)).generate(decks)
        val second = BoardGenerator(world(seed = 2)).generate(decks)
        assertTrue(first != second, "seed ні на що не впливає")
    }

    @Test
    fun switchingGenerationOffLeavesTheStaticDecks() {
        val off = BoardGenerator(world().copy(enabled = false)).generate(decks)
        assertTrue(off.isEmpty(), "вимкнена генерація все одно підмінила колоду")
    }

    @Test
    fun everyRequestedCardIsBuilt() {
        val generated = BoardGenerator(world()).generate(decks)
        decks.forEach { (type, size) ->
            assertEquals(size, generated.getValue(type).size, "колода $type неповна")
            assertEquals((1..size).toSet(), generated.getValue(type).keys)
        }
    }

    @Test
    fun theWorldLeaksIntoTheText() {
        val generated = BoardGenerator(world()).generate(decks)
        val texts = generated.getValue(BoardCardType.Shopping).values
            .filterIsInstance<BoardCard.Shopping>()
            .map { it.description }
        assertTrue(texts.any { "Марсі" in it }, "місцевість не потрапила в тексти")
        assertTrue(texts.any { "2140 рік" in it }, "епоха не потрапила в тексти")
        assertTrue(texts.any { "космічна колонія" in it }, "тематика не потрапила в тексти")
    }

    @Test
    fun businessesStayInsideTheBandsFromTheSpec() {
        val generated = BoardGenerator(world()).generate(decks)
        val bands = mapOf(
            BoardCardType.SmallBusiness to (200L..8_000L to 12..100),
            BoardCardType.MediumBusiness to (50_000L..950_000L to 3..10),
            BoardCardType.BigBusiness to (700_000L..8_000_000L to 6..10),
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
        val chance = BoardGenerator(world()).generate(bigDecks).getValue(BoardCardType.Chance).values
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
        val events = BoardGenerator(world()).generate(BoardCardType.entries.associateWith { 200 })
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
        val deputies = BoardGenerator(world()).generate(BoardCardType.entries.associateWith { 200 })
            .getValue(BoardCardType.Deputy)
            .values
            .filterIsInstance<BoardCard.Deputy>()
        val corrupt = deputies.count { it.corrupt }
        assertTrue(corrupt in 70..130, "продажних депутатів $corrupt з ${deputies.size} — шанс поїхав")
    }
}
