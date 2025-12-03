package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import ua.vald_zx.game.rat.race.card.beans.Shares
import ua.vald_zx.game.rat.race.card.beans.SharesType
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.components.NumberTextField
import ua.vald_zx.game.rat.race.card.label
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.raceRate2store
import ua.vald_zx.game.rat.race.card.shared.emptyIfZero
import ua.vald_zx.game.rat.race.card.splitDecimal

class BuySharesScreen(private val shares: Shares = Shares(SharesType.SCT, 0, 0)) : Screen {
    @Composable
    override fun Content() {
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        BottomSheetContainer {
            var type by remember { mutableStateOf(shares.type) }
            val inputCount = remember { mutableStateOf(TextFieldValue(shares.count.emptyIfZero())) }
            val inputPrice = remember { mutableStateOf(TextFieldValue(shares.buyPrice.emptyIfZero())) }
            val count = inputCount.value.text
            val price = inputPrice.value.text
            SharesType.entries.forEach { entry ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    RadioButton(type == entry, onClick = {
                        type = entry
                    })
                    Text(entry.label())
                }
            }
            Text(
                "Сумарно: ${((count.toLongOrNull() ?: 0) * (price.toLongOrNull() ?: 0)).splitDecimal()}",
                style = MaterialTheme.typography.titleSmall
            )
            NumberTextField(
                input = inputPrice,
                inputLabel = "Ціна покупки",
            )
            NumberTextField(
                input = inputCount,
                inputLabel = "Кількість",
            )
            ElevatedButton(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    bottomSheetNavigator.hide()
                    raceRate2store.dispatch(
                        RatRace2CardAction.BuyShares(
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