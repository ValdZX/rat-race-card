@file:OptIn(ExperimentalMaterial3Api::class)

package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.*
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
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Bow
import ua.vald_zx.game.rat.race.card.resource.images.RatPlayer1
import ua.vald_zx.game.rat.race.card.resource.images.Send
import ua.vald_zx.game.rat.race.card.shared.Gender
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.cashFlow
import ua.vald_zx.game.rat.race.card.shared.total
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
    places: Map<Int, Place>,
    pointerState: PlayerPointState,
    vm: BoardViewModel,
    spotSize: DpSize,
    index: Int,
    count: Int,
) {

    var offset by remember {
        val place = places[pointerState.position]!!
        mutableStateOf(calculatePointerOffset(spotSize.width, spotSize.height, place, index, count))
    }
    LaunchedEffect(pointerState.position, count, spotSize.width, spotSize.height) {
        val place = places[pointerState.position]!!
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
    val secondaryColor = remember(pointerState.player.isInactive) {
        if (pointerState.player.isInactive) {
            Color.Black
        } else {
            Color.White
        }
    }
    Box(
        modifier = Modifier
            .offset(animatedX, animatedY)
            .size(spotSize.width, spotSize.height)
            .graphicsLayer(clip = false)
    ) {
        Box(
            modifier = Modifier
                .optionalModifier(pointerState.isActivePlayer) {
                    boxShadow(
                        blurRadius = blurRadius,
                        spreadRadius = spreadRadius,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.onBackground,
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
                                playerColor,
                                secondaryColor,
                                playerColor,
                                playerColor,
                                secondaryColor,
                                playerColor,
                                playerColor,
                                secondaryColor,
                                playerColor,
                                playerColor,
                                secondaryColor,
                                playerColor,
                                playerColor,
                                secondaryColor,
                                playerColor,
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
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above, 4.dp),
                tooltip = {
                    if (pointerState.isCurrentPlayer) {
//                        CurrentPlayerTooltip(
//                            pointerState = pointerState,
//                            vm = vm,
//                            tooltipState = tooltipState,
//                            coroutineScope = coroutineScope
//                        )
                    } else {
                        PlayerTooltip(pointerState, tooltipState, coroutineScope) {
                            bottomSheetNavigator.show(
                                SendScreen(
                                    pointerState.player.id,
                                    pointerState.player.card.name
                                ) { id, price ->
                                    vm.sendMoney(id, price)
                                })
                        }
                    }
                },
                state = tooltipState,
                enableUserInput = false
            ) {
                if (pointerState.player.isInactive) {
                    Image(
                        imageVector = Images.RatPlayer1,
                        contentDescription = null,
                        modifier = Modifier.clickable {
                            coroutineScope.launch { tooltipState.show() }
                        },
                        colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply {
                            setToSaturation(0f)
                        })
                    )
                } else {
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
        if (pointerState.player.card.gender == Gender.FEMALE) {
            Image(
                imageVector = Images.Bow,
                contentDescription = null,
                modifier = Modifier
                    .size(spotSize / 2)
                    .offset(spotSize.width / 3.5f, -spotSize.height / 3.5f)
                    .rotate(45f)
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
fun CurrentPlayerTooltip(
    pointerState: PlayerPointState,
    vm: BoardViewModel,
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
        val colorState = remember { mutableStateOf(pointerState.color) }
        ColorsSelector(colorState)
        LaunchedEffect(colorState.value) {
            vm.changePlayerColor(colorState.value)
        }
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
                if (state.player.isInactive) {
                    Text(state.player.card.name + " - Offline")
                } else {
                    Text(state.player.card.name)
                }
                CashFlowField(
                    name = "Статки",
                    rainbow = GoldRainbow,
                    value = state.player.total().splitDecimal(),
                    fontSize = 12.sp
                )
                CashFlowField(
                    name = "Cash Flow",
                    value = state.player.cashFlow().splitDecimal(),
                    fontSize = 12.sp
                )
            }
            IconButton(
                onClick = { send() },
                content = {
                    Icon(Images.Send, contentDescription = null)
                },
                modifier = Modifier.align(Alignment.Bottom)
            )
        }
    }
}