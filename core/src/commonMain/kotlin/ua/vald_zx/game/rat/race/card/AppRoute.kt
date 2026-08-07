package ua.vald_zx.game.rat.race.card

sealed interface AppRoute {
    data object Start : AppRoute
    data object Card : AppRoute
    data object Online : AppRoute
    data class Board(val boardId: String) : AppRoute
}

private const val CARD_SEGMENT = "card"
private const val ONLINE_SEGMENT = "online"
private const val BOARD_SEGMENT = "board"

fun AppRoute.path(): String = when (this) {
    AppRoute.Start -> "/"
    AppRoute.Card -> "/$CARD_SEGMENT"
    AppRoute.Online -> "/$ONLINE_SEGMENT"
    is AppRoute.Board -> "/$BOARD_SEGMENT/$boardId"
}

fun appRouteOf(raw: String?): AppRoute {
    val segments = raw.orEmpty()
        .removePrefix("#")
        .split('/')
        .map(String::trim)
        .filter(String::isNotEmpty)
    return when (segments.firstOrNull()) {
        CARD_SEGMENT -> AppRoute.Card
        ONLINE_SEGMENT -> AppRoute.Online
        BOARD_SEGMENT -> segments.getOrNull(1)?.let(AppRoute::Board) ?: AppRoute.Online
        else -> AppRoute.Start
    }
}

expect fun readAppRoute(): AppRoute

expect fun writeAppRoute(route: AppRoute)

interface RoutedScreen {
    val appRoute: AppRoute
}
