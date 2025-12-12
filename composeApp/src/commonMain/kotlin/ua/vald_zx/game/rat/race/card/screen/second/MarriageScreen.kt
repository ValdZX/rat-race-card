package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.stringResource
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.getDigits
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.raceRate2store
import ua.vald_zx.game.rat.race.card.splitDecimal

class MarriageScreen : Screen {
    @Composable
    override fun Content() {
        val state by raceRate2store.observeState().collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        BottomSheetContainer {
            if (state.isMarried) {
                var halfCash by remember { mutableStateOf(false) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Віддати половиту готівки та депозиту\n(${((state.cash + state.deposit) / 2).splitDecimal()} $)",
                        modifier = Modifier.weight(1f)
                    )
                    Switch(halfCash, onCheckedChange = { halfCash = it })
                }
                var babyBye by remember { mutableStateOf(false) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(Res.string.give),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(babyBye, onCheckedChange = { babyBye = it })
                }
                ElevatedButton(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .widthIn(min = 200.dp),
                    onClick = {
                        bottomSheetNavigator.hide()
                        raceRate2store.dispatch(
                            RatRace2CardAction.UpdateFamily(
                                isMarried = false,
                                halfCash = halfCash,
                                babies = if (babyBye) 0 else state.babies
                            )
                        )
                    },
                    content = {
                        Text(stringResource(Res.string.divorce_proposal))
                    }
                )
            } else {
                var marriageCost by remember { mutableStateOf("0") }
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.price)) },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    value = marriageCost,
                    onValueChange = { marriageCost = it.getDigits() }
                )
                ElevatedButton(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .widthIn(min = 200.dp),
                    onClick = {
                        bottomSheetNavigator.hide()
                        raceRate2store.dispatch(
                            RatRace2CardAction.UpdateFamily(
                                isMarried = true,
                                marriageCost = marriageCost.toLong()
                            )
                        )
                    },
                    content = {
                        Text(stringResource(Res.string.marriage_proposal))
                    }
                )
            }
        }
    }
}