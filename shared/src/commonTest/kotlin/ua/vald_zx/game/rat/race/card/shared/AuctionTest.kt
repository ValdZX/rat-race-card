package ua.vald_zx.game.rat.race.card.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class AuctionTest {
    private val auction = Auction.EstateAuction(
        estate = Estate(name = "Apartment", price = 10_000),
        firstBid = 8_000,
    )

    @Test
    fun startingBidIsMinimumWithoutBids() {
        assertEquals(8_000L, auction.minimumBid(emptyList()))
    }

    @Test
    fun startingBidRemainsMinimumWhenStoredBidsAreLower() {
        val bids = listOf(Bid(playerId = "player", bid = 5_000, count = 0))

        assertEquals(8_000L, auction.minimumBid(bids))
    }

    @Test
    fun highestBidBecomesMinimumWhenItExceedsStartingBid() {
        val bids = listOf(
            Bid(playerId = "first", bid = 9_000, count = 0),
            Bid(playerId = "second", bid = 12_000, count = 0),
        )

        assertEquals(12_000L, auction.minimumBid(bids))
    }
}
