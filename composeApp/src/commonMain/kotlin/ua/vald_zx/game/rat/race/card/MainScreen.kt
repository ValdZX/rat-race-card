package ua.vald_zx.game.rat.race.card

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ua.vald_zx.game.rat.race.card.beans.Business
import ua.vald_zx.game.rat.race.card.beans.BusinessType
import ua.vald_zx.game.rat.race.card.beans.Shares
import ua.vald_zx.game.rat.race.card.components.SmoothRainbowText
import ua.vald_zx.game.rat.race.card.logic.AppAction
import ua.vald_zx.game.rat.race.card.logic.AppSideEffect
import ua.vald_zx.game.rat.race.card.logic.AppState
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.fromvectorimages.IcDarkMode
import ua.vald_zx.game.rat.race.card.resource.fromvectorimages.IcLightMode
import ua.vald_zx.game.rat.race.card.resource.fromvectorimages.Settings
import ua.vald_zx.game.rat.race.card.resource.fromvectorimages.Stars
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark


class MainScreen : Screen {

    @OptIn(
        ExperimentalMaterialApi::class,
        ExperimentalFoundationApi::class,
        ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content() {
        val state by store.observeState().collectAsState()
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
                    modifier = Modifier.align(Alignment.TopEnd),
                    onClick = { navigator?.push(SettingsScreen()) },
                    content = {
                        Icon(Images.Settings, contentDescription = null)
                    }
                )
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.status(),
                        maxLines = 2,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 24.dp),
                        overflow = TextOverflow.Ellipsis,
                    )
                    CashFlowField("Cash Flow", state.cashFlow().toString())
                    BalanceField("Готівка", state.cash.toString())
                    PositiveField("Депозит", state.deposit.toString())
                    NegativeField("Кредит", state.loan.toString())
                    if (state.config.hasFunds) {
                        FundsField("Фонди", state.fundAmount().toString())
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
                    ElevatedButton(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            .widthIn(min = 200.dp),
                        onClick = { bottomSheetNavigator.show(ActionsScreen()) },
                        content = { Text("Дія") }
                    )
                }
            }
            var expensiveShares: Shares? by remember { mutableStateOf(null) }
            var salaryApproveDialog by remember { mutableStateOf(false) }
            var confirmDismissalDialog: Business? by remember { mutableStateOf(null) }
            var confirmSellingAllBusinessDialog: Business? by remember { mutableStateOf(null) }
            LaunchedEffect(Unit) {
                store.observeSideEffect().onEach { effect ->
                    when (effect) {
                        is AppSideEffect.SharesToExpensive -> {
                            expensiveShares = effect.shares
                        }

                        is AppSideEffect.ShowSalaryApprove -> {
                            salaryApproveDialog = true
                        }

                        is AppSideEffect.ConfirmDismissal -> {
                            confirmDismissalDialog = effect.business
                        }

                        is AppSideEffect.ConfirmSellingAllBusiness -> {
                            confirmSellingAllBusinessDialog = effect.business
                        }
                    }
                }.launchIn(this)
            }
            if (expensiveShares != null) {
                AlertDialog(
                    title = { Text(text = "Недостатньо готівки") },
                    text = { Text(text = "Не вистачило: ${state.cash - (expensiveShares?.price ?: 0)}") },
                    onDismissRequest = { expensiveShares = null },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                bottomSheetNavigator.show(BuySharesScreen(expensiveShares!!))
                                expensiveShares = null
                            }
                        ) { Text("Редагувати") }
                    },
                    dismissButton = {
                        TextButton(onClick = { expensiveShares = null }) { Text("Гаразд") }
                    }
                )
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
                                store.dispatch(AppAction.GetSalaryApproved)
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
                                store.dispatch(AppAction.DismissalConfirmed(confirmDismissalDialog!!))
                                confirmDismissalDialog = null
                            }
                        ) { Text("Звільнитися") }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmDismissalDialog = null }) { Text("Відміна") }
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
                                    AppAction.SellingAllBusinessConfirmed(
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
        }
    }

    @Composable
    private fun StatePage(state: AppState) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(state = rememberScrollState())) {
            DetailsField(
                "Активний прибуток",
                state.activeProfit().toString(),
                MaterialTheme.colorScheme.primary
            )
            DetailsField(
                "Пасивний прибуток",
                state.passiveProfit().toString(),
                MaterialTheme.colorScheme.primary
            )
            DetailsField(
                "Загальний прибуток",
                state.totalProfit().toString(),
                MaterialTheme.colorScheme.primary
            )
            DetailsField(
                "Витрати по кредиту",
                state.creditExpenses().toString(),
                MaterialTheme.colorScheme.tertiary
            )
            DetailsField(
                "Загальні витрати",
                state.totalExpenses().toString(),
                MaterialTheme.colorScheme.tertiary
            )
            Text("Багатство", style = MaterialTheme.typography.titleSmall)
            DetailsField("Авто", "${state.cars}")
            DetailsField("Квартири", "${state.apartment}")
            DetailsField("Будинки", "${state.apartment}")
            DetailsField("Яхти", "${state.yacht}")
            DetailsField("Літаки", "${state.flight}")
            Text("Сімейний стан", style = MaterialTheme.typography.titleSmall)
            DetailsField("Шлюб", if (state.isMarried) "Так" else "Hi")
            DetailsField("Діти", "${state.babies}")
            Text("Витрати", style = MaterialTheme.typography.titleSmall)
            DetailsField("Оренда житла", state.professionCard.rent.toString())
            DetailsField("Витрати на їжу", state.professionCard.food.toString())
            DetailsField("Витрати на одяг", state.professionCard.cloth.toString())
            DetailsField("Витрати на проїзд", state.professionCard.transport.toString())
            DetailsField("Витрати на телефонні переговори", state.professionCard.phone.toString())
            DetailsField("Витрати на дітей", "${state.babies * 300} (${state.babies} * 300)")
            DetailsField(
                "Витрати на квартиру",
                "${state.apartment * 200} (${state.apartment} * 200)"
            )
            DetailsField("Витрати на машину", "${state.cars * 600} (${state.cars} * 600)")
            DetailsField("Утримання котеджу", "${state.cottage * 1000} (${state.cottage} * 1000)")
            DetailsField("Утримання яхти", "${state.yacht * 1500} (${state.yacht} * 1500)")
            DetailsField("Утримання літака", "${state.flight * 5000} (${state.flight} * 5000)")
        }
    }

    @Composable
    private fun BusinessListPage(state: AppState) {
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(state.business.filter { it.type != BusinessType.WORK }) { index, business ->
                Column(modifier = Modifier.padding(8.dp)) {
                    val title = when (business.type) {
                        BusinessType.WORK -> ""
                        BusinessType.SMALL -> "Малий бізнес"
                        BusinessType.MEDIUM -> "Середній бізнес"
                        BusinessType.LARGE -> "Крупний бізнес"
                    }
                    Text(
                        "${index + 1}) $title - ${business.name}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SDetailsField(
                            name = "Ціна",
                            value = business.price.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        SDetailsField(
                            name = "Прибуток",
                            value = business.profit.toString(),
                            additionalValue = business.extentions.map { it.toString() },
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { bottomSheetNavigator.show(SellBusinessScreen(business)) }) {
                            Text("Продати")
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }

    @Composable
    private fun SharesPage(state: AppState) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.sharesList) { shares ->
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(shares.type.name, style = MaterialTheme.typography.titleSmall)
                    Row {
                        SDetailsField(
                            name = "Кількість",
                            value = shares.count.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        SDetailsField(
                            name = "Ціна покупки",
                            value = shares.buyPrice.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }

    @Composable
    private fun FundsPage(state: AppState) {
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.funds) { fund ->
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SDetailsField(
                                name = "Фонд на",
                                value = "${fund.rate}%",
                                modifier = Modifier.weight(1f)
                            )
                            SDetailsField(
                                name = "Сума",
                                value = fund.amount.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { bottomSheetNavigator.show(SellFundScreen(fund)) }) {
                                Text("Зняти")
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
            var capitalizationConfirm by remember { mutableStateOf(false) }
            var capitalizationStarConfirm by remember { mutableStateOf(false) }
            Row {
                ElevatedButton(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        .widthIn(min = 200.dp),
                    onClick = { capitalizationConfirm = true },
                    content = { Text("Капіталізувати") }
                )
                ElevatedButton(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    onClick = { capitalizationStarConfirm = true },
                    content = {
                        Icon(Images.Stars, contentDescription = null)
                    }
                )
            }
            if (capitalizationConfirm) {
                AlertDialog(
                    title = { Text(text = "Капіталізація") },
                    text = { Text(text = "Сума капіталізації: ${state.capitalization()}") },
                    onDismissRequest = { capitalizationConfirm = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                store.dispatch(AppAction.CapitalizeFunds)
                                capitalizationConfirm = false
                            }
                        ) { Text("Капіталізувати") }
                    },
                    dismissButton = {
                        TextButton(onClick = { capitalizationConfirm = false }) { Text("Відміна") }
                    }
                )
            }
            if (capitalizationStarConfirm) {
                AlertDialog(
                    title = { Text(text = "Капіталізація") },
                    text = { Text(text = "Сума капіталізації(${state.config.fundStartRate}%): ${state.capitalizationStart()}") },
                    onDismissRequest = { capitalizationStarConfirm = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                store.dispatch(AppAction.CapitalizeStarsFunds)
                                capitalizationStarConfirm = false
                            }
                        ) { Text("Капіталізувати") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            capitalizationStarConfirm = false
                        }) { Text("Відміна") }
                    }
                )
            }
        }
    }
}

