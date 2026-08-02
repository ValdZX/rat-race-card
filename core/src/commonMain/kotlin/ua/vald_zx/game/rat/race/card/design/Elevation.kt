package ua.vald_zx.game.rat.race.card.design

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.lennartegb.shadows.boxShadow

/**
 * Цоколь: суцільна тінь без розмиття, зміщена вниз. Саме вона робить об'єкт
 * «предметом на столі», а не пласким прямокутником, і не коштує Wasm жодного блюру.
 */
fun Modifier.plinth(
    color: Color,
    depth: Dp,
    shape: Shape,
): Modifier = boxShadow(
    blurRadius = 0.dp,
    color = color,
    offset = DpOffset(0.dp, depth),
    shape = shape,
    clip = false,
)

private fun Modifier.softShadow(
    color: Color,
    offsetY: Dp,
    blur: Dp,
    spread: Dp,
    shape: Shape,
): Modifier = boxShadow(
    blurRadius = blur,
    color = color,
    spreadRadius = spread,
    offset = DpOffset(0.dp, offsetY),
    shape = shape,
    clip = false,
)

/** L1 — ложе треку: втоплена поверхня. */
fun Modifier.levelBed(colors: DesignColors, shape: Shape): Modifier = boxShadow(
    blurRadius = 0.dp,
    color = colors.scaffold.outline,
    offset = DpOffset(0.dp, 2.dp),
    shape = shape,
    inset = true,
    clip = false,
)

/** L2 — клітинка або чіп: тонкий цоколь у колірі кромки. */
fun Modifier.levelTile(edge: Color, shape: Shape): Modifier = plinth(edge, 2.dp, shape)

/** L3 — картка або фішка. */
fun Modifier.levelCard(colors: DesignColors, shape: Shape): Modifier = this
    .softShadow(colors.scaffold.shadow, offsetY = 6.dp, blur = 8.dp, spread = (-4).dp, shape = shape)
    .plinth(colors.scaffold.outline, 3.dp, shape)

/** L4 — афорданс: об'єкт стоїть на цоколі й кличе натиснути. */
fun Modifier.levelAffordance(colors: DesignColors, shape: Shape, depth: Dp = 4.dp): Modifier = this
    .softShadow(colors.scaffold.shadow, offsetY = 10.dp, blur = 14.dp, spread = (-8).dp, shape = shape)
    .plinth(colors.scaffold.accentDim, depth, shape)

/** L5 — шторка або діалог: тінь угору, бо об'єкт наїжджає знизу. */
fun Modifier.levelSheet(colors: DesignColors, shape: Shape): Modifier =
    softShadow(colors.scaffold.shadow, offsetY = (-12).dp, blur = 16.dp, spread = (-8).dp, shape = shape)
