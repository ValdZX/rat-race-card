package ua.vald_zx.game.rat.race.card.screen.board.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import rat_race_card.composeapp.generated.resources.Res
import rat_race_card.composeapp.generated.resources.purchase_price
import ua.vald_zx.game.rat.race.card.components.SDetailsField
import ua.vald_zx.game.rat.race.card.formatAmount
import ua.vald_zx.game.rat.race.card.shared.Estate
import ua.vald_zx.game.rat.race.card.shared.Player

@Composable
fun EstatePage(state: Player) {
    LazyColumn(Modifier.fillMaxSize()) {
        items(state.estateList) { estate ->
            EstateItem(estate)
        }
    }
}

@Composable
fun EstateItem(estate: Estate) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(estate.name, style = MaterialTheme.typography.titleSmall)
        SDetailsField(
            name = stringResource(Res.string.purchase_price),
            value = estate.price.formatAmount(),
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider()
    }
}