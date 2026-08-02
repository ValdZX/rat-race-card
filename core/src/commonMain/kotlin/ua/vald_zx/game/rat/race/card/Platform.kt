package ua.vald_zx.game.rat.race.card

import app.lexilabs.basic.sound.ExperimentalBasicSound
import app.lexilabs.basic.sound.SoundBoard
import app.lexilabs.basic.sound.SoundByte
import app.lexilabs.basic.sound.play
import io.github.aakira.napier.Napier
import io.github.alexzhirkevich.compottie.LottieComposition
import androidx.compose.runtime.mutableStateOf
import io.github.xxfast.kstore.KStore
import kotlinx.serialization.Serializable
import nl.marc_apps.tts.TextToSpeechInstance
import nl.marc_apps.tts.experimental.ExperimentalVoiceApi
import ua.vald_zx.game.rat.race.card.resources.Res

var lottieDiceAnimations: Map<Int, LottieComposition> = emptyMap()

@Serializable
data class AppDataStorageBean(
    val clientUuid: String,
    val theme: Boolean?,
    val designV2: Boolean = false,
)

val designV2Enabled = mutableStateOf(false)

var storageKeyPrefix = ""

val appKStore: KStore<AppDataStorageBean>
    get() = getStore("${storageKeyPrefix}_appData.json", AppDataStorageBean("", null))

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
fun playCoin() {
    soundBoard.mixer.play("coin")
}

expect val noIme: Boolean
expect val platformContext: Any
expect fun openUrl(url: String?)
expect fun share(data: String?)
expect suspend fun getTts(): TextToSpeechInstance?

@OptIn(ExperimentalVoiceApi::class)
suspend fun ttsIsUkraineSupported(): Boolean {
    val voices = getTts()?.voices
    return voices?.find { it.languageTag == "uk-UA" } != null
}

suspend fun tts(string: String) {
    runCatching {
        getTts()?.say(string)
    }
}

expect fun vibrateClick()
