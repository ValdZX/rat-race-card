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

var offlinePlayers: Map<String, Map<String, OfflinePlayer>> = emptyMap()
val playerFlow = MutableSharedFlow<OfflinePlayer>()
val sendMoneyFlow = MutableSharedFlow<SendMoneyPack>()

class RaceRatCardServiceImpl(
    private val uuid: String,
) : RaceRatCardService, CoroutineScope by CoroutineScope(Dispatchers.Default) {
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

    override suspend fun hello(player: OfflinePlayer): String {
        room = player.room
        offlinePlayers = offlinePlayers.toMutableMap().apply {
            this[player.room] = this.getOrPut(player.room, { emptyMap() }).toMutableMap().apply {
                if (player.id.isNotEmpty()) {
                    this.remove(player.id)
                    playerFlow.emit(player.copy(removed = true))
                }
                val updatedPlayer = player.copy(id = uuid)
                this[uuid] = updatedPlayer
                playerFlow.emit(player)
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