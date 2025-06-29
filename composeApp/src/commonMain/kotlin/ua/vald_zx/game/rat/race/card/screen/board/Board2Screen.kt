package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieAnimatable
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import rat_race_card.composeapp.generated.resources.Bubbleboddy
import rat_race_card.composeapp.generated.resources.Res
import rat_race_card.composeapp.generated.resources.logo
import ua.vald_zx.game.rat.race.card.components.OutlinedText
import ua.vald_zx.game.rat.race.card.components.Rotation
import ua.vald_zx.game.rat.race.card.components.SkittlesRainbow
import ua.vald_zx.game.rat.race.card.components.optionalModifier
import ua.vald_zx.game.rat.race.card.components.rotateLayout
import ua.vald_zx.game.rat.race.card.currentPlayerId
import ua.vald_zx.game.rat.race.card.logic.BoardLayer
import ua.vald_zx.game.rat.race.card.logic.RatRace2BoardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2BoardState
import ua.vald_zx.game.rat.race.card.logic.RatRace2BoardStore
import ua.vald_zx.game.rat.race.card.logic.players
import ua.vald_zx.game.rat.race.card.raceRate2BoardStore
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Back
import ua.vald_zx.game.rat.race.card.resource.images.Dice
import ua.vald_zx.game.rat.race.card.resource.images.Money
import ua.vald_zx.game.rat.race.card.resource.images.RatPlayer1
import ua.vald_zx.game.rat.race.card.resource.images.UpDoubleArrow
import ua.vald_zx.game.rat.race.card.shared.pointerColors
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import kotlin.math.absoluteValue

enum class Side(val isHorizontal: Boolean) {
    TOP(true), LEFT(false), BOTTOM(true), RIGHT(false)
}

data class Location(
    val side: Side,
    val position: Int,
)

data class Place(
    val type: PlaceType,
    val location: Location,
    val offset: DpOffset,
    val size: DpSize,
)

private val positionX = -30f

data class BoardRoute(
    val horizontalCells: Int,
    val verticalCells: Int,
    val places: List<PlaceType>,
)

class Board2Screen : Screen {

    override val key: ScreenKey = "Board2Screen"

    @Composable
    override fun Content() {
        val state by raceRate2BoardStore.observeState().collectAsState()
        Board2(state, raceRate2BoardStore::dispatch)
    }
}

