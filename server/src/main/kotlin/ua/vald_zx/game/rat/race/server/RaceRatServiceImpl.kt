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

    override suspend fun init(player: Player, uuid: String): String {
        if(this.uuid != uuid) {
            players.value = players.value.filter { it.uuid != uuid }
        }
        players.value += player.copy(uuid = this.uuid)
        LOGGER.debug("Added user ${player.professionCard.profession}")
        return this.uuid
    }

    override suspend fun update(state: Card2State) {
        val player = players.value.find { it.uuid == uuid } ?: return
        players.value = players.value.replaceItem(player, player.copy(state = state))
    }

    override suspend fun playersObserve(): Flow<List<Player>> = players

    override suspend fun sendMoney(id: String, cash: Long) {
        cashSendBus.emit(id to cash)
    }

    override suspend fun closeSession() {
        players.value = players.value.filter { it.uuid != uuid }
    }

    override suspend fun inputCashObserve(): Flow<Long> = inputCashFlow
}

fun <T> List<T>.replaceItem(item: T, newItem: T): List<T> {
    val list = toMutableList()
    val index = list.indexOf(item)
    if (index >= 0) {
        list.remove(item)
        list.add(index, newItem)
    }
    return list
}