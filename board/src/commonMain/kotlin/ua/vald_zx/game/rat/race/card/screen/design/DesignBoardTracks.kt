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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
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

enum class FocusSource { Cell, Token, Tap }

@Stable
class CellFocus {
    var key by mutableStateOf<Pair<Int, Int>?>(null)
        private set
    var source by mutableStateOf(FocusSource.Cell)
        private set
    private var fromTap by mutableStateOf(false)
    private var pendingRelease by mutableStateOf(false)

    internal var epoch by mutableStateOf(0)
        private set

    fun set(key: Pair<Int, Int>, focused: Boolean, source: FocusSource) {
        epoch++
        if (focused) {
            this.key = key
            this.source = source
            fromTap = source == FocusSource.Tap
            pendingRelease = false
        } else if (this.key == key && !fromTap) {
            pendingRelease = true
        }
    }

    internal fun clearHover() {
        if (pendingRelease && !fromTap) {
            key = null
            pendingRelease = false
        }
    }

    internal fun releaseTap() {
        if (fromTap) {
            key = null
            fromTap = false
        }
    }

    internal val holdsTap: Boolean get() = fromTap && key != null
    internal val awaitsRelease: Boolean get() = pendingRelease
}

private const val HOVER_GRACE_MS = 140L

@Composable
fun rememberCellFocus(): CellFocus {
    val focus = remember { CellFocus() }
    LaunchedEffect(focus.key, focus.holdsTap) {
        if (focus.holdsTap) {
            delay(2500)
            focus.releaseTap()
        }
    }
    LaunchedEffect(focus.epoch) {
        if (focus.awaitsRelease) {
            delay(HOVER_GRACE_MS)
            focus.clearHover()
        }
    }
    return focus
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
                drawFadedBed(
                    color = colors.scaffold.surface4,
                    fade = layout.cellSize.width.toPx(),
                )
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
    val width by animateDpAsState(
        targetValue = if (expanded) maxOf(expandedBox.width, expandedWidth) else place.size.width - trackGap,
        label = "CellWidth",
    )
    val height by animateDpAsState(
        targetValue = if (expanded) expandedBox.height else place.size.height - trackGap,
        label = "CellHeight",
    )
    DesignPlaceCell(
        type = place.type,
        surface = surface,
        label = label,
        expanded = expanded,
        expandedIcon = expandedIcon,
        onFocusChange = if (canExpand) {
            { focused, byTap ->
                focus.set(level to index, focused, if (byTap) FocusSource.Tap else FocusSource.Cell)
            }
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
            .offset(
                x = (place.offset.x + (place.size.width - width) / 2)
                    .coerceIn(0.dp, (layout.size.width - width).coerceAtLeast(0.dp)),
                y = (place.offset.y + (place.size.height - height) / 2)
                    .coerceIn(0.dp, (layout.size.height - height).coerceAtLeast(0.dp)),
            )
            .size(width, height)
            .zIndex(if (expanded) 2f else if (waitingAmount != null) 1f else 0f),
    )
}

private fun DrawScope.drawFadedBed(color: Color, fade: Float) {
    val w = size.width
    val h = size.height
    val edge = fade.coerceAtMost(minOf(w, h) / 2f)
    if (edge <= 0f) {
        drawRect(color)
        return
    }
    drawRect(color, topLeft = Offset(edge, edge), size = Size(w - edge * 2, h - edge * 2))
    val transparent = Color.Transparent
    drawRect(
        brush = Brush.verticalGradient(listOf(transparent, color), 0f, edge),
        topLeft = Offset(edge, 0f),
        size = Size(w - edge * 2, edge),
    )
    drawRect(
        brush = Brush.verticalGradient(listOf(color, transparent), h - edge, h),
        topLeft = Offset(edge, h - edge),
        size = Size(w - edge * 2, edge),
    )
    drawRect(
        brush = Brush.horizontalGradient(listOf(transparent, color), 0f, edge),
        topLeft = Offset(0f, edge),
        size = Size(edge, h - edge * 2),
    )
    drawRect(
        brush = Brush.horizontalGradient(listOf(color, transparent), w - edge, w),
        topLeft = Offset(w - edge, edge),
        size = Size(edge, h - edge * 2),
    )
    listOf(
        Offset(edge, edge) to Offset(0f, 0f),
        Offset(w - edge, edge) to Offset(w - edge, 0f),
        Offset(edge, h - edge) to Offset(0f, h - edge),
        Offset(w - edge, h - edge) to Offset(w - edge, h - edge),
    ).forEach { (center, corner) ->
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(color, transparent),
                center = center,
                radius = edge,
            ),
            topLeft = corner,
            size = Size(edge, edge),
        )
    }
}

@Composable
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
