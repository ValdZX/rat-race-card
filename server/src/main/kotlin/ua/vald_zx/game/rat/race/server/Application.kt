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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.rpc.krpc.ktor.server.Krpc
import kotlinx.rpc.krpc.ktor.server.rpc
import kotlinx.rpc.krpc.serialization.json.json
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.RaceRatService
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


val boards = MutableStateFlow<List<MutableStateFlow<Board>>>(emptyList())
val players = MutableStateFlow<Map<String, MutableStateFlow<Player>>>(emptyMap())
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
            closeReason.invokeOnCompletion {
                players.update {
                    it.filter { (id, _) -> id != uuid }
                }
                boards.value.find { board ->
                    board.value.playerIds.contains(uuid)
                }?.let { boardState ->
                    boardState.update { board ->
                        board.copy(playerIds = board.playerIds - uuid)
                    }
                    validateBoard(boardState.value.id)
                }
            }
        }
    }
}

fun validateBoard(boardId: String) {
    boards.value.find { boardState -> boardState.value.id == boardId }?.let { boardState ->
        val board = boardState.value
        val timeZone = TimeZone.currentSystemDefault()
        if (players.value.none { (_, player) -> !player.value.isInactive }) {
            val now = Clock.System.now()
            val lastCheckTime = board.lastCheckTime.toInstant(timeZone)
            val duration: Duration = now - lastCheckTime
            if (duration > 1.hours) {
                boards.value = boards.value.filter { it.value.id != boardId }
            }
        }
        boardState.value = boardState.value.copy(lastCheckTime = Clock.System.now().toLocalDateTime(timeZone))
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