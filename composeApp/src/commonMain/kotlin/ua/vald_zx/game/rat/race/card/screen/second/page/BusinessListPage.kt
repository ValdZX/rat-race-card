package ua.vald_zx.game.rat.race.card.screen.second.page

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.beans.BusinessType
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.components.SDetailsField
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardState
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardStore
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Dice
import ua.vald_zx.game.rat.race.card.screen.second.BuyBusinessScreen
import ua.vald_zx.game.rat.race.card.screen.second.ExtendBusinessScreen
import ua.vald_zx.game.rat.race.card.screen.second.SellBusinessScreen

@Composable
fun BusinessListPage(state: RatRace2CardState) {
    val raceRate2store = koinInject<RatRace2CardStore>()
    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    Column {
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(state.business.filter { it.type != BusinessType.WORK }) { index, business ->
                Column(
                    modifier = Modifier
                        .clickable { raceRate2store.dispatch(RatRace2CardAction.HideAlarm) }
                        .background(
                            if (business.alarmed) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.background
                            }
                        )
                        .padding(top = 8.dp)
                ) {
                    val title = when (business.type) {
                        BusinessType.WORK -> ""
                        BusinessType.SMALL -> stringResource(Res.string.small_business)
                        BusinessType.MEDIUM -> stringResource(Res.string.medium_business)
                        BusinessType.LARGE -> stringResource(Res.string.large_business)
                        BusinessType.CORRUPTION -> stringResource(Res.string.corruption_business)
                    }
                    Text(
                        "${index + 1}) $title - ${business.name}",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (business.type == BusinessType.CORRUPTION) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SDetailsField(
                            name = stringResource(Res.string.price),
                            value = business.price.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        SDetailsField(
                            name = stringResource(Res.string.salary),
                            value = business.profit.toString(),
                            additionalValue = business.extentions.map { it.toString() },
                            modifier = Modifier.weight(1f)
                        )
                        Column {
                            TextButton(onClick = {
                                bottomSheetNavigator.show(SellBusinessScreen(business))
                            }) { Text(stringResource(Res.string.sell)) }
                            if (business.type == BusinessType.SMALL) {
                                TextButton(onClick = {
                                    bottomSheetNavigator.show(ExtendBusinessScreen(business))
                                }) { Text(stringResource(Res.string.expand)) }
                            }
                        }
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
                bottomSheetNavigator.show(BuyBusinessScreen())
            }
            if (state.business.size > 1)
                ElevatedButton(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    onClick = { raceRate2store.dispatch(RatRace2CardAction.RandomBusiness) },
                    content = { Icon(Images.Dice, contentDescription = null) }
                )
        }
    }
}