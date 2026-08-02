package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.*
import androidx.constraintlayout.compose.ExperimentalMotionApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.composables.core.BottomSheet
import com.composables.core.SheetDetent
import com.composables.core.rememberBottomSheetState
import dev.lennartegb.shadows.boxShadow
import io.github.alexzhirkevich.compottie.rememberLottieAnimatable
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.appKStore
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.screen.design.DesignDreamDialog
import ua.vald_zx.game.rat.race.card.screen.design.DesignPlayerSheet
import ua.vald_zx.game.rat.race.card.components.SkittlesRainbow
import ua.vald_zx.game.rat.race.card.components.clickableSingle
import ua.vald_zx.game.rat.race.card.logic.BoardUiAction
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.logic.players
import ua.vald_zx.game.rat.race.card.lottieDiceAnimations
import ua.vald_zx.game.rat.race.card.playCoin
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.ArrowUp
import ua.vald_zx.game.rat.race.card.resource.images.Back
import ua.vald_zx.game.rat.race.card.resource.images.IcDarkMode
import ua.vald_zx.game.rat.race.card.resource.images.IcLightMode
import ua.vald_zx.game.rat.race.card.screen.BoardListScreen
import ua.vald_zx.game.rat.race.card.screen.LoadOnlineScreen
import ua.vald_zx.game.rat.race.card.screen.board.deck.CardDialog
import ua.vald_zx.game.rat.race.card.shared.*
import ua.vald_zx.game.rat.race.card.splitDecimal
import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark
import kotlin.math.absoluteValue

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
                    BoardScreenContent(vm)
                }
                BottomSheet(state = scaffoldState) {
                    Box(Modifier.navigationBarsPadding().onSizeChanged { size ->
                        sheetContentSize.value = with(density) { size.height.toDp() }
                        scaffoldState.invalidateDetents()
                    }) {
                        if (designV2Enabled.value) {
                            DesignPlayerSheet(vm, scaffoldState)
                        } else {
                            LegacyPlayerSheet(vm, scaffoldState)
                        }
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
                modifier = Modifier.align(Alignment.TopStart).statusBarsPadding(),
                onClick = { navigator.popUntil { screen ->
                    screen is BoardListScreen
                } },
                content = {
                    Icon(Images.Back, contentDescription = null)
                }
            )
        }

        var confirmDismissalDialog: Business? by remember { mutableStateOf(null) }
        var firedDialog: Business? by remember { mutableStateOf(null) }
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
        var investmentResultDialog: Pair<Int, Long>? by remember { mutableStateOf(null) }
        var capitalizedDialog by remember { mutableStateOf(0L) }
        var loanAddedDialog by remember { mutableStateOf(0L) }
        var simpleDialog by remember { mutableStateOf(Res.string.app_name) }
        var loanOverlimitedDialog by remember { mutableStateOf(false) }
        var receivedCashDialog by remember { mutableStateOf<BoardUiAction.ReceivedCash?>(null) }
        var dreamOfferedDialog by remember { mutableStateOf(false) }
        var victoryDialog by remember { mutableStateOf<BoardUiAction.PlayerWon?>(null) }
        LaunchedEffect(state.canBuyDream) {
            if (state.canBuyDream) {
                dreamOfferedDialog = true
            }
        }
        LaunchedEffect(Unit) {
            vm.init(player)
            vm.actions.collect { event ->
                when (event) {
                    is BoardUiAction.ConfirmDismissal -> {
                        confirmDismissalDialog = event.business
                    }

                    is BoardUiAction.Fired -> {
                        firedDialog = event.business
                    }

                    is BoardUiAction.ConfirmSellingAllBusiness -> {
                        confirmSellingAllBusinessDialog = event.business
                    }

                    is BoardUiAction.HighRiskPlayed -> {
                        investmentResultDialog = event.outcome.dice to event.outcome.payout
                            .let { if (it > 0) it else -event.outcome.stake }
                    }

                    is BoardUiAction.MediumRiskPlayed -> {
                        investmentResultDialog = event.outcome.dice to event.outcome.payout
                            .let { if (it > 0) it else -event.outcome.stake }
                    }

                    is BoardUiAction.FundsCapitalized -> {
                        capitalizedDialog = event.profit
                    }

                    is BoardUiAction.DepositWithdraw -> {
                        depositWithdrawDialog = event.balance
                    }

                    is BoardUiAction.LoanAdded -> {
                        loanAddedDialog = event.balance
                    }

                    is BoardUiAction.ReceivedCash -> {
                        receivedCashDialog = event
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

                    BoardUiAction.LoanOverlimited -> {
                        loanOverlimitedDialog = true
                    }

                    BoardUiAction.ConnectionLost -> {
                        navigator.push(LoadOnlineScreen())
                    }

                    BoardUiAction.BidBusinessAuctionSuccessBuy -> {
                        simpleDialog = Res.string.bidBusinessAuctionSuccessBuy
                    }

                    BoardUiAction.BidEstateAuctionSuccessBuy -> {
                        simpleDialog = Res.string.bidEstateAuctionSuccessBuy
                    }

                    BoardUiAction.BidLandAuctionSuccessBuy -> {
                        simpleDialog = Res.string.bidLandAuctionSuccessBuy
                    }

                    BoardUiAction.BidSharesAuctionSuccessBuy -> {
                        simpleDialog = Res.string.bidSharesAuctionSuccessBuy
                    }

                    BoardUiAction.DreamOffered -> {
                        dreamOfferedDialog = true
                    }

                    is BoardUiAction.PlayerWon -> {
                        victoryDialog = event
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
        firedDialog?.let { business ->
            AlertDialog(
                title = { Text(text = stringResource(Res.string.fire_from_job)) },
                text = {
                    Text(
                        text = stringResource(
                            Res.string.lose_job_on_second_business_with_salary,
                            business.profit.toString()
                        )
                    )
                },
                onDismissRequest = { confirmDismissalDialog = null },
                confirmButton = {
                    TextButton(onClick = { confirmDismissalDialog = null }) {
                        Text(stringResource(Res.string.ok))
                    }
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

        if (simpleDialog != Res.string.app_name) {
            AlertDialog(
                text = { Text(text = stringResource(simpleDialog)) },
                onDismissRequest = { simpleDialog = Res.string.app_name },
                confirmButton = {
                    TextButton(onClick = { simpleDialog = Res.string.app_name }) {
                        Text(stringResource(Res.string.ok))
                    }
                },
            )
        }
        investmentResultDialog?.let { (dice, amount) ->
            AlertDialog(
                title = { Text(text = stringResource(Res.string.investments)) },
                text = {
                    Text(
                        text = if (amount > 0) {
                            stringResource(Res.string.investment_win, dice.toString(), amount.splitDecimal())
                        } else {
                            stringResource(Res.string.investment_lose, dice.toString(), (-amount).splitDecimal())
                        }
                    )
                },
                onDismissRequest = { investmentResultDialog = null },
                confirmButton = {
                    TextButton(onClick = { investmentResultDialog = null }) {
                        Text(stringResource(Res.string.ok))
                    }
                },
            )
        }
        if (capitalizedDialog != 0L) {
            AlertDialog(
                title = { Text(text = stringResource(Res.string.investments)) },
                text = {
                    Text(text = stringResource(Res.string.funds_capitalized, capitalizedDialog.splitDecimal()))
                },
                onDismissRequest = { capitalizedDialog = 0 },
                confirmButton = {
                    TextButton(onClick = { capitalizedDialog = 0 }) {
                        Text(stringResource(Res.string.ok))
                    }
                },
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
        if (loanOverlimitedDialog) {
            AlertDialog(
                title = { Text(text = stringResource(Res.string.attention)) },
                text = {
                    Text(
                        text = stringResource(Res.string.limitOverload, state.board.loanLimit)
                    )
                },
                onDismissRequest = { loanOverlimitedDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            navigator.popUntilRoot()
                            navigator.push(InitPlayerScreen(board))
                            loanOverlimitedDialog = false
                        }
                    ) { Text(stringResource(Res.string.yes)) }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            navigator.popUntilRoot()
                            MainScope().launch {
                                appKStore.update {
                                    it?.copy(clientUuid = "")
                                }
                            }
                            loanOverlimitedDialog = false
                        }
                    ) { Text(stringResource(Res.string.no)) }
                }
            )
        }
        val receivedCash = receivedCashDialog
        if (receivedCash != null) {
            val player = players.collectAsState().value.find { it.id == receivedCash.receiverId }
            AlertDialog(
                text = {
                    Text(
                        text = stringResource(
                            Res.string.cash_received_amount,
                            player?.card?.name ?: "Incognito",
                            receivedCash.amount
                        )
                    )
                },
                onDismissRequest = { receivedCashDialog = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            receivedCashDialog = null
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
        if (dreamOfferedDialog) {
            val dream = state.currentDream
            val canPay = dream != null && state.canPay(dream.price)
            if (designV2Enabled.value) {
                DesignDreamDialog(
                    dream = dream,
                    canPay = canPay,
                    onBuy = {
                        vm.buyDream()
                        dreamOfferedDialog = false
                    },
                    onPass = {
                        vm.pass()
                        dreamOfferedDialog = false
                    },
                )
            } else {
                LegacyDreamDialog(
                    vm = vm,
                    dream = dream,
                    canPay = canPay,
                    onDone = { dreamOfferedDialog = false },
                )
            }
        }
        victoryDialog?.let { victory ->
            AlertDialog(
                title = { Text(stringResource(Res.string.victory)) },
                text = {
                    Text(
                        if (victory.isCurrentPlayer) {
                            stringResource(Res.string.you_won)
                        } else {
                            stringResource(Res.string.player_won, victory.playerName)
                        }
                    )
                },
                onDismissRequest = { victoryDialog = null },
                confirmButton = {
                    TextButton(onClick = { victoryDialog = null }) {
                        Text(stringResource(Res.string.great))
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMotionApi::class, ExperimentalMaterialApi::class)
@Composable
fun BoardScreenContent(vm: BoardViewModel) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        BoardFragment(vm)
        Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Controls(vm)
        }
        CardDialog(vm)
    }
}

@Composable
fun BoardFragment(vm: BoardViewModel) {
    val rotX = remember { Animatable(0f) }
    val rotY = remember { Animatable(0f) }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .rotateOnDrag(rotX, rotY)
    ) {
        val state by vm.uiState.collectAsState()
        val isVertical = maxHeight > maxWidth
        val scale by animateFloatAsState(if (state.layer == BoardLayer.INNER) INNER_LAYER_SCALE else 1.0f)
        BoxWithConstraints(
            modifier = Modifier
                .padding(32.dp)
                .align(Alignment.Center)
                .fitBoardFrame(maxWidth, maxHeight, isVertical)
                .graphicsLayer {
                    rotationX = (-rotX.value).coerceIn(-180f, 180f)
                    rotationY = rotY.value.coerceIn(-180f, 180f)
                    cameraDistance = 25f
                    shape = RoundedCornerShape(6.dp)
                    scaleX = scale
                    scaleY = scale
                }
        ) {
            BoardPanel(isVertical, vm)
            if (isFlipped(rotY, rotX)) {
                BackSide()
            }
            Dice(vm)
        }
    }
}

@Composable
fun BoxScope.Controls(vm: BoardViewModel) {
    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    val state by vm.uiState.collectAsState()
    Column(
        modifier = Modifier.align(Alignment.TopEnd),
        horizontalAlignment = Alignment.End,
    ) {
        if (state.currentPlayerIsActive) {
            TextButton(onClick = {
                bottomSheetNavigator.show(DebugScreen(vm))
            }) { Text(stringResource(Res.string.debug_tools)) }
        }
    }
}

@Composable
fun BoxWithConstraintsScope.Dice(vm: BoardViewModel) {
    val state by vm.uiState.collectAsState()
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
    val rollSize = min(maxWidth, maxHeight) / 7
    val infiniteTransition = rememberInfiniteTransition(label = "InfiniteTransition")
    val spreadRadius by infiniteTransition.animateValue(
        initialValue = 0.dp,
        targetValue = rollSize * 0.3f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "FloatAnimation",
        typeConverter = TwoWayConverter({ AnimationVector(it.value) }, { it.value.dp })
    )
    val arrowSize = rollSize * 0.7f
    Row(
        modifier = Modifier
            .align(Alignment.Center),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(rollSize),
            contentAlignment = Alignment.Center,
        ) {
            if (state.canRoll) {
                Box(
                    modifier = Modifier
                        .size(rollSize * 0.2f)
                        .padding(top = rollSize * 0.3f)
                        .boxShadow(
                            blurRadius = rollSize * 0.3f,
                            spreadRadius = spreadRadius,
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onBackground,
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
                    .size(rollSize)
                    .clickableSingle(enabled = state.canRoll) { vm.rollDice() }
            )
        }
        if (state.canEnterOuterCircle) {
            RainbowOuterCircleButton(
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .size(arrowSize),
                enabled = !state.isProgress,
                onClick = { vm.enterOuterCircle() },
                contentDescription = stringResource(Res.string.enter_outer_circle),
            )
        }
    }

}

@Composable
private fun RainbowOuterCircleButton(
    modifier: Modifier,
    enabled: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "OuterCircleArrow")
    val gradientOffset by transition.animateFloat(
        initialValue = -100f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "OuterCircleArrowGradient",
    )
    val arrow = rememberVectorPainter(Images.ArrowUp)
    Canvas(
        modifier = modifier
            .graphicsLayer {
                alpha = if (enabled) 1f else 0.38f
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .clickableSingle(
                enabled = enabled,
                onClickLabel = contentDescription,
                role = Role.Button,
                onClick = onClick,
            )
    ) {
        with(arrow) { draw(size) }
        val unit = size.width / 100f
        drawRect(
            brush = Brush.linearGradient(
                colors = SkittlesRainbow,
                start = Offset(gradientOffset * unit, 0f),
                end = Offset((gradientOffset + 100f) * unit, size.height),
                tileMode = TileMode.Repeated,
            ),
            blendMode = BlendMode.SrcIn,
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
fun BoxScope.ColorsSelector(
    colorState: MutableState<Long>,
) {
    FlowRow(modifier = Modifier.align(Alignment.TopCenter).padding(horizontal = 64.dp)) {
        val colors = pointerColors
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
