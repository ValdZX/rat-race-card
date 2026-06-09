package ua.vald_zx.game.rat.race.server.data

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.Player
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
object Storage {
    private val log = KtorSimpleLogger("Storage")
    private val db: MongoDatabase by lazy { connectToDatabase() }
    private val persistenceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val boardsFlow = MutableStateFlow<List<Board>>(emptyList())
    private val players: MutableMap<String, MutableStateFlow<Player>> = mutableMapOf()
    private val boards: MutableMap<String, MutableStateFlow<Board>> = mutableMapOf()

    private val playersLock = Mutex()
    private val boardsLock = Mutex()

    private val playerWriteQueue = WriteBehindQueue<Player>(persistenceScope) { db.updatePlayer(it) }
    private val boardWriteQueue = WriteBehindQueue<Board>(persistenceScope) { db.updateBoard(it) }

    private suspend fun getPlayerStateOrNull(id: String): MutableStateFlow<Player>? {
        players[id]?.let { return it }
        return playersLock.withLock {
            players[id]?.let { return it }
            val player = db.getPlayer(id) ?: return null
            MutableStateFlow(player).also { players[id] = it }
        }
    }

    private suspend fun getBoardStateOrNull(id: String): MutableStateFlow<Board>? {
        boards[id]?.let { return it }
        return boardsLock.withLock {
            boards[id]?.let { return it }
            val board = db.getBoard(id) ?: return null
            MutableStateFlow(board).also { boards[id] = it }
        }
    }

    suspend fun getPlayerOrNull(id: String): Player? = getPlayerStateOrNull(id)?.value

    suspend fun getBoardOrNull(id: String): Board? = getBoardStateOrNull(id)?.value

    suspend fun getPlayer(id: String): Player =
        getPlayerOrNull(id) ?: error("Player not found: $id")

    suspend fun getBoard(id: String): Board =
        getBoardOrNull(id) ?: error("Board not found: $id")

    suspend fun updatePlayer(player: Player) {
        cachePlayer(player)
        playerWriteQueue.enqueue(player.id, player)
        recalculateBoardActivity(player.boardId)
    }

    private fun cachePlayer(player: Player) {
        val state = players[player.id]
        if (state != null) state.value = player
        else players[player.id] = MutableStateFlow(player)
    }

    private fun cacheBoard(board: Board) {
        val state = boards[board.id]
        if (state != null) state.value = board
        else boards[board.id] = MutableStateFlow(board)
    }

    private suspend fun recalculateBoardActivity(boardId: String) {
        val board = getBoardOrNull(boardId) ?: return
        val boardPlayers = board.playerIds.mapNotNull { getPlayerOrNull(it) }
        val allInactive = boardPlayers.all { it.isInactive }
        val updated = when {
            allInactive && board.allInactiveSinceEpochMs == null ->
                board.copy(allInactiveSinceEpochMs = Clock.System.now().toEpochMilliseconds())

            !allInactive && board.allInactiveSinceEpochMs != null ->
                board.copy(allInactiveSinceEpochMs = null)

            else -> return
        }
        updateBoard(updated)
    }

    suspend fun players(boardId: String): List<Player> {
        val board = getBoardOrNull(boardId) ?: return emptyList()
        return board.playerIds.mapNotNull { playerId -> getPlayerOrNull(playerId) }
    }

    suspend fun removeBoard(boardId: String) {
        getBoardOrNull(boardId)?.playerIds?.forEach { playerId -> removePlayer(playerId) }
        boardWriteQueue.cancel(boardId)
        boards.remove(boardId)
        runCatching { db.removeBoard(boardId) }
            .onFailure { log.error("Failed to remove board $boardId", it) }
        updateBoardList()
    }

    suspend fun removePlayer(playerId: String) {
        playerWriteQueue.cancel(playerId)
        players.remove(playerId)
        runCatching { db.removePlayer(playerId) }
            .onFailure { log.error("Failed to remove player $playerId", it) }
    }

