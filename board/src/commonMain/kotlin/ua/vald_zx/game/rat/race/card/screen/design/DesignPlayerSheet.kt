package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.composables.core.BottomSheetState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.clickableSingle
import ua.vald_zx.game.rat.race.card.design.*
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Deposit
import ua.vald_zx.game.rat.race.card.resource.images.Repay
import ua.vald_zx.game.rat.race.card.resource.images.Settings
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.verticalScroll
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.screen.board.*
import ua.vald_zx.game.rat.race.card.screen.board.page.*
import ua.vald_zx.game.rat.race.card.shared.*
import ua.vald_zx.game.rat.race.card.splitDecimal

private const val SHEET_SCREEN_SHARE = 0.72f

@Composable
fun DesignPlayerSheet(vm: BoardViewModel, scaffoldState: BottomSheetState) {
    val state by vm.uiState.collectAsState()
    val player = state.player
    val colors = Design.colors
    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    val coroutineScope = rememberCoroutineScope()

    val screenHeight = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.height.toDp()
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(screenHeight * SHEET_SCREEN_SHARE)
            .levelSheet(colors, DesignShapes.sheetTop)
            .clip(DesignShapes.sheetTop)
            .background(colors.scaffold.surface1)
            .border(1.dp, colors.scaffold.outline, DesignShapes.sheetTop)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                if (scaffoldState.currentDetent != ContentExpanded) {
                    coroutineScope.launch { scaffoldState.animateTo(ContentExpanded) }
                }
            }
            .padding(horizontal = 16.dp),
    ) {
        Column(Modifier.fillMaxHeight()) {
            SheetHandle(
                isCollapsed = scaffoldState.currentDetent == HalfExpanded,
                onToggle = {
                    coroutineScope.launch {
                        scaffoldState.animateTo(
                            if (scaffoldState.currentDetent == HalfExpanded) ContentExpanded else HalfExpanded
                        )
                    }
                },
                onOpenSettings = { bottomSheetNavigator.show(OnlineSettingsScreen(vm)) },
            )
            Row(
                modifier = Modifier.height(collapsedSheetContentHeight - 40.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PlayerHeader(player, modifier = Modifier.weight(1f))
                BalanceRow(
                    player = player,
                    onRepay = { bottomSheetNavigator.show(RepayCreditScreen(vm)) },
                )
            }
            PendingSalary(vm, player)
            val pageCount = if (player.config.hasFunds) 6 else 5
            val pagerState = rememberPagerState(pageCount = { pageCount })
            TabsRow(player, pagerState.currentPage) { page ->
                coroutineScope.launch { pagerState.animateScrollToPage(page) }
            }
            DesignSheetPages(
                player = player,
                board = state.board,
                pagerState = pagerState,
                modifier = Modifier.weight(1f),
                onDeposit = { bottomSheetNavigator.show(ToDepositScreen(vm)) },
            )
        }
    }
}

@Composable
private fun SheetHandle(
    isCollapsed: Boolean,
    onToggle: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth().height(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(width = 44.dp, height = 5.dp)
                .clip(DesignShapes.full)
                .background(Design.scaffold.outlineStrong)
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(36.dp)
                .clip(DesignShapes.sm)
                .background(Design.scaffold.surface3)
                .border(1.dp, Design.scaffold.outline, DesignShapes.sm)
                .clickableSingle(onClick = onToggle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isCollapsed) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(
                    if (isCollapsed) Res.string.expand else Res.string.collapse
                ),
                tint = Design.scaffold.onSurfaceMuted,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(36.dp)
                .clip(DesignShapes.sm)
                .background(Design.scaffold.surface3)
                .border(1.dp, Design.scaffold.outline, DesignShapes.sm)
                .clickableSingle(onClick = onOpenSettings),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Images.Settings,
                contentDescription = stringResource(Res.string.online_settings),
                tint = Design.scaffold.onSurfaceMuted,
            )
        }
    }
}

