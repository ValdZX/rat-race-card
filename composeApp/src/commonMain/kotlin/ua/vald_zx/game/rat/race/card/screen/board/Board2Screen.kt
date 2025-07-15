package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.constraintlayout.compose.ExperimentalMotionApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import com.composables.core.BottomSheet
import com.composables.core.SheetDetent
import com.composables.core.SheetDetent.Companion.FullyExpanded
import com.composables.core.rememberBottomSheetState
import com.russhwolf.settings.set
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieAnimatable
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import rat_race_card.composeapp.generated.resources.Res
import rat_race_card.composeapp.generated.resources.logo
import ua.vald_zx.game.rat.race.card.components.SkittlesRainbow
import ua.vald_zx.game.rat.race.card.currentPlayerId
import ua.vald_zx.game.rat.race.card.logic.BoardAction
import ua.vald_zx.game.rat.race.card.logic.BoardLayer
import ua.vald_zx.game.rat.race.card.logic.BoardSideEffect
import ua.vald_zx.game.rat.race.card.logic.BoardState
import ua.vald_zx.game.rat.race.card.logic.BoardStore
import ua.vald_zx.game.rat.race.card.logic.players
import ua.vald_zx.game.rat.race.card.raceRate2BoardStore
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Dice
import ua.vald_zx.game.rat.race.card.resource.images.IcDarkMode
import ua.vald_zx.game.rat.race.card.resource.images.IcLightMode
import ua.vald_zx.game.rat.race.card.settings
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.pointerColors
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark
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
) {
    val isVertical: Boolean
        get() = (location.side == Side.TOP || location.side == Side.BOTTOM) && !type.isBig
}

data class Board(
    val layers: Map<BoardLayer, BoardRoute>
)

data class BoardRoute(
    val horizontalCells: Int,
    val verticalCells: Int,
    val places: List<PlaceType>,
    val offset: Int = 0,
) {
    fun rotate(): BoardRoute {
        val offset = horizontalCells - 4
        val firstPart = places.take(offset)
        val secondPart = places.drop(offset)
        return BoardRoute(
            horizontalCells = verticalCells,
            verticalCells = horizontalCells,
            places = secondPart + firstPart,
            offset = places.size - offset
        )
    }
}

const val INNER_LAYER_SCALE = 1.2f

val navigationBarHeightState = mutableStateOf(0.dp)
val statusBarHeightState = mutableStateOf(0.dp)
val deckCoordinatesMap = mutableMapOf<BoardCardType, MutableState<Pair<DpOffset, DpSize>>>()
val discardPilesCoordinatesMap = mutableMapOf<BoardCardType, MutableState<Pair<DpOffset, DpSize>>>()
val littleDetailsHeight = 100.dp
val HalfExpanded: SheetDetent =
    SheetDetent("hidden") { containerHeight, sheetHeight -> littleDetailsHeight }

class Board2Screen : Screen {

    override val key: ScreenKey = "Board2Screen"

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val state by raceRate2BoardStore.observeState().collectAsState()
        var dice by remember { mutableStateOf(0) }
        val scaffoldState = rememberBottomSheetState(
            initialDetent = HalfExpanded,
            detents = listOf(HalfExpanded, FullyExpanded)
        )
        Box {
            BottomSheetNavigator {
                BoardScreenContent(state, raceRate2BoardStore::dispatch, dice)
                Box(modifier = Modifier.systemBarsPadding()) {
                    BottomSheet(state = scaffoldState) { Board2PlayerDetailsScreen(scaffoldState) }
                }
            }
            val density = LocalDensity.current
            Box(
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .onGloballyPositioned {
                        with(density) { navigationBarHeightState.value = it.size.height.toDp() }
                    }
            ) {
                Box(modifier = Modifier.navigationBarsPadding())
            }
            Box(modifier = Modifier.fillMaxWidth().onGloballyPositioned {
                with(density) { statusBarHeightState.value = it.size.height.toDp() }
            }) {
                Box(modifier = Modifier.statusBarsPadding())
            }
        }
        LaunchedEffect(Unit) {
            raceRate2BoardStore.observeSideEffect().onEach { effect ->
                when (effect) {
                    is BoardSideEffect.ShowDice -> {
                        dice = effect.dice
                        delay(5000)
                        dice = 0
                    }
                }
            }.launchIn(this)
        }
    }
}

