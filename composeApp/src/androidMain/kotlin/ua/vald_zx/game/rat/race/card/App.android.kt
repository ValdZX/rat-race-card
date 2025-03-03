@file:OptIn(ExperimentalVoiceApi::class)

package ua.vald_zx.game.rat.race.card

import android.app.Application
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.LANG_COUNTRY_AVAILABLE
import android.speech.tts.TextToSpeech.OnInitListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.storeOf
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import nl.marc_apps.tts.TextToSpeechEngine
import nl.marc_apps.tts.TextToSpeechFactory
import nl.marc_apps.tts.TextToSpeechInstance
import nl.marc_apps.tts.experimental.ExperimentalVoiceApi
import java.util.Locale

class AndroidApp : Application() {
    companion object {
        lateinit var INSTANCE: AndroidApp
        lateinit var ACTIVITY: AppActivity
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        Napier.base(DebugAntilog())
    }
}

val uk = Locale("uk", "UA")

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { App() }
        AndroidApp.ACTIVITY = this
    }
}

internal actual val platformContext: Any
    get() = AndroidApp.ACTIVITY

internal actual fun openUrl(url: String?) {
    val uri = url?.let { Uri.parse(it) } ?: return
    val intent = Intent().apply {
        action = Intent.ACTION_VIEW
        data = uri
        addFlags(FLAG_ACTIVITY_NEW_TASK)
    }
    AndroidApp.INSTANCE.startActivity(intent)
}

internal val storageDir: String
    get() = AndroidApp.INSTANCE.filesDir.path

internal actual inline fun <reified T : @Serializable Any> getStore(name: String): KStore<T> {
    return storeOf(file = Path("$storageDir/$name"))
}

internal actual fun share(data: String?) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, data)
        type = "text/plain"
    }
    AndroidApp.ACTIVITY.startActivity(
        Intent.createChooser(shareIntent, "Rat race card")
    )
}


private var tts: TextToSpeechInstance? = null
actual suspend fun getTts(): TextToSpeechInstance? {
    if (tts != null) return tts
    TextToSpeechFactory(AndroidApp.ACTIVITY, TextToSpeechEngine.SystemDefault).create()
        .onSuccess { newTts ->
            newTts.voices.find { it.language == "Ukrainian" }?.let { newTts.currentVoice = it }
            tts = newTts
        }.onFailure {
            Napier.e("tts failed", it)
        }
    return tts
}