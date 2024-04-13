package ua.vald_zx.game.rat.race.card.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Button(text: String, onClick: () -> Unit) = ElevatedButton(
    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        .widthIn(min = 200.dp),
    onClick = onClick,
    content = {
        Text(text)
    }
)