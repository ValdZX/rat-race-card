package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.composables.core.BottomSheetState
import kotlinx.coroutines.launch
import ua.vald_zx.game.rat.race.card.components.FundsField
import ua.vald_zx.game.rat.race.card.components.GoldRainbow
import ua.vald_zx.game.rat.race.card.components.SkittlesRainbow
import ua.vald_zx.game.rat.race.card.components.SmoothRainbowText
import ua.vald_zx.game.rat.race.card.logic.BoardState
import ua.vald_zx.game.rat.race.card.screen.board.page.BusinessListPage
import ua.vald_zx.game.rat.race.card.screen.board.page.FundsPage
import ua.vald_zx.game.rat.race.card.screen.board.page.SharesPage
import ua.vald_zx.game.rat.race.card.screen.board.page.StatePage
import ua.vald_zx.game.rat.race.card.screen.second.BuyFundScreen
import ua.vald_zx.game.rat.race.card.shared.*
import ua.vald_zx.game.rat.race.card.splitDecimal
import ua.vald_zx.game.rat.race.card.theme.AppTheme


@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun Board2PlayerDetailsScreen(state: BoardState, scaffoldState: BottomSheetState) {
    val player = state.player
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
                        text = player.status(),
                        maxLines = 2,
                        style = MaterialTheme.typography.titleLarge,
                        overflow = TextOverflow.Ellipsis,
                    )
                    SmoothRainbowText(
                        player.total().splitDecimal(),
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
                        Text("${player.cashFlow()}")
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Готівка", color = AppTheme.colors.cash)
                        Text("${player.cash} $")
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Депозит", color = MaterialTheme.colorScheme.primary)
                        Text("${player.deposit} $")
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Кредит", color = MaterialTheme.colorScheme.tertiary)
                        Text("${player.loan} $")
                    }
                }
            }
            if (player.config.hasFunds) {
                FundsField(
                    name = "Фонди",
                    value = "${player.fundAmount()} $"
                ) {
                    bottomSheetNavigator.show(BuyFundScreen())
                }
            }
            val coroutineScope = rememberCoroutineScope()
            val titles = mutableListOf("Стан", "Бізнес", "Акції")
            if (player.config.hasFunds) {
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
                    0 -> StatePage(player)
                    1 -> BusinessListPage(player)
                    2 -> SharesPage(player)
                    3 -> FundsPage(player)
                }
            }
        }
    }
}

fun Player.status(): String {
    return when {
        businesses.any { it.type == BusinessType.SMALL } -> "${card.name} - Підприємець"
        businesses.any { it.type == BusinessType.MEDIUM } -> "${card.name} - Бізнесмен"
        totalExpenses() > 1_000_000 -> "${card.name} - Мільйонер"
        else -> card.name
    }
}