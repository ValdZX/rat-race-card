package ua.vald_zx.game.rat.race.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ua.vald_zx.game.rat.race.card.shared.OfflinePlayer
import ua.vald_zx.game.rat.race.card.shared.RaceRatCardService
import ua.vald_zx.game.rat.race.card.shared.SendMoneyPack
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val rooms = ConcurrentHashMap<String, ConcurrentHashMap<String, OfflinePlayer>>()
private val playerFlow = MutableSharedFlow<OfflinePlayer>()
private val sendMoneyFlow = MutableSharedFlow<SendMoneyPack>()

private fun roomPlayers(room: String) = rooms.getOrPut(room) { ConcurrentHashMap() }

class RaceRatCardServiceImpl : RaceRatCardService, CoroutineScope by CoroutineScope(Dispatchers.Default) {
    private var uuid: String = ""
    private var room = ""
    private val localPlayerFlow = MutableSharedFlow<OfflinePlayer>()
    private val localSendMoneyFlow = MutableSharedFlow<SendMoneyPack>()

    init {
        sendMoneyFlow.onEach {
            if (it.receiverId == uuid) {
                localSendMoneyFlow.emit(it)
            }
        }.launchIn(this)
        playerFlow.onEach {
            if (it.room == room) {
                localPlayerFlow.emit(it)
            }
        }.launchIn(this)
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun hello(player: OfflinePlayer): String {
        room = player.room
        uuid = player.id.ifEmpty { Uuid.random().toString() }
        val updatedPlayer = player.copy(id = uuid)
        roomPlayers(room)[uuid] = updatedPlayer
        playerFlow.emit(updatedPlayer)
        return uuid
    }

    override suspend fun getPlayers(): List<OfflinePlayer> {
        return rooms[room]?.values?.toList().orEmpty()
    }

    override fun playersObserve(): Flow<OfflinePlayer> = localPlayerFlow
    override fun sendMoneyObserve(): Flow<SendMoneyPack> = localSendMoneyFlow

    override suspend fun updatePlayer(player: OfflinePlayer) {
        if (player.room.isNotEmpty()) {
            if (player.removed) {
                rooms[player.room]?.remove(player.id)
            } else {
                roomPlayers(player.room)[player.id] = player
            }
        }
        playerFlow.emit(player)
    }

    override suspend fun sendMoney(pack: SendMoneyPack) {
        sendMoneyFlow.emit(pack)
    }
}
