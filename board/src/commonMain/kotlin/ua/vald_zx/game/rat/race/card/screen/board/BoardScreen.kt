package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.composables.core.BottomSheet
import com.composables.core.BottomSheetState
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
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.design.DesignMessageDialog
import ua.vald_zx.game.rat.race.card.design.DesignShapes
import ua.vald_zx.game.rat.race.card.screen.design.DesignActiveWaves
import ua.vald_zx.game.rat.race.card.screen.design.DesignDreamDetailsDialog
import ua.vald_zx.game.rat.race.card.screen.design.DesignDreamDialog
import ua.vald_zx.game.rat.race.card.screen.design.DesignPlayerSheet
import ua.vald_zx.game.rat.race.card.screen.design.DesignBoardOverlay
import ua.vald_zx.game.rat.race.card.screen.design.rememberCellFocus
import ua.vald_zx.game.rat.race.card.screen.design.rememberTokenBubbleState
import ua.vald_zx.game.rat.race.card.components.SkittlesRainbow
import ua.vald_zx.game.rat.race.card.components.NavigationBackButton
import ua.vald_zx.game.rat.race.card.components.EButton
import ua.vald_zx.game.rat.race.card.components.clickableSingle
import ua.vald_zx.game.rat.race.card.logic.BoardUiAction
import ua.vald_zx.game.rat.race.card.logic.BoardConnectionState
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.logic.players
import ua.vald_zx.game.rat.race.card.GameSound
import ua.vald_zx.game.rat.race.card.lottieDiceAnimations
import ua.vald_zx.game.rat.race.card.play
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.ArrowUp
import ua.vald_zx.game.rat.race.card.resource.images.IcDarkMode
import ua.vald_zx.game.rat.race.card.resource.images.IcLightMode
import ua.vald_zx.game.rat.race.card.screen.BoardListScreen
import ua.vald_zx.game.rat.race.card.screen.board.deck.CardDialog
import ua.vald_zx.game.rat.race.card.shared.*
import ua.vald_zx.game.rat.race.card.splitDecimal
import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark
import kotlin.math.absoluteValue
import androidx.compose.ui.text.intl.Locale

val navigationBarHeightState = mutableStateOf(0.dp)
val statusBarHeightState = mutableStateOf(0.dp)
val deckCoordinatesMap = mutableMapOf<BoardCardType, MutableState<Pair<DpOffset, DpSize>>>()
val discardPilesCoordinatesMap = mutableMapOf<BoardCardType, MutableState<Pair<DpOffset, DpSize>>>()
val collapsedSheetContentHeight = 140.dp
val collapsedSheetHandleHeight = 52.dp
val littleDetailsHeight
    get() = collapsedSheetContentHeight + navigationBarHeightState.value
var sheetContentSize = mutableStateOf(0.dp)
val HalfExpanded = SheetDetent("hidden") { _, _ ->
    littleDetailsHeight
}
val ContentExpanded = SheetDetent("content") { containerHeight, _ ->
    val content = sheetContentSize.value.takeIf { it > 0.dp } ?: containerHeight
    minOf(content, containerHeight - statusBarHeightState.value)
}

@Composable
fun PlayerSheetContainer(
    scaffoldState: BottomSheetState,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    BoxWithConstraints {
        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .heightIn(max = maxHeight - statusBarHeightState.value)
                .onSizeChanged { size ->
                    sheetContentSize.value = with(density) { size.height.toDp() }
                    scaffoldState.invalidateDetents()
                }
        ) {
            content()
        }
    }
}

