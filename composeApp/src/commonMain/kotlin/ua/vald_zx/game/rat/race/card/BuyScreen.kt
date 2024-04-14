package ua.vald_zx.game.rat.race.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
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
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import ua.vald_zx.game.rat.race.card.beans.BusinessType
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.logic.AppAction

class BuyScreen() : Screen {
    @Composable
    override fun Content() {
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        BottomSheetContainer {
            Button("Купити авто") {
                bottomSheetNavigator.replace(BuyCarScreen())
            }
            Button("Купити квартиру") {
                bottomSheetNavigator.replace(BuyApartmentScreen())
            }
            Button("Купити будинок") {
                bottomSheetNavigator.replace(BuyCottageScreen())
            }
            Button("Купити яхту") {
                bottomSheetNavigator.replace(BuyYachtScreen())
            }
            Button("Купити літак") {
                bottomSheetNavigator.replace(BuyFlightScreen())
            }
        }
    }
}

class BuyCarScreen : Screen {
    @Composable
    override fun Content() {
        val state by store.observeState().collectAsState()
        InputScreen(
            inputLabel = "Ціна авто",
            buttonText = "Купити",
            validation = { price -> price.isNotEmpty() && state.cash >= price.toLong() },
            onClick = { price -> store.dispatch(AppAction.BuyCar(price = price.toLong())) },
            value = state.cash.toString()
        )
    }
}

class BuyApartmentScreen : Screen {
    @Composable
    override fun Content() {
        val state by store.observeState().collectAsState()
        InputScreen(
            inputLabel = "Ціна квартири",
            buttonText = "Купити",
            validation = { price -> price.isNotEmpty() && state.cash >= price.toLong() },
            onClick = { price -> store.dispatch(AppAction.BuyApartment(price = price.toLong())) },
            value = state.cash.toString()
        )
    }
}

class BuyCottageScreen : Screen {
    @Composable
    override fun Content() {
        val state by store.observeState().collectAsState()
        InputScreen(
            inputLabel = "Ціна будинку",
            buttonText = "Купити",
            validation = { price -> price.isNotEmpty() && state.cash >= price.toLong() },
            onClick = { price -> store.dispatch(AppAction.BuyCottage(price = price.toLong())) },
            value = state.cash.toString()
        )
    }
}

class BuyYachtScreen : Screen {
    @Composable
    override fun Content() {
        val state by store.observeState().collectAsState()
        InputScreen(
            inputLabel = "Ціна яхти",
            buttonText = "Купити",
            validation = { price -> price.isNotEmpty() && state.cash >= price.toLong() },
            onClick = { price -> store.dispatch(AppAction.BuyYacht(price = price.toLong())) },
            value = state.cash.toString()
        )
    }
}

class BuyFlightScreen : Screen {
    @Composable
    override fun Content() {
        val state by store.observeState().collectAsState()
        InputScreen(
            inputLabel = "Ціна літака",
            buttonText = "Купити",
            validation = { price -> price.isNotEmpty() && state.cash >= price.toLong() },
            onClick = { price -> store.dispatch(AppAction.BuyFlight(price = price.toLong())) },
            value = state.cash.toString()
        )
    }
}