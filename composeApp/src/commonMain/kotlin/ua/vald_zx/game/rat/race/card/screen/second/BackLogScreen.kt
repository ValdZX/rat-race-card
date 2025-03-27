package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import ua.vald_zx.game.rat.race.card.components.BalanceField
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.components.CashFlowField
import ua.vald_zx.game.rat.race.card.components.DetailsField
import ua.vald_zx.game.rat.race.card.components.FundsField
import ua.vald_zx.game.rat.race.card.components.GoldRainbow
import ua.vald_zx.game.rat.race.card.components.NegativeField
import ua.vald_zx.game.rat.race.card.components.PositiveField
import ua.vald_zx.game.rat.race.card.components.SmoothRainbowText
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
            mutableStateOf(
                log[log.lastIndex - backCount]
            )
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
                name = "Cash Flow",
                value = state.cashFlow().toString()
            )
            BalanceField(
                name = "Готівка",
                value = state.cash.toString(),
            )
            PositiveField(
                name = "Депозит",
                value = state.deposit.toString(),
            )
            NegativeField(
                name = "Кредит", value = state.loan.toString(),
            )
            if (state.config.hasFunds) {
                FundsField("Фонди", state.fundAmount().toString()) { }
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                if (backCount != log.lastIndex) {
                    Button("Відмінити останню дію") {
                        backCount += 1
                    }
                }
                if (backCount != 0) {
                    Button("Повернути") {
                        backCount -= 1
                    }
                }
            }
            if (backCount != 0) {
                val nextState = remember(backCount) { log[log.lastIndex - backCount + 1] }
                DetailsField("Статки", (nextState.balance() - state.balance()).printLong())
                DetailsField("CashFlow", (nextState.cashFlow() - state.cashFlow()).printLong())
                DetailsField("Готівка", (nextState.cash - state.cash).printLong())
                DetailsField("Депозит", (nextState.deposit - state.deposit).printLong())
                DetailsField("Кредит", (nextState.loan - state.loan).printLong())
            }
            ElevatedButton(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    raceRate2store.dispatch(RatRace2CardAction.BackToState(state, backCount))
                },
                content = {
                    Text("Зберегти")
                }
            )
        }
    }

    private fun Long.printLong(): String {
        return if (this > 0) "+$this" else this.toString()
    }
}