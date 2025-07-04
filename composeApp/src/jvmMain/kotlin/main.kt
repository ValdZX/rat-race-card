import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import ua.vald_zx.game.rat.race.card.App
import java.awt.Dimension

fun main() {
    Napier.base(DebugAntilog())
    application {
        Window(
            title = "Cashflow: Rat Race Edition",
            state = rememberWindowState(width = 300.dp, height = 700.dp),
            onCloseRequest = ::exitApplication,
        ) {
            window.minimumSize = Dimension(100, 100)
            App()
        }
    }
}