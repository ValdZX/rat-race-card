package ua.vald_zx.game.rat.race.card.resource.images

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.resource.Images

val Images.Repay: ImageVector
    get() {
        if (_Repay != null) {
            return _Repay!!
        }
        _Repay = ImageVector.Builder(
            name = "Repay",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 193.77f,
            viewportHeight = 193.77f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(149.2f, 41.1f)
                lineToRelative(-9.35f, 12.01f)
                curveToRelative(20.15f, 15.68f, 30.2f, 41.06f, 26.23f, 66.25f)
                curveToRelative(-2.91f, 18.48f, -12.84f, 34.73f, -27.96f, 45.75f)
                curveToRelative(-15.13f, 11.01f, -33.64f, 15.49f, -52.12f, 12.57f)
                curveToRelative(-38.16f, -6.01f, -64.32f, -41.94f, -58.32f, -80.1f)
                curveTo(30.58f, 79.1f, 40.52f, 62.85f, 55.65f, 51.83f)
                curveToRelative(13.21f, -9.61f, 28.99f, -14.23f, 45.09f, -13.32f)
                lineTo(87.58f, 52.32f)
                lineToRelative(9.76f, 9.31f)
                lineToRelative(20.77f, -21.8f)
                lineToRelative(0f, 0.01f)
                lineToRelative(9.3f, -9.77f)
                lineToRelative(-9.75f, -9.3f)
                lineToRelative(-0f, 0f)
                lineTo(95.86f, 0f)
                lineToRelative(-9.31f, 9.77f)
                lineToRelative(14.2f, 13.52f)
                curveToRelative(-19.3f, -0.91f, -38.21f, 4.7f, -54.06f, 16.24f)
                curveTo(28.28f, 52.94f, 16.19f, 72.72f, 12.65f, 95.22f)
                curveToRelative(-7.3f, 46.44f, 24.54f, 90.18f, 70.99f, 97.49f)
                curveToRelative(4.49f, 0.71f, 8.98f, 1.05f, 13.43f, 1.05f)
                curveToRelative(17.89f, 0f, 35.27f, -5.62f, 50.01f, -16.36f)
                curveToRelative(18.42f, -13.41f, 30.5f, -33.18f, 34.04f, -55.68f)
                curveTo(185.95f, 91.08f, 173.72f, 60.18f, 149.2f, 41.1f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(105.24f, 151.97f)
                verticalLineToRelative(-0f)
                horizontalLineToRelative(0f)
                verticalLineToRelative(-8.76f)
                curveToRelative(10.38f, -1.16f, 20.49f, -7.72f, 20.49f, -20.17f)
                curveToRelative(0f, -16.92f, -15.73f, -18.86f, -27.22f, -20.27f)
                curveToRelative(-7.35f, -0.88f, -12.97f, -1.9f, -12.97f, -6.35f)
                curveToRelative(0f, -6.19f, 8.72f, -6.86f, 12.47f, -6.86f)
                curveToRelative(5.57f, 0f, 11.51f, 2.62f, 13.52f, 5.96f)
                lineToRelative(0.59f, 0.97f)
                lineToRelative(11.54f, -5.34f)
                lineToRelative(-0.57f, -1.16f)
                curveToRelative(-4.3f, -8.79f, -12.01f, -11.34f, -17.85f, -12.36f)
                verticalLineToRelative(-7.71f)
                horizontalLineTo(91.72f)
                verticalLineToRelative(7.68f)
                curveToRelative(-12.58f, 1.86f, -20.05f, 8.84f, -20.05f, 18.83f)
                curveToRelative(0f, 16.29f, 14.79f, 17.94f, 25.58f, 19.15f)
                curveToRelative(9.62f, 1.13f, 14.09f, 3.51f, 14.09f, 7.47f)
                curveToRelative(0f, 7.56f, -10.47f, 8.15f, -13.69f, 8.15f)
                curveToRelative(-7.15f, 0f, -14.04f, -3.57f, -16.03f, -8.3f)
                lineToRelative(-0.5f, -1.17f)
                lineToRelative(-12.54f, 5.32f)
                lineToRelative(0.5f, 1.17f)
                curveToRelative(3.71f, 8.69f, 11.73f, 14.14f, 22.63f, 15.43f)
                verticalLineToRelative(8.34f)
                horizontalLineTo(105.24f)
                close()
            }
        }.build()

        return _Repay!!
    }

@Suppress("ObjectPropertyName")
private var _Repay: ImageVector? = null
