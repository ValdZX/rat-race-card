package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.RatPlayer1


data class PlayerPointState(
    val position: Int,
    val color: Long,
    val level: Int,
    val name: String,
)

@Composable
fun PlayerPoint(
    places: List<Place>,
    state: PlayerPointState,
    spotWidth: Dp,
    spotHeight: Dp,
    index: Int,
    count: Int,
) {
    var offset by remember {
        val place = places[state.position]
        mutableStateOf(calculatePointerOffset(spotWidth, spotHeight, place, index, count))
    }
    LaunchedEffect(state.position, count, spotWidth, spotHeight) {
        val place = places[state.position]
        offset = calculatePointerOffset(spotWidth, spotHeight, place, index, count)
    }
    val animatedX by animateDpAsState(offset.first)
    val animatedY by animateDpAsState(offset.second)
    var selectDialog by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .offset(animatedX, animatedY)
            .size(spotWidth, spotHeight)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .border(
                width = spotWidth * 0.1f,
                shape = CircleShape,
                color = Color(state.color)
            ).clickable {
                selectDialog = true
            }
    ) {
        Image(
            imageVector = Images.RatPlayer1,
            contentDescription = null,
        )
    }
    if (selectDialog) {
        AlertDialog(
            title = { Text(text = "Pointer") },
            text = {
                Text(
                    text = state.name
                )
            },
            onDismissRequest = { selectDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectDialog = false
                    }
                ) { Text("Ok") }
            },
        )
    }
}