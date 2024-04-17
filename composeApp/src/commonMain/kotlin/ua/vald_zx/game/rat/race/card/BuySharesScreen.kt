package ua.vald_zx.game.rat.race.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import ua.vald_zx.game.rat.race.card.beans.Shares
import ua.vald_zx.game.rat.race.card.beans.SharesType
import ua.vald_zx.game.rat.race.card.logic.AppAction

class BuySharesScreen(private val shares: Shares = Shares(SharesType.SCT, 0, 0)) : Screen {
    @Composable
    override fun Content() {
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        BottomSheetContainer {
            var type by remember { mutableStateOf(shares.type) }
            var count by remember { mutableStateOf(shares.count.emptyIfZero()) }
            var price by remember { mutableStateOf(shares.buyPrice.emptyIfZero()) }
            SharesType.entries.forEach { entry ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    RadioButton(type == entry, onClick = {
                        type = entry
                    })
                    Text(entry.name)
                }
            }
            Text(
                "Сумарно: ${((count.toLongOrNull() ?: 0) * (price.toLongOrNull() ?: 0)).splitDecimal()}",
                style = MaterialTheme.typography.titleSmall
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Кількість") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                value = count,
                onValueChange = { count = it.getDigits() }
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Ціна покупки") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                value = price,
                onValueChange = { price = it.getDigits() }
            )
            ElevatedButton(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    bottomSheetNavigator.hide()
                    store.dispatch(
                        AppAction.BuyShares(
                            Shares(
                                type = type,
                                count = count.toLong(),
                                buyPrice = price.toLong(),
                            )
                        )
                    )
                },
                enabled = count.isNotEmpty() && price.isNotEmpty(),
                content = {
                    Text("Купити")
                }
            )
        }
    }
}