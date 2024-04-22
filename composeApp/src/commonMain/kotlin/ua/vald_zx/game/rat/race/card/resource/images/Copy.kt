package ua.vald_zx.game.rat.race.card.resource.images

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.resource.Images

val Images.Copy: ImageVector
    get() {
        if (_copy != null) {
            return _copy!!
        }
        _copy = Builder(name = "Copy", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 960.0f, viewportHeight = 960.0f).apply {
            group {
                path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(360.0f, -240.0f)
                    quadToRelative(-33.0f, 0.0f, -56.5f, -23.5f)
                    reflectiveQuadTo(280.0f, -320.0f)
                    verticalLineToRelative(-480.0f)
                    quadToRelative(0.0f, -33.0f, 23.5f, -56.5f)
                    reflectiveQuadTo(360.0f, -880.0f)
                    horizontalLineToRelative(360.0f)
                    quadToRelative(33.0f, 0.0f, 56.5f, 23.5f)
                    reflectiveQuadTo(800.0f, -800.0f)
                    verticalLineToRelative(480.0f)
                    quadToRelative(0.0f, 33.0f, -23.5f, 56.5f)
                    reflectiveQuadTo(720.0f, -240.0f)
                    horizontalLineTo(360.0f)
                    close()
                    moveToRelative(0.0f, -80.0f)
                    horizontalLineToRelative(360.0f)
                    verticalLineToRelative(-480.0f)
                    horizontalLineTo(360.0f)
                    verticalLineToRelative(480.0f)
                    close()
                    moveTo(200.0f, -80.0f)
                    quadToRelative(-33.0f, 0.0f, -56.5f, -23.5f)
                    reflectiveQuadTo(120.0f, -160.0f)
                    verticalLineToRelative(-560.0f)
                    horizontalLineToRelative(80.0f)
                    verticalLineToRelative(560.0f)
                    horizontalLineToRelative(440.0f)
                    verticalLineToRelative(80.0f)
                    horizontalLineTo(200.0f)
                    close()
                    moveToRelative(160.0f, -240.0f)
                    verticalLineToRelative(-480.0f)
                    verticalLineToRelative(480.0f)
                    close()
                }
            }
        }
        .build()
        return _copy!!
    }

private var _copy: ImageVector? = null
