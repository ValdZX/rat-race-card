package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel

@Composable
internal fun BoxScope.LegacyBoardRoutes(layout: BoardLayout, vm: BoardViewModel) {
    Places(layout = layout.outerRoute, vm = vm)
    Places(layout = layout.innerRoute, vm = vm)
    CardDecks(layout = layout.cardDecks, vm = vm)
    PlayerMessages(layout = layout.outerRoute, vm = vm)
    PlayerMessages(layout = layout.innerRoute, vm = vm)
}
