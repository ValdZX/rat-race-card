package ua.vald_zx.game.rat.race.server.data

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.Player

object Storage {
    private val db: MongoDatabase by lazy { connectToDatabase() }

    private val boardsFlow = MutableStateFlow<List<Board>>(emptyList())
    private val players: MutableMap<String, MutableStateFlow<Player>> = mutableMapOf()
    private val boards: MutableMap<String, MutableStateFlow<Board>> = mutableMapOf()

    private suspend fun getPlayerState(id: String): MutableStateFlow<Player> {
        return players.getOrPut(id) { MutableStateFlow(db.getPlayer(id)) }
    }

    private suspend fun getBoardState(id: String): MutableStateFlow<Board> {
        return boards.getOrPut(id) { MutableStateFlow(db.getBoard(id)) }
    }

    suspend fun getPlayer(id: String): Player {
        return getPlayerState(id).value
    }

    suspend fun updatePlayer(player: Player) {
        getPlayerState(player.id).value = player
        db.updatePlayer(player)
    }

    suspend fun getBoard(id: String): Board {
        return getBoardState(id).value
    }

    suspend fun players(boardId: String): List<Player> {
        return getBoard(boardId).playerIds.map { playerId -> getPlayer(playerId) }
    }

    suspend fun removeBoard(boardId: String) {
        getBoard(boardId).playerIds.forEach { playerId -> removePlayer(playerId) }
        boards.remove(boardId)
        db.removeBoard(boardId)
        updateBoardList()
    }

    suspend fun removePlayer(playerId: String) {
        players.remove(playerId)
        db.removePlayer(playerId)
    }

    private suspend fun updateBoardList() {
        boardsFlow.emit(boards.map { (_, boardState) -> boardState.value })
    }

    suspend fun updateBoard(board: Board) {
        getBoardState(board.id).value = board
        db.updateBoard(board)
    }

    suspend fun boards(): List<Board> {
        if (boards.isEmpty()) {
            val storageBoard = db.boards()
            storageBoard.forEach { board ->
                boards[board.id] = MutableStateFlow(board)
            }
            return storageBoard
        } else {
            return boards.values.map { it.value }
        }
    }

    fun observeBoards(): Flow<List<Board>> = boardsFlow

    suspend fun observeBoard(id: String): Flow<Board> = getBoardState(id)

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