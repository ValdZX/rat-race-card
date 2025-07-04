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
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import androidx.constraintlayout.compose.MotionScene
import androidx.constraintlayout.compose.Visibility
import io.github.aakira.napier.Napier
import ua.vald_zx.game.rat.race.card.logic.BoardLayer
import ua.vald_zx.game.rat.race.card.logic.RatRace2BoardState

const val animationDuration = 2000

@OptIn(ExperimentalMotionApi::class)
@Composable
fun CardDialog(state: RatRace2BoardState, selectedCard: BoardCard?) {
    if (selectedCard != null) {
        var targetProgress by remember { mutableStateOf(0f) }
        val progress by animateFloatAsState(
            targetValue = targetProgress,
            animationSpec = tween(durationMillis = animationDuration)
        )
        val coordinatesState = remember(selectedCard) {
            cardCoordinatesMap[selectedCard]
        }
        if (coordinatesState != null) {
            val coordinates by coordinatesState
            val motionScene = remember(coordinates.second, state.layer) {
                val scale = if (state.layer == BoardLayer.INNER) INNER_LAYER_SCALE else 1.0f
                val size = coordinates.second * scale
                val offset = coordinates.first
                MotionScene {
                    val back = createRefFor("back")
                    val front = createRefFor("front")
                    constraintSet("start") {
                        constrain(back) {
                            visibility = Visibility.Visible
                            start.linkTo(parent.start, offset.x)
                            top.linkTo(parent.top, offset.y)
                            width = Dimension.value(size.width)
                            height = Dimension.value(size.height)
                        }

                        constrain(front) {
                            visibility = Visibility.Gone
                            centerTo(parent)
                            rotationY = -90f
                        }
                    }

                    constraintSet("middle1") {
                        constrain(back) {
                            visibility = Visibility.Visible
                            centerTo(parent)
                            width = Dimension.value(size.width)
                            height = Dimension.value(size.height)
                            if (size.width < size.height) {
                                rotationZ = -90f
                            }
                        }
                        constrain(front) {
                            visibility = Visibility.Gone
                            centerTo(parent)
                            rotationY = -90f
                        }
                    }

                    constraintSet("middle2") {
                        constrain(back) {
                            visibility = Visibility.Visible
                            centerTo(parent)
                            width = Dimension.value(size.width)
                            height = Dimension.value(size.height)
                            rotationY = 90f
                            if (size.width < size.height) {
                                rotationZ = -90f
                            }
                        }
                        constrain(front) {
                            visibility = Visibility.Visible
                            centerTo(parent)
                            width = Dimension.value(size.width)
                            height = Dimension.value(size.height)
                            rotationY = -90f
                        }
                    }

                    constraintSet("end") {
                        constrain(back) {
                            visibility = Visibility.Gone
                            centerTo(parent)
                            if (size.width < size.height) {
                                rotationZ = -90f
                            }
                        }
                        constrain(front) {
                            visibility = Visibility.Visible
                            centerTo(parent)
                            width = Dimension.value(max(size.width, size.height) * 4)
                            height = Dimension.value(min(size.width, size.height) * 4)
                            rotationY = 0f
                        }
                    }
                }
            }
            MotionLayout(
                modifier = Modifier.fillMaxSize(),
                motionScene = motionScene,
                constraintSetName = when {
                    progress < 0.1f -> "start"
                    progress < 0.49f -> "middle1"
                    progress < 0.51f -> "middle2"
                    else -> "end"
                }.apply { Napier.d("constraintSetName $progress $this") },
                animationSpec = tween(animationDuration / 7)
            ) {
                BoxWithConstraints(modifier = Modifier.layoutId("back")) {
                    BoardCardBack(selectedCard)
                }
                BoxWithConstraints(modifier = Modifier.layoutId("front")) {
                    BoardCardFront(selectedCard)
                }
            }
            LaunchedEffect(Unit) {
                targetProgress = 1f
            }
        }
    }
}