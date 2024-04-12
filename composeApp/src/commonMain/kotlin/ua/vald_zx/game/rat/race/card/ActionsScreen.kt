package ua.vald_zx.game.rat.race.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
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
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ElevatedButton(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    bottomSheetNavigator.hide()
                    store.dispatch(AppAction.GetSalary)
                },
                content = {
                    Text("Отримати зарплатню")
                }
            )
            ElevatedButton(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    bottomSheetNavigator.replace(SideProfitScreen())
                },
                content = {
                    Text("Сторонніх дохід")
                }
            )
            ElevatedButton(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    bottomSheetNavigator.replace(ToDepositScreen())
                },
                content = {
                    Text("Покласти на депозит")
                }
            )
            ElevatedButton(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    bottomSheetNavigator.replace(FromDepositScreen())
                },
                content = {
                    Text("Зняти з депозиту")
                }
            )
            ElevatedButton(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    bottomSheetNavigator.replace(GetLoanScreen())
                },
                content = {
                    Text("Взяти в кредит")
                }
            )
            ElevatedButton(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    bottomSheetNavigator.replace(RepayCreditScreen())
                },
                content = {
                    Text("Погасити кредит")
                }
            )
            ElevatedButton(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    bottomSheetNavigator.replace(SideExpensesScreen())
                },
                content = {
                    Text("Сторонні витрати")
                }
            )
            ElevatedButton(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    bottomSheetNavigator.replace(BuyBusinessScreen())
                },
                content = {
                    Text("Купити бізнес")
                }
            )
            if (state.business.any { it.type == BusinessType.SMALL }) {
                ElevatedButton(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        .widthIn(min = 200.dp),
                    onClick = {
                        bottomSheetNavigator.replace(ExtendBusinessScreen())
                    },
                    content = {
                        Text("Розширення малого бізнесу")
                    }
                )
            }
            ElevatedButton(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    bottomSheetNavigator.replace(BuySharesScreen())
                },
                content = {
                    Text("Купити акції")
                }
            )
            if (state.sharesList.isNotEmpty()) {
                ElevatedButton(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        .widthIn(min = 200.dp),
                    onClick = {
                        bottomSheetNavigator.replace(SellSharesScreen())
                    },
                    content = {
                        Text("Продати акції")
                    }
                )
            }
            ElevatedButton(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    bottomSheetNavigator.replace(FillProfessionCardBottomSheetScreen())
                },
                content = {
                    Text("Редагувати картку професії")
                }
            )
            var resetDialog by remember { mutableStateOf(false) }
            ElevatedButton(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    resetDialog = true
                },
                content = {
                    Text("Нова гра")
                }
            )
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