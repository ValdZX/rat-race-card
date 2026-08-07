package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.intl.Locale
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.ColumnScope
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.design.*
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.logic.players
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.screen.board.BoardLayout
import ua.vald_zx.game.rat.race.card.screen.board.Place
import ua.vald_zx.game.rat.race.card.screen.board.RouteLayout
import ua.vald_zx.game.rat.race.card.screen.board.SalaryScreen
import ua.vald_zx.game.rat.race.card.shared.TrackId
import ua.vald_zx.game.rat.race.card.shared.Dream
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.cashFlow
import ua.vald_zx.game.rat.race.card.shared.dreamById
import ua.vald_zx.game.rat.race.card.shared.canSelectDream
import ua.vald_zx.game.rat.race.card.shared.dreamSelectors
import ua.vald_zx.game.rat.race.card.shared.fundAmount
import ua.vald_zx.game.rat.race.card.shared.moveTo
import ua.vald_zx.game.rat.race.card.splitDecimal
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape

private val trackGap = 1.5.dp
private val tokenRowGap = 2.dp
private val tokenCellGap = 4.dp

internal const val FOCUSED_CELL_Z = 10f

private val minExpandedHeight = 34.dp
private val dreamCellWidth = 220.dp
private val cellTextBaseline = 30.dp

internal class DreamCellContext(
    val dream: (String) -> Dream?,
    val selectors: (String) -> List<Player>,
    val canSelect: (String) -> Boolean,
    val currentPlayerId: String,
    val purchasedDreamIds: Set<String>,
    val onSelect: (String) -> Unit,
)

private fun Int.onTrack(layout: RouteLayout): Int =
    moveTo(this, layout.route.places.size, layout.route.offset)

@Composable
fun BoxScope.DesignBoardTracks(
    vm: BoardViewModel,
    layout: BoardLayout,
    focus: CellFocus,
    bubble: TokenBubbleState,
) {
    val state by vm.uiState.collectAsState()
    val playerTrackId = state.player.location.trackId

    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    val onSalary = {
        if (state.player.investmentPosition == null) {
            vm.takeSalary()
        } else {
            bottomSheetNavigator.show(SalaryScreen(vm))
        }
    }
    val onStart = { vm.capitalizeFunds() }
    val locale = Locale.current.language
    val allPlayers by players.collectAsState()
    val dreams = DreamCellContext(
        dream = { dreamId -> state.board.dreamById(dreamId, locale) },
        selectors = { dreamId -> allPlayers.dreamSelectors(dreamId) },
        canSelect = { dreamId -> state.board.canSelectDream(dreamId, state.player.id, allPlayers) },
        currentPlayerId = state.player.id,
        purchasedDreamIds = state.board.purchasedDreamIds,
        onSelect = vm::selectDream,
    )

    layout.routes.forEach { route ->
        DesignTrack(
            layout = route,
            surface = if (playerTrackId == route.trackId) CellSurface.Tile else CellSurface.Engraved,
            player = state.player,
            focus = focus,
            onSalaryClick = onSalary,
            onStartClick = onStart,
            dreams = dreams,
            inflationPercent = state.board.economy.cumulativeInflationPercent
                .takeIf { state.board.inflation.enabled },
        ) { DesignPlayerTokens(vm = vm, layout = route, focus = focus, bubble = bubble) }
    }
}

@Stable
class CellFocus {
    private var hovered by mutableStateOf<Pair<TrackId, Int>?>(null)
    private var tapped by mutableStateOf<Pair<TrackId, Int>?>(null)

    internal var tapCount by mutableStateOf(0)
        private set

    var expandedBox by mutableStateOf<DpSize?>(null)
        private set

    private var tokenCountKey: Pair<TrackId, Int>? = null
    private var reportedTokenCount = 1

    val key: Pair<TrackId, Int>? get() = hovered ?: tapped

    internal fun reportExpandedBox(size: DpSize) {
        expandedBox = size
    }

    internal fun reportTokenCount(cell: Pair<TrackId, Int>, count: Int) {
        tokenCountKey = cell
        reportedTokenCount = count
    }

    internal fun tokenCountFor(cell: Pair<TrackId, Int>?) =
        reportedTokenCount.takeIf { cell != null && tokenCountKey == cell } ?: 1

    internal fun hover(cell: Pair<TrackId, Int>?) {
        hovered = cell
    }

