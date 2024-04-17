package ua.vald_zx.game.rat.race.card

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import ua.vald_zx.game.rat.race.card.beans.Fund
import ua.vald_zx.game.rat.race.card.logic.AppAction

class SellFundScreen(val fund: Fund) : Screen {
    @Composable
    override fun Content() {
        val state by store.observeState().collectAsState()
        InputScreen(
            inputLabel = "Сума зняття",
            buttonText = "Зняни",
            validation = { amount -> amount.isNotEmpty() && fund.amount >= amount.toLong() },
            onClick = { amount -> store.dispatch(AppAction.FromFund(fund, amount.toLong())) },
            value = fund.amount.toString()
        )
    }
}