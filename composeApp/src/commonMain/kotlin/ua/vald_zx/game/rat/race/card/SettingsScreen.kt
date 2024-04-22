package ua.vald_zx.game.rat.race.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.russhwolf.settings.set
import ua.vald_zx.game.rat.race.card.beans.Config
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.logic.AppAction
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.fromvectorimages.Back
import ua.vald_zx.game.rat.race.card.resource.fromvectorimages.IcDarkMode
import ua.vald_zx.game.rat.race.card.resource.fromvectorimages.IcLightMode
import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark

class SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val state by store.observeState().collectAsState()
        val navigator = LocalNavigator.current
        var isDark by LocalThemeIsDark.current
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
                ElevatedButton(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        .widthIn(min = 200.dp),
                    onClick = {
                        navigator?.push(ExportScreen())
                    },
                    content = { Text("Експорт картки") }
                )
                ElevatedButton(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        .widthIn(min = 200.dp),
                    onClick = {
                        navigator?.push(ImportScreen())
                    },
                    content = { Text("Імпорт картки") }
                )
                Button("Редагувати картку професії") {
                    navigator?.push(EditPersonCardScreen())
                }
                var resetDialog by remember { mutableStateOf(false) }
                Button("Нова гра") {
                    resetDialog = true
                }
                if (resetDialog) {
                    AlertDialog(
                        title = { Text(text = "Точно?") },
                        text = { Text(text = "Всі данні буде затерто.") },
                        onDismissRequest = { resetDialog = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    navigator?.replaceAll(PersonCardScreen())
                                    resetDialog = false
                                }
                            ) { Text("Точно") }
                        },
                        dismissButton = {
                            TextButton(onClick = { resetDialog = false }) { Text("Відміна") }
                        }
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Механіка фондів")
                    Switch(state.config.hasFunds, onCheckedChange = {
                        store.dispatch(
                            AppAction.UpdateConfig(
                                state.config.copy(hasFunds = it)
                            )
                        )
                    })
                }
                var depositRate by remember { mutableStateOf(state.config.depositRate.toString()) }
                var loadRate by remember { mutableStateOf(state.config.loadRate.toString()) }
                var babyCost by remember { mutableStateOf(state.config.babyCost.toString()) }
                var carCost by remember { mutableStateOf(state.config.carCost.toString()) }
                var apartmentCost by remember { mutableStateOf(state.config.apartmentCost.toString()) }
                var cottageCost by remember { mutableStateOf(state.config.cottageCost.toString()) }
                var yachtCost by remember { mutableStateOf(state.config.yachtCost.toString()) }
                var flightCost by remember { mutableStateOf(state.config.flightCost.toString()) }
                var fundBaseRate by remember { mutableStateOf(state.config.fundBaseRate.toString()) }
                var fundStartRate by remember { mutableStateOf(state.config.fundStartRate.toString()) }

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ставка депозиту %") },
                    value = depositRate,
                    onValueChange = { depositRate = it.getDigits() }
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Базова ставка фонду") },
                    value = fundBaseRate,
                    onValueChange = { fundBaseRate = it.getDigits() }
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Виграшна ставка фонду") },
                    value = fundStartRate,
                    onValueChange = { fundStartRate = it.getDigits() }
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Процент кредиту %") },
                    value = loadRate,
                    onValueChange = { loadRate = it.getDigits() }
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Витрати на дитину") },
                    value = babyCost,
                    onValueChange = { babyCost = it.getDigits() }
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Витрати на автомобіль") },
                    value = carCost,
                    onValueChange = { carCost = it.getDigits() }
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Витрати на квариру") },
                    value = apartmentCost,
                    onValueChange = { apartmentCost = it.getDigits() }
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Витрати на маєток") },
                    value = cottageCost,
                    onValueChange = { cottageCost = it.getDigits() }
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Витрати на яхту") },
                    value = yachtCost,
                    onValueChange = { yachtCost = it.getDigits() }
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Витрати на літак") },
                    value = flightCost,
                    onValueChange = { flightCost = it.getDigits() }
                )


                ElevatedButton(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        .widthIn(min = 200.dp),
                    onClick = {
                        store.dispatch(
                            AppAction.UpdateConfig(
                                Config(
                                    depositRate = depositRate.toLong(),
                                    loadRate = loadRate.toLong(),
                                    babyCost = babyCost.toLong(),
                                    carCost = carCost.toLong(),
                                    apartmentCost = apartmentCost.toLong(),
                                    cottageCost = cottageCost.toLong(),
                                    yachtCost = yachtCost.toLong(),
                                    flightCost = flightCost.toLong(),
                                    fundBaseRate = fundBaseRate.toLong(),
                                    fundStartRate = fundStartRate.toLong(),
                                )
                            )
                        )
                        navigator?.pop()
                    },
                    enabled = depositRate.isNotEmpty()
                            && loadRate.isNotEmpty()
                            && babyCost.isNotEmpty()
                            && carCost.isNotEmpty()
                            && apartmentCost.isNotEmpty()
                            && cottageCost.isNotEmpty()
                            && yachtCost.isNotEmpty()
                            && flightCost.isNotEmpty(),
                    content = { Text("Зберегти") }
                )
            }
        }
    }
}