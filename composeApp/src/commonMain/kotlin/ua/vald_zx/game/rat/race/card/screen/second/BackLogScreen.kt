package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import org.jetbrains.compose.resources.stringResource
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.components.*
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.logic.total
import ua.vald_zx.game.rat.race.card.raceRate2store
import ua.vald_zx.game.rat.race.card.splitDecimal

class BackLogScreen : Screen {
    @Composable
    override fun Content() {
        val log = raceRate2store.statistics?.log ?: return
        var backCount by remember { mutableStateOf(0) }
        var state by remember(backCount) {
            mutableStateOf(log[log.lastIndex - backCount])
        }
        BottomSheetContainer {
            SmoothRainbowText(
                state.total().splitDecimal(),
                rainbow = GoldRainbow,
                style = LocalTextStyle.current.copy(fontSize = 30.sp),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                duration = 4000
            )
            CashFlowField(
                name = stringResource(Res.string.cash_flow),
                value = state.cashFlow().toString()
            )
            BalanceField(
                name = stringResource(Res.string.cash),
                value = state.cash.toString(),
            )
            PositiveField(
                name = stringResource(Res.string.deposit),
                value = state.deposit.toString(),
            )
            NegativeField(
                name = stringResource(Res.string.loan), value = state.loan.toString(),
            )
            if (state.config.hasFunds) {
                FundsField(stringResource(Res.string.funds), state.fundAmount().toString()) { }
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                if (backCount != log.lastIndex) {
                    Button(stringResource(Res.string.undo_action)) {
                        backCount += 1
                    }
                }
                if (backCount != 0) {
                    Button(stringResource(Res.string.redo_action)) {
                        backCount -= 1
                    }
                }
            }
            if (backCount != 0) {
                val nextState = remember(backCount) { log[log.lastIndex - backCount + 1] }
                DetailsField(stringResource(Res.string.total_assets), (nextState.balance() - state.balance()).printLong())
                DetailsField(stringResource(Res.string.cash_flow), (nextState.cashFlow() - state.cashFlow()).printLong())
                DetailsField(stringResource(Res.string.cash), (nextState.cash - state.cash).printLong())
                DetailsField(stringResource(Res.string.deposit), (nextState.deposit - state.deposit).printLong())
                DetailsField(stringResource(Res.string.loan), (nextState.loan - state.loan).printLong())
            }
            ElevatedButton(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    raceRate2store.dispatch(RatRace2CardAction.BackToState(state, backCount))
                },
                content = {
                    Text(stringResource(Res.string.save))
                }
            )
        }
    }

    private fun Long.printLong(): String {
        return if (this > 0) "+$this" else this.toString()
    }
}