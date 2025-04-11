package ua.vald_zx.game.rat.race.server

import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ua.vald_zx.game.rat.race.card.shared.Card2State
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.RaceRatService
import kotlin.coroutines.CoroutineContext

internal val LOGGER = KtorSimpleLogger("RaceRatServiceImpl")

private val cashSendBus = MutableSharedFlow<Pair<String, Long>>()

class RaceRatServiceImpl(
    private val uuid: String,
    override val coroutineContext: CoroutineContext
) : RaceRatService {
    private val inputCashFlow = MutableSharedFlow<Long>()

    init {
        cashSendBus.onEach { (id, cash) ->
            if (id == uuid) {
                inputCashFlow.emit(cash)
            }
        }.launchIn(this)
    }

    override suspend fun init(player: Player): String {
        val mutableMap = players.value.toMutableMap()
        mutableMap[uuid] = player
        players.value = mutableMap
        LOGGER.debug("Added user ${player.professionCard.profession}")
        return "Hello ${player.professionCard.profession}"
    }

    override suspend fun update(state: Card2State) {
        val player = players.value[uuid] ?: return
        val mutableMap = players.value.toMutableMap()
        mutableMap[uuid] = player.copy(state = state)
        players.value = mutableMap
    }

    override suspend fun playersObserve(): Flow<Map<String, Player>> = players

    override suspend fun sendMoney(id: String, cash: Long) {
        cashSendBus.emit(id to cash)
    }

    override suspend fun inputCashObserve(): Flow<Long> = inputCashFlow
}