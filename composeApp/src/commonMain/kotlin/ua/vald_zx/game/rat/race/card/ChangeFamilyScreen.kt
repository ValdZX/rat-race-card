package ua.vald_zx.game.rat.race.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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

class ChangeFamilyScreen : Screen {
    @Composable
    override fun Content() {
        val state by store.observeState().collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        BottomSheetContainer {
            var isMarried by remember { mutableStateOf(state.isMarried) }
            var babies by remember { mutableStateOf(state.babies.toString()) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Шлюб")
                Switch(isMarried, onCheckedChange = { isMarried = it })
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Кількість дітей") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                value = babies,
                onValueChange = { babies = it.getDigits() }
            )
            ElevatedButton(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    bottomSheetNavigator.hide()
                    store.dispatch(
                        AppAction.UpdateFamily(
                            isMarried = isMarried,
                            babies = babies.toLong()
                        )
                    )
                },
                enabled = babies.isNotEmpty(),
                content = {
                    Text("Зберегти")
                }
            )
        }
    }
}