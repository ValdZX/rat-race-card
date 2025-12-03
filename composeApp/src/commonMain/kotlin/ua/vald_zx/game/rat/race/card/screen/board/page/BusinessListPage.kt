package ua.vald_zx.game.rat.race.card.screen.board.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import ua.vald_zx.game.rat.race.card.components.SDetailsField
import ua.vald_zx.game.rat.race.card.screen.board.SellBusinessScreen
import ua.vald_zx.game.rat.race.card.shared.BusinessType
import ua.vald_zx.game.rat.race.card.shared.Player

@Composable
fun BusinessListPage(state: Player) {
    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    Column {
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(state.businesses.filter { it.type != BusinessType.WORK }) { index, business ->
                Column(
                    modifier = Modifier
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
                        BusinessType.CORRUPTION -> "Корупційний бізнес"
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
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}