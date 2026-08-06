package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.runtime.collectAsState
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.design.DesignBoardRoutes
import ua.vald_zx.game.rat.race.card.screen.design.CellFocus
import ua.vald_zx.game.rat.race.card.screen.design.TokenBubbleState
import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark

@Composable
fun BoxWithConstraintsScope.rememberBoardLayout(vm: BoardViewModel, isVertical: Boolean): BoardLayout? {
    val maxWidth = maxWidth
    val maxHeight = maxHeight
    val board = vm.uiState.collectAsState().value.board
    val layers = remember(board.tracks, board.trackDefinitions, board.generatedPlaces) { boardLayersOf(board) }
    return remember(isVertical, maxWidth, maxHeight, layers) {
        val contentSize = DpSize(maxWidth, maxHeight).boardContentSize()
        calculateBoardLayout(
            boardSize = contentSize,
            isVertical = isVertical,
            layers = layers,
        )
    }
}

internal fun DpSize.boardContentSize(): DpSize {
    if (width <= 0.dp || height <= 0.dp) return DpSize.Zero
    val availableWidth = (width - boardContentPadding * 2).coerceAtLeast(0.dp)
    val availableHeight = (height - boardContentPadding * 2).coerceAtLeast(0.dp)
    val scale = minOf(availableWidth / width, availableHeight / height).coerceIn(0f, 1f)
    return DpSize(width * scale, height * scale)
}

private val boardContentPadding = 12.dp

@Composable
fun BoxWithConstraintsScope.BoardPanel(
    vm: BoardViewModel,
    layout: BoardLayout,
    focus: CellFocus,
    bubble: TokenBubbleState,
) {
    val maxWidth = maxWidth
    val maxHeight = maxHeight
    val density = LocalDensity.current
    val isDark by LocalThemeIsDark.current
    val boardBrush = boardBackgroundBrush(
        isDark = isDark,
        radius = with(density) { min(maxWidth, maxHeight).toPx() },
    )
    val background = if (designV2Enabled.value) {
        Modifier.background(Design.scaffold.surface4)
    } else {
        Modifier.background(boardBrush)
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .shadow(30.dp, shape = RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .then(background)
    ) {
        if (designV2Enabled.value) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(layout.routes.first().size)
                    .background(boardBrush)
            )
        }
        BoardRoutes(
            layout = layout,
            vm = vm,
            focus = focus,
            bubble = bubble,
        )
    }
}

@Composable
private fun BoxScope.BoardRoutes(
    layout: BoardLayout,
    vm: BoardViewModel,
    focus: CellFocus,
    bubble: TokenBubbleState,
) {
    if (designV2Enabled.value) {
        DesignBoardRoutes(layout = layout, vm = vm, focus = focus, bubble = bubble)
    } else {
        LegacyBoardRoutes(layout = layout, vm = vm)
    }
}

private fun boardBackgroundBrush(
    isDark: Boolean,
    radius: Float,
): Brush {
    return if (isDark) {
        Brush.radialGradient(
            0.0f to Color(0xFF31250E),
            0.4f to Color(0xFF282215),
            0.6f to Color(0xFF2F200C),
            1.0f to Color(0xFF320202),
            radius = radius,
            tileMode = TileMode.Repeated
        )
    } else {
        Brush.radialGradient(
            0.0f to Color(0xFFD7C228),
            0.4f to Color(0xFFF8C954),
            0.6f to Color(0xFFFFB370),
            1.0f to Color(0xFFFFB370),
            radius = radius,
            tileMode = TileMode.Repeated
        )
    }
}
