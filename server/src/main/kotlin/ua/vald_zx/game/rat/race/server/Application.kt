@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package ua.vald_zx.game.rat.race.server

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.rpc.krpc.ktor.server.Krpc
import kotlinx.rpc.krpc.ktor.server.rpc
import kotlinx.rpc.krpc.serialization.json.json
import ua.vald_zx.game.rat.race.card.shared.GlobalEvent
import ua.vald_zx.game.rat.race.card.shared.RaceRatCardService
import ua.vald_zx.game.rat.race.card.shared.RaceRatService
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

fun main() {
    embeddedServer(
        Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val db: MongoDatabase by lazy { connectToDatabase() }
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
                RaceRatServiceImpl(appScope, db, uuidStateProvider)
            }
            registerService<RaceRatCardService> {
                RaceRatCardServiceImpl()
            }
            closeReason.invokeOnCompletion {
                launch {
                    val uuid = uuidStateProvider.value
                    if (uuid.isEmpty()) return@launch
                    val player = db.getPlayer(uuid)
                    db.updatePlayer(player.copy(isInactive = true))
                    getGlobalEventBus(player.boardId).emit(GlobalEvent.PlayerChanged(player))
                    val board = db.getBoard(player.id)
                    if (db.players(board.id).none { player -> !player.isInactive }) {
                        appScope.launch {
                            delay(1000 * 60 * 60 * 24)
                            db.removeBoard(board.id)
                            appScope.cancel()
                        }
                    } else {
                        if (db.getPlayer(board.activePlayerId).isInactive) {
                            db.nextPlayer(board)
                        }
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