package ua.vald_zx.game.rat.race.card.screen.page

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import ua.vald_zx.game.rat.race.card.beans.BusinessType
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.components.SDetailsField
import ua.vald_zx.game.rat.race.card.logic.AppAction
import ua.vald_zx.game.rat.race.card.logic.AppState
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Dice
import ua.vald_zx.game.rat.race.card.resource.images.Stars
import ua.vald_zx.game.rat.race.card.screen.BuyBusinessScreen
import ua.vald_zx.game.rat.race.card.screen.ExtendBusinessScreen
import ua.vald_zx.game.rat.race.card.screen.SellBusinessScreen
import ua.vald_zx.game.rat.race.card.store
import ua.vald_zx.game.rat.race.card.theme.AppTheme

@Composable
fun BusinessListPage(state: AppState) {
    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    Column {
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(state.business.filter { it.type != BusinessType.WORK }) { index, business ->
                Column(
                    modifier = Modifier
                        .clickable { store.dispatch(AppAction.HideAlarm) }
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
                        BusinessType.SMALL -> "Малий бізнес"
                        BusinessType.MEDIUM -> "Середній бізнес"
                        BusinessType.LARGE -> "Крупний бізнес"
                    }
                    Text(
                        "${index + 1}) $title - ${business.name}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SDetailsField(
                            name = "Ціна",
                            value = business.price.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        SDetailsField(
                            name = "Прибуток",
                            value = business.profit.toString(),
                            additionalValue = business.extentions.map { it.toString() },
                            modifier = Modifier.weight(1f)
                        )
                        Column {
                            TextButton(onClick = {
                                bottomSheetNavigator.show(SellBusinessScreen(business))
                            }) { Text("Продати") }
                            if (business.type == BusinessType.SMALL) {
                                TextButton(onClick = {
                                    bottomSheetNavigator.show(ExtendBusinessScreen(business))
                                }) { Text("Розширити") }
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
            Button("Купити", modifier = Modifier.weight(1f)) {
                bottomSheetNavigator.show(BuyBusinessScreen())
            }
            if (state.business.size > 1)
                ElevatedButton(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    onClick = { store.dispatch(AppAction.RandomBusiness) },
                    content = { Icon(Images.Dice, contentDescription = null) }
                )
        }
    }
}