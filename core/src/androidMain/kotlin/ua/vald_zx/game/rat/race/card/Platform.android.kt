@file:OptIn(ExperimentalVoiceApi::class)

package ua.vald_zx.game.rat.race.card

import android.app.Application
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import app.lexilabs.basic.haptic.DependsOnAndroidVibratePermission
import app.lexilabs.basic.haptic.Haptic
import io.github.aakira.napier.Napier
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.storeOf
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import nl.marc_apps.tts.TextToSpeechEngine
import nl.marc_apps.tts.TextToSpeechFactory
import nl.marc_apps.tts.TextToSpeechInstance
import nl.marc_apps.tts.experimental.ExperimentalVoiceApi

object AndroidPlatform {
    lateinit var application: Application
    lateinit var activity: ComponentActivity
}

actual val platformContext: Any
    get() = AndroidPlatform.activity

@Suppress("unused")
actual fun openUrl(url: String?) {
    val uri = url?.toUri() ?: return
    val intent = Intent().apply {
        action = Intent.ACTION_VIEW
        data = uri
        addFlags(FLAG_ACTIVITY_NEW_TASK)
    }
    AndroidPlatform.application.startActivity(intent)
}

val storageDir: String
    get() = AndroidPlatform.application.filesDir.path

actual inline fun <reified T : @Serializable Any> getStore(name: String, default: T?): KStore<T> {
    return storeOf(file = Path("$storageDir/$name"), json = Json { ignoreUnknownKeys = true }, default = default)
}

actual fun share(data: String?) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, data)
        type = "text/plain"
    }
    val activity = AndroidPlatform.activity
    val appLabel = activity.applicationInfo.loadLabel(activity.packageManager)
    activity.startActivity(Intent.createChooser(shareIntent, appLabel))
}

private var tts: TextToSpeechInstance? = null
actual suspend fun getTts(): TextToSpeechInstance? {
    if (tts != null) return tts
    TextToSpeechFactory(AndroidPlatform.activity, TextToSpeechEngine.Google).create()
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
