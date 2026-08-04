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

@Composable
fun BoxScope.DesignBoardRoutes(
    layout: BoardLayout,
    vm: BoardViewModel,
    focus: CellFocus,
    bubble: TokenBubbleState,
) {
    val routes = remember(layout) { listOf(layout.innerRoute, layout.outerRoute) }
    Box(Modifier.matchParentSize().cellFocusTracking(routes, focus)) {
        DesignBoardTracks(vm = vm, layout = layout, focus = focus, bubble = bubble)
        DesignCardDecks(layout = layout.cardDecks, vm = vm)
    }
}

@Composable
fun BoxScope.DesignBoardOverlay(
    layout: BoardLayout,
    vm: BoardViewModel,
    focus: CellFocus,
    bubble: TokenBubbleState,
) {
    Box(Modifier.matchParentSize()) {
        DesignPlayerMessages(vm = vm, layout = layout.outerRoute, focus = focus)
        DesignPlayerMessages(vm = vm, layout = layout.innerRoute, focus = focus)
        TokenBubbleScrim(bubble)
        DesignTokenBubbles(vm = vm, layout = layout.outerRoute, focus = focus, bubble = bubble)
        DesignTokenBubbles(vm = vm, layout = layout.innerRoute, focus = focus, bubble = bubble)
    }
}
