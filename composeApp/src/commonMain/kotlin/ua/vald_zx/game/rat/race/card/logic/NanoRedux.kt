package ua.vald_zx.game.rat.race.card.logic

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface State
interface Action
interface Effect

interface Store<S : State, A : Action, E : Effect> {
    fun observeState(): StateFlow<S>
    fun observeSideEffect(): Flow<E>
    fun dispatch(action: A)
}