package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.composables.core.BottomSheetState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ua.vald_zx.game.rat.race.card.*
import ua.vald_zx.game.rat.race.card.beans.Business
import ua.vald_zx.game.rat.race.card.components.FundsField
import ua.vald_zx.game.rat.race.card.components.GoldRainbow
import ua.vald_zx.game.rat.race.card.components.SkittlesRainbow
import ua.vald_zx.game.rat.race.card.components.SmoothRainbowText
import ua.vald_zx.game.rat.race.card.logic.CardAction
import ua.vald_zx.game.rat.race.card.logic.CardSideEffect
import ua.vald_zx.game.rat.race.card.logic.total
import ua.vald_zx.game.rat.race.card.screen.board.page.BusinessListPage
import ua.vald_zx.game.rat.race.card.screen.board.page.FundsPage
import ua.vald_zx.game.rat.race.card.screen.board.page.SharesPage
import ua.vald_zx.game.rat.race.card.screen.board.page.StatePage
import ua.vald_zx.game.rat.race.card.screen.second.BuyFundScreen
import ua.vald_zx.game.rat.race.card.theme.AppTheme


@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun Board2PlayerDetailsScreen(scaffoldState: BottomSheetState) {
    val boardState by raceRate2BoardStore.observeState().collectAsState()
    val store = raceRate2BoardStore.card
    val state by store.observeState().collectAsState()
    val currentPlayer = boardState.currentPlayer ?: return
    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    val coroutineScope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp)
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (scaffoldState.currentDetent != ContentExpanded) {
                    coroutineScope.launch {
                        scaffoldState.animateTo(ContentExpanded)
                    }
                }
            }
            .padding(horizontal = 16.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Column(
                modifier = Modifier.height(littleDetailsHeight),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = state.status(currentPlayer),
                        maxLines = 2,
                        style = MaterialTheme.typography.titleLarge,
                        overflow = TextOverflow.Ellipsis,
                    )
                    SmoothRainbowText(
                        state.total().splitDecimal(),
                        rainbow = GoldRainbow,
                        style = LocalTextStyle.current.copy(fontSize = 30.sp),
                        modifier = Modifier.padding(start = 16.dp),
                        duration = 4000
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SmoothRainbowText(
                            text = "Cash Flow",
                            rainbow = SkittlesRainbow,
                            style = LocalTextStyle.current.copy(fontSize = 16.sp),
                        )
                        Text("${state.cashFlow(currentPlayer)}")
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Готівка", color = AppTheme.colors.cash)
                        Text("${state.cash} $")
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Депозит", color = MaterialTheme.colorScheme.primary)
                        Text("${state.deposit} $")
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Кредит", color = MaterialTheme.colorScheme.tertiary)
                        Text("${state.loan} $")
                    }
                }
            }
            if (state.config.hasFunds) {
                FundsField(
                    name = "Фонди",
                    value = "${state.fundAmount()} $"
                ) {
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
                    0 -> StatePage(state, currentPlayer)
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
    var confirmSellingAllBusinessDialog: Business? by remember { mutableStateOf(null) }
    LaunchedEffect(Unit) {
        store.observeSideEffect().onEach { effect ->
            when (effect) {
                is CardSideEffect.ShowSalaryApprove -> {
                    salaryApproveDialog = true
                }

                is CardSideEffect.ConfirmDismissal -> {
                    confirmDismissalDialog = effect.business
                }

                is CardSideEffect.ConfirmSellingAllBusiness -> {
                    confirmSellingAllBusinessDialog = effect.business
                }

                is CardSideEffect.DepositWithdraw -> {
                    depositWithdrawDialog = effect.balance
                }

                is CardSideEffect.LoanAdded -> {
                    loanAddedDialog = effect.balance
                }

                is CardSideEffect.ReceivedCash -> {
                    receivedCashDialog = effect.amount
                }

                is CardSideEffect.AddCash -> {
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

                is CardSideEffect.SubCash -> {
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

                CardSideEffect.ConfirmFired -> {
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
                        state.cashFlow(currentPlayer).splitDecimal()
                    }"
                )
            },
            onDismissRequest = { salaryApproveDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        store.dispatch(CardAction.GetSalaryApproved)
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
                    text = "При купівлі другого бізнеса, втрачається робота з зарплатою ${currentPlayer.playerCard.salary}"
                )
            },
            onDismissRequest = { confirmDismissalDialog = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        store.dispatch(
                            CardAction.DismissalConfirmed(
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
                    text = "При звільненні, втрачається дохід на сумму ${currentPlayer.playerCard.salary}"
                )
            },
            onDismissRequest = { confirmFiredDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        store.dispatch(CardAction.FiredConfirmed)
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
                        store.dispatch(
                            CardAction.SellingAllBusinessConfirmed(
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
    if (receivedCashDialog != 0L) {
        AlertDialog(
            text = {
                Text(
                    text = "Вам прислали готівку: $receivedCashDialog"
                )
            },
            onDismissRequest = { receivedCashDialog = 0 },
            confirmButton = {
                TextButton(
                    onClick = {
                        receivedCashDialog = 0
                    }
                ) { Text("Чудово") }
            },
        )
    }
}