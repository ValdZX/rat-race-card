package ua.vald_zx.game.rat.race.card.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.components.clickableSingle

@Composable
fun DesignToggleRow(
    label: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = Design.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(DesignShapes.md)
            .background(colors.scaffold.surface2)
            .border(
                width = if (checked) 1.5.dp else 1.dp,
                color = if (checked) colors.scaffold.accentDim else colors.scaffold.outline,
                shape = DesignShapes.md,
            )
            .clickableSingle { onCheckedChange(!checked) }
            .padding(start = 14.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = label,
            style = Design.type.body,
            color = if (checked) colors.scaffold.onSurface else colors.scaffold.onSurfaceMuted,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.scaffold.onAccent,
                checkedTrackColor = colors.scaffold.accent,
                checkedBorderColor = colors.scaffold.accentDim,
                uncheckedThumbColor = colors.scaffold.onSurfaceMuted,
                uncheckedTrackColor = colors.scaffold.surface3,
                uncheckedBorderColor = colors.scaffold.outlineStrong,
            ),
        )
    }
}
