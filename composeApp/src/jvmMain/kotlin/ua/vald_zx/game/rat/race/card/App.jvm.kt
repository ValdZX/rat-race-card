package ua.vald_zx.game.rat.race.card

import io.github.aakira.napier.Napier
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.storeOf
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.harawata.appdirs.AppDirsFactory
import nl.marc_apps.tts.TextToSpeechFactory
import nl.marc_apps.tts.TextToSpeechInstance
import nl.marc_apps.tts.experimental.ExperimentalDesktopTarget
import nl.marc_apps.tts.experimental.ExperimentalVoiceApi
import java.awt.Desktop
import java.net.URI

internal actual val platformContext: Any
    get() = Unit

internal actual fun openUrl(url: String?) {
    val uri = url?.let { URI.create(it) } ?: return
    Desktop.getDesktop().browse(uri)
}

val storageDir: String by lazy {
    AppDirsFactory.getInstance()
        .getUserDataDir("ua.vald_zx.game.rat.race.card", "1.0", "VALD_ZX").apply {
            val path = Path(this)
            with(SystemFileSystem) { if (!exists(path)) createDirectories(path) }
        }
}

internal actual fun share(data: String?) {
}

private var tts: TextToSpeechInstance? = null

@OptIn(ExperimentalVoiceApi::class, ExperimentalDesktopTarget::class)
actual suspend fun getTts(): TextToSpeechInstance? {
    if (tts != null) return tts
    TextToSpeechFactory().create()
        .onSuccess { newTts ->
            newTts.voices.find { it.language == "Ukrainian" }?.let { newTts.currentVoice = it }
            tts = newTts
        }.onFailure {
            Napier.e("tts failed", it)
        }
    return tts
}

actual inline fun <reified T : @Serializable Any> getStore(name: String, default: T?): KStore<T> {
    return storeOf(file = Path("$storageDir/$name"), json = Json { ignoreUnknownKeys = true }, default = default)
}

actual val noIme: Boolean = false
actual fun vibrateClick() {
}