    private fun updateBoardList() {
        boardsFlow.value = boards.values.map { it.value }
    }

    suspend fun updateBoard(board: Board) {
        cacheBoard(board)
        boardWriteQueue.enqueue(board.id, board)
        updateBoardList()
    }

    suspend fun boards(): List<Board> {
        if (boards.isEmpty()) {
            boardsLock.withLock {
                if (boards.isEmpty()) {
                    db.boards().forEach { board ->
                        boards[board.id] = MutableStateFlow(board)
                    }
                    updateBoardList()
                }
            }
        }
        return boards.values.map { it.value }
    }

    fun observeBoards(): Flow<List<Board>> = boardsFlow

    suspend fun observeBoard(id: String): Flow<Board> =
        getBoardStateOrNull(id) ?: error("Board not found: $id")

    suspend fun newBoard(board: Board) {
        val initialized =
            if (board.allInactiveSinceEpochMs == null) {
                board.copy(allInactiveSinceEpochMs = Clock.System.now().toEpochMilliseconds())
            } else board
        cacheBoard(initialized)
        updateBoardList()
        boardWriteQueue.enqueue(initialized.id, initialized)
    }

    suspend fun newPlayer(player: Player) {
        cachePlayer(player)
        playerWriteQueue.enqueue(player.id, player)
    }

    suspend fun removeInactiveBoardsOlderThan(maxInactivity: Duration): List<String> {
        boards().forEach { board -> recalculateBoardActivity(board.id) }
        val nowMs = Clock.System.now().toEpochMilliseconds()
        val maxInactivityMs = maxInactivity.inWholeMilliseconds
        val expiredBoardIds = boards().filter { board ->
            val sinceMs = board.allInactiveSinceEpochMs ?: return@filter false
            nowMs - sinceMs >= maxInactivityMs
        }.map { it.id }
        expiredBoardIds.forEach { boardId -> removeBoard(boardId) }
        return expiredBoardIds
    }

    suspend fun flushPendingWrites() {
        playerWriteQueue.flush()
        boardWriteQueue.flush()
    }

    private class WriteBehindQueue<T>(
        private val scope: CoroutineScope,
        private val write: suspend (T) -> Unit,
    ) {
        private val lock = Mutex()
        private val pending = mutableMapOf<String, T>()
        private val workers = mutableMapOf<String, Job>()

        suspend fun enqueue(key: String, value: T) {
            lock.withLock {
                pending[key] = value
                if (workers[key]?.isActive != true) {
                    workers[key] = scope.launch { drain(key) }
                }
            }
        }

        suspend fun cancel(key: String) {
            val worker = lock.withLock {
                pending.remove(key)
                workers.remove(key)
            }
            worker?.cancelAndJoin()
        }

        private suspend fun drain(key: String) {
            while (true) {
                val value = lock.withLock {
                    val next = pending.remove(key)
                    if (next == null) {
                        workers.remove(key)
                        return
                    }
                    next
                }
                persist(key, value)
            }
        }

        private suspend fun persist(key: String, value: T) {
            var attempt = 0
            while (true) {
                val result = runCatching { write(value) }
                if (result.isSuccess) return
                attempt += 1
                if (attempt >= RETRY_LIMIT) {
                    LOG.error("Giving up persisting $key after $attempt attempts", result.exceptionOrNull())
                    return
                }
                LOG.warn("Persist attempt $attempt failed for $key, retrying", result.exceptionOrNull())
                delay(RETRY_DELAY)
            }
        }

        suspend fun flush() {
            val activeWorkers = lock.withLock { workers.values.toList() }
            activeWorkers.joinAll()
            val leftovers = lock.withLock {
                val snapshot = pending.toMap()
                pending.clear()
                snapshot
            }
            leftovers.forEach { (key, value) -> persist(key, value) }
        }

        private companion object {
            val LOG = KtorSimpleLogger("Storage.WriteBehind")
            const val RETRY_LIMIT = 5
            val RETRY_DELAY = 2.seconds
        }
    }
}
