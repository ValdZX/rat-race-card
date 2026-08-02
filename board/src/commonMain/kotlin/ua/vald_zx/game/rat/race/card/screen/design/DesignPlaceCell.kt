package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.vald_zx.game.rat.race.card.components.Rotation
import ua.vald_zx.game.rat.race.card.components.clickableSingle
import ua.vald_zx.game.rat.race.card.splitDecimal
import ua.vald_zx.game.rat.race.card.components.optionalModifier
import ua.vald_zx.game.rat.race.card.components.rotateLayout
import ua.vald_zx.game.rat.race.card.design.*
import ua.vald_zx.game.rat.race.card.shared.PlaceType

enum class CellFamily { Service, Card, Loss, Asset, Life }

enum class CellSurface { Tile, Engraved }

val PlaceType.family: CellFamily
    get() = when (this) {
        PlaceType.Salary, PlaceType.Start, PlaceType.Rest, PlaceType.TaxInspection -> CellFamily.Service
        PlaceType.Chance, PlaceType.Store, PlaceType.Shopping, PlaceType.Deputy -> CellFamily.Card
        PlaceType.Expenses, PlaceType.Bankruptcy, PlaceType.Divorce -> CellFamily.Loss
        PlaceType.Business, PlaceType.BigBusiness -> CellFamily.Asset
        PlaceType.Child, PlaceType.Love, PlaceType.Resignation -> CellFamily.Life
        is PlaceType.Desire -> CellFamily.Life
    }

@Composable
fun PlaceType.tone(): SemanticTone = when (this) {
    PlaceType.Salary -> Design.semantic.salary
    PlaceType.Start -> Design.semantic.start
    PlaceType.Rest -> Design.semantic.rest
    PlaceType.TaxInspection -> Design.semantic.inspection
    PlaceType.Chance -> Design.semantic.chance
    PlaceType.Store -> Design.semantic.store
    PlaceType.Shopping -> Design.semantic.shopping
    PlaceType.Deputy -> Design.semantic.deputy
    PlaceType.Expenses -> Design.semantic.expenses
    PlaceType.Bankruptcy -> Design.semantic.bankruptcy
    PlaceType.Divorce -> Design.semantic.divorce
    PlaceType.Business -> Design.semantic.business
    PlaceType.BigBusiness -> Design.semantic.bigBusiness
    PlaceType.Child -> Design.semantic.family
    PlaceType.Love -> Design.semantic.love
    PlaceType.Resignation -> Design.semantic.exaltation
    is PlaceType.Desire -> Design.semantic.dream
}

@Composable
fun DesignPlaceCell(
    type: PlaceType,
    modifier: Modifier = Modifier,
    surface: CellSurface = CellSurface.Tile,
    label: String? = null,
    compact: Boolean = false,
    expanded: Boolean = false,
    expandedIcon: Dp = expandedIconSize,
    waitingAmount: Long? = null,
    onTap: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val colors = Design.colors
    val tone = type.tone()
    val engraved = surface == CellSurface.Engraved
    val shape = remember(type.family) { CellShape(type.family) }

    Box(modifier = modifier, contentAlignment = Alignment.TopEnd) {
        if (waitingAmount != null) {
            DesignActivePulse(shape = shape, color = colors.scaffold.brass)
        }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .optionalModifier(waitingAmount != null) { plinth(colors.scaffold.brass, 3.dp, shape) }
            .optionalModifier(!engraved && !compact) { levelTile(tone.edge, shape) }
            .clip(shape)
            .optionalModifier(!engraved) { background(tone.fill) }
            .optionalModifier(engraved) {
                drawBehind { drawHatch(colors.scaffold.outlineStrong) }
            }
            .border(
                width = when {
                    waitingAmount != null -> 2.dp
                    engraved -> 1.25.dp
                    else -> 1.dp
                },
                color = when {
                    waitingAmount != null -> colors.scaffold.brass
                    engraved -> colors.scaffold.outlineStrong
                    else -> tone.edge
                },
                shape = shape,
            )
            .optionalModifier(onClick != null) { clickableSingle { onClick?.invoke() } }
            .optionalModifier(onClick == null && onTap != null) {
                clickableSingle { onTap?.invoke() }
            },
        contentAlignment = Alignment.Center,
    ) {
        val ink = if (engraved) colors.scaffold.onSurface else colors.scaffold.onFill
        val icon = @Composable { modifier: Modifier ->
            Icon(
                painter = type.icon(),
                contentDescription = label,
                tint = if (engraved && !expanded) colors.scaffold.onSurfaceMuted else ink,
                modifier = modifier,
            )
        }
        if (expanded && label != null) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            ) {
                icon(Modifier.size(expandedIcon))
                Text(
                    text = label,
                    style = expandedLabelStyle(),
                    color = ink,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        } else {
            icon(Modifier.fillMaxSize(0.66f))
        }
    }
        if (waitingAmount != null) {
            WaitingToken(waitingAmount, Modifier.offset(x = 6.dp, y = (-8).dp))
        }
    }
}

@Composable
private fun WaitingToken(amount: Long, modifier: Modifier = Modifier) {
    val colors = Design.colors
    Box(
        modifier = modifier
            .height(18.dp)
            .clip(DesignShapes.sm)
            .background(colors.scaffold.brass)
            .border(1.5.dp, colors.scaffold.onFill, DesignShapes.sm)
            .padding(horizontal = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (amount >= 0) "+${amount.splitDecimal()}" else "−${(-amount).splitDecimal()}",
            style = Design.type.monoMeta,
            color = colors.scaffold.brassInk,
            maxLines = 1,
            softWrap = false,
        )
    }
}

private fun DrawScope.drawHatch(color: Color) {
    val step = 7.dp.toPx()
    clipRect {
        var x = -size.height
        while (x < size.width + size.height) {
            drawLine(
                color = color,
                start = Offset(x, size.height),
                end = Offset(x + size.height, 0f),
                strokeWidth = 1f,
                alpha = 0.35f,
            )
            x += step
        }
    }
}

private class CellShape(private val family: CellFamily) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val r = with(density) { DesignRadius.cell.toPx() }
        val base = Path().apply {
            addRoundRect(RoundRect(0f, 0f, size.width, size.height, CornerRadius(r)))
        }
        val cut = when (family) {
            CellFamily.Card -> {
                val fold = size.minDimension * 0.28f
                Path().apply {
                    moveTo(size.width - fold, -1f)
                    lineTo(size.width + 1f, fold)
                    lineTo(size.width + 1f, -1f)
                    close()
                }
            }
            CellFamily.Loss -> {
                val notch = size.minDimension * 0.36f
                Path().apply {
                    moveTo(size.width / 2 - notch / 2, size.height + 1f)
                    lineTo(size.width / 2, size.height - notch * 0.55f)
                    lineTo(size.width / 2 + notch / 2, size.height + 1f)
                    close()
                }
            }

            else -> null
        }
        val path = if (cut == null) base else Path().apply { op(base, cut, PathOperation.Difference) }
        return Outline.Generic(path)
    }
}

internal val expandedIconSize = 24.dp

internal const val COLLAPSED_ICON_FRACTION = 0.66f

internal fun expandedLabelChrome(iconSize: Dp): Dp = iconSize + 6.dp + 16.dp

@Composable
internal fun expandedLabelStyle() = Design.type.cellSm.copy(
    fontWeight = FontWeight.ExtraBold,
    letterSpacing = 0.sp,
    textAlign = TextAlign.Center,
)