@OptIn(ExperimentalMotionApi::class, ExperimentalMaterialApi::class)
@Composable
fun BoardScreenContent(
    state: BoardState,
    dispatch: (BoardAction) -> Unit,
    dice: Int
) {
    Box(modifier = Modifier.fillMaxSize().padding(bottom = littleDetailsHeight)) {
        Board(state, dispatch)
        Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Controls(state, dispatch, dice)
        }
        CardDialog(state, dispatch)
    }
}

@Composable
fun Board(
    state: BoardState,
    dispatch: (BoardAction) -> Unit,
) {
    val rotX = remember { Animatable(0f) }
    val rotY = remember { Animatable(0f) }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .rotateOnDrag(rotX, rotY)
    )
    {
        val outRoute = boardLayers.layers[BoardLayer.OUTER] ?: error("Fix board")
        val horizontalRatio =
            outRoute.horizontalCells.toFloat() / outRoute.verticalCells.toFloat()
        val verticalRatio =
            outRoute.verticalCells.toFloat() / outRoute.horizontalCells.toFloat()
        val isVertical = maxHeight > maxWidth
        val scale by animateFloatAsState(if (state.layer == BoardLayer.INNER) INNER_LAYER_SCALE else 1.0f)
        BoxWithConstraints(
            modifier = Modifier
                .padding(32.dp)
                .align(Alignment.Center)
                .let { modifier ->
                    if (isVertical) {
                        if (maxWidth / maxHeight > horizontalRatio) {
                            modifier.fillMaxWidth().aspectRatio(verticalRatio)
                        } else {
                            modifier.fillMaxHeight().aspectRatio(verticalRatio)
                        }
                    } else {
                        if (maxWidth / maxHeight > horizontalRatio) {
                            modifier.fillMaxHeight().aspectRatio(horizontalRatio)
                        } else {
                            modifier.fillMaxWidth().aspectRatio(horizontalRatio)
                        }
                    }
                }
                .graphicsLayer {
                    rotationX = (-rotX.value).coerceIn(-180f, 180f)
                    rotationY = rotY.value.coerceIn(-180f, 180f)
                    cameraDistance = 25f
                    shape = RoundedCornerShape(6.dp)
                    scaleX = scale
                    scaleY = scale
                }
        ) {
            Board(state, isVertical, dispatch)
            if (isFlipped(rotY, rotX)) {
                BackSide()
            }
        }
    }
}

@Composable
fun BoxScope.Controls(
    state: BoardState,
    dispatch: (BoardAction) -> Unit,
    dice: Int
) {
    var isDark by LocalThemeIsDark.current
    val icon = remember(isDark) {
        if (isDark) Images.IcLightMode
        else Images.IcDarkMode
    }
    IconButton(
        modifier = Modifier.align(Alignment.TopEnd),
        onClick = {
            isDark = !isDark
            settings["theme"] = isDark
        },
        content = {
            Icon(icon, contentDescription = null)
        }
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
    LaunchedEffect(dice) {
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
    }
    LaunchedEffect(animatable.isAtEnd) {
        if (animatable.isAtEnd && currentPlayerId == state.board?.activePlayer && dice != 0) {
            dispatch(BoardAction.Move(dice))
        }
    }
    AnimatedVisibility(
        visible = dice != 0,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.Center)
    ) {
        Image(
            painter = rememberLottiePainter(
                composition = composition,
                progress = animatable::value
            ),
            contentDescription = "Lottie animation",
            modifier = Modifier.size(100.dp)
        )
    }
    if (state.canRoll) {
        ElevatedButton(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                .align(Alignment.BottomEnd),
            onClick = {
                dispatch(BoardAction.RollDice)
            },
            content = { Icon(Images.Dice, contentDescription = null) }
        )
    }
}


