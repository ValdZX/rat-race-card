package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardStore
import ua.vald_zx.game.rat.race.card.screen.InputScreen

class BuyScreen() : Screen {
    @Composable
    override fun Content() {
        val raceRate2store = koinInject<RatRace2CardStore>()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        BottomSheetContainer {
            Button(stringResource(Res.string.buy_car)) {
                bottomSheetNavigator.replace(BuyCarScreen())
            }
            Button(stringResource(Res.string.buy_apartment)) {
                bottomSheetNavigator.replace(BuyApartmentScreen())
            }
            Button(stringResource(Res.string.buy_house)) {
                bottomSheetNavigator.replace(BuyCottageScreen())
            }
            Button(stringResource(Res.string.buy_yacht)) {
                bottomSheetNavigator.replace(BuyYachtScreen())
            }
            Button(stringResource(Res.string.buy_plane)) {
                bottomSheetNavigator.replace(BuyFlightScreen())
            }
        }
    }
}

class BuyCarScreen : Screen {
    @Composable
    override fun Content() {
        val raceRate2store = koinInject<RatRace2CardStore>()
        InputScreen(
            inputLabel = "Ціна авто",
            buttonText = stringResource(Res.string.buy),
            validation = { price -> price.isNotEmpty() },
            onClick = { price -> raceRate2store.dispatch(RatRace2CardAction.BuyCar(price = price.toLong())) },
            value = ""
        )
    }
}

class BuyApartmentScreen : Screen {
    @Composable
    override fun Content() {
        val raceRate2store = koinInject<RatRace2CardStore>()
        InputScreen(
            inputLabel = "Ціна квартири",
            buttonText = stringResource(Res.string.buy),
            validation = { price -> price.isNotEmpty() },
            onClick = { price -> raceRate2store.dispatch(RatRace2CardAction.BuyApartment(price = price.toLong())) },
            value = ""
        )
    }
}

class BuyCottageScreen : Screen {
    @Composable
    override fun Content() {
        val raceRate2store = koinInject<RatRace2CardStore>()
        InputScreen(
            inputLabel = "Ціна будинку",
            buttonText = stringResource(Res.string.buy),
            validation = { price -> price.isNotEmpty() },
            onClick = { price -> raceRate2store.dispatch(RatRace2CardAction.BuyCottage(price = price.toLong())) },
            value = ""
        )
    }
}

class BuyYachtScreen : Screen {
    @Composable
    override fun Content() {
        val raceRate2store = koinInject<RatRace2CardStore>()
        InputScreen(
            inputLabel = "Ціна яхти",
            buttonText = stringResource(Res.string.buy),
            validation = { price -> price.isNotEmpty() },
            onClick = { price -> raceRate2store.dispatch(RatRace2CardAction.BuyYacht(price = price.toLong())) },
            value = ""
        )
    }
}

class BuyFlightScreen : Screen {
    @Composable
    override fun Content() {
        val raceRate2store = koinInject<RatRace2CardStore>()
        InputScreen(
            inputLabel = "Ціна літака",
            buttonText = stringResource(Res.string.buy),
            validation = { price -> price.isNotEmpty() },
            onClick = { price -> raceRate2store.dispatch(RatRace2CardAction.BuyFlight(price = price.toLong())) },
            value = ""
        )
    }
}