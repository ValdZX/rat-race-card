package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.design.*
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.screen.board.BoardLayout
import ua.vald_zx.game.rat.race.card.screen.board.RouteLayout
import ua.vald_zx.game.rat.race.card.screen.board.SalaryScreen
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.cashFlow
import ua.vald_zx.game.rat.race.card.shared.fundAmount
import ua.vald_zx.game.rat.race.card.shared.moveTo

private val trackGap = 1.5.dp

private fun Int.onTrack(layout: RouteLayout): Int =
    moveTo(this, layout.layer.cellCount, layout.route.offset)

/**
 * Обидва треки. Живий отримує плитки, минулий або ще недосяжний — гравіювання.
 * Різниця в характері, а не в яскравості: у чорно-білому знімку теж видно,
 * де гравець зараз ходить.
 */
@Composable
fun BoxScope.DesignBoardTracks(vm: BoardViewModel, layout: BoardLayout) {
    val state by vm.uiState.collectAsState()
    val playerLevel = state.player.location.level

    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    val onSalary = { bottomSheetNavigator.show(SalaryScreen(vm)) }
    val onStart = { vm.capitalizeFunds() }

    // Розкрита клітинка одна на всю дошку: два підписи водночас перекривали б
    // одне одного й самі клітинки.
    val focus = rememberCellFocus()

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

/**
 * Фокус клітинки. Тап тримається кілька секунд і сам згасає — інакше підпис
 * лишався б висіти, бо на дошці немає «порожнього місця», куди тапнути.
 */
@Stable
internal class CellFocus {
    var key by mutableStateOf<Pair<Int, Int>?>(null)
        private set
    private var fromTap by mutableStateOf(false)

    fun set(key: Pair<Int, Int>, focused: Boolean, byTap: Boolean) {
        if (focused) {
            this.key = key
            fromTap = byTap
        } else if (this.key == key && !fromTap) {
            this.key = null
        }
    }

    fun release() {
        if (fromTap) key = null
    }

    val holdsTap: Boolean get() = fromTap && key != null
}

@Composable
internal fun rememberCellFocus(): CellFocus {
    val focus = remember { CellFocus() }
    LaunchedEffect(focus.key, focus.holdsTap) {
        if (focus.holdsTap) {
            delay(2500)
            focus.release()
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
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val labelStyle = expandedLabelStyle()
    val level = layout.layer.level

    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .size(layout.size)
            .alpha(bedAlpha)
            // Не clip: розкрита клітинка навмисно вилазить за межі треку.
            .zIndex(if (focus.key?.first == level) 1f else 0f)
            .background(colors.scaffold.surface4, DesignShapes.md)
            .levelBed(colors, DesignShapes.md)
            .border(
                width = if (live) 2.dp else 1.dp,
                color = if (live) colors.scaffold.outlineStrong else colors.scaffold.outline,
                shape = DesignShapes.md,
            )
    ) {
        layout.places.forEach { (index, place) ->
            val onThisLayer = live && player != null
            val salaryHere = onThisLayer && place.type == PlaceType.Salary &&
                    listOfNotNull(player.salaryPosition, player.investmentPosition)
                        .any { it.onTrack(layout) == index }
            val startHere = onThisLayer && place.type == PlaceType.Start &&
                    player.startCapitalization?.position?.onTrack(layout) == index

            // Клітинка мовчить, поки на неї не навели чи не тапнули: підпис у
            // 15dp однаково нечитабельний, тому його показує розкриття.
            val label = place.type.shortLabel()
            val expanded = focus.key == level to index
            // Ширину під підпис міряємо, а не вгадуємо: слово мусить лягти в
            // один рядок за будь-якої довжини й локалі.
            val expandedWidth = remember(label, expanded) {
                if (!expanded) 0.dp
                else with(density) { measurer.measure(label, labelStyle).size.width.toDp() } + 44.dp
            }
            val width by animateDpAsState(
                targetValue = if (expanded) maxOf(place.size.width, expandedWidth) else place.size.width - trackGap,
                label = "CellWidth",
            )
            val height by animateDpAsState(
                targetValue = if (expanded) maxOf(place.size.height, 34.dp) else place.size.height - trackGap,
                label = "CellHeight",
            )
            DesignPlaceCell(
                type = place.type,
                surface = surface,
                label = label,
                expanded = expanded,
                onFocusChange = { focused, byTap -> focus.set(level to index, focused, byTap) },
                // Цоколь на дрібній клітинці не видно, а тінь із власною формою —
                // окремий шар на кожну з ~90 клітинок.
                compact = minOf(place.size.width, place.size.height) < 30.dp,
                waitingAmount = when {
                    salaryHere -> player.cashFlow()
                    startHere -> player.fundAmount()
                    else -> null
                },
                onClick = when {
                    salaryHere -> onSalaryClick
                    startHere -> onStartClick
                    else -> null
                },
                modifier = Modifier
                    // Клітинка росте від свого центру, тому зсув компенсує приріст.
                    .offset(
                        x = place.offset.x + (place.size.width - width) / 2,
                        y = place.offset.y + (place.size.height - height) / 2,
                    )
                    .size(width, height)
                    .zIndex(if (expanded) 1f else 0f),
            )
        }
    }
}

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
