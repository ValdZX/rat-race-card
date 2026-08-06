package ua.vald_zx.game.rat.race.server

import kotlinx.serialization.json.Json
import ua.vald_zx.game.rat.race.card.shared.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CardMechanicsCharacterizationTest {
    private val json = Json

    @Test
    fun everyCurrentMechanicalCardVariantSurvivesSerialization() {
        val cards = mechanicalVariants()
        val decoded = cards.map { card ->
            json.decodeFromString(BoardCard.serializer(), json.encodeToString(BoardCard.serializer(), card))
        }

        assertEquals(20, cards.size)
        assertEquals(cards, decoded)
        assertEquals(cards.map { it::class }, decoded.map { it::class })
    }

    @Test
    fun everyShoppingAssetKindRemainsRepresentable() {
        val purchases = ShopType.entries.map { type -> BoardCard.Shopping(type.name, 1, type, "") }

        assertEquals(ShopType.entries, purchases.map { it.shopType })
    }

    @Test
    fun chanceCorruptionIsOuterOnlyWhileRegularChanceCardsRemainInnerCompatible() {
        val board = board(
            type = BoardCardType.Chance,
            cards = mapOf(
                1 to BoardCard.Chance.RandomJob("job", 1),
                2 to BoardCard.Chance.Land("land", "land", 1, 1),
                3 to BoardCard.Chance.Estate("estate", "estate", 1),
                4 to BoardCard.Chance.Shares("shares", 1, 1, "IT"),
                5 to BoardCard.Chance.CorruptBusiness("corrupt", 1, 1, 0, 1),
                6 to BoardCard.Chance.CorruptLand("corrupt", 1, 1, 1),
            ),
        )

        assertEquals(listOf(1, 2, 3, 4), board.availableCardIds(BoardCardType.Chance, BoardLayer.INNER))
        assertEquals((1..6).toList(), board.availableCardIds(BoardCardType.Chance, BoardLayer.OUTER))
    }

    @Test
    fun marketCorruptionAndReelectionAreOuterOnly() {
        val cards = mechanicalVariants().filterIsInstance<BoardCard.EventStore>()
            .mapIndexed { index, card -> index + 1 to card }
            .toMap()
        val board = board(BoardCardType.EventStore, cards)
        val inner = board.availableCardIds(BoardCardType.EventStore, BoardLayer.INNER)

        assertEquals(5, inner.size)
        assertIs<BoardCard.EventStore.Reelection>(cards.getValue(5))
        assertTrue(inner.map(cards::getValue).none { it is BoardCard.EventStore.Reelection })
        assertTrue(inner.map(cards::getValue).none { it is BoardCard.EventStore.CorruptBusiness })
        assertTrue(inner.map(cards::getValue).none { it is BoardCard.EventStore.CorruptLand })
        assertEquals(cards.keys.toList(), board.availableCardIds(BoardCardType.EventStore, BoardLayer.OUTER))
    }

    @Test
    fun auctionProfitAndShareQuantityKeepTheirCurrentMeaning() {
        val shares = Shares("IT", count = 10, buyPrice = 100)
        val auction = Auction.SharesAuction(shares, firstBid = 120)
        val bid = Bid("buyer", bid = 150, count = 4)

        assertEquals(200, auction.getProfit(bid))
        assertEquals(10, auction.quantity)
        assertIs<Auction.SharesAuction>(auction.copy(200))
        assertEquals(200, auction.copy(200).getBid)
    }

    private fun mechanicalVariants(): List<BoardCard> = listOf(
        BoardCard.SmallBusiness("small", "", 1, 1),
        BoardCard.MediumBusiness("medium", "", 1, 1),
        BoardCard.BigBusiness("big", "", 1, 1),
        BoardCard.Shopping("shopping", 1, ShopType.AUTO, ""),
        BoardCard.Expenses("expenses", "", 1, PayerType.ALL),
        BoardCard.Chance.RandomJob("job", 1),
        BoardCard.Chance.Land("land", "", 1, 1),
        BoardCard.Chance.Shares("shares", 1, 1, "IT"),
        BoardCard.Chance.Estate("estate", "", 1),
        BoardCard.Chance.CorruptBusiness("corrupt business", 1, 1, 0, 1),
        BoardCard.Chance.CorruptLand("corrupt land", 1, 1, 1),
        BoardCard.EventStore.Shares("IT", "shares", 1),
        BoardCard.EventStore.Land("land", 1),
        BoardCard.EventStore.Estate("estate", 1),
        BoardCard.EventStore.BusinessExtending("extension", 1),
        BoardCard.EventStore.Reelection("reelection"),
        BoardCard.EventStore.Announcement("announcement"),
        BoardCard.EventStore.CorruptBusiness("corrupt business", 200),
        BoardCard.EventStore.CorruptLand("corrupt land", 1),
        BoardCard.Deputy("deputy", corrupt = true),
    )

    private fun board(type: BoardCardType, cards: Map<Int, BoardCard>) = Board(
        id = "board",
        name = "Board",
        loanLimit = 0,
        businessLimit = 0,
        createDateTime = kotlinx.datetime.LocalDateTime(2026, 1, 1, 0, 0),
        cards = mapOf(type to cards.keys.toList()),
        generatedCards = mapOf(type to cards),
    )
}
