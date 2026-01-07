package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.components.NumberTextField
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardStore
import ua.vald_zx.game.rat.race.card.splitDecimal

class SellLandScreen : Screen {
    @Composable
    override fun Content() {
        val raceRate2store = koinInject<RatRace2CardStore>()
        val state by raceRate2store.observeState().collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        BottomSheetContainer {
            val totalArea = state.lands.sumOf { it.area }
            val inputArea = remember {
                mutableStateOf(
                    TextFieldValue(
                        totalArea.toString()
                    )
                )
            }
            val inputPrice = remember { mutableStateOf(TextFieldValue("")) }
            val area = inputArea.value.text
            val price = inputPrice.value.text
            Text(
                stringResource(Res.string.total) + ": ${((area.toLongOrNull() ?: 0) * (price.toLongOrNull() ?: 0)).splitDecimal()}",
                style = MaterialTheme.typography.titleSmall
            )
            NumberTextField(
                input = inputArea,
                inputLabel = stringResource(Res.string.area),
            )
            NumberTextField(
                input = inputPrice,
                inputLabel = stringResource(Res.string.sale_price),
            )
            ElevatedButton(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    bottomSheetNavigator.hide()
                    raceRate2store.dispatch(
                        RatRace2CardAction.SellLand(
                            area = area.toLong(),
                            priceOfUnit = price.toLong(),
                        )
                    )
                },
                enabled = area.isNotEmpty()
                        && price.isNotEmpty()
                        && area.toLong() <= totalArea,
                content = {
                    Text(stringResource(Res.string.sell))
                }
            )
        }
    }
}