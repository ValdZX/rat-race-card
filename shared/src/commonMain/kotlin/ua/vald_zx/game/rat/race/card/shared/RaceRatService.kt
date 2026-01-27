package ua.vald_zx.game.rat.race.card.shared

import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.Serializable

@Serializable
sealed class GlobalEvent {

    @Serializable
    data class SendMoney(val playerId: String, val receiverId: String, val amount: Long) : GlobalEvent()

    @Serializable
    data class PlayerChanged(val player: Player) : GlobalEvent()

    @Serializable
    data class PlayerHadBaby(val playerId: String, val babies: Long) : GlobalEvent()

    @Serializable
    data class PlayerMarried(val playerId: String) : GlobalEvent()

    @Serializable
    data class PlayerDivorced(val playerId: String) : GlobalEvent()

    @Serializable
    data class BidSelled(val bid: Bid, val auction: Auction) : GlobalEvent()
}

@Serializable
sealed class Event {
    @Serializable
    data class MoneyIncome(val playerId: String, val amount: Long) : Event()

    @Serializable
    data class PlayerHadBaby(val playerId: String, val babies: Long) : Event()

    @Serializable
    data class PlayerMarried(val playerId: String) : Event()

    @Serializable
    data class PlayerDivorced(val playerId: String) : Event()

    @Serializable
    data class PlayerChanged(val player: Player) : Event()

    @Serializable
    data class BoardChanged(val board: Board) : Event()

    @Serializable
    data class LoanAdded(val balance: Long) : Event()

    @Serializable
    data class DepositWithdraw(val balance: Long) : Event()

    @Serializable
    data class AddCash(val amount: Long) : Event()

    @Serializable
    data class SubCash(val amount: Long) : Event()

    @Serializable
    data class ConfirmDismissal(val business: Business) : Event()

    @Serializable
    data class ConfirmSellingAllBusiness(val business: Business) : Event()

    @Serializable
    data class BankruptBusiness(val business: Business) : Event()

    @Serializable
    data class Resignation(val business: Business) : Event()

    @Serializable
    data object LoanOverlimited : Event()

    @Serializable
    data object BidBusinessAuctionSuccessBuy : Event()

    @Serializable
    data object BidEstateAuctionSuccessBuy : Event()

    @Serializable
    data object BidLandAuctionSuccessBuy : Event()

    @Serializable
    data object BidSharesAuctionSuccessBuy : Event()

    @Serializable
    data object CheckState : Event()
}

@Serializable
data class Instance(val playerId: String, val board: Board?, val player: Player?)

@Rpc
interface RaceRatService {
    suspend fun hello(helloUuid: String = ""): Instance

    suspend fun ping()
    suspend fun connectionIsValid()
    suspend fun getBoards(): List<BoardId>
    fun observeBoards(): Flow<List<BoardId>>
    suspend fun createBoard(
        name: String,
        loanLimit: Long,
        businessLimit: Long,
        decks: Map<BoardCardType, Int>
    ): Board

    suspend fun selectBoard(id: String): Board
    suspend fun updateAttributes(attrs: PlayerAttributes)
    suspend fun getPlayer(): Player
    suspend fun makePlayer(
        color: Long,
        card: PlayerCard,
    ): Player

    fun eventsObserve(): Flow<Event>
    suspend fun getPlayers(): List<Player>
    suspend fun getBoard(): Board

    suspend fun sendMoney(receiverId: String, amount: Long)

    suspend fun rollDice()
    suspend fun next()
    suspend fun takeCard(cardType: BoardCardType)
    suspend fun takeSalary()
    suspend fun buyBusiness(business: Business, needGoNextPlayer: Boolean = true)

    suspend fun dismissalConfirmed(business: Business)
    suspend fun sellingAllBusinessConfirmed(business: Business)
    suspend fun minusCash(price: Long)
    suspend fun buyThing(card: BoardCard.Shopping)
    suspend fun changePosition(position: Int)
    suspend fun buyEstate(estate: Estate, needGoNextPlayer: Boolean = true)
    suspend fun buyLand(land: Land, needGoNextPlayer: Boolean = true)
    suspend fun randomJob(card: BoardCard.Chance.RandomJob)
    suspend fun buyShares(shares: Shares, needGoNextPlayer: Boolean = true)
    suspend fun selectCardByNo(cardId: Int)
    suspend fun extendBusiness(business: Business, card: BoardCard.EventStore.BusinessExtending)
    suspend fun sellLands(area: Long, priceOfUnit: Long)
    suspend fun sellShares(card: BoardCard.EventStore.Shares, count: Long)
    suspend fun sellEstate(card: List<Estate>, price: Long)
    suspend fun passLand()
    suspend fun passShares(sharesType: SharesType)
    suspend fun passEstate()
    suspend fun toDeposit(amount: Long)
    suspend fun repayLoan(amount: Long)
    suspend fun advertiseAuction(auction: Auction)
    suspend fun sellBid(bid: Bid)
    suspend fun makeBid(price: Long, count: Long)
}