@Composable
private fun PlayerHeader(player: Player, modifier: Modifier = Modifier) {
    val colors = Design.colors
    val type = Design.type
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(DesignShapes.full)
                .background(Color(player.attrs.color))
                .border(2.dp, colors.scaffold.onSurface, DesignShapes.full),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = player.card.name.take(1).uppercase(),
                style = type.subtitle,
                color = colors.scaffold.onFill,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = player.card.name,
                    style = type.title,
                    color = colors.scaffold.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                YouChip()
            }
            Text(
                text = player.card.profession,
                style = type.body,
                color = colors.scaffold.onSurfaceMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun YouChip() {
    val colors = Design.colors
    Box(
        modifier = Modifier
            .clip(DesignShapes.xs)
            .background(colors.scaffold.onSurface)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(stringResource(Res.string.you), style = Design.type.micro, color = colors.scaffold.background)
    }
}

@Composable
internal fun BalanceRow(player: Player, onRepay: () -> Unit = {}) {
    val colors = Design.colors
    val type = Design.type
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(end = 44.dp),
    ) {
        AmountBlock(
            label = stringResource(Res.string.total_assets),
            amount = player.total(),
            color = colors.scaffold.brass,
            changes = player.lastTotals,
        )
        AmountBlock(
            label = stringResource(Res.string.cash_flow),
            amount = player.cashFlow(),
            color = if (player.cashFlow() >= 0) Design.semantic.positive.edge else Design.semantic.negative.edge,
            changes = player.lastCashFlows,
        )
        if (player.loan > 0) {
            LoanBlock(player.loan, onRepay)
        }
    }
}

@Composable
private fun LoanBlock(amount: Long, onRepay: () -> Unit) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = stringResource(Res.string.loan),
            style = Design.type.micro,
            color = Design.scaffold.onSurfaceMuted,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = amount.splitDecimal(),
                style = Design.type.amountMd,
                color = Design.semantic.negative.edge,
                maxLines = 1,
                softWrap = false,
            )
            DesignIconButton(
                icon = Images.Repay,
                contentDescription = stringResource(Res.string.repay_action),
                onClick = onRepay,
            )
        }
    }
}

@Composable
private fun AmountBlock(
    label: String,
    amount: Long,
    color: androidx.compose.ui.graphics.Color,
    changes: List<Long>,
) {
    Column(horizontalAlignment = Alignment.End) {
        Text(label, style = Design.type.micro, color = Design.scaffold.onSurfaceMuted)
        Text(
            text = amount.splitDecimal(),
            style = Design.type.amountMd,
            color = color,
            maxLines = 1,
            softWrap = false,
        )
        RecentChanges(changes)
    }
}

@Composable
private fun RecentChanges(changes: List<Long>) {
    val recent = changes.takeLast(RECENT_CHANGES)
    if (recent.isEmpty()) return
    Column(horizontalAlignment = Alignment.End) {
        recent.forEach { change ->
            Text(
                text = change.formatSigned(),
                style = Design.type.monoMeta.copy(lineHeight = 11.sp),
                color = if (change >= 0) {
                    Design.scaffold.accentDim
                } else {
                    Design.semantic.negative.edge
                },
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

private const val RECENT_CHANGES = 3

@Composable
private fun PendingSalary(vm: BoardViewModel, player: Player) {
    val pending = player.salaryPosition ?: return
    val colors = Design.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(DesignShapes.md)
            .background(colors.scaffold.surface2)
            .border(1.dp, colors.scaffold.outline, DesignShapes.md)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.salary),
                style = Design.type.label,
                color = colors.scaffold.onSurface,
            )
            Text(
                text = "${stringResource(Res.string.cell)} $pending",
                style = Design.type.monoMeta,
                color = colors.scaffold.onSurfaceMuted,
            )
        }
        BrassToken(stringResource(Res.string.salary), player.cashFlow())
    }
}

@Composable
private fun TabsRow(player: Player, selected: Int, onSelect: (Int) -> Unit) {
    val titles = mutableListOf(
        stringResource(Res.string.status),
        stringResource(Res.string.business),
        stringResource(Res.string.shares),
        stringResource(Res.string.land),
        stringResource(Res.string.realEstate),
    )
    if (player.config.hasFunds) titles += stringResource(Res.string.funds)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        titles.forEachIndexed { index, title ->
            DesignChip(text = title, selected = index == selected) { onSelect(index) }
        }
    }
}

