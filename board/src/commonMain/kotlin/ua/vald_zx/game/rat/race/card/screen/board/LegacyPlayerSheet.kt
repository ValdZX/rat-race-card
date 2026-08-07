package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import ua.vald_zx.game.rat.race.card.components.GoldRainbow
import ua.vald_zx.game.rat.race.card.components.SkittlesRainbow
import ua.vald_zx.game.rat.race.card.components.SmoothRainbowText
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Deposit
import ua.vald_zx.game.rat.race.card.resource.images.Repay
import ua.vald_zx.game.rat.race.card.resource.images.Settings
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.screen.board.page.*
import ua.vald_zx.game.rat.race.card.shared.*
import ua.vald_zx.game.rat.race.card.splitDecimal
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import androidx.compose.ui.text.intl.Locale

@Composable
fun LegacyPlayerSheet(vm: BoardViewModel, scaffoldState: BottomSheetState) {
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
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp),
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
                modifier = Modifier.height(collapsedSheetContentHeight),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
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
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        player.lastTotals.asReversed().forEach { totalDiff ->
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            SmoothRainbowText(
                                text = "Cash Flow",
                                rainbow = SkittlesRainbow,
                                style = LocalTextStyle.current.copy(fontSize = 16.sp),
                            )
                            Text("${player.cashFlow()}")
                        }
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            player.lastCashFlows.asReversed().forEach { diffs ->
                                val total = if (diffs > 0) {
                                    "+${diffs}"
                                } else {
                                    diffs.toString()
                                }
                                Text(
                                    text = total,
                                    style = LocalTextStyle.current.copy(fontSize = 10.sp),
                                    color = if (diffs > 0) {
                                        AppTheme.colors.cash
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    },
                                    lineHeight = 10.sp
                                )
                            }
                        }
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
                        if (player.cash > 0) {
                            IconButton(
                                onClick = { bottomSheetNavigator.show(ToDepositScreen(vm)) },
                                content = {
                                    Icon(Images.Deposit, contentDescription = null)
                                }
                            )
                        }
                    }
                    if (player.loan > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(Res.string.loan), color = MaterialTheme.colorScheme.tertiary)
                                Text("${player.loan} $")
                                player.lastLoans.asReversed().forEach { change ->
                                    Text(
                                        text = if (change > 0) "+$change" else change.toString(),
                                        style = LocalTextStyle.current.copy(fontSize = 10.sp),
                                        color = if (change > 0) AppTheme.colors.cash else MaterialTheme.colorScheme.error,
                                        lineHeight = 10.sp,
                                    )
                                }
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
            }
            val coroutineScope = rememberCoroutineScope()
            val titles = mutableListOf(
                stringResource(Res.string.status),
                stringResource(Res.string.business),
                stringResource(Res.string.shares),
                stringResource(Res.string.land),
                stringResource(Res.string.realEstate),
            )
            if (player.config.hasFunds) {
                titles += stringResource(Res.string.funds)
            }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 72.dp),
            ) { page ->
                when (page) {
                    0 -> StatePage(
                        player = player,
                        selectedDream = state.board.dreamById(player.selectedDreamId, Locale.current.language),
                        outerCircleConditions = state.board.outerCircleConditions,
                        victoryConditions = state.board.victoryConditions,
                        loanLimit = state.board.loanLimit,
                        inflation = state.board.inflation,
                        economy = state.board.economy,
                    )
                    1 -> BusinessListPage(player)
                    2 -> SharesPage(player, state.board)
                    3 -> LandPage(player)
                    4 -> EstatePage(player)
                    5 -> FundsPage(player)
                }
            }
        }
        FilledTonalIconButton(
            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
            onClick = { bottomSheetNavigator.show(OnlineSettingsScreen(vm)) },
        ) {
            Icon(
                imageVector = Images.Settings,
                contentDescription = stringResource(Res.string.online_settings),
            )
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
