package ua.vald_zx.game.rat.race.card

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview


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