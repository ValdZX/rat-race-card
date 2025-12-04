package ua.vald_zx.game.rat.race.card.shared

import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.Serializable

@Serializable
sealed class GlobalEvent {

    @Serializable
    data class MoneyIncome(val playerId: String, val receiverId: String, val amount: Long) :
        GlobalEvent()

    @Serializable
    data class PlayerChanged(val player: Player) : GlobalEvent()
}

@Serializable
sealed class Event {
    @Serializable
    data class MoneyIncome(val playerId: String, val amount: Long) : Event()

    @Serializable
    data class PlayerChanged(val player: Player) : Event()

    @Serializable
    data class BoardChanged(val board: Board) : Event()

    @Serializable
    data class LoanAdded(val balance: Long) : Event()

    @Serializable
    data class DepositWithdraw(val balance: Long) : Event()

    @Serializable
    data class SubCash(val amount: Long) : Event()

    @Serializable
    data class ConfirmDismissal(val business: Business) : Event()

    @Serializable
    data class ConfirmSellingAllBusiness(val business: Business) : Event()
}

@Serializable
data class Instance(val playerId: String, val board: Board?, val player: Player?)

@Rpc
interface RaceRatService {
    suspend fun hello(id: String = ""): Instance
    suspend fun closeSession()
    suspend fun getBoards(): List<BoardId>
    fun observeBoards(): Flow<List<BoardId>>
    suspend fun createBoard(name: String, decks: Map<BoardCardType, Int>): Board
    suspend fun selectBoard(boardId: String): Board
    suspend fun updateAttributes(attrs: PlayerAttributes)
    suspend fun getPlayer(): Player
    suspend fun makePlayer(
        name: String,
        gender: Gender,
        color: Long,
    ): Player

    fun eventsObserve(): Flow<Event>
    suspend fun getPlayers(): List<Player>
    suspend fun getBoard(): Board

    suspend fun sendMoney(receiverId: String, amount: Long)

    suspend fun rollDice()
    suspend fun move()

    suspend fun discardPile()
    suspend fun takeCard(cardType: BoardCardType)
    suspend fun takeSalary()
    suspend fun buyBusiness(business: Business)

    suspend fun dismissalConfirmed(business: Business)
    suspend fun sellingAllBusinessConfirmed(business: Business)
    suspend fun minusCash(price: Long)
    suspend fun buy(card: BoardCard.Shopping)
}