class BoardScreen(
    private val board: Board,
    private val player: Player,
) : Screen {

    override val key: ScreenKey = "Board2Screen"
    private val exitRequested = mutableStateOf(false)

    fun requestExit() {
        exitRequested.value = true
    }

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
                    PlayerSheetContainer(scaffoldState) {
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
            val reconnecting = state.connectionState as? BoardConnectionState.Reconnecting
            if (reconnecting != null) {
                val designColors = Design.colors
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(designColors.scaffold.shadow.copy(alpha = 0.62f))
                        .clickable(onClick = {}),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .widthIn(max = 380.dp)
                            .clip(DesignShapes.dialog)
                            .background(designColors.scaffold.surface1)
                            .border(1.dp, designColors.scaffold.outline, DesignShapes.dialog)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(color = designColors.scaffold.accent)
                        Text(
                            text = stringResource(
                                Res.string.reconnecting_to_server,
                                reconnecting.attempt,
                            ),
                            style = Design.type.subtitle,
                            color = designColors.scaffold.onSurface,
                        )
                        Text(
                            text = stringResource(Res.string.reconnecting_to_server_hint),
                            style = Design.type.body,
                            color = designColors.scaffold.onSurfaceMuted,
                        )
                    }
                }
            }
            NavigationBackButton(
                modifier = Modifier.align(Alignment.TopStart).statusBarsPadding(),
                onClick = ::requestExit,
            )
        }

        if (exitRequested.value) {
            DesignMessageDialog(
                title = stringResource(Res.string.confirm_leave_board),
                message = stringResource(Res.string.leave_board_warning),
                onDismissRequest = { exitRequested.value = false },
                confirmLabel = stringResource(Res.string.exit),
                onConfirm = {
                    exitRequested.value = false
                    navigator.returnToBoardList()
                },
                dismissLabel = stringResource(Res.string.cancel),
                onDismissAction = { exitRequested.value = false },
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
        var taxInspectionBribeDialog by remember { mutableStateOf(0L) }
        var loanAddedDialog by remember { mutableStateOf(0L) }
        var simpleDialog by remember { mutableStateOf(Res.string.app_name) }
        var loanOverlimitedDialog by remember { mutableStateOf(false) }
        var receivedCashDialog by remember { mutableStateOf<BoardUiAction.ReceivedCash?>(null) }
        var dreamOfferedDialog by remember { mutableStateOf(false) }
        var victoryDialog by remember { mutableStateOf<BoardUiAction.PlayerWon?>(null) }
        var serverRequestFailedDialog by remember { mutableStateOf(false) }
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

                    is BoardUiAction.TaxInspectionPaid -> {
                        taxInspectionBribeDialog = event.amount
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

                    is BoardUiAction.AddCash -> Unit

                    is BoardUiAction.SubCash -> Unit

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

                    BoardUiAction.ServerRequestFailed -> {
                        serverRequestFailedDialog = true
                    }
                }
            }
        }
        if (serverRequestFailedDialog) {
            DesignMessageDialog(
                onDismissRequest = { serverRequestFailedDialog = false },
                title = stringResource(Res.string.connection_failed),
                message = stringResource(Res.string.server_request_failed),
                confirmLabel = stringResource(Res.string.ok),
                onConfirm = { serverRequestFailedDialog = false },
            )
        }
        if (taxInspectionBribeDialog > 0) {
            DesignMessageDialog(
                onDismissRequest = { taxInspectionBribeDialog = 0 },
                title = stringResource(Res.string.tax_inspection),
                message = stringResource(
                    Res.string.tax_inspection_bribe,
                    taxInspectionBribeDialog.splitDecimal(),
                ),
                confirmLabel = stringResource(Res.string.ok),
                onConfirm = { taxInspectionBribeDialog = 0 },
            )
        }
        confirmDismissalDialog?.let { business ->
            DesignMessageDialog(
                title = stringResource(Res.string.fire_from_job),
                message = stringResource(
                    Res.string.lose_job_on_second_business_with_salary,
                    state.player.card.salary.toString(),
                ),
                onDismissRequest = { confirmDismissalDialog = null },
                confirmLabel = stringResource(Res.string.resign),
                onConfirm = {
                    vm.dismissalConfirmed(business)
                    confirmDismissalDialog = null
                },
                dismissLabel = stringResource(Res.string.cancel),
                onDismissAction = {
                    vm.pass()
                    confirmDismissalDialog = null
                },
            )
        }
        firedDialog?.let { business ->
            DesignMessageDialog(
                title = stringResource(Res.string.fire_from_job),
                message = stringResource(
                    Res.string.lose_job_on_second_business_with_salary,
                    business.profit.toString(),
                ),
                onDismissRequest = { firedDialog = null },
                confirmLabel = stringResource(Res.string.ok),
                onConfirm = { firedDialog = null },
            )
        }
        confirmSellingAllBusinessDialog?.let { business ->
            DesignMessageDialog(
                title = stringResource(Res.string.buy_business_title),
                message = stringResource(
                    Res.string.need_sell_all_businesses_with_sum,
                    state.player.businesses.sumOf { it.price }.toString(),
                ),
                onDismissRequest = { confirmSellingAllBusinessDialog = null },
                confirmLabel = stringResource(Res.string.buy),
                onConfirm = {
                    vm.sellingAllBusinessConfirmed(business)
                    confirmSellingAllBusinessDialog = null
                },
                dismissLabel = stringResource(Res.string.cancel),
                onDismissAction = {
                    vm.pass()
                    confirmSellingAllBusinessDialog = null
                },
            )
        }

        if (simpleDialog != Res.string.app_name) {
            DesignMessageDialog(
                message = stringResource(simpleDialog),
                onDismissRequest = { simpleDialog = Res.string.app_name },
                confirmLabel = stringResource(Res.string.ok),
                onConfirm = { simpleDialog = Res.string.app_name },
            )
        }
        investmentResultDialog?.let { (dice, amount) ->
            DesignMessageDialog(
                title = stringResource(Res.string.investments),
                message = if (amount > 0) {
                    stringResource(Res.string.investment_win, dice.toString(), amount.splitDecimal())
                } else {
                    stringResource(Res.string.investment_lose, dice.toString(), (-amount).splitDecimal())
                },
                onDismissRequest = { investmentResultDialog = null },
                confirmLabel = stringResource(Res.string.ok),
                onConfirm = { investmentResultDialog = null },
            )
        }
        if (capitalizedDialog != 0L) {
            DesignMessageDialog(
                title = stringResource(Res.string.investments),
                message = stringResource(Res.string.funds_capitalized, capitalizedDialog.splitDecimal()),
                onDismissRequest = { capitalizedDialog = 0 },
                confirmLabel = stringResource(Res.string.ok),
                onConfirm = { capitalizedDialog = 0 },
            )
        }
        if (depositWithdrawDialog != 0L) {
            DesignMessageDialog(
                title = stringResource(Res.string.attention),
                message = stringResource(
                    Res.string.not_enough_cash_taken_from_deposit,
                    depositWithdrawDialog.toString(),
                ),
                onDismissRequest = { depositWithdrawDialog = 0 },
                confirmLabel = stringResource(Res.string.ok),
                onConfirm = { depositWithdrawDialog = 0 },
            )
        }
        if (loanAddedDialog != 0L) {
            DesignMessageDialog(
                title = stringResource(Res.string.attention),
                message = stringResource(Res.string.not_enough_cash_loan_taken, loanAddedDialog.toString()),
                onDismissRequest = { loanAddedDialog = 0 },
                confirmLabel = stringResource(Res.string.ok),
                onConfirm = { loanAddedDialog = 0 },
            )
        }
        if (loanOverlimitedDialog) {
            DesignMessageDialog(
                title = stringResource(Res.string.attention),
                message = stringResource(Res.string.limitOverload, state.board.loanLimit),
                onDismissRequest = { loanOverlimitedDialog = false },
                confirmLabel = stringResource(Res.string.yes),
                onConfirm = {
                    navigator.returnToBoardList()
                    navigator.push(InitPlayerScreen(board))
                    loanOverlimitedDialog = false
                },
                dismissLabel = stringResource(Res.string.no),
                onDismissAction = {
                    navigator.returnToBoardList()
                    MainScope().launch {
                        appKStore.update {
                            it?.copy(clientUuid = "")
                        }
                    }
                    loanOverlimitedDialog = false
                },
            )
        }
        val receivedCash = receivedCashDialog
        if (receivedCash != null) {
            val player = players.collectAsState().value.find { it.id == receivedCash.receiverId }
            DesignMessageDialog(
                message = stringResource(
                    Res.string.cash_received_amount,
                    player?.card?.name ?: "Incognito",
                    receivedCash.amount,
                ),
                onDismissRequest = { receivedCashDialog = null },
                confirmLabel = stringResource(Res.string.great),
                onConfirm = { receivedCashDialog = null },
            )
        }
        bankruptBusinessDialog?.let { business ->
            DesignMessageDialog(
                message = stringResource(
                    Res.string.business_bankruptcy,
                    business.name,
                    business.profit.toString(),
                ),
                onDismissRequest = { bankruptBusinessDialog = null },
                confirmLabel = stringResource(Res.string.ok),
                onConfirm = { bankruptBusinessDialog = null },
            )
        }
        if (congratulationsWithBabyDialog) {
            DesignMessageDialog(
                title = stringResource(Res.string.сongratulations),
                message = stringResource(Res.string.congratulationsWithBaby),
                onDismissRequest = { congratulationsWithBabyDialog = false },
                confirmLabel = stringResource(Res.string.great),
                onConfirm = { congratulationsWithBabyDialog = false },
            )
        }
        if (congratulationsWithMarriageDialog) {
            DesignMessageDialog(
                title = stringResource(Res.string.сongratulations),
                message = stringResource(Res.string.congratulationsWithMarriage),
                onDismissRequest = { congratulationsWithMarriageDialog = false },
                confirmLabel = stringResource(Res.string.great),
                onConfirm = { congratulationsWithMarriageDialog = false },
            )
        }
        if (youDivorcedDialog) {
            DesignMessageDialog(
                message = stringResource(Res.string.youDivorced),
                onDismissRequest = { youDivorcedDialog = false },
                confirmLabel = stringResource(Res.string.ok),
                onConfirm = { youDivorcedDialog = false },
            )
        }
        playerDivorcedDialog?.let { playerName ->
            DesignMessageDialog(
                message = stringResource(Res.string.playerDivorced, playerName),
                onDismissRequest = { playerDivorcedDialog = null },
                confirmLabel = stringResource(Res.string.ok),
                onConfirm = { playerDivorcedDialog = null },
            )
        }
        playerHadBabyDialog?.let { (playerName, babies) ->
            DesignMessageDialog(
                title = stringResource(Res.string.kids),
                message = stringResource(Res.string.playerHadBaby, playerName, babies),
                onDismissRequest = { playerHadBabyDialog = null },
                confirmLabel = stringResource(Res.string.ok),
                onConfirm = { playerHadBabyDialog = null },
            )
        }
        playerMarriedDialog?.let { playerName ->
            DesignMessageDialog(
                title = stringResource(Res.string.marriage),
                message = stringResource(Res.string.playerMarried, playerName),
                onDismissRequest = { playerMarriedDialog = null },
                confirmLabel = stringResource(Res.string.ok),
                onConfirm = { playerMarriedDialog = null },
            )
        }
        resignationDialog?.let { business ->
            DesignMessageDialog(
                message = stringResource(Res.string.resignation, business.profit),
                onDismissRequest = { resignationDialog = null },
                confirmLabel = stringResource(Res.string.ok),
                onConfirm = { resignationDialog = null },
            )
        }
        if (dreamOfferedDialog) {
            val dream = state.board.dreamById(state.currentDream?.id, Locale.current.language)
            val canPay = dream != null && state.canPay(dream.price)
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
        }
        val inspectedDreamId by vm.inspectedDreamId.collectAsState()
        val inspectedDream = inspectedDreamId?.let { dreamId ->
            state.board.dreamById(dreamId, Locale.current.language)
        }
        inspectedDream?.let { dream ->
            val allPlayers by players.collectAsState()
            DesignDreamDetailsDialog(
                dream = dream,
                selectors = allPlayers.dreamSelectors(dream.id),
                currentPlayerId = state.player.id,
                isPurchased = dream.id in state.board.purchasedDreamIds,
                canSelect = state.board.canSelectDream(dream.id, state.player.id, allPlayers),
                onSelect = { vm.selectDream(dream.id) },
                onDismiss = { vm.closeInspectedDream() },
            )
        }
        victoryDialog?.let { victory ->
            DesignMessageDialog(
                title = stringResource(Res.string.victory),
                message = if (victory.isCurrentPlayer) {
                    stringResource(Res.string.you_won)
                } else {
                    stringResource(Res.string.player_won, victory.playerName)
                },
                onDismissRequest = { victoryDialog = null },
                confirmLabel = stringResource(Res.string.great),
                onConfirm = { victoryDialog = null },
            )
        }
    }
}

