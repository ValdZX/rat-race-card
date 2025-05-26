@file:OptIn(ExperimentalVoiceApi::class)

package ua.vald_zx.game.rat.race.card

import android.app.Application
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import io.github.aakira.napier.DebugAntilog
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
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction

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

class AppActivity : ComponentActivity() {
    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            val resultCode = result.resultCode
            when (resultCode) {
                RESULT_OK -> {
                    Napier.d("Update flow completed!")
                }

                RESULT_CANCELED -> {
                    Napier.d("User cancelled Update flow!")
                }

                else -> {
                    Napier.d("Update flow failed with resultCode:$resultCode")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent {
            App()
            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

            LaunchedEffect(lifecycleState) {
                Napier.d(lifecycleState.toString())
                if (lifecycleState == Lifecycle.State.RESUMED) {
                    raceRate2store.dispatch(RatRace2CardAction.OnResume)
                }
                if (lifecycleState == Lifecycle.State.CREATED) {
                    raceRate2store.dispatch(RatRace2CardAction.OnPause)
                }
            }
        }
        AndroidApp.ACTIVITY = this
    }

    override fun onResume() {
        super.onResume()
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    // If an in-app update is already running, resume the update.
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        activityResultLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    )
                }
            }
    }
}

internal actual val platformContext: Any
    get() = AndroidApp.ACTIVITY

@Suppress("unused")
internal actual fun openUrl(url: String?) {
    val uri = url?.toUri() ?: return
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
    return storeOf(file = Path("$storageDir/$name"), json = Json { ignoreUnknownKeys = true })
}

internal actual fun share(data: String?) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, data)
        type = "text/plain"
    }
    AndroidApp.ACTIVITY.startActivity(
        Intent.createChooser(shareIntent, AndroidApp.ACTIVITY.getString(R.string.app_name))
    )
}


private var tts: TextToSpeechInstance? = null
actual suspend fun getTts(): TextToSpeechInstance? {
    if (tts != null) return tts
    TextToSpeechFactory(AndroidApp.ACTIVITY, TextToSpeechEngine.Google).create()
        .onSuccess { newTts ->
            newTts.voices.find { it.language == "Ukrainian" }?.let { newTts.currentVoice = it }
            tts = newTts
        }.onFailure {
            Napier.e("tts failed", it)
        }
    return tts
}

actual val noIme: Boolean = false