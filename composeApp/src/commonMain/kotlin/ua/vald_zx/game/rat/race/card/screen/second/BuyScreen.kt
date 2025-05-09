package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.screen.InputScreen
import ua.vald_zx.game.rat.race.card.raceRate2store

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
        val state by raceRate2store.observeState().collectAsState()
        InputScreen(
            inputLabel = "Ціна авто",
            buttonText = "Купити",
            validation = { price -> price.isNotEmpty() },
            onClick = { price -> raceRate2store.dispatch(RatRace2CardAction.BuyCar(price = price.toLong())) },
            value = state.cash.toString()
        )
    }
}

class BuyApartmentScreen : Screen {
    @Composable
    override fun Content() {
        val state by raceRate2store.observeState().collectAsState()
        InputScreen(
            inputLabel = "Ціна квартири",
            buttonText = "Купити",
            validation = { price -> price.isNotEmpty() },
            onClick = { price -> raceRate2store.dispatch(RatRace2CardAction.BuyApartment(price = price.toLong())) },
            value = state.cash.toString()
        )
    }
}

class BuyCottageScreen : Screen {
    @Composable
    override fun Content() {
        val state by raceRate2store.observeState().collectAsState()
        InputScreen(
            inputLabel = "Ціна будинку",
            buttonText = "Купити",
            validation = { price -> price.isNotEmpty() },
            onClick = { price -> raceRate2store.dispatch(RatRace2CardAction.BuyCottage(price = price.toLong())) },
            value = state.cash.toString()
        )
    }
}

class BuyYachtScreen : Screen {
    @Composable
    override fun Content() {
        val state by raceRate2store.observeState().collectAsState()
        InputScreen(
            inputLabel = "Ціна яхти",
            buttonText = "Купити",
            validation = { price -> price.isNotEmpty() },
            onClick = { price -> raceRate2store.dispatch(RatRace2CardAction.BuyYacht(price = price.toLong())) },
            value = state.cash.toString()
        )
    }
}

class BuyFlightScreen : Screen {
    @Composable
    override fun Content() {
        val state by raceRate2store.observeState().collectAsState()
        InputScreen(
            inputLabel = "Ціна літака",
            buttonText = "Купити",
            validation = { price -> price.isNotEmpty() },
            onClick = { price -> raceRate2store.dispatch(RatRace2CardAction.BuyFlight(price = price.toLong())) },
            value = state.cash.toString()
        )
    }
}