@Composable
fun Board2(state: RatRace2BoardState, dispatch: (RatRace2BoardAction) -> Unit) {
    val rotX = remember { Animatable(positionX) }
    val rotY = remember { Animatable(0f) }
    BoxWithConstraints(
        modifier = Modifier.systemBarsPadding()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .rotateOnDrag(rotX, rotY)
                .align(Alignment.Center),
        ) {
            val minSide = min(maxWidth, maxHeight)
            val outSpotWidth = (minSide / outRoute.horizontalCells)
            val outSpotHeight = (minSide / outRoute.horizontalCells)
            val outBoardWidth = outSpotWidth * outRoute.horizontalCells
            val outBoardHeight = outSpotHeight * outRoute.verticalCells
            val boardOffset = remember(minSide) {
                val x = ((maxWidth - outBoardWidth) / 2)
                val y = ((maxHeight - outBoardHeight) / 3)
                DpOffset(x, y)
            }
            val inPadding = outSpotWidth / 3
            val inSpotWidth =
                ((minSide - outSpotWidth * 4 - inPadding * 2) / inRoute.horizontalCells)
            val inSpotHeight =
                (((outSpotWidth * inRoute.verticalCells) - outSpotWidth * 4 - inPadding * 2) / inRoute.verticalCells)

            val inBoardWidth = inSpotWidth * inRoute.horizontalCells
            val inBoardHeight = inSpotHeight * inRoute.verticalCells
            val scale by animateFloatAsState(if (state.layer == BoardLayer.INNER) 1.3f else 1.0f)
            val isVertical = maxHeight > maxWidth
            val rotate by animateFloatAsState(if (isVertical) -90f else 0f)
            ColorsSelector(state, dispatch)
            Box(
                modifier = Modifier.offset(boardOffset.x, boardOffset.y)
                    .size(width = outBoardWidth, height = outBoardHeight)
                    .graphicsLayer {
                        rotationX = (-rotX.value).coerceIn(-180f, 180f)
                        rotationY = rotY.value.coerceIn(-180f, 180f)
                        cameraDistance = 25f
                        shape = RoundedCornerShape(6.dp)
                        scaleX = scale
                        scaleY = scale
                    }
                    .rotate(rotate)
            ) {
                val outSize = DpSize(outBoardWidth, outBoardHeight)
                val inSize = DpSize(inBoardWidth, inBoardHeight)
                Box(
                    modifier = Modifier.fillMaxSize()
                        .shadow(30.dp, shape = RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Places(
                        state = state,
                        layer = BoardLayer.OUTER,
                        size = outSize,
                        route = outRoute,
                        isVertical = isVertical,
                    )
                    Places(
                        state = state,
                        layer = BoardLayer.INNER,
                        size = inSize,
                        route = inRoute,
                        isVertical = isVertical,
                    )
                }
                if (isFlipped(rotY, rotX)) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .shadow(30.dp, shape = RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF45465C))
                            .border(
                                width = 6.dp,
                                shape = RoundedCornerShape(16.dp),
                                brush = Brush.horizontalGradient(SkittlesRainbow)
                            )
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.logo),
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }

        ElevatedButton(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                .align(Alignment.TopStart),
            onClick = {
                dispatch(RatRace2BoardAction.SwitchLayer)
            },
            content = {
                Icon(
                    Images.UpDoubleArrow,
                    contentDescription = null,
                    modifier = Modifier
                        .optionalModifier(state.layer == BoardLayer.OUTER) {
                            rotate(180f)
                        })
            },
        )

        val cube1 by rememberLottieComposition {
            LottieCompositionSpec.JsonString(
                Res.readBytes("files/cube_1.json").decodeToString()
            )
        }
        val cube2 by rememberLottieComposition {
            LottieCompositionSpec.JsonString(
                Res.readBytes("files/cube_2.json").decodeToString()
            )
        }
        val cube3 by rememberLottieComposition {
            LottieCompositionSpec.JsonString(
                Res.readBytes("files/cube_3.json").decodeToString()
            )
        }
        val cube4 by rememberLottieComposition {
            LottieCompositionSpec.JsonString(
                Res.readBytes("files/cube_4.json").decodeToString()
            )
        }
        val cube5 by rememberLottieComposition {
            LottieCompositionSpec.JsonString(
                Res.readBytes("files/cube_5.json").decodeToString()
            )
        }
        val cube6 by rememberLottieComposition {
            LottieCompositionSpec.JsonString(
                Res.readBytes("files/cube_6.json").decodeToString()
            )
        }
        val animatable = rememberLottieAnimatable()
        val coroutineScope = rememberCoroutineScope()
        var composition by remember { mutableStateOf<LottieComposition?>(null) }
        var dice by remember { mutableStateOf(0) }
        LaunchedEffect(animatable.isAtEnd) {
            if (animatable.isAtEnd) {
                dispatch(RatRace2BoardAction.Move(dice))
            }
        }
        ElevatedButton(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                .align(Alignment.BottomStart),
            onClick = {
                dispatch(RatRace2BoardAction.BackLastMove)
            },
            content = { Icon(Images.Back, contentDescription = null) },
            enabled = state.positionsHistory.size > 1
        )
        Image(
            painter = rememberLottiePainter(
                composition = composition,
                progress = animatable::value
            ),
            contentDescription = "Lottie animation",
            modifier = Modifier.align(Alignment.BottomCenter).size(100.dp)
        )
        ElevatedButton(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                .align(Alignment.BottomEnd),
            onClick = {
                dice = (1..6).random()
                composition = when (dice) {
                    1 -> cube1
                    2 -> cube2
                    3 -> cube3
                    4 -> cube4
                    5 -> cube5
                    else -> cube6
                }
                coroutineScope.launch {
                    animatable.animate(composition, iterations = 1, initialProgress = 0f)
                }
            },
            content = { Icon(Images.Dice, contentDescription = null) }
        )
    }
}

