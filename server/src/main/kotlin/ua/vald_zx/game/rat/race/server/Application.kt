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
import ua.vald_zx.game.rat.race.server.data.Env
import ua.vald_zx.game.rat.race.server.data.Storage
import ua.vald_zx.game.rat.race.server.data.generateStableDbId
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

val checkStatusFlow = MutableSharedFlow<String>()
private val instanceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
val checkStatusJobs = mutableMapOf<String, Job>()

private const val STATUS_SWEEP_INTERVAL = 60
private const val INACTIVITY_GRACE_MS = 5000L
private val BOARD_CLEANUP_INTERVAL = 1.days
private val BOARD_MAX_INACTIVITY = 7.days

fun main() {
    Runtime.getRuntime().addShutdownHook(Thread {
        runBlocking {
            runCatching { Storage.flushPendingWrites() }
                .onFailure { LOGGER.error("Failed to flush pending writes on shutdown", it) }
        }
    })
    instanceScope.launch {
        while (isActive) {
            delay(STATUS_SWEEP_INTERVAL.seconds)
            runStatusSweep()
        }
    }
    instanceScope.launch {
        while (isActive) {
            delay(BOARD_CLEANUP_INTERVAL)
            runBoardCleanup()
        }
    }
    embeddedServer(
        Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

private suspend fun runStatusSweep() {
    checkStatusJobs.values.forEach { it.cancel() }
    checkStatusJobs.clear()

    checkStatusFlow.emit(Uuid.random().toString())

    Storage.boards().forEach { board ->
        val players = board.playerIds.mapNotNull { playerId -> Storage.getPlayerOrNull(playerId) }
        players.forEach { player ->
            if (!player.isInactive) {
                checkStatusJobs[player.id] = instanceScope.launch {
                    delay(INACTIVITY_GRACE_MS.milliseconds)
                    val current = Storage.getPlayerOrNull(player.id) ?: return@launch
                    if (!current.isInactive) {
                        Storage.updatePlayer(current.copy(isInactive = true))
                    }
                    checkStatusJobs.remove(player.id)
                }
            }
        }
    }
}

private val globalEventBusMap = mutableMapOf<String, MutableSharedFlow<GlobalEvent>>()
fun getGlobalEventBus(boardId: String): MutableSharedFlow<GlobalEvent> {
    return globalEventBusMap.getOrPut(boardId) { MutableSharedFlow() }
}

private suspend fun runBoardCleanup() {
    runCatching {
        val removed = Storage.removeInactiveBoardsOlderThan(BOARD_MAX_INACTIVITY)
        if (removed.isNotEmpty()) {
            LOGGER.info("Removed ${removed.size} inactive boards: $removed")
        }
    }.onFailure { error ->
        LOGGER.error("Board cleanup failed", error)
    }
}

fun Application.module() {
    install(Krpc)
    installCORS()
    monitor.subscribe(ApplicationStopping) {
        runBlocking {
            runCatching { Storage.flushPendingWrites() }
                .onFailure { LOGGER.error("Failed to flush pending writes on stop", it) }
        }
    }
    routing {
        staticResources("/content", "mycontent")
        get("/") { call.respondText("Race rat RPC services") }
        rpc("/api") {
            rpcConfig {
                serialization {
                    json()
                }
            }
            val uuidStateProvider = MutableStateFlow("")
            val connectionScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
            registerService<RaceRatService> {
                RaceRatServiceImpl(uuidStateProvider, connectionScope)
            }
            registerService<RaceRatCardService> {
                RaceRatCardServiceImpl(connectionScope)
            }
            closeReason.invokeOnCompletion {
                instanceScope.launch {
                    try {
                        handleDisconnect(uuidStateProvider.value)
                    } finally {
                        connectionScope.cancel()
                    }
                }
            }
        }
    }
}

private suspend fun handleDisconnect(uuid: String) {
    if (uuid.isEmpty()) return
    Storage.boards().forEach { board ->
        val playerId = generateStableDbId(board.id, uuid)
        if (!board.playerIds.contains(playerId)) return@forEach
        val player = Storage.getPlayerOrNull(playerId) ?: return@forEach
        if (player.isInactive) return@forEach

        val inactivePlayer = player.copy(isInactive = true)
        Storage.updatePlayer(inactivePlayer)
        getGlobalEventBus(board.id).emit(GlobalEvent.PlayerChanged(inactivePlayer))

        val freshBoard = Storage.getBoardOrNull(board.id) ?: return@forEach
        val activePlayer = Storage.getPlayerOrNull(freshBoard.activePlayerId)
        if (activePlayer == null || activePlayer.isInactive) {
            nextPlayer(freshBoard)
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

        val allowedOrigins = Env["ALLOWED_ORIGINS"]
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            .orEmpty()
        if (allowedOrigins.isEmpty()) {
            anyHost()
        } else {
            allowedOrigins.forEach { origin ->
                val schemeAndHost = origin.removePrefix("https://").removePrefix("http://")
                allowHost(schemeAndHost, schemes = listOf("http", "https"))
            }
            allowCredentials = true
        }
    }
}