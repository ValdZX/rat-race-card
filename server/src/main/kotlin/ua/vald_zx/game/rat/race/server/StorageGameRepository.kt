package ua.vald_zx.game.rat.race.server

import ua.vald_zx.game.rat.race.card.shared.GameRepository
import ua.vald_zx.game.rat.race.card.shared.GameSnapshot
import ua.vald_zx.game.rat.race.server.data.Storage

internal object StorageGameRepository : GameRepository {
    override suspend fun load(boardId: String): GameSnapshot? {
        val board = Storage.getBoardOrNull(boardId) ?: return null
        return GameSnapshot(board, Storage.players(boardId))
    }

    override suspend fun save(previous: GameSnapshot, updated: GameSnapshot) {
        updated.players.forEach { player ->
            if (previous.players.firstOrNull { it.id == player.id } != player) {
                Storage.updatePlayer(player)
            }
        }
        Storage.updateBoard(updated.board)
    }
}
