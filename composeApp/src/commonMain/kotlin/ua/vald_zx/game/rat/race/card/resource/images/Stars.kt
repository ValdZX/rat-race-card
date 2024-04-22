package ua.vald_zx.game.rat.race.card.resource.images

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.resource.Images

val Images.Stars: ImageVector
    get() {
        if (_stars != null) {
            return _stars!!
        }
        _stars = Builder(name = "Stars", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 32.0f, viewportHeight = 32.0f).apply {
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(12.0f, 17.0f)
                curveToRelative(0.8f, -4.2f, 1.9f, -5.3f, 6.1f, -6.1f)
                curveToRelative(0.5f, -0.1f, 0.8f, -0.5f, 0.8f, -1.0f)
                reflectiveCurveToRelative(-0.3f, -0.9f, -0.8f, -1.0f)
                curveTo(13.9f, 8.1f, 12.8f, 7.0f, 12.0f, 2.8f)
                curveTo(11.9f, 2.3f, 11.5f, 2.0f, 11.0f, 2.0f)
                curveToRelative(-0.5f, 0.0f, -0.9f, 0.3f, -1.0f, 0.8f)
                curveTo(9.2f, 7.0f, 8.1f, 8.1f, 3.9f, 8.9f)
                curveTo(3.5f, 9.0f, 3.1f, 9.4f, 3.1f, 9.9f)
                reflectiveCurveToRelative(0.3f, 0.9f, 0.8f, 1.0f)
                curveToRelative(4.2f, 0.8f, 5.3f, 1.9f, 6.1f, 6.1f)
                curveToRelative(0.1f, 0.5f, 0.5f, 0.8f, 1.0f, 0.8f)
                reflectiveCurveTo(11.9f, 17.4f, 12.0f, 17.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(22.0f, 24.0f)
                curveToRelative(-2.8f, -0.6f, -3.4f, -1.2f, -4.0f, -4.0f)
                curveToRelative(-0.1f, -0.5f, -0.5f, -0.8f, -1.0f, -0.8f)
                reflectiveCurveToRelative(-0.9f, 0.3f, -1.0f, 0.8f)
                curveToRelative(-0.6f, 2.8f, -1.2f, 3.4f, -4.0f, 4.0f)
                curveToRelative(-0.5f, 0.1f, -0.8f, 0.5f, -0.8f, 1.0f)
                reflectiveCurveToRelative(0.3f, 0.9f, 0.8f, 1.0f)
                curveToRelative(2.8f, 0.6f, 3.4f, 1.2f, 4.0f, 4.0f)
                curveToRelative(0.1f, 0.5f, 0.5f, 0.8f, 1.0f, 0.8f)
                reflectiveCurveToRelative(0.9f, -0.3f, 1.0f, -0.8f)
                curveToRelative(0.6f, -2.8f, 1.2f, -3.4f, 4.0f, -4.0f)
                curveToRelative(0.5f, -0.1f, 0.8f, -0.5f, 0.8f, -1.0f)
                reflectiveCurveTo(22.4f, 24.1f, 22.0f, 24.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(29.2f, 14.0f)
                curveToRelative(-2.2f, -0.4f, -2.7f, -0.9f, -3.1f, -3.1f)
                curveToRelative(-0.1f, -0.5f, -0.5f, -0.8f, -1.0f, -0.8f)
                curveToRelative(-0.5f, 0.0f, -0.9f, 0.3f, -1.0f, 0.8f)
                curveToRelative(-0.4f, 2.2f, -0.9f, 2.7f, -3.1f, 3.1f)
                curveToRelative(-0.5f, 0.1f, -0.8f, 0.5f, -0.8f, 1.0f)
                reflectiveCurveToRelative(0.3f, 0.9f, 0.8f, 1.0f)
                curveToRelative(2.2f, 0.4f, 2.7f, 0.9f, 3.1f, 3.1f)
                curveToRelative(0.1f, 0.5f, 0.5f, 0.8f, 1.0f, 0.8f)
                curveToRelative(0.5f, 0.0f, 0.9f, -0.3f, 1.0f, -0.8f)
                curveToRelative(0.4f, -2.2f, 0.9f, -2.7f, 3.1f, -3.1f)
                curveToRelative(0.5f, -0.1f, 0.8f, -0.5f, 0.8f, -1.0f)
                reflectiveCurveTo(29.7f, 14.1f, 29.2f, 14.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(5.7f, 22.3f)
                curveTo(5.4f, 22.0f, 5.0f, 21.9f, 4.6f, 22.1f)
                curveToRelative(-0.1f, 0.0f, -0.2f, 0.1f, -0.3f, 0.2f)
                curveToRelative(-0.1f, 0.1f, -0.2f, 0.2f, -0.2f, 0.3f)
                curveTo(4.0f, 22.7f, 4.0f, 22.9f, 4.0f, 23.0f)
                reflectiveCurveToRelative(0.0f, 0.3f, 0.1f, 0.4f)
                curveToRelative(0.1f, 0.1f, 0.1f, 0.2f, 0.2f, 0.3f)
                curveToRelative(0.1f, 0.1f, 0.2f, 0.2f, 0.3f, 0.2f)
                curveTo(4.7f, 24.0f, 4.9f, 24.0f, 5.0f, 24.0f)
                curveToRelative(0.1f, 0.0f, 0.3f, 0.0f, 0.4f, -0.1f)
                reflectiveCurveToRelative(0.2f, -0.1f, 0.3f, -0.2f)
                curveToRelative(0.1f, -0.1f, 0.2f, -0.2f, 0.2f, -0.3f)
                curveTo(6.0f, 23.3f, 6.0f, 23.1f, 6.0f, 23.0f)
                reflectiveCurveToRelative(0.0f, -0.3f, -0.1f, -0.4f)
                curveTo(5.9f, 22.5f, 5.8f, 22.4f, 5.7f, 22.3f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(28.0f, 7.0f)
                curveToRelative(0.3f, 0.0f, 0.5f, -0.1f, 0.7f, -0.3f)
                curveTo(28.9f, 6.5f, 29.0f, 6.3f, 29.0f, 6.0f)
                reflectiveCurveToRelative(-0.1f, -0.5f, -0.3f, -0.7f)
                curveToRelative(-0.1f, -0.1f, -0.2f, -0.2f, -0.3f, -0.2f)
                curveToRelative(-0.2f, -0.1f, -0.5f, -0.1f, -0.8f, 0.0f)
                curveToRelative(-0.1f, 0.0f, -0.2f, 0.1f, -0.3f, 0.2f)
                curveTo(27.1f, 5.5f, 27.0f, 5.7f, 27.0f, 6.0f)
                curveToRelative(0.0f, 0.3f, 0.1f, 0.5f, 0.3f, 0.7f)
                curveTo(27.5f, 6.9f, 27.7f, 7.0f, 28.0f, 7.0f)
                close()
            }
        }
        .build()
        return _stars!!
    }

private var _stars: ImageVector? = null
