package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import ua.vald_zx.game.rat.race.card.beans.BusinessType
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.components.RainbowButton
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.raceRate2store
import ua.vald_zx.game.rat.race.card.theme.AppTheme

class AllActionsScreen() : Screen {
    @Composable
    override fun Content() = ActionsScreen {
        val state by raceRate2store.observeState().collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        RainbowButton("Отримати дохід") {
            bottomSheetNavigator.hide()
            raceRate2store.dispatch(RatRace2CardAction.GetSalary)
        }
        Button("Сторонній дохід", AppTheme.colors.positive) {
            bottomSheetNavigator.replace(SideProfitScreen())
        }
        Button("Покласти на депозит", AppTheme.colors.positive) {
            bottomSheetNavigator.replace(ToDepositScreen())
        }
        Button("Погасити кредит", AppTheme.colors.positive) {
            bottomSheetNavigator.replace(RepayCreditScreen())
        }
        Button("Сторонні витрати", AppTheme.colors.negative) {
            bottomSheetNavigator.replace(SideExpensesScreen())
        }
        Button("Зняти з депозиту", AppTheme.colors.negative) {
            bottomSheetNavigator.replace(FromDepositScreen())
        }
        Button("Взяти кредит", AppTheme.colors.negative) {
            bottomSheetNavigator.replace(GetLoanScreen())
        }
        if (state.business.any { it.type == BusinessType.WORK }) {
            Button("Звільнитися", AppTheme.colors.negative) {
                raceRate2store.dispatch(RatRace2CardAction.Fired)
            }
        }
        Button("Купити бізнес", AppTheme.colors.action) {
            bottomSheetNavigator.replace(BuyBusinessScreen())
        }
        Button("Купити акції", AppTheme.colors.action) {
            bottomSheetNavigator.replace(BuySharesScreen())
        }
        if (state.sharesList.isNotEmpty()) {
            Button("Продати акції", AppTheme.colors.action) {
                bottomSheetNavigator.replace(SellSharesScreen())
            }
        }
        if (state.config.hasFunds) {
            Button("Інвестувати", AppTheme.colors.funds) {
                bottomSheetNavigator.replace(BuyFundScreen())
            }
        }
        Button("Покупки", AppTheme.colors.buy) {
            bottomSheetNavigator.replace(BuyScreen())
        }
        Button("Сімейний стан", AppTheme.colors.family) {
            bottomSheetNavigator.replace(MarriageScreen())
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
