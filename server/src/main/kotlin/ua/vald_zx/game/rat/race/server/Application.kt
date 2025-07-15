@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package ua.vald_zx.game.rat.race.server

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.rpc.krpc.ktor.server.Krpc
import kotlinx.rpc.krpc.ktor.server.rpc
import kotlinx.rpc.krpc.serialization.json.json
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.CardLink
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

data class BoardState(
    val name: String,
    val createDateTime: LocalDateTime = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()),
    val lastCheckTime: LocalDateTime = createDateTime,
    val id: String = Uuid.random().toString(),
    val cards: Map<BoardCardType, MutableList<Int>> = mapOf(
        BoardCardType.Chance to (1..52).toMutableList(),
        BoardCardType.SmallBusiness to (1..53).toMutableList(),
        BoardCardType.MediumBusiness to (1..52).toMutableList(),
        BoardCardType.BigBusiness to (1..52).toMutableList(),
        BoardCardType.Expenses to (1..52).toMutableList(),
        BoardCardType.EventStore to (1..52).toMutableList(),
        BoardCardType.Shopping to (1..52).toMutableList(),
        BoardCardType.Deputy to (1..52).toMutableList(),
    ),
    val takenCard: CardLink? = null,
    val discard: MutableMap<BoardCardType, MutableList<Int>> = mutableMapOf(),
    val players: Map<String, Player> = mutableMapOf(),
    val activePlayer: String = "",
    val moveCount: Int = 0,
)

val boards = MutableStateFlow<List<MutableStateFlow<BoardState>>>(emptyList())

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
                boards.value.find { board ->
                    board.value.players.keys.contains(uuid)
                }?.let { board ->
                    board.value =
                        board.value.copy(players = board.value.players.toMutableMap().apply {
                            this[uuid]?.let { player ->
                                this[uuid] = player.copy(isInactive = true)
                            }
                        })
                    validateBoard(board.value.id)
                }
            }
        }
    }
}

fun validateBoard(boardId: String) {
    boards.value.find { boardState -> boardState.value.id == boardId }?.let { boardState ->
        val board = boardState.value
        val timeZone = TimeZone.currentSystemDefault()
        if (board.players.values.none { !it.isInactive }) {
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