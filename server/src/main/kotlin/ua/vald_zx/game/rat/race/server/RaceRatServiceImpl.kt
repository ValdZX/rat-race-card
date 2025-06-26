package ua.vald_zx.game.rat.race.server

import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ua.vald_zx.game.rat.race.card.shared.Event
import ua.vald_zx.game.rat.race.card.shared.Event.MoneyIncome
import ua.vald_zx.game.rat.race.card.shared.Event.PlayerChanged
import ua.vald_zx.game.rat.race.card.shared.InternalEvent
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes
import ua.vald_zx.game.rat.race.card.shared.PlayerState
import ua.vald_zx.game.rat.race.card.shared.ProfessionCard
import ua.vald_zx.game.rat.race.card.shared.RaceRatService
import kotlin.coroutines.CoroutineContext

internal val LOGGER = KtorSimpleLogger("RaceRatService")

private val internalEventBus = MutableSharedFlow<InternalEvent>()

class RaceRatServiceImpl(
    private val uuid: String,
    override val coroutineContext: CoroutineContext
) : RaceRatService {
    private val eventBus = MutableSharedFlow<Event>()

    init {
        internalEventBus.onEach { event ->
            when (event) {
                is InternalEvent.MoneyIncome -> {
                    if (event.receiverId == uuid) {
                        eventBus.emit(MoneyIncome(event.playerId, event.amount))
                    }
                }

                is InternalEvent.PlayerChanged -> {
                    if (event.player.id != uuid) {
                        eventBus.emit(PlayerChanged(event.player))
                    }
                }
            }
        }.launchIn(this)
    }

    override suspend fun hello(id: String): String {
        val initialInstance = if (uuid != id && instances.value.contains(id)) {
            val instance = instances.value[id]
            instances.value = instances.value.toMutableMap().apply { remove(id) }
            instance ?: Instance()
        } else Instance()
        instances.value = instances.value.toMutableMap().apply { this[uuid] = initialInstance }
        LOGGER.debug("User $uuid")
        return this.uuid
    }

    override suspend fun updatePlayerCard(professionCard: ProfessionCard) = changeCurrentPlayer {
        copy(professionCard = professionCard)
    }

    override suspend fun updateState(state: PlayerState) = changeCurrentPlayer {
        copy(state = state)
    }

    override suspend fun updateAttributes(attrs: PlayerAttributes) = changeCurrentPlayer{
        copy(attrs = attrs)
    }

    override fun eventsObserve(): Flow<Event> = eventBus

    override fun playersList(): Flow<Set<String>> = instances.map { it.keys }

    override suspend fun getPlayer(id: String): Player? {
        return instances.value[id]?.player
    }

    override suspend fun sendMoney(receiverId: String, amount: Long) {
        internalEventBus.emit(InternalEvent.MoneyIncome(uuid, receiverId, amount))
    }

    override suspend fun changePosition(position: Int) = changeCurrentPlayer {
        copy(state = state.copy(position = position))
    }

    override suspend fun closeSession() {
        instances.value = instances.value.toMutableMap().apply { remove(uuid) }
    }

    private suspend fun changeCurrentPlayer(todo: Player.() -> Player) {
        val instance = instances.value[uuid] ?: return
        val player = instance.player ?: Player(uuid)
        instances.value = instances.value.toMutableMap().apply {
            val updatedPlayer = player.todo()
            internalEventBus.emit(InternalEvent.PlayerChanged(updatedPlayer))
            this[uuid] = instance.copy(player = updatedPlayer)
        }
    }
}