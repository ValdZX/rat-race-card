package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.zIndex
import kotlin.math.abs
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.board.RouteLayout
import ua.vald_zx.game.rat.race.card.screen.board.SendMessageDialog
import ua.vald_zx.game.rat.race.card.screen.board.SendMoneyScreen
import ua.vald_zx.game.rat.race.card.screen.board.calculatePointerOffset
import ua.vald_zx.game.rat.race.card.screen.board.forEachPlayerPoint

@Composable
fun BoxScope.DesignPlayerTokens(vm: BoardViewModel, layout: RouteLayout, focus: CellFocus) {
    var messageDialog by remember { mutableStateOf(false) }
    if (messageDialog) {
        SendMessageDialog(
            onDismiss = { messageDialog = false },
            onSend = {
                vm.sendMessage(it)
                messageDialog = false
            },
        )
    }
    TokenLayer(vm, layout, focus) { messageDialog = true }
}

private const val COVERED_NEIGHBOURS = 1
private const val PLAYER_TOKEN_Z = 1f

@Composable
private fun BoxScope.TokenLayer(
    vm: BoardViewModel,
    layout: RouteLayout,
    focus: CellFocus,
    onOwnToken: () -> Unit,
) {
    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    val state by vm.uiState.collectAsState()
    val live = state.player.location.level == layout.layer.level
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .size(layout.size)
            .zIndex(PLAYER_TOKEN_Z)
    ) {
        forEachPlayerPoint(vm, layout) { pointerState, places, index, count ->
            val place = places.getValue(pointerState.position)
            val focused = focus.key
            val stepsAside = live && focused != null &&
                    focused.first == layout.layer.level &&
                    abs(focused.second - pointerState.position) <= COVERED_NEIGHBOURS
            val openBox = focus.expandedBox ?: expandedCellBox(layout)
            val target = if (stepsAside) {
                expandedTokenOffset(layout, place, index, count, openBox)
            } else {
                calculatePointerOffset(
                    layout.cellSize.width,
                    layout.cellSize.height,
                    place,
                    index,
                    count,
                )
            }
            if (stepsAside && focused.second == pointerState.position) {
                focus.reportTokenCount(focused, count)
            }
            val x by animateDpAsState(target.first, label = "TokenX")
            val y by animateDpAsState(target.second, label = "TokenY")
            DesignPlayerToken(
                player = pointerState.player,
                isCurrentPlayer = pointerState.isCurrentPlayer,
                isActivePlayer = pointerState.isActivePlayer,
                spotSize = layout.cellSize,
                modifier = Modifier
                    .offset(x, y)
                    .testTag("player-token-${pointerState.player.id}"),
                onClick = if (pointerState.isCurrentPlayer) {
                    onOwnToken
                } else {
                    {
                        bottomSheetNavigator.show(
                            SendMoneyScreen(
                                vm = vm,
                                playerId = pointerState.player.id,
                                playerName = pointerState.player.card.name,
                            )
                        )
                    }
                },
            )
        }
    }
}
