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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.components.SDetailsField
import ua.vald_zx.game.rat.race.card.shared.BusinessType
import ua.vald_zx.game.rat.race.card.shared.Player

@Composable
fun BusinessListPage(state: Player) {
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
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}