@Composable
fun ColumnScope.DetailsField(
    name: String,
    value: String,
    color: Color = Color.Unspecified,
) {
    SDetailsField(name, value, color, modifier = Modifier.fillMaxWidth())
    HorizontalDivider()
}

@Composable
fun SDetailsField(
    name: String,
    value: String,
    nameColor: Color = Color.Unspecified,
    additionalValue: List<String> = emptyList(),
    modifier: Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(8.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = nameColor,
        )
        Column {
            Text(
                text = value.splitDecimal(),
                style = MaterialTheme.typography.bodyMedium
            )
            additionalValue.forEach { item ->
                Text(
                    text = "+ ${item.splitDecimal()}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ColumnScope.PositiveField(name: String, value: String, fontSize: TextUnit = 16.sp) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(name, color = MaterialTheme.colorScheme.primary)
        Text(value.splitDecimal(), fontSize = fontSize)
    }
    HorizontalDivider()
}

@Composable
fun ColumnScope.FundsField(name: String, value: String, fontSize: TextUnit = 16.sp) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(name, color = AppTheme.colors.funds)
        Text(value.splitDecimal(), fontSize = fontSize)
    }
    HorizontalDivider()
}

@Composable
fun ColumnScope.NegativeField(name: String, value: String, fontSize: TextUnit = 16.sp) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(name, color = MaterialTheme.colorScheme.tertiary)
        Text(value.splitDecimal(), fontSize = fontSize)
    }
    HorizontalDivider()
}

@Composable
fun ColumnScope.CashFlowField(name: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        SmoothRainbowText(name, style = LocalTextStyle.current.copy(fontSize = 20.sp))
        Text(value.splitDecimal(), fontSize = 20.sp)
    }
    HorizontalDivider()
}

@Composable
fun ColumnScope.BalanceField(name: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(name)
        Text(value.splitDecimal(), fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
    }
    HorizontalDivider()
}