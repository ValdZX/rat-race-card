package ua.vald_zx.game.rat.race.card.shared

import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc

@Rpc
interface RaceRatService : RemoteService {
    suspend fun init(name: String): String
    suspend fun getListOfUsers(): Flow<List<String>>
}