@Composable
internal fun DesignSheetPages(
    player: Player,
    board: Board,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    onDeposit: () -> Unit = {},
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) { page ->
        when (page) {
            0 -> DesignStatePage(player, board, onDeposit)
            1 -> DesignBusinessPage(player)
            2 -> DesignSharesPage(player, board)
            3 -> DesignLandPage(player)
            4 -> DesignEstatePage(player)
            5 -> DesignFundsPage(player)
        }
    }
}

@Composable
internal fun DesignPlayerStatePageForTest(
    player: Player,
    board: Board,
    onDeposit: () -> Unit = {},
) = DesignStatePage(player, board, onDeposit)

@Composable
private fun DesignStatePage(player: Player, board: Board, onDeposit: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ValueField(
                label = stringResource(Res.string.cash),
                amount = player.cash,
                tone = Design.semantic.cash,
                modifier = Modifier.weight(1f),
            )
            ValueField(
                label = stringResource(Res.string.cash_flow),
                amount = player.cashFlow(),
                tone = if (player.cashFlow() >= 0) Design.semantic.positive else Design.semantic.negative,
                signed = true,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ValueField(
                label = stringResource(Res.string.deposit),
                amount = player.deposit,
                tone = Design.semantic.cash,
                modifier = Modifier.weight(1f),
                trailing = if (player.cash > 0) {
                    {
                        DesignIconButton(
                            icon = Images.Deposit,
                            contentDescription = stringResource(Res.string.deposit_to_deposit),
                            onClick = onDeposit,
                        )
                    }
                } else {
                    null
                },
            )
            ValueField(
                label = stringResource(Res.string.loan),
                amount = -player.loan,
                tone = Design.semantic.negative,
                modifier = Modifier.weight(1f),
            )
        }
        ValueField(
            label = stringResource(Res.string.loanLimit),
            amount = board.loanLimit,
            tone = Design.semantic.action,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ValueField(
                label = stringResource(Res.string.active_profit),
                amount = player.activeProfit(),
                tone = Design.semantic.business,
                signed = true,
                modifier = Modifier.weight(1f),
            )
            ValueField(
                label = stringResource(Res.string.passive_profit),
                amount = player.passiveProfit(),
                tone = Design.semantic.business,
                signed = true,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ValueField(
                label = stringResource(Res.string.total_profit),
                amount = player.totalProfit(),
                tone = Design.semantic.positive,
                signed = true,
                modifier = Modifier.weight(1f),
            )
            ValueField(
                label = stringResource(Res.string.funds),
                amount = player.fundAmount(),
                tone = Design.semantic.funds,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ValueField(
                label = stringResource(Res.string.credit_expenses),
                amount = -player.creditExpenses(),
                tone = Design.semantic.negative,
                modifier = Modifier.weight(1f),
            )
            ValueField(
                label = stringResource(Res.string.total_expenses),
                amount = -player.totalExpenses(),
                tone = Design.semantic.expenses,
                modifier = Modifier.weight(1f),
            )
        }
        PossessionsGrid(player)
        ConditionsBlock(player, board)
    }
}

@Composable
private fun PossessionsGrid(player: Player) {
    val config = player.config
    val items = listOf(
        Possession(stringResource(Res.string.work), Res.drawable.asset_work, player.card.salary, 0, player.businesses.any { it.type == BusinessType.WORK }),
        Possession(stringResource(Res.string.marriage), Res.drawable.cell_love, 0, 0, player.isMarried),
        Possession(stringResource(Res.string.kids), Res.drawable.cell_child, player.babies * config.babyCost, player.babies, player.babies > 0),
        Possession(stringResource(Res.string.car), Res.drawable.asset_car, player.cars * config.carCost, player.cars, player.cars > 0),
        Possession(stringResource(Res.string.apartment), Res.drawable.asset_apartment, player.apartment * config.apartmentCost, player.apartment, player.apartment > 0),
        Possession(stringResource(Res.string.estate), Res.drawable.asset_estate, player.cottage * config.cottageCost, player.cottage, player.cottage > 0),
        Possession(stringResource(Res.string.yacht), Res.drawable.asset_yacht, player.yacht * config.yachtCost, player.yacht, player.yacht > 0),
        Possession(stringResource(Res.string.plane), Res.drawable.asset_plane, player.flight * config.flightCost, player.flight, player.flight > 0),
    )
    SectionCard(stringResource(Res.string.status)) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items.forEach { PossessionTile(it) }
        }
    }
}

