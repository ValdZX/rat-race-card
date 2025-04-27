package ua.vald_zx.game.rat.race.card.resource.images

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.resource.Images

val Images.UpDoubleArrow: ImageVector
    get() {
        if (_UpDoubleArrow != null) {
            return _UpDoubleArrow!!
        }
        _UpDoubleArrow = ImageVector.Builder(
            name = "UpDoubleArrow",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(389.83f, 121.76f)
                lineToRelative(-128f, -119.47f)
                curveToRelative(-3.29f, -3.06f, -8.36f, -3.06f, -11.65f, 0f)
                lineToRelative(-128f, 119.47f)
                curveToRelative(-1.72f, 1.61f, -2.71f, 3.87f, -2.71f, 6.24f)
                verticalLineToRelative(136.53f)
                curveToRelative(0f, 3.45f, 2.08f, 6.56f, 5.27f, 7.89f)
                curveToRelative(3.19f, 1.32f, 6.85f, 0.59f, 9.29f, -1.85f)
                lineToRelative(121.97f, -121.97f)
                lineToRelative(121.97f, 121.97f)
                curveToRelative(1.64f, 1.63f, 3.82f, 2.5f, 6.03f, 2.5f)
                curveToRelative(1.1f, 0f, 2.21f, -0.21f, 3.27f, -0.65f)
                curveToRelative(3.19f, -1.32f, 5.26f, -4.44f, 5.26f, -7.89f)
                verticalLineTo(128f)
                curveTo(392.53f, 125.63f, 391.55f, 123.37f, 389.83f, 121.76f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(261.83f, 241.23f)
                curveToRelative(-3.29f, -3.06f, -8.36f, -3.06f, -11.65f, 0f)
                lineToRelative(-128f, 119.47f)
                curveToRelative(-1.72f, 1.61f, -2.71f, 3.87f, -2.71f, 6.24f)
                verticalLineToRelative(136.53f)
                curveToRelative(0f, 3.45f, 2.08f, 6.56f, 5.27f, 7.89f)
                curveToRelative(3.19f, 1.31f, 6.85f, 0.6f, 9.29f, -1.85f)
                lineToRelative(121.97f, -121.97f)
                lineToRelative(121.97f, 121.97f)
                curveToRelative(1.64f, 1.63f, 3.82f, 2.5f, 6.03f, 2.5f)
                curveToRelative(1.1f, 0f, 2.21f, -0.21f, 3.27f, -0.65f)
                curveToRelative(3.19f, -1.32f, 5.26f, -4.44f, 5.26f, -7.89f)
                verticalLineTo(366.93f)
                curveToRelative(0f, -2.36f, -0.98f, -4.63f, -2.7f, -6.24f)
                lineTo(261.83f, 241.23f)
                close()
            }
        }.build()

        return _UpDoubleArrow!!
    }

@Suppress("ObjectPropertyName")
private var _UpDoubleArrow: ImageVector? = null
