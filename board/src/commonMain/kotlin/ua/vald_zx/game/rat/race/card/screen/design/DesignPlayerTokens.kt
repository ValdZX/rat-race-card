package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
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
import ua.vald_zx.game.rat.race.card.components.clickableSingle
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.board.Place
import ua.vald_zx.game.rat.race.card.screen.board.RouteLayout
import ua.vald_zx.game.rat.race.card.screen.board.SendMessageDialog
import ua.vald_zx.game.rat.race.card.screen.board.SendMoneyScreen
import ua.vald_zx.game.rat.race.card.screen.board.SpeechBubble
import ua.vald_zx.game.rat.race.card.screen.board.calculatePointerOffset
import ua.vald_zx.game.rat.race.card.screen.board.forEachPlayerPoint

@Stable
class TokenBubbleState {
    var ownerId by mutableStateOf<String?>(null)
        private set

    val isOpen: Boolean
        get() = ownerId != null

    fun toggle(playerId: String) {
        ownerId = if (ownerId == playerId) null else playerId
    }

    fun close() {
        ownerId = null
    }
}

@Composable
fun rememberTokenBubbleState(): TokenBubbleState = remember { TokenBubbleState() }

private const val COVERED_NEIGHBOURS = 1
private const val PLAYER_TOKEN_Z = 1f

@Composable
fun BoxScope.DesignPlayerTokens(
    vm: BoardViewModel,
    layout: RouteLayout,
    focus: CellFocus,
    bubble: TokenBubbleState,
) {
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .size(layout.size)
            .zIndex(PLAYER_TOKEN_Z)
    ) {
        forEachPlayerPoint(vm, layout) { pointerState, places, index, count ->
            val target = tokenTarget(vm, layout, focus, places, pointerState.position, index, count)
            if (target.holdsFocus) {
                focus.key?.let { focus.reportTokenCount(it, count) }
            }
            val x by animateDpAsState(target.offset.first, label = "TokenX")
            val y by animateDpAsState(target.offset.second, label = "TokenY")
            val playerId = pointerState.player.id
            DesignPlayerToken(
                player = pointerState.player,
                isCurrentPlayer = pointerState.isCurrentPlayer,
                isActivePlayer = pointerState.isActivePlayer,
                spotSize = layout.cellSize,
                modifier = Modifier
                    .offset(x, y)
                    .testTag("player-token-$playerId"),
                onClick = { bubble.toggle(playerId) },
            )
        }
    }
}

@Composable
fun BoxScope.DesignTokenBubbles(
    vm: BoardViewModel,
    layout: RouteLayout,
    focus: CellFocus,
    bubble: TokenBubbleState,
) {
    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    val messageLog by vm.playerMessageLog.collectAsState()
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
    Box(modifier = Modifier.align(Alignment.Center).size(layout.size)) {
        forEachPlayerPoint(vm, layout) { pointerState, places, index, count ->
            val playerId = pointerState.player.id
            if (bubble.ownerId != playerId) return@forEachPlayerPoint
            val target = tokenTarget(vm, layout, focus, places, pointerState.position, index, count)
            TokenBubbleAnchor(layout, target.offset) {
                DesignTokenBubble(
                    player = pointerState.player,
                    isCurrentPlayer = pointerState.isCurrentPlayer,
                    isActivePlayer = pointerState.isActivePlayer,
                    messages = messageLog[playerId].orEmpty(),
                    onSendMessage = {
                        bubble.close()
                        messageDialog = true
                    },
                    onSendMoney = if (pointerState.isCurrentPlayer) {
                        null
                    } else {
                        {
                            bubble.close()
                            bottomSheetNavigator.show(
                                SendMoneyScreen(
                                    vm = vm,
                                    playerId = playerId,
                                    playerName = pointerState.player.card.name,
                                )
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun BoxScope.DesignPlayerMessages(vm: BoardViewModel, layout: RouteLayout, focus: CellFocus) {
    val messages by vm.playerMessages.collectAsState()
    if (messages.isEmpty()) return
    Box(modifier = Modifier.align(Alignment.Center).size(layout.size)) {
        forEachPlayerPoint(vm, layout) { pointerState, places, index, count ->
            val text = messages[pointerState.player.id]?.text ?: return@forEachPlayerPoint
            val target = tokenTarget(vm, layout, focus, places, pointerState.position, index, count)
            val x by animateDpAsState(target.offset.first, label = "MessageX")
            val y by animateDpAsState(target.offset.second, label = "MessageY")
            Box(modifier = Modifier.offset(x, y).size(layout.cellSize)) {
                SpeechBubble(
                    text = text,
                    modifier = Modifier.align(Alignment.TopCenter).testTag(speechBubbleTag(pointerState.player.id)),
                    cellSize = layout.cellSize,
                )
            }
        }
    }
}

fun speechBubbleTag(playerId: String) = "speech-bubble-$playerId"

@Composable
fun BoxScope.TokenBubbleScrim(bubble: TokenBubbleState) {
    if (!bubble.isOpen) return
    Box(Modifier.matchParentSize().clickableSingle { bubble.close() })
}

internal class TokenTarget(val offset: Pair<Dp, Dp>, val holdsFocus: Boolean)

@Composable
internal fun tokenTarget(
    vm: BoardViewModel,
    layout: RouteLayout,
    focus: CellFocus,
    places: Map<Int, Place>,
    position: Int,
    index: Int,
    count: Int,
): TokenTarget {
    val state by vm.uiState.collectAsState()
    val place = places.getValue(position)
    val focused = focus.key
    val live = state.player.location.trackId == layout.trackId
    val stepsAside = live && focused != null &&
            focused.first == layout.trackId &&
            abs(focused.second - position) <= COVERED_NEIGHBOURS
    val offset = if (stepsAside) {
        expandedTokenOffset(layout, place, index, count, focus.expandedBox ?: expandedCellBox(layout))
    } else {
        calculatePointerOffset(layout.cellSize.width, layout.cellSize.height, place, index, count)
    }
    return TokenTarget(offset, stepsAside && focused?.second == position)
}

@Composable
private fun BoxScope.TokenBubbleAnchor(
    layout: RouteLayout,
    tokenOffset: Pair<Dp, Dp>,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    var bubbleHeight by remember { mutableStateOf(0.dp) }
    val above = tokenOffset.second > layout.size.height / 2
    val left = (tokenOffset.first + layout.cellSize.width / 2 - tokenBubbleWidth / 2)
        .coerceIn(0.dp, (layout.size.width - tokenBubbleWidth).coerceAtLeast(0.dp))
    val top = if (above) {
        tokenOffset.second - bubbleHeight
    } else {
        tokenOffset.second + layout.cellSize.height
    }
    Box(
        modifier = Modifier
            .offset(x = left, y = top)
            .onSizeChanged { bubbleHeight = with(density) { it.height.toDp() } },
    ) {
        content()
    }
}
