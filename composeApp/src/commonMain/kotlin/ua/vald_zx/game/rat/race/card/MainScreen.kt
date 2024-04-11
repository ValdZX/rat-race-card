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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.vectorResource
import rat_race_card.composeapp.generated.resources.Res
import rat_race_card.composeapp.generated.resources.ic_dark_mode
import rat_race_card.composeapp.generated.resources.ic_light_mode
import ua.vald_zx.game.rat.race.card.beans.BusinessType
import ua.vald_zx.game.rat.race.card.components.SmoothRainbowText
import ua.vald_zx.game.rat.race.card.logic.AppState
import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark

class MainScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        val state by store.observeState().collectAsState()
        BottomSheetNavigator {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(16.dp),
            ) {
                var isDark by LocalThemeIsDark.current
                val icon = remember(isDark) {
                    if (isDark) Res.drawable.ic_light_mode
                    else Res.drawable.ic_dark_mode
                }
                IconButton(
                    modifier = Modifier.align(Alignment.TopEnd),
                    onClick = { isDark = !isDark },
                    content = {
                        Icon(vectorResource(icon), contentDescription = null)
                    }
                )
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.status(), style = MaterialTheme.typography.headlineMedium)
                    PositiveField("Активний прибуток", state.activeProfit().toString())
                    PositiveField("Пасивний прибуток", state.passiveProfit().toString())
                    PositiveField("Загальний прибуток", state.totalProfit().toString())
                    NegativeField("Витрати по кредиту", state.creditExpenses().toString())
                    NegativeField("Загальні витрати", state.totalExpenses().toString())
                    CashFlowField("Cash Flow", state.cashFlow().toString())
                    BalanceField("Готівка", state.cash.toString())
                    BalanceField("Депозит", state.deposit.toString())
                    BalanceField("Кредит", state.loan.toString())
                    val coroutineScope = rememberCoroutineScope()
                    val titles = listOf("Стан", "Бізнеси", "Акції")
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
                        }
                    }
                    val bottomSheetNavigator = LocalBottomSheetNavigator.current
                    ElevatedButton(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            .widthIn(min = 200.dp),
                        onClick = {
                            bottomSheetNavigator.show(ActionsScreen())
                        },
                        content = {
                            Text("Дія")
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun StatePage(state: AppState) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(state = rememberScrollState())) {
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
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.business.filter { it.type != BusinessType.WORK }) { business ->
                Column {
                    val title = when(business.type) {
                        BusinessType.WORK -> ""
                        BusinessType.SMALL -> "Малий бізнес"
                        BusinessType.MEDIUM -> "Середній бізнес"
                        BusinessType.LARGE -> "Крупний бізнес"
                    }
                    Text("$title: ${business.name}", style = MaterialTheme.typography.titleSmall)
                    Row {
                        SDetailsField("Ціна", business.price.toString())
                        SDetailsField("Прибуток", business.profit.toString())
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
                Column {
                    Text(shares.type.name, style = MaterialTheme.typography.titleSmall)
                    Row {
                        SDetailsField("Кількість", shares.count.toString())
                        SDetailsField("Ціна покупки", shares.price.toString())
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun ColumnScope.DetailsField(name: String, value: String) {
    SDetailsField(name, value)
    HorizontalDivider()
}

@Composable
fun SDetailsField(name: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = value.splitDecimal(),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun ColumnScope.PositiveField(name: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(name, color = MaterialTheme.colorScheme.primary)
        Text(value.splitDecimal(), fontSize = 16.sp)
    }
    HorizontalDivider()
}

@Composable
fun ColumnScope.NegativeField(name: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(name, color = MaterialTheme.colorScheme.tertiary)
        Text(value.splitDecimal(), fontSize = 16.sp)
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
        Text(value.splitDecimal(), fontSize = 16.sp)
    }
    HorizontalDivider()
}