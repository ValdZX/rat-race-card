package ua.vald_zx.game.rat.race.card.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.vald_zx.game.rat.race.card.getDigits
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Add
import ua.vald_zx.game.rat.race.card.resource.images.Deposit
import ua.vald_zx.game.rat.race.card.resource.images.Repay
import ua.vald_zx.game.rat.race.card.resource.images.Salary
import ua.vald_zx.game.rat.race.card.resource.images.Substract
import ua.vald_zx.game.rat.race.card.splitDecimal
import ua.vald_zx.game.rat.race.card.theme.AppTheme

@Composable
fun Button(
    text: String,
    containerColor: Color = ButtonDefaults.elevatedButtonColors().containerColor,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) = ElevatedButton(
    modifier = modifier
        .padding(horizontal = 8.dp, vertical = 4.dp)
        .widthIn(min = 240.dp),
    onClick = onClick,
    content = {
        Text(text)
    },
    colors = ButtonDefaults.elevatedButtonColors().copy(containerColor = containerColor)
)

@Composable
fun RainbowButton(
    text: String,
    onClick: () -> Unit
) {
    val currentFontSizePx = with(LocalDensity.current) { 50.dp.toPx() }
    val currentFontSizeDoublePx = currentFontSizePx * 2

    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = currentFontSizeDoublePx,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing))
    )
    androidx.compose.material3.Button(
        modifier = Modifier,
        onClick = onClick,
        content = {
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 240.dp)
                    .clip(CircleShape)
                    .drawWithCache {
                        val brush = Brush.linearGradient(
                            SkittlesRainbow,
                            start = Offset(offset, offset),
                            end = Offset(offset + currentFontSizePx, offset + currentFontSizePx),
                            tileMode = TileMode.Mirror
                        )

                        onDrawBehind {
                            drawRect(
                                brush = brush,
                                blendMode = BlendMode.SrcIn
                            )
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = ButtonDefaults.buttonColors().copy(containerColor = Color.Transparent)
    )
}


@Composable
fun DetailsField(
    name: String,
    value: String,
    color: Color = Color.Unspecified,
) {
    SDetailsField(name, value, color, modifier = Modifier.fillMaxWidth())
    HorizontalDivider()
}

@Composable
fun SDetailsField(
    name: String,
    value: String,
    nameColor: Color = Color.Unspecified,
    additionalValue: List<String> = emptyList(),
    modifier: Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(8.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = nameColor,
        )
        Column {
            Text(
                text = value.splitDecimal(),
                style = MaterialTheme.typography.bodyMedium
            )
            additionalValue.forEach { item ->
                Text(
                    text = "+ ${item.splitDecimal()}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun PositiveField(
    name: String,
    value: String,
    fontSize: TextUnit = 16.sp,
    onClick: () -> Unit = {},
    deposit: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 8.dp)
    ) {
        Text(name, color = MaterialTheme.colorScheme.primary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value.splitDecimal(), fontSize = fontSize)
            if (deposit != null) {
                IconButton(
                    onClick = deposit,
                    content = {
                        Icon(Images.Deposit, contentDescription = null)
                    }
                )
            }
        }
    }
    HorizontalDivider()
}

@Composable
fun FundsField(
    name: String,
    value: String,
    fontSize: TextUnit = 16.sp,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
    ) {
        Text(name, color = AppTheme.colors.funds)
        Text(value.splitDecimal(), fontSize = fontSize)
    }
    HorizontalDivider()
}

@Composable
fun NegativeField(
    name: String, value: String, fontSize: TextUnit = 16.sp,
    onClick: () -> Unit = {},
    repay: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 8.dp)
    ) {
        Text(name, color = MaterialTheme.colorScheme.tertiary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value.splitDecimal(), fontSize = fontSize)
            if (repay != null) {
                IconButton(
                    onClick = repay,
                    content = {
                        Icon(Images.Repay, contentDescription = null)
                    }
                )
            }
        }
    }
    HorizontalDivider()
}

@Composable
fun CashFlowField(
    name: String,
    value: String,
    fontSize: TextUnit = 20.sp,
    rainbow: List<Color> = SkittlesRainbow,
    onClick: () -> Unit = {},
    salary: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 8.dp)
    ) {
        SmoothRainbowText(
            name,
            rainbow = rainbow,
            style = LocalTextStyle.current.copy(fontSize = fontSize)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value.splitDecimal(), fontSize = fontSize)
            if (salary != null) {
                IconButton(
                    onClick = salary,
                    content = {
                        Icon(Images.Salary, contentDescription = null)
                    }
                )
            }
        }
    }
    HorizontalDivider()
}

