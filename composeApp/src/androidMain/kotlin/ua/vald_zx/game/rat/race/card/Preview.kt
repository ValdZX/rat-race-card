package ua.vald_zx.game.rat.race.card

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ua.vald_zx.game.rat.race.card.components.BalanceField
import ua.vald_zx.game.rat.race.card.components.CashFlowField
import ua.vald_zx.game.rat.race.card.components.NegativeField
import ua.vald_zx.game.rat.race.card.components.PositiveField


@Preview
@Composable
fun FieldPreview() {
    Column {
        PositiveField("Активний прибуток", "123123123")
        NegativeField("Загальні витрати", "123123123")
        CashFlowField("Cash Flow", "123123123")
        BalanceField("Готівка", "123123123")
    }
}