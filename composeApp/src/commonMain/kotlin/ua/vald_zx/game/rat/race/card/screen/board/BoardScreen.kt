package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.constraintlayout.compose.ExperimentalMotionApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.composables.core.BottomSheet
import com.composables.core.SheetDetent
import com.composables.core.rememberBottomSheetState
import com.russhwolf.settings.set
import dev.lennartegb.shadows.boxShadow
import io.github.alexzhirkevich.compottie.rememberLottieAnimatable
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.components.SkittlesRainbow
import ua.vald_zx.game.rat.race.card.components.clickableSingle
import ua.vald_zx.game.rat.race.card.currentPlayerId
import ua.vald_zx.game.rat.race.card.logic.BoardState
import ua.vald_zx.game.rat.race.card.logic.BoardUiAction
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.logic.players
import ua.vald_zx.game.rat.race.card.lottieDiceAnimations
import ua.vald_zx.game.rat.race.card.playCoin
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Back
import ua.vald_zx.game.rat.race.card.resource.images.IcDarkMode
import ua.vald_zx.game.rat.race.card.resource.images.IcLightMode
import ua.vald_zx.game.rat.race.card.settings
import ua.vald_zx.game.rat.race.card.shared.*
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

data class BoardLayers(
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
val littleDetailsHeight
    get() = 100.dp + statusBarHeightState.value + navigationBarHeightState.value
var sheetContentSize = mutableStateOf(0.dp)
val HalfExpanded = SheetDetent("hidden") { _, _ ->
    littleDetailsHeight
}
val ContentExpanded = SheetDetent("content") { containerHeight, _ ->
    if (sheetContentSize.value == 0.dp) {
        containerHeight
    } else {
        sheetContentSize.value
    } - statusBarHeightState.value
}

class BoardScreen(
    private val board: Board,
    private val player: Player,
) : Screen {

    override val key: ScreenKey = "Board2Screen"

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val vm = koinViewModel<BoardViewModel>(
            parameters = { parametersOf(board, player) }
        )
        val state by vm.uiState.collectAsState()
        val scaffoldState = rememberBottomSheetState(
            initialDetent = HalfExpanded,
            detents = listOf(HalfExpanded, ContentExpanded)
        )
        val density = LocalDensity.current
        val navigator = LocalNavigator.currentOrThrow
        Box {
            BottomSheetNavigator {
                Box(modifier = Modifier.padding(bottom = littleDetailsHeight)) {
                    BoardScreenContent(state, vm)
                }
                BottomSheet(state = scaffoldState) {
                    Box(Modifier.navigationBarsPadding().onSizeChanged { size ->
                        sheetContentSize.value = with(density) { size.height.toDp() }
                        scaffoldState.invalidateDetents()
                    }) {
                        Board2PlayerDetailsScreen(state, scaffoldState)
                    }
                }
            }
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
                        with(density) {
                            navigationBarHeightState.value = it.size.height.toDp()
                            scaffoldState.invalidateDetents()
                        }
                    }
            ) {
                Box(modifier = Modifier.navigationBarsPadding())
            }
            Box(modifier = Modifier.fillMaxWidth().onGloballyPositioned {
                with(density) {
                    statusBarHeightState.value = it.size.height.toDp()
                    scaffoldState.invalidateDetents()
                }
            }) {
                Box(modifier = Modifier.statusBarsPadding())
            }
            IconButton(
                modifier = Modifier.align(Alignment.TopStart),
                onClick = { navigator.pop() },
                content = {
                    Icon(Images.Back, contentDescription = null)
                }
            )
        }

        var confirmDismissalDialog: Business? by remember { mutableStateOf(null) }
        var confirmSellingAllBusinessDialog: Business? by remember { mutableStateOf(null) }
        var bankruptBusinessDialog: Business? by remember { mutableStateOf(null) }
        var congratulationsWithBabyDialog by remember { mutableStateOf(false) }
        var congratulationsWithMarriageDialog by remember { mutableStateOf(false) }
        var playerDivorcedDialog: String? by remember { mutableStateOf(null) }
        var playerHadBabyDialog: Pair<String, Long>? by remember { mutableStateOf(null) }
        var playerMarriedDialog: String? by remember { mutableStateOf(null) }
        var youDivorcedDialog by remember { mutableStateOf(false) }
        var resignationDialog: Business? by remember { mutableStateOf(null) }
        var depositWithdrawDialog by remember { mutableStateOf(0L) }
        var loanAddedDialog by remember { mutableStateOf(0L) }
        var receivedCashDialog by remember { mutableStateOf(0L) }
        LaunchedEffect(Unit) {
            vm.init()
            vm.actions.collect { event ->
                when (event) {
                    is BoardUiAction.ConfirmDismissal -> {
                        confirmDismissalDialog = event.business
                    }

                    is BoardUiAction.ConfirmSellingAllBusiness -> {
                        confirmSellingAllBusinessDialog = event.business
                    }

                    is BoardUiAction.DepositWithdraw -> {
                        depositWithdrawDialog = event.balance
                    }

                    is BoardUiAction.LoanAdded -> {
                        loanAddedDialog = event.balance
                    }

                    is BoardUiAction.ReceivedCash -> {
                        receivedCashDialog = event.amount
                    }

                    is BoardUiAction.AddCash -> {
                        playCoin()
                    }

                    is BoardUiAction.SubCash -> {
                        playCoin()
                    }

                    is BoardUiAction.BankruptBusiness -> {
                        bankruptBusinessDialog = event.business
                    }

                    BoardUiAction.CongratulationsWithBaby -> {
                        congratulationsWithBabyDialog = true
                    }

                    BoardUiAction.CongratulationsWithMarriage -> {
                        congratulationsWithMarriageDialog = true
                    }

                    BoardUiAction.YouDivorced -> {
                        youDivorcedDialog = true
                    }

                    is BoardUiAction.PlayerDivorced -> {
                        playerDivorcedDialog = event.playerName
                    }

                    is BoardUiAction.PlayerHadBaby -> {
                        playerHadBabyDialog = event.playerName to event.babies
                    }

                    is BoardUiAction.PlayerMarried -> {
                        playerMarriedDialog = event.playerName
                    }

                    is BoardUiAction.Resignation -> {
                        resignationDialog = event.business
                    }
                }
            }
        }
        confirmDismissalDialog?.let { business ->
            AlertDialog(
                title = { Text(text = stringResource(Res.string.fire_from_job)) },
                text = {
                    Text(
                        text = stringResource(
                            Res.string.lose_job_on_second_business_with_salary,
                            state.player.card.salary.toString()
                        )
                    )
                },
                onDismissRequest = { confirmDismissalDialog = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            vm.dismissalConfirmed(business)
                            confirmDismissalDialog = null
                        }
                    ) { Text(stringResource(Res.string.resign)) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        vm.pass()
                        confirmDismissalDialog = null
                    }) { Text(stringResource(Res.string.cancel)) }
                }
            )
        }
        confirmSellingAllBusinessDialog?.let { business ->
            AlertDialog(
                title = { Text(text = stringResource(Res.string.buy_business_title)) },
                text = {
                    Text(
                        text = stringResource(
                            Res.string.need_sell_all_businesses_with_sum,
                            state.player.businesses.sumOf { it.price }.toString()
                        )
                    )
                },
                onDismissRequest = { confirmSellingAllBusinessDialog = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            vm.sellingAllBusinessConfirmed(business)
                            confirmSellingAllBusinessDialog = null
                        }
                    ) { Text(stringResource(Res.string.buy)) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        vm.pass()
                        confirmSellingAllBusinessDialog = null
                    }) { Text(stringResource(Res.string.cancel)) }
                }
            )
        }


        if (depositWithdrawDialog != 0L) {
            AlertDialog(
                title = { Text(text = stringResource(Res.string.attention)) },
                text = {
                    Text(
                        text = stringResource(
                            Res.string.not_enough_cash_taken_from_deposit,
                            depositWithdrawDialog.toString()
                        )
                    )
                },
                onDismissRequest = { depositWithdrawDialog = 0 },
                confirmButton = {
                    TextButton(
                        onClick = {
                            depositWithdrawDialog = 0
                        }
                    ) { Text(stringResource(Res.string.ok)) }
                },
            )
        }
        if (loanAddedDialog != 0L) {
            AlertDialog(
                title = { Text(text = stringResource(Res.string.attention)) },
                text = {
                    Text(
                        text = stringResource(Res.string.not_enough_cash_loan_taken, loanAddedDialog.toString())
                    )
                },
                onDismissRequest = { loanAddedDialog = 0 },
                confirmButton = {
                    TextButton(
                        onClick = {
                            loanAddedDialog = 0
                        }
                    ) { Text(stringResource(Res.string.ok)) }
                },
            )
        }
        if (receivedCashDialog != 0L) {
            AlertDialog(
                text = {
                    Text(
                        text = stringResource(Res.string.cash_received_amount, receivedCashDialog.toString())
                    )
                },
                onDismissRequest = { receivedCashDialog = 0 },
                confirmButton = {
                    TextButton(
                        onClick = {
                            receivedCashDialog = 0
                        }
                    ) { Text(stringResource(Res.string.great)) }
                },
            )
        }
        bankruptBusinessDialog?.let { business ->
            AlertDialog(
                text = {
                    Text(
                        text = stringResource(Res.string.business_bankruptcy, business.name, business.profit.toString())
                    )
                },
                onDismissRequest = { bankruptBusinessDialog = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            bankruptBusinessDialog = null
                        }
                    ) { Text(stringResource(Res.string.ok)) }
                },
            )
        }
        if (congratulationsWithBabyDialog) {
            AlertDialog(
                title = { Text(text = stringResource(Res.string.сongratulations)) },
                text = {
                    Text(text = stringResource(Res.string.congratulationsWithBaby))
                },
                onDismissRequest = { congratulationsWithBabyDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            congratulationsWithBabyDialog = false
                        }
                    ) { Text(stringResource(Res.string.great)) }
                },
            )
        }
        if (congratulationsWithMarriageDialog) {
            AlertDialog(
                title = { Text(text = stringResource(Res.string.сongratulations)) },
                text = {
                    Text(text = stringResource(Res.string.congratulationsWithMarriage))
                },
                onDismissRequest = { congratulationsWithMarriageDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            congratulationsWithMarriageDialog = false
                        }
                    ) { Text(stringResource(Res.string.great)) }
                },
            )
        }
        if (youDivorcedDialog) {
            AlertDialog(
                text = {
                    Text(text = stringResource(Res.string.youDivorced))
                },
                onDismissRequest = { youDivorcedDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            youDivorcedDialog = false
                        }
                    ) { Text(stringResource(Res.string.ok)) }
                },
            )
        }
        playerDivorcedDialog?.let { playerName ->
            AlertDialog(
                text = {
                    Text(stringResource(Res.string.playerDivorced, playerName))
                },
                onDismissRequest = { playerDivorcedDialog = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            playerDivorcedDialog = null
                        }
                    ) { Text(stringResource(Res.string.ok)) }
                },
            )
        }
        playerHadBabyDialog?.let { (playerName, babies) ->
            AlertDialog(
                title = { Text(text = stringResource(Res.string.kids)) },
                text = {
                    Text(stringResource(Res.string.playerHadBaby, playerName, babies))
                },
                onDismissRequest = { playerHadBabyDialog = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            playerHadBabyDialog = null
                        }
                    ) { Text(stringResource(Res.string.ok)) }
                },
            )
        }
        playerMarriedDialog?.let { playerName ->
            AlertDialog(
                title = { Text(text = stringResource(Res.string.marriage)) },
                text = {
                    Text(stringResource(Res.string.playerMarried, playerName))
                },
                onDismissRequest = { playerMarriedDialog = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            playerMarriedDialog = null
                        }
                    ) { Text(stringResource(Res.string.ok)) }
                },
            )
        }
        resignationDialog?.let { business ->
            AlertDialog(
                text = {
                    Text(stringResource(Res.string.resignation, business.profit))
                },
                onDismissRequest = { resignationDialog = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            resignationDialog = null
                        }
                    ) { Text(stringResource(Res.string.ok)) }
                },
            )
        }
    }
}

