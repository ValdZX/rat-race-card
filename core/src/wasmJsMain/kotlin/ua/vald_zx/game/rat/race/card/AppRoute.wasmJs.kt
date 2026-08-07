package ua.vald_zx.game.rat.race.card

import kotlinx.browser.window

actual fun readAppRoute(): AppRoute = appRouteOf(window.location.hash)

actual fun writeAppRoute(route: AppRoute) {
    val target = "#${route.path()}"
    if (window.location.hash == target) return
    window.history.replaceState(null, "", target)
}