@Composable
fun BalanceField(
    name: String,
    value: String,
    onClick: () -> Unit = {},
    add: (() -> Unit)? = null,
    sub: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 8.dp)
    ) {
        Text(name)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (sub != null) {
                IconButton(
                    onClick = sub,
                    content = {
                        Icon(Images.Substract, contentDescription = null)
                    }
                )
            }
            Text(value.splitDecimal(), fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            if (add != null) {
                IconButton(
                    onClick = add,
                    content = {
                        Icon(Images.Add, contentDescription = null)
                    }
                )
            }
        }
    }
    HorizontalDivider()
}

@Composable
fun NumberTextField(
    modifier: Modifier = Modifier,
    input: MutableState<TextFieldValue>,
    inputLabel: String
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        modifier = modifier.fillMaxWidth().onFocusSelectAll(input),
        label = { Text(inputLabel) },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Number
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        value = input.value,
        onValueChange = { input.value = it.copy(text = it.text.getDigits()) },
        visualTransformation = AmountTransformation
    )
}

@Composable
fun OutlinedText(
    text: String,
    modifier: Modifier = Modifier,
    fillColor: Color = Color.Unspecified,
    outlineColor: Color,
    autoSize: TextAutoSize? = null,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = TextStyle.Default,
    outlineDrawStyle: Stroke = Stroke(),
) {
    Box(modifier = modifier) {
        BasicText(
            text = text,
            autoSize = autoSize,
            style = style.merge(
                color = outlineColor,
                fontWeight = fontWeight,
                textAlign = textAlign ?: TextAlign.Unspecified,
                lineHeight = lineHeight,
                fontFamily = fontFamily,
                textDecoration = textDecoration,
                fontStyle = fontStyle,
                letterSpacing = letterSpacing,
                shadow = null,
                drawStyle = outlineDrawStyle,
            ),
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = onTextLayout,
        )
        BasicText(
            text = text,
            autoSize = autoSize,
            style = style.merge(
                color = fillColor,
                fontWeight = fontWeight,
                textAlign = textAlign ?: TextAlign.Unspecified,
                lineHeight = lineHeight,
                fontFamily = fontFamily,
                textDecoration = textDecoration,
                fontStyle = fontStyle,
                letterSpacing = letterSpacing,
            ),
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = onTextLayout,
        )
    }
}

fun Modifier.rotateLayout(rotation: Rotation): Modifier {
    return when (rotation) {
        Rotation.ROT_0, Rotation.ROT_180 -> this
        Rotation.ROT_90, Rotation.ROT_270 -> then(HorizontalLayoutModifier)
    } then rotate(rotation.degrees)
}


enum class Rotation(val degrees: Float) {
    ROT_0(0f),
    ROT_90(90f),
    ROT_180(180f),
    ROT_270(270f),
}


/** Swap horizontal and vertical constraints */
private fun Constraints.transpose(): Constraints {
    return copy(
        minWidth = minHeight,
        maxWidth = maxHeight,
        minHeight = minWidth,
        maxHeight = maxWidth
    )
}

private object HorizontalLayoutModifier : LayoutModifier {
    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints.transpose())
        return layout(placeable.height, placeable.width) {
            placeable.place(
                x = -(placeable.width / 2 - placeable.height / 2),
                y = -(placeable.height / 2 - placeable.width / 2)
            )
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicHeight(measurable: IntrinsicMeasurable, width: Int): Int {
        return measurable.maxIntrinsicWidth(width)
    }

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(measurable: IntrinsicMeasurable, width: Int): Int {
        return measurable.maxIntrinsicWidth(width)
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(measurable: IntrinsicMeasurable, height: Int): Int {
        return measurable.minIntrinsicHeight(height)
    }

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(measurable: IntrinsicMeasurable, height: Int): Int {
        return measurable.maxIntrinsicHeight(height)
    }
}

@Composable
fun Modifier.optionalModifier(
    isNeed: Boolean,
    todo: @Composable Modifier.() -> Modifier
): Modifier {
    return if (isNeed) {
        todo()
    } else {
        this
    }
}