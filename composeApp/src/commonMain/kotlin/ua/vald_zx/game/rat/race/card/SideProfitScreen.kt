package ua.vald_zx.game.rat.race.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import ua.vald_zx.game.rat.race.card.logic.AppAction

class SideProfitScreen : Screen {
    @Composable
    override fun Content() {
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .imePadding()
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            var amount by remember { mutableStateOf("") }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Сума доходу") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                value = amount,
                onValueChange = { amount = it.getDigits() }
            )
            ElevatedButton(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    bottomSheetNavigator.hide()
                    store.dispatch(AppAction.SideProfit(amount = amount.toLong()))
                },
                enabled = amount.isNotEmpty(),
                content = {
                    Text("Отримати")
                }
            )
        }
    }
}