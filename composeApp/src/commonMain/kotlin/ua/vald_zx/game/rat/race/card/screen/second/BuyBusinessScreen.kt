package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import ua.vald_zx.game.rat.race.card.beans.Business
import ua.vald_zx.game.rat.race.card.beans.BusinessType
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.components.NumberTextField
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.raceRate2store

class BuyBusinessScreen() : Screen {
    @Composable
    override fun Content() {
        val state by raceRate2store.observeState().collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        BottomSheetContainer {
            var businessType by remember {
                val type = raceRate2store.observeState().value.business.firstOrNull()?.type
                mutableStateOf(if (type == null || type == BusinessType.WORK) BusinessType.SMALL else type)
            }
            var businessName by remember { mutableStateOf("") }
            val inputBusinessPrise = remember { mutableStateOf(TextFieldValue("")) }
            val inputBusinessProfit = remember { mutableStateOf(TextFieldValue("")) }
            val businessPrise = inputBusinessPrise.value.text
            val businessProfit = inputBusinessProfit.value.text
            val noBusiness = state.business.isEmpty()
            val hasWork = state.business.any { it.type == BusinessType.WORK }
            val hasSmall = state.business.any { it.type == BusinessType.SMALL }
            val hasMedium = state.business.any { it.type == BusinessType.MEDIUM }
            val hasLarge = state.business.any { it.type == BusinessType.LARGE }
            val hasCorruption = state.business.any { it.type == BusinessType.CORRUPTION }
            if (noBusiness || !hasMedium && !hasLarge && !hasCorruption) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    RadioButton(
                        selected = businessType == BusinessType.SMALL,
                        onClick = { businessType = BusinessType.SMALL }
                    )
                    Text("Малий бізнес")
                }
            }
            if (noBusiness || !hasWork) {
                if (noBusiness || (hasSmall || hasMedium) && !hasLarge && !hasCorruption) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        RadioButton(
                            selected = businessType == BusinessType.MEDIUM,
                            onClick = { businessType = BusinessType.MEDIUM }
                        )
                        Text("Середній бізнес")
                    }
                }
                if (noBusiness || hasMedium || hasLarge || hasCorruption) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        RadioButton(
                            selected = businessType == BusinessType.LARGE,
                            onClick = { businessType = BusinessType.LARGE }
                        )
                        Text("Крупний бізнес")
                    }
                }
                if (noBusiness || hasLarge) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        RadioButton(
                            selected = businessType == BusinessType.CORRUPTION,
                            onClick = { businessType = BusinessType.CORRUPTION }
                        )
                        Text("Корупційний бізнес")
                    }
                }
            }
            val focusManager = LocalFocusManager.current
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Назва бізнеса") },
                value = businessName,
                onValueChange = { businessName = it },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
            )
            NumberTextField(
                input = inputBusinessPrise,
                inputLabel = "Ціна бізнеса",
            )
            NumberTextField(
                input = inputBusinessProfit,
                inputLabel = "Дохід бізнеса",
            )
            ElevatedButton(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    bottomSheetNavigator.hide()
                    raceRate2store.dispatch(
                        RatRace2CardAction.BuyBusiness(
                            Business(
                                type = businessType,
                                name = businessName.trim(),
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