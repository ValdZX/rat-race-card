package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.stringResource
import rat_race_card.composeapp.generated.resources.Res
import rat_race_card.composeapp.generated.resources.buy
import rat_race_card.composeapp.generated.resources.contribution
import rat_race_card.composeapp.generated.resources.percent
import ua.vald_zx.game.rat.race.card.beans.Fund
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.components.NumberTextField
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.raceRate2store

class BuyFundScreen() : Screen {
    @Composable
    override fun Content() {
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        BottomSheetContainer {
            val inputRate = remember { mutableStateOf(TextFieldValue("")) }
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