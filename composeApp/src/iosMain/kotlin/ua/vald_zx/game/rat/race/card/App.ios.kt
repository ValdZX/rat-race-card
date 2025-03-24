package ua.vald_zx.game.rat.race.card

import io.github.aakira.napier.Napier
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.storeOf
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import nl.marc_apps.tts.TextToSpeechFactory
import nl.marc_apps.tts.TextToSpeechInstance
import nl.marc_apps.tts.experimental.ExperimentalVoiceApi
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

internal actual val platformContext: Any
    get() = Unit

internal actual fun openUrl(url: String?) {
    val nsUrl = url?.let { NSURL.URLWithString(it) } ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}

val fileManager: NSFileManager = NSFileManager.defaultManager

@OptIn(ExperimentalForeignApi::class)
val cachesUrl: NSURL = fileManager.URLForDirectory(
    directory = NSCachesDirectory,
    appropriateForURL = null,
    create = false,
    inDomain = NSUserDomainMask,
    error = null
)!!

internal actual inline fun <reified T : @Serializable Any> getStore(name: String): KStore<T> {
    return storeOf(file = Path("$cachesUrl/$name"))
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
            newTts.voices.find { it.language == "Ukrainian" }?.let { newTts.currentVoice = it }
            tts = newTts
        }.onFailure {
            Napier.e("tts failed", it)
        }
    return tts
}

actual val noIme: Boolean = false