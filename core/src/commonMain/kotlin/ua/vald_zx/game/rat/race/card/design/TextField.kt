package ua.vald_zx.game.rat.race.card.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DesignTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    suffix: String? = null,
    textStyle: TextStyle? = null,
    inputTestTag: String? = null,
    fieldHeight: Dp? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
) {
    val colors = Design.colors
    val type = Design.type
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val active = focused || value.isNotEmpty()

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = type.micro,
                color = colors.scaffold.onSurfaceMuted,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            textStyle = (textStyle ?: type.subtitle).copy(color = colors.scaffold.onSurface),
            cursorBrush = SolidColor(colors.scaffold.accent),
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            interactionSource = interaction,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (focused) Modifier.plinth(colors.scaffold.accentDim, 4.dp, DesignShapes.md)
                    else Modifier
                )
                .then(if (inputTestTag != null) Modifier.testTag(inputTestTag) else Modifier)
                .then(
                    if (fieldHeight != null) Modifier.height(fieldHeight)
                    else Modifier.heightIn(min = if (singleLine) 60.dp else 104.dp)
                )
                .clip(DesignShapes.md)
                .background(colors.scaffold.surface2)
                .border(
                    width = 1.5.dp,
                    color = if (active) colors.scaffold.outlineStrong else colors.scaffold.outline,
                    shape = DesignShapes.md,
                ),
            decorationBox = { inner ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(contentPadding),
                    verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder != null) {
                            Text(
                                placeholder,
                                style = textStyle ?: type.subtitle,
                                color = colors.scaffold.onSurfaceMuted,
                                maxLines = if (singleLine) 1 else maxLines,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        inner()
                    }
                    if (suffix != null) {
                        Text(
                            text = suffix,
                            style = type.subtitle,
                            color = colors.scaffold.onSurfaceMuted,
                            modifier = Modifier.padding(start = 10.dp),
                        )
                    }
                }
            },
        )
    }
}
