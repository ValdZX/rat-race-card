package ua.vald_zx.game.rat.race.card.resource.images

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.resource.Images

val Images.RatPlayer1: ImageVector
    get() {
        if (_RatPlayer1 != null) {
            return _RatPlayer1!!
        }
        _RatPlayer1 = ImageVector.Builder(
            name = "RatPlayer1",
            defaultWidth = 800.dp,
            defaultHeight = 800.dp,
            viewportWidth = 64f,
            viewportHeight = 64f
        ).apply {
            path(fill = SolidColor(Color(0xFFF29A2E))) {
                moveTo(18.9f, 46f)
                curveToRelative(-0.7f, 0.2f, -1.5f, 0.6f, -2.2f, 1f)
                reflectiveCurveToRelative(-1.4f, 0.9f, -2f, 1.4f)
                curveToRelative(-0.6f, 0.5f, -1.1f, 1.1f, -1.5f, 1.7f)
                curveToRelative(-0.4f, 0.6f, -0.7f, 1.2f, -0.8f, 1.8f)
                curveToRelative(-0.1f, 0.6f, 0f, 1.2f, 0.3f, 1.8f)
                curveToRelative(0.3f, 0.6f, 0.8f, 1.2f, 1.4f, 1.8f)
                curveToRelative(0.3f, 0.3f, 0.6f, 0.5f, 1f, 0.8f)
                curveToRelative(0.3f, 0.2f, 0.7f, 0.5f, 1.1f, 0.7f)
                curveToRelative(0.7f, 0.4f, 1.5f, 0.8f, 2.3f, 1.1f)
                curveToRelative(1.6f, 0.6f, 3.4f, 0.9f, 5.2f, 1.2f)
                curveToRelative(1.8f, 0.3f, 3.7f, 0.5f, 5.5f, 0.7f)
                curveToRelative(1.9f, 0.2f, 3.7f, 0.5f, 5.6f, 0.8f)
                curveToRelative(1.8f, 0.3f, 3.7f, 0.7f, 5.5f, 1.1f)
                curveToRelative(-1.9f, -0.2f, -3.7f, -0.4f, -5.6f, -0.6f)
                curveToRelative(-1.9f, -0.1f, -3.7f, -0.2f, -5.6f, -0.3f)
                curveToRelative(-1.9f, -0.1f, -3.7f, -0.1f, -5.6f, -0.2f)
                curveToRelative(-1.9f, -0.1f, -3.8f, -0.3f, -5.7f, -0.8f)
                curveToRelative(-1f, -0.3f, -1.9f, -0.6f, -2.8f, -1f)
                curveToRelative(-0.5f, -0.2f, -0.9f, -0.4f, -1.3f, -0.7f)
                curveToRelative(-0.4f, -0.3f, -0.9f, -0.5f, -1.3f, -0.9f)
                curveToRelative(-0.8f, -0.6f, -1.6f, -1.4f, -2.3f, -2.4f)
                curveToRelative(-0.3f, -0.5f, -0.6f, -1.1f, -0.7f, -1.7f)
                curveToRelative(-0.2f, -0.6f, -0.2f, -1.2f, -0.2f, -1.8f)
                curveToRelative(0f, -0.6f, 0.1f, -1.2f, 0.3f, -1.8f)
                curveToRelative(0.2f, -0.6f, 0.4f, -1.1f, 0.7f, -1.6f)
                curveToRelative(0.5f, -1f, 1.2f, -1.9f, 1.9f, -2.6f)
                curveToRelative(0.7f, -0.8f, 1.5f, -1.5f, 2.4f, -2.1f)
                curveToRelative(0.9f, -0.6f, 1.8f, -1.2f, 2.8f, -1.6f)
                lineToRelative(1.6f, 4.2f)
            }
            path(fill = SolidColor(Color(0xFF999A9C))) {
                moveTo(44.6f, 12.4f)
                moveToRelative(-10.4f, 0f)
                arcToRelative(10.4f, 10.4f, 0f, isMoreThanHalf = true, isPositiveArc = true, 20.8f, 0f)
                arcToRelative(10.4f, 10.4f, 0f, isMoreThanHalf = true, isPositiveArc = true, -20.8f, 0f)
            }
            path(fill = SolidColor(Color(0xFFFFC5D3))) {
                moveTo(44.5f, 13.6f)
                moveToRelative(-7.7f, 0f)
                arcToRelative(7.7f, 7.7f, 0f, isMoreThanHalf = true, isPositiveArc = true, 15.4f, 0f)
                arcToRelative(7.7f, 7.7f, 0f, isMoreThanHalf = true, isPositiveArc = true, -15.4f, 0f)
            }
            path(fill = SolidColor(Color(0xFF999A9C))) {
                moveTo(19.4f, 12.4f)
                moveToRelative(-10.4f, 0f)
                arcToRelative(10.4f, 10.4f, 0f, isMoreThanHalf = true, isPositiveArc = true, 20.8f, 0f)
                arcToRelative(10.4f, 10.4f, 0f, isMoreThanHalf = true, isPositiveArc = true, -20.8f, 0f)
            }
            path(fill = SolidColor(Color(0xFFFFC5D3))) {
                moveTo(19.5f, 13.6f)
                moveToRelative(-7.7f, 0f)
                arcToRelative(7.7f, 7.7f, 0f, isMoreThanHalf = true, isPositiveArc = true, 15.4f, 0f)
                arcToRelative(7.7f, 7.7f, 0f, isMoreThanHalf = true, isPositiveArc = true, -15.4f, 0f)
            }
            path(fill = SolidColor(Color(0xFF999A9C))) {
                moveTo(50.3f, 45.9f)
                curveToRelative(0f, -10.2f, -4f, -19.5f, -5.3f, -22.3f)
                curveToRelative(-0.1f, -0.3f, -0.2f, -0.5f, -0.3f, -0.8f)
                curveTo(42.9f, 19f, 38.9f, 10.6f, 32f, 10.6f)
                curveToRelative(-6.9f, 0f, -10.9f, 8.4f, -12.6f, 12.2f)
                curveToRelative(-0.1f, 0.3f, -0.2f, 0.5f, -0.3f, 0.8f)
                curveToRelative(-1.3f, 2.8f, -5.3f, 12.2f, -5.3f, 22.3f)
                curveToRelative(0f, 8.4f, 2.7f, 10.6f, 6.9f, 11f)
                curveToRelative(0.2f, 0.9f, 1.1f, 1.6f, 2.1f, 1.6f)
                horizontalLineToRelative(1.5f)
                curveToRelative(1.1f, 0f, 2f, -0.8f, 2.2f, -1.8f)
                curveToRelative(1.7f, -0.1f, 3.6f, -0.3f, 5.5f, -0.3f)
                curveToRelative(1.8f, 0f, 3.6f, 0.1f, 5.3f, 0.3f)
                curveToRelative(0.1f, 1f, 1.1f, 1.8f, 2.2f, 1.8f)
                horizontalLineTo(41f)
                curveToRelative(1f, 0f, 1.9f, -0.7f, 2.1f, -1.6f)
                curveToRelative(4.4f, -0.3f, 7.2f, -2.4f, 7.2f, -11f)
            }
            path(fill = SolidColor(Color(0xFFFFC5D3))) {
                moveTo(38.5f, 33.8f)
                arcToRelative(3.2f, 2.2f, 0f, isMoreThanHalf = true, isPositiveArc = false, 6.4f, 0f)
                arcToRelative(3.2f, 2.2f, 0f, isMoreThanHalf = true, isPositiveArc = false, -6.4f, 0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFC5D3))) {
                moveTo(19.1f, 33.8f)
                arcToRelative(3.2f, 2.2f, 0f, isMoreThanHalf = true, isPositiveArc = false, 6.4f, 0f)
                arcToRelative(3.2f, 2.2f, 0f, isMoreThanHalf = true, isPositiveArc = false, -6.4f, 0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF3E4347))) {
                moveTo(34.9f, 33.8f)
                curveToRelative(-0.6f, -0.6f, -2.4f, -0.7f, -2.9f, -0.7f)
                reflectiveCurveToRelative(-2.3f, 0f, -2.9f, 0.7f)
                curveToRelative(-0.4f, 0.4f, -0.1f, 1.5f, 1f, 2.4f)
                curveToRelative(0.7f, 0.6f, 1.4f, 0.8f, 1.9f, 0.8f)
                reflectiveCurveToRelative(1.2f, -0.2f, 1.9f, -0.8f)
                curveToRelative(1.1f, -0.9f, 1.4f, -2f, 1f, -2.4f)
            }
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(20.6f, 26.7f)
                arcToRelative(4.6f, 4.5f, 0f, isMoreThanHalf = true, isPositiveArc = false, 9.2f, 0f)
                arcToRelative(4.6f, 4.5f, 0f, isMoreThanHalf = true, isPositiveArc = false, -9.2f, 0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF3E4347))) {
                moveTo(22f, 26.7f)
                arcToRelative(3.2f, 3.1f, 0f, isMoreThanHalf = true, isPositiveArc = false, 6.4f, 0f)
                arcToRelative(3.2f, 3.1f, 0f, isMoreThanHalf = true, isPositiveArc = false, -6.4f, 0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(34.2f, 26.7f)
                arcToRelative(4.6f, 4.5f, 0f, isMoreThanHalf = true, isPositiveArc = false, 9.2f, 0f)
                arcToRelative(4.6f, 4.5f, 0f, isMoreThanHalf = true, isPositiveArc = false, -9.2f, 0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF3E4347))) {
                moveTo(35.6f, 26.7f)
                arcToRelative(3.2f, 3.1f, 0f, isMoreThanHalf = true, isPositiveArc = false, 6.4f, 0f)
                arcToRelative(3.2f, 3.1f, 0f, isMoreThanHalf = true, isPositiveArc = false, -6.4f, 0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF3E4347))) {
                moveTo(31f, 39.3f)
                curveToRelative(0.1f, -0.3f, 0.3f, -0.5f, 0.5f, -0.7f)
                curveToRelative(0.2f, -0.2f, 0.5f, -0.3f, 0.7f, -0.4f)
                curveToRelative(0.3f, -0.1f, 0.5f, -0.2f, 0.8f, -0.2f)
                curveToRelative(0.3f, 0f, 0.6f, 0f, 0.8f, 0.3f)
                curveToRelative(-0.3f, 0f, -0.5f, 0.2f, -0.7f, 0.3f)
                curveToRelative(-0.2f, 0.1f, -0.4f, 0.2f, -0.7f, 0.3f)
                curveToRelative(-0.2f, 0.1f, -0.5f, 0.2f, -0.7f, 0.3f)
                curveToRelative(-0.2f, 0f, -0.4f, 0f, -0.7f, 0.1f)
            }
        }.build()

        return _RatPlayer1!!
    }

@Suppress("ObjectPropertyName")
private var _RatPlayer1: ImageVector? = null
