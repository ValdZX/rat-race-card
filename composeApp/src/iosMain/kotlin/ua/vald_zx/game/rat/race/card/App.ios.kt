package ua.vald_zx.game.rat.race.card

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.InternalComposeUiApi
import app.lexilabs.basic.haptic.DependsOnAndroidVibratePermission
import app.lexilabs.basic.haptic.Haptic
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
import platform.Foundation.*
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
            newTts.voices.find { it.languageTag == "uk-UA" }?.let { newTts.currentVoice = it }
            tts = newTts
        }.onFailure {
            Napier.e("tts failed", it)
        }
    return tts
}

actual val noIme: Boolean = false

@OptIn(DependsOnAndroidVibratePermission::class)
val haptic by lazy {
    Haptic(platformContext)
}

actual fun vibrateClick() {
    @OptIn(DependsOnAndroidVibratePermission::class)
    haptic.vibrate(Haptic.DEFAULTS.CLICK)
}

@OptIn(InternalComposeUiApi::class)
actual object LocalAppLocale {
    private const val LANG_KEY = "AppleLanguages"
    private val default = NSLocale.preferredLanguages.first() as String
    private val LocalAppLocale = staticCompositionLocalOf { default }
    actual val current: String
        @Composable get() = LocalAppLocale.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val new = value ?: default
        if (value == null) {
            NSUserDefaults.standardUserDefaults.removeObjectForKey(LANG_KEY)
        } else {
            NSUserDefaults.standardUserDefaults.setObject(arrayListOf(new), LANG_KEY)
        }
        return LocalAppLocale.provides(new)
    }
}