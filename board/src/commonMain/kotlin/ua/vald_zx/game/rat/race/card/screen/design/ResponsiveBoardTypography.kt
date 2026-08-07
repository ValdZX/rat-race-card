package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

internal fun responsiveBoardTextScale(actualSize: Dp, baselineSize: Dp): Float =
    (actualSize.value / baselineSize.value).coerceIn(1f, 2f)

internal fun TextStyle.scaleForBoard(scale: Float): TextStyle = copy(
    fontSize = fontSize * scale,
    lineHeight = lineHeight * scale,
)
