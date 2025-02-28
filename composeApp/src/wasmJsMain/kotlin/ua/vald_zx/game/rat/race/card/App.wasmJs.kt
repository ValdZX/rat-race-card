package ua.vald_zx.game.rat.race.card

import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.storage.storeOf
import kotlinx.browser.window
import kotlinx.serialization.Serializable

internal actual val platformContext: Any
    get() = Unit

internal actual fun openUrl(url: String?) {
    url?.let { window.open(it) }
}

actual inline fun <reified T : @Serializable Any> getStore(name: String): KStore<T> {
    return storeOf(name)
}

actual fun share(data: String?) {
    //nop
}

actual fun ttsIsUkraineSupported(): Boolean = false

actual fun tts(string: String) {
    //nop
}