    fun tap(cell: Pair<TrackId, Int>) {
        tapped = cell
        tapCount++
    }

    internal fun releaseTap() {
        tapped = null
    }
}

private const val TAP_HOLD_MS = 2500L

@Composable
fun rememberCellFocus(): CellFocus {
    val focus = remember { CellFocus() }
    LaunchedEffect(focus.tapCount) {
        if (focus.tapCount > 0) {
            delay(TAP_HOLD_MS)
            focus.releaseTap()
        }
    }
    return focus
}

fun Modifier.cellFocusTracking(routes: List<RouteLayout>, focus: CellFocus): Modifier =
    pointerInput(routes) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.changes.any { it.pressed }) continue
                val point = event.changes.firstOrNull()?.position
                focus.hover(
                    if (event.type == PointerEventType.Exit || point == null) {
                        null
                    } else {
                        routes.firstNotNullOfOrNull {
                            heldCell(
                                it,
                                point,
                                size,
                                focus.key,
                                focus.expandedBox ?: expandedCellBox(it),
                                focus.tokenCountFor(focus.key),
                            )
                        } ?: routes.firstNotNullOfOrNull { cellUnder(it, point, size) }
                    }
                )
            }
        }
    }

internal fun Density.heldCell(
    route: RouteLayout,
    point: Offset,
    boardSize: IntSize,
    held: Pair<TrackId, Int>?,
    openBox: DpSize = expandedCellBox(route),
    tokenCount: Int = 1,
): Pair<TrackId, Int>? {
    if (held?.first != route.trackId) return null
    val place = route.places.firstOrNull { it.index == held.second }?.place ?: return null
    val local = toLocal(route, point, boardSize)
    return held.takeIf {
        focusBounds(route, place, openBox, tokenCount).holds(local.first, local.second)
    }
}

internal fun Density.cellUnder(
    route: RouteLayout,
    point: Offset,
    boardSize: IntSize,
): Pair<TrackId, Int>? {
    val local = toLocal(route, point, boardSize)
    return route.places
        .firstOrNull { cellBounds(route, it.place, it.place.size).holds(local.first, local.second) }
        ?.let { route.trackId to it.index }
}

private fun Density.toLocal(route: RouteLayout, point: Offset, boardSize: IntSize) = Pair(
    (point.x - (boardSize.width - route.size.width.toPx()) / 2f).toDp(),
    (point.y - (boardSize.height - route.size.height.toPx()) / 2f).toDp(),
)

private fun DpRect.holds(x: Dp, y: Dp) = x >= left && x < right && y >= top && y < bottom

internal fun cellBounds(layout: RouteLayout, place: Place, size: DpSize): DpRect {
    val left = (place.offset.x + (place.size.width - size.width) / 2)
        .coerceIn(0.dp, (layout.size.width - size.width).coerceAtLeast(0.dp))
    val top = (place.offset.y + (place.size.height - size.height) / 2)
        .coerceIn(0.dp, (layout.size.height - size.height).coerceAtLeast(0.dp))
    return DpRect(left, top, left + size.width, top + size.height)
}

internal fun tokenFloat(layout: RouteLayout, place: Place, openBox: DpSize): DpOffset {
    val centeredX = place.offset.x + (place.size.width - layout.cellSize.width) / 2
    val centeredY = place.offset.y + (place.size.height - layout.cellSize.height) / 2
    val fromCenterX = place.offset.x + place.size.width / 2 - layout.size.width / 2
    val fromCenterY = place.offset.y + place.size.height / 2 - layout.size.height / 2
    val expanded = cellBounds(layout, place, openBox)
    return if (place.location.side.isHorizontal) {
        val targetY = if (layout.isOutermost) {
            if (fromCenterY > 0.dp) {
                expanded.top - layout.cellSize.height - tokenCellGap
            } else {
                expanded.bottom + tokenCellGap
            }
        } else if (fromCenterY > 0.dp) {
            expanded.bottom + tokenCellGap
        } else {
            expanded.top - layout.cellSize.height - tokenCellGap
        }
        DpOffset(0.dp, targetY - centeredY)
    } else {
        val targetX = if (layout.isOutermost) {
            if (fromCenterX > 0.dp) {
                expanded.left - layout.cellSize.width - tokenCellGap
            } else {
                expanded.right + tokenCellGap
            }
        } else if (fromCenterX > 0.dp) {
            expanded.right + tokenCellGap
        } else {
            expanded.left - layout.cellSize.width - tokenCellGap
        }
        DpOffset(targetX - centeredX, 0.dp)
    }
}

