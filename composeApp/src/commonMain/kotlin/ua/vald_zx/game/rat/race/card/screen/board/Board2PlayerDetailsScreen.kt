package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.components.GoldRainbow
import ua.vald_zx.game.rat.race.card.components.SkittlesRainbow
import ua.vald_zx.game.rat.race.card.components.SmoothRainbowText
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Deposit
import ua.vald_zx.game.rat.race.card.resource.images.Repay
import ua.vald_zx.game.rat.race.card.screen.board.page.*
import ua.vald_zx.game.rat.race.card.shared.*
import ua.vald_zx.game.rat.race.card.splitDecimal
import ua.vald_zx.game.rat.race.card.theme.AppTheme


@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun Board2PlayerDetailsScreen(vm: BoardViewModel, scaffoldState: BottomSheetState) {
    val state by vm.uiState.collectAsState()
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
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = {
                coroutineScope.launch {
                    if (scaffoldState.currentDetent == HalfExpanded) {
                        scaffoldState.animateTo(ContentExpanded)
                    } else {
                        scaffoldState.animateTo(HalfExpanded)
                    }
                }
            }
        ) {
            Icon(
                if (scaffoldState.currentDetent == HalfExpanded) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                }, contentDescription = null
            )
        }
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
                        Text(stringResource(Res.string.cash), color = AppTheme.colors.cash)
                        Text("${player.cash} $")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(Res.string.deposit), color = MaterialTheme.colorScheme.primary)
                            Text("${player.deposit} $")
                        }
                        IconButton(
                            onClick = { bottomSheetNavigator.show(ToDepositScreen(vm)) },
                            content = {
                                Icon(Images.Deposit, contentDescription = null)
                            }
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(Res.string.loan), color = MaterialTheme.colorScheme.tertiary)
                            Text("${player.loan} $")
                        }
                        IconButton(
                            onClick = { bottomSheetNavigator.show(RepayCreditScreen(vm)) },
                            content = {
                                Icon(Images.Repay, contentDescription = null)
                            }
                        )
                    }
                }
            }
            val coroutineScope = rememberCoroutineScope()
            val titles = mutableListOf(
                stringResource(Res.string.status),
                stringResource(Res.string.business),
                stringResource(Res.string.shares),
                stringResource(Res.string.land),
                stringResource(Res.string.realEstate),
            )
//            if (player.config.hasFunds) {
//                titles += stringResource(Res.string.funds)
//            }
            val pagerState = rememberPagerState(pageCount = { titles.size })
            PrimaryScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 0.dp,
                minTabWidth = 0.dp,
                modifier = Modifier.align(Alignment.CenterHorizontally).wrapContentWidth(),
            ) {
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
                    3 -> LandPage(player)
                    4 -> EstatePage(player)
                    5 -> FundsPage(player)
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