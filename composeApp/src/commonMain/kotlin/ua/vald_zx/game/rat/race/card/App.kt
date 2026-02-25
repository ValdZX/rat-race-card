@file:OptIn(InternalCompottieApi::class)

package ua.vald_zx.game.rat.race.card

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import app.lexilabs.basic.sound.ExperimentalBasicSound
import app.lexilabs.basic.sound.SoundBoard
import app.lexilabs.basic.sound.SoundByte
import app.lexilabs.basic.sound.play
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.github.alexzhirkevich.compottie.*
import io.github.sudarshanmhasrup.localina.api.LocalinaApp
import io.github.xxfast.kstore.KStore
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import nl.marc_apps.tts.TextToSpeechInstance
import nl.marc_apps.tts.experimental.ExperimentalVoiceApi
import org.koin.compose.KoinApplication
import rat_race_card.composeapp.generated.resources.Res
import ua.vald_zx.game.rat.race.card.di.baseModule
import ua.vald_zx.game.rat.race.card.screen.SelectTypeScreen
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark

var lottieDiceAnimations: Map<Int, LottieComposition> = emptyMap()

@Serializable
data class AppDataStorageBean(
    val clientUuid: String,
    val theme: Boolean?,
)

var storageKeyPrefix = ""

val appKStore: KStore<AppDataStorageBean>
    get() = getStore("${storageKeyPrefix}_appData.json", AppDataStorageBean("", null))

@Composable
internal fun App() {
    KoinApplication(application = { modules(baseModule) }) {
        val lottieCache = LocalLottieCache.current
        LaunchedEffect(Unit) {
            lottieDiceAnimations = (1..6).associate { side ->
                withContext(Compottie.ioDispatcher()) {
                    val specInstance = LottieCompositionSpec.JsonString(
                        Res.readBytes("files/cube_$side.json").decodeToString()
                    )
                    side to lottieCache.getOrPut(specInstance.key, specInstance::load)
                }
            }
        }
//        Navigator(SelectTypeScreen()) { navigator ->
//            LocalinaApp {
//                AppTheme {
//                    CurrentScreen()
//                }
//            }
//        }

        AppTheme {
            val raceRate2store = koinInject<RatRace2CardStore>()
            var kStoreLoaded by remember { mutableStateOf(false) }
            if (kStoreLoaded) {
                val raceRate2State by raceRate2store.observeState().collectAsState()
                val hasProfession = raceRate2State.playerCard.profession.isNotEmpty()
                val startScreen = if (hasProfession) RaceRate2Screen() else PersonCard2Screen()
                Navigator(startScreen) {
                    LocalinaApp {
                        AppTheme {
                            CurrentScreen()
                        }
                    }
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
