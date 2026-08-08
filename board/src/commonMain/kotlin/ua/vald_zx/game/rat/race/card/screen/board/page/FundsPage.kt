package ua.vald_zx.game.rat.race.card.screen.board.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.SDetailsField
import ua.vald_zx.game.rat.race.card.resources.Res
import ua.vald_zx.game.rat.race.card.resources.amount
import ua.vald_zx.game.rat.race.card.resources.fund_at
import ua.vald_zx.game.rat.race.card.resources.funds
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.fundAmount
import ua.vald_zx.game.rat.race.card.splitDecimal
import ua.vald_zx.game.rat.race.card.formatAmount

@Composable
fun FundsPage(player: Player) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(player.funds) { fund ->
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SDetailsField(
                            name = stringResource(Res.string.fund_at),
                            value = "${fund.rate}%",
                            modifier = Modifier.weight(1f)
                        )
                        SDetailsField(
                            name = stringResource(Res.string.amount),
                            value = fund.amount.formatAmount(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
        if (player.funds.isNotEmpty()) {
            SDetailsField(
                name = stringResource(Res.string.funds),
                value = player.fundAmount().formatAmount(),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
        }
    }
}
