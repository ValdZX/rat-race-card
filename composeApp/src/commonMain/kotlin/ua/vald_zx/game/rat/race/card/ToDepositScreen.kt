package ua.vald_zx.game.rat.race.card

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import ua.vald_zx.game.rat.race.card.logic.AppAction

class ToDepositScreen : Screen {
    @Composable
    override fun Content() {
        val state by store.observeState().collectAsState()
        InputScreen(
            inputLabel = "Сума вкладу",
            buttonText = "Вкласти",
            validation = { amount -> amount.isNotEmpty() && state.cash >= amount.toLong() },
            onClick = { amount -> store.dispatch(AppAction.ToDeposit(amount = amount.toLong())) },
            value = state.cash.toString()
        )
    }
}