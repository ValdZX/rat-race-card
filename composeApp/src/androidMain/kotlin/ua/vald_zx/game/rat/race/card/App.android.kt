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
    var mediaPlayer: MediaPlayer? = null
    var textToSpeech: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { App() }
        AndroidApp.ACTIVITY = this
        mediaPlayer = MediaPlayer.create(this, R.raw.coin)
        textToSpeech = TextToSpeech(this, object : OnInitListener {
            override fun onInit(status: Int) {

                if (status == TextToSpeech.SUCCESS) {
                    val result = if (ttsIsUkraineSupported()) {
                        textToSpeech?.setLanguage(uk)
                    } else {
                        textToSpeech?.setLanguage(Locale.UK)
                    }

                    // tts.setPitch(5); // set pitch level

                    // tts.setSpeechRate(2); // set speech speed rate

                    if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED
                    ) {
                        Napier.e("Language is not supported")
                        textToSpeech?.setLanguage(Locale.UK)
                    } else {
                        Napier.d("TTS inited")
                    }

                } else {
                    Napier.e("Initilization Failed");
                }
            }
        })
    }
}

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

internal actual fun playCoin() {
    AndroidApp.ACTIVITY.mediaPlayer?.start()
}


internal actual fun ttsIsUkraineSupported(): Boolean {
    return AndroidApp.ACTIVITY.textToSpeech?.isLanguageAvailable(uk) == LANG_COUNTRY_AVAILABLE
}

internal actual fun tts(string: String) {
    AndroidApp.ACTIVITY.textToSpeech?.speak(string, TextToSpeech.QUEUE_FLUSH, null, null)
}