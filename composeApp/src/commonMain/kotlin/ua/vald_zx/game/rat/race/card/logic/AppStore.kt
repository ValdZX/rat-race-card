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
    val cash: Long = 0,
    val deposit: Long = 0,
    val loan: Long = 0,
    val business: List<Business> = emptyList(),
    val isMarried: Boolean = false,
    val babies: Long = 0,
    val cars: Long = 0,
    val apartment: Long = 0,
    val cottage: Long = 0,
    val yacht: Long = 0,
    val flight: Long = 0,
    val sharesList: List<Shares> = emptyList(),
) : State {

    fun activeProfit(): Long {
        return business.sumOf { it.profit + it.extentions.sum() }
    }

    fun passiveProfit(): Long {
        return (deposit * 0.02).toLong()
    }

    fun totalProfit(): Long {
        return activeProfit() + passiveProfit()
    }

    fun creditExpenses(): Long {
        return loan / 10
    }

    fun totalExpenses(): Long {
        var totalExpenses = 0L
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

    fun cashFlow(): Long {
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

    fun sharesCount(entry: SharesType): Long {
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
    data class SellBusiness(val business: Business, val amount: Long) : AppAction()
    data class DismissalConfirmed(val business: Business) : AppAction()
    data class SellingAllBusinessConfirmed(val business: Business) : AppAction()
    data class ExtendBusiness(val amount: Long) : AppAction()
    data class SideProfit(val amount: Long) : AppAction()
    data class SideExpenses(val amount: Long) : AppAction()
    data class GetLoan(val amount: Long) : AppAction()
    data class RepayLoan(val amount: Long) : AppAction()
    data class ToDeposit(val amount: Long) : AppAction()
    data class FromDeposit(val amount: Long) : AppAction()
    data class BuyCar(val price: Long) : AppAction()
    data class BuyApartment(val price: Long) : AppAction()
    data class BuyCottage(val price: Long) : AppAction()
    data class BuyYacht(val price: Long) : AppAction()
    data class BuyFlight(val price: Long) : AppAction()
    data class BuyShares(val shares: Shares) : AppAction()
    data class SellShares(val type: SharesType, val count: Long, val sellPrice: Long) : AppAction()
    data class UpdateFamily(val isMarried: Boolean, val babies: Long) : AppAction()
}

sealed class AppSideEffect : Effect {
    data class SharesToExpensive(val shares: Shares) : AppSideEffect()
    data class ConfirmDismissal(val business: Business) : AppSideEffect()
    data class ConfirmSellingAllBusiness(val business: Business) : AppSideEffect()
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

            is AppAction.SideProfit -> {
                oldState.copy(cash = oldState.cash + action.amount)
            }

            is AppAction.SideExpenses -> {
                oldState.copy(cash = oldState.cash - action.amount)
            }

            is AppAction.BuyBusiness -> {
                val currentBusiness = oldState.business
                if (action.business.type == BusinessType.SMALL && currentBusiness.any { it.type == BusinessType.WORK } && currentBusiness.count { it.type == BusinessType.SMALL } == 1) {
                    launch { sideEffect.emit(AppSideEffect.ConfirmDismissal(action.business)) }
                    oldState
                } else if (currentBusiness.isNotEmpty() && currentBusiness.first().type != action.business.type && !currentBusiness.any { it.type == BusinessType.WORK }) {
                    launch { sideEffect.emit(AppSideEffect.ConfirmSellingAllBusiness(action.business)) }
                    oldState
                } else oldState.copy(
                    cash = oldState.cash - action.business.price,
                    business = currentBusiness + action.business
                )
            }

            is AppAction.SellBusiness -> {
                val business = oldState.business.toMutableList()
                business.remove(action.business)
                oldState.copy(business = business, cash = oldState.cash + action.amount)
            }

            is AppAction.ExtendBusiness -> {
                val business = oldState.business.last()
                val extended = business.copy(extentions = business.extentions + action.amount)
                oldState.copy(business = oldState.business.replace(business, extended))
            }

            is AppAction.DismissalConfirmed -> {
                val business =
                    oldState.business.filter { it.type != BusinessType.WORK } + action.business
                oldState.copy(cash = oldState.cash - action.business.price, business = business)
            }

            is AppAction.SellingAllBusinessConfirmed -> {
                oldState.copy(
                    business = listOf(action.business),
                    cash = oldState.cash + oldState.business.sumOf { it.price } - action.business.price)
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
                while (needToSell != 0L && index < sharesByType.size) {
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

            is AppAction.RepayLoan -> {
                oldState.copy(
                    cash = oldState.cash - action.amount,
                    loan = oldState.loan - action.amount
                )
            }

            is AppAction.GetLoan -> {
                oldState.copy(
                    cash = oldState.cash + action.amount,
                    loan = oldState.loan + action.amount
                )
            }

            is AppAction.FromDeposit -> {
                oldState.copy(
                    cash = oldState.cash + action.amount,
                    deposit = oldState.deposit - action.amount
                )
            }

            is AppAction.ToDeposit -> {
                oldState.copy(
                    cash = oldState.cash - action.amount,
                    deposit = oldState.deposit + action.amount
                )
            }

            is AppAction.UpdateFamily -> {
                oldState.copy(isMarried = action.isMarried, babies = action.babies)
            }

            is AppAction.BuyApartment -> {
                oldState.copy(
                    cash = oldState.cash - action.price,
                    apartment = oldState.apartment + 1
                )
            }

            is AppAction.BuyCar -> {
                oldState.copy(
                    cash = oldState.cash - action.price,
                    cars = oldState.cars + 1
                )
            }

            is AppAction.BuyCottage -> {
                oldState.copy(
                    cash = oldState.cash - action.price,
                    cottage = oldState.cottage + 1
                )
            }

            is AppAction.BuyFlight -> {
                oldState.copy(
                    cash = oldState.cash - action.price,
                    flight = oldState.flight + 1
                )
            }

            is AppAction.BuyYacht -> {
                oldState.copy(
                    cash = oldState.cash - action.price,
                    yacht = oldState.yacht + 1
                )
            }
        }
        if (newState != oldState) {
            state.value = newState
            launch { kStore.set(newState) }
        }
    }
}