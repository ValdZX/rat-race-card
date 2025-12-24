package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.beans.Business
import ua.vald_zx.game.rat.race.card.components.*
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.ExtendBusiness
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardSideEffect
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardStore
import ua.vald_zx.game.rat.race.card.logic.total
import ua.vald_zx.game.rat.race.card.playCoin
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Menu
import ua.vald_zx.game.rat.race.card.resource.images.Rat
import ua.vald_zx.game.rat.race.card.resource.images.Settings
import ua.vald_zx.game.rat.race.card.screen.second.page.BusinessListPage
import ua.vald_zx.game.rat.race.card.screen.second.page.FundsPage
import ua.vald_zx.game.rat.race.card.screen.second.page.SharesPage
import ua.vald_zx.game.rat.race.card.screen.second.page.StatePage
import ua.vald_zx.game.rat.race.card.splitDecimal
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import ua.vald_zx.game.rat.race.card.tts
import ua.vald_zx.game.rat.race.card.ttsIsUkraineSupported


class RaceRate2Screen : Screen {

    @OptIn(
        ExperimentalMaterialApi::class,
        ExperimentalMaterial3Api::class,
    )
    @Composable
    override fun Content() {
        val raceRate2store = koinInject<RatRace2CardStore>()
        val state by raceRate2store.observeState().collectAsState()
        val navigator = LocalNavigator.current
        BottomSheetNavigator {
            val bottomSheetNavigator = LocalBottomSheetNavigator.current
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(16.dp),
            ) {
                IconButton(
                    modifier = Modifier.align(Alignment.TopStart),
                    onClick = { bottomSheetNavigator.show(AllActionsScreen()) },
                    content = { Icon(Images.Menu, contentDescription = null) }
                )
                Icon(
                    imageVector = Images.Settings,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.TopEnd)
                        .size(48.dp)
                        .clip(CircleShape)
                        .combinedClickable(onClick = { navigator?.push(SettingsScreen()) })
                        .padding(8.dp),
                )
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.status(),
                            maxLines = 2,
                            style = MaterialTheme.typography.titleLarge,
                            overflow = TextOverflow.Ellipsis,
                        )
                        IconButton(
                            onClick = { bottomSheetNavigator.show(PlayersScreen()) },
                            content = {
                                Icon(
                                    Images.Rat,
                                    contentDescription = null,
                                    tint = if (state.room.isNotEmpty()) AppTheme.colors.cash else MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                    Row(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SmoothRainbowText(
                            state.total().splitDecimal(),
                            rainbow = GoldRainbow,
                            style = LocalTextStyle.current.copy(fontSize = 30.sp),
                            modifier = Modifier.clickable {
                                if (raceRate2store.statistics != null) {
                                    bottomSheetNavigator.show(StatisticsScreen())
                                }
                            },
                            duration = 4000
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            state.lastTotals.forEach { totalDiff ->
                                val total = if (totalDiff > 0) {
                                    "+${totalDiff}"
                                } else {
                                    totalDiff.toString()
                                }
                                Text(
                                    text = total,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (totalDiff > 0) {
                                        AppTheme.colors.cash
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    },
                                    lineHeight = 11.sp
                                )
                            }
                        }
                    }
                    CashFlowField(
                        name = "Cash Flow",
                        lastCashFlows = state.lastCashFlows,
                        value = "${state.cashFlow()}",
                        onClick = { raceRate2store.dispatch(RatRace2CardAction.GetSalary) },
                        salary = { raceRate2store.dispatch(RatRace2CardAction.GetSalary) })
                    BalanceField(
                        name = stringResource(Res.string.cash),
                        value = "${state.cash} $",
                        add = { bottomSheetNavigator.show(SideProfitScreen()) },
                        sub = { bottomSheetNavigator.show(SideExpensesScreen()) },
                        onClick = { bottomSheetNavigator.show(CashActionsScreen()) },
                    )
                    PositiveField(
                        name = stringResource(Res.string.deposit),
                        value = "${state.deposit} $",
                        onClick = { bottomSheetNavigator.show(DepositActionsScreen()) },
                        deposit = { bottomSheetNavigator.show(ToDepositScreen()) },
                    )
                    NegativeField(
                        name = stringResource(Res.string.loan),
                        value = "${state.loan} $",
                        onClick = { bottomSheetNavigator.show(LoanActionsScreen()) },
                        repay = {
                            if (state.loan > 0) {
                                bottomSheetNavigator.show(RepayCreditScreen())
                            }
                        },
                    )
                    if (state.config.hasFunds) {
                        FundsField(
                            name = stringResource(Res.string.funds),
                            value = "${state.fundAmount()} $"
                        ) {
                            bottomSheetNavigator.show(BuyFundScreen())
                        }
                    }
                    val coroutineScope = rememberCoroutineScope()
                    val titles = mutableListOf(
                        stringResource(Res.string.status),
                        stringResource(Res.string.business),
                        stringResource(Res.string.shares)
                    )
                    if (state.config.hasFunds) {
                        titles += stringResource(Res.string.funds)
                    }
                    val pagerState = rememberPagerState(pageCount = { titles.size })
                    PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                        titles.forEachIndexed { index, title ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                },
                                text = {
                                    Text(
                                        text = title,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                    ) { page ->
                        when (page) {
                            0 -> StatePage(state)
                            1 -> BusinessListPage(state)
                            2 -> SharesPage(state)
                            3 -> FundsPage(state)
                        }
                    }
                }
            }
            var depositWithdrawDialog by remember { mutableStateOf(0L) }
            var loanAddedDialog by remember { mutableStateOf(0L) }
            var receivedCashDialog by remember { mutableStateOf(0L) }
            var salaryApproveDialog by remember { mutableStateOf(false) }
            var confirmFiredDialog by remember { mutableStateOf(false) }
            var confirmDismissalDialog: Business? by remember { mutableStateOf(null) }
            var confirmDismissalOnExtentionDialog: ExtendBusiness? by remember { mutableStateOf(null) }
            var confirmSellingAllBusinessDialog: Business? by remember { mutableStateOf(null) }
            LaunchedEffect(Unit) {
                raceRate2store.observeSideEffect().onEach { effect ->
                    when (effect) {
                        is RatRace2CardSideEffect.ShowSalaryApprove -> {
                            salaryApproveDialog = true
                        }

                        is RatRace2CardSideEffect.ConfirmDismissal -> {
                            confirmDismissalDialog = effect.business
                        }


                        is RatRace2CardSideEffect.ConfirmDismissalOnExtention -> {
                            confirmDismissalOnExtentionDialog = effect.extention
                        }

                        is RatRace2CardSideEffect.ConfirmSellingAllBusiness -> {
                            confirmSellingAllBusinessDialog = effect.business
                        }

                        is RatRace2CardSideEffect.DepositWithdraw -> {
                            depositWithdrawDialog = effect.balance
                        }

                        is RatRace2CardSideEffect.LoanAdded -> {
                            loanAddedDialog = effect.balance
                        }

                        is RatRace2CardSideEffect.ReceivedCash -> {
                            receivedCashDialog = effect.amount
                        }

                        is RatRace2CardSideEffect.AddCash -> {
                            if (state.config.tts) {
                                if (ttsIsUkraineSupported()) {
                                    tts("Зараховано ${effect.amount}")
                                } else {
                                    tts("${effect.amount} is credited")
                                }
                            } else {
                                playCoin()
                            }
                        }

                        is RatRace2CardSideEffect.SubCash -> {
                            if (state.config.tts) {
                                if (ttsIsUkraineSupported()) {
                                    tts("Списано ${effect.amount}")
                                } else {
                                    tts("${effect.amount} is debited")
                                }
                            } else {
                                playCoin()
                            }
                        }

                        is RatRace2CardSideEffect.Capitalized -> {
                            if (state.config.tts) {
                                if (ttsIsUkraineSupported()) {
                                    tts("Капіталізовано ${effect.amount}")
                                } else {
                                    tts("Capitalized ${effect.amount}")
                                }
                            } else {
                                playCoin()
                            }
                        }

                        RatRace2CardSideEffect.ConfirmFired -> {
                            confirmFiredDialog = true
                        }
                    }
                }.launchIn(this)
            }
            if (salaryApproveDialog) {
                AlertDialog(
                    title = { Text(text = stringResource(Res.string.profit)) },
                    text = {
                        Text(
                            text = stringResource(
                                Res.string.income_with_assets_and_loans,
                                state.cashFlow().splitDecimal()
                            )
                        )
                    },
                    onDismissRequest = { salaryApproveDialog = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                raceRate2store.dispatch(RatRace2CardAction.GetSalaryApproved)
                                salaryApproveDialog = false
                            }
                        ) { Text(stringResource(Res.string.receive)) }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            salaryApproveDialog = false
                        }) { Text(stringResource(Res.string.cancel)) }
                    }
                )
            }
            if (confirmDismissalDialog != null) {
                AlertDialog(
                    title = { Text(text = stringResource(Res.string.fire_from_job)) },
                    text = {
                        Text(
                            text = stringResource(
                                Res.string.lose_job_on_second_business_with_salary,
                                state.playerCard.salary.toString()
                            )
                        )
                    },
                    onDismissRequest = { confirmDismissalDialog = null },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                raceRate2store.dispatch(
                                    RatRace2CardAction.DismissalConfirmed(
                                        confirmDismissalDialog!!
                                    )
                                )
                                confirmDismissalDialog = null
                            }
                        ) { Text(stringResource(Res.string.resign)) }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            confirmDismissalDialog = null
                        }) { Text(stringResource(Res.string.cancel)) }
                    }
                )
            }
            if (confirmDismissalOnExtentionDialog != null) {
                AlertDialog(
                    title = { Text(text = stringResource(Res.string.fire_from_job)) },
                    text = {
                        Text(
                            text = stringResource(
                                Res.string.lose_job_on_second_business_with_salary,
                                state.playerCard.salary.toString()
                            )
                        )
                    },
                    onDismissRequest = { confirmDismissalOnExtentionDialog = null },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                raceRate2store.dispatch(
                                    RatRace2CardAction.DismissalConfirmedOnExtention(
                                        confirmDismissalOnExtentionDialog!!
                                    )
                                )
                                confirmDismissalOnExtentionDialog = null
                            }
                        ) { Text(stringResource(Res.string.resign)) }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            confirmDismissalOnExtentionDialog = null
                        }) { Text(stringResource(Res.string.cancel)) }
                    }
                )
            }
            if (confirmFiredDialog) {
                AlertDialog(
                    title = { Text(text = stringResource(Res.string.fire_from_job)) },
                    text = {
                        Text(
                            text = stringResource(
                                Res.string.fired_lose_income_amount,
                                state.playerCard.salary.toString()
                            )
                        )
                    },
                    onDismissRequest = { confirmFiredDialog = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                raceRate2store.dispatch(RatRace2CardAction.FiredConfirmed)
                                confirmFiredDialog = false
                            }
                        ) { Text(stringResource(Res.string.resign)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmFiredDialog = false }) { Text(stringResource(Res.string.cancel)) }
                    }
                )
            }
            if (confirmSellingAllBusinessDialog != null) {
                AlertDialog(
                    title = { Text(text = stringResource(Res.string.buy_business_title)) },
                    text = {
                        Text(
                            text = stringResource(
                                Res.string.need_sell_all_businesses_with_sum,
                                state.business.sumOf { it.price }.toString()
                            )
                        )
                    },
                    onDismissRequest = { confirmSellingAllBusinessDialog = null },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                raceRate2store.dispatch(
                                    RatRace2CardAction.SellingAllBusinessConfirmed(
                                        confirmSellingAllBusinessDialog!!
                                    )
                                )
                                confirmSellingAllBusinessDialog = null
                            }
                        ) { Text(stringResource(Res.string.buy)) }
                    },
                    dismissButton = {
                        TextButton(onClick = {
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
                        ) { Text(stringResource(Res.string.ok)) }
                    },
                )
            }
        }
    }
}