internal fun expandedTokenOffset(
    layout: RouteLayout,
    place: Place,
    index: Int,
    count: Int,
    openBox: DpSize,
): Pair<Dp, Dp> {
    val safeCount = count.coerceAtLeast(1)
    val float = tokenFloat(layout, place, openBox)
    val centeredX = place.offset.x + (place.size.width - layout.cellSize.width) / 2 + float.x
    val centeredY = place.offset.y + (place.size.height - layout.cellSize.height) / 2 + float.y
    return if (place.location.side.isHorizontal) {
        val rowWidth = layout.cellSize.width * safeCount + tokenRowGap * (safeCount - 1)
        val rowStart = (place.offset.x + place.size.width / 2 - rowWidth / 2)
            .coerceIn(0.dp, (layout.size.width - rowWidth).coerceAtLeast(0.dp))
        Pair(rowStart + (layout.cellSize.width + tokenRowGap) * index, centeredY)
    } else {
        val rowHeight = layout.cellSize.height * safeCount + tokenRowGap * (safeCount - 1)
        val rowStart = (place.offset.y + place.size.height / 2 - rowHeight / 2)
            .coerceIn(0.dp, (layout.size.height - rowHeight).coerceAtLeast(0.dp))
        Pair(centeredX, rowStart + (layout.cellSize.height + tokenRowGap) * index)
    }
}

private fun parkedTokenBounds(
    layout: RouteLayout,
    place: Place,
    openBox: DpSize,
    tokenCount: Int,
): DpRect {
    val first = expandedTokenOffset(layout, place, 0, tokenCount, openBox)
    val last = expandedTokenOffset(layout, place, tokenCount - 1, tokenCount, openBox)
    return DpRect(
        left = minOf(first.first, last.first),
        top = minOf(first.second, last.second),
        right = maxOf(first.first, last.first) + layout.cellSize.width,
        bottom = maxOf(first.second, last.second) + layout.cellSize.height,
    )
}

private fun focusBounds(
    layout: RouteLayout,
    place: Place,
    openBox: DpSize,
    tokenCount: Int,
): DpRect {
    val expanded = cellBounds(layout, place, openBox)
    val parked = parkedTokenBounds(layout, place, openBox, tokenCount)
    return DpRect(
        left = minOf(expanded.left, parked.left),
        top = minOf(expanded.top, parked.top),
        right = maxOf(expanded.right, parked.right),
        bottom = maxOf(expanded.bottom, parked.bottom),
    )
}

@Composable
internal fun BoxScope.DesignTrackForTest(
    layout: RouteLayout,
    surface: CellSurface,
    focus: CellFocus = rememberCellFocus(),
    dreams: DreamCellContext? = null,
    tokenContent: @Composable BoxScope.() -> Unit = {},
) = DesignTrack(
    layout,
    surface,
    player = null,
    focus = focus,
    onSalaryClick = null,
    onStartClick = null,
    dreams = dreams,
    tokenContent = tokenContent,
)

@Composable
private fun BoxScope.DesignTrack(
    layout: RouteLayout,
    surface: CellSurface,
    player: Player?,
    focus: CellFocus,
    onSalaryClick: (() -> Unit)?,
    onStartClick: (() -> Unit)?,
    dreams: DreamCellContext? = null,
    inflationPercent: Long? = null,
    tokenContent: @Composable BoxScope.() -> Unit,
) {
    val colors = Design.colors
    val blendBedEdges = !layout.isOutermost
    val live = surface == CellSurface.Tile
    val bedAlpha by animateFloatAsState(if (live) 1f else 0.55f, label = "TrackBed")
    val trackId = layout.trackId
    val expandedBox = expandedCellBox(layout)
    val expandedIcon = remember(layout, expandedBox) {
        val widest = layout.places.maxOf { minOf(it.place.size.width, it.place.size.height) }
        (widest * COLLAPSED_ICON_FRACTION)
            .coerceAtLeast(expandedIconSize)
            .coerceAtMost(expandedBox.height - 10.dp)
    }
    val focusedIndex = if (live && focus.key?.first == trackId) focus.key?.second else null

    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .size(layout.size)
            .alpha(bedAlpha)
            .drawBehind {
                if (blendBedEdges) {
                    drawBlendedBed(colors.scaffold.surface4, layout.cellSize.width.toPx())
                } else {
                    drawRect(colors.scaffold.surface4)
                }
            }
    )
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .size(layout.size)
            .zIndex(if (focusedIndex != null) FOCUSED_CELL_Z else 0f)
    ) {
        layout.places.forEach { (index, place) ->
            TrackCell(
                layout, place, index, live, surface, player, focus,
                expandedBox, expandedIcon, index == focusedIndex,
                onSalaryClick, onStartClick, bedAlpha, dreams, inflationPercent,
            )
        }
        tokenContent()
    }
}

