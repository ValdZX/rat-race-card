import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import ua.vald_zx.game.rat.race.card.App
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.raceRate2store
import java.awt.Dimension

fun main() {
    Napier.base(DebugAntilog())
    application {
        Window(
            title = "Cashflow: Rat Race Edition",
            state = rememberWindowState(width = 800.dp, height = 500.dp),
            onCloseRequest = ::exitApplication,
        ) {
            window.minimumSize = Dimension(350, 650)
            App()
            LaunchedEffect(Unit) {
                raceRate2store.dispatch(RatRace2CardAction.OnResume)
            }
        }
    }
}