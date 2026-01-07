package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.beans.BusinessType
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.components.RainbowButton
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardStore
import ua.vald_zx.game.rat.race.card.theme.AppTheme

class AllActionsScreen() : Screen {
    @Composable
    override fun Content() = ActionsScreen {
        val raceRate2store = koinInject<RatRace2CardStore>()
        val state by raceRate2store.observeState().collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        RainbowButton(stringResource(Res.string.receive_income)) {
            bottomSheetNavigator.hide()
            raceRate2store.dispatch(RatRace2CardAction.GetSalary)
        }
        Button(stringResource(Res.string.third_party_income), AppTheme.colors.positive) {
            bottomSheetNavigator.replace(SideProfitScreen())
        }
        Button(stringResource(Res.string.deposit_to_deposit), AppTheme.colors.positive) {
            bottomSheetNavigator.replace(ToDepositScreen())
        }
        Button(stringResource(Res.string.repay_loan), AppTheme.colors.positive) {
            bottomSheetNavigator.replace(RepayCreditScreen())
        }
        Button(stringResource(Res.string.third_party_expense), AppTheme.colors.negative) {
            bottomSheetNavigator.replace(SideExpensesScreen())
        }
        Button(stringResource(Res.string.withdraw_deposit), AppTheme.colors.negative) {
            bottomSheetNavigator.replace(FromDepositScreen())
        }
        Button(stringResource(Res.string.get_loan), AppTheme.colors.negative) {
            bottomSheetNavigator.replace(GetLoanScreen())
        }
        if (state.business.any { it.type == BusinessType.WORK }) {
            Button(stringResource(Res.string.resign), AppTheme.colors.negative) {
                raceRate2store.dispatch(RatRace2CardAction.Fired)
            }
        }
        Button(stringResource(Res.string.buy_business), AppTheme.colors.action) {
            bottomSheetNavigator.replace(BuyBusinessScreen())
        }
        Button(stringResource(Res.string.buy_shares), AppTheme.colors.action) {
            bottomSheetNavigator.replace(BuySharesScreen())
        }
        if (state.sharesList.isNotEmpty()) {
            Button(stringResource(Res.string.sell_shares), AppTheme.colors.action) {
                bottomSheetNavigator.replace(SellSharesScreen())
            }
        }
        if (state.config.hasFunds) {
            Button(stringResource(Res.string.invest), AppTheme.colors.funds) {
                bottomSheetNavigator.replace(BuyFundScreen())
            }
        }
        Button(stringResource(Res.string.purchases), AppTheme.colors.buy) {
            bottomSheetNavigator.replace(BuyScreen())
        }
        Button(stringResource(Res.string.marital_status), AppTheme.colors.family) {
            bottomSheetNavigator.replace(MarriageScreen())
        }
        Button(stringResource(Res.string.exit), AppTheme.colors.negative) {
            navigator.parent?.pop()
        }
    }
}

class CashActionsScreen() : Screen {
    @Composable
    override fun Content() = ActionsScreen {
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        Button("Сторонній дохід", AppTheme.colors.positive) {
            bottomSheetNavigator.replace(SideProfitScreen())
        }
        Button("Сторонні витрати", AppTheme.colors.negative) {
            bottomSheetNavigator.replace(SideExpensesScreen())
        }
    }
}

class DepositActionsScreen() : Screen {
    @Composable
    override fun Content() = ActionsScreen {
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        Button("Покласти на депозит", AppTheme.colors.positive) {
            bottomSheetNavigator.replace(ToDepositScreen())
        }
        Button("Зняти з депозиту", AppTheme.colors.negative) {
            bottomSheetNavigator.replace(FromDepositScreen())
        }
    }
}

class LoanActionsScreen() : Screen {
    @Composable
    override fun Content() = ActionsScreen {
        val raceRate2store = koinInject<RatRace2CardStore>()
        val state by raceRate2store.observeState().collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        if (state.loan > 0) {
            Button("Погасити кредит", AppTheme.colors.positive) {
                bottomSheetNavigator.replace(RepayCreditScreen())
            }
        }
        Button("Взяти кредит", AppTheme.colors.negative) {
            bottomSheetNavigator.replace(GetLoanScreen())
        }
    }
}

@Composable
fun ActionsScreen(
    content: @Composable ColumnScope.() -> Unit
) = AppTheme {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .navigationBarsPadding()
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}
