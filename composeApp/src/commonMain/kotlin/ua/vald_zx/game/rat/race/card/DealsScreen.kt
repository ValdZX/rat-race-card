package ua.vald_zx.game.rat.race.card

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import ua.vald_zx.game.rat.race.card.logic.AppAction
import kotlin.math.min

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

class FromDepositScreen : Screen {
    @Composable
    override fun Content() {
        val state by store.observeState().collectAsState()
        InputScreen(
            inputLabel = "Сума зняття з депозиту",
            buttonText = "Зняти",
            validation = { amount -> amount.isNotEmpty() && state.deposit >= amount.toLong() },
            onClick = { amount -> store.dispatch(AppAction.FromDeposit(amount = amount.toLong())) },
            value = state.deposit.toString()
        )
    }
}

class RepayCreditScreen : Screen {
    @Composable
    override fun Content() {
        val state by store.observeState().collectAsState()
        InputScreen(
            inputLabel = "Сума погашення",
            buttonText = "Погасити",
            validation = { amount -> amount.isNotEmpty() },
            onClick = { amount -> store.dispatch(AppAction.RepayLoan(amount = amount.toLong())) },
            value = min(state.loan, state.cash).toString()
        )
    }
}

class GetLoanScreen : Screen {
    @Composable
    override fun Content() {
        InputScreen(
            inputLabel = "Сума кредиту",
            buttonText = "Взяти",
            validation = { amount -> amount.isNotEmpty() },
            onClick = { amount -> store.dispatch(AppAction.GetLoan(amount = amount.toLong())) },
        )
    }
}