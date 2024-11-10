import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import ua.vald_zx.game.rat.race.card.App

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
