package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
    // Один шар. Розкладати фішки по двох шарах не можна: при зміні фокуса
    // фішка переїжджає між ними, перестворюється, губить наведення — і
    // починається те саме блимання.
    TokenLayer(vm, layout, focus) { messageDialog = true }
}

/** Скільки сусідніх клітинок накриває розкрита пігулка — вони теж відпливають. */
private const val COVERED_NEIGHBOURS = 1

@Composable
private fun BoxScope.TokenLayer(
    vm: BoardViewModel,
    layout: RouteLayout,
    focus: CellFocus,
    onOwnToken: () -> Unit,
) {
    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    val expandedBox = expandedCellBox(layout)
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .size(layout.size)
            .zIndex(FOCUSED_CELL_Z + 1f)
    ) {
        forEachPlayerPoint(vm, layout) { pointerState, places, index, count ->
            val cellKey = layout.layer.level to pointerState.position
            val place = places.getValue(pointerState.position)
            val target = calculatePointerOffset(
                layout.cellSize.width,
                layout.cellSize.height,
                place,
                index,
                count,
            )
            // Наведення на фішку тримає ту саму клітинку розкритою. Без цього
            // фішка перехоплювала вказівник, клітинка згорталась, фішка їхала
            // назад — і натиснути її було нічим.
            val interaction = remember { MutableInteractionSource() }
            val tokenHovered by interaction.collectIsHoveredAsState()
            LaunchedEffect(tokenHovered) { focus.set(cellKey, tokenHovered, FocusSource.Token) }
            // Чи відпливати — вирішується один раз на розкриття й далі не
            // змінюється. Інакше фішка, з якої почалось наведення, тікала
            // з-під курсора, фокус гаснув, вона поверталась — і так без кінця.
            // Сусідні фішки відпливають теж: пігулка ширша за клітинку й
            // інакше вони затуляли б підпис.
            val floats = remember(focus.key) {
                val focused = focus.key
                when {
                    focused == null || focused.first != layout.layer.level -> false
                    focused == cellKey -> focus.source != FocusSource.Token
                    else -> kotlin.math.abs(focused.second - pointerState.position) <= COVERED_NEIGHBOURS
                }
            }
            val shift = expandedBox.height / 2 + layout.cellSize.height / 2 + 4.dp
            val floatBy = when {
                !floats -> 0.dp
                // Угору, якщо є куди: біля верхнього краю треку відпливаємо вниз.
                place.offset.y > shift -> -shift
                else -> shift
            }
            val x by animateDpAsState(target.first, label = "TokenX")
            val y by animateDpAsState(target.second + floatBy, label = "TokenY")
            DesignPlayerToken(
                player = pointerState.player,
                isCurrentPlayer = pointerState.isCurrentPlayer,
                isActivePlayer = pointerState.isActivePlayer,
                spotSize = layout.cellSize,
                modifier = Modifier.offset(x, y).hoverable(interaction),
                // Як і на старій дошці: своя фішка — повідомлення, чужа — переказ.
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
