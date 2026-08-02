package ua.vald_zx.game.rat.race.card.screen.board.deck.front

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.resources.Res
import ua.vald_zx.game.rat.race.card.resources.close
import ua.vald_zx.game.rat.race.card.components.EButton
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.shared.CardLink

@Composable
fun BoxWithConstraintsScope.DeputyCardFront(
    card: CardLink,
    vm: BoardViewModel,
) {
    val state by vm.uiState.collectAsState()
    if (state.currentPlayerIsActive) {
        val density = LocalDensity.current
        val cardWidth = max(maxWidth, 100.dp)
        val unitTS = with(density) { (cardWidth.toPx() / 300).toSp() }
        val unitDp = cardWidth / 300
        EButton(
            modifier = Modifier.align(Alignment.Center).padding(unitDp * 12),
            onClick = { vm.pass() },
            title = stringResource(Res.string.close),
            unitTS = unitTS,
            unitDp = unitDp,
        )
    }
}