@Composable
private fun BoxScope.TrackCell(
    layout: RouteLayout,
    place: Place,
    index: Int,
    live: Boolean,
    surface: CellSurface,
    player: Player?,
    focus: CellFocus,
    expandedBox: DpSize,
    expandedIcon: Dp,
    expanded: Boolean,
    onSalaryClick: (() -> Unit)?,
    onStartClick: (() -> Unit)?,
    cellAlpha: Float,
    dreams: DreamCellContext?,
    inflationPercent: Long?,
) {
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val textScale = responsiveBoardTextScale(minOf(layout.cellSize.width, layout.cellSize.height), cellTextBaseline)
    val labelStyle = expandedLabelStyle(textScale)
    val amountStyle = Design.type.monoMeta.scaleForBoard(textScale)
    val trackId = layout.trackId
    val onThisLayer = live && player != null
    val salaryHere = onThisLayer && place.type == PlaceType.Salary &&
            listOfNotNull(player.salaryPosition, player.investmentPosition)
                .any { it.onTrack(layout) == index }
    val startHere = onThisLayer && place.type == PlaceType.Start &&
            player.startCapitalization?.position?.onTrack(layout) == index
    val waitingAmount = when {
        salaryHere -> player.cashFlow()
        startHere -> player.fundAmount()
        else -> null
    }

    val label = place.type.shortLabel()
    val canExpand = live
    val expandedWidth = remember(label, expanded, expandedIcon, waitingAmount, amountStyle) {
        if (!expanded) return@remember 0.dp
        val labelWidth = with(density) {
            measurer.measure(label, labelStyle).size.width.toDp()
        } + expandedLabelChrome(expandedIcon)
        val amountWidth = waitingAmount?.let { amount ->
            with(density) {
                measurer.measure(waitingAmountLabel(amount), amountStyle).size.width.toDp()
            } + 14.dp
        } ?: 0.dp
        maxOf(labelWidth, amountWidth)
    }
    val dream = if (expanded && place.type is PlaceType.Desire) {
        dreams?.dream(place.type.dreamId)
    } else {
        null
    }
    val dreamSelectors = dream?.let { dreams?.selectors(it.id).orEmpty() }.orEmpty()
    val dreamDetail = dreamDetail(dream, dreamSelectors, dreams, textScale)
    val openBox = if (dreamDetail != null && dream != null) {
        dreamCellSize(
            dream = dream,
            selectorCount = dreamSelectors.size,
            expandedBox = expandedBox,
            expandedIcon = expandedIcon,
            textScale = textScale,
        )
    } else {
        DpSize(maxOf(expandedBox.width, expandedWidth), expandedBox.height)
    }
    LaunchedEffect(expanded, openBox) {
        if (expanded) focus.reportExpandedBox(openBox)
    }
    val width by animateDpAsState(
        targetValue = if (expanded) openBox.width else place.size.width - trackGap,
        label = "CellWidth",
    )
    val height by animateDpAsState(
        targetValue = if (expanded) openBox.height else place.size.height - trackGap,
        label = "CellHeight",
    )
    val bounds = cellBounds(layout, place, DpSize(width, height))
    DesignPlaceCell(
        type = place.type,
        surface = surface,
        label = label,
        expanded = expanded,
        expandedIcon = expandedIcon,
        textScale = textScale,
        onTap = if (canExpand) {
            { focus.tap(trackId to index) }
        } else {
            null
        },
        compact = minOf(place.size.width, place.size.height) < 30.dp,
        waitingAmount = waitingAmount,
        inflationPercent = inflationPercent.takeIf { place.type == PlaceType.Start },
        secret = debugToolsUnlock.takeIf { place.type == PlaceType.Start },
        expandedDetail = dreamDetail,
        claimMarks = dreamClaimMarks(place.type, dreams),
        onClick = when {
            salaryHere -> onSalaryClick
            startHere -> onStartClick
            else -> null
        },
        modifier = Modifier
            .offset(bounds.left, bounds.top)
            .size(width, height)
            .alpha(cellAlpha)
            .zIndex(if (expanded) 2f else if (waitingAmount != null) 1f else 0f),
    )
}

