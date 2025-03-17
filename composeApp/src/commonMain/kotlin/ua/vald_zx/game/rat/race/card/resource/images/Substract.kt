package ua.vald_zx.game.rat.race.card.resource.images

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.resource.Images

val Images.Substract: ImageVector
    get() {
        if (_Substract != null) {
            return _Substract!!
        }
        _Substract = ImageVector.Builder(
            name = "Substract",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF0F1729))) {
                moveTo(8f, 11f)
                curveTo(7.448f, 11f, 7f, 11.448f, 7f, 12f)
                curveTo(7f, 12.552f, 7.448f, 13f, 8f, 13f)
                horizontalLineTo(16f)
                curveTo(16.552f, 13f, 17f, 12.552f, 17f, 12f)
                curveTo(17f, 11.448f, 16.552f, 11f, 16f, 11f)
                horizontalLineTo(8f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF0F1729)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(12f, 2f)
                curveTo(6.477f, 2f, 2f, 6.477f, 2f, 12f)
                curveTo(2f, 17.523f, 6.477f, 22f, 12f, 22f)
                curveTo(17.523f, 22f, 22f, 17.523f, 22f, 12f)
                curveTo(22f, 6.477f, 17.523f, 2f, 12f, 2f)
                close()
                moveTo(4f, 12f)
                curveTo(4f, 7.582f, 7.582f, 4f, 12f, 4f)
                curveTo(16.418f, 4f, 20f, 7.582f, 20f, 12f)
                curveTo(20f, 16.418f, 16.418f, 20f, 12f, 20f)
                curveTo(7.582f, 20f, 4f, 16.418f, 4f, 12f)
                close()
            }
        }.build()

        return _Substract!!
    }

@Suppress("ObjectPropertyName")
private var _Substract: ImageVector? = null
