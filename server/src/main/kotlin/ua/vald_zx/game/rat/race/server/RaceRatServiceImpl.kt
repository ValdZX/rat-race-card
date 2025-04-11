package ua.vald_zx.game.rat.race.server

import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.flow.Flow
import ua.vald_zx.game.rat.race.card.shared.Card2State
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.RaceRatService
import kotlin.coroutines.CoroutineContext

internal val LOGGER = KtorSimpleLogger("RaceRatServiceImpl")


class RaceRatServiceImpl(
    private val uuid: String,
    override val coroutineContext: CoroutineContext
) : RaceRatService {

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
}