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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.composables.core.BottomSheetState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.clickableSingle
import ua.vald_zx.game.rat.race.card.design.*
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Settings
import ua.vald_zx.game.rat.race.card.resource.images.Fly
import ua.vald_zx.game.rat.race.card.resource.images.Yacht
import ua.vald_zx.game.rat.race.card.resource.images.Estate
import ua.vald_zx.game.rat.race.card.resource.images.Flat
import ua.vald_zx.game.rat.race.card.resource.images.Car
import ua.vald_zx.game.rat.race.card.resource.images.Baby
import ua.vald_zx.game.rat.race.card.resource.images.Mariage
import ua.vald_zx.game.rat.race.card.resource.images.Work
import androidx.compose.ui.graphics.vector.ImageVector
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
            SheetHandle()
            Row(
                modifier = Modifier.height(littleDetailsHeight - 26.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PlayerHeader(player, modifier = Modifier.weight(1f))
                BalanceRow(player)
            }
            PendingSalary(vm, player)
            val pageCount = if (player.config.hasFunds) 6 else 5
            val pagerState = rememberPagerState(pageCount = { pageCount })
            TabsRow(player, pagerState.currentPage) { page ->
                coroutineScope.launch { pagerState.animateScrollToPage(page) }
            }
            DesignSheetPages(player, state.board, pagerState, Modifier.weight(1f))
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp)
                .size(36.dp)
                .clip(DesignShapes.sm)
                .background(colors.scaffold.surface3)
                .border(1.dp, colors.scaffold.outline, DesignShapes.sm)
                .clickableSingle { bottomSheetNavigator.show(OnlineSettingsScreen(vm)) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Images.Settings,
                contentDescription = stringResource(Res.string.online_settings),
                tint = colors.scaffold.onSurfaceMuted,
            )
        }
    }
}

@Composable
private fun SheetHandle() {
    Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(width = 44.dp, height = 5.dp)
                .clip(DesignShapes.full)
                .background(Design.scaffold.outlineStrong)
        )
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
private fun BalanceRow(player: Player) {
    val colors = Design.colors
    val type = Design.type
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(end = 44.dp),
    ) {
        AmountBlock(
            label = stringResource(Res.string.total_assets),
            amount = player.total(),
            color = colors.scaffold.brass,
        )
        AmountBlock(
            label = stringResource(Res.string.cash_flow),
            amount = player.cashFlow(),
            color = if (player.cashFlow() >= 0) Design.semantic.positive.edge else Design.semantic.negative.edge,
        )
    }
}

@Composable
private fun AmountBlock(label: String, amount: Long, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.End) {
        Text(label, style = Design.type.micro, color = Design.scaffold.onSurfaceMuted)
        Text(
            text = amount.splitDecimal(),
            style = Design.type.amountLg,
            color = color,
            maxLines = 1,
            softWrap = false,
        )
    }
}

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
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) { page ->
        when (page) {
            0 -> DesignStatePage(player, board)
            1 -> DesignBusinessPage(player)
            2 -> DesignSharesPage(player)
            3 -> DesignLandPage(player)
            4 -> DesignEstatePage(player)
            5 -> DesignFundsPage(player)
        }
    }
}

@Composable
internal fun DesignPlayerStatePageForTest(player: Player, board: Board) = DesignStatePage(player, board)

@Composable
private fun DesignStatePage(player: Player, board: Board) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    ) {
        PossessionsGrid(player)
        ConditionsBlock(player, board)
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
            )
            ValueField(
                label = stringResource(Res.string.loan),
                amount = -player.loan,
                tone = Design.semantic.negative,
                modifier = Modifier.weight(1f),
            )
        }
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
    }
}

@Composable
private fun PossessionsGrid(player: Player) {
    val config = player.config
    val items = listOf(
        Possession(stringResource(Res.string.work), Images.Work, player.card.salary, 0, player.businesses.any { it.type == BusinessType.WORK }),
        Possession(stringResource(Res.string.marriage), Images.Mariage, 0, 0, player.isMarried),
        Possession(stringResource(Res.string.kids), Images.Baby, player.babies * config.babyCost, player.babies, player.babies > 0),
        Possession(stringResource(Res.string.car), Images.Car, player.cars * config.carCost, player.cars, player.cars > 0),
        Possession(stringResource(Res.string.apartment), Images.Flat, player.apartment * config.apartmentCost, player.apartment, player.apartment > 0),
        Possession(stringResource(Res.string.estate), Images.Estate, player.cottage * config.cottageCost, player.cottage, player.cottage > 0),
        Possession(stringResource(Res.string.yacht), Images.Yacht, player.yacht * config.yachtCost, player.yacht, player.yacht > 0),
        Possession(stringResource(Res.string.plane), Images.Fly, player.flight * config.flightCost, player.flight, player.flight > 0),
    )
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { PossessionTile(it) }
    }
}

private data class Possession(
    val label: String,
    val icon: ImageVector,
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
            .width(80.dp)
            .clip(DesignShapes.sm)
            .background(if (possession.owned) colors.scaffold.surface2 else colors.scaffold.surface1)
            .border(
                width = 1.dp,
                color = if (possession.owned) colors.scaffold.accentDim else colors.scaffold.outline,
                shape = DesignShapes.sm,
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            imageVector = possession.icon,
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
        if (possession.price != 0L) {
            Text(
                text = possession.price.splitDecimal(),
                style = Design.type.monoMeta,
                color = if (possession.owned) colors.scaffold.brass else colors.scaffold.onSurfaceMuted,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

@Composable
private fun ConditionsBlock(player: Player, board: Board) {
    when (player.location.level.toLayer()) {
        BoardLayer.INNER -> {
            val conditions = board.outerCircleConditions
            ConditionsCard(stringResource(Res.string.outer_circle_conditions)) {
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
            ConditionsCard(stringResource(Res.string.victory_conditions)) {
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
private fun ConditionsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    val colors = Design.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DesignShapes.md)
            .background(colors.scaffold.surface2)
            .border(1.dp, colors.scaffold.outline, DesignShapes.md)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
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
