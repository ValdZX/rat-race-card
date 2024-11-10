package ua.vald_zx.game.rat.race.card.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.components.NumberTextField
import ua.vald_zx.game.rat.race.card.logic.AppAction
import ua.vald_zx.game.rat.race.card.store

class SideExpensesScreen : Screen {
    @Composable
    override fun Content() {
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        BottomSheetContainer {
            val inputAmount = remember { mutableStateOf(TextFieldValue("")) }
            val amount = inputAmount.value.text
            NumberTextField(
                input = inputAmount,
                inputLabel = "Сума витрат",
            )
            ElevatedButton(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    bottomSheetNavigator.hide()
                    store.dispatch(AppAction.SideExpenses(amount = amount.toLong()))
                },
                enabled = amount.isNotEmpty(),
                content = {
                    Text("Віддати")
                }
            )
        }
    }
}