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
    val sound: Boolean = true,
    val language: String? = null,
)

val designV2Enabled = mutableStateOf(true)

val soundEnabled = mutableStateOf(true)

var storageKeyPrefix = ""

val appKStore: KStore<AppDataStorageBean>
    get() = getStore("${storageKeyPrefix}_appData.json", AppDataStorageBean("", null))

enum class GameSound(internal val file: String) {
    Coin("coin.mp3"),
    PlaceService("place_service.mp3"),
    PlaceCard("place_card.mp3"),
    PlaceLoss("place_loss.mp3"),
    PlaceAsset("place_asset.mp3"),
    PlaceLife("place_life.mp3"),
    TokenStep("token_step.mp3"),
    DiceRoll("dice_roll.wav"),
}

@OptIn(ExperimentalBasicSound::class)
private val soundBoard = SoundBoard(platformContext).apply {
    GameSound.entries.forEach { sound ->
        load(SoundByte(name = sound.name, localPath = Res.getUri("files/${sound.file}")))
    }
    try {
        powerUp()
    } catch (e: Exception) {
        Napier.e("sound error", e)
    }
}

@OptIn(ExperimentalBasicSound::class)
fun play(sound: GameSound) {
    if (!soundEnabled.value) return
    try {
        soundBoard.mixer.play(sound.name)
    } catch (e: Exception) {
        Napier.e("sound error", e)
    }
}

fun playCoin() = play(GameSound.Coin)

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
