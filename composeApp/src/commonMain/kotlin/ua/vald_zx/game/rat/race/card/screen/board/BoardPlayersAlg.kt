package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

fun calculatePointerOffset(
    pointerWidth: Dp,
    pointerHeight: Dp,
    place: Place,
    index: Int,
    count: Int,
): Pair<Dp, Dp> {
    val width = place.size.width
    val height = place.size.height
    val centerX = place.offset.x + width / 2 - pointerWidth / 2
    val centerY = place.offset.y + height / 2 - pointerHeight / 2
    val offsetX = width / 3
    val offsetY = height / 3
    val offset = when (count) {
        1 -> Pair(0.dp, 0.dp)

        2 -> when (index) {
            0 -> Pair(-offsetX, -offsetY) // Верхній лівий
            else -> Pair(offsetX, offsetY) // Нижній правий
        }

        3 -> when (index) {
            0 -> Pair(-offsetX, -offsetY) // Верхній лівий
            1 -> Pair(0.dp, 0.dp)             // Центр
            else -> Pair(offsetX, offsetY) // Нижній правий
        }

        4 -> when (index) {
            0 -> Pair(-offsetX, -offsetY) // Верхній лівий
            1 -> Pair(offsetX, -offsetY)  // Верхній правий
            2 -> Pair(-offsetX, offsetY)  // Нижній лівий
            else -> Pair(offsetX, offsetY) // Нижній правий
        }

        5 -> when (index) {
            0 -> Pair(-offsetX, -offsetY) // Верхній лівий
            1 -> Pair(offsetX, -offsetY)  // Верхній правий
            2 -> Pair(-offsetX, offsetY)  // Нижній лівий
            3 -> Pair(offsetX, offsetY)  // Нижній правий
            else -> Pair(0.dp, 0.dp)          // Центр
        }

        6 -> when (index) {
            0 -> Pair(-offsetX, -offsetY) // Стовпчик 1
            1 -> Pair(-offsetX, 0.dp)
            2 -> Pair(-offsetX, offsetY)
            3 -> Pair(offsetX, -offsetY)  // Стовпчик 2
            4 -> Pair(offsetX, 0.dp)
            else -> Pair(offsetX, offsetY)
        }

        7 -> when (index) {
            0 -> Pair(-offsetX, -offsetY) // 6 точок як у випадку 6
            1 -> Pair(-offsetX, 0.dp)
            2 -> Pair(-offsetX, offsetY)
            3 -> Pair(offsetX, -offsetY)
            4 -> Pair(offsetX, 0.dp)
            5 -> Pair(offsetX, offsetY)
            else -> Pair(0.dp, 0.dp)          // 7-а точка в центрі
        }

        8 -> {
            // Сітка 3x3 без центральної точки.
            // "Пропускаємо" центральний індекс (4)
            val adjustedIndex = if (index >= 4) index + 1 else index
            val col = adjustedIndex % 3
            val row = adjustedIndex / 3
            Pair(offsetX * (col - 1), offsetY * (row - 1))
        }

        9 -> {
            // Повна сітка 3x3
            val col = index % 3 // 0, 1, 2
            val row = index / 3 // 0, 1, 2
            // Перетворюємо індекси (0,1,2) в множники (-1, 0, 1) для зміщення
            Pair(offsetX * (col - 1), offsetY * (row - 1))
        }

        else -> Pair(0.dp, 0.dp) // Недосяжний код через валідацію, але компілятор вимагає
    }


    return Pair(centerX + offset.first, centerY + offset.second)
}

@Preview
@Composable
fun AlgPreview() {
    Box(modifier = Modifier.size(60.dp, 100.dp).background(Color.Black)) {
        val count = 2
        repeat(count) { index ->
            val offset = calculatePointerOffset(
                pointerWidth = 10.dp,
                pointerHeight = 10.dp,
                place = Place(
                    type = PlaceType.Love,
                    location = Location(Side.TOP, 2),
                    offset = DpOffset(0.dp, 0.dp),
                    size = DpSize(60.dp, 100.dp)
                ),
                index = index,
                count = count
            )
            Box(
                modifier = Modifier
                    .offset(offset.first, offset.second)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}