package ua.vald_zx.game.rat.race.card.resource.images

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.resource.Images

val Images.Bow: ImageVector
    get() {
        if (_Bow != null) {
            return _Bow!!
        }
        _Bow = ImageVector.Builder(
            name = "Bow",
            defaultWidth = 800.dp,
            defaultHeight = 800.dp,
            viewportWidth = 511.99f,
            viewportHeight = 511.99f
        ).apply {
            path(fill = SolidColor(Color(0xFFDA4453))) {
                moveTo(318.7f, 230.76f)
                lineToRelative(-62.7f, 27.59f)
                lineToRelative(-62.69f, -27.59f)
                lineToRelative(-68.76f, 156.23f)
                lineToRelative(48.44f, -2f)
                lineToRelative(29.68f, 36.37f)
                lineToRelative(53.34f, -121.18f)
                lineToRelative(53.34f, 121.18f)
                lineToRelative(29.67f, -36.37f)
                lineToRelative(48.44f, 2f)
                close()
            }
            path(fill = SolidColor(Color(0xFFDA4453))) {
                moveTo(398.06f, 246.39f)
                lineToRelative(-141.1f, 18f)
                lineToRelative(-123.66f, -14.22f)
                lineTo(14.35f, 304.62f)
                lineToRelative(3.44f, 9.28f)
                curveToRelative(0.5f, 1.34f, 12.7f, 32.76f, 61.75f, 32.76f)
                horizontalLineToRelative(0.01f)
                curveToRelative(40.81f, 0f, 95.45f, -21.86f, 162.4f, -64.97f)
                lineToRelative(14.05f, -9.05f)
                lineToRelative(14.06f, 9.05f)
                curveToRelative(66.94f, 43.11f, 121.58f, 64.97f, 162.41f, 64.97f)
                lineToRelative(0f, 0f)
                curveToRelative(49.05f, 0f, 61.25f, -31.42f, 61.75f, -32.76f)
                lineToRelative(3.08f, -8.31f)
                lineTo(398.06f, 246.39f)
                close()
            }
            path(fill = SolidColor(Color(0xFFED5564))) {
                moveTo(484.31f, 116.25f)
                curveToRelative(-11.58f, -17f, -28.98f, -25.62f, -51.7f, -25.62f)
                curveToRelative(-34.9f, 0f, -82.48f, 20.8f, -141.4f, 61.83f)
                curveToRelative(-12.83f, 8.94f, -24.76f, 17.83f, -35.2f, 25.94f)
                curveToRelative(-10.45f, -8.11f, -22.37f, -17f, -35.2f, -25.94f)
                curveTo(161.87f, 111.42f, 114.3f, 90.63f, 79.4f, 90.63f)
                curveToRelative(-22.73f, 0f, -40.13f, 8.63f, -51.72f, 25.62f)
                curveTo(-4.78f, 163.94f, -9.06f, 240.87f, 16.8f, 312.23f)
                curveToRelative(2.27f, 6.26f, 7.84f, 10f, 14.89f, 10f)
                curveToRelative(7.04f, 0f, 15.76f, -3.64f, 28.97f, -9.17f)
                curveToRelative(27.93f, -11.67f, 74.68f, -31.22f, 141.8f, -31.22f)
                curveToRelative(15.52f, 0f, 31.49f, 1.08f, 47.54f, 3.19f)
                curveToRelative(1.72f, 0.36f, 10.27f, 0.36f, 12f, 0f)
                curveToRelative(16.05f, -2.11f, 32.02f, -3.19f, 47.53f, -3.19f)
                curveToRelative(67.12f, 0f, 113.87f, 19.55f, 141.81f, 31.22f)
                curveToRelative(13.2f, 5.53f, 21.94f, 9.17f, 28.97f, 9.17f)
                curveToRelative(7.05f, 0f, 12.62f, -3.73f, 14.89f, -10f)
                curveTo(521.05f, 240.87f, 516.77f, 163.93f, 484.31f, 116.25f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFCE54))) {
                moveTo(320.01f, 229.34f)
                curveToRelative(0f, 35.36f, -28.66f, 64.01f, -64.01f, 64.01f)
                curveToRelative(-35.35f, 0f, -64.01f, -28.66f, -64.01f, -64.01f)
                curveToRelative(0f, -35.36f, 28.66f, -64.01f, 64.01f, -64.01f)
                curveTo(291.36f, 165.33f, 320.01f, 193.98f, 320.01f, 229.34f)
                close()
            }
            path(fill = SolidColor(Color(0xFFF6BB42))) {
                moveTo(288f, 229.34f)
                curveToRelative(0f, 17.67f, -14.33f, 32f, -32f, 32f)
                curveToRelative(-17.68f, 0f, -32.01f, -14.33f, -32.01f, -32f)
                reflectiveCurveToRelative(14.33f, -32f, 32.01f, -32f)
                curveTo(273.67f, 197.34f, 288f, 211.67f, 288f, 229.34f)
                close()
            }
        }.build()

        return _Bow!!
    }

@Suppress("ObjectPropertyName")
private var _Bow: ImageVector? = null