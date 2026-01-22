package ua.vald_zx.game.rat.race.server

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.Player

suspend inline fun MutableStateFlow<Board>?.changeBoard(todo: suspend Board.() -> Board) {
    this?.update { it.todo() }
}

fun Board.players(): List<Player> {
    return playerIds.map { playerId ->
        getPlayerState(playerId)?.value ?: error("No player found for $playerId")
    }
}

suspend fun MutableStateFlow<Board>?.nextPlayer() {
    LOGGER.debug("next player")
    val board = this?.value ?: return
    val activePlayers = board.players().filter { player -> !player.isInactive }
    if (activePlayers.isEmpty()) return
    val playerIds = activePlayers.map { it.id }
    val activePlayerIndex = playerIds.indexOf(board.activePlayer)
    val nextPlayerId = if (activePlayerIndex + 1 == playerIds.size) {
        playerIds.first()
    } else {
        playerIds[activePlayerIndex + 1]
    }
    changeBoard {
        discardPileB().copy(
            activePlayer = nextPlayerId,
            moveCount = moveCount + 1,
            canRoll = true,
            diceRolling = false,
            takenCard = null,
            canTakeCard = null,
            auction = null,
            bidList = emptyList()
        )
    }
    val nextPlayer = activePlayers.find { it.id == nextPlayerId }
    if ((nextPlayer?.inRest ?: 0) > 0) {
        getPlayerState(nextPlayerId)?.update {
            it.copy(inRest = it.inRest - 1)
        }
        nextPlayer()
    }
}

fun Board.discardPileB(): Board {
    val card = takenCard
    return if (card != null) {
        val discard = discard.toMutableMap()
        discard[card.type] = discard[card.type].orEmpty() + card.id
        val cards = cards.toMutableMap()
        cards[card.type] = cards[card.type].orEmpty() - card.id
        copy(
            discard = discard,
            cards = cards,
            takenCard = null,
        ).invalidateDecks()
    } else this
}

fun Board.invalidateDecks(): Board {
    val discard = discard.toMutableMap()
    val cards = cards.map { (type, list) ->
        type to list.ifEmpty {
            val cards = discard[type]
            discard[type] = emptyList()
            cards
        }.orEmpty()
    }.toMap()
    return copy(cards = cards, discard = discard)
}