private fun Navigator.returnToBoardList() {
    if (!popUntil { it is BoardListScreen }) {
        replaceAll(BoardListScreen())
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
    val focus = rememberCellFocus()
    val bubble = rememberTokenBubbleState()
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
            val layout = rememberBoardLayout(vm, isVertical)
            if (layout != null) {
                BoardPanel(vm, layout, focus, bubble)
            }
            if (isFlipped(rotY, rotX)) {
                BackSide()
            }
            Dice(vm)
            if (layout != null && designV2Enabled.value) {
                DesignBoardOverlay(layout, vm, focus, bubble)
            }
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

private const val DICE_WAVE_FRACTION = 1.15f
private const val DICE_BODY_DROP = 0.16f

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
            play(GameSound.DiceRoll)
            coroutineScope.launch {
                animatable.animate(composition, iterations = 1, initialProgress = 0f)
            }
        }
    }
    val rollSize = min(maxWidth, maxHeight) / 7
    val arrowSize = rollSize * 0.7f
    Row(
        modifier = Modifier.align(Alignment.Center),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(rollSize),
            contentAlignment = Alignment.Center,
        ) {
            if (state.canRoll) {
                if (designV2Enabled.value) {
                    DesignActiveWaves(
                        color = Design.scaffold.accentDim,
                        modifier = Modifier
                            .size(rollSize * DICE_WAVE_FRACTION)
                            .offset(y = rollSize * DICE_BODY_DROP),
                    )
                } else {
                    LegacyDiceGlow(rollSize)
                }
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
        if (state.canSkipDeputies) {
            EButton(
                modifier = Modifier.align(Alignment.CenterVertically),
                onClick = { vm.skipDeputies() },
                title = stringResource(Res.string.skip_deputies),
                unitTS = with(LocalDensity.current) { (rollSize.toPx() / 100).toSp() },
                unitDp = rollSize / 100,
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