private fun dreamClaimMarks(type: PlaceType, dreams: DreamCellContext?): List<Color> {
    if (dreams == null || type !is PlaceType.Desire) return emptyList()
    return dreams.selectors(type.dreamId).map { Color(it.attrs.color) }
}

@Composable
private fun dreamDetail(
    dream: Dream?,
    selectors: List<Player>,
    dreams: DreamCellContext?,
    textScale: Float,
): (@Composable ColumnScope.() -> Unit)? {
    if (dream == null || dreams == null) return null
    val isSelected = selectors.any { it.id == dreams.currentPlayerId }
    val isPurchased = dream.id in dreams.purchasedDreamIds
    val selectLabel = stringResource(Res.string.select_action)
    val chosenByLabel = stringResource(Res.string.dream_chosen_by)
    val disabledReason = when {
        isPurchased -> stringResource(Res.string.dream_already_purchased)
        isSelected -> stringResource(Res.string.dream_already_selected)
        else -> stringResource(Res.string.dream_taken_by_other)
    }
    val colors = Design.colors
    val cellStyle = Design.type.cellSm.scaleForBoard(textScale)
    val metaStyle = Design.type.monoMeta.scaleForBoard(textScale)
    return {
        Text(
            text = dream.name,
            style = cellStyle,
            color = colors.scaffold.onFill,
        )
        Text(
            text = dream.price.splitDecimal(),
            style = metaStyle,
            color = colors.scaffold.onFill,
            maxLines = 1,
            softWrap = false,
        )
        if (dream.description.isNotBlank()) {
            Text(
                text = dream.description,
                style = metaStyle,
                color = colors.scaffold.onFill,
            )
        }
        if (selectors.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "$chosenByLabel:",
                    style = metaStyle,
                    color = colors.scaffold.onFill,
                    maxLines = 1,
                    softWrap = false,
                )
                selectors.forEach { selector ->
                    DreamSelectorChip(selector, textScale)
                }
            }
        }
        DesignButton(
            text = selectLabel,
            enabled = !isSelected && dreams.canSelect(dream.id),
            disabledReason = disabledReason,
            height = 26.dp * textScale,
            fontSize = 11.sp * textScale,
            padding = 10.dp * textScale,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = { dreams.onSelect(dream.id) },
        )
    }
}

@Composable
private fun dreamCellSize(
    dream: Dream,
    selectorCount: Int,
    expandedBox: DpSize,
    expandedIcon: Dp,
    textScale: Float,
): DpSize {
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val scaledDreamCellWidth = dreamCellWidth * textScale
    val contentWidth = scaledDreamCellWidth - 16.dp * textScale
    val maxWidth = with(density) { contentWidth.roundToPx() }
    val cellStyle = Design.type.cellSm.scaleForBoard(textScale)
    val metaStyle = Design.type.monoMeta.scaleForBoard(textScale)
    val headerStyle = expandedLabelStyle(textScale)
    fun measuredHeight(text: String, maxLines: Int = Int.MAX_VALUE): Dp = with(density) {
        measurer.measure(
            text = text,
            style = if (maxLines == 1) headerStyle else metaStyle,
            maxLines = maxLines,
            constraints = Constraints(maxWidth = maxWidth),
        ).size.height.toDp()
    }
    val headerHeight = maxOf(expandedIcon, measuredHeight("Dream", maxLines = 1))
    val nameHeight = with(density) {
        measurer.measure(
            text = dream.name,
            style = cellStyle,
            constraints = Constraints(maxWidth = maxWidth),
        ).size.height.toDp()
    }
    val detailHeights = buildList {
        add(headerHeight)
        add(nameHeight)
        add(measuredHeight(dream.price.toString()))
        if (dream.description.isNotBlank()) add(measuredHeight(dream.description))
        if (selectorCount > 0) {
            val lineHeight = measuredHeight("Selected by")
            add(lineHeight * (selectorCount + 1) + 2.dp * textScale * selectorCount)
        }
        add(26.dp * textScale)
    }
    val contentHeight = detailHeights.fold(12.dp * textScale) { total, height -> total + height } +
            4.dp * textScale * (detailHeights.size - 1)
    return DpSize(
        width = maxOf(expandedBox.width, scaledDreamCellWidth),
        height = maxOf(expandedBox.height, contentHeight),
    )
}

