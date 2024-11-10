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

val Images.Dice: ImageVector
    get() {
        if (_dice != null) {
            return _dice!!
        }
        _dice = Builder(name = "Dice", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 512.0f, viewportHeight = 512.0f).apply {
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(454.609f, 111.204f)
                lineTo(280.557f, 6.804f)
                curveTo(272.992f, 2.268f, 264.503f, 0.0f, 255.999f, 0.0f)
                curveToRelative(-8.507f, 0.0f, -16.995f, 2.268f, -24.557f, 6.796f)
                lineTo(57.391f, 111.204f)
                curveToRelative(-5.346f, 3.202f, -9.917f, 7.369f, -13.556f, 12.192f)
                lineToRelative(207.904f, 124.708f)
                curveToRelative(2.622f, 1.575f, 5.9f, 1.575f, 8.519f, 0.0f)
                lineTo(468.16f, 123.396f)
                curveTo(464.524f, 118.573f, 459.951f, 114.406f, 454.609f, 111.204f)
                close()
                moveTo(157.711f, 130.313f)
                curveToRelative(-10.96f, 7.611f, -28.456f, 7.422f, -39.081f, -0.452f)
                curveToRelative(-10.618f, -7.859f, -10.342f, -20.413f, 0.618f, -28.031f)
                curveToRelative(10.964f, -7.626f, 28.46f, -7.422f, 39.081f, 0.438f)
                curveTo(168.95f, 110.134f, 168.674f, 122.68f, 157.711f, 130.313f)
                close()
                moveTo(274.159f, 131.021f)
                curveToRelative(-10.594f, 7.362f, -27.496f, 7.166f, -37.762f, -0.429f)
                curveToRelative(-10.263f, -7.596f, -9.992f, -19.727f, 0.599f, -27.089f)
                curveToRelative(10.591f, -7.362f, 27.492f, -7.174f, 37.759f, 0.43f)
                curveTo(285.018f, 111.528f, 284.75f, 123.659f, 274.159f, 131.021f)
                close()
                moveTo(391.908f, 132.702f)
                curveToRelative(-10.964f, 7.618f, -28.461f, 7.414f, -39.085f, -0.444f)
                curveToRelative(-10.617f, -7.86f, -10.343f, -20.42f, 0.621f, -28.046f)
                curveToRelative(10.957f, -7.61f, 28.456f, -7.422f, 39.078f, 0.452f)
                curveTo(403.147f, 112.523f, 402.868f, 125.076f, 391.908f, 132.702f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(246.136f, 258.366f)
                lineTo(38.007f, 133.523f)
                curveToRelative(-2.46f, 5.802f, -3.798f, 12.117f, -3.798f, 18.62f)
                verticalLineToRelative(208.084f)
                curveToRelative(0.0f, 16.773f, 8.797f, 32.311f, 23.182f, 40.946f)
                lineToRelative(174.051f, 104.392f)
                curveToRelative(5.829f, 3.497f, 12.204f, 5.629f, 18.714f, 6.435f)
                verticalLineTo(265.464f)
                curveTo(250.156f, 262.556f, 248.63f, 259.858f, 246.136f, 258.366f)
                close()
                moveTo(75.845f, 369.736f)
                curveToRelative(-12.056f, -6.57f, -21.829f, -21.671f, -21.829f, -33.727f)
                curveToRelative(0.0f, -12.056f, 9.773f, -16.502f, 21.829f, -9.932f)
                curveToRelative(12.056f, 6.571f, 21.826f, 21.671f, 21.826f, 33.728f)
                curveTo(97.671f, 371.861f, 87.901f, 376.307f, 75.845f, 369.736f)
                close()
                moveTo(75.845f, 247.87f)
                curveToRelative(-12.056f, -6.579f, -21.829f, -21.679f, -21.829f, -33.728f)
                curveToRelative(0.0f, -12.056f, 9.773f, -16.502f, 21.829f, -9.931f)
                curveToRelative(12.056f, 6.57f, 21.826f, 21.671f, 21.826f, 33.728f)
                curveTo(97.671f, 249.987f, 87.901f, 254.44f, 75.845f, 247.87f)
                close()
                moveTo(197.715f, 436.158f)
                curveToRelative(-12.052f, -6.57f, -21.826f, -21.671f, -21.826f, -33.728f)
                curveToRelative(0.0f, -12.048f, 9.773f, -16.494f, 21.826f, -9.924f)
                curveToRelative(12.056f, 6.571f, 21.826f, 21.671f, 21.826f, 33.72f)
                curveTo(219.541f, 438.284f, 209.771f, 442.729f, 197.715f, 436.158f)
                close()
                moveTo(197.715f, 314.292f)
                curveToRelative(-12.052f, -6.571f, -21.826f, -21.671f, -21.826f, -33.728f)
                reflectiveCurveToRelative(9.773f, -16.502f, 21.826f, -9.931f)
                curveToRelative(12.056f, 6.57f, 21.826f, 21.671f, 21.826f, 33.727f)
                curveTo(219.541f, 316.417f, 209.771f, 320.862f, 197.715f, 314.292f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(473.993f, 133.523f)
                lineToRelative(-208.13f, 124.843f)
                curveToRelative(-2.494f, 1.492f, -4.02f, 4.19f, -4.02f, 7.099f)
                verticalLineTo(512.0f)
                curveToRelative(6.511f, -0.806f, 12.886f, -2.938f, 18.714f, -6.435f)
                lineToRelative(174.052f, -104.392f)
                curveToRelative(14.38f, -8.635f, 23.182f, -24.173f, 23.182f, -40.946f)
                verticalLineTo(152.142f)
                curveTo(477.791f, 145.64f, 476.453f, 139.325f, 473.993f, 133.523f)
                close()
                moveTo(370.478f, 355.11f)
                curveToRelative(-19.287f, 10.512f, -34.922f, 3.398f, -34.922f, -15.892f)
                curveToRelative(0.0f, -19.282f, 15.635f, -43.447f, 34.922f, -53.951f)
                curveToRelative(19.293f, -10.519f, 34.925f, -3.406f, 34.925f, 15.884f)
                curveTo(405.403f, 320.434f, 389.771f, 344.598f, 370.478f, 355.11f)
                close()
            }
        }
        .build()
        return _dice!!
    }

private var _dice: ImageVector? = null
