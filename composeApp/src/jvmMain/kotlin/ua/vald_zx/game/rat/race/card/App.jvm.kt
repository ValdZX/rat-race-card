package ua.vald_zx.game.rat.race.card

import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.storeOf
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import net.harawata.appdirs.AppDirsFactory
import java.awt.Desktop
import java.net.URI

internal actual val platformContext: Any
    get() = Unit

internal actual fun openUrl(url: String?) {
    val uri = url?.let { URI.create(it) } ?: return
    Desktop.getDesktop().browse(uri)
}

val storageDir: String
    get() = AppDirsFactory.getInstance()
        .getUserDataDir("ua.vald_zx.game.rat.race.card", "1.0", "VALD_ZX")

internal actual fun share(data: String?) {
}

internal actual fun ttsIsUkraineSupported(): Boolean  = false
internal actual fun tts(string: String) {
}

actual inline fun <reified T : @Serializable Any> getStore(name: String): KStore<T> {
    return storeOf(file = Path("$storageDir/$name"))
}