@Composable
fun BackSide() {
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

@Composable
fun BoxWithConstraintsScope.Board(
    state: BoardState,
    isVertical: Boolean,
    dispatch: (BoardAction) -> Unit
) {
    val maxWidth = maxWidth
    val maxHeight = maxHeight
    val density = LocalDensity.current
    var isDark by LocalThemeIsDark.current
    Box(
        modifier = Modifier.fillMaxSize()
            .shadow(30.dp, shape = RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isDark) {
                    Brush.radialGradient(
                        0.0f to Color(0xFFE6C85B), // м’яке золото
                        0.4f to Color(0xFFD9A848), // глибше золото
                        0.6f to Color(0xFFB3752E), // мідь
                        1.0f to Color(0x66000000), // напівпрозорий чорний (тінь/глибина)
                        radius = with(density) { min(maxWidth, maxHeight).toPx() },
                        tileMode = TileMode.Repeated
                    )
                } else {
                    Brush.radialGradient(
                        0.0f to Color(0xFFFFEC73),
                        0.4f to Color(0xFFFFDC85),
                        0.6f to Color(0xFFFFB370),
                        1.0f to Color(0xFFFFB370),
                        radius = with(density) { min(maxWidth, maxHeight).toPx() },
                        tileMode = TileMode.Repeated
                    )
                }
            )
    ) {
        val outRoute = boardLayers.layers[BoardLayer.OUTER] ?: return
        val inRoute = boardLayers.layers[BoardLayer.INNER] ?: return
        val actualOutRoute = remember(isVertical) {
            if (isVertical) outRoute.rotate() else outRoute
        }
        Places(
            state = state,
            layer = BoardLayer.OUTER,
            size = DpSize(maxWidth, maxHeight),
            route = actualOutRoute,
            dispatch = dispatch,
        )
        val outSpotSize =
            maxWidth / actualOutRoute.horizontalCells
        val inPadding = outSpotSize / 3
        val actualInRoute = remember(isVertical) {
            if (isVertical) inRoute.rotate() else inRoute
        }
        val inBoardWidth =
            (maxWidth - outSpotSize * 4 - inPadding * 2)
        val inBoardHeight =
            (maxHeight - outSpotSize * 4 - inPadding * 2)
        Places(
            state = state,
            layer = BoardLayer.INNER,
            size = DpSize(inBoardWidth, inBoardHeight),
            route = actualInRoute,
            dispatch = dispatch,
        )
        val inSpotSize =
            inBoardWidth / actualInRoute.horizontalCells
        val cardsPadding = inSpotSize
        val cardsWidth =
            (inBoardWidth - inSpotSize * 4 - cardsPadding * 2)
        val cardsHeight =
            (inBoardHeight - inSpotSize * 4 - cardsPadding * 2)
        CardDecks(
            state = state,
            size = DpSize(cardsWidth, cardsHeight),
            highlightedCard = state.highlightedCard,
            dispatch = dispatch
        )
    }
}

