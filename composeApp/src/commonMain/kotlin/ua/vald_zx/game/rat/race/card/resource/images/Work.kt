package ua.vald_zx.game.rat.race.card.resource.images

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.resource.Images

val Images.Work: ImageVector
    get() {
        if (_Work != null) {
            return _Work!!
        }
        _Work = ImageVector.Builder(
            name = "Work",
            defaultWidth = 48.dp,
            defaultHeight = 48.dp,
            viewportWidth = 6.35f,
            viewportHeight = 6.35f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFCFA15A)),
                strokeLineWidth = 0.26458335f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(1.2277f, 1.551f)
                curveToRelative(-0.237f, 0f, -0.4316f, 0.1946f, -0.4316f, 0.4316f)
                verticalLineToRelative(2.8398f)
                curveToRelative(0f, 0.2304f, 0.1883f, 0.4316f, 0.4316f, 0.4316f)
                horizontalLineToRelative(3.8945f)
                curveToRelative(0.2433f, 0f, 0.4316f, -0.2013f, 0.4316f, -0.4316f)
                verticalLineToRelative(-2.8398f)
                curveToRelative(0f, -0.237f, -0.1946f, -0.4316f, -0.4316f, -0.4316f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF615535)),
                strokeLineWidth = 0.26458335f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(2.7609f, 1.0959f)
                curveToRelative(-0.1091f, 0f, -0.2089f, 0.03f, -0.2891f, 0.0879f)
                curveToRelative(-0.0802f, 0.0579f, -0.1426f, 0.1514f, -0.1426f, 0.2598f)
                curveToRelative(0f, 0.11f, 0.0598f, 0.2089f, 0.1406f, 0.2695f)
                curveToRelative(0.0809f, 0.0607f, 0.1819f, 0.0898f, 0.291f, 0.0898f)
                horizontalLineToRelative(0.8281f)
                curveToRelative(0.1091f, 0f, 0.2101f, -0.0292f, 0.291f, -0.0898f)
                curveToRelative(0.0809f, -0.0607f, 0.1406f, -0.1595f, 0.1406f, -0.2695f)
                curveToRelative(0f, -0.1084f, -0.0624f, -0.2019f, -0.1426f, -0.2598f)
                curveToRelative(-0.0802f, -0.0579f, -0.18f, -0.0879f, -0.2891f, -0.0879f)
                close()
                moveTo(2.7609f, 1.3596f)
                horizontalLineToRelative(0.8281f)
                curveToRelative(0.0586f, 0f, 0.1071f, 0.0191f, 0.1348f, 0.0391f)
                curveToRelative(0.0277f, 0.02f, 0.0313f, 0.0335f, 0.0313f, 0.0449f)
                curveToRelative(0f, 0.0218f, -0.0063f, 0.0364f, -0.0332f, 0.0566f)
                curveToRelative(-0.0269f, 0.0202f, -0.0742f, 0.0391f, -0.1328f, 0.0391f)
                lineTo(2.7609f, 1.5392f)
                curveToRelative(-0.0587f, 0f, -0.1059f, -0.0188f, -0.1328f, -0.0391f)
                curveToRelative(-0.027f, -0.0202f, -0.0332f, -0.0349f, -0.0332f, -0.0566f)
                curveToRelative(0f, -0.0114f, 0.0036f, -0.0249f, 0.0313f, -0.0449f)
                curveToRelative(0.0277f, -0.02f, 0.0761f, -0.0391f, 0.1348f, -0.0391f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFC18538)),
                strokeLineWidth = 0.26458335f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(1.2281f, 1.5506f)
                curveToRelative(-0.2371f, 0f, -0.4315f, 0.1951f, -0.4315f, 0.432f)
                verticalLineToRelative(0.4392f)
                curveToRelative(0f, 0.023f, 0.0061f, 0.0456f, 0.0176f, 0.0656f)
                curveToRelative(0.0492f, 0.0862f, 0.0964f, 0.1684f, 0.1106f, 0.1922f)
                curveToRelative(-0.0047f, -0.008f, 0.0176f, 0.031f, 0.0176f, 0.031f)
                curveToRelative(0.0235f, 0.0415f, 0.0675f, 0.0672f, 0.1152f, 0.0672f)
                horizontalLineToRelative(0.0424f)
                lineToRelative(2.0257f, 0.7726f)
                curveToRelative(0.0299f, 0.0114f, 0.063f, 0.0116f, 0.093f, 5.0E-4f)
                lineToRelative(2.1182f, -0.7813f)
                curveToRelative(0.0044f, -0.002f, 0.0087f, -0.004f, 0.0129f, -0.006f)
                lineToRelative(0.1307f, -0.0651f)
                curveToRelative(0.0449f, -0.0223f, 0.0733f, -0.0682f, 0.0734f, -0.1183f)
                verticalLineToRelative(-0.5979f)
                curveToRelative(0f, -0.2371f, -0.1949f, -0.432f, -0.432f, -0.432f)
                close()
                moveTo(3.1737f, 3.2859f)
                curveToRelative(-2.1163f, 2.0426f, -1.0582f, 1.0213f, 0f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF9A6A44)),
                strokeLineWidth = 0.26458335f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(3.0734f, 2.8693f)
                curveToRelative(-0.237f, 0f, -0.4316f, 0.1946f, -0.4316f, 0.4316f)
                verticalLineToRelative(0.2031f)
                curveToRelative(0f, 0.237f, 0.1946f, 0.4316f, 0.4316f, 0.4316f)
                lineTo(3.2648f, 3.9357f)
                curveToRelative(0.237f, 0f, 0.4316f, -0.1946f, 0.4316f, -0.4316f)
                verticalLineToRelative(-0.2031f)
                curveToRelative(0f, -0.237f, -0.1946f, -0.4316f, -0.4316f, -0.4316f)
                close()
            }
        }.build()

        return _Work!!
    }

@Suppress("ObjectPropertyName")
private var _Work: ImageVector? = null
