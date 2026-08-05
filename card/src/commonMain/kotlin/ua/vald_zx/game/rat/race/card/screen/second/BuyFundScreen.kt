package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import ua.vald_zx.game.rat.race.card.resources.Res
import ua.vald_zx.game.rat.race.card.resources.buy
import ua.vald_zx.game.rat.race.card.resources.contribution
import ua.vald_zx.game.rat.race.card.resources.percent
import ua.vald_zx.game.rat.race.card.resources.all_in
import ua.vald_zx.game.rat.race.card.beans.Fund
import ua.vald_zx.game.rat.race.card.components.ClosableBottomSheetContainer
import ua.vald_zx.game.rat.race.card.components.NumberTextField
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardStore
import ua.vald_zx.game.rat.race.card.design.proportionalAmountOptions

class BuyFundScreen() : Screen {
    @Composable
    override fun Content() {
        val raceRate2store = koinInject<RatRace2CardStore>()
        val state by raceRate2store.observeState().collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        ClosableBottomSheetContainer {
            val inputRate = remember(state.config.fundBaseRate) {
                mutableStateOf(TextFieldValue(state.config.fundBaseRate.toString()))
            }
            val inputAmount = remember { mutableStateOf(TextFieldValue("")) }
            val amount = inputAmount.value.text
            val rate = inputRate.value.text
            NumberTextField(
                input = inputRate,
                inputLabel = stringResource(Res.string.percent),
            )
            NumberTextField(
                input = inputAmount,
                inputLabel = stringResource(Res.string.contribution),
                quickOptions = proportionalAmountOptions(state.cash, stringResource(Res.string.all_in)),
            )
            ElevatedButton(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    bottomSheetNavigator.hide()
                    raceRate2store.dispatch(
                        RatRace2CardAction.AddFund(
                            Fund(
                                rate = rate.toLong(),
                                amount = amount.toLong(),
                            )
                        )
                    )
                },
                enabled = rate.isNotEmpty() && amount.isNotEmpty(),
                content = {
                    Text(stringResource(Res.string.buy))
                }
            )
        }
    }
}
