package ua.vald_zx.game.rat.race.card

import androidx.compose.runtime.Composable
import io.github.xxfast.kstore.file.utils.CachesDirectory
import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

internal actual fun openUrl(url: String?) {
    val nsUrl = url?.let { NSURL.URLWithString(it) } ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}

@OptIn(ExperimentalKStoreApi::class)
internal actual val storageDir: String
    get() = NSFileManager.defaultManager.CachesDirectory?.relativePath.orEmpty()

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

@Composable
internal actual fun GameLayer() {

}