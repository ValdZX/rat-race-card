package ua.vald_zx.game.rat.race.card.logic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ua.vald_zx.game.rat.race.card.beans.Business
import ua.vald_zx.game.rat.race.card.beans.BusinessType
import ua.vald_zx.game.rat.race.card.beans.ProfessionCard
import ua.vald_zx.game.rat.race.card.beans.Shares
import ua.vald_zx.game.rat.race.card.beans.SharesType
import ua.vald_zx.game.rat.race.card.kStore
import ua.vald_zx.game.rat.race.card.replace

@Serializable
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

    fun existsShares(): Set<SharesType> {
        return sharesList.map { it.type }.toSet()
    }

    fun sharesCount(entry: SharesType): Int {
        return sharesList.filter { it.type == entry }.sumOf { it.count }
    }
}

sealed class AppAction : Action {
    data class LoadState(val state: AppState) : AppAction()
    data class FillProfessionCard(val professionCard: ProfessionCard) : AppAction()
    data class EditFillProfessionCard(val professionCard: ProfessionCard) : AppAction()
    data object GetSalary : AppAction()
    data object GetSalaryApproved : AppAction()
    data class BuyBusiness(val business: Business) : AppAction()
    data class BuyShares(val shares: Shares) : AppAction()
    data class SellShares(val type: SharesType, val count: Int, val sellPrice: Int) : AppAction()
    data object Exit : AppAction()
}

sealed class AppSideEffect : Effect {
    data class SharesToExpensive(val shares: Shares) : AppSideEffect()
    data object ShowSalaryApprove : AppSideEffect()
}

class AppStore : Store<AppState, AppAction, AppSideEffect>,
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    private val state = MutableStateFlow(AppState())
    private val sideEffect = MutableSharedFlow<AppSideEffect>()

    override fun observeState(): StateFlow<AppState> = state

    override fun observeSideEffect(): Flow<AppSideEffect> = sideEffect

    override fun dispatch(action: AppAction) {
        val oldState = state.value
        val newState = when (action) {
            is AppAction.LoadState -> action.state
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

            is AppAction.BuyBusiness -> {
                oldState
            }

            is AppAction.BuyShares -> {
                if (oldState.cash < action.shares.price) {
                    launch { sideEffect.emit(AppSideEffect.SharesToExpensive(action.shares)) }
                    oldState
                } else {
                    val cash = oldState.cash - action.shares.price
                    val sharesList = oldState.sharesList + action.shares
                    oldState.copy(cash = cash, sharesList = sharesList)
                }
            }

            AppAction.GetSalary -> {
                launch { sideEffect.emit(AppSideEffect.ShowSalaryApprove) }
                oldState
            }

            AppAction.GetSalaryApproved -> {
                oldState.copy(cash = oldState.cash + oldState.cashFlow())
            }

            is AppAction.SellShares -> {
                var resultList = oldState.sharesList.toMutableList()
                val sharesByType = resultList.filter { it.type == action.type }
                var needToSell = action.count
                var index = 0
                while (needToSell != 0 && index < sharesByType.size) {
                    val shares = sharesByType[index]
                    if (needToSell < shares.count) {
                        resultList = resultList.replace(
                            shares,
                            shares.copy(count = shares.count - needToSell)
                        ).toMutableList()
                        break
                    } else if (needToSell == shares.count) {
                        resultList.remove(shares)
                        break
                    } else {
                        resultList.remove(shares)
                        needToSell -= shares.count
                        index += 1
                    }
                }
                oldState.copy(
                    cash = oldState.cash + (action.count * action.sellPrice),
                    sharesList = resultList
                )
            }
        }
        if (newState != oldState) {
            state.value = newState
            launch { kStore.set(newState) }
        }
    }
}