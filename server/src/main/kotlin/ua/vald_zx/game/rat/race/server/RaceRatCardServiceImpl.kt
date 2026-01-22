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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

var offlinePlayers: Map<String, Map<String, OfflinePlayer>> = emptyMap()
val playerFlow = MutableSharedFlow<OfflinePlayer>()
val sendMoneyFlow = MutableSharedFlow<SendMoneyPack>()

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
        offlinePlayers = offlinePlayers.toMutableMap().apply {
            this[player.room] = this.getOrPut(player.room, { emptyMap() }).toMutableMap().apply {
                uuid = player.id.ifEmpty { Uuid.random().toString() }
                val updatedPlayer = player.copy(id = uuid)
                this[uuid] = updatedPlayer
                playerFlow.emit(updatedPlayer)
            }
        }
        return uuid
    }

    override suspend fun getPlayers(): List<OfflinePlayer> {
        return offlinePlayers[room]?.values?.toList().orEmpty()
    }

    override fun playersObserve(): Flow<OfflinePlayer> = localPlayerFlow
    override fun sendMoneyObserve(): Flow<SendMoneyPack> = localSendMoneyFlow

    override suspend fun updatePlayer(player: OfflinePlayer) {
        playerFlow.emit(player)
    }

    override suspend fun sendMoney(pack: SendMoneyPack) {
        sendMoneyFlow.emit(pack)
    }
}