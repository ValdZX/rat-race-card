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
import androidx.compose.ui.text.input.VisualTransformation
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
                textStyle = type.subtitle.copy(color = colors.scaffold.onSurface),
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
                    .heightIn(min = if (singleLine) 60.dp else 104.dp)
                    .clip(DesignShapes.md)
                    .background(colors.scaffold.surface2)
                    .border(
                        width = 1.5.dp,
                        color = if (active) colors.scaffold.outlineStrong else colors.scaffold.outline,
                        shape = DesignShapes.md,
                    ),
                decorationBox = { inner ->
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                        contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart,
                    ) {
                        if (value.isEmpty() && placeholder != null) {
                            Text(placeholder, style = type.subtitle, color = colors.scaffold.onSurfaceMuted)
                        }
                        inner()
                    }
            },
        )
    }
}