@OptIn(ExperimentalMotionApi::class, ExperimentalMaterialApi::class)
@Composable
fun BoardScreenContent(
    state: BoardState,
    vm: BoardViewModel
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        BoardFragment(state, vm)
        Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Controls()
        }
        CardDialog(state, vm)
    }
}

@Composable
fun BoardFragment(
    state: BoardState,
    vm: BoardViewModel,
) {
    val rotX = remember { Animatable(0f) }
    val rotY = remember { Animatable(0f) }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .rotateOnDrag(rotX, rotY)
    ) {
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
            BoardPanel(state, isVertical, vm)
            if (isFlipped(rotY, rotX)) {
                BackSide()
            }
            Dice(state, vm)
        }
    }
}

@Composable
fun BoxScope.Controls() {
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
}

@Composable
fun BoxWithConstraintsScope.Dice(
    state: BoardState,
    vm: BoardViewModel,
) {
    val composition = lottieDiceAnimations[state.board.dice]
    val animatable = rememberLottieAnimatable()
    LaunchedEffect(Unit) {
        animatable.snapTo(composition, progress = 1f)
    }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(state.board.dice, state.board.diceRolling) {
        if (state.board.diceRolling) {
            coroutineScope.launch {
                animatable.animate(composition, iterations = 1, initialProgress = 0f)
            }
        }
    }
    LaunchedEffect(animatable.isAtEnd) {
        if (animatable.isAtEnd && currentPlayerId == state.board.activePlayer) {
            vm.move()
        }
    }
    val size = min(maxWidth, maxHeight) / 6
    val infiniteTransition = rememberInfiniteTransition(label = "InfiniteTransition")
    val spreadRadius by infiniteTransition.animateValue(
        initialValue = 0.dp,
        targetValue = size * 0.6f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "FloatAnimation",
        typeConverter = TwoWayConverter({ AnimationVector(it.value) }, { it.value.dp })
    )
    Box(modifier = Modifier.align(Alignment.Center), contentAlignment = Alignment.Center) {
        if (state.canRoll) {
            Box(
                modifier = Modifier
                    .size(size * 0.2f)
                    .padding(top = size * 0.3f)
                    .boxShadow(
                        blurRadius = size * 0.6f,
                        spreadRadius = spreadRadius,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.background,
                    )
            )
        }
        Image(
            painter = rememberLottiePainter(
                composition = composition,
                progress = animatable::value
            ),
            contentDescription = "Lottie animation",
            modifier = Modifier
                .size(size)
                .clickableSingle(enabled = state.canRoll) { vm.rollDice() }
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
fun BoxWithConstraintsScope.BoardPanel(
    state: BoardState,
    isVertical: Boolean,
    vm: BoardViewModel,
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
                        0.0f to Color(0xFFD7C228),
                        0.4f to Color(0xFFF8C954),
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
            vm = vm,
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
            vm = vm,
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
            highlightedDeck = state.board.canTakeCard,
            vm = vm,
        )
    }
}

@Composable
fun BoxScope.CardDecks(
    size: DpSize,
    highlightedDeck: BoardCardType?,
    vm: BoardViewModel,
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
                    LeftCardDecks(highlightedDeck, cardSize, vm = vm, state = state)
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
                    RightCardDecks(highlightedDeck, cardSize, vm = vm, state = state)
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
                    LeftCardDecks(highlightedDeck, cardSize, vm = vm, state = state)
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
                    RightCardDecks(highlightedDeck, cardSize, vm = vm, state = state)
                }
            }
        }
    }
}

