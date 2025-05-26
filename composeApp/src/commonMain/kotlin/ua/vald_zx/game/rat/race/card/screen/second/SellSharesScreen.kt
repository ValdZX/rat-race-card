package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.components.NumberTextField
import ua.vald_zx.game.rat.race.card.label
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.splitDecimal
import ua.vald_zx.game.rat.race.card.raceRate2store

class SellSharesScreen : Screen {
    @Composable
    override fun Content() {
        val state by raceRate2store.observeState().collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        BottomSheetContainer {
            val existsShares = state.existsShares()
            var type by remember { mutableStateOf(existsShares.first()) }
            val inputCount = remember {
                mutableStateOf(
                    TextFieldValue(
                        state.sharesCount(existsShares.first()).toString()
                    )
                )
            }
            val inputPrice = remember { mutableStateOf(TextFieldValue("")) }
            existsShares.forEach { entry ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    RadioButton(type == entry, onClick = {
                        inputCount.value =
                            inputCount.value.copy(text = state.sharesCount(entry).toString())
                        type = entry
                    })
                    Text("${entry.label()}: ${state.sharesCount(entry)}")
                }
            }
            val count = inputCount.value.text
            val price = inputPrice.value.text
            Text(
                "Сумарно: ${((count.toLongOrNull() ?: 0) * (price.toLongOrNull() ?: 0)).splitDecimal()}",
                style = MaterialTheme.typography.titleSmall
            )
            NumberTextField(
                input = inputCount,
                inputLabel = "Кількість",
            )
            NumberTextField(
                input = inputPrice,
                inputLabel = "Ціна продажу",
            )
            ElevatedButton(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    bottomSheetNavigator.hide()
                    raceRate2store.dispatch(
                        RatRace2CardAction.SellShares(
                            type = type,
                            count = count.toLong(),
                            sellPrice = price.toLong(),
                        )
                    )
                },
                enabled = count.isNotEmpty()
                        && price.isNotEmpty()
                        && count.toLong() <= state.sharesCount(type),
                content = {
                    Text("Продати")
                }
            )
        }
    }
}