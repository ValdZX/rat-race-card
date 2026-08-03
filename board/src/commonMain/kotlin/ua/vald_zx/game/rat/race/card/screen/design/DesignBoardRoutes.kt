package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.board.BoardLayout
import ua.vald_zx.game.rat.race.card.screen.board.PlayerMessages

@Composable
fun BoxScope.DesignBoardRoutes(layout: BoardLayout, vm: BoardViewModel) {
    val focus = rememberCellFocus()
    val routes = remember(layout) { listOf(layout.innerRoute, layout.outerRoute) }
    Box(Modifier.matchParentSize().cellFocusTracking(routes, focus)) {
        DesignBoardTracks(vm = vm, layout = layout, focus = focus)
        DesignCardDecks(layout = layout.cardDecks, vm = vm)
        Box(Modifier.fillMaxSize().zIndex(FOCUSED_CELL_Z + 1f)) {
            PlayerMessages(layout = layout.outerRoute, vm = vm)
            PlayerMessages(layout = layout.innerRoute, vm = vm)
        }
    }
}
