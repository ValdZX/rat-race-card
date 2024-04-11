package ua.vald_zx.game.rat.race.card

import io.github.xxfast.kstore.file.utils.CachesDirectory
import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

internal actual fun openUrl(url: String?) {
    val nsUrl = url?.let { NSURL.URLWithString(it) } ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}

@OptIn(ExperimentalKStoreApi::class)
internal actual val storageDir: String
    get() = NSFileManager.defaultManager.CachesDirectory?.relativePath.orEmpty()