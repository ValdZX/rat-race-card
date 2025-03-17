package ua.vald_zx.game.rat.race.card.resource.images
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.resource.Images

val Images.Add: ImageVector
    get() {
        if (_Add != null) {
            return _Add!!
        }
        _Add = ImageVector.Builder(
            name = "Add",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF0F1729))) {
                moveTo(13f, 8f)
                curveTo(13f, 7.448f, 12.552f, 7f, 12f, 7f)
                curveTo(11.448f, 7f, 11f, 7.448f, 11f, 8f)
                verticalLineTo(11f)
                horizontalLineTo(8f)
                curveTo(7.448f, 11f, 7f, 11.448f, 7f, 12f)
                curveTo(7f, 12.552f, 7.448f, 13f, 8f, 13f)
                horizontalLineTo(11f)
                verticalLineTo(16f)
                curveTo(11f, 16.552f, 11.448f, 17f, 12f, 17f)
                curveTo(12.552f, 17f, 13f, 16.552f, 13f, 16f)
                verticalLineTo(13f)
                horizontalLineTo(16f)
                curveTo(16.552f, 13f, 17f, 12.552f, 17f, 12f)
                curveTo(17f, 11.448f, 16.552f, 11f, 16f, 11f)
                horizontalLineTo(13f)
                verticalLineTo(8f)
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

        return _Add!!
    }

@Suppress("ObjectPropertyName")
private var _Add: ImageVector? = null