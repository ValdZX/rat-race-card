package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ua.vald_zx.game.rat.race.card.beans.Business
import ua.vald_zx.game.rat.race.card.components.BalanceField
import ua.vald_zx.game.rat.race.card.components.CashFlowField
import ua.vald_zx.game.rat.race.card.components.FundsField
import ua.vald_zx.game.rat.race.card.components.GoldRainbow
import ua.vald_zx.game.rat.race.card.components.NegativeField
import ua.vald_zx.game.rat.race.card.components.PositiveField
import ua.vald_zx.game.rat.race.card.components.SmoothRainbowText
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardSideEffect
import ua.vald_zx.game.rat.race.card.logic.total
import ua.vald_zx.game.rat.race.card.playCoin
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Menu
import ua.vald_zx.game.rat.race.card.resource.images.Settings
import ua.vald_zx.game.rat.race.card.screen.second.page.BusinessListPage
import ua.vald_zx.game.rat.race.card.screen.second.page.FundsPage
import ua.vald_zx.game.rat.race.card.screen.second.page.SharesPage
import ua.vald_zx.game.rat.race.card.screen.second.page.StatePage
import ua.vald_zx.game.rat.race.card.splitDecimal
import ua.vald_zx.game.rat.race.card.raceRate2store
import ua.vald_zx.game.rat.race.card.tts
import ua.vald_zx.game.rat.race.card.ttsIsUkraineSupported


class RaceRate2Screen : Screen {

