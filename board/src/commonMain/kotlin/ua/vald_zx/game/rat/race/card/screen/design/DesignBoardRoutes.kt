package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.board.BoardLayout
import ua.vald_zx.game.rat.race.card.screen.board.PlayerMessages

/** Нова мова дошки: треки, колоди, фішки. Стара — LegacyBoardRoutes. */
@Composable
fun BoxScope.DesignBoardRoutes(layout: BoardLayout, vm: BoardViewModel) {
    DesignBoardTracks(vm = vm, layout = layout)
    DesignCardDecks(layout = layout.cardDecks, vm = vm)
    DesignPlayerTokens(vm = vm, layout = layout.outerRoute)
    DesignPlayerTokens(vm = vm, layout = layout.innerRoute)
    PlayerMessages(layout = layout.outerRoute, vm = vm)
    PlayerMessages(layout = layout.innerRoute, vm = vm)
}
