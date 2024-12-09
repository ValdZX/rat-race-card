package ua.vald_zx.game.rat.race.card.logic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ua.vald_zx.game.rat.race.card.raceRate4KStore

@Serializable
data class RatRace4CardState(
    val list: List<String> = emptyList()
) : State

sealed class RatRace4CardAction : Action {
    data class LoadState(val state: RatRace4CardState) : RatRace4CardAction()
}

sealed class RatRace4CardSideEffect : Effect {

}

class RatRace4CardStore : Store<RatRace4CardState, RatRace4CardAction, RatRace4CardSideEffect>,
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    private val state = MutableStateFlow(RatRace4CardState())
    private val sideEffect = MutableSharedFlow<RatRace4CardSideEffect>()

    override fun observeState(): StateFlow<RatRace4CardState> = state

    override fun observeSideEffect(): Flow<RatRace4CardSideEffect> = sideEffect

    override fun dispatch(action: RatRace4CardAction) {
        val oldState = state.value
        val newState = when (action) {
            is RatRace4CardAction.LoadState -> action.state
        }
        if (newState != oldState) {
            state.value = newState
            launch { raceRate4KStore.set(newState) }
        }
    }
}