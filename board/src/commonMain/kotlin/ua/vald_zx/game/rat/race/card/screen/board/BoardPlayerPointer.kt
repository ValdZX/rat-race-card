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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import dev.lennartegb.shadows.boxShadow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.CashFlowField
import ua.vald_zx.game.rat.race.card.components.GoldRainbow
import ua.vald_zx.game.rat.race.card.components.optionalModifier
import ua.vald_zx.game.rat.race.card.design.DesignDialog
import ua.vald_zx.game.rat.race.card.design.DesignTextField
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.max
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Bow
import ua.vald_zx.game.rat.race.card.resource.images.RatPlayer1
import ua.vald_zx.game.rat.race.card.resource.images.Send
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.shared.Gender
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.TrackId
import ua.vald_zx.game.rat.race.card.shared.cashFlow
import ua.vald_zx.game.rat.race.card.shared.total
import ua.vald_zx.game.rat.race.card.splitDecimal
import ua.vald_zx.game.rat.race.card.formatAmount

data class PlayerPointState(
    val position: Int,
    val color: Long,
    val trackId: TrackId,
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

    var isMessageDialogVisible by remember(pointerState.player.id) { mutableStateOf(false) }

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
                    if (!pointerState.isCurrentPlayer) {
                        PlayerTooltip(pointerState, tooltipState, coroutineScope) {
                            bottomSheetNavigator.show(
                                SendMoneyScreen(
                                    vm = vm,
                                    playerId = pointerState.player.id,
                                    playerName = pointerState.player.card.name,
                                )
                            )
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
                            if (pointerState.isCurrentPlayer) {
                                isMessageDialogVisible = true
                            } else {
                                coroutineScope.launch { tooltipState.show() }
                            }
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
                            if (pointerState.isCurrentPlayer) {
                                isMessageDialogVisible = true
                            } else {
                                coroutineScope.launch { tooltipState.show() }
                            }
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
    if (isMessageDialogVisible) {
        SendMessageDialog(
            onDismiss = { isMessageDialogVisible = false },
            onSend = {
                vm.sendMessage(it)
                isMessageDialogVisible = false
            }
        )
    }
}

internal val bubbleCorner = 6
internal val bubbleTailWidth = 14
internal val bubbleTailHeight = 10
internal val bubbleGap = 2
internal val bubbleMinWidth = 64
internal val bubbleMaxWidth = 168
internal val bubbleOutline = 1

internal class SpeechBubbleShape(
    private val corner: Dp,
    private val tailWidth: Dp,
    private val tailHeight: Dp,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val r = with(density) { corner.toPx() }
        val tailHalf = with(density) { tailWidth.toPx() } / 2f
        val bodyBottom = size.height - with(density) { tailHeight.toPx() }
        val centerX = size.width / 2f
        val path = Path().apply {
            moveTo(r, 0f)
            lineTo(size.width - r, 0f)
            arcTo(Rect(size.width - 2 * r, 0f, size.width, 2 * r), -90f, 90f, false)
            lineTo(size.width, bodyBottom - r)
            arcTo(Rect(size.width - 2 * r, bodyBottom - 2 * r, size.width, bodyBottom), 0f, 90f, false)
            lineTo(centerX + tailHalf, bodyBottom)
            lineTo(centerX, size.height)
            lineTo(centerX - tailHalf, bodyBottom)
            lineTo(r, bodyBottom)
            arcTo(Rect(0f, bodyBottom - 2 * r, 2 * r, bodyBottom), 90f, 90f, false)
            lineTo(0f, r)
            arcTo(Rect(0f, 0f, 2 * r, 2 * r), 180f, 90f, false)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
internal fun SpeechBubble(
    text: String,
    modifier: Modifier = Modifier,
    cellSize: DpSize,
) {
    val coef = cellSize.max / 20
    val shape = remember { SpeechBubbleShape(coef * bubbleCorner, coef * bubbleTailWidth, coef * bubbleTailHeight) }
    Box(
        modifier = modifier
            .layout { measurable, _ ->
                val placeable = measurable.measure(
                    Constraints(
                        minWidth = (coef * bubbleMinWidth).roundToPx(),
                        maxWidth = (coef * bubbleMaxWidth).roundToPx()
                    )
                )
                layout(0, 0) {
                    placeable.place(
                        x = -placeable.width / 2,
                        y = -placeable.height - (coef * bubbleGap).roundToPx()
                    )
                }
            }
            .background(Color.White, shape)
            .border(coef * bubbleOutline, Color.Black, shape),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                start = coef * 10,
                end = coef * 10,
                top = coef * 6,
                bottom = coef * 6 + coef * bubbleTailHeight
            ),
            color = Color.Black,
            fontSize = coef.value * 13.sp,
            fontFamily = FontFamily(Font(Res.font.Bubbleboddy, weight = FontWeight.Medium)),
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun SendMessageDialog(
    onDismiss: () -> Unit,
    onSend: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    DesignDialog(
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.send_message),
        confirmLabel = stringResource(Res.string.send),
        confirmEnabled = text.isNotBlank(),
        onConfirm = { onSend(text.trim()) },
        dismissLabel = stringResource(Res.string.cancel),
        onDismissAction = onDismiss,
    ) {
        DesignTextField(
            value = text,
            onValueChange = { text = it.take(160) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 3,
        )
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
                    name = stringResource(Res.string.total_assets),
                    rainbow = GoldRainbow,
                    value = state.player.total().formatAmount(),
                    fontSize = 12.sp
                )
                CashFlowField(
                    name = stringResource(Res.string.cash_flow),
                    value = state.player.cashFlow().formatAmount(),
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
