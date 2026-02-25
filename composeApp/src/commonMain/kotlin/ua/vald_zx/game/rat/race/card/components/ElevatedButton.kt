package ua.vald_zx.game.rat.race.card.components

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
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        content = {
            Text(title, fontSize = unitTS * 14)
        },
        contentPadding = OutlinedTextFieldDefaults.contentPadding(
            start = unitDp * 4,
            top = unitDp * 2,
            bottom = unitDp * 2,
            end = unitDp * 4
        ),
    )
}