@Composable
private fun BoxScope.Places(
    state: RatRace2BoardState,
    layer: BoardLayer,
    size: DpSize,
    route: BoardRoute,
    isVertical: Boolean,
) {
    val spotWidth = size.width / route.horizontalCells
    val spotHeight = size.height / route.verticalCells
    val places = remember(route.places, size) {
        var placeOffset = 0
        route.places.map { type ->
            val location =
                getLocationOnBoard(placeOffset, route.horizontalCells, route.verticalCells)
            val cellSize = type.getDpSize(location, spotWidth, spotHeight)
            val cellOffset = type.dpOffset(
                location,
                spotWidth,
                spotHeight,
                route.horizontalCells,
                route.verticalCells
            )
            placeOffset += if (type.isBig) 2 else 1
            Place(type, location, cellOffset, cellSize)
        }
    }
    val alpha by animateFloatAsState(if (state.layer == layer) 1f else 0.3f)
    Box(
        modifier = Modifier.align(Alignment.Center).size(size).alpha(alpha)
    ) {
        places.forEach { place ->
            Box(
                modifier = Modifier
                    .size(width = place.size.width, height = place.size.height)
                    .offset(x = place.offset.x, y = place.offset.y)
                    .background(place.type.color)
            ) {
                val isVertical =
                    (place.location.side == Side.TOP || place.location.side == Side.BOTTOM) && !place.type.isBig
                PlaceItem(place.type, place.size, isVertical)
            }
        }
        val players by players
        val points = remember(players) {
            players.filter { layer.level == it.state.level }.map {
                PlayerPointState(
                    position = it.state.position,
                    color = it.attrs.color,
                    level = it.state.level
                )
            }
        }
        points.groupBy { it.position }.forEach { (_, gPoints) ->
            gPoints.forEachIndexed { index, state ->
                PlayerPoint(
                    places = places,
                    state = state,
                    spotWidth = spotWidth,
                    spotHeight = spotHeight,
                    isVertical = isVertical,
                    index = index,
                    count = gPoints.size
                )
            }
        }
    }
}

data class PlayerPointState(
    val position: Int,
    val color: Long,
    val level: Int,
)

@Composable
private fun PlayerPoint(
    places: List<Place>,
    state: PlayerPointState,
    spotWidth: Dp,
    spotHeight: Dp,
    isVertical: Boolean,
    index: Int,
    count: Int,
) {
    var offset by remember {
        val place = places[state.position]
        mutableStateOf(calculatePointerOffset(spotWidth, spotHeight, place, index, count))
    }
    LaunchedEffect(state.position, count, spotWidth, spotHeight) {
        val place = places[state.position]
        offset = calculatePointerOffset(spotWidth, spotHeight, place, index, count)
    }
    val animatedX by animateDpAsState(offset.first)
    val animatedY by animateDpAsState(offset.second)
    Box(
        modifier = Modifier
            .offset(animatedX, animatedY)
            .size(spotWidth, spotHeight)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .border(
                width = spotWidth * 0.1f,
                shape = CircleShape,
                color = Color(state.color)
            )
    ) {
        Image(
            imageVector = Images.RatPlayer1,
            contentDescription = null,
            modifier = Modifier.rotate(if (isVertical) 90f else 0f)
        )
    }
}

@Composable
private fun BoxScope.PlaceItem(
    placeType: PlaceType,
    cellSize: DpSize,
    isVertical: Boolean,
) {
    when (placeType) {
        PlaceType.Salary -> {
            Image(Images.Money, contentDescription = null)
        }

        else -> {
            OutlinedText(
                text = placeType.text,
                autoSize = TextAutoSize.StepBased(minFontSize = 1.sp),
                fontFamily = FontFamily(
                    Font(
                        Res.font.Bubbleboddy,
                        weight = FontWeight.Medium
                    )
                ),
                modifier = Modifier.align(Alignment.Center)
                    .padding(min(cellSize.height, cellSize.width) / 14)
                    .optionalModifier(isVertical) {
                        rotateLayout(Rotation.ROT_90)
                    },
                fillColor = Color.White,
                outlineColor = Color(0xFF8A8A8A),
                outlineDrawStyle = Stroke(2f),
                maxLines = 1
            )
        }
    }
}

@Composable
fun BoxWithConstraintsScope.ColorsSelector(
    state: RatRace2BoardState,
    dispatch: (RatRace2BoardAction) -> Unit
) {
    val players by players
    FlowRow(modifier = Modifier.align(Alignment.TopCenter).padding(horizontal = 64.dp)) {
        (pointerColors - players.filter { it.id != currentPlayerId }.map { it.attrs.color }).forEach { color ->
            RadioButton(
                selected = color == state.color,
                onClick = { dispatch(RatRace2BoardAction.ChangeColor(color)) },
                colors = RadioButtonDefaults.colors()
                    .copy(selectedColor = Color(color), unselectedColor = Color(color)),
            )
        }
    }
}

private fun PlaceType.getDpSize(
    location: Location,
    spotWidth: Dp,
    spotHeight: Dp,
): DpSize {
    return if (isBig) {
        DpSize(spotWidth * 2, spotHeight * 2)
    } else {
        if (location.side.isHorizontal) {
            DpSize(spotWidth, spotHeight * 2)
        } else {
            DpSize(spotWidth * 2, spotHeight)
        }
    }
}

private fun PlaceType.dpOffset(
    location: Location,
    spotWidth: Dp,
    spotHeight: Dp,
    cellX: Int,
    cellY: Int,
): DpOffset {
    return when (location.side) {
        Side.TOP -> DpOffset(
            x = spotWidth * location.position,
            y = 0.dp
        )

        Side.LEFT -> DpOffset(
            x = spotWidth * (cellX - 2),
            y = spotHeight * location.position
        )

        Side.BOTTOM -> DpOffset(
            x = spotWidth * (location.position - if (isBig) 1 else 0),
            y = spotHeight * (cellY - 2),
        )

        Side.RIGHT -> DpOffset(
            x = 0.dp,
            y = spotHeight * (location.position - if (isBig) 1 else 0)
        )
    }
}

private fun getLocationOnBoard(
    position: Int,
    cellX: Int,
    cellY: Int
): Location {
    val leftSideMax = cellX + cellY - 2
    val bottomSideMax = leftSideMax + cellX - 2
    return if (position < cellX) {
        Location(Side.TOP, position)
    } else if (position in cellX..<leftSideMax) {
        Location(Side.LEFT, position - cellX + 2)
    } else if (position in leftSideMax..<bottomSideMax) {
        Location(Side.BOTTOM, cellX - (position - leftSideMax) - 3)
    } else {
        Location(Side.RIGHT, cellY - (position - bottomSideMax + 3))
    }
}

private val PlaceType.text: String
    get() {
        return when (this) {
            PlaceType.Salary -> "Прибуток"
            PlaceType.Chance -> "Шанс!"
            PlaceType.Store -> "Ринок"
            PlaceType.Business -> "Бізнес"
            PlaceType.Deputy -> "Депутат"
            PlaceType.Expenses -> "Витрати"
            PlaceType.Shopping -> "Покупки"
            PlaceType.Rest -> "Відпустка"
            PlaceType.Desire -> "Мрія"
            PlaceType.Start -> "Старт"
            PlaceType.Exaltation -> "Звільнення"
            else -> name
        }
    }


val outRoute = BoardRoute(26, 18, outPlaces)
val inRoute = BoardRoute(28, 18, inPlaces)

private fun Modifier.rotateOnDrag(
    rotX: Animatable<Float, AnimationVector1D>,
    rotY: Animatable<Float, AnimationVector1D>
): Modifier = composed {
    var dRotX by remember { mutableStateOf(0f) }
    var dRotY by remember { mutableStateOf(0f) }
    pointerInput(Unit) {
        coroutineScope {
            while (true) {
                awaitPointerEventScope {
                    val pointerId = awaitFirstDown().run {
                        launch {
                            dRotX = 0f
                            dRotY = 0f
                        }
                        id
                    }
                    drag(pointerId) {
                        launch {
                            dRotX = rotX.value + it.positionChange().y * 0.09f
                            dRotY = rotY.value + it.positionChange().x * 0.09f
                            // TODO Constrain 180 deg flip only on one axis at a time
                            rotX.snapTo(dRotX)
                            rotY.snapTo(dRotY)
                        }
                    }
                    //region Restore animation
                    launch {
                        awaitAll(
                            async {
                                rotY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                                )
                            },
                            async {
                                rotX.animateTo(
                                    targetValue = positionX,
                                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                                )
                            }
                        )
                    }
                    //endregion
                }
            }
        }
    }
}

const val FlipThreshDeg = 90f

@Composable
fun isFlipped(
    rotY: Animatable<Float, AnimationVector1D>,
    rotX: Animatable<Float, AnimationVector1D>
) = rotY.value.absoluteValue > FlipThreshDeg || rotX.value.absoluteValue > FlipThreshDeg

@Preview
@Composable
fun Board2Preview() {
    val store = remember { RatRace2BoardStore() }
    val state by store.observeState().collectAsState()
    AppTheme {
        Board2(state, store::dispatch)
    }
}