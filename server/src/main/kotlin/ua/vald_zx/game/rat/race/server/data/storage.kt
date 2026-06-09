package ua.vald_zx.game.rat.race.server.data

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.Player

object Storage {
    private val db: MongoDatabase by lazy { connectToDatabase() }

    private val boardsFlow = MutableStateFlow<List<Board>>(emptyList())
    private val players: MutableMap<String, MutableStateFlow<Player>> = mutableMapOf()
    private val boards: MutableMap<String, MutableStateFlow<Board>> = mutableMapOf()

    private val playersLock = Mutex()
    private val boardsLock = Mutex()

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
        val state = getPlayerStateOrNull(player.id)
        if (state != null) {
            state.value = player
        } else {
            players[player.id] = MutableStateFlow(player)
        }
        db.updatePlayer(player)
    }

    suspend fun players(boardId: String): List<Player> {
        val board = getBoardOrNull(boardId) ?: return emptyList()
        return board.playerIds.mapNotNull { playerId -> getPlayerOrNull(playerId) }
    }

    suspend fun removeBoard(boardId: String) {
        getBoardOrNull(boardId)?.playerIds?.forEach { playerId -> removePlayer(playerId) }
        boards.remove(boardId)
        db.removeBoard(boardId)
        updateBoardList()
    }

    suspend fun removePlayer(playerId: String) {
        players.remove(playerId)
        db.removePlayer(playerId)
    }

    private suspend fun updateBoardList() {
        boardsFlow.emit(boards.values.map { it.value })
    }

    suspend fun updateBoard(board: Board) {
        val state = getBoardStateOrNull(board.id)
        if (state != null) {
            state.value = board
        } else {
            boards[board.id] = MutableStateFlow(board)
        }
        db.updateBoard(board)
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
        db.newBoard(board)
        boards[board.id] = MutableStateFlow(board)
        updateBoardList()
    }

    suspend fun newPlayer(player: Player) {
        db.newPlayer(player)
        players[player.id] = MutableStateFlow(player)
    }
}