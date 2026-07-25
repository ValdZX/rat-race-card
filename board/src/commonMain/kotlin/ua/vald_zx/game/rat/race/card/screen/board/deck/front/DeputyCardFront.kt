package ua.vald_zx.game.rat.race.card.screen.board.deck.front

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.resources.Res
import ua.vald_zx.game.rat.race.card.resources.close
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.shared.CardLink


@Composable
fun BoxWithConstraintsScope.DeputyCardFront(
    card: CardLink,
    vm: BoardViewModel,
) {
    val state by vm.uiState.collectAsState()
    if (state.currentPlayerIsActive) {
        Button(stringResource(Res.string.close)) {
            vm.pass()
        }
    }
}