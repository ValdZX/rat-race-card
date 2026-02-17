import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ua.vald_zx.game.rat.race.card.App
import ua.vald_zx.game.rat.race.card.storageKeyPrefix
import java.awt.Dimension

fun main() {
    storageKeyPrefix = System.getenv("storage")
    application {
        Window(
            title = "Cashflow: Rat Race Edition",
            state = rememberWindowState(width = 800.dp, height = 600.dp),
            onCloseRequest = ::exitApplication,
        ) {
            window.minimumSize = Dimension(800, 600)
            App()
        }
    }
}