    @OptIn(
        ExperimentalMaterialApi::class,
        ExperimentalFoundationApi::class,
        ExperimentalMaterial3Api::class,
        ExperimentalKStoreApi::class
    )
    @Composable
    override fun Content() {
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
                    content = {
                        Icon(Images.Menu, contentDescription = null)
                    }
                )
                IconButton(
                    modifier = Modifier.align(Alignment.TopEnd),
                    onClick = { navigator?.push(SettingsScreen()) },
                    content = {
                        Icon(Images.Settings, contentDescription = null)
                    }
                )
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.status(),
                        maxLines = 2,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 48.dp),
                        overflow = TextOverflow.Ellipsis,
                    )

                    SmoothRainbowText(
                        state.total().splitDecimal().toString(),
                        rainbow = GoldRainbow,
                        style = LocalTextStyle.current.copy(fontSize = 30.sp),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                            .clickable {
                                if (raceRate2store.statistics != null) {
                                    bottomSheetNavigator.show(StatisticsScreen())
                                }
                            },
                        duration = 4000
                    )
                    CashFlowField(
                        name = "Cash Flow",
                        value = state.cashFlow().toString(),
                        onClick = { raceRate2store.dispatch(RatRace2CardAction.GetSalary) },
                        salary = { raceRate2store.dispatch(RatRace2CardAction.GetSalary) })
                    BalanceField(
                        name = "Готівка",
                        value = state.cash.toString(),
                        add = { bottomSheetNavigator.show(SideProfitScreen()) },
                        sub = { bottomSheetNavigator.show(SideExpensesScreen()) },
                        onClick = { bottomSheetNavigator.show(CashActionsScreen()) },
                    )
                    PositiveField(
                        name = "Депозит",
                        value = state.deposit.toString(),
                        onClick = { bottomSheetNavigator.show(DepositActionsScreen()) },
                        deposit = { bottomSheetNavigator.show(ToDepositScreen()) },
                    )
                    NegativeField(
                        name = "Кредит", value = state.loan.toString(),
                        onClick = { bottomSheetNavigator.show(LoanActionsScreen()) },
                        repay = { bottomSheetNavigator.show(RepayCreditScreen()) },
                    )
                    if (state.config.hasFunds) {
                        FundsField("Фонди", state.fundAmount().toString()) {
                            bottomSheetNavigator.show(BuyFundScreen())
                        }
                    }
                    val coroutineScope = rememberCoroutineScope()
                    val titles = mutableListOf("Стан", "Бізнес", "Акції")
                    if (state.config.hasFunds) {
                        titles += "Фонди"
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
            var salaryApproveDialog by remember { mutableStateOf(false) }
            var confirmFiredDialog by remember { mutableStateOf(false) }
            var confirmDismissalDialog: Business? by remember { mutableStateOf(null) }
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

                        is RatRace2CardSideEffect.ConfirmSellingAllBusiness -> {
                            confirmSellingAllBusinessDialog = effect.business
                        }

                        is RatRace2CardSideEffect.DepositWithdraw -> {
                            depositWithdrawDialog = effect.balance
                        }

                        is RatRace2CardSideEffect.LoanAdded -> {
                            loanAddedDialog = effect.balance
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

                        RatRace2CardSideEffect.ConfirmFired -> {
                            confirmFiredDialog = true
                        }
                    }
                }.launchIn(this)
            }
            if (salaryApproveDialog) {
                AlertDialog(
                    title = { Text(text = "Дохід") },
                    text = {
                        Text(
                            text = "Дохід з урахуванням, активів, пасивів та кредитів: ${
                                state.cashFlow().splitDecimal()
                            }"
                        )
                    },
                    onDismissRequest = { salaryApproveDialog = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                raceRate2store.dispatch(RatRace2CardAction.GetSalaryApproved)
                                salaryApproveDialog = false
                            }
                        ) { Text("Отримати") }
                    },
                    dismissButton = {
                        TextButton(onClick = { salaryApproveDialog = false }) { Text("Відміна") }
                    }
                )
            }
            if (confirmDismissalDialog != null) {
                AlertDialog(
                    title = { Text(text = "Звільнення з роботи") },
                    text = {
                        Text(
                            text = "При купівлі другого бізнеса, втрачається робота з зарплатою ${state.professionCard.salary}"
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
                        ) { Text("Звільнитися") }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmDismissalDialog = null }) { Text("Відміна") }
                    }
                )
            }
            if (confirmFiredDialog) {
                AlertDialog(
                    title = { Text(text = "Звільнення з роботи") },
                    text = {
                        Text(
                            text = "При звільненні, втрачається дохід на сумму ${state.professionCard.salary}"
                        )
                    },
                    onDismissRequest = { confirmFiredDialog = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                raceRate2store.dispatch(RatRace2CardAction.FiredConfirmed)
                                confirmFiredDialog = false
                            }
                        ) { Text("Звільнитися") }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmFiredDialog = false }) { Text("Відміна") }
                    }
                )
            }
            if (confirmSellingAllBusinessDialog != null) {
                AlertDialog(
                    title = { Text(text = "Купівля бізнесу") },
                    text = {
                        Text(
                            text = "Щоб купити бізнес вищого класу, потрібно продати всі наявні бізнеса. Сума продажу бізнесів: ${state.business.sumOf { it.price }}"
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
                        ) { Text("Купити") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            confirmSellingAllBusinessDialog = null
                        }) { Text("Відміна") }
                    }
                )
            }
            if (depositWithdrawDialog != 0L) {
                AlertDialog(
                    title = { Text(text = "Увага") },
                    text = {
                        Text(
                            text = "Не вистачило готівки, тому було знато з депозита: $depositWithdrawDialog"
                        )
                    },
                    onDismissRequest = { depositWithdrawDialog = 0 },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                depositWithdrawDialog = 0
                            }
                        ) { Text("Гаразд") }
                    },
                )
            }
            if (loanAddedDialog != 0L) {
                AlertDialog(
                    title = { Text(text = "Увага") },
                    text = {
                        Text(
                            text = "Не вистачило готівки, тому взято в кредит на суму: $loanAddedDialog"
                        )
                    },
                    onDismissRequest = { loanAddedDialog = 0 },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                loanAddedDialog = 0
                            }
                        ) { Text("Гаразд") }
                    },
                )
            }
        }
    }
}