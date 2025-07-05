package ua.vald_zx.game.rat.race.card

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.lexilabs.basic.sound.ExperimentalBasicSound
import app.lexilabs.basic.sound.SoundBoard
import app.lexilabs.basic.sound.SoundByte
import app.lexilabs.basic.sound.play
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.github.xxfast.kstore.KStore
import kotlinx.serialization.Serializable
import nl.marc_apps.tts.TextToSpeechInstance
import nl.marc_apps.tts.experimental.ExperimentalVoiceApi
import rat_race_card.composeapp.generated.resources.Res
import ua.vald_zx.game.rat.race.card.logic.BoardStore
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardState
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardStore
import ua.vald_zx.game.rat.race.card.logic.Statistics
import ua.vald_zx.game.rat.race.card.screen.LoadScreen
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark

internal expect inline fun <reified T : @Serializable Any> getStore(name: String): KStore<T>
val raceRate2KStore: KStore<RatRace2CardState>
    get() = getStore("raceRate2.json")
val statistics2KStore: KStore<Statistics>
    get() = getStore("statistics2.json")
val raceRate2store by lazy { RatRace2CardStore() }
val raceRate2BoardStore by lazy { BoardStore() }
val settings: Settings = Settings()
val invalidServer = mutableStateOf(false)

@Composable
internal fun App() {
    var isDarkTheme by LocalThemeIsDark.current
    LaunchedEffect(Unit) {
        Napier.base(DebugAntilog())
        val systemIsDark = settings["theme", isDarkTheme]
        isDarkTheme = systemIsDark
    }
    AppTheme {
        var kStoreLoaded by remember { mutableStateOf(false) }
        if (kStoreLoaded) {
            Navigator(LoadScreen()) { navigator ->
                CurrentScreen()
                StartServices(navigator)
            }
        } else {
            LaunchedEffect(Unit) {
                val state2 = runCatching { raceRate2KStore.get() }.getOrNull()
                if (state2 != null) {
                    raceRate2store.dispatch(RatRace2CardAction.LoadState(state2))
                }
                kStoreLoaded = true
            }
        }
    }
}

@OptIn(ExperimentalBasicSound::class)
private val soundBoard = SoundBoard(platformContext).apply {
    val coin = SoundByte(
        name = "coin",
        localPath = Res.getUri("files/coin.mp3")
    )
    load(coin)
    try {
        powerUp()
    } catch (e: Exception) {
        Napier.e("sound error", e)
    }
}

@OptIn(ExperimentalBasicSound::class)
internal fun playCoin() {
    soundBoard.mixer.play("coin")
}

internal expect val noIme: Boolean
internal expect val platformContext: Any
internal expect fun openUrl(url: String?)
internal expect fun share(data: String?)
internal expect suspend fun getTts(): TextToSpeechInstance?

@OptIn(ExperimentalVoiceApi::class)
internal suspend fun ttsIsUkraineSupported(): Boolean {
    val voices = getTts()?.voices
    return voices?.find { it.languageTag == "uk-UA" } != null
}

internal suspend fun tts(string: String) {
    runCatching {
        getTts()?.say(string)
    }
}

expect fun vibrateClick()
