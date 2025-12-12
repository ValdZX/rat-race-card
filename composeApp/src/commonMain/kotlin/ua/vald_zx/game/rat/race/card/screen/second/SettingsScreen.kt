package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.russhwolf.settings.set
import org.jetbrains.compose.resources.stringResource
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.beans.Config
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.components.NumberTextField
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.raceRate2store
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Back
import ua.vald_zx.game.rat.race.card.resource.images.IcDarkMode
import ua.vald_zx.game.rat.race.card.resource.images.IcLightMode
import ua.vald_zx.game.rat.race.card.screen.ExportScreen
import ua.vald_zx.game.rat.race.card.settings
import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark

class SettingsScreen : Screen {
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val state by raceRate2store.observeState().collectAsState()
        val navigator = LocalNavigator.current
        var isDark by LocalThemeIsDark.current
        BottomSheetNavigator {
            val bottomSheetNavigator = LocalBottomSheetNavigator.current
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(16.dp),
            ) {
                IconButton(
                    modifier = Modifier.align(Alignment.TopStart),
                    onClick = { navigator?.pop() },
                    content = {
                        Icon(Images.Back, contentDescription = null)
                    }
                )
                val icon = remember(isDark) {
                    if (isDark) Images.IcLightMode
                    else Images.IcDarkMode
                }
                IconButton(
                    modifier = Modifier.align(Alignment.TopEnd),
                    onClick = {
                        isDark = !isDark
                        settings["theme"] = isDark
                    },
                    content = {
                        Icon(icon, contentDescription = null)
                    }
                )
                Column(
                    modifier = Modifier
                        .padding(top = 48.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (raceRate2store.statistics?.log != null) {
                        ElevatedButton(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                .widthIn(min = 200.dp),
                            onClick = {
                                bottomSheetNavigator.show(BackLogScreen())
                            },
                            content = { Text(stringResource(Res.string.undo_actions)) }
                        )
                    }
                    ElevatedButton(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            .widthIn(min = 200.dp),
                        onClick = {
                            navigator?.push(ExportScreen())
                        },
                        content = { Text(stringResource(Res.string.export_card)) }
                    )
                    ElevatedButton(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            .widthIn(min = 200.dp),
                        onClick = {
                            navigator?.push(ImportScreen())
                        },
                        content = { Text(stringResource(Res.string.import_card)) }
                    )
                    Button(stringResource(Res.string.edit_profession_card)) {
                        navigator?.push(EditPersonCardScreen())
                    }
                    var resetDialog by remember { mutableStateOf(false) }
                    Button(stringResource(Res.string.new_game)) {
                        resetDialog = true
                    }
                    if (resetDialog) {
                        AlertDialog(
                            title = { Text(text = stringResource(Res.string.confirm_reset)) },
                            text = { Text(text = stringResource(Res.string.reset_data_warning)) },
                            onDismissRequest = { resetDialog = false },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        navigator?.replaceAll(PersonCard2Screen())
                                        resetDialog = false
                                    }
                                ) { Text(stringResource(Res.string.yes)) }
                            },
                            dismissButton = {
                                TextButton(onClick = { resetDialog = false }) { Text(stringResource(Res.string.cancel)) }
                            }
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(Res.string.funds))
                        Switch(state.config.hasFunds, onCheckedChange = {
                            raceRate2store.dispatch(
                                RatRace2CardAction.UpdateConfig(
                                    state.config.copy(hasFunds = it)
                                )
                            )
                        })
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("TTS")
                        Switch(state.config.tts, onCheckedChange = {
                            raceRate2store.dispatch(
                                RatRace2CardAction.UpdateConfig(
                                    state.config.copy(tts = it)
                                )
                            )
                        })
                    }
                    val inputDepositRate =
                        remember { mutableStateOf(TextFieldValue(state.config.depositRate.toString())) }
                    val inputLoadRate =
                        remember { mutableStateOf(TextFieldValue(state.config.loadRate.toString())) }
                    val inputBabyCost =
                        remember { mutableStateOf(TextFieldValue(state.config.babyCost.toString())) }
                    val inputCarCost =
                        remember { mutableStateOf(TextFieldValue(state.config.carCost.toString())) }
                    val inputApartmentCost =
                        remember { mutableStateOf(TextFieldValue(state.config.apartmentCost.toString())) }
                    val inputCottageCost =
                        remember { mutableStateOf(TextFieldValue(state.config.cottageCost.toString())) }
                    val inputYachtCost =
                        remember { mutableStateOf(TextFieldValue(state.config.yachtCost.toString())) }
                    val inputFlightCost =
                        remember { mutableStateOf(TextFieldValue(state.config.flightCost.toString())) }
                    val inputFundBaseRate =
                        remember { mutableStateOf(TextFieldValue(state.config.fundBaseRate.toString())) }
                    val inputFundStartRate =
                        remember { mutableStateOf(TextFieldValue(state.config.fundStartRate.toString())) }
                    NumberTextField(input = inputDepositRate, inputLabel = stringResource(Res.string.deposit_rate))
                    NumberTextField(input = inputFundBaseRate, inputLabel = stringResource(Res.string.base_fund_rate))
                    NumberTextField(
                        input = inputFundStartRate,
                        inputLabel = stringResource(Res.string.winning_fund_rate)
                    )
                    NumberTextField(input = inputLoadRate, inputLabel = stringResource(Res.string.loan_rate))
                    NumberTextField(input = inputBabyCost, inputLabel = stringResource(Res.string.baby_cost))
                    NumberTextField(input = inputCarCost, inputLabel = stringResource(Res.string.car_cost))
                    NumberTextField(input = inputApartmentCost, inputLabel = stringResource(Res.string.apartment_cost))
                    NumberTextField(input = inputCottageCost, inputLabel = stringResource(Res.string.cottage_cost))
                    NumberTextField(input = inputYachtCost, inputLabel = stringResource(Res.string.yacht_cost))
                    NumberTextField(input = inputFlightCost, inputLabel = stringResource(Res.string.flight_cost))
                    ElevatedButton(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            .widthIn(min = 200.dp),
                        onClick = {
                            raceRate2store.dispatch(
                                RatRace2CardAction.UpdateConfig(
                                    Config(
                                        depositRate = inputDepositRate.value.text.toLong(),
                                        loadRate = inputLoadRate.value.text.toLong(),
                                        babyCost = inputBabyCost.value.text.toLong(),
                                        carCost = inputCarCost.value.text.toLong(),
                                        apartmentCost = inputApartmentCost.value.text.toLong(),
                                        cottageCost = inputCottageCost.value.text.toLong(),
                                        yachtCost = inputYachtCost.value.text.toLong(),
                                        flightCost = inputFlightCost.value.text.toLong(),
                                        fundBaseRate = inputFundBaseRate.value.text.toLong(),
                                        fundStartRate = inputFundStartRate.value.text.toLong(),
                                    )
                                )
                            )
                            navigator?.pop()
                        },
                        enabled = inputDepositRate.value.text.isNotEmpty()
                                && inputLoadRate.value.text.isNotEmpty()
                                && inputBabyCost.value.text.isNotEmpty()
                                && inputCarCost.value.text.isNotEmpty()
                                && inputApartmentCost.value.text.isNotEmpty()
                                && inputCottageCost.value.text.isNotEmpty()
                                && inputYachtCost.value.text.isNotEmpty()
                                && inputFlightCost.value.text.isNotEmpty(),
                        content = { Text("Зберегти") }
                    )
                }
            }
        }
    }
}