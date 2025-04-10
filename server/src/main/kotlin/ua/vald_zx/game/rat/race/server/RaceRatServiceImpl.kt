package ua.vald_zx.game.rat.race.server

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import ua.vald_zx.game.rat.race.card.shared.RaceRatService
import kotlin.coroutines.CoroutineContext

class RaceRatServiceImpl(override val coroutineContext: CoroutineContext) : RaceRatService {
    private val listFlow = MutableStateFlow(listOf<String>())

    override suspend fun init(name: String) {
        listFlow.emit(listFlow.value + name)
    }

    override suspend fun getListOfUsers(): Flow<List<String>> = listFlow
}