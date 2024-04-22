package ua.vald_zx.game.rat.race.card.resource.fromvectorimages

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

val Images.Share: ImageVector
    get() {
        if (_share != null) {
            return _share!!
        }
        _share = Builder(name = "Share", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 960.0f, viewportHeight = 960.0f).apply {
            group {
                path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(720.0f, -80.0f)
                    quadToRelative(-50.0f, 0.0f, -85.0f, -35.0f)
                    reflectiveQuadToRelative(-35.0f, -85.0f)
                    quadToRelative(0.0f, -7.0f, 1.0f, -14.5f)
                    reflectiveQuadToRelative(3.0f, -13.5f)
                    lineTo(322.0f, -392.0f)
                    quadToRelative(-17.0f, 15.0f, -38.0f, 23.5f)
                    reflectiveQuadToRelative(-44.0f, 8.5f)
                    quadToRelative(-50.0f, 0.0f, -85.0f, -35.0f)
                    reflectiveQuadToRelative(-35.0f, -85.0f)
                    quadToRelative(0.0f, -50.0f, 35.0f, -85.0f)
                    reflectiveQuadToRelative(85.0f, -35.0f)
                    quadToRelative(23.0f, 0.0f, 44.0f, 8.5f)
                    reflectiveQuadToRelative(38.0f, 23.5f)
                    lineToRelative(282.0f, -164.0f)
                    quadToRelative(-2.0f, -6.0f, -3.0f, -13.5f)
                    reflectiveQuadToRelative(-1.0f, -14.5f)
                    quadToRelative(0.0f, -50.0f, 35.0f, -85.0f)
                    reflectiveQuadToRelative(85.0f, -35.0f)
                    quadToRelative(50.0f, 0.0f, 85.0f, 35.0f)
                    reflectiveQuadToRelative(35.0f, 85.0f)
                    quadToRelative(0.0f, 50.0f, -35.0f, 85.0f)
                    reflectiveQuadToRelative(-85.0f, 35.0f)
                    quadToRelative(-23.0f, 0.0f, -44.0f, -8.5f)
                    reflectiveQuadTo(638.0f, -672.0f)
                    lineTo(356.0f, -508.0f)
                    quadToRelative(2.0f, 6.0f, 3.0f, 13.5f)
                    reflectiveQuadToRelative(1.0f, 14.5f)
                    quadToRelative(0.0f, 7.0f, -1.0f, 14.5f)
                    reflectiveQuadToRelative(-3.0f, 13.5f)
                    lineToRelative(282.0f, 164.0f)
                    quadToRelative(17.0f, -15.0f, 38.0f, -23.5f)
                    reflectiveQuadToRelative(44.0f, -8.5f)
                    quadToRelative(50.0f, 0.0f, 85.0f, 35.0f)
                    reflectiveQuadToRelative(35.0f, 85.0f)
                    quadToRelative(0.0f, 50.0f, -35.0f, 85.0f)
                    reflectiveQuadToRelative(-85.0f, 35.0f)
                    close()
                    moveToRelative(0.0f, -640.0f)
                    quadToRelative(17.0f, 0.0f, 28.5f, -11.5f)
                    reflectiveQuadTo(760.0f, -760.0f)
                    quadToRelative(0.0f, -17.0f, -11.5f, -28.5f)
                    reflectiveQuadTo(720.0f, -800.0f)
                    quadToRelative(-17.0f, 0.0f, -28.5f, 11.5f)
                    reflectiveQuadTo(680.0f, -760.0f)
                    quadToRelative(0.0f, 17.0f, 11.5f, 28.5f)
                    reflectiveQuadTo(720.0f, -720.0f)
                    close()
                    moveTo(240.0f, -440.0f)
                    quadToRelative(17.0f, 0.0f, 28.5f, -11.5f)
                    reflectiveQuadTo(280.0f, -480.0f)
                    quadToRelative(0.0f, -17.0f, -11.5f, -28.5f)
                    reflectiveQuadTo(240.0f, -520.0f)
                    quadToRelative(-17.0f, 0.0f, -28.5f, 11.5f)
                    reflectiveQuadTo(200.0f, -480.0f)
                    quadToRelative(0.0f, 17.0f, 11.5f, 28.5f)
                    reflectiveQuadTo(240.0f, -440.0f)
                    close()
                    moveToRelative(480.0f, 280.0f)
                    quadToRelative(17.0f, 0.0f, 28.5f, -11.5f)
                    reflectiveQuadTo(760.0f, -200.0f)
                    quadToRelative(0.0f, -17.0f, -11.5f, -28.5f)
                    reflectiveQuadTo(720.0f, -240.0f)
                    quadToRelative(-17.0f, 0.0f, -28.5f, 11.5f)
                    reflectiveQuadTo(680.0f, -200.0f)
                    quadToRelative(0.0f, 17.0f, 11.5f, 28.5f)
                    reflectiveQuadTo(720.0f, -160.0f)
                    close()
                    moveToRelative(0.0f, -600.0f)
                    close()
                    moveTo(240.0f, -480.0f)
                    close()
                    moveToRelative(480.0f, 280.0f)
                    close()
                }
            }
        }
        .build()
        return _share!!
    }

private var _share: ImageVector? = null
