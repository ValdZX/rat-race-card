package ua.vald_zx.game.rat.race.card

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.resources.Res
import ua.vald_zx.game.rat.race.card.resources.active_profit
import ua.vald_zx.game.rat.race.card.resources.cash
import ua.vald_zx.game.rat.race.card.resources.total_expenses
import ua.vald_zx.game.rat.race.card.components.BalanceField
import ua.vald_zx.game.rat.race.card.components.CashFlowField
import ua.vald_zx.game.rat.race.card.components.NegativeField
import ua.vald_zx.game.rat.race.card.components.PositiveField

@Preview
@Composable
fun FieldPreview() {
    Column {
        PositiveField(stringResource(Res.string.active_profit), "123123123")
        NegativeField(stringResource(Res.string.total_expenses), "123123123")
        CashFlowField("Cash Flow", emptyList(),"123123123")
        BalanceField(stringResource(Res.string.cash), "123123123")
    }
}