@Composable
fun LeftCardDecks(
    highlightedCard: BoardCardType?,
    size: DpSize,
    vm: BoardViewModel,
    state: BoardState
) {
    CardDeck(BoardCardType.Chance, size, highlightedCard, state, vm)
    CardDeck(BoardCardType.BigBusiness, size, highlightedCard, state, vm)
    CardDeck(BoardCardType.MediumBusiness, size, highlightedCard, state, vm)
    CardDeck(BoardCardType.SmallBusiness, size, highlightedCard, state, vm)
}

@Composable
fun RightCardDecks(
    highlightedCard: BoardCardType?,
    size: DpSize,
    vm: BoardViewModel,
    state: BoardState
) {
    CardDeck(BoardCardType.Expenses, size, highlightedCard, state, vm)
    CardDeck(BoardCardType.Deputy, size, highlightedCard, state, vm)
    CardDeck(BoardCardType.EventStore, size, highlightedCard, state, vm)
    CardDeck(BoardCardType.Shopping, size, highlightedCard, state, vm)
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
    colorState: MutableState<Long>,
) {
    val players by players.collectAsState()
    FlowRow(modifier = Modifier.align(Alignment.TopCenter).padding(horizontal = 64.dp)) {
        val colors = pointerColors - players.filter { !it.isCurrentPlayer }
            .map { it.attrs.color }.toSet()
        LaunchedEffect(colors) {
            if (!colors.contains(colorState.value)) {
                colorState.value = colors.first()
            }
        }
        colors.forEach { color ->
            RadioButton(
                selected = color == colorState.value,
                onClick = {
                    colorState.value = color
                },
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

val boardLayers = BoardLayers(
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