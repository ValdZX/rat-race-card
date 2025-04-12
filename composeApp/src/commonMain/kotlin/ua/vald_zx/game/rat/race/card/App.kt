package ua.vald_zx.game.rat.race.card

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import app.lexilabs.basic.sound.AudioByte
import app.lexilabs.basic.sound.ExperimentalBasicSound
import cafe.adriel.voyager.navigator.Navigator
import com.russhwolf.settings.Settings
import io.github.aakira.napier.Napier
import io.github.xxfast.kstore.KStore
import kotlinx.serialization.Serializable
import nl.marc_apps.tts.TextToSpeechInstance
import nl.marc_apps.tts.experimental.ExperimentalVoiceApi
import rat_race_card.composeapp.generated.resources.Res
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardState
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardStore
import ua.vald_zx.game.rat.race.card.logic.RatRace4CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace4CardState
import ua.vald_zx.game.rat.race.card.logic.RatRace4CardStore
import ua.vald_zx.game.rat.race.card.logic.Statistics
import ua.vald_zx.game.rat.race.card.screen.second.PersonCard2Screen
import ua.vald_zx.game.rat.race.card.screen.second.RaceRate2Screen
import ua.vald_zx.game.rat.race.card.theme.AppTheme

internal expect inline fun <reified T : @Serializable Any> getStore(name: String): KStore<T>
val raceRate2KStore: KStore<RatRace2CardState>
    get() = getStore("raceRate2.json")
val statistics2KStore: KStore<Statistics>
    get() = getStore("statistics2.json")
val raceRate4KStore: KStore<RatRace4CardState>
    get() = getStore("raceRate4.json")
val raceRate2store by lazy { RatRace2CardStore() }
val raceRate4store by lazy { RatRace4CardStore() }
val settings: Settings = Settings()

@Composable
internal fun App() = AppTheme {
    var kStoreLoaded by remember { mutableStateOf(false) }
    if (kStoreLoaded) {
        val raceRate2State by raceRate2store.observeState().collectAsState()
        val startScreen =
            if (raceRate2State.professionCard.profession.isNotEmpty()) {
                RaceRate2Screen()
            } else {
                PersonCard2Screen()
            }
        Navigator(startScreen)
//        Navigator(SelectBoardScreen())
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

private val audioByte by lazy {
    AudioByte()
}

private val coin: Any by lazy {
    audioByte.load(platformContext, Res.getUri("files/coin.mp3"))
}

@OptIn(ExperimentalBasicSound::class)
internal fun playCoin() {
    audioByte.play(coin)
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
