package ua.vald_zx.game.rat.race.card

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.navigator.Navigator
import com.russhwolf.settings.Settings
import io.github.xxfast.kstore.KStore
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardState
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardStore
import ua.vald_zx.game.rat.race.card.logic.RatRace4CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace4CardState
import ua.vald_zx.game.rat.race.card.logic.RatRace4CardStore
import ua.vald_zx.game.rat.race.card.screen.SelectBoardScreen
import ua.vald_zx.game.rat.race.card.theme.AppTheme

internal expect inline fun <reified T : @Serializable Any> getStore(name: String): KStore<T>
val raceRate2KStore: KStore<RatRace2CardState>
    get() = getStore("raceRate2.json")
val raceRate4KStore: KStore<RatRace4CardState>
    get() = getStore("raceRate4.json")
val raceRate2store = RatRace2CardStore()
val raceRate4store = RatRace4CardStore()
val settings: Settings = Settings()

@Composable
internal fun App() = AppTheme {
    var kStoreLoaded by remember { mutableStateOf(false) }
    if (kStoreLoaded) {
        Navigator(SelectBoardScreen())
    } else {
        LaunchedEffect(Unit) {
            val state2 = raceRate2KStore.get()
            if (state2 != null) {
                raceRate2store.dispatch(RatRace2CardAction.LoadState(state2))
            }
            val state4 = raceRate4KStore.get()
            if (state4 != null) {
                raceRate4store.dispatch(RatRace4CardAction.LoadState(state4))
            }
            kStoreLoaded = true
        }
    }
}

internal expect fun openUrl(url: String?)
internal expect fun share(data: String?)
internal expect fun playCoin()
internal expect fun ttsIsUkraineSupported(): Boolean
internal expect fun tts(string: String)
