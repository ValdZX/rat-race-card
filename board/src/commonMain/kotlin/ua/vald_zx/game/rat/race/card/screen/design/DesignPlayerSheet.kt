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

    // Висота шторки — частка екрана, а не весь екран: дошка позаду мусить
    // лишатися видимою. Вкладки беруть решту після шапки, тож довгі списки
    // скроляться всередині, а не ростуть у висоту й не стрибають між табами.
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
            // Один рядок замість двох: у згорнутому стані шторки висота
            // фіксована, і двоповерхова шапка різала суми.
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
            DesignSheetPages(player, pagerState, Modifier.weight(1f))
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
            label = stringResource(Res.string.cash),
            amount = player.cash,
            color = colors.scaffold.onSurface,
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

/**
 * Пейджер веде стан вкладок сам: клік по чипу гортає його імперативно, а
 * підсвічений чип читається з `currentPage`. Через `LaunchedEffect` це не
 * можна — ефект перезапускає анімацію й забирає жест у списків усередині,
 * тож довгі вкладки переставали скролитись.
 */
@Composable
internal fun DesignSheetPages(
    player: Player,
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
            0 -> DesignStatePage(player)
            1 -> DesignBusinessPage(player)
            2 -> DesignSharesPage(player)
            3 -> DesignLandPage(player)
            4 -> DesignEstatePage(player)
            5 -> DesignFundsPage(player)
        }
    }
}

@Composable
internal fun DesignPlayerStatePageForTest(player: Player) = DesignStatePage(player)

@Composable
private fun DesignStatePage(player: Player) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ValueField(
                label = stringResource(Res.string.cash_flow),
                amount = player.cashFlow(),
                tone = if (player.cashFlow() >= 0) Design.semantic.positive else Design.semantic.negative,
                signed = true,
                modifier = Modifier.weight(1f),
            )
            ValueField(
                label = stringResource(Res.string.deposit),
                amount = player.deposit,
                tone = Design.semantic.cash,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ValueField(
                label = stringResource(Res.string.active_profit),
                amount = player.totalProfit(),
                tone = Design.semantic.business,
                signed = true,
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
                label = stringResource(Res.string.total_expenses),
                amount = -player.totalExpenses(),
                tone = Design.semantic.expenses,
                modifier = Modifier.weight(1f),
            )
            ValueField(
                label = stringResource(Res.string.funds),
                amount = player.fundAmount(),
                tone = Design.semantic.funds,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
