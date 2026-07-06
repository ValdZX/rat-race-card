package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.shared.BoardLayer
import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark

@Composable
fun BoxWithConstraintsScope.BoardPanel(
    isVertical: Boolean,
    vm: BoardViewModel,
) {
    val maxWidth = maxWidth
    val maxHeight = maxHeight
    val density = LocalDensity.current
    val isDark by LocalThemeIsDark.current

    Box(
        modifier = Modifier.fillMaxSize()
            .shadow(30.dp, shape = RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(
                boardBackgroundBrush(
                    isDark = isDark,
                    radius = with(density) { min(maxWidth, maxHeight).toPx() }
                )
            )
    ) {
        BoardRoutes(
            isVertical = isVertical,
            size = DpSize(maxWidth, maxHeight),
            vm = vm,
        )
    }
}

@Composable
private fun BoxScope.BoardRoutes(
    isVertical: Boolean,
    size: DpSize,
    vm: BoardViewModel,
) {
    val outRoute = boardLayers.layers[BoardLayer.OUTER] ?: return
    val inRoute = boardLayers.layers[BoardLayer.INNER] ?: return
    val actualOutRoute = remember(isVertical) {
        if (isVertical) outRoute.rotate() else outRoute
    }

    Places(
        layer = BoardLayer.OUTER,
        size = size,
        route = actualOutRoute,
        vm = vm,
    )

    val outSpotSize = size.width / actualOutRoute.horizontalCells
    val inPadding = outSpotSize / 3
    val actualInRoute = remember(isVertical) {
        if (isVertical) inRoute.rotate() else inRoute
    }
    val inBoardSize = DpSize(
        width = size.width - outSpotSize * 4 - inPadding * 2,
        height = size.height - outSpotSize * 4 - inPadding * 2,
    )

    Places(
        layer = BoardLayer.INNER,
        size = inBoardSize,
        route = actualInRoute,
        vm = vm,
    )

    CardDecks(
        size = cardDeckAreaSize(inBoardSize, actualInRoute),
        vm = vm,
    )
}

private fun cardDeckAreaSize(
    inBoardSize: DpSize,
    inRoute: BoardRoute,
): DpSize {
    val inSpotSize = inBoardSize.width / inRoute.horizontalCells
    val cardsPadding = inSpotSize
    return DpSize(
        width = inBoardSize.width - inSpotSize * 4 - cardsPadding * 2,
        height = inBoardSize.height - inSpotSize * 4 - cardsPadding * 2,
    )
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
