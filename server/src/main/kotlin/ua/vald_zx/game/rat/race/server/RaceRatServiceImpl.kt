package ua.vald_zx.game.rat.race.server

import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ua.vald_zx.game.rat.race.card.shared.RaceRatService
import kotlin.coroutines.CoroutineContext

internal val LOGGER = KtorSimpleLogger("RaceRatServiceImpl")
private val listFlow = MutableStateFlow(listOf<String>())


class RaceRatServiceImpl(override val coroutineContext: CoroutineContext) : RaceRatService {

    init {
        listFlow.onEach { list ->
            LOGGER.debug("Current users")
            list.forEach {
                LOGGER.debug(it)
            }
        }.launchIn(this)
    }

    override suspend fun init(name: String) : String {
        listFlow.emit(listFlow.value + name)
        LOGGER.debug("Added user $name")
        return "Hello $name"
    }

    override suspend fun getListOfUsers(): Flow<List<String>> = listFlow
}