@Composable
private fun DreamSelectorChip(player: Player, textScale: Float) {
    val colors = Design.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp * textScale),
    ) {
        Box(
            modifier = Modifier
                .size(7.dp * textScale)
                .background(Color(player.attrs.color), CircleShape)
                .border(1.dp, colors.scaffold.onFill, CircleShape)
        )
        Text(
            text = player.card.name.ifBlank { player.card.profession },
            style = Design.type.monoMeta.scaleForBoard(textScale),
            color = colors.scaffold.onFill,
            maxLines = 1,
            softWrap = false,
        )
    }
}

private fun DrawScope.drawBlendedBed(color: Color, fade: Float) {
    val w = size.width
    val h = size.height
    val edge = fade.coerceAtMost(minOf(w, h) / 2f)
    if (edge <= 0f) {
        drawRect(color)
        return
    }
    val clear = color.copy(alpha = 0f)
    drawRect(
        brush = Brush.verticalGradient(listOf(clear, color), 0f, edge),
        topLeft = Offset(edge, 0f),
        size = Size(w - edge * 2, edge),
    )
    drawRect(
        brush = Brush.verticalGradient(listOf(color, clear), h - edge, h),
        topLeft = Offset(edge, h - edge),
        size = Size(w - edge * 2, edge),
    )
    drawRect(
        brush = Brush.horizontalGradient(listOf(clear, color), 0f, edge),
        topLeft = Offset(0f, edge),
        size = Size(edge, h - edge * 2),
    )
    drawRect(
        brush = Brush.horizontalGradient(listOf(color, clear), w - edge, w),
        topLeft = Offset(w - edge, edge),
        size = Size(edge, h - edge * 2),
    )
    listOf(
        Offset(edge, edge) to Offset.Zero,
        Offset(w - edge, edge) to Offset(w - edge, 0f),
        Offset(edge, h - edge) to Offset(0f, h - edge),
        Offset(w - edge, h - edge) to Offset(w - edge, h - edge),
    ).forEach { (center, corner) ->
        drawRect(
            brush = Brush.radialGradient(listOf(color, clear), center = center, radius = edge),
            topLeft = corner,
            size = Size(edge, edge),
        )
    }
    val seamGuard = 1f
    drawRect(
        color = color,
        topLeft = Offset(edge - seamGuard, edge - seamGuard),
        size = Size(w - (edge - seamGuard) * 2, h - (edge - seamGuard) * 2),
    )
}

internal fun expandedCellBox(layout: RouteLayout) = DpSize(
    width = layout.cellSize.width * 2,
    height = maxOf(minExpandedHeight, layout.cellSize.height * 2),
)

@Composable
private fun PlaceType.shortLabel(): String = when (this) {
    PlaceType.Salary -> stringResource(Res.string.salary)
    PlaceType.Start -> stringResource(Res.string.start)
    PlaceType.Chance -> stringResource(Res.string.chance)
    PlaceType.Store -> stringResource(Res.string.store)
    PlaceType.Business -> stringResource(Res.string.business)
    PlaceType.BigBusiness -> stringResource(Res.string.business)
    PlaceType.Deputy -> stringResource(Res.string.deputy)
    PlaceType.Expenses -> stringResource(Res.string.expenses)
    PlaceType.Shopping -> stringResource(Res.string.shopping)
    PlaceType.Rest -> stringResource(Res.string.rest)
    PlaceType.Resignation -> stringResource(Res.string.exaltation)
    PlaceType.Divorce -> stringResource(Res.string.divorce)
    PlaceType.Bankruptcy -> stringResource(Res.string.bankruptcy)
    PlaceType.TaxInspection -> stringResource(Res.string.tax_inspection)
    PlaceType.Child -> stringResource(Res.string.child)
    PlaceType.Love -> stringResource(Res.string.love)
    is PlaceType.Desire -> stringResource(Res.string.desire)
    is PlaceType.Custom -> type.value
}
