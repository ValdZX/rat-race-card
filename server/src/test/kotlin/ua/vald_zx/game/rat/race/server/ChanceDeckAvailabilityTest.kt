package ua.vald_zx.game.rat.race.server

import kotlinx.datetime.LocalDateTime
import ua.vald_zx.game.rat.race.card.shared.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ChanceDeckAvailabilityTest {
    private val cards = mapOf(
        1 to BoardCard.Chance.CorruptBusiness(
            description = "Corrupt business",
            price = 10_000,
            profit = 1_000,
            oneTimeProfit = 0,
            deputies = 1,
        ),
        2 to BoardCard.Chance.CorruptLand(
            description = "Corrupt land",
            price = 10_000,
            area = 10,
            deputies = 1,
        ),
        3 to BoardCard.Chance.RandomJob(
            description = "Side job",
            profit = 1_000,
        ),
    )

    @Test
    fun innerCircleExcludesCorruptChanceCards() {
        val board = board(drawPile = listOf(1, 2, 3))

        assertEquals(listOf(3), board.availableCardIds(BoardCardType.Chance, BoardLayer.INNER))
    }

    @Test
    fun outerCircleKeepsEveryChanceCardAvailable() {
        val board = board(drawPile = listOf(1, 2, 3))

        assertEquals(listOf(1, 2, 3), board.availableCardIds(BoardCardType.Chance, BoardLayer.OUTER))
    }

    @Test
    fun innerCircleAlsoFiltersCorruptionFromLegacyDecks() {
        val board = board(drawPile = listOf(120, 121, 138), generated = emptyMap())

        assertEquals(listOf(120), board.availableCardIds(BoardCardType.Chance, BoardLayer.INNER))
    }

    @Test
    fun innerCircleRecyclesNonCorruptCardsWithoutRecyclingCorruptCards() {
        val board = board(drawPile = listOf(1, 2), discardPile = listOf(3))

        val prepared = board.prepareCardDeck(BoardCardType.Chance, BoardLayer.INNER)

        assertEquals(listOf(1, 2, 3), prepared.cards[BoardCardType.Chance])
        assertEquals(emptyList(), prepared.discard[BoardCardType.Chance])
        assertEquals(listOf(3), prepared.availableCardIds(BoardCardType.Chance, BoardLayer.INNER))
    }

    @Test
    fun innerCircleExcludesCorruptionFromGeneratedMarketEvents() {
        val eventCards = mapOf(
            1 to BoardCard.EventStore.CorruptBusiness("Corrupt business sale", 300),
            2 to BoardCard.EventStore.CorruptLand("Corrupt land sale", 10_000),
            3 to BoardCard.EventStore.Announcement("Market news"),
        )
        val board = board(
            drawPile = listOf(1, 2, 3),
            generated = eventCards,
            cardType = BoardCardType.EventStore,
        )

        assertEquals(listOf(3), board.availableCardIds(BoardCardType.EventStore, BoardLayer.INNER))
        assertEquals(listOf(1, 2, 3), board.availableCardIds(BoardCardType.EventStore, BoardLayer.OUTER))
    }

    @Test
    fun innerCircleExcludesCorruptionFromLegacyMarketEvents() {
        val board = board(
            drawPile = listOf(110, 111, 124),
            generated = emptyMap(),
            cardType = BoardCardType.EventStore,
        )

        assertEquals(listOf(110), board.availableCardIds(BoardCardType.EventStore, BoardLayer.INNER))
    }

    private fun board(
        drawPile: List<Int>,
        discardPile: List<Int> = emptyList(),
        generated: Map<Int, BoardCard> = cards,
        cardType: BoardCardType = BoardCardType.Chance,
    ) = Board(
        id = "board",
        name = "Board",
        loanLimit = 0,
        businessLimit = 0,
        createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
        cards = mapOf(cardType to drawPile),
        discard = mapOf(cardType to discardPile),
        generatedCards = generated.takeIf { it.isNotEmpty() }
            ?.let { mapOf(cardType to it) }
            .orEmpty(),
    )
}
