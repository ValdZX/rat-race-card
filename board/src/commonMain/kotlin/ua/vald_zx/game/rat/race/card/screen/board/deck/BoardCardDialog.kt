package ua.vald_zx.game.rat.race.card.screen.board.deck

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.constraintlayout.compose.*
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.clickableSingle
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.design.DesignIconButton
import ua.vald_zx.game.rat.race.card.design.DesignShapes
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.isVertical
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.max
import ua.vald_zx.game.rat.race.card.resources.Res
import ua.vald_zx.game.rat.race.card.resources.collapse
import ua.vald_zx.game.rat.race.card.resources.open_card
import ua.vald_zx.game.rat.race.card.screen.board.INNER_LAYER_SCALE
import ua.vald_zx.game.rat.race.card.screen.board.deck.front.BoardCardFront
import ua.vald_zx.game.rat.race.card.screen.board.deckCoordinatesMap
import ua.vald_zx.game.rat.race.card.screen.board.discardPilesCoordinatesMap
import ua.vald_zx.game.rat.race.card.screen.design.DesignCardDialog
import ua.vald_zx.game.rat.race.card.screen.design.icon
import ua.vald_zx.game.rat.race.card.screen.design.shortLabel
import ua.vald_zx.game.rat.race.card.screen.design.tone
import ua.vald_zx.game.rat.race.card.shared.CardLink
import ua.vald_zx.game.rat.race.card.shared.CoreTrackIds

const val cardMoveAnimationDuration = 2000

@Composable
fun BoxWithConstraintsScope.CardDialog(vm: BoardViewModel) {
    val state by vm.uiState.collectAsState()
    val interaction = state.board.pendingInteractions.firstOrNull { it.playerId == state.player.id }
    if (interaction != null && !cardInteractionRendererRegistry.hasSpecializedRenderer(interaction)) return
    val takenCard = state.board.takenCard
    var collapsed by remember { mutableStateOf(false) }
    LaunchedEffect(takenCard) {
        collapsed = false
    }
    if (takenCard != null && collapsed) {
        CollapsedCardChip(card = takenCard, onExpand = { collapsed = false })
        return
    }
    val onCollapse = { collapsed = true }
    if (designV2Enabled.value) {
        DesignCardDialog(vm, onCollapse)
    } else {
        LegacyCardDialog(vm, onCollapse)
    }
}

@Composable
private fun BoxScope.CollapsedCardChip(
    card: CardLink,
    onExpand: () -> Unit,
) {
    val colors = Design.colors
    val tone = card.type.tone()
    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 12.dp)
            .clip(DesignShapes.full)
            .background(colors.scaffold.surface1)
            .border(2.dp, tone.edge, DesignShapes.full)
            .clickableSingle(
                onClickLabel = stringResource(Res.string.open_card),
                role = Role.Button,
                onClick = onExpand,
            )
            .testTag("collapsed-card-chip")
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painter = card.type.icon(),
            contentDescription = null,
            tint = tone.edge,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = card.type.shortLabel(),
            style = Design.type.label,
            color = colors.scaffold.onSurface,
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = stringResource(Res.string.open_card),
            tint = colors.scaffold.onSurfaceMuted,
            modifier = Modifier.size(18.dp),
        )
    }
}

