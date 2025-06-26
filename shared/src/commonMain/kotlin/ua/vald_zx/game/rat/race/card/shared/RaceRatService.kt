package ua.vald_zx.game.rat.race.card.shared

import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val id: String,
    val professionCard: ProfessionCard = ProfessionCard(),
    val state: PlayerState = PlayerState(),
    val color: Int = 0,
    val avatar: Int = 0,
)

@Serializable
data class ProfessionCard(
    val profession: String = "",
    val salary: Long = 0,
    val rent: Long = 0,
    val food: Long = 0,
    val cloth: Long = 0,
    val transport: Long = 0,
    val phone: Long = 0,
)

@Serializable
data class PlayerState(
    val position: Int = 0,
    val totalExpenses: Long = 0,
    val cashFlow: Long = 0,
)

@Serializable
sealed class InternalEvent {

    @Serializable
    data class MoneyIncome(val playerId: String, val receiverId: String, val amount: Long) : InternalEvent()
    @Serializable
    data class PlayerChanged(val player: Player) : InternalEvent()
}

@Serializable
sealed class Event {
    @Serializable
    data class MoneyIncome(val playerId: String, val amount: Long) : Event()
    @Serializable
    data class PlayerChanged(val player: Player) : Event()
}

@Rpc
interface RaceRatService : RemoteService {
    suspend fun hello(id: String = ""): String
    suspend fun updatePlayerCard(professionCard: ProfessionCard)
    suspend fun updateState(state: PlayerState)
    fun eventsObserve(): Flow<Event>
    fun playersList(): Flow<Set<String>>
    suspend fun getPlayer(id: String): Player?
    suspend fun sendMoney(receiverId: String, amount: Long)
    suspend fun changePosition(position: Int)
    suspend fun closeSession()
}