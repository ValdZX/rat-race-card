package ua.vald_zx.game.rat.race.card.logic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ua.vald_zx.game.rat.race.card.logic.RatRace2BoardAction.LoadState
import ua.vald_zx.game.rat.race.card.service
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes

enum class BoardLayer(val cellCount: Int, val level: Int) {
    INNER(78, 0),
    OUTER(74, 1),
}

@Serializable
data class RatRace2BoardState(
    val position: Int = 1,
    val color: ULong = ULong.MIN_VALUE,
    val layer: BoardLayer = BoardLayer.INNER,
    val positionsHistory: List<Int> = listOf(1),
) : State

sealed class RatRace2BoardAction : Action {
    data class LoadState(val state: RatRace2BoardState) : RatRace2BoardAction()
    data class Move(val dice: Int) : RatRace2BoardAction()
    data class ChangeColor(val color: ULong) : RatRace2BoardAction()
    data object BackLastMove : RatRace2BoardAction()
    data object SwitchLayer : RatRace2BoardAction()
}

sealed class RatRace2BoardSideEffect : Effect

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

            RatRace2BoardAction.BackLastMove -> {
                val moves = oldState.positionsHistory
                val newMoves = moves.subList(0, moves.lastIndex)
                val position = newMoves.last()
                launch { service?.changePosition(position) }
                oldState.copy(
                    positionsHistory = newMoves,
                    position = position
                )
            }

            is RatRace2BoardAction.Move -> {
                val position = oldState.moveTo(action.dice)
                launch { service?.changePosition(position) }
                oldState.copy(
                    positionsHistory = oldState.positionsHistory + position,
                    position = position
                )
            }

            RatRace2BoardAction.SwitchLayer -> {
                oldState.copy(
                    layer = if (oldState.layer == BoardLayer.INNER) {
                        BoardLayer.OUTER
                    } else {
                        BoardLayer.INNER
                    },
                    position = 1
                )
            }

            is RatRace2BoardAction.ChangeColor -> {
                launch { service?.updateAttributes(PlayerAttributes(color = action.color)) }
                oldState.copy(color = action.color)
            }
        }
        if (newState != oldState) {
            state.value = newState
//            launch {
//                raceRate2BoardKStore.set(newState)
//            }
        }
    }

    private fun RatRace2BoardState.moveTo(dice: Int): Int {
        val nextPosition = position + dice
        return if (nextPosition < 0) {
            layer.cellCount + nextPosition
        } else if (layer.cellCount <= nextPosition) {
            nextPosition - layer.cellCount
        } else {
            nextPosition
        }
    }
}
