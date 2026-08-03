package ua.vald_zx.game.rat.race.card.design

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ua.vald_zx.game.rat.race.card.resources.*
import kotlin.math.floor

private const val DRIFT_PERIOD = 8
private const val DRIFT_CYCLE_MS = 90_000

private val driftIcons
    @Composable get() = listOf(
        painterResource(Res.drawable.cell_salary),
        painterResource(Res.drawable.cell_business),
        painterResource(Res.drawable.cell_big_business),
        painterResource(Res.drawable.cell_chance),
        painterResource(Res.drawable.cell_store),
        painterResource(Res.drawable.cell_shopping),
        painterResource(Res.drawable.cell_expenses),
        painterResource(Res.drawable.cell_dream),
        painterResource(Res.drawable.cell_rest),
        painterResource(Res.drawable.cell_love),
        painterResource(Res.drawable.cell_child),
        painterResource(Res.drawable.asset_car),
        painterResource(Res.drawable.asset_apartment),
        painterResource(Res.drawable.asset_yacht),
        painterResource(Res.drawable.asset_plane),
        painterResource(Res.drawable.cell_start),
    )

@Composable
fun DesignIconDrift(modifier: Modifier = Modifier) {
    val icons = driftIcons
    val palette = driftPalette()
    val slots = remember(icons.size, palette.size) { driftSlots(icons.size, palette.size) }
    val transition = rememberInfiniteTransition(label = "IconDrift")
    val travel by transition.animateFloat(
        initialValue = 0f,
        targetValue = DRIFT_PERIOD.toFloat(),
        animationSpec = infiniteRepeatable(tween(DRIFT_CYCLE_MS, easing = LinearEasing)),
        label = "IconDriftTravel",
    )
    Canvas(modifier) {
        drawDriftLayer(icons, palette, slots, tile = 88.dp, travel = -travel, alpha = 0.04f, scale = 0.36f)
        drawDriftLayer(icons, palette, slots, tile = 128.dp, travel = travel, alpha = 0.09f, scale = 0.4f)
    }
}

@Composable
private fun driftPalette(): List<Color> {
    val semantic = Design.semantic
    return listOf(
        semantic.salary.edge,
        semantic.business.edge,
        semantic.chance.edge,
        semantic.store.edge,
        semantic.dream.edge,
        semantic.love.edge,
        semantic.rest.edge,
        Design.scaffold.onSurfaceMuted,
    )
}

private class DriftSlot(val icon: Int, val color: Int, val scale: Float)

private fun driftSlots(iconCount: Int, colorCount: Int): List<DriftSlot> {
    var seed = 20260803
    return List(DRIFT_PERIOD * DRIFT_PERIOD) {
        seed = seed * 1103515245 + 12345
        val bits = seed ushr 8
        DriftSlot(
            icon = (bits % iconCount + iconCount) % iconCount,
            color = (bits / 7 % colorCount + colorCount) % colorCount,
            scale = 0.8f + (bits / 13 % 5) * 0.1f,
        )
    }
}

private fun DrawScope.drawDriftLayer(
    icons: List<Painter>,
    palette: List<Color>,
    slots: List<DriftSlot>,
    tile: Dp,
    travel: Float,
    alpha: Float,
    scale: Float,
) {
    val step = tile.toPx()
    if (step <= 0f) return
    val base = floor(travel).toInt()
    val shift = (travel - base) * step
    val columns = (size.width / step).toInt() + 2
    val rows = (size.height / step).toInt() + 2
    for (row in -1..rows) {
        for (column in -1..columns) {
            val slot = slots[slotIndex(column - base, row - base)]
            val painter = icons[slot.icon]
            val side = step * scale * slot.scale
            translate(
                left = column * step + shift + (step - side) / 2,
                top = row * step + shift + (step - side) / 2,
            ) {
                with(painter) {
                    draw(
                        size = androidx.compose.ui.geometry.Size(side, side),
                        alpha = alpha,
                        colorFilter = ColorFilter.tint(palette[slot.color]),
                    )
                }
            }
        }
    }
}

private fun slotIndex(column: Int, row: Int): Int {
    val x = ((column % DRIFT_PERIOD) + DRIFT_PERIOD) % DRIFT_PERIOD
    val y = ((row % DRIFT_PERIOD) + DRIFT_PERIOD) % DRIFT_PERIOD
    return y * DRIFT_PERIOD + x
}
