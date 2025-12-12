package ua.vald_zx.game.rat.race.card.screen.second.page

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.stringResource
import rat_race_card.composeapp.generated.resources.Res
import rat_race_card.composeapp.generated.resources.buy
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.components.SDetailsField
import ua.vald_zx.game.rat.race.card.label
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardState
import ua.vald_zx.game.rat.race.card.screen.second.BuySharesScreen
import ua.vald_zx.game.rat.race.card.screen.second.SellSharesScreen
import ua.vald_zx.game.rat.race.card.theme.AppTheme

@Composable
fun SharesPage(state: RatRace2CardState) {
    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    Column {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.sharesList) { shares ->
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(shares.type.label(), style = MaterialTheme.typography.titleSmall)
                    Row {
                        SDetailsField(
                            name = "Кількість",
                            value = shares.count.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        SDetailsField(
                            name = "Ціна покупки",
                            value = shares.buyPrice.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(stringResource(Res.string.buy), modifier = Modifier.weight(1f)) {
                bottomSheetNavigator.show(BuySharesScreen())
            }
            if (state.sharesList.isNotEmpty()) {
                Button("Продати", AppTheme.colors.action, modifier = Modifier.weight(1f)) {
                    bottomSheetNavigator.show(SellSharesScreen())
                }
            }
        }
    }
}