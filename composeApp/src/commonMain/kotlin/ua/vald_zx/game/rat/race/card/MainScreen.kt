package ua.vald_zx.game.rat.race.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.vectorResource
import rat_race_card.composeapp.generated.resources.Res
import rat_race_card.composeapp.generated.resources.ic_dark_mode
import rat_race_card.composeapp.generated.resources.ic_light_mode
import ua.vald_zx.game.rat.race.card.components.SmoothRainbowText
import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark

class MainScreen : Screen {
    @Composable
    override fun Content() {
        val state by store.observeState().collectAsState()
        BottomSheetNavigator {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(16.dp),
            ) {
                var isDark by LocalThemeIsDark.current
                val icon = remember(isDark) {
                    if (isDark) Res.drawable.ic_light_mode
                    else Res.drawable.ic_dark_mode
                }
                IconButton(
                    modifier = Modifier.align(Alignment.TopEnd),
                    onClick = { isDark = !isDark },
                    content = {
                        Icon(vectorResource(icon), contentDescription = null)
                    }
                )
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.status(), style = MaterialTheme.typography.headlineMedium)
                    PositiveField("Активний прибуток", state.activeProfit().toString())
                    PositiveField("Пасивний прибуток", state.passiveProfit().toString())
                    PositiveField("Загальний прибуток", state.totalProfit().toString())
                    NegativeField("Витрати по кредиту", state.creditExpenses().toString())
                    NegativeField("Загальні витрати", state.totalExpenses().toString())
                    CashFlowField("Cash Flow", state.cashFlow().toString())
                    BalanceField("Готівка", state.cash.toString())
                    BalanceField("Депозит", state.deposit.toString())
                    BalanceField("Кредит", state.loan.toString())
                    val bottomSheetNavigator = LocalBottomSheetNavigator.current
                    ElevatedButton(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            .widthIn(min = 200.dp),
                        onClick = {
                            bottomSheetNavigator.show(ActionsScreen())
                        },
                        content = {
                            Text("Дія")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ColumnScope.PositiveField(name: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(name, color = MaterialTheme.colorScheme.primary)
        Text(value.splitDecimal(), fontSize = 16.sp)
    }
    HorizontalDivider()
}

@Composable
fun ColumnScope.NegativeField(name: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(name, color = MaterialTheme.colorScheme.tertiary)
        Text(value.splitDecimal(), fontSize = 16.sp)
    }
    HorizontalDivider()
}

@Composable
fun ColumnScope.CashFlowField(name: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        SmoothRainbowText(name, style = LocalTextStyle.current.copy(fontSize = 20.sp))
        Text(value.splitDecimal(), fontSize = 20.sp)
    }
    HorizontalDivider()
}

@Composable
fun ColumnScope.BalanceField(name: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(name)
        Text(value.splitDecimal(), fontSize = 16.sp)
    }
    HorizontalDivider()
}