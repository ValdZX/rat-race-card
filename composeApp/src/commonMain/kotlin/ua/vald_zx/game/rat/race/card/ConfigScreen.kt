package ua.vald_zx.game.rat.race.card

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import org.jetbrains.compose.resources.vectorResource
import rat_race_card.composeapp.generated.resources.Res
import rat_race_card.composeapp.generated.resources.back
import ua.vald_zx.game.rat.race.card.beans.Config
import ua.vald_zx.game.rat.race.card.logic.AppAction

class ConfigScreen : Screen {
    @Composable
    override fun Content() {
        val state by store.observeState().collectAsState()
        val navigator = LocalNavigator.current
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
                    Icon(vectorResource(Res.drawable.back), contentDescription = null)
                }
            )
            Column(
                modifier = Modifier
                    .padding(top = 48.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                var depositRate by remember { mutableStateOf(state.config.depositRate.toString()) }
                var loadRate by remember { mutableStateOf(state.config.loadRate.toString()) }
                var babyCost by remember { mutableStateOf(state.config.babyCost.toString()) }
                var carCost by remember { mutableStateOf(state.config.carCost.toString()) }
                var apartmentCost by remember { mutableStateOf(state.config.apartmentCost.toString()) }
                var cottageCost by remember { mutableStateOf(state.config.cottageCost.toString()) }
                var yachtCost by remember { mutableStateOf(state.config.yachtCost.toString()) }
                var flightCost by remember { mutableStateOf(state.config.flightCost.toString()) }

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ставка депозиту %") },
                    value = depositRate,
                    onValueChange = { depositRate = it.getDigits() }
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