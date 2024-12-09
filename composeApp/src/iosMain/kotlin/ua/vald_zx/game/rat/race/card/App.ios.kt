package ua.vald_zx.game.rat.race.card

import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.storeOf
import io.github.xxfast.kstore.file.utils.CachesDirectory
import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

internal actual fun openUrl(url: String?) {
    val nsUrl = url?.let { NSURL.URLWithString(it) } ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}

@OptIn(ExperimentalKStoreApi::class)
val storageDir: String
    get() = NSFileManager.defaultManager.CachesDirectory?.relativePath.orEmpty()

internal actual inline fun <reified T : @Serializable Any> getStore(name: String): KStore<T> {
    return storeOf(file = Path("$storageDir/$name"))
}

internal actual fun share(data: String?) {
    val activityItems = listOf(data)
    val activityViewController = UIActivityViewController(activityItems, null)

    val application = UIApplication.sharedApplication
    application.keyWindow?.rootViewController?.presentViewController(
        activityViewController,
        animated = true,
        completion = null
    )
}

internal actual fun playCoin() {
}

internal actual fun ttsIsUkraineSupported(): Boolean = false

internal actual fun tts(string: String) {
}