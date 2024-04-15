package ua.vald_zx.game.rat.race.card

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.navigator.Navigator
import com.russhwolf.settings.Settings
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.storeOf
import okio.Path.Companion.toPath
import ua.vald_zx.game.rat.race.card.logic.AppAction
import ua.vald_zx.game.rat.race.card.logic.AppState
import ua.vald_zx.game.rat.race.card.logic.AppStore
import ua.vald_zx.game.rat.race.card.theme.AppTheme

internal expect val storageDir: String
val kStore: KStore<AppState>
    get() = storeOf(file = "$storageDir/app.json".toPath())
val store = AppStore()
val settings: Settings = Settings()

@Composable
internal fun App() = AppTheme {
    var kStoreLoaded by remember { mutableStateOf(false) }
    if (kStoreLoaded) {
        val state by store.observeState().collectAsState()
        val startScreen = if (state.professionCard.profession.isNotEmpty()) {
            MainScreen()
        } else {
            FillProfessionCardScreen()
        }
        Navigator(startScreen)
    } else {
        LaunchedEffect(Unit) {
            val state = kStore.get()
            if (state != null) {
                store.dispatch(AppAction.LoadState(state))
            }
            kStoreLoaded = true
        }
    }
}


internal expect fun openUrl(url: String?)
internal expect fun share(data: String?)
