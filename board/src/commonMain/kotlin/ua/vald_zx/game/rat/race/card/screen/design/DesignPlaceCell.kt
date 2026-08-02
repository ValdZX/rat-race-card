package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.vald_zx.game.rat.race.card.components.Rotation
import ua.vald_zx.game.rat.race.card.components.clickableSingle
import ua.vald_zx.game.rat.race.card.splitDecimal
import ua.vald_zx.game.rat.race.card.components.optionalModifier
import ua.vald_zx.game.rat.race.card.components.rotateLayout
import ua.vald_zx.game.rat.race.card.design.*
import ua.vald_zx.game.rat.race.card.shared.PlaceType

/**
 * П'ять силуетів рамки. Тип клітинки має читатись із 60 см, де кольори
 * сусідніх клітинок уже зливаються, тому форма несе більше за колір.
 */
enum class CellFamily { Service, Card, Loss, Asset, Life }

/** Живий трек — плитка; неактивний — гравіювання на дошці. */
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
    waitingAmount: Long? = null,
    onFocusChange: ((focused: Boolean, byTap: Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val colors = Design.colors
    val tone = type.tone()
    val engraved = surface == CellSurface.Engraved
    val markerColor = if (engraved) colors.scaffold.outlineStrong else tone.edge
    val shape = remember(type.family) { CellShape(type.family) }

    // Наведення курсором на десктопі й вебі; тап — на телефоні. Обидва ведуть
    // в один стан «клітинка в фокусі», який тримає трек.
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    LaunchedEffect(hovered) { onFocusChange?.invoke(hovered, false) }

    Box(modifier = modifier, contentAlignment = Alignment.TopEnd) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .optionalModifier(waitingAmount != null) { plinth(colors.scaffold.brass, 3.dp, shape) }
            // Цоколь на клітинці 15dp не видно, а тінь із власною формою — це
            // окремий шар на кожну з ~90 клітинок. На телефоні платити за це нічим.
            .optionalModifier(!engraved && !compact) { levelTile(tone.edge, shape) }
            .clip(shape)
            .optionalModifier(!engraved) { background(tone.fill) }
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
            .hoverable(interaction)
            // Клітинка з дією лишає тап дії; решта тапом розкриває підпис.
            .optionalModifier(onClick != null) { clickableSingle { onClick?.invoke() } }
            .optionalModifier(onClick == null && onFocusChange != null) {
                clickableSingle { onFocusChange?.invoke(true, true) }
            }
            .drawBehind {
                if (engraved) drawHatch(colors.scaffold.outlineStrong)
                drawFamilyMarker(type.family, markerColor, engraved)
            },
        contentAlignment = Alignment.Center,
    ) {
        val ink = if (engraved) colors.scaffold.onSurface else colors.scaffold.onFill
        val icon = @Composable { modifier: Modifier ->
            Icon(
                painter = type.icon(),
                contentDescription = label,
                // На неактивному треку знак приглушений: він орієнтир, а не сигнал.
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
                icon(Modifier.fillMaxHeight(0.7f).aspectRatio(1f))
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
        // Жетон із сумою: сигнал «чекає на тебе» містить цифру,
        // тому не плутається з «натисни мене».
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

private fun DrawScope.drawFamilyMarker(family: CellFamily, color: Color, engraved: Boolean) {
    val unit = size.minDimension
    val stroke = if (engraved) unit * 0.05f else unit * 0.07f
    val alpha = if (engraved) 0.9f else 0.55f
    when (family) {
        // Смуга-капітель зверху
        CellFamily.Service -> drawRect(
            color = color,
            topLeft = Offset(0f, 0f),
            size = Size(size.width, unit * 0.16f),
            alpha = alpha,
        )

        // Внутрішня рамка (загнутий кут — у самій формі)
        CellFamily.Card -> {
            val inset = unit * 0.16f
            drawRect(
                color = color,
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2, size.height - inset * 2),
                style = Stroke(width = stroke * 0.6f),
                alpha = alpha,
            )
        }

        // Виїмка — у самій формі; тут лише лінія-підсічка над нею
        CellFamily.Loss -> drawLine(
            color = color,
            start = Offset(unit * 0.16f, size.height * 0.62f),
            end = Offset(size.width - unit * 0.16f, size.height * 0.62f),
            strokeWidth = stroke * 0.5f,
            alpha = alpha,
        )

        // Стовпчики + верхня лінія-горизонт
        CellFamily.Asset -> {
            val barWidth = unit * 0.13f
            val gap = unit * 0.09f
            val baseY = size.height * 0.82f
            val startX = size.width / 2 - (barWidth * 3 + gap * 2) / 2
            listOf(0.28f, 0.46f, 0.64f).forEachIndexed { index, heightFactor ->
                val x = startX + index * (barWidth + gap)
                drawRect(
                    color = color,
                    topLeft = Offset(x, baseY - size.height * heightFactor * 0.6f),
                    size = Size(barWidth, size.height * heightFactor * 0.6f),
                    alpha = alpha,
                )
            }
            drawLine(
                color = color,
                start = Offset(unit * 0.14f, size.height * 0.2f),
                end = Offset(size.width - unit * 0.14f, size.height * 0.2f),
                strokeWidth = stroke * 0.5f,
                alpha = alpha,
            )
        }

        // Куполоподібний низ + кільця
        CellFamily.Life -> {
            val radius = unit * 0.3f
            drawCircle(
                color = color,
                radius = radius,
                center = Offset(size.width / 2, size.height * 0.55f),
                style = Stroke(width = stroke * 0.5f),
                alpha = alpha,
            )
            drawCircle(
                color = color,
                radius = radius * 0.5f,
                center = Offset(size.width / 2, size.height * 0.55f),
                style = Stroke(width = stroke * 0.5f),
                alpha = alpha,
            )
            drawArc(
                color = color,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(0f, size.height - unit * 0.24f),
                size = Size(size.width, unit * 0.24f),
                style = Stroke(width = stroke * 0.5f),
                alpha = alpha,
            )
        }
    }
}

/** Штрих 135° — гравіювання неактивного треку. */
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


/**
 * Силует рамки за родиною. Саме форма, а не колір, робить тип упізнаваним,
 * коли клітинка займає 17×34dp і стоїть у щільному ряду.
 */
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
            // Загнутий кут праворуч угорі
            CellFamily.Card -> {
                val fold = size.minDimension * 0.28f
                Path().apply {
                    moveTo(size.width - fold, -1f)
                    lineTo(size.width + 1f, fold)
                    lineTo(size.width + 1f, -1f)
                    close()
                }
            }
            // Клин, вирізаний із нижньої кромки
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

/** Кегль розкритого підпису: фіксований і читабельний, клітинка росте під нього. */
@Composable
internal fun expandedLabelStyle() = Design.type.cellSm.copy(
    fontWeight = FontWeight.ExtraBold,
    letterSpacing = 0.sp,
    textAlign = TextAlign.Center,
)
