package ua.vald_zx.game.rat.race.card.screen.second.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.stringResource
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.components.SDetailsField
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardState
import ua.vald_zx.game.rat.race.card.screen.second.BuyLandScreen
import ua.vald_zx.game.rat.race.card.screen.second.SellLandScreen

@Composable
fun LandListPage(state: RatRace2CardState) {
    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    Column {
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(state.lands) { index, land ->
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(top = 8.dp)
                ) {
                    Text(
                        "${index + 1}) ${land.name}",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    SDetailsField(
                        name = stringResource(Res.string.price),
                        value = (land.price).toString(),
                        modifier = Modifier.fillParentMaxWidth(0.5f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SDetailsField(
                            name = stringResource(Res.string.area),
                            value = land.area.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        SDetailsField(
                            name = stringResource(Res.string.priceOfUnit),
                            value = land.priceOfUnit.toString(),
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
                bottomSheetNavigator.show(BuyLandScreen())
            }
            if (state.lands.isNotEmpty())
                Button(stringResource(Res.string.sell), modifier = Modifier.weight(1f)) {
                    bottomSheetNavigator.show(SellLandScreen())
                }
        }
    }
}