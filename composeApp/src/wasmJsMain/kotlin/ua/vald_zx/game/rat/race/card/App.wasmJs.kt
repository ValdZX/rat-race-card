package ua.vald_zx.game.rat.race.card

import io.github.aakira.napier.Napier
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.storage.storeOf
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import nl.marc_apps.tts.TextToSpeechFactory
import nl.marc_apps.tts.TextToSpeechInstance
import nl.marc_apps.tts.experimental.ExperimentalVoiceApi

internal actual val platformContext: Any
    get() = Unit

@Suppress("unused")
internal actual fun openUrl(url: String?) {
    url?.let { window.open(it) }
}

actual inline fun <reified T : @Serializable Any> getStore(name: String, default: T?): KStore<T> {
    return storeOf(name, format = Json { ignoreUnknownKeys = true }, default = default)
}

actual fun share(data: String?) {
    //nop
}

private var tts: TextToSpeechInstance? = null

@OptIn(ExperimentalVoiceApi::class)
actual suspend fun getTts(): TextToSpeechInstance? {
    if (tts != null) return tts
    TextToSpeechFactory().create()
        .onSuccess { newTts ->
            newTts.voices.find { it.languageTag == "uk-UA" }?.let { newTts.currentVoice = it }
            tts = newTts
        }.onFailure {
            Napier.e("tts failed", it)
        }
    return tts
}

actual val noIme: Boolean = true
actual fun vibrateClick() {
}