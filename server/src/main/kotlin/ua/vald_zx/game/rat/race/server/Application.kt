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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.rpc.krpc.ktor.server.Krpc
import kotlinx.rpc.krpc.ktor.server.rpc
import kotlinx.rpc.krpc.serialization.json.json
import ua.vald_zx.game.rat.race.card.shared.GlobalEvent
import ua.vald_zx.game.rat.race.card.shared.RaceRatCardService
import ua.vald_zx.game.rat.race.card.shared.RaceRatService
import ua.vald_zx.game.rat.race.server.data.Storage
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

val checkStatusFlow = MutableSharedFlow<String>()
private val instanceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
val checkStatusJobs = mutableMapOf<String, Job>()

fun main() {
    instanceScope.launch {
        while (true) {
            delay(1000 * 60)
            checkStatusJobs.clear()
            checkStatusFlow.emit(Uuid.random().toString())
            Storage.boards().forEach { board ->
                val players = board.playerIds.map { playerId -> Storage.getPlayer(playerId) }
                if(players.all { it.isInactive }) {
                    //So sad
                } else {
                    players.forEach { player ->
                        if (!player.isInactive) {
                            checkStatusJobs[player.id] = launch {
                                delay(5000)
                                val player = Storage.getPlayer(player.id)
                                Storage.updatePlayer(player.copy(isInactive = true))
                            }
                        }
                    }
                }
            }
        }
    }
    embeddedServer(
        Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}


private val globalEventBusMap = mutableMapOf<String, MutableSharedFlow<GlobalEvent>>()
fun getGlobalEventBus(boardId: String): MutableSharedFlow<GlobalEvent> {
    return globalEventBusMap.getOrPut(boardId) { MutableSharedFlow() }
}

fun Application.module() {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
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
            rpcConfig {
                serialization {
                    json()
                }
            }
            val uuidStateProvider = MutableStateFlow("")
            registerService<RaceRatService> {
                RaceRatServiceImpl(appScope, uuidStateProvider)
            }
            registerService<RaceRatCardService> {
                RaceRatCardServiceImpl()
            }
            closeReason.invokeOnCompletion {
                runBlocking {
                    val uuid = uuidStateProvider.value
                    if (uuid.isEmpty()) return@runBlocking
                    val player = Storage.getPlayer(uuid).copy(isInactive = true)
                    Storage.updatePlayer(player)
                    getGlobalEventBus(player.boardId).emit(GlobalEvent.PlayerChanged(player))
                    val board = Storage.getBoard(player.boardId)
                    if (Storage.getPlayer(board.activePlayerId).isInactive) {
                        nextPlayer(board)
                    }
                }
            }
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