import androidx.compose.ui.window.ComposeUIViewController
import ua.vald_zx.game.rat.race.card.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
