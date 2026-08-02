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
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.design.*
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.screen.board.BoardLayout
import ua.vald_zx.game.rat.race.card.screen.board.Place
import ua.vald_zx.game.rat.race.card.screen.board.RouteLayout
import ua.vald_zx.game.rat.race.card.screen.board.SalaryScreen
import ua.vald_zx.game.rat.race.card.shared.BoardLayer
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.cashFlow
import ua.vald_zx.game.rat.race.card.shared.fundAmount
import ua.vald_zx.game.rat.race.card.shared.moveTo

private val trackGap = 1.5.dp

internal const val FOCUSED_CELL_Z = 10f

private val minExpandedHeight = 34.dp

private fun Int.onTrack(layout: RouteLayout): Int =
    moveTo(this, layout.layer.cellCount, layout.route.offset)

@Composable
fun BoxScope.DesignBoardTracks(vm: BoardViewModel, layout: BoardLayout, focus: CellFocus) {
    val state by vm.uiState.collectAsState()
    val playerLevel = state.player.location.level

    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    val onSalary = { bottomSheetNavigator.show(SalaryScreen(vm)) }
    val onStart = { vm.capitalizeFunds() }

    DesignTrack(
        layout = layout.outerRoute,
        surface = if (playerLevel == layout.outerRoute.layer.level) CellSurface.Tile else CellSurface.Engraved,
        player = state.player,
        focus = focus,
        onSalaryClick = onSalary,
        onStartClick = onStart,
    )
    DesignTrack(
        layout = layout.innerRoute,
        surface = if (playerLevel == layout.innerRoute.layer.level) CellSurface.Tile else CellSurface.Engraved,
        player = state.player,
        focus = focus,
        onSalaryClick = onSalary,
        onStartClick = onStart,
    )
}

@Stable
class CellFocus {
    private var hovered by mutableStateOf<Pair<Int, Int>?>(null)
    private var tapped by mutableStateOf<Pair<Int, Int>?>(null)

    internal var tapCount by mutableStateOf(0)
        private set

    var expandedBox by mutableStateOf<DpSize?>(null)
        private set

    val key: Pair<Int, Int>? get() = hovered ?: tapped

    internal fun reportExpandedBox(size: DpSize) {
        expandedBox = size
    }

    internal fun hover(cell: Pair<Int, Int>?) {
        hovered = cell
    }

    fun tap(cell: Pair<Int, Int>) {
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
                            heldCell(it, point, size, focus.key, focus.expandedBox ?: expandedCellBox(it))
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
    held: Pair<Int, Int>?,
    openBox: DpSize = expandedCellBox(route),
): Pair<Int, Int>? {
    if (held?.first != route.layer.level) return null
    val place = route.places.firstOrNull { it.index == held.second }?.place ?: return null
    val local = toLocal(route, point, boardSize)
    return held.takeIf { focusBounds(route, place, openBox).holds(local.first, local.second) }
}

internal fun Density.cellUnder(
    route: RouteLayout,
    point: Offset,
    boardSize: IntSize,
): Pair<Int, Int>? {
    val local = toLocal(route, point, boardSize)
    return route.places
        .firstOrNull { cellBounds(route, it.place, it.place.size).holds(local.first, local.second) }
        ?.let { route.layer.level to it.index }
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
    val fromCenterX = place.offset.x + place.size.width / 2 - layout.size.width / 2
    val fromCenterY = place.offset.y + place.size.height / 2 - layout.size.height / 2
    return if (place.location.side.isHorizontal) {
        val shift = openBox.height / 2 + layout.cellSize.height / 2 + 4.dp
        DpOffset(0.dp, if (fromCenterY > 0.dp) -shift else shift)
    } else {
        val shift = openBox.width / 2 + layout.cellSize.width / 2 + 4.dp
        DpOffset(if (fromCenterX > 0.dp) -shift else shift, 0.dp)
    }
}

private fun parkedTokenBounds(layout: RouteLayout, place: Place, openBox: DpSize): DpRect {
    val float = tokenFloat(layout, place, openBox)
    val left = place.offset.x + (place.size.width - layout.cellSize.width) / 2 + float.x
    val top = place.offset.y + (place.size.height - layout.cellSize.height) / 2 + float.y
    return DpRect(left, top, left + layout.cellSize.width, top + layout.cellSize.height)
}

private fun focusBounds(layout: RouteLayout, place: Place, openBox: DpSize): DpRect {
    val expanded = cellBounds(layout, place, openBox)
    val parked = parkedTokenBounds(layout, place, openBox)
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
) = DesignTrack(layout, surface, player = null, focus = focus, onSalaryClick = null, onStartClick = null)

@Composable
private fun BoxScope.DesignTrack(
    layout: RouteLayout,
    surface: CellSurface,
    player: Player?,
    focus: CellFocus,
    onSalaryClick: (() -> Unit)?,
    onStartClick: (() -> Unit)?,
) {
    val colors = Design.colors
    val blendBedEdges = layout.layer != BoardLayer.OUTER
    val live = surface == CellSurface.Tile
    val bedAlpha by animateFloatAsState(if (live) 1f else 0.55f, label = "TrackBed")
    val level = layout.layer.level
    val expandedBox = expandedCellBox(layout)
    val expandedIcon = remember(layout, expandedBox) {
        val widest = layout.places.maxOf { minOf(it.place.size.width, it.place.size.height) }
        (widest * COLLAPSED_ICON_FRACTION)
            .coerceAtLeast(expandedIconSize)
            .coerceAtMost(expandedBox.height - 10.dp)
    }
    val focusedIndex = if (live && focus.key?.first == level) focus.key?.second else null

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
            .alpha(bedAlpha)
            .zIndex(if (focusedIndex != null) FOCUSED_CELL_Z else 0f)
    ) {
        layout.places.forEach { (index, place) ->
            TrackCell(
                layout, place, index, live, surface, player, focus,
                expandedBox, expandedIcon, index == focusedIndex,
                onSalaryClick, onStartClick,
            )
        }
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
) {
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val labelStyle = expandedLabelStyle()
    val level = layout.layer.level
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
    val expandedWidth = remember(label, expanded, expandedIcon) {
        if (!expanded) 0.dp
        else with(density) {
            measurer.measure(label, labelStyle).size.width.toDp()
        } + expandedLabelChrome(expandedIcon)
    }
    val openBox = DpSize(maxOf(expandedBox.width, expandedWidth), expandedBox.height)
    LaunchedEffect(expanded, openBox) {
        if (expanded) focus.reportExpandedBox(openBox)
    }
    val width by animateDpAsState(
        targetValue = if (expanded) openBox.width else place.size.width - trackGap,
        label = "CellWidth",
    )
    val height by animateDpAsState(
        targetValue = if (expanded) expandedBox.height else place.size.height - trackGap,
        label = "CellHeight",
    )
    val bounds = cellBounds(layout, place, DpSize(width, height))
    DesignPlaceCell(
        type = place.type,
        surface = surface,
        label = label,
        expanded = expanded,
        expandedIcon = expandedIcon,
        onTap = if (canExpand) {
            { focus.tap(level to index) }
        } else {
            null
        },
        compact = minOf(place.size.width, place.size.height) < 30.dp,
        waitingAmount = waitingAmount,
        onClick = when {
            salaryHere -> onSalaryClick
            startHere -> onStartClick
            else -> null
        },
        modifier = Modifier
            .offset(bounds.left, bounds.top)
            .size(width, height)
            .zIndex(if (expanded) 2f else if (waitingAmount != null) 1f else 0f),
    )
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
}
