package ua.vald_zx.game.rat.race.card

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.navigator.Navigator
import ua.vald_zx.game.rat.race.card.logic.AppStore
import ua.vald_zx.game.rat.race.card.theme.AppTheme

val store = AppStore()

@Composable
internal fun App() = AppTheme {
    val state by store.observeState().collectAsState()
    val startScreen = if (state.professionCard.profession.isNotEmpty()) {
        MainScreen()
    } else {
        FillProfessionCardScreen()
    }
    Navigator(startScreen)
}


internal expect fun openUrl(url: String?)
