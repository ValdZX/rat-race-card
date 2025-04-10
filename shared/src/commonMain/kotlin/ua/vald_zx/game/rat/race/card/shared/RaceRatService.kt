package ua.vald_zx.game.rat.race.card.shared

import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val professionCard: ProfessionCard,
    val state: Card2State,
    val uuid: String = "",
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
data class Card2State(
    val totalExpenses: Long,
    val cashFlow: Long,
)

@Rpc
interface RaceRatService : RemoteService {
    suspend fun init(player: Player): String
    suspend fun update(state: Card2State)
    suspend fun playersObserve(): Flow<List<Player>>
    suspend fun sendMoney(id: String, cash: Long)
    suspend fun inputCashObserve(): Flow<Long>
}