@OptIn(ExperimentalMotionApi::class)
@Composable
private fun BoxWithConstraintsScope.LegacyCardDialog(
    vm: BoardViewModel,
    onCollapse: () -> Unit,
) {
    val state by vm.uiState.collectAsState()
    val takenCard = state.board.takenCard
    var showDialog by remember { mutableStateOf(false) }
    LaunchedEffect(takenCard) {
        if (takenCard != null) {
            showDialog = true
        }
    }
    if (showDialog) {
        val card = remember { takenCard } ?: return
        var targetProgress by remember { mutableStateOf(0f) }

        val progress by animateFloatAsState(
            targetValue = targetProgress,
            animationSpec = tween(durationMillis = cardMoveAnimationDuration)
        )
        val deckCoordinatesState = remember(card) {
            deckCoordinatesMap[card.type]
        }
        if (deckCoordinatesState != null) {
            val dialogWidth = min(max(min(maxWidth, maxHeight) * 2.7f / 3f, 300.dp), 600.dp)
            val dialogSize = DpSize(dialogWidth, dialogWidth * 2f / 3f)
            val deckCoordinates by deckCoordinatesState
            val scale = if (state.trackId == CoreTrackIds.Inner) INNER_LAYER_SCALE else 1.0f
            val onBoardSize = deckCoordinates.second
            val onBoardSizeScaled = onBoardSize * scale
            val onBoardScaleToLow = onBoardSizeScaled.max / dialogSize.max
            val motionScene = remember(onBoardSize, state.trackId) {
                val deckOffset = deckCoordinates.first
                val discardOffset = discardPilesCoordinatesMap[card.type]?.value?.first!!
                MotionScene {
                    val back = createRefFor("back")
                    val front = createRefFor("front")
                    val showStart = constraintSet {
                        constrain(back) {
                            visibility = Visibility.Visible
                            start.linkTo(parent.start, deckOffset.x)
                            top.linkTo(parent.top, deckOffset.y)
                            width = Dimension.value(onBoardSizeScaled.width)
                            height = Dimension.value(onBoardSizeScaled.height)
                            scaleX = 1f
                            scaleY = 1f
                            pivotX = 0f
                            pivotY = 0f
                        }

                        constrain(front) {
                            visibility = Visibility.Gone
                            centerTo(parent)
                            rotationY = -90f
                        }
                    }
                    val toCenter = constraintSet {
                        constrain(back) {
                            visibility = Visibility.Visible
                            centerTo(parent)
                            width = Dimension.value(onBoardSizeScaled.width)
                            height = Dimension.value(onBoardSizeScaled.height)
                            scaleX = 1f
                            scaleY = 1f
                            pivotX = 0.5f
                            pivotY = 0.5f
                            if (onBoardSize.width < onBoardSize.height) {
                                rotationZ = -90f
                            }
                        }
                        constrain(front) {
                            visibility = Visibility.Gone
                            centerTo(parent)
                            rotationY = -90f
                        }
                    }
                    val flip = constraintSet {
                        constrain(back) {
                            visibility = Visibility.Visible
                            centerTo(parent)
                            width = Dimension.value(onBoardSizeScaled.width)
                            height = Dimension.value(onBoardSizeScaled.height)
                            pivotX = 0.5f
                            pivotY = 0.5f
                            rotationY = 90f
                            if (onBoardSize.width < onBoardSize.height) {
                                rotationZ = -90f
                            }
                        }
                        constrain(front) {
                            visibility = Visibility.Visible
                            centerTo(parent)
                            width = Dimension.value(dialogSize.width)
                            height = Dimension.wrapContent
                            scaleX = onBoardScaleToLow
                            scaleY = onBoardScaleToLow
                            pivotX = 0.5f
                            pivotY = 0.5f
                            rotationY = -90f
                        }
                    }
                    val dialogShowed = constraintSet {
                        constrain(back) {
                            visibility = Visibility.Gone
                            centerTo(parent)
                            rotationY = 90f
                            if (onBoardSize.width < onBoardSize.height) {
                                rotationZ = -90f
                            }
                        }
                        constrain(front) {
                            visibility = Visibility.Visible
                            centerTo(parent)
                            width = Dimension.value(dialogSize.width)
                            height = Dimension.wrapContent
                            rotationY = 0f
                            scaleX = 1f
                            scaleY = 1f
                        }
                    }
                    val hideEnd = constraintSet {
                        constrain(back) {
                            visibility = Visibility.Visible
                            start.linkTo(parent.start, discardOffset.x)
                            top.linkTo(parent.top, discardOffset.y)
                            width = Dimension.value(onBoardSizeScaled.width)
                            height = Dimension.value(onBoardSizeScaled.height)
                            scaleX = 1f
                            scaleY = 1f
                            pivotX = 0f
                            pivotY = 0f
                        }

                        constrain(front) {
                            visibility = Visibility.Gone
                            centerTo(parent)
                            rotationY = -90f
                        }
                    }
                    transition(showStart, toCenter, "show1") {

                    }
                    transition(toCenter, flip, "show2") {

                    }
                    transition(flip, dialogShowed, "show3") {

                    }
                    transition(dialogShowed, flip, "hide1") {

                    }
                    transition(flip, toCenter, "hide2") {

                    }
                    transition(toCenter, hideEnd, "hide3") {

                    }
                }
            }
            MotionLayout(
                modifier = Modifier.fillMaxSize(),
                motionScene = motionScene,
                transitionName = when {
                    progress < 1f -> "show1"
                    progress < 2f -> "show2"
                    progress < 3f -> "show3"
                    progress < 4f -> "hide1"
                    progress < 5f -> "hide2"
                    progress < 6f -> "hide3"
                    else -> "hide3"
                },
                progress = when {
                    progress < 1f -> progress
                    progress < 2f -> progress - 1f
                    progress < 3f -> progress - 2f
                    progress < 4f -> progress - 3f
                    progress < 5f -> progress - 4f
                    progress < 6f -> progress - 5f
                    else -> 6f
                }
            ) {
                BoxWithConstraints(modifier = Modifier.layoutId("back")) {
                    BoardCardBack(
                        card.type, DpSize(maxWidth, maxHeight), onBoardSize.isVertical
                    )
                }
                BoxWithConstraints(modifier = Modifier.layoutId("front")) {
                    BoardCardFront(
                        card = card,
                        size = dialogSize,
                        vm = vm
                    )
                }
            }
            LaunchedEffect(takenCard) {
                if (takenCard == null) {
                    targetProgress = 6f
                }
            }
            LaunchedEffect(Unit) {
                targetProgress = 3f
            }
            if (progress == 6f) {
                LaunchedEffect(Unit) {
                    showDialog = false
                }
            }
            if (takenCard != null) {
                DesignIconButton(
                    icon = Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(Res.string.collapse),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(12.dp)
                        .testTag("card-collapse"),
                    onClick = onCollapse,
                )
            }
        }
    }
}
