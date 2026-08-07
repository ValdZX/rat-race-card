import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import ua.vald_zx.game.rat.race.card.App
import ua.vald_zx.game.rat.race.card.BuildConfig

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    println(
        "Rat race client v${BuildConfig.APP_VERSION} (${BuildConfig.BUILD_COMMIT}) built ${BuildConfig.BUILD_TIME}"
    )
    ComposeViewport(document.body!!) {
        App()
    }
}
