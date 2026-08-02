package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.board.SendMoneyScreen
import ua.vald_zx.game.rat.race.card.screen.board.RouteLayout
import ua.vald_zx.game.rat.race.card.screen.board.calculatePointerOffset
import ua.vald_zx.game.rat.race.card.screen.board.forEachPlayerPoint

@Composable
fun BoxScope.DesignPlayerTokens(vm: BoardViewModel, layout: RouteLayout) {
    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    Box(modifier = Modifier.align(Alignment.Center).size(layout.size)) {
        forEachPlayerPoint(vm, layout) { pointerState, places, index, count ->
            val place = places.getValue(pointerState.position)
            val target = calculatePointerOffset(
                layout.cellSize.width,
                layout.cellSize.height,
                place,
                index,
                count,
            )
            // Фішка їде по дошці, а не телепортується: без анімації гравець
            // не бачить, скільки клітинок він пройшов.
            val x by animateDpAsState(target.first, label = "TokenX")
            val y by animateDpAsState(target.second, label = "TokenY")
            DesignPlayerToken(
                player = pointerState.player,
                isCurrentPlayer = pointerState.isCurrentPlayer,
                isActivePlayer = pointerState.isActivePlayer,
                spotSize = layout.cellSize,
                modifier = Modifier.offset(x, y),
                // Переказ грошей живе на чужій фішці — як і на старій дошці,
                // тільки без проміжного тултипа.
                onClick = if (pointerState.isCurrentPlayer) null else {
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
