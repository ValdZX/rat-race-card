package ua.vald_zx.game.rat.race.card

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import rat_race_card.composeapp.generated.resources.Res
import rat_race_card.composeapp.generated.resources.active_profit
import rat_race_card.composeapp.generated.resources.cash
import rat_race_card.composeapp.generated.resources.total_expenses
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
        CashFlowField("Cash Flow", "123123123")
        BalanceField(stringResource(Res.string.cash), "123123123")
    }
}