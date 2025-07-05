package ua.vald_zx.game.rat.race.card.screen.board.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import ua.vald_zx.game.rat.race.card.components.SDetailsField
import ua.vald_zx.game.rat.race.card.logic.CardState
import ua.vald_zx.game.rat.race.card.screen.second.SellFundScreen

@Composable
fun FundsPage(state: CardState) {
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
                            name = "Фонд на",
                            value = "${fund.rate}%",
                            modifier = Modifier.weight(1f)
                        )
                        SDetailsField(
                            name = "Сума",
                            value = fund.amount.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { bottomSheetNavigator.show(SellFundScreen(fund)) }) {
                            Text("Зняти")
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}