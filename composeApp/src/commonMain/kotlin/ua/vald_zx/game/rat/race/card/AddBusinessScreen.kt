package ua.vald_zx.game.rat.race.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import ua.vald_zx.game.rat.race.card.beans.Business
import ua.vald_zx.game.rat.race.card.beans.BusinessType
import ua.vald_zx.game.rat.race.card.logic.AppAction

class AddBusinessScreen() : Screen {
    @Composable
    override fun Content() {
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            var businessType by remember {
                val type = store.observeState().value.business.firstOrNull()?.type
                mutableStateOf(if (type == null || type == BusinessType.WORK) BusinessType.SMALL else type)
            }
            var businessName by remember { mutableStateOf("") }
            var businessPrise by remember { mutableStateOf("") }
            var businessProfit by remember { mutableStateOf("") }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                RadioButton(businessType == BusinessType.SMALL, onClick = {
                    businessType = BusinessType.SMALL
                })
                Text("Малий бізнес")
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                RadioButton(businessType == BusinessType.MEDIUM, onClick = {
                    businessType = BusinessType.MEDIUM
                })
                Text("Середній бізнес")
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                RadioButton(businessType == BusinessType.LARGE, onClick = {
                    businessType = BusinessType.LARGE
                })
                Text("Крупний бізнес")
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Назва бізнеса") },
                value = businessName,
                onValueChange = { businessName = it }
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Ціна бізнеса") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                value = businessPrise,
                onValueChange = { businessPrise = it.getDigits() }
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Дохід бізнеса") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                value = businessProfit,
                onValueChange = { businessProfit = it.getDigits() }
            )
            ElevatedButton(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    bottomSheetNavigator.hide()
                    store.dispatch(
                        AppAction.AddBusiness(
                            Business(
                                type = businessType,
                                name = businessName,
                                price = businessPrise.toInt(),
                                profit = businessProfit.toInt()
                            )
                        )
                    )
                },
                enabled = businessName.isNotEmpty() && businessPrise.isNotEmpty() && businessProfit.isNotEmpty(),
                content = {
                    Text("Купити")
                }
            )
        }
    }
}