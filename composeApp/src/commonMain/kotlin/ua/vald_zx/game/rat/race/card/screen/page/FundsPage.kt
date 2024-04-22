package ua.vald_zx.game.rat.race.card.screen.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import ua.vald_zx.game.rat.race.card.components.SDetailsField
import ua.vald_zx.game.rat.race.card.logic.AppAction
import ua.vald_zx.game.rat.race.card.logic.AppState
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Plus
import ua.vald_zx.game.rat.race.card.resource.images.Stars
import ua.vald_zx.game.rat.race.card.screen.BuyFundScreen
import ua.vald_zx.game.rat.race.card.screen.SellFundScreen
import ua.vald_zx.game.rat.race.card.store

@Composable
fun FundsPage(state: AppState) {
    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.funds) { fund ->
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SDetailsField(
                            name = "Фонд на",
                            value = "${fund.rate}%",
                            modifier = Modifier.weight(1f)
                        )
                        SDetailsField(
                            name = "Сума",
                            value = fund.amount.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { bottomSheetNavigator.show(SellFundScreen(fund)) }) {
                            Text("Зняти")
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
        var capitalizationConfirm by remember { mutableStateOf(false) }
        var capitalizationStarConfirm by remember { mutableStateOf(false) }
        Row {
            ElevatedButton(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                onClick = { bottomSheetNavigator.show(BuyFundScreen()) },
                content = { Icon(Images.Plus, contentDescription = null) }
            )
            ElevatedButton(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).weight(1f),
                onClick = { capitalizationConfirm = true },
                content = { Text("Капіталізувати") }
            )
            ElevatedButton(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                onClick = { capitalizationStarConfirm = true },
                content = { Icon(Images.Stars, contentDescription = null) }
            )
        }
        if (capitalizationConfirm) {
            AlertDialog(
                title = { Text(text = "Капіталізація") },
                text = { Text(text = "Сума капіталізації: ${state.capitalization()}") },
                onDismissRequest = { capitalizationConfirm = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            store.dispatch(AppAction.CapitalizeFunds)
                            capitalizationConfirm = false
                        }
                    ) { Text("Капіталізувати") }
                },
                dismissButton = {
                    TextButton(onClick = { capitalizationConfirm = false }) { Text("Відміна") }
                }
            )
        }
        if (capitalizationStarConfirm) {
            AlertDialog(
                title = { Text(text = "Капіталізація") },
                text = { Text(text = "Сума капіталізації(${state.config.fundStartRate}%): ${state.capitalizationStart()}") },
                onDismissRequest = { capitalizationStarConfirm = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            store.dispatch(AppAction.CapitalizeStarsFunds)
                            capitalizationStarConfirm = false
                        }
                    ) { Text("Капіталізувати") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        capitalizationStarConfirm = false
                    }) { Text("Відміна") }
                }
            )
        }
    }
}