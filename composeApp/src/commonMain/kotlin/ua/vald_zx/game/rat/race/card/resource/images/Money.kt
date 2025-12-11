package ua.vald_zx.game.rat.race.card.resource.images

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.theme.AppTheme


val Images.Money: ImageVector
    @Composable
    get() {
        return if (AppTheme.colors.isDark) {
            MoneyDark
        } else {
            MoneyLight
        }
    }
val Images.MoneyLight: ImageVector
    get() {
        if (_Money != null) {
            return _Money!!
        }
        _Money = ImageVector.Builder(
            name = "Money",
            defaultWidth = 800.dp,
            defaultHeight = 800.dp,
            viewportWidth = 64f,
            viewportHeight = 64f
        ).apply {
            path(fill = SolidColor(Color(0xFF699635))) {
                moveTo(61f, 20.9f)
                curveTo(54.8f, 15.3f, 48.6f, 9.6f, 42.4f, 4f)
                curveTo(33.3f, 17.8f, 17.9f, 30.1f, 3.3f, 39f)
                curveToRelative(-0.4f, 1.9f, -0.8f, 3.7f, -1.3f, 5.5f)
                curveTo(8.4f, 50.3f, 14.8f, 56.2f, 21.2f, 62f)
                curveTo(36.3f, 53.7f, 52.3f, 41.9f, 62f, 28.5f)
                curveToRelative(-0.3f, -2.5f, -0.7f, -5f, -1f, -7.6f)
            }
            path(fill = SolidColor(Color(0xFF83BF4F))) {
                moveTo(22.4f, 54.6f)
                curveTo(16.1f, 48.8f, 9.8f, 43f, 3.5f, 37.3f)
                curveToRelative(15.7f, -8.9f, 28.9f, -21f, 38.6f, -35.3f)
                curveTo(48.4f, 7.8f, 54.7f, 13.5f, 61f, 19.3f)
                curveToRelative(-9.7f, 14.3f, -22.9f, 26.4f, -38.6f, 35.3f)
            }
            path(fill = SolidColor(Color(0xFF699635))) {
                moveTo(20.8f, 50.8f)
                curveToRelative(-4.2f, -3.8f, -8.4f, -7.7f, -12.6f, -11.5f)
                curveToRelative(0.3f, -0.2f, 0.5f, -0.3f, 0.8f, -0.5f)
                curveToRelative(0.8f, -0.5f, 1f, -1.4f, 0.3f, -2.1f)
                lineToRelative(-0.6f, -0.6f)
                curveTo(21.4f, 28.3f, 32.4f, 18.3f, 41f, 6.7f)
                lineToRelative(0.6f, 0.6f)
                curveToRelative(0.7f, 0.6f, 1.7f, 0.5f, 2.3f, -0.2f)
                curveToRelative(0.2f, -0.2f, 0.3f, -0.5f, 0.5f, -0.7f)
                curveToRelative(4.2f, 3.8f, 8.4f, 7.7f, 12.6f, 11.5f)
                curveToRelative(-0.2f, 0.2f, -0.3f, 0.5f, -0.5f, 0.7f)
                curveToRelative(-0.6f, 0.8f, -0.5f, 1.9f, 0.2f, 2.6f)
                lineToRelative(0.6f, 0.6f)
                curveTo(48.7f, 33.3f, 37.7f, 43.3f, 25f, 51.1f)
                lineToRelative(-0.6f, -0.6f)
                curveToRelative(-0.7f, -0.6f, -1.9f, -0.7f, -2.8f, -0.2f)
                curveToRelative(-0.3f, 0.2f, -0.5f, 0.3f, -0.8f, 0.5f)
                moveToRelative(-9.9f, -11.4f)
                curveToRelative(3.4f, 3.1f, 6.8f, 6.2f, 10.3f, 9.4f)
                curveToRelative(1.3f, -0.4f, 2.8f, -0.3f, 4f, 0.3f)
                curveTo(36.8f, 41.7f, 46.9f, 32.5f, 55f, 21.8f)
                curveToRelative(-0.7f, -1.1f, -0.8f, -2.5f, -0.3f, -3.7f)
                curveToRelative(-3.4f, -3.1f, -6.8f, -6.2f, -10.3f, -9.4f)
                curveToRelative(-0.9f, 0.8f, -2.2f, 0.9f, -3.3f, 0.3f)
                curveTo(33f, 19.8f, 22.9f, 29f, 11.3f, 36.4f)
                curveToRelative(0.6f, 1f, 0.5f, 2.2f, -0.4f, 3f)
            }
            path(fill = SolidColor(Color(0xFF699635))) {
                moveTo(20f, 37.3f)
                curveToRelative(1.1f, -0.4f, 2.3f, -0.3f, 3.1f, 0.4f)
                curveToRelative(-0.5f, 0.4f, -1.1f, 0.8f, -1.6f, 1.2f)
                curveToRelative(-0.2f, -0.2f, -0.7f, -0.2f, -0.9f, 0f)
                curveToRelative(-0.3f, 0.2f, -0.4f, 0.5f, -0.1f, 0.7f)
                curveToRelative(0.2f, 0.2f, 0.7f, 0.2f, 1f, 0f)
                curveToRelative(1.2f, -0.8f, 2.9f, -0.9f, 3.8f, 0f)
                curveToRelative(0.7f, 0.7f, 0.8f, 1.7f, 0.1f, 2.5f)
                curveToRelative(0.2f, 0.2f, 0.4f, 0.3f, 0.6f, 0.5f)
                curveToRelative(-0.5f, 0.3f, -1f, 0.7f, -1.5f, 1f)
                curveToRelative(-0.2f, -0.2f, -0.4f, -0.4f, -0.6f, -0.5f)
                curveToRelative(-1.1f, 0.3f, -2.3f, 0.2f, -3f, -0.5f)
                curveToRelative(0.6f, -0.4f, 1.1f, -0.7f, 1.7f, -1.1f)
                curveToRelative(0.2f, 0.2f, 0.7f, 0.2f, 1f, 0f)
                curveToRelative(0.3f, -0.2f, 0.3f, -0.5f, 0.1f, -0.7f)
                curveToRelative(-0.2f, -0.2f, -0.6f, -0.2f, -0.9f, 0f)
                curveToRelative(-0.7f, 0.5f, -1.6f, 0.7f, -2.4f, 0.6f)
                curveToRelative(-0.6f, -0.1f, -1.1f, -0.3f, -1.5f, -0.6f)
                curveToRelative(-0.7f, -0.7f, -0.7f, -1.6f, -0.1f, -2.4f)
                curveToRelative(-0.2f, -0.1f, -0.3f, -0.3f, -0.5f, -0.5f)
                curveToRelative(0.5f, -0.3f, 1f, -0.7f, 1.5f, -1f)
                curveToRelative(-0.1f, 0.1f, 0.1f, 0.2f, 0.2f, 0.4f)
            }
            path(fill = SolidColor(Color(0xFF699635))) {
                moveTo(43.2f, 15.9f)
                curveToRelative(0.9f, -0.6f, 2f, -0.6f, 2.7f, 0.1f)
                lineToRelative(-1.2f, 1.5f)
                curveToRelative(-0.2f, -0.2f, -0.6f, -0.2f, -0.8f, 0.1f)
                curveToRelative(-0.2f, 0.3f, -0.2f, 0.7f, 0f, 0.9f)
                reflectiveCurveToRelative(0.6f, 0.2f, 0.8f, -0.1f)
                curveToRelative(0.9f, -1.1f, 2.4f, -1.3f, 3.3f, -0.5f)
                curveToRelative(0.7f, 0.7f, 0.9f, 1.8f, 0.5f, 2.8f)
                curveToRelative(0.2f, 0.2f, 0.4f, 0.3f, 0.6f, 0.5f)
                curveToRelative(-0.4f, 0.5f, -0.7f, 0.9f, -1.1f, 1.4f)
                curveToRelative(-0.2f, -0.2f, -0.4f, -0.4f, -0.6f, -0.5f)
                curveToRelative(-0.9f, 0.5f, -1.9f, 0.5f, -2.6f, -0.2f)
                curveToRelative(0.4f, -0.5f, 0.9f, -1f, 1.3f, -1.5f)
                curveToRelative(0.2f, 0.2f, 0.6f, 0.2f, 0.8f, -0.1f)
                reflectiveCurveToRelative(0.2f, -0.7f, 0f, -0.9f)
                curveToRelative(-0.2f, -0.2f, -0.6f, -0.2f, -0.8f, 0.1f)
                curveToRelative(-0.5f, 0.6f, -1.3f, 1f, -2f, 1f)
                curveToRelative(-0.5f, 0f, -1f, -0.2f, -1.4f, -0.5f)
                curveToRelative(-0.7f, -0.7f, -0.9f, -1.7f, -0.5f, -2.7f)
                curveToRelative(-0.2f, -0.1f, -0.3f, -0.3f, -0.5f, -0.5f)
                curveToRelative(0.4f, -0.4f, 0.8f, -0.9f, 1.1f, -1.4f)
                curveToRelative(0f, 0.2f, 0.2f, 0.3f, 0.4f, 0.5f)
            }
            path(fill = SolidColor(Color(0xFF699635))) {
                moveTo(40.2f, 35.5f)
                curveToRelative(-3.2f, 2.9f, -8.6f, 2.8f, -11.7f, 0f)
                curveToRelative(-3.1f, -2.9f, -2.9f, -7.4f, 0.3f, -10.3f)
                curveToRelative(3.2f, -2.9f, 8.2f, -3.2f, 11.3f, -0.3f)
                curveToRelative(3.2f, 2.8f, 3.3f, 7.7f, 0.1f, 10.6f)
            }
            path(fill = SolidColor(Color(0xFFFFDD7D))) {
                moveTo(21.8f, 24.5f)
                lineToRelative(18.9f, 17.3f)
                curveToRelative(2.2f, -1.8f, 4.2f, -3.7f, 6.2f, -5.7f)
                lineTo(28.1f, 18.8f)
                curveToRelative(-2f, 1.9f, -4.1f, 3.8f, -6.3f, 5.7f)
            }
            path(fill = SolidColor(Color(0xFFDBB471))) {
                moveTo(40.8f, 49.4f)
                curveToRelative(2.1f, -1.6f, 4.2f, -3.3f, 6.2f, -5f)
                verticalLineTo(36f)
                curveToRelative(-2f, 2f, -4.1f, 3.9f, -6.2f, 5.7f)
                verticalLineToRelative(7.7f)
            }
            path(fill = SolidColor(Color(0xFF8D9998))) {
                moveTo(12.1f, 24.7f)
                curveToRelative(1.1f, -0.4f, 2f, -1f, 2.9f, -1.7f)
                curveToRelative(0.9f, -0.7f, 1.7f, -1.4f, 2.6f, -2.2f)
                curveToRelative(0.9f, -0.7f, 1.8f, -1.5f, 2.8f, -2f)
                curveToRelative(1f, -0.6f, 2.3f, -0.9f, 3.4f, -0.7f)
                curveToRelative(-1.1f, 0.2f, -2.1f, 0.7f, -3f, 1.4f)
                curveToRelative(-0.9f, 0.6f, -1.7f, 1.4f, -2.6f, 2.1f)
                curveToRelative(-0.9f, 0.7f, -1.8f, 1.5f, -2.8f, 2.1f)
                curveToRelative(-1f, 0.6f, -2.2f, 1f, -3.3f, 1f)
            }
            path(fill = SolidColor(Color(0xFFE8E8E8))) {
                moveTo(39.8f, 33.1f)
                reflectiveCurveToRelative(4.9f, 1.4f, 16.3f, -1.6f)
                reflectiveCurveToRelative(6f, 29.8f, -14.8f, 29.8f)
                curveToRelative(-2.3f, 0f, -2.4f, -2.9f, 4f, -8.1f)
                curveToRelative(0f, 0f, -9f, 2.1f, -5.1f, -4.6f)
                curveToRelative(0f, 0f, -6.4f, 1.1f, -3.5f, -4f)
                curveToRelative(0f, 0f, 1.3f, -3.8f, 0.1f, -8.1f)
                curveToRelative(0.1f, 0f, 0.8f, -2.2f, 3f, -3.4f)
            }
            path(fill = SolidColor(Color(0xFFD1D1D1))) {
                moveTo(61.5f, 34.7f)
                curveToRelative(0.2f, 8.8f, -6.8f, 23.4f, -20.7f, 23.4f)
                horizontalLineToRelative(-0.2f)
                curveToRelative(-1.3f, 2.1f, -0.7f, 3.3f, 0.7f, 3.3f)
                curveToRelative(15.8f, -0.1f, 22.7f, -18.9f, 20.2f, -26.7f)
            }
            path(fill = SolidColor(Color(0xFFD1D1D1))) {
                moveTo(54.3f, 39.5f)
                curveToRelative(-3f, 10.8f, -15f, 11.7f, -15f, 11.7f)
                curveToRelative(-0.5f, 2.3f, 0.5f, 3.3f, 1.8f, 3.3f)
                curveToRelative(4.4f, 0.1f, 12.6f, -7.2f, 13.2f, -15f)
            }
            path(fill = SolidColor(Color(0xFFD1D1D1))) {
                moveTo(45.9f, 39.5f)
                curveToRelative(-7.1f, 10f, -9.6f, 6.3f, -9.6f, 6.3f)
                curveToRelative(-1.8f, 4f, 2.9f, 4f, 3.7f, 3.1f)
                curveToRelative(2.3f, -2.5f, 6f, -7.7f, 5.9f, -9.4f)
            }
            path(fill = SolidColor(Color(0xFF8D9998))) {
                moveTo(40.5f, 58.4f)
                curveToRelative(1.2f, -1.6f, 2.7f, -2.9f, 4.2f, -4.2f)
                curveToRelative(1.5f, -1.3f, 3f, -2.6f, 4.5f, -4f)
                curveToRelative(1.4f, -1.4f, 2.8f, -3f, 3.8f, -4.8f)
                curveToRelative(1f, -1.8f, 1.5f, -3.9f, 1.3f, -5.9f)
                curveToRelative(-0.2f, 2f, -0.9f, 3.9f, -2f, 5.5f)
                reflectiveCurveToRelative(-2.4f, 3.1f, -3.8f, 4.5f)
                reflectiveCurveToRelative(-2.9f, 2.7f, -4.3f, 4.2f)
                curveToRelative(-1.5f, 1.3f, -2.8f, 2.9f, -3.7f, 4.7f)
            }
            path(fill = SolidColor(Color(0xFF8D9998))) {
                moveTo(39.3f, 51.2f)
                curveToRelative(0.4f, -1.1f, 1f, -2f, 1.7f, -2.9f)
                curveToRelative(0.7f, -0.9f, 1.4f, -1.7f, 2.2f, -2.6f)
                curveToRelative(0.7f, -0.9f, 1.5f, -1.8f, 2f, -2.8f)
                curveToRelative(0.6f, -1f, 0.9f, -2.3f, 0.7f, -3.4f)
                curveToRelative(-0.2f, 1.1f, -0.7f, 2.1f, -1.4f, 3f)
                curveToRelative(-0.6f, 0.9f, -1.4f, 1.7f, -2.1f, 2.6f)
                curveToRelative(-0.7f, 0.9f, -1.5f, 1.8f, -2.1f, 2.8f)
                curveToRelative(-0.6f, 1f, -1f, 2.2f, -1f, 3.3f)
            }
            path(fill = SolidColor(Color(0xFFE8E8E8))) {
                moveTo(30.2f, 24.2f)
                reflectiveCurveToRelative(-1.4f, -4.9f, 1.6f, -16.3f)
                reflectiveCurveTo(2f, 1.9f, 2f, 22.7f)
                curveToRelative(0f, 2.3f, 2.9f, 2.4f, 8.1f, -4f)
                curveToRelative(0f, 0f, -2.1f, 9f, 4.6f, 5.1f)
                curveToRelative(0f, 0f, -1.1f, 6.4f, 4f, 3.5f)
                curveToRelative(0f, 0f, 3.8f, -1.3f, 8.1f, -0.1f)
                curveToRelative(0f, -0.1f, 2.2f, -0.8f, 3.4f, -3f)
            }
            path(fill = SolidColor(Color(0xFFD1D1D1))) {
                moveTo(28.6f, 2.5f)
                curveTo(19.8f, 2.3f, 5.2f, 9.3f, 5.2f, 23.2f)
                verticalLineToRelative(0.2f)
                curveToRelative(-2.1f, 1.2f, -3.2f, 0.7f, -3.2f, -0.7f)
                curveTo(2f, 6.9f, 20.8f, 0f, 28.6f, 2.5f)
            }
            path(fill = SolidColor(Color(0xFFD1D1D1))) {
                moveTo(23.8f, 9.7f)
                curveToRelative(-10.8f, 3f, -11.7f, 15f, -11.7f, 15f)
                curveToRelative(-2.3f, 0.5f, -3.3f, -0.5f, -3.3f, -1.8f)
                curveToRelative(-0.1f, -4.4f, 7.2f, -12.6f, 15f, -13.2f)
            }
            path(fill = SolidColor(Color(0xFFD1D1D1))) {
                moveTo(23.8f, 18.1f)
                curveToRelative(-10f, 7.1f, -6.3f, 9.6f, -6.3f, 9.6f)
                curveToRelative(-4f, 1.8f, -4f, -2.9f, -3.1f, -3.7f)
                curveToRelative(2.5f, -2.3f, 7.7f, -6f, 9.4f, -5.9f)
            }
            path(fill = SolidColor(Color(0xFF8D9998))) {
                moveTo(4.9f, 23.5f)
                curveToRelative(1.6f, -1.2f, 2.9f, -2.7f, 4.2f, -4.2f)
                curveToRelative(1.3f, -1.5f, 2.6f, -3f, 4f, -4.5f)
                curveToRelative(1.4f, -1.4f, 3f, -2.8f, 4.8f, -3.8f)
                curveToRelative(1.8f, -1f, 3.9f, -1.5f, 5.9f, -1.3f)
                curveToRelative(-2f, 0.2f, -3.9f, 0.9f, -5.5f, 2f)
                reflectiveCurveToRelative(-3.1f, 2.4f, -4.5f, 3.8f)
                reflectiveCurveToRelative(-2.7f, 2.9f, -4.2f, 4.3f)
                curveToRelative(-1.4f, 1.5f, -2.9f, 2.8f, -4.7f, 3.7f)
            }
            path(fill = SolidColor(Color(0xFF8D9998))) {
                moveTo(12.1f, 24.7f)
                curveToRelative(1.1f, -0.4f, 2f, -1f, 2.9f, -1.7f)
                curveToRelative(0.9f, -0.7f, 1.7f, -1.4f, 2.6f, -2.2f)
                curveToRelative(0.9f, -0.7f, 1.8f, -1.5f, 2.8f, -2f)
                curveToRelative(1f, -0.6f, 2.3f, -0.9f, 3.4f, -0.7f)
                curveToRelative(-1.1f, 0.2f, -2.1f, 0.7f, -3f, 1.4f)
                curveToRelative(-0.9f, 0.6f, -1.7f, 1.4f, -2.6f, 2.1f)
                curveToRelative(-0.9f, 0.7f, -1.8f, 1.5f, -2.8f, 2.1f)
                curveToRelative(-1f, 0.6f, -2.2f, 1f, -3.3f, 1f)
            }
        }.build()

        return _Money!!
    }

@Suppress("ObjectPropertyName")
private var _Money: ImageVector? = null
