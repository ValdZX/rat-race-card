package ua.vald_zx.game.rat.race.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import ua.vald_zx.game.rat.race.card.beans.BusinessType
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.components.RainbowButton
import ua.vald_zx.game.rat.race.card.logic.AppAction

class ActionsScreen() : Screen {
    @Composable
    override fun Content() {
        val state by store.observeState().collectAsState()
        val navigator = LocalNavigator.current?.parent
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .navigationBarsPadding()
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RainbowButton("Отримати дохід") {
                bottomSheetNavigator.hide()
                store.dispatch(AppAction.GetSalary)
            }
            Button("Сторонній дохід") {
                bottomSheetNavigator.replace(SideProfitScreen())
            }
            Button("Сторонні витрати", containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                bottomSheetNavigator.replace(SideExpensesScreen())
            }
            Button("Покласти на депозит") {
                bottomSheetNavigator.replace(ToDepositScreen())
            }
            Button("Зняти з депозиту", containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                bottomSheetNavigator.replace(FromDepositScreen())
            }
            Button("Погасити кредит") {
                bottomSheetNavigator.replace(RepayCreditScreen())
            }
            Button("Взяти в кредит", containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                bottomSheetNavigator.replace(GetLoanScreen())
            }
            Button("Купити бізнес", containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                bottomSheetNavigator.replace(BuyBusinessScreen())
            }
            if (state.business.any { it.type == BusinessType.SMALL }) {
                Button("Розширення малого бізнесу") {
                    bottomSheetNavigator.replace(ExtendBusinessScreen())
                }
            }
            Button("Купити акції", containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                bottomSheetNavigator.replace(BuySharesScreen())
            }
            if (state.sharesList.isNotEmpty()) {
                Button("Продати акції") {
                    bottomSheetNavigator.replace(SellSharesScreen())
                }
            }
            Button("Покупки") {
                bottomSheetNavigator.replace(BuyScreen())
            }
            Button("Сімейний стан") {
                bottomSheetNavigator.replace(ChangeFamilyScreen())
            }
            Button("Редагувати картку професії") {
                bottomSheetNavigator.replace(FillProfessionCardBottomSheetScreen())
            }
            var resetDialog by remember { mutableStateOf(false) }
            Button("Нова гра") {
                resetDialog = true
            }
            if (resetDialog) {
                AlertDialog(
                    title = { Text(text = "Точно?") },
                    text = { Text(text = "Всі данні буде затерто.") },
                    onDismissRequest = { resetDialog = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                navigator?.replaceAll(FillProfessionCardScreen())
                                resetDialog = false
                            }
                        ) { Text("Точно") }
                    },
                    dismissButton = {
                        TextButton(onClick = { resetDialog = false }) { Text("Відміна") }
                    }
                )
            }
        }
    }
}