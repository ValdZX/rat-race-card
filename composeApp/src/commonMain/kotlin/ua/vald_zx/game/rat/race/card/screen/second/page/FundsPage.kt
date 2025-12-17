package ua.vald_zx.game.rat.race.card.screen.second.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.components.ExtendedButton
import ua.vald_zx.game.rat.race.card.components.SDetailsField
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardState
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardStore
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Plus
import ua.vald_zx.game.rat.race.card.screen.second.BuyFundScreen
import ua.vald_zx.game.rat.race.card.screen.second.SellFundScreen

@Composable
fun FundsPage(state: RatRace2CardState) {
    val raceRate2store = koinInject<RatRace2CardStore>()
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
                            name = stringResource(Res.string.fund_at),
                            value = "${fund.rate}%",
                            modifier = Modifier.weight(1f)
                        )
                        SDetailsField(
                            name = stringResource(Res.string.amount),
                            value = fund.amount.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { bottomSheetNavigator.show(SellFundScreen(fund)) }) {
                            Text(stringResource(Res.string.withdraw))
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
            if (state.funds.isNotEmpty()) {
                ExtendedButton(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp).weight(1f),
                    onClick = { capitalizationConfirm = true },
                    onLongClick = { capitalizationStarConfirm = true },
                    content = { Text(stringResource(Res.string.capitalize)) },
                )
            }
        }
        if (capitalizationConfirm) {
            AlertDialog(
                title = { Text(text = stringResource(Res.string.capitalization)) },
                text = { Text(text = stringResource(Res.string.capitalization_total, state.capitalization().toString())) },
                onDismissRequest = { capitalizationConfirm = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            raceRate2store.dispatch(RatRace2CardAction.CapitalizeFunds)
                            capitalizationConfirm = false
                        }
                    ) { Text(stringResource(Res.string.capitalize)) }
                },
                dismissButton = {
                    TextButton(onClick = { capitalizationConfirm = false }) { Text(stringResource(Res.string.cancel)) }
                }
            )
        }
        if (capitalizationStarConfirm) {
            AlertDialog(
                title = { Text(text = stringResource(Res.string.capitalization)) },
                text = { Text(text = stringResource(Res.string.capitalization_amount, state.config.fundStartRate.toString(), state.capitalizationStart().toString())) },
                onDismissRequest = { capitalizationStarConfirm = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            raceRate2store.dispatch(RatRace2CardAction.CapitalizeStarsFunds)
                            capitalizationStarConfirm = false
                        }
                    ) { Text(stringResource(Res.string.capitalize)) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        capitalizationStarConfirm = false
                    }) { Text(stringResource(Res.string.cancel)) }
                }
            )
        }
    }
}