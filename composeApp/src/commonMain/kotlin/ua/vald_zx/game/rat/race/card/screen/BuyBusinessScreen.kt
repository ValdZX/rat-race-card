package ua.vald_zx.game.rat.race.card.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import ua.vald_zx.game.rat.race.card.beans.Business
import ua.vald_zx.game.rat.race.card.beans.BusinessType
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.getDigits
import ua.vald_zx.game.rat.race.card.logic.AppAction
import ua.vald_zx.game.rat.race.card.store

class BuyBusinessScreen() : Screen {
    @Composable
    override fun Content() {
        val state by store.observeState().collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        BottomSheetContainer {
            var businessType by remember {
                val type = store.observeState().value.business.firstOrNull()?.type
                mutableStateOf(if (type == null || type == BusinessType.WORK) BusinessType.SMALL else type)
            }
            var businessName by remember { mutableStateOf("") }
            var businessPrise by remember { mutableStateOf("") }
            var businessProfit by remember { mutableStateOf("") }
            val noBusiness = state.business.isEmpty()
            val hasWork = state.business.any { it.type == BusinessType.WORK }
            val hasSmall = state.business.any { it.type == BusinessType.SMALL }
            val hasMedium = state.business.any { it.type == BusinessType.MEDIUM }
            val hasLarge = state.business.any { it.type == BusinessType.LARGE }
            if (noBusiness || !hasMedium && !hasLarge) {
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
            }
            if (noBusiness || !hasWork) {
                if (noBusiness || (hasSmall || hasMedium) && !hasLarge) {
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
                }
                if (noBusiness || hasMedium || hasLarge) {
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
                }
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
                        AppAction.BuyBusiness(
                            Business(
                                type = businessType,
                                name = businessName,
                                price = businessPrise.toLong(),
                                profit = businessProfit.toLong()
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