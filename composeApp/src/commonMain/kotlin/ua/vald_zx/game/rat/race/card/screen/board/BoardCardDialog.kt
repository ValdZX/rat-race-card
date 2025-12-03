package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.constraintlayout.compose.*
import ua.vald_zx.game.rat.race.card.currentPlayerId
import ua.vald_zx.game.rat.race.card.isVertical
import ua.vald_zx.game.rat.race.card.logic.BoardState
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.max
import ua.vald_zx.game.rat.race.card.shared.BoardLayer

const val cardMoveAnimationDuration = 2000

@OptIn(ExperimentalMotionApi::class)
@Composable
fun BoxWithConstraintsScope.CardDialog(
    state: BoardState,
    vm: BoardViewModel,
) {
    val takenCard = state.board.takenCard
    var showDialog by remember { mutableStateOf(false) }
    LaunchedEffect(takenCard) {
        if (takenCard != null) {
            showDialog = true
        }
    }
    if (showDialog) {
        val card = remember { takenCard ?: error("Illegal state") }
        var targetProgress by remember { mutableStateOf(0f) }

        val progress by animateFloatAsState(
            targetValue = targetProgress,
            animationSpec = tween(durationMillis = cardMoveAnimationDuration)
        )
//        var progress by remember { mutableStateOf(0f) }
//        Slider(
//            value = progress,
//            onValueChange = { progress = it },
//            valueRange = 0f..6f,
//            steps = 120,
//            modifier = Modifier.padding(30.dp)
//        )
        val deckCoordinatesState = remember(card) {
            deckCoordinatesMap[card.type]
        }
        if (deckCoordinatesState != null) {
            val dialogWidth = min(max(min(maxWidth, maxHeight) * 2.7f / 3f, 300.dp), 600.dp)
            val dialogSize = DpSize(dialogWidth, dialogWidth * 2f / 3f)
            val deckCoordinates by deckCoordinatesState
            val scale = if (state.layer == BoardLayer.INNER) INNER_LAYER_SCALE else 1.0f
            val onBoardSize = deckCoordinates.second
            val onBoardSizeScaled = onBoardSize * scale
            val onBoardScaleToLow = onBoardSizeScaled.max / dialogSize.max
            val motionScene = remember(onBoardSize, state.layer) {
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
                            height = Dimension.value(dialogSize.height)
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
                            height = Dimension.value(dialogSize.height)
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
                    val isActive = remember(card) { currentPlayerId == state.board.activePlayer }
                    BoardCardFront(
                        card = card,
                        isActive = isActive,
                        size = dialogSize,
                        discardCard = { targetProgress = 6f },
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
        }
    }
}