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
import ua.vald_zx.game.rat.race.card.beans.Config
import ua.vald_zx.game.rat.race.card.beans.Fund
import ua.vald_zx.game.rat.race.card.beans.ProfessionCard
import ua.vald_zx.game.rat.race.card.beans.Shares
import ua.vald_zx.game.rat.race.card.beans.SharesType
import ua.vald_zx.game.rat.race.card.kStore
import ua.vald_zx.game.rat.race.card.remove
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
    val funds: List<Fund> = emptyList(),
    val config: Config = Config()
) : State {

    fun balance(): Long {
        return cash + deposit
    }

    fun activeProfit(): Long {
        return business.sumOf { it.profit + it.extentions.sum() }
    }

    fun passiveProfit(): Long {
        return ((deposit / 100.0) * config.depositRate).toLong()
    }

    fun totalProfit(): Long {
        return activeProfit() + passiveProfit()
    }

    fun creditExpenses(): Long {
        return ((loan / 100.0) * config.loadRate).toLong()
    }

    fun totalExpenses(): Long {
        var totalExpenses = 0L
        totalExpenses += professionCard.food
        totalExpenses += professionCard.rent
        totalExpenses += professionCard.cloth
        totalExpenses += professionCard.phone
        totalExpenses += professionCard.transport
        totalExpenses += babies * config.babyCost
        totalExpenses += cars * config.carCost
        totalExpenses += apartment * config.apartmentCost
        totalExpenses += cottage * config.cottageCost
        totalExpenses += yacht * config.yachtCost
        totalExpenses += flight * config.flightCost
        totalExpenses += creditExpenses()
        return totalExpenses
    }

    fun cashFlow(): Long {
        return totalProfit() - totalExpenses()
    }

    fun status(): String {
        return when {
            business.any { it.type == BusinessType.SMALL } -> "${professionCard.profession} - Підприємець"
            business.any { it.type == BusinessType.MEDIUM } -> "${professionCard.profession} - Бізнесмен"
            business.any { it.type == BusinessType.LARGE } -> "${professionCard.profession} - Мільйонер"
            else -> professionCard.profession
        }
    }

    fun existsShares(): Set<SharesType> {
        return sharesList.map { it.type }.toSet()
    }

    fun sharesCount(entry: SharesType): Long {
        return sharesList.filter { it.type == entry }.sumOf { it.count }
    }

    fun capitalization(): Long {
        return funds.sumOf { (it.rate / 100.0) * it.amount }.toLong()
    }

    fun capitalizationStart(): Long {
        return funds.sumOf { (config.fundStartRate / 100.0) * it.amount }.toLong()
    }

    fun fundAmount(): Long {
        return funds.sumOf { it.amount }
    }
}

sealed class AppAction : Action {
    data class LoadState(val state: AppState) : AppAction()
    data class FillProfessionCard(val professionCard: ProfessionCard) : AppAction()
    data class EditFillProfessionCard(val professionCard: ProfessionCard) : AppAction()
    data object GetSalary : AppAction()
    data object GetSalaryApproved : AppAction()
    data class AddFund(val fund: Fund) : AppAction()
    data class FromFund(val fund: Fund, val amount: Long) : AppAction()
    data object CapitalizeFunds : AppAction()
    data object CapitalizeStarsFunds : AppAction()
    data object RandomBusiness : AppAction()
    data object HideAlarm : AppAction()
    data class BuyBusiness(val business: Business) : AppAction()
    data class SellBusiness(val business: Business, val amount: Long) : AppAction()
    data class DismissalConfirmed(val business: Business) : AppAction()
    data class SellingAllBusinessConfirmed(val business: Business) : AppAction()
    data class ExtendBusiness(val amount: Long, val business: Business) : AppAction()
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
    data class UpdateConfig(val config: Config) : AppAction()
    data class SellShares(val type: SharesType, val count: Long, val sellPrice: Long) : AppAction()
    data class UpdateFamily(val isMarried: Boolean, val babies: Long) : AppAction()
}

