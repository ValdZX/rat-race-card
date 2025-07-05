package ua.vald_zx.game.rat.race.card.logic

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ua.vald_zx.game.rat.race.card.logic.RatRace2BoardAction.LoadState
import ua.vald_zx.game.rat.race.card.logic.RatRace2BoardSideEffect.ShowCard
import ua.vald_zx.game.rat.race.card.screen.board.BoardCardType
import ua.vald_zx.game.rat.race.card.screen.board.PlaceType
import ua.vald_zx.game.rat.race.card.screen.board.board
import ua.vald_zx.game.rat.race.card.service
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes

val players = mutableStateOf(emptyList<Player>())

enum class BoardLayer(val cellCount: Int, val level: Int) {
    INNER(78, 0),
    OUTER(74, 1),
}

fun Int.toLayer(): BoardLayer {
    return BoardLayer.entries.find { it.level == this } ?: BoardLayer.INNER
}

@Serializable
data class RatRace2BoardState(
    val positionsHistory: List<Int> = listOf(1),
    val currentPlayer: Player? = null,
    val highlightedCard: BoardCardType? = null,
) : State {
    val layer: BoardLayer
        get() = currentPlayer?.state?.level?.toLayer() ?: BoardLayer.INNER
    val color: Long
        get() = currentPlayer?.attrs?.color ?: 0
}

sealed class RatRace2BoardAction : Action {
    data class LoadState(val state: RatRace2BoardState) : RatRace2BoardAction()
    data class Move(val dice: Int) : RatRace2BoardAction()
    data class ChangeColor(val color: Long) : RatRace2BoardAction()
    data object BackLastMove : RatRace2BoardAction()
    data object SwitchLayer : RatRace2BoardAction()
    data class UpdateCurrentPlayer(val player: Player) : RatRace2BoardAction()
    data class HighlightCard(val card: BoardCardType) : RatRace2BoardAction()
    data class SelectedCard(val card: BoardCardType) : RatRace2BoardAction()
    data class ToDiscardPile(val card: BoardCardType) : RatRace2BoardAction()
}

sealed class RatRace2BoardSideEffect : Effect {
    data class ShowCard(val card: BoardCardType) : RatRace2BoardSideEffect()
}

class RatRace2BoardStore : Store<RatRace2BoardState, RatRace2BoardAction, RatRace2BoardSideEffect>,
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    private val state = MutableStateFlow(RatRace2BoardState())
    private val sideEffect = MutableSharedFlow<RatRace2BoardSideEffect>()

    override fun observeState(): StateFlow<RatRace2BoardState> = state

    override fun observeSideEffect(): Flow<RatRace2BoardSideEffect> = sideEffect

    override fun dispatch(action: RatRace2BoardAction) {
        val oldState = state.value
        val newState = when (action) {
            is LoadState -> {
                action.state
            }

            is RatRace2BoardAction.UpdateCurrentPlayer -> {
                oldState.copy(currentPlayer = action.player)
            }

            RatRace2BoardAction.BackLastMove -> {
                val moves = oldState.positionsHistory
                val newMoves = moves.subList(0, moves.lastIndex)
                val position = newMoves.last()
                launch { service?.changePosition(position, oldState.layer.level) }
                oldState.copy(
                    positionsHistory = newMoves,
                )
            }

            is RatRace2BoardAction.HighlightCard -> {
                oldState.copy(highlightedCard = action.card)
            }

            is RatRace2BoardAction.SelectedCard -> {
                launch { sideEffect.emit(ShowCard(action.card)) }
                oldState.copy(highlightedCard = null)
            }

            is RatRace2BoardAction.Move -> {
                val position = moveTo(
                    oldState.currentPlayer?.state?.position ?: 1,
                    oldState.layer.cellCount,
                    action.dice
                )
                launch {
                    service?.changePosition(position, oldState.layer.level)
                    delay(500)
                    board.layers[oldState.layer]?.places[position]?.let { place ->
                        oldState.processPlace(place)
                    }
                }
                oldState.copy(
                    positionsHistory = oldState.positionsHistory + position,
                )
            }

            RatRace2BoardAction.SwitchLayer -> {
                val currentLayer = oldState.layer
                val layer = if (currentLayer == BoardLayer.INNER) {
                    BoardLayer.OUTER
                } else {
                    BoardLayer.INNER
                }
                launch { service?.changePosition(1, layer.level) }
                oldState
            }

            is RatRace2BoardAction.ChangeColor -> {
                launch { service?.updateAttributes(PlayerAttributes(color = action.color)) }
                oldState
            }

            is RatRace2BoardAction.ToDiscardPile -> {
                oldState//todo
            }
        }
        if (newState != oldState) {
            state.value = newState
        }
    }

    private fun RatRace2BoardState.processPlace(place: PlaceType) {
        when (place) {
            PlaceType.Bankruptcy -> {
                //todo
            }

            PlaceType.BigBusiness -> {
                //todo
            }

            PlaceType.Business -> {
                dispatch(RatRace2BoardAction.HighlightCard(BoardCardType.SmallBusiness))
            }

            PlaceType.Chance -> {
                dispatch(RatRace2BoardAction.HighlightCard(BoardCardType.Chance))
            }

            PlaceType.Child -> {
                //todo
            }

            PlaceType.Deputy -> {
                //todo
            }

            PlaceType.Desire -> {
                //todo
            }

            PlaceType.Divorce -> {
                //todo
            }

            PlaceType.Exaltation -> {
                //todo
            }

            PlaceType.Expenses -> {
                dispatch(RatRace2BoardAction.HighlightCard(BoardCardType.Expenses))
            }

            PlaceType.Love -> {
                //todo
            }

            PlaceType.Rest -> {
                //todo
            }

            PlaceType.Salary -> {
                //todo
            }

            PlaceType.Shopping -> {
                dispatch(RatRace2BoardAction.HighlightCard(BoardCardType.Shopping))
            }

            PlaceType.Start -> {
                //todo
            }

            PlaceType.Store -> {
                dispatch(RatRace2BoardAction.HighlightCard(BoardCardType.EventStore))
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
