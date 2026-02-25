package ua.vald_zx.game.rat.race.card.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

@Composable
fun EButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    unitTS: TextUnit,
    unitDp: Dp,
) {
    ElevatedButton(
        modifier = modifier.height(unitDp * 32),
        onClick = onClick,
        enabled = enabled,
        content = {
            Text(title, fontSize = unitTS * 12)
        },
        contentPadding = OutlinedTextFieldDefaults.contentPadding(
            start = unitDp * 8,
            top = unitDp,
            bottom = unitDp,
            end = unitDp * 8
        ),
    )
}