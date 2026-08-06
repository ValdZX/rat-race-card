package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.shared.legacyLayerOrNull

@Composable
internal fun BoxScope.LegacyBoardRoutes(layout: BoardLayout, vm: BoardViewModel) {
    layout.routes.filter { it.trackId.legacyLayerOrNull() != null }.forEach { route ->
        Places(layout = route, vm = vm)
    }
    CardDecks(layout = layout.cardDecks, vm = vm)
    layout.routes.filter { it.trackId.legacyLayerOrNull() != null }.forEach { route ->
        PlayerMessages(layout = route, vm = vm)
    }
}
