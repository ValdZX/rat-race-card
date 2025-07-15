package ua.vald_zx.game.rat.race.card.logic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ua.vald_zx.game.rat.race.card.currentPlayerId
import ua.vald_zx.game.rat.race.card.logic.BoardAction.LoadState
import ua.vald_zx.game.rat.race.card.logic.BoardSideEffect.ShowDice
import ua.vald_zx.game.rat.race.card.screen.board.PlaceType
import ua.vald_zx.game.rat.race.card.screen.board.boardLayers
import ua.vald_zx.game.rat.race.card.service
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes

val players = MutableStateFlow(emptyList<Player>())

enum class BoardLayer(val cellCount: Int, val level: Int) {
    INNER(78, 0),
    OUTER(74, 1),
}

fun Int.toLayer(): BoardLayer {
    return BoardLayer.entries.find { it.level == this } ?: BoardLayer.INNER
}

@Serializable
data class BoardState(
    val positionsHistory: List<Int> = listOf(1),
    val currentPlayer: Player? = null,
    val highlightedCard: BoardCardType? = null,
    val board: Board? = null,
    val canRoll: Boolean = false,
) : State {
    val layer: BoardLayer
        get() = currentPlayer?.state?.level?.toLayer() ?: BoardLayer.INNER
    val color: Long
        get() = currentPlayer?.attrs?.color ?: 0
    val currentPlayerIsActive: Boolean
        get() = currentPlayerId == board?.activePlayer
}

sealed class BoardAction : Action {
    data class LoadState(val state: BoardState) : BoardAction()
    data class Move(val dice: Int) : BoardAction()
    data class ChangeColor(val color: Long) : BoardAction()
    data object BackLastMove : BoardAction()
    data object SwitchLayer : BoardAction()
    data object RollDice : BoardAction()
    data class UpdateCurrentPlayer(val player: Player) : BoardAction()
    data class HighlightCard(val card: BoardCardType) : BoardAction()
    data class SelectedCard(val card: BoardCardType) : BoardAction()
    data object ToDiscardPile : BoardAction()
    data class UpdateBoard(val board: Board) : BoardAction()
    data class DiceRolled(val dice: Int) : BoardAction()
}

sealed class BoardSideEffect : Effect {
    data class ShowDice(val dice: Int) : BoardSideEffect()
}

class BoardStore : Store<BoardState, BoardAction, BoardSideEffect>,
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    private val state = MutableStateFlow(BoardState())
    private val sideEffect = MutableSharedFlow<BoardSideEffect>()

    val card = CardStore(state)

    override fun observeState(): StateFlow<BoardState> = state

    override fun observeSideEffect(): Flow<BoardSideEffect> = sideEffect

    override fun dispatch(action: BoardAction) {
        val oldState = state.value
        val newState = when (action) {
            is LoadState -> {
                action.state
            }

            is BoardAction.UpdateCurrentPlayer -> {
                oldState.copy(currentPlayer = action.player)
            }

            BoardAction.BackLastMove -> {
                val moves = oldState.positionsHistory
                val newMoves = moves.subList(0, moves.lastIndex)
                val position = newMoves.last()
                launch { service?.changePosition(position, oldState.layer.level) }
                oldState.copy(
                    positionsHistory = newMoves,
                )
            }

            is BoardAction.HighlightCard -> {
                oldState.copy(highlightedCard = action.card)
            }

            is BoardAction.SelectedCard -> {
                launch { service?.takeCard(action.card) }
                oldState.copy(highlightedCard = null)
            }

            is BoardAction.Move -> {
                val position = moveTo(
                    oldState.currentPlayer?.state?.position ?: 1,
                    oldState.layer.cellCount,
                    action.dice
                )
                launch {
                    service?.changePosition(position, oldState.layer.level)
                    delay(500)
                    boardLayers.layers[oldState.layer]?.places[position]?.let { place ->
                        oldState.processPlace(place)
                    }
                }
                oldState.copy(
                    positionsHistory = oldState.positionsHistory + position,
                )
            }

            BoardAction.SwitchLayer -> {
                val currentLayer = oldState.layer
                val layer = if (currentLayer == BoardLayer.INNER) {
                    BoardLayer.OUTER
                } else {
                    BoardLayer.INNER
                }
                launch { service?.changePosition(1, layer.level) }
                oldState
            }

            is BoardAction.ChangeColor -> {
                launch { service?.updateAttributes(PlayerAttributes(color = action.color)) }
                oldState
            }

            BoardAction.ToDiscardPile -> {
                launch {
                    service?.discardPile()
                    service?.nextPlayer()
                }
                oldState//todo
            }

            is BoardAction.DiceRolled -> {
                launch { sideEffect.emit(ShowDice(action.dice)) }
                oldState
            }

            BoardAction.RollDice -> {
                launch { service?.rollDice() }
                oldState.copy(canRoll = false)
            }

            is BoardAction.UpdateBoard -> {
                val rollCountChanged =
                    oldState.board?.moveCount != action.board.moveCount || action.board.moveCount == 0
                val canRoll = oldState.canRoll || rollCountChanged &&
                        action.board.activePlayer == currentPlayerId &&
                        action.board.takenCard == null &&
                        oldState.highlightedCard == null
                oldState.copy(board = action.board, canRoll = canRoll)
            }
        }
        if (newState != oldState) {
            state.value = newState
        }
    }

    private fun BoardState.processPlace(place: PlaceType) = launch {
        when (place) {
            PlaceType.Bankruptcy -> {
                service?.nextPlayer()
            }

            PlaceType.BigBusiness -> {
                service?.nextPlayer()
            }

            PlaceType.Business -> {
                dispatch(BoardAction.HighlightCard(BoardCardType.SmallBusiness))
            }

            PlaceType.Chance -> {
                dispatch(BoardAction.HighlightCard(BoardCardType.Chance))
            }

            PlaceType.Child -> {
                service?.nextPlayer()
            }

            PlaceType.Deputy -> {
                service?.nextPlayer()
            }

            PlaceType.Desire -> {
                service?.nextPlayer()
            }

            PlaceType.Divorce -> {
                service?.nextPlayer()
            }

            PlaceType.Exaltation -> {
                service?.nextPlayer()
            }

            PlaceType.Expenses -> {
                dispatch(BoardAction.HighlightCard(BoardCardType.Expenses))
            }

            PlaceType.Love -> {
                service?.nextPlayer()
            }

            PlaceType.Rest -> {
                service?.nextPlayer()
            }

            PlaceType.Salary -> {
                service?.nextPlayer()
            }

            PlaceType.Shopping -> {
                dispatch(BoardAction.HighlightCard(BoardCardType.Shopping))
            }

            PlaceType.Start -> {
                //todo
            }

            PlaceType.Store -> {
                dispatch(BoardAction.HighlightCard(BoardCardType.EventStore))
            }

            PlaceType.TaxInspection -> {
                //todo
            }
        }
    }
}

fun moveTo(position: Int, cellCount: Int, toMove: Int): Int {
    val nextPosition = position + toMove
    return if (nextPosition < 0) {
        cellCount + nextPosition
    } else if (cellCount <= nextPosition) {
        nextPosition - cellCount
    } else {
        nextPosition
    }
}
