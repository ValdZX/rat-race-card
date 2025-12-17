package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
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
import org.koin.compose.koinInject
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.beans.Business
import ua.vald_zx.game.rat.race.card.beans.BusinessType
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.components.NumberTextField
import ua.vald_zx.game.rat.race.card.components.SDetailsField
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardStore

class SellBusinessScreen(private val businessToSell: Business) : Screen {
    @Composable
    override fun Content() {
        val raceRate2store = koinInject<RatRace2CardStore>()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        BottomSheetContainer {
            val inputAmount = remember { mutableStateOf(TextFieldValue(businessToSell.price.toString())) }
            val amount = inputAmount.value.text
            val title = when (businessToSell.type) {
                BusinessType.WORK -> ""
                BusinessType.SMALL -> stringResource(Res.string.small_business)
                BusinessType.MEDIUM -> stringResource(Res.string.medium_business)
                BusinessType.LARGE -> stringResource(Res.string.large_business)
                BusinessType.CORRUPTION -> stringResource(Res.string.corruption_business)
            }
            Text("$title: ${businessToSell.name}", style = MaterialTheme.typography.titleSmall)
            Row {
                SDetailsField(
                    name = stringResource(Res.string.price),
                    value = businessToSell.price.toString(),
                    modifier = Modifier.weight(1f)
                )
                SDetailsField(
                    name =  stringResource(Res.string.salary),
                    value = businessToSell.profit.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            NumberTextField(
                input = inputAmount,
                inputLabel = stringResource(Res.string.sell_amount),
            )
            ElevatedButton(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    bottomSheetNavigator.hide()
                    raceRate2store.dispatch(RatRace2CardAction.SellBusiness(amount = amount.toLong(), business = businessToSell))
                },
                enabled = amount.isNotEmpty(),
                content = {
                    Text(stringResource(Res.string.sell))
                }
            )
        }
    }
}