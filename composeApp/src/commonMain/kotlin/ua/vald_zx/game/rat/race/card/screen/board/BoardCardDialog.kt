package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import androidx.constraintlayout.compose.MotionScene
import androidx.constraintlayout.compose.Visibility
import ua.vald_zx.game.rat.race.card.currentPlayerId
import ua.vald_zx.game.rat.race.card.logic.BoardAction
import ua.vald_zx.game.rat.race.card.logic.BoardLayer
import ua.vald_zx.game.rat.race.card.logic.BoardState

const val cardMoveAnimationDuration = 2000

@OptIn(ExperimentalMotionApi::class)
@Composable
fun CardDialog(
    state: BoardState,
    dispatch: (BoardAction) -> Unit
) {
    val takenCard = state.board?.takenCard
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
        val deckCoordinatesState = remember(card) {
            deckCoordinatesMap[card.type]
        }
        if (deckCoordinatesState != null) {
            val cardSize = DpSize(300.dp, 200.dp)
            val deckCoordinates by deckCoordinatesState
            val motionScene = remember(deckCoordinates.second, state.layer) {
                val scale = if (state.layer == BoardLayer.INNER) INNER_LAYER_SCALE else 1.0f
                val onBoardSize = deckCoordinates.second * scale
                val onBoardScaleX = onBoardSize.width / cardSize.width
                val onBoardScaleY = onBoardSize.height / cardSize.height
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
                            width = Dimension.value(cardSize.width)
                            height = Dimension.value(cardSize.height)
                            scaleX = onBoardScaleX
                            scaleY = onBoardScaleY
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
                            width = Dimension.value(cardSize.width)
                            height = Dimension.value(cardSize.height)
                            scaleX = onBoardScaleX
                            scaleY = onBoardScaleY
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
                            width = Dimension.value(cardSize.width)
                            height = Dimension.value(cardSize.height)
                            scaleX = onBoardScaleX
                            scaleY = onBoardScaleY
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
                            width = Dimension.value(cardSize.width)
                            height = Dimension.value(cardSize.height)
                            scaleX = onBoardScaleX
                            scaleY = onBoardScaleY
                            pivotX = 0.5f
                            pivotY = 0.5f
                            rotationY = -90f
                        }
                    }
                    val dialogShowed = constraintSet {
                        constrain(back) {
                            visibility = Visibility.Gone
                            centerTo(parent)
                            if (onBoardSize.width < onBoardSize.height) {
                                rotationZ = -90f
                            }
                        }
                        constrain(front) {
                            visibility = Visibility.Visible
                            centerTo(parent)
                            width = Dimension.value(cardSize.width)
                            height = Dimension.value(cardSize.height)
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
                            width = Dimension.value(cardSize.width)
                            height = Dimension.value(cardSize.height)
                            scaleX = onBoardScaleX
                            scaleY = onBoardScaleY
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
                    BoardCardBack(card.type, DpSize(maxWidth, maxHeight))
                }
                BoxWithConstraints(modifier = Modifier.layoutId("front")) {
                    val isActive = remember(card) { currentPlayerId == state.board?.activePlayer }
                    BoardCardFront(card, isActive, cardSize) {
                        dispatch(BoardAction.ToDiscardPile)
                        targetProgress = 6f
                    }
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