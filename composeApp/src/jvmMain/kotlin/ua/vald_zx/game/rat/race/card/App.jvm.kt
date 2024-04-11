package ua.vald_zx.game.rat.race.card

import net.harawata.appdirs.AppDirsFactory
import java.awt.Desktop
import java.net.URI

internal actual fun openUrl(url: String?) {
    val uri = url?.let { URI.create(it) } ?: return
    Desktop.getDesktop().browse(uri)
}

internal actual val storageDir: String
    get() = AppDirsFactory.getInstance()
        .getUserDataDir("ua.vald_zx.game.rat.race.card", "1.0", "VALD_ZX")