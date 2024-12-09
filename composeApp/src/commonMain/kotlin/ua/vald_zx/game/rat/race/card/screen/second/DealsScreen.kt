package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.screen.InputScreen
import ua.vald_zx.game.rat.race.card.raceRate2store
import kotlin.math.min

class ToDepositScreen : Screen {
    @Composable
    override fun Content() {
        val state by raceRate2store.observeState().collectAsState()
        InputScreen(
            inputLabel = "Сума вкладу",
            buttonText = "Вкласти",
            validation = { amount -> amount.isNotEmpty() },
            onClick = { amount -> raceRate2store.dispatch(RatRace2CardAction.ToDeposit(amount = amount.toLong())) },
            value = state.cash.toString()
        )
    }
}

class FromDepositScreen : Screen {
    @Composable
    override fun Content() {
        val state by raceRate2store.observeState().collectAsState()
        InputScreen(
            inputLabel = "Сума зняття з депозиту",
            buttonText = "Зняти",
            validation = { amount -> amount.isNotEmpty() && state.deposit >= amount.toLong() },
            onClick = { amount -> raceRate2store.dispatch(RatRace2CardAction.FromDeposit(amount = amount.toLong())) },
            value = state.deposit.toString()
        )
    }
}

class RepayCreditScreen : Screen {
    @Composable
    override fun Content() {
        val state by raceRate2store.observeState().collectAsState()
        InputScreen(
            inputLabel = "Сума погашення",
            buttonText = "Погасити",
            validation = { amount -> amount.isNotEmpty() && state.balance() >= amount.toInt() },
            onClick = { amount -> raceRate2store.dispatch(RatRace2CardAction.RepayLoan(amount = amount.toLong())) },
            value = min(state.loan, state.balance()).toString()
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
            onClick = { amount -> raceRate2store.dispatch(RatRace2CardAction.GetLoan(amount = amount.toLong())) },
        )
    }
}