private data class Possession(
    val label: String,
    val icon: DrawableResource,
    val price: Long,
    val count: Long,
    val owned: Boolean,
)

@Composable
private fun PossessionTile(possession: Possession) {
    val colors = Design.colors
    val ink = if (possession.owned) colors.scaffold.onSurface else colors.scaffold.onSurfaceMuted
    Column(
        modifier = Modifier
            .size(POSSESSION_TILE_WIDTH, POSSESSION_TILE_HEIGHT)
            .clip(DesignShapes.sm)
            .background(if (possession.owned) colors.scaffold.surface2 else colors.scaffold.surface1)
            .border(
                width = 1.dp,
                color = if (possession.owned) colors.scaffold.accentDim else colors.scaffold.outline,
                shape = DesignShapes.sm,
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Icon(
            painter = painterResource(possession.icon),
            contentDescription = possession.label,
            tint = ink,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = if (possession.count > 0) "${possession.label} ×${possession.count}" else possession.label,
            style = Design.type.micro,
            color = ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = if (possession.price != 0L) possession.price.splitDecimal() else "",
            style = Design.type.monoMeta,
            color = if (possession.owned) colors.scaffold.brass else colors.scaffold.onSurfaceMuted,
            maxLines = 1,
            softWrap = false,
        )
    }
}

private val POSSESSION_TILE_WIDTH = 80.dp
private val POSSESSION_TILE_HEIGHT = 84.dp

@Composable
private fun ConditionsBlock(player: Player, board: Board) {
    when (player.location.level.toLayer()) {
        BoardLayer.INNER -> {
            val conditions = board.outerCircleConditions
            SectionCard(stringResource(Res.string.outer_circle_conditions)) {
                Condition(
                    done = player.cashFlow() >= conditions.minimumCashFlow,
                    text = stringResource(
                        Res.string.cash_flow_progress,
                        player.cashFlow().splitDecimal(),
                        conditions.minimumCashFlow.splitDecimal(),
                    ),
                )
                if (conditions.apartmentRequired) {
                    Condition(player.apartment > 0, stringResource(Res.string.apartment_required))
                }
                if (conditions.carRequired) {
                    Condition(player.cars > 0, stringResource(Res.string.car_required))
                }
                Condition(
                    done = player.balance() >= conditions.minimumAccountBalance,
                    text = stringResource(
                        Res.string.account_balance_progress,
                        player.balance().splitDecimal(),
                        conditions.minimumAccountBalance.splitDecimal(),
                    ),
                )
            }
        }

        BoardLayer.OUTER -> {
            val conditions = board.victoryConditions
            SectionCard(stringResource(Res.string.victory_conditions)) {
                if (conditions.dreamRequired) {
                    Condition(
                        done = player.selectedDreamId != null &&
                                player.selectedDreamId in player.purchasedDreamIds,
                        text = board.dreamById(player.selectedDreamId)?.name
                            ?: stringResource(Res.string.choose_dream_on_board),
                    )
                }
                if (conditions.planeRequired) {
                    Condition(player.flight > 0, stringResource(Res.string.plane))
                }
                if (conditions.estateRequired) {
                    Condition(player.cottage > 0, stringResource(Res.string.estate))
                }
                Condition(
                    done = player.balance() >= conditions.minimumAccountBalance,
                    text = stringResource(
                        Res.string.victory_account_balance_value,
                        conditions.minimumAccountBalance.splitDecimal(),
                    ),
                )
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    val colors = Design.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DesignShapes.lg)
            .background(colors.scaffold.surface2)
            .border(1.dp, colors.scaffold.outline, DesignShapes.lg)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = {
            Text(text = title, style = Design.type.label, color = colors.scaffold.onSurface)
            content()
        },
    )
}

@Composable
private fun Condition(done: Boolean, text: String) {
    val colors = Design.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (done) "✓" else "○",
            style = Design.type.body,
            color = if (done) colors.scaffold.accent else colors.scaffold.onSurfaceMuted,
        )
        Text(
            text = text,
            style = Design.type.body,
            color = if (done) colors.scaffold.onSurface else colors.scaffold.onSurfaceMuted,
        )
    }
}
