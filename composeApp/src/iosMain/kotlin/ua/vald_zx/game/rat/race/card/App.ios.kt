package ua.vald_zx.game.rat.race.card

import io.github.aakira.napier.Napier
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.storeOf
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import nl.marc_apps.tts.TextToSpeechFactory
import nl.marc_apps.tts.TextToSpeechInstance
import nl.marc_apps.tts.experimental.ExperimentalVoiceApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

internal actual val platformContext: Any
    get() = Unit

@Suppress("unused")
internal actual fun openUrl(url: String?) {
    val nsUrl = url?.let { NSURL.URLWithString(it) } ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}

val fileManager: NSFileManager = NSFileManager.defaultManager

@OptIn(ExperimentalForeignApi::class)
val documentsUrl: NSURL = fileManager.URLForDirectory(
    directory = NSDocumentDirectory,
    appropriateForURL = null,
    create = false,
    inDomain = NSUserDomainMask,
    error = null
)!!

internal actual inline fun <reified T : @Serializable Any> getStore(name: String): KStore<T> {
    val files = Path(documentsUrl.path.orEmpty())
    return storeOf(file = Path(files, name), json = Json { ignoreUnknownKeys = true })
}

internal actual fun share(data: String?) {
    val activityItems = listOf(data)
    val activityViewController = UIActivityViewController(activityItems, null)

    val application = UIApplication.sharedApplication
    application.keyWindow?.rootViewController?.presentViewController(
        activityViewController,
        animated = true,
        completion = null
    )
}

private var tts: TextToSpeechInstance? = null

@OptIn(ExperimentalVoiceApi::class)
actual suspend fun getTts(): TextToSpeechInstance? {
    if (tts != null) return tts
    TextToSpeechFactory().create()
        .onSuccess { newTts ->
            Napier.d("SUCCESS ${newTts.voices.count()}")
            newTts.voices.forEach { Napier.d("${it.name}, ${it.language}, ${it.region}, ${it.languageTag}") }
            newTts.voices.find { it.languageTag == "uk-UA" }?.let { newTts.currentVoice = it }
            tts = newTts
        }.onFailure {
            Napier.e("tts failed", it)
        }
    return tts
}

actual val noIme: Boolean = false