@Composable
fun BoxScope.CardDecks(
    size: DpSize,
    highlightedCard: BoardCardType?,
    dispatch: (BoardAction) -> Unit,
    state: BoardState
) {
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .size(size.width, size.height)
    ) {
        if (size.width < size.height) {
            val width = size.width / 5
            val height = (width * 3) / 2
            val cardSize = DpSize(width, height)
            Column(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopStart),
                verticalArrangement = Arrangement.spacedBy(width / 2)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LeftCardDecks(highlightedCard, cardSize, dispatch = dispatch, state = state)
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LeftDiscardPiles(cardSize, state = state)
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(width / 2)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RightDiscardPiles(cardSize, state = state)
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RightCardDecks(highlightedCard, cardSize, dispatch = dispatch, state = state)
                }
            }
        } else {
            val height = size.height / 5
            val width = (height * 3) / 2
            val cardSize = DpSize(width, height)
            Row(
                modifier = Modifier.fillMaxHeight().align(Alignment.TopStart),
                horizontalArrangement = Arrangement.spacedBy(height / 2)
            ) {
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    LeftCardDecks(highlightedCard, cardSize, dispatch = dispatch, state = state)
                }
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    LeftDiscardPiles(
                        size = cardSize,
                        state = state,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxHeight().align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.spacedBy(height / 2)
            ) {
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    RightDiscardPiles(cardSize, state = state)
                }
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    RightCardDecks(highlightedCard, cardSize, dispatch = dispatch, state = state)
                }
            }
        }
    }
}

@Composable
fun LeftCardDecks(
    highlightedCard: BoardCardType?,
    size: DpSize,
    dispatch: (BoardAction) -> Unit,
    state: BoardState
) {
    CardDeck(BoardCardType.Chance, size, highlightedCard, state, dispatch)
    CardDeck(BoardCardType.BigBusiness, size, highlightedCard, state, dispatch)
    CardDeck(BoardCardType.MediumBusiness, size, highlightedCard, state, dispatch)
    CardDeck(BoardCardType.SmallBusiness, size, highlightedCard, state, dispatch)
}

@Composable
fun RightCardDecks(
    highlightedCard: BoardCardType?,
    size: DpSize,
    dispatch: (BoardAction) -> Unit,
    state: BoardState
) {
    CardDeck(BoardCardType.Expenses, size, highlightedCard, state, dispatch)
    CardDeck(BoardCardType.Deputy, size, highlightedCard, state, dispatch)
    CardDeck(BoardCardType.EventStore, size, highlightedCard, state, dispatch)
    CardDeck(BoardCardType.Shopping, size, highlightedCard, state, dispatch)
}

@Composable
fun LeftDiscardPiles(
    size: DpSize,
    state: BoardState
) {
    DiscardPile(BoardCardType.Chance, size, state)
    DiscardPile(BoardCardType.BigBusiness, size, state)
    DiscardPile(BoardCardType.MediumBusiness, size, state)
    DiscardPile(BoardCardType.SmallBusiness, size, state)
}

@Composable
fun RightDiscardPiles(
    size: DpSize,
    state: BoardState
) {
    DiscardPile(BoardCardType.Expenses, size, state)
    DiscardPile(BoardCardType.Deputy, size, state)
    DiscardPile(BoardCardType.EventStore, size, state)
    DiscardPile(BoardCardType.Shopping, size, state)
}

@Composable
fun BoxScope.ColorsSelector(
    state: BoardState,
    dispatch: (BoardAction) -> Unit
) {
    val players by players.collectAsState()
    FlowRow(modifier = Modifier.align(Alignment.TopCenter).padding(horizontal = 64.dp)) {
        (pointerColors - players.filter { !it.isCurrentPlayer }
            .map { it.attrs.color }).forEach { color ->
            RadioButton(
                selected = color == state.color,
                onClick = { dispatch(BoardAction.ChangeColor(color)) },
                colors = RadioButtonDefaults.colors()
                    .copy(selectedColor = Color(color), unselectedColor = Color(color)),
            )
        }
    }
}

val Player.isCurrentPlayer: Boolean
    get() = id == currentPlayerId

fun PlaceType.getDpSize(
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

fun PlaceType.dpOffset(
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

fun getLocationOnBoard(
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

val boardLayers = Board(
    layers = mapOf(
        BoardLayer.OUTER to BoardRoute(26, 18, outPlaces),
        BoardLayer.INNER to BoardRoute(28, 18, inPlaces),
    )
)

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
                                    targetValue = 0f,
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
    val store = remember { BoardStore() }
    val state by store.observeState().collectAsState()
    AppTheme {
        BoardScreenContent(state, store::dispatch, 0)
    }
}