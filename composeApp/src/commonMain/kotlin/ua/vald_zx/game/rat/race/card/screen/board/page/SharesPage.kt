package ua.vald_zx.game.rat.race.card.screen.board.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.components.SDetailsField
import ua.vald_zx.game.rat.race.card.label
import ua.vald_zx.game.rat.race.card.logic.CardState

@Composable
fun SharesPage(state: CardState) {
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
    }
}