sealed class AppSideEffect : Effect {
    data class ConfirmDismissal(val business: Business) : AppSideEffect()
    data class ConfirmSellingAllBusiness(val business: Business) : AppSideEffect()
    data class DepositWithdraw(val balance: Long) : AppSideEffect()
    data class LoanAdded(val balance: Long) : AppSideEffect()
    data object AddCash : AppSideEffect()
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
                oldState.plusCash(action.amount)
            }

            is AppAction.SideExpenses -> {
                oldState.minusCash(action.amount)
            }

            AppAction.HideAlarm -> {
                oldState.copy(business = oldState.business.map { it.copy(alarmed = false) })
            }

            AppAction.RandomBusiness -> {
                val random = (0..<oldState.business.size).random()
                val business = oldState.business[random]
                val businessList = oldState.business.map { it.copy(alarmed = business == it) }
                oldState.copy(business = businessList)
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
                    business = currentBusiness + action.business
                ).minusCash(action.business.price)
            }

            is AppAction.SellBusiness -> {
                val business = oldState.business.toMutableList()
                business.remove(action.business)
                oldState.copy(business = business).plusCash(action.amount)
            }

            is AppAction.ExtendBusiness -> {
                val business = action.business
                val extended = business.copy(extentions = business.extentions + action.amount)
                oldState.copy(business = oldState.business.replace(business, extended))
            }

            is AppAction.DismissalConfirmed -> {
                val business =
                    oldState.business.filter { it.type != BusinessType.WORK } + action.business
                oldState.copy(business = business).minusCash(action.business.price)
            }

            is AppAction.SellingAllBusinessConfirmed -> {
                oldState.copy(business = listOf(action.business))
                    .plusCash(oldState.business.sumOf { it.price } - action.business.price)
            }

            is AppAction.BuyShares -> {
                val sharesList = oldState.sharesList + action.shares
                oldState.copy(sharesList = sharesList).minusCash(action.shares.price)
            }

            AppAction.GetSalary -> {
                launch { sideEffect.emit(AppSideEffect.ShowSalaryApprove) }
                oldState
            }

            AppAction.GetSalaryApproved -> {
                oldState.plusCash(oldState.cashFlow())
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
                oldState.copy(sharesList = resultList).plusCash(action.count * action.sellPrice)
            }

            is AppAction.RepayLoan -> {
                oldState.copy(
                    loan = oldState.loan - action.amount
                ).minusCash(action.amount)
            }

            is AppAction.GetLoan -> {
                oldState.copy(loan = oldState.loan + action.amount).plusCash(action.amount)
            }

            is AppAction.FromDeposit -> {
                oldState.copy(deposit = oldState.deposit - action.amount).plusCash(action.amount)
            }

            is AppAction.ToDeposit -> {
                oldState.copy(
                    deposit = oldState.deposit + action.amount
                ).minusCash(action.amount)
            }

            is AppAction.UpdateFamily -> {
                oldState.copy(isMarried = action.isMarried, babies = action.babies)
            }

            is AppAction.BuyApartment -> {
                oldState.copy(
                    apartment = oldState.apartment + 1
                ).minusCash(action.price)
            }

            is AppAction.BuyCar -> {
                oldState.copy(
                    cars = oldState.cars + 1
                ).minusCash(action.price)
            }

            is AppAction.BuyCottage -> {
                oldState.copy(
                    cottage = oldState.cottage + 1
                ).minusCash(action.price)
            }

            is AppAction.BuyFlight -> {
                oldState.copy(
                    flight = oldState.flight + 1
                ).minusCash(action.price)
            }

            is AppAction.BuyYacht -> {
                oldState.copy(
                    yacht = oldState.yacht + 1
                ).minusCash(action.price)
            }

            is AppAction.UpdateConfig -> {
                oldState.copy(config = action.config)
            }

            is AppAction.AddFund -> {
                val currentFund = oldState.funds.find { it.rate == action.fund.rate }
                val funds = if (currentFund != null) {
                    oldState.funds.replace(
                        currentFund,
                        currentFund.copy(amount = currentFund.amount + action.fund.amount)
                    )
                } else {
                    oldState.funds + action.fund
                }
                oldState.copy(funds = funds).minusCash(action.fund.amount)
            }

            is AppAction.FromFund -> {
                if ((action.fund.amount - action.amount) == 0L) {
                    oldState.copy(funds = oldState.funds.remove(action.fund))
                        .plusCash(action.amount)
                } else {
                    val newFund = action.fund.copy(amount = action.fund.amount - action.amount)
                    val funds = oldState.funds.replace(action.fund, newFund)
                    oldState.copy(funds = funds).plusCash(action.amount)
                }
            }

            AppAction.CapitalizeFunds -> {
                val amount = oldState.funds.sumOf { it.amount } + oldState.capitalization()
                val funds = listOf(Fund(rate = oldState.config.fundBaseRate, amount))
                oldState.copy(funds = funds)
            }

            AppAction.CapitalizeStarsFunds -> {
                val amount = oldState.funds.sumOf { it.amount } + oldState.capitalizationStart()
                val funds = listOf(Fund(rate = oldState.config.fundBaseRate, amount))
                oldState.copy(funds = funds)
            }
        }
        if (newState != oldState) {
            state.value = newState
            launch { kStore.set(newState) }
        }
    }

    private fun AppState.plusCash(value: Long): AppState {
        launch { sideEffect.emit(AppSideEffect.AddCash) }
        return copy(cash = cash + value)
    }

    private fun AppState.minusCash(value: Long): AppState {
        if (cash > value) {
            return this.copy(cash = cash - value)
        } else if ((cash + deposit) > value) {
            launch { sideEffect.emit(AppSideEffect.DepositWithdraw(value - cash)) }
            return this.copy(cash = 0, deposit = (deposit + cash) - value)
        } else {
            launch { sideEffect.emit(AppSideEffect.LoanAdded(value - (cash + deposit))) }
            return this.copy(cash = 0, deposit = 0, loan = loan + (value - (deposit + cash)))
        }
    }
}