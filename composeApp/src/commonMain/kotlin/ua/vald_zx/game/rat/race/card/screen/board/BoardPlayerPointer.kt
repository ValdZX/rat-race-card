@file:OptIn(ExperimentalMaterial3Api::class)

package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import dev.lennartegb.shadows.boxShadow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ua.vald_zx.game.rat.race.card.components.CashFlowField
import ua.vald_zx.game.rat.race.card.components.GoldRainbow
import ua.vald_zx.game.rat.race.card.components.optionalModifier
import ua.vald_zx.game.rat.race.card.logic.BoardAction
import ua.vald_zx.game.rat.race.card.logic.BoardState
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.RatPlayer1
import ua.vald_zx.game.rat.race.card.resource.images.Send
import ua.vald_zx.game.rat.race.card.screen.second.SendScreen
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.splitDecimal


data class PlayerPointState(
    val position: Int,
    val color: Long,
    val level: Int,
    val name: String,
    val isCurrentPlayer: Boolean,
    val isActivePlayer: Boolean,
    val player: Player,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerPoint(
    places: List<Place>,
    pointerState: PlayerPointState,
    state: BoardState,
    dispatch: (BoardAction) -> Unit,
    spotSize: DpSize,
    index: Int,
    count: Int,
) {
    var offset by remember {
        val place = places[pointerState.position]
        mutableStateOf(calculatePointerOffset(spotSize.width, spotSize.height, place, index, count))
    }
    LaunchedEffect(pointerState.position, count, spotSize.width, spotSize.height) {
        val place = places[pointerState.position]
        offset = calculatePointerOffset(spotSize.width, spotSize.height, place, index, count)
    }
    val animatedX by animateDpAsState(offset.first)
    val animatedY by animateDpAsState(offset.second)
    val blurRadius = spotSize.width * 0.2f

    val infiniteTransition = rememberInfiniteTransition(label = "InfiniteTransition")

    val spreadRadius by infiniteTransition.animateValue(
        initialValue = 0.dp,
        targetValue = spotSize.width * 0.2f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "FloatAnimation",
        typeConverter = TwoWayConverter({ AnimationVector(it.value) }, { it.value.dp })
    )
    val playerColor = remember(pointerState.color) { Color(pointerState.color) }
    Box(
        modifier = Modifier
            .offset(animatedX, animatedY)
            .size(spotSize.width, spotSize.height)
            .optionalModifier(pointerState.isActivePlayer) {
                boxShadow(
                    blurRadius = blurRadius,
                    spreadRadius = spreadRadius,
                    shape = CircleShape,
                    color = Color.White,
                )
            }
            .clip(CircleShape)
            .background(Color.White)
            .optionalModifier(pointerState.isCurrentPlayer) {
                border(
                    width = spotSize.width * 0.1f,
                    shape = CircleShape,
                    color = playerColor
                )
            }
            .optionalModifier(!pointerState.isCurrentPlayer) {
                border(
                    width = spotSize.width * 0.1f,
                    shape = CircleShape,
                    brush = Brush.sweepGradient(
                        listOf(
                            playerColor,
                            Color.White,
                            playerColor,
                            Color.White,
                            playerColor,
                            Color.White,
                            playerColor,
                            Color.White,
                            playerColor,
                            Color.White,
                            playerColor,
                        )
                    )
                )
            }
    ) {
        val coroutineScope = rememberCoroutineScope()
        val tooltipState = rememberTooltipState(isPersistent = true)
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        TooltipBox(
            modifier = Modifier,
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
            onDismissRequest = {},
            tooltip = {
                if (pointerState.isCurrentPlayer) {
                    CurrentPlayerTooltip(
                        pointerState,
                        state,
                        dispatch,
                        tooltipState,
                        coroutineScope
                    )
                } else {
                    PlayerTooltip(pointerState, tooltipState, coroutineScope) {
                        bottomSheetNavigator.show(SendScreen(pointerState.player))
                    }
                }
            },
            state = tooltipState,
            enableUserInput = false
        ) {
            Image(
                imageVector = Images.RatPlayer1,
                contentDescription = null,
                modifier = Modifier.clickable {
                    coroutineScope.launch { tooltipState.show() }
                }
            )
        }
    }
}

@Composable
fun CurrentPlayerTooltip(
    pointerState: PlayerPointState,
    state: BoardState,
    dispatch: (BoardAction) -> Unit,
    tooltipState: TooltipState,
    coroutineScope: CoroutineScope,
) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background).padding(8.dp)
    ) {
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = { coroutineScope.launch { tooltipState.dismiss() } }) {
            Icon(Icons.Default.Close, contentDescription = null)
        }
        ColorsSelector(state, dispatch)
    }
}

@Composable
fun PlayerTooltip(
    state: PlayerPointState,
    tooltipState: TooltipState,
    coroutineScope: CoroutineScope,
    send: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(IntrinsicSize.Min)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
    ) {
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = { coroutineScope.launch { tooltipState.dismiss() } }) {
            Icon(Icons.Default.Close, contentDescription = null)
        }
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(state.player.playerCard.profession)
                CashFlowField(
                    name = "Статки",
                    rainbow = GoldRainbow,
                    value = state.player.state.totalExpenses.splitDecimal(),
                    fontSize = 12.sp
                )
                CashFlowField(
                    name = "Cash Flow",
                    value = state.player.state.cashFlow.splitDecimal(),
                    fontSize = 12.sp
                )
            }
            androidx.compose.material3.IconButton(
                onClick = { send() },
                content = {
                    Icon(Images.Send, contentDescription = null)
                },
                modifier = Modifier.align(Alignment.Bottom)
            )
        }
    }
}