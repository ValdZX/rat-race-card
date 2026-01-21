@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package ua.vald_zx.game.rat.race.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.rpc.krpc.ktor.server.Krpc
import kotlinx.rpc.krpc.ktor.server.rpc
import kotlinx.rpc.krpc.serialization.json.json
import ua.vald_zx.game.rat.race.card.shared.*
import ua.vald_zx.game.rat.race.server.utils.existInStorage
import ua.vald_zx.game.rat.race.server.utils.removeFromStorage
import ua.vald_zx.game.rat.race.server.utils.savedMutableStateFlow
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun main() {
    embeddedServer(
        Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun DefaultScope(): CoroutineScope = object : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()
    override fun toString(): String = "CoroutineScope(coroutineContext=$coroutineContext)"
}


val boards by lazy { savedMutableStateFlow<List<String>>({ emptyList() }, "boards") }
val players by lazy { savedMutableStateFlow<List<String>>({ emptyList() }, "players") }

val boardMap = mutableMapOf<String, MutableStateFlow<Board>>()
val playerMap = mutableMapOf<String, MutableStateFlow<Player>>()

fun getBoardState(id: String): MutableStateFlow<Board>? {
    return boardMap.getOrPut(id) {
        if (!existInStorage(id)) return null
        savedMutableStateFlow({ error("WTF") }, id)
    }
}

fun getPlayerState(id: String): MutableStateFlow<Player>? {
    return playerMap.getOrPut(id) {
        if (!existInStorage(id)) return null
        savedMutableStateFlow({ error("WTF") }, id)
    }
}

operator fun MutableStateFlow<Map<String, MutableStateFlow<Player>>>.get(key: String): Player {
    return this.value[key]?.value ?: error("No player found for $key")
}

fun Application.module() {
    install(Krpc)
    installCORS()
    routing {
        staticResources("/content", "mycontent")

        get("/") {
            call.respondText("Race rat RPC services")
        }

        get("/error-test") {
            throw IllegalStateException("Too Busy")
        }

        rpc("/api") {
            val uuid = Uuid.random().toString()
            rpcConfig {
                serialization {
                    json()
                }
            }
            registerService<RaceRatService> {
                RaceRatServiceImpl(uuid)
            }
            registerService<RaceRatCardService> {
                RaceRatCardServiceImpl(uuid)
            }
            closeReason.invokeOnCompletion {
                launch {
                    getPlayerState(uuid)?.let { playerFlow ->
                        val player = playerFlow.value.copy(isInactive = true)
                        playerFlow.update { player }
                        getGlobalEventBus(player.boardId).emit(GlobalEvent.PlayerChanged(player))
                        validateBoard(player.boardId, player.id)
                    }
                }
            }
        }
    }
}

suspend fun validateBoard(boardId: String, inActivePlayerId: String = "") {
    getBoardState(boardId)?.let { boardState ->
        val board = boardState.value
        val timeZone = TimeZone.currentSystemDefault()
        if (board.players().none { player -> !player.isInactive }) {
            val now = Clock.System.now()
            val lastCheckTime = board.lastCheckTime.toInstant(timeZone)
            val duration: Duration = now - lastCheckTime
            if (duration > 1.hours) {
                boards.value = boards.value.filter { it != boardId }
                removeFromStorage(boardId)
            }
        }
        boardState.value = boardState.value.copy(lastCheckTime = Clock.System.now().toLocalDateTime(timeZone))
        if (inActivePlayerId == board.activePlayer) {
            boardState.nextPlayer()
        }
    }
}

fun Application.installCORS() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader(HttpHeaders.Upgrade)
        exposeHeader("X-My-Custom-Header")
        exposeHeader("X-Another-Custom-Header")
        allowNonSimpleContentTypes = true
        allowCredentials = true
        allowSameOrigin = true
        anyHost()
    }
}