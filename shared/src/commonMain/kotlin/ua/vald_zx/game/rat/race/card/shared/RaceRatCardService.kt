package ua.vald_zx.game.rat.race.card.shared

import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
interface RaceRatCardService {
    suspend fun hello(player: OfflinePlayer, clientVersion: String? = null): String
    suspend fun getPlayers(): List<OfflinePlayer>
    fun playersObserve(): Flow<OfflinePlayer>

    suspend fun updatePlayer(player: OfflinePlayer)
    fun sendMoneyObserve(): Flow<SendMoneyPack>
    suspend fun sendMoney(pack: SendMoneyPack)
}
