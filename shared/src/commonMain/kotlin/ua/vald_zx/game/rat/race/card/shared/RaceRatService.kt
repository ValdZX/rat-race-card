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

    @Serializable
    data class RollDice(val playerId: String, val dice: Int) : GlobalEvent()
}

@Serializable
sealed class Event {
    @Serializable
    data class MoneyIncome(val playerId: String, val amount: Long) : Event()

    @Serializable
    data class PlayerChanged(val player: Player) : Event()

    @Serializable
    data class RollDice(val dice: Int) : Event()

    @Serializable
    data class BoardChanged(val board: Board) : Event()
}

@Serializable
data class Instance(val id: String, val boardId: String)

@Rpc
interface RaceRatService {
    suspend fun hello(id: String = ""): Instance
    suspend fun getBoards(): List<BoardId>
    fun observeBoards(): Flow<List<BoardId>>
    suspend fun createBoard(name: String): Board
    suspend fun selectBoard(boardId: String): Board?
    suspend fun makePlayerOnBoard()
    suspend fun updatePlayerCard(playerCard: PlayerCard)
    suspend fun updateState(state: PlayerState)
    suspend fun updateAttributes(attrs: PlayerAttributes)
    fun eventsObserve(): Flow<Event>
    suspend fun getPlayer(id: String): Player?
    suspend fun getPlayers(ids: Set<String>): List<Player>
    suspend fun sendMoney(receiverId: String, amount: Long)

    suspend fun rollDice(): Int

    suspend fun changePosition(position: Int, level: Int)

    suspend fun takeCard(cardType: BoardCardType)
    suspend fun discardPile()

    suspend fun nextPlayer()

    suspend fun closeSession()
}