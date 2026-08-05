package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.design.proportionalAmountOptions
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardStore
import ua.vald_zx.game.rat.race.card.screen.InputScreen
import ua.vald_zx.game.rat.race.card.resources.*
import kotlin.math.min

class ToDepositScreen : Screen {
    @Composable
    override fun Content() {
        val raceRate2store = koinInject<RatRace2CardStore>()
        val state by raceRate2store.observeState().collectAsState()
        InputScreen(
            inputLabel = "Сума вкладу",
            buttonText = "Вкласти",
            validation = { amount -> amount.isNotEmpty() },
            onClick = { amount -> raceRate2store.dispatch(RatRace2CardAction.ToDeposit(amount = amount.toLong())) },
            value = state.cash.toString(),
            quickOptions = proportionalAmountOptions(state.cash, stringResource(Res.string.all_in)),
        )
    }
}

class FromDepositScreen : Screen {
    @Composable
    override fun Content() {
        val raceRate2store = koinInject<RatRace2CardStore>()
        val state by raceRate2store.observeState().collectAsState()
        InputScreen(
            inputLabel = "Сума зняття з депозиту",
            buttonText = "Зняти",
            validation = { amount -> amount.isNotEmpty() && state.deposit >= amount.toLong() },
            onClick = { amount -> raceRate2store.dispatch(RatRace2CardAction.FromDeposit(amount = amount.toLong())) },
            value = state.deposit.toString(),
            quickOptions = proportionalAmountOptions(state.deposit, stringResource(Res.string.all_in)),
        )
    }
}

class RepayCreditScreen : Screen {
    @Composable
    override fun Content() {
        val raceRate2store = koinInject<RatRace2CardStore>()
        val state by raceRate2store.observeState().collectAsState()
        val repayable = min(state.loan, state.balance())
        InputScreen(
            inputLabel = "Сума погашення",
            buttonText = "Погасити",
            validation = { amount -> amount.isNotEmpty() && state.balance() >= amount.toInt() },
            onClick = { amount -> raceRate2store.dispatch(RatRace2CardAction.RepayLoan(amount = amount.toLong())) },
            value = repayable.toString(),
            quickOptions = proportionalAmountOptions(repayable, stringResource(Res.string.all_in)),
        )
    }
}

class GetLoanScreen : Screen {
    @Composable
    override fun Content() {
        val raceRate2store = koinInject<RatRace2CardStore>()
        InputScreen(
            inputLabel = "Сума кредиту",
            buttonText = "Взяти",
            validation = { amount -> amount.isNotEmpty() },
            onClick = { amount -> raceRate2store.dispatch(RatRace2CardAction.GetLoan(amount = amount.toLong())) },
        )
    }
}
