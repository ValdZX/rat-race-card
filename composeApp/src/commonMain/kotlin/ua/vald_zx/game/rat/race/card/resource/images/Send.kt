package ua.vald_zx.game.rat.race.card.resource.images

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.resource.Images

val Images.Send: ImageVector
    get() {
        if (_Send != null) {
            return _Send!!
        }
        _Send = ImageVector.Builder(
            name = "Send",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF0F0F0F))) {
                moveTo(13.309f, 0.293f)
                curveTo(13.699f, -0.098f, 14.332f, -0.098f, 14.723f, 0.293f)
                lineTo(17.719f, 3.291f)
                curveTo(18.109f, 3.682f, 18.109f, 4.315f, 17.719f, 4.706f)
                lineTo(14.716f, 7.71f)
                curveTo(14.325f, 8.101f, 13.692f, 8.101f, 13.302f, 7.71f)
                curveTo(12.911f, 7.32f, 12.911f, 6.686f, 13.302f, 6.295f)
                lineTo(14.609f, 4.987f)
                lineTo(7f, 4.987f)
                curveTo(6.448f, 4.987f, 6f, 4.539f, 6f, 3.987f)
                curveTo(6f, 3.434f, 6.448f, 2.986f, 7f, 2.986f)
                lineTo(14.585f, 2.986f)
                lineTo(13.309f, 1.708f)
                curveTo(12.918f, 1.317f, 12.918f, 0.684f, 13.309f, 0.293f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(12f, 20.998f)
                curveTo(14.209f, 20.998f, 16f, 19.206f, 16f, 16.995f)
                curveTo(16f, 14.785f, 14.209f, 12.993f, 12f, 12.993f)
                curveTo(9.791f, 12.993f, 8f, 14.785f, 8f, 16.995f)
                curveTo(8f, 19.206f, 9.791f, 20.998f, 12f, 20.998f)
                close()
                moveTo(12f, 19.093f)
                curveTo(10.842f, 19.093f, 9.903f, 18.154f, 9.903f, 16.995f)
                curveTo(9.903f, 15.837f, 10.842f, 14.897f, 12f, 14.897f)
                curveTo(13.158f, 14.897f, 14.097f, 15.837f, 14.097f, 16.995f)
                curveTo(14.097f, 18.154f, 13.158f, 19.093f, 12f, 19.093f)
                close()
            }
            path(fill = SolidColor(Color(0xFF0F0F0F))) {
                moveTo(7f, 16.995f)
                curveTo(7f, 17.548f, 6.552f, 17.996f, 6f, 17.996f)
                curveTo(5.448f, 17.996f, 5f, 17.548f, 5f, 16.995f)
                curveTo(5f, 16.443f, 5.448f, 15.995f, 6f, 15.995f)
                curveTo(6.552f, 15.995f, 7f, 16.443f, 7f, 16.995f)
                close()
            }
            path(fill = SolidColor(Color(0xFF0F0F0F))) {
                moveTo(19f, 16.995f)
                curveTo(19f, 17.548f, 18.552f, 17.996f, 18f, 17.996f)
                curveTo(17.448f, 17.996f, 17f, 17.548f, 17f, 16.995f)
                curveTo(17f, 16.443f, 17.448f, 15.995f, 18f, 15.995f)
                curveTo(18.552f, 15.995f, 19f, 16.443f, 19f, 16.995f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(21f, 9.991f)
                curveTo(22.657f, 9.991f, 24f, 11.335f, 24f, 12.993f)
                verticalLineTo(20.998f)
                curveTo(24f, 22.656f, 22.657f, 24f, 21f, 24f)
                horizontalLineTo(3f)
                curveTo(1.343f, 24f, 0f, 22.656f, 0f, 20.998f)
                verticalLineTo(12.993f)
                curveTo(0f, 11.335f, 1.343f, 9.991f, 3f, 9.991f)
                horizontalLineTo(21f)
                close()
                moveTo(4f, 11.992f)
                horizontalLineTo(20f)
                curveTo(20f, 12.255f, 20.052f, 12.515f, 20.152f, 12.758f)
                curveTo(20.253f, 13.001f, 20.4f, 13.221f, 20.586f, 13.407f)
                curveTo(20.771f, 13.593f, 20.992f, 13.741f, 21.235f, 13.841f)
                curveTo(21.477f, 13.942f, 21.737f, 13.993f, 22f, 13.993f)
                verticalLineTo(19.997f)
                curveTo(21.737f, 19.997f, 21.477f, 20.049f, 21.235f, 20.15f)
                curveTo(20.992f, 20.25f, 20.771f, 20.398f, 20.586f, 20.583f)
                curveTo(20.4f, 20.769f, 20.253f, 20.99f, 20.152f, 21.233f)
                curveTo(20.052f, 21.476f, 20f, 21.736f, 20f, 21.999f)
                horizontalLineTo(4f)
                curveTo(4f, 21.736f, 3.948f, 21.476f, 3.848f, 21.233f)
                curveTo(3.747f, 20.99f, 3.6f, 20.769f, 3.414f, 20.583f)
                curveTo(3.228f, 20.398f, 3.008f, 20.25f, 2.765f, 20.15f)
                curveTo(2.523f, 20.049f, 2.263f, 19.997f, 2f, 19.997f)
                verticalLineTo(13.993f)
                curveTo(2.263f, 13.993f, 2.523f, 13.942f, 2.765f, 13.841f)
                curveTo(3.008f, 13.741f, 3.228f, 13.593f, 3.414f, 13.407f)
                curveTo(3.6f, 13.221f, 3.747f, 13.001f, 3.848f, 12.758f)
                curveTo(3.948f, 12.515f, 4f, 12.255f, 4f, 11.992f)
                close()
            }
        }.build()

        return _Send!!
    }

@Suppress("ObjectPropertyName")
private var _Send: ImageVector? = null