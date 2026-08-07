@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class, FlowPreview::class)

package ua.vald_zx.game.rat.race.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.rpc.krpc.ktor.server.Krpc
import kotlinx.rpc.krpc.ktor.server.rpc
import kotlinx.rpc.krpc.serialization.json.json
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.BoardId
import ua.vald_zx.game.rat.race.card.shared.GlobalEvent
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.RaceRatCardService
import ua.vald_zx.game.rat.race.card.shared.RaceRatService
import ua.vald_zx.game.rat.race.server.data.Env
import ua.vald_zx.game.rat.race.server.data.Storage
import ua.vald_zx.game.rat.race.server.data.generateStableDbId
import ua.vald_zx.game.rat.race.server.generation.BoardGenerationCoordinator
import ua.vald_zx.game.rat.race.server.generation.LlmSettings
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

val checkStatusFlow = MutableSharedFlow<String>(
    extraBufferCapacity = EVENT_BUS_CAPACITY,
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
)
internal val instanceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
val checkStatusJobs = ConcurrentHashMap<String, Job>()
private val connectionIdsByUuid = ConcurrentHashMap<String, MutableSet<String>>()

private const val STATUS_SWEEP_INTERVAL = 60
private const val INACTIVITY_GRACE_MS = 20_000L
private const val STUCK_ROLL_SWEEP_INTERVAL = 10
private const val STUCK_ROLL_GRACE_MS = 12_000L
private const val BOARD_LIST_DEBOUNCE_MS = 300L

private val boardListFlow = MutableSharedFlow<List<BoardId>>(
    replay = 1,
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
)

internal fun observeBoardList(): Flow<List<BoardId>> = boardListFlow

fun main() {
    Runtime.getRuntime().addShutdownHook(Thread {
        runBlocking {
            runCatching { Storage.flushPendingWrites() }
                .onFailure { LOGGER.error("Failed to flush pending writes on shutdown", it) }
        }
    })
    embeddedServer(
        Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

private fun startBackgroundJobs() {
    instanceScope.launch {
        while (isActive) {
            delay(STATUS_SWEEP_INTERVAL.seconds)
            runCatching { runStatusSweep() }
                .onFailure { LOGGER.error("Status sweep failed", it) }
        }
    }
    instanceScope.launch {
        while (isActive) {
            delay(STUCK_ROLL_SWEEP_INTERVAL.seconds)
            runCatching { recoverStuckRolls(STUCK_ROLL_GRACE_MS, System.currentTimeMillis()) }
                .onFailure { LOGGER.error("Stuck roll sweep failed", it) }
        }
    }
    instanceScope.launch {
        projectBoardList()
        Storage.observeBoards()
            .debounce(BOARD_LIST_DEBOUNCE_MS)
            .collect { projectBoardList() }
    }
}

private suspend fun projectBoardList() {
    runCatching { boardListFlow.emit(boardIdList(System.currentTimeMillis())) }
        .onFailure { LOGGER.error("Board list projection failed", it) }
}

private suspend fun runStatusSweep() {
    checkStatusJobs.values.forEach { it.cancel() }
    checkStatusJobs.clear()

    checkStatusFlow.emit(Uuid.random().toString())

    Storage.boards().forEach { board ->
        val players = board.playerIds.mapNotNull { playerId -> Storage.getPlayerOrNull(playerId) }
        reconcileBoardActivity(board, players)
        players.forEach { player ->
            if (!player.isInactive) {
                checkStatusJobs[player.id] = instanceScope.launch {
                    try {
                        delay(INACTIVITY_GRACE_MS.milliseconds)
                        markPlayerInactive(player.id)
                    } finally {
                        checkStatusJobs.remove(player.id, coroutineContext.job)
                    }
                }
            }
        }
    }
}

private suspend fun reconcileBoardActivity(board: Board, players: List<Player>) {
    val allInactive = players.all { it.isInactive }
    val updated = when {
        allInactive && board.allInactiveSinceEpochMs == null ->
            board.copy(allInactiveSinceEpochMs = System.currentTimeMillis())

        !allInactive && board.allInactiveSinceEpochMs != null ->
            board.copy(allInactiveSinceEpochMs = null)

        else -> return
    }
    Storage.updateBoard(updated)
}

private val globalEventBusMap = ConcurrentHashMap<String, MutableSharedFlow<GlobalEvent>>()
fun getGlobalEventBus(boardId: String): MutableSharedFlow<GlobalEvent> {
    return globalEventBusMap.computeIfAbsent(boardId) {
        MutableSharedFlow(
            extraBufferCapacity = EVENT_BUS_CAPACITY,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    }
}

internal fun releaseGlobalEventBus(boardId: String) {
    globalEventBusMap.remove(boardId)
}

fun Application.module() {
    install(WebSockets) {
        pingPeriodMillis = 15.seconds.inWholeMilliseconds
        timeoutMillis = 15.seconds.inWholeMilliseconds
    }
    install(Krpc)
    installCORS()
    startBackgroundJobs()
    LlmSettings.logConfiguration()
    instanceScope.launch { BoardGenerationCoordinator.markPendingAsPaused() }
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
            val connectionId = Uuid.random().toString()
            registerService<RaceRatService> {
                RaceRatServiceImpl(
                    uuidStateProvider = uuidStateProvider,
                    scope = connectionScope,
                    connectionIdentified = { uuid -> registerConnection(uuid, connectionId) },
                )
            }
            registerService<RaceRatCardService> {
                RaceRatCardServiceImpl(connectionScope)
            }
            closeReason.invokeOnCompletion {
                instanceScope.launch {
                    try {
                        val uuid = uuidStateProvider.value
                        if (unregisterConnection(uuid, connectionId)) {
                            handleDisconnect(uuid)
                        }
                    } finally {
                        connectionScope.cancel()
                    }
                }
            }
        }
    }
}

private fun registerConnection(uuid: String, connectionId: String) {
    if (uuid.isEmpty()) return
    connectionIdsByUuid.compute(uuid) { _, current ->
        (current ?: ConcurrentHashMap.newKeySet()).apply { add(connectionId) }
    }
}

private fun unregisterConnection(uuid: String, connectionId: String): Boolean {
    if (uuid.isEmpty()) return false
    var hasActiveConnections = false
    connectionIdsByUuid.compute(uuid) { _, current ->
        current?.apply { remove(connectionId) }
            ?.takeIf { remaining -> remaining.isNotEmpty() }
            ?.also { hasActiveConnections = true }
    }
    return !hasActiveConnections
}

private suspend fun handleDisconnect(uuid: String) {
    if (uuid.isEmpty()) return
    Storage.boards().forEach { board ->
        val playerId = generateStableDbId(board.id, uuid)
        if (!board.playerIds.contains(playerId)) return@forEach
        checkStatusJobs.remove(playerId)?.cancel()
        markPlayerInactive(playerId)
    }
}

private suspend fun markPlayerInactive(playerId: String) {
    val player = Storage.getPlayerOrNull(playerId) ?: return
    if (player.isInactive) return
    publishPlayerChange(player.copy(isInactive = true))

    val board = Storage.getBoardOrNull(player.boardId) ?: return
    val sanitizedBoard = board.copy(
        bidList = board.bidList.filterNot { bid -> bid.playerId == playerId },
        processedPlayerIds = board.processedPlayerIds - playerId,
    )
    if (sanitizedBoard != board) {
        Storage.updateBoard(sanitizedBoard)
    }
    if (sanitizedBoard.activePlayerId == playerId) {
        nextPlayer(sanitizedBoard)
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
