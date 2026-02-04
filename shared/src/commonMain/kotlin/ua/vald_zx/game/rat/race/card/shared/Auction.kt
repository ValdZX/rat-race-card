package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.Serializable


@Serializable
sealed class Auction {
    @Serializable
    data class BusinessAuction(val business: Business, val firstBid: Long) : Auction()

    @Serializable
    data class LandAuction(val land: Land, val firstBid: Long) : Auction()

    @Serializable
    data class EstateAuction(val estate: Estate, val firstBid: Long) : Auction()

    @Serializable
    data class SharesAuction(val shares: Shares, val firstBid: Long) : Auction()

    val getBid: Long
        get() = when (this) {
            is BusinessAuction -> firstBid
            is EstateAuction -> firstBid
            is LandAuction -> firstBid
            is SharesAuction -> firstBid
        }

    fun copy(bid: Long): Auction {
        return when (this) {
            is BusinessAuction -> copy(firstBid = bid)
            is EstateAuction -> copy(firstBid = bid)
            is LandAuction -> copy(firstBid = bid)
            is SharesAuction -> copy(firstBid = bid)
        }
    }

    val quantity: Long
        get() = when (this) {
            is BusinessAuction -> 0
            is EstateAuction -> 0
            is LandAuction -> 0
            is SharesAuction -> shares.count
        }

    fun getProfit(bid: Bid): Long {
        return when (this) {
            is BusinessAuction -> {
                bid.bid - this.business.price
            }

            is EstateAuction -> {
                bid.bid - this.estate.price
            }

            is LandAuction -> {
                bid.bid - this.land.price
            }

            is SharesAuction -> {
                bid.bid * bid.count - this.shares.buyPrice * bid.count
            }
        }
    }
}

@Serializable
data class Bid(
    val playerId: String,
    val bid: Long,
    val count: Long,
)