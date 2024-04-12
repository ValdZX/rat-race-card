package ua.vald_zx.game.rat.race.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import ua.vald_zx.game.rat.race.card.logic.AppAction

class ActionsScreen() : Screen {
    @Composable
    override fun Content() {
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
                    bottomSheetNavigator.replace(BuyBusinessScreen())
                },
                content = {
                    Text("Купити бізнес")
                }
            )
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
            ElevatedButton(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    navigator?.replaceAll(FillProfessionCardScreen())
                },
                content = {
                    Text("Почати з початку")
                }
            )
        }
    }
}