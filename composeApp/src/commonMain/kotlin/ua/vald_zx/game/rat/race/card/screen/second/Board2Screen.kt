package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
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
import ua.vald_zx.game.rat.race.card.logic.BoardLayer
import ua.vald_zx.game.rat.race.card.logic.RatRace2BoardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2BoardState
import ua.vald_zx.game.rat.race.card.logic.RatRace2BoardStore
import ua.vald_zx.game.rat.race.card.raceRate2BoardStore
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Back
import ua.vald_zx.game.rat.race.card.resource.images.Dice
import ua.vald_zx.game.rat.race.card.resource.images.Money
import ua.vald_zx.game.rat.race.card.resource.images.RatPlayer1
import ua.vald_zx.game.rat.race.card.resource.images.UpDoubleArrow
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

sealed class PlaceType(val name: String, val color: Color, val isBig: Boolean = false) {
    data object Start : PlaceType("Start", Color.Yellow)
    data object Salary : PlaceType("Salary", Color(0xffa1e64c), isBig = true)
    data object Business : PlaceType("Business", Color(0xff00ba87))
    data object Shopping : PlaceType("Shopping", Color.Cyan)
    data object Chance : PlaceType("Chance", Color(0xfff36d4e))
    data object Expenses : PlaceType("Expenses", Color.Red)
    data object Store : PlaceType("Store", Color(0xff0788e8))
    data object Bankruptcy : PlaceType("Bankruptcy", Color(0xff94a5dd), isBig = true)
    data object Child : PlaceType("Child", Color.Black, isBig = true)
    data object Love : PlaceType("Love", Color.Magenta)
    data object Rest : PlaceType("Rest", Color.White)
    data object Divorce : PlaceType("Divorce", Color.Red)
    data object Desire : PlaceType("Desire", Color(0xffde9bc2))
    data object Deputy : PlaceType("Deputy", Color(0xff8a8fdc))
    data object TaxInspection : PlaceType("TaxInspection", Color(0xffc5dcc7), isBig = true)
    data object Exaltation : PlaceType("Exaltation", Color.Black)
}

private val positionX = -40f

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
            val minSide = min(maxWidth, maxHeight) * 0.85f
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
            val scale by animateFloatAsState(if(state.layer == BoardLayer.INNER) 1.3f else 1.0f)
            val rotate by animateFloatAsState(if(maxHeight > maxWidth) -90f else 0f)

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
                    )
                    Places(
                        state = state,
                        layer = BoardLayer.INNER,
                        size = inSize,
                        route = inRoute,
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
                val dice = (1..6).random()
                composition = when (dice) {
                    1 -> cube1
                    2 -> cube2
                    3 -> cube3
                    4 -> cube4
                    5 -> cube5
                    else -> cube6
                }
                dispatch(RatRace2BoardAction.Move(dice))
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
        if (state.layer == layer) {
            val place = remember(state.position) {
                places[state.position]
            }
            Image(
                imageVector = Images.RatPlayer1,
                contentDescription = null,
                modifier = Modifier
                    .offset(place.offset.x, place.offset.y)
                    .offset(
                        place.size.width / 2 - spotWidth / 2,
                        place.size.height / 2 - spotHeight / 2
                    )
                    .size(spotWidth, spotHeight)
            )
        }
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

private val inPlaces = listOf(
    PlaceType.Salary,
    PlaceType.Start,
    PlaceType.Business,
    PlaceType.Shopping,
    PlaceType.Chance,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Shopping,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Business,
    PlaceType.Bankruptcy,
    PlaceType.Store,
    PlaceType.Expenses,
    PlaceType.Business,
    PlaceType.Chance,
    PlaceType.Shopping,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Business,
    PlaceType.Expenses,
    PlaceType.Store,

    PlaceType.Salary,
    PlaceType.Chance,
    PlaceType.Business,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Love,
    PlaceType.Shopping,
    PlaceType.Chance,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Rest,
    PlaceType.Chance,
    PlaceType.Business,
    PlaceType.Expenses,
    PlaceType.Store,

    PlaceType.Salary,
    PlaceType.Shopping,
    PlaceType.Chance,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Business,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Shopping,
    PlaceType.Business,
    PlaceType.Child,
    PlaceType.Expenses,
    PlaceType.Chance,
    PlaceType.Business,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Divorce,
    PlaceType.Shopping,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Expenses,

    PlaceType.Salary,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Business,
    PlaceType.Exaltation,
    PlaceType.Expenses,
    PlaceType.Chance,
    PlaceType.Shopping,
    PlaceType.Business,
    PlaceType.Love,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Expenses,
)

private val outPlaces = listOf(
    PlaceType.Salary,
    PlaceType.Start,
    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.Business,
    PlaceType.Desire,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Desire,
    PlaceType.Business,
    PlaceType.Store,
    PlaceType.Bankruptcy,
    PlaceType.Chance,
    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.Desire,
    PlaceType.Business,
    PlaceType.Chance,
    PlaceType.Store,
    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.Desire,
    PlaceType.Salary,

    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.Business,
    PlaceType.Deputy,
    PlaceType.Desire,
    PlaceType.Chance,
    PlaceType.Store,
    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.Business,
    PlaceType.Deputy,
    PlaceType.Desire,
    PlaceType.Chance,
    PlaceType.Store,
    PlaceType.Salary,

    PlaceType.Chance,
    PlaceType.Desire,
    PlaceType.Business,
    PlaceType.Shopping,
    PlaceType.Desire,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Desire,
    PlaceType.Business,
    PlaceType.Chance,
    PlaceType.TaxInspection,
    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.Desire,
    PlaceType.Store,
    PlaceType.Business,
    PlaceType.Desire,
    PlaceType.Chance,
    PlaceType.Shopping,
    PlaceType.Desire,
    PlaceType.Store,
    PlaceType.Salary,

    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.Business,
    PlaceType.Deputy,
    PlaceType.Desire,
    PlaceType.Chance,
    PlaceType.Store,
    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.Chance,
    PlaceType.Deputy,
    PlaceType.Desire,
    PlaceType.Chance,
    PlaceType.Store,
)


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

@Composable
fun animateDpSizeAsState(
    targetValue: DpSize,
    animationSpec: AnimationSpec<DpSize> = spring(),
    label: String = "SizeAnimation",
    finishedListener: ((DpSize) -> Unit)? = null
): State<DpSize> {
    return animateValueAsState(
        targetValue,
        TwoWayConverter(
            convertToVector = { AnimationVector2D(it.width.value, it.height.value) },
            convertFromVector = { DpSize(it.v1.dp, it.v2.dp) }
        ),
        animationSpec,
        label = label,
        finishedListener = finishedListener
    )
}

@Preview
@Composable
fun Board2Preview() {
    val store = remember { RatRace2BoardStore() }
    val state by store.observeState().collectAsState()
    AppTheme {
        Board2(state, store::dispatch)
    }
}