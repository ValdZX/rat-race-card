package ua.vald_zx.game.rat.race.card.logic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ua.vald_zx.game.rat.race.card.beans.Business
import ua.vald_zx.game.rat.race.card.beans.BusinessType
import ua.vald_zx.game.rat.race.card.beans.ProfessionCard
import ua.vald_zx.game.rat.race.card.beans.Shares

data class AppState(
    val professionCard: ProfessionCard = ProfessionCard(),
    val cash: Int = 0,
    val deposit: Int = 0,
    val loan: Int = 0,
    val business: List<Business> = emptyList(),
    val isMarried: Boolean = false,
    val babies: Int = 0,
    val cars: Int = 0,
    val apartment: Int = 0,
    val cottage: Int = 0,
    val yacht: Int = 0,
    val flight: Int = 0,
    val sharesList: List<Shares> = emptyList(),
) : State {

    fun activeProfit(): Int {
        return business.sumOf { it.profit + it.extentions.sum() }
    }

    fun passiveProfit(): Int {
        return (deposit * 0.02).toInt()
    }

    fun totalProfit(): Int {
        return activeProfit() + passiveProfit()
    }

    fun creditExpenses(): Int {
        return loan / 10
    }

    fun totalExpenses(): Int {
        var totalExpenses = 0
        totalExpenses += professionCard.food
        totalExpenses += professionCard.rent
        totalExpenses += professionCard.cloth
        totalExpenses += professionCard.phone
        totalExpenses += professionCard.transport
        totalExpenses += babies * 300
        totalExpenses += cars * 600
        totalExpenses += apartment * 200
        totalExpenses += cottage * 1000
        totalExpenses += yacht * 1500
        totalExpenses += flight * 5000
        totalExpenses += creditExpenses()
        return totalExpenses
    }

    fun cashFlow(): Int {
        return totalProfit() - totalExpenses()
    }

    fun status(): String {
        return when {
            business.any { it.type == BusinessType.SMALL } && business.size > 1 -> "Підприємець"
            business.any { it.type == BusinessType.MEDIUM } -> "Бізнесмен"
            business.any { it.type == BusinessType.LARGE } -> "Мільйонер"
            else -> professionCard.profession
        }
    }
}

sealed class AppAction : Action {
    data class FillProfessionCard(val professionCard: ProfessionCard) : AppAction()
    data class EditFillProfessionCard(val professionCard: ProfessionCard) : AppAction()
    data object GetSalary : AppAction()
    data class AddBusiness(val business: Business) : AppAction()
    data object Exit : AppAction()
}

sealed class AppSideEffect : Effect {
    object Exit : AppSideEffect()
}

class AppStore constructor() : Store<AppState, AppAction, AppSideEffect>,
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    private val state = MutableStateFlow(AppState())
    private val sideEffect = MutableSharedFlow<AppSideEffect>()

    override fun observeState(): StateFlow<AppState> = state

    override fun observeSideEffect(): Flow<AppSideEffect> = sideEffect

    override fun dispatch(action: AppAction) {
        val oldState = state.value
        val newState = when (action) {
            is AppAction.FillProfessionCard -> {
                AppState(
                    professionCard = action.professionCard,
                    business = listOf(
                        Business(
                            type = BusinessType.WORK,
                            name = action.professionCard.profession,
                            price = 0,
                            profit = action.professionCard.salary
                        )
                    )
                )
            }

            is AppAction.EditFillProfessionCard -> {
                oldState.copy(professionCard = action.professionCard)
            }

            AppAction.Exit -> {
                oldState
            }

            is AppAction.AddBusiness -> {
                oldState
            }

            AppAction.GetSalary -> {
                oldState
            }
        }
        if (newState != oldState) {
            state.value = newState
        }
    }
}