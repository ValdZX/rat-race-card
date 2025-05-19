package ua.vald_zx.game.rat.race.card.resource.images

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.resource.Images

val Images.Mariage: ImageVector
    get() {
        if (_Mariage != null) {
            return _Mariage!!
        }
        _Mariage = ImageVector.Builder(
            name = "Mariage",
            defaultWidth = 48.dp,
            defaultHeight = 48.dp,
            viewportWidth = 72f,
            viewportHeight = 72f
        ).apply {
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFF3E4154),
                        1f to Color(0xFF1B2129)
                    ),
                    start = Offset(50f, 6.394f),
                    end = Offset(50f, 10.881f)
                )
            ) {
                moveTo(50f, 11f)
                moveToRelative(-5f, 0f)
                arcToRelative(5f, 5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 10f, 0f)
                arcToRelative(5f, 5f, 0f, isMoreThanHalf = true, isPositiveArc = true, -10f, 0f)
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFDB686B),
                        1f to Color(0xFFC23542)
                    ),
                    start = Offset(50f, 10.199f),
                    end = Offset(50f, 13.138f)
                )
            ) {
                moveTo(50f, 16f)
                moveToRelative(-6f, 0f)
                arcToRelative(6f, 6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 12f, 0f)
                arcToRelative(6f, 6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -12f, 0f)
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFF3E4154),
                        1f to Color(0xFF1B2129)
                    ),
                    start = Offset(50f, 11.75f),
                    end = Offset(50f, 27.795f)
                )
            ) {
                moveTo(50f, 24f)
                moveToRelative(-13f, 0f)
                arcToRelative(13f, 13f, 0f, isMoreThanHalf = true, isPositiveArc = true, 26f, 0f)
                arcToRelative(13f, 13f, 0f, isMoreThanHalf = true, isPositiveArc = true, -26f, 0f)
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFFFFFFF),
                        0.345f to Color(0xFFF5F7F7),
                        1f to Color(0xFFE6EAEB)
                    ),
                    start = Offset(50f, 9.286f),
                    end = Offset(50f, 60.322f)
                )
            ) {
                moveTo(54f, 40f)
                horizontalLineTo(46f)
                arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 34f, 52f)
                verticalLineToRelative(3f)
                arcToRelative(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6f, 6f)
                horizontalLineTo(60f)
                arcToRelative(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6f, -6f)
                verticalLineTo(52f)
                arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 54f, 40f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFEDF1F2),
                        1f to Color(0xFFC6CED0)
                    ),
                    start = Offset(50f, 37.732f),
                    end = Offset(50f, 48.559f)
                )
            ) {
                moveTo(54f, 40f)
                horizontalLineTo(46f)
                arcToRelative(12.041f, 12.041f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.5f, 0.265f)
                verticalLineTo(41.5f)
                arcToRelative(6.5f, 6.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 13f, 0f)
                verticalLineTo(40.265f)
                arcTo(12.041f, 12.041f, 0f, isMoreThanHalf = false, isPositiveArc = false, 54f, 40f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFF1D2BD),
                        1f to Color(0xFFFEB592)
                    ),
                    start = Offset(50f, 39.21f),
                    end = Offset(50f, 45.478f)
                )
            ) {
                moveTo(45.5f, 37f)
                horizontalLineToRelative(9f)
                verticalLineToRelative(4.5f)
                arcTo(4.5f, 4.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 50f, 46f)
                horizontalLineToRelative(0f)
                arcToRelative(4.5f, 4.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, -4.5f, -4.5f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFF1D2BD),
                        1f to Color(0xFFFEB592)
                    ),
                    start = Offset(50f, 22.983f),
                    end = Offset(50f, 40.745f)
                )
            ) {
                moveTo(50f, 40.8f)
                horizontalLineToRelative(0f)
                arcTo(10.079f, 10.079f, 0f, isMoreThanHalf = false, isPositiveArc = true, 40f, 30.644f)
                verticalLineTo(24.956f)
                arcTo(10.079f, 10.079f, 0f, isMoreThanHalf = false, isPositiveArc = true, 50f, 14.8f)
                horizontalLineToRelative(0f)
                arcTo(10.079f, 10.079f, 0f, isMoreThanHalf = false, isPositiveArc = true, 60f, 24.956f)
                verticalLineToRelative(5.688f)
                arcTo(10.079f, 10.079f, 0f, isMoreThanHalf = false, isPositiveArc = true, 50f, 40.8f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFF3E4154),
                        1f to Color(0xFF1B2129)
                    ),
                    start = Offset(50f, 11.75f),
                    end = Offset(50f, 27.795f)
                )
            ) {
                moveTo(60.4f, 29.081f)
                lineToRelative(-1.733f, -3.466f)
                arcToRelative(6.157f, 6.157f, 0f, isMoreThanHalf = false, isPositiveArc = true, -5.2f, -3.467f)
                reflectiveCurveTo(50f, 25.615f, 41.333f, 25.615f)
                lineTo(39.6f, 29.081f)
                verticalLineTo(23.8f)
                arcTo(10.4f, 10.4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 50f, 13.4f)
                horizontalLineToRelative(0f)
                arcTo(10.4f, 10.4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 60.4f, 23.8f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFF1D2BD),
                        1f to Color(0xFFFEB592)
                    ),
                    start = Offset(61f, 23.304f),
                    end = Offset(61f, 39.7f)
                )
            ) {
                moveTo(61f, 27f)
                lineTo(61f, 27f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 64f, 30f)
                lineTo(64f, 30f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 61f, 33f)
                lineTo(61f, 33f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 58f, 30f)
                lineTo(58f, 30f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 61f, 27f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFF1D2BD),
                        1f to Color(0xFFFEB592)
                    ),
                    start = Offset(39f, 23.304f),
                    end = Offset(39f, 39.7f)
                )
            ) {
                moveTo(39f, 27f)
                lineTo(39f, 27f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 42f, 30f)
                lineTo(42f, 30f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 39f, 33f)
                lineTo(39f, 33f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 36f, 30f)
                lineTo(36f, 30f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 39f, 27f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFDB686B),
                        1f to Color(0xFFC23542)
                    ),
                    start = Offset(50.075f, 33.364f),
                    end = Offset(50.075f, 36.547f)
                )
            ) {
                moveTo(47.237f, 33.4f)
                arcToRelative(0.805f, 0.805f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.684f, 1.223f)
                arcToRelative(4.126f, 4.126f, 0f, isMoreThanHalf = false, isPositiveArc = false, 7.044f, 0f)
                arcToRelative(0.805f, 0.805f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.684f, -1.223f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFFFFFFF),
                        1f to Color(0xFFEDF1F2)
                    ),
                    start = Offset(54f, 26.932f),
                    end = Offset(54f, 31.224f)
                )
            ) {
                moveTo(54f, 29f)
                moveToRelative(-2.5f, 0f)
                arcToRelative(2.5f, 2.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 5f, 0f)
                arcToRelative(2.5f, 2.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, -5f, 0f)
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFF3E4154),
                        1f to Color(0xFF1B2129)
                    ),
                    start = Offset(54f, 27.651f),
                    end = Offset(54f, 30.563f)
                )
            ) {
                moveTo(55.167f, 29f)
                arcTo(1.166f, 1.166f, 0f, isMoreThanHalf = false, isPositiveArc = true, 54f, 27.833f)
                arcToRelative(1.132f, 1.132f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.167f, -0.566f)
                arcTo(1.523f, 1.523f, 0f, isMoreThanHalf = false, isPositiveArc = false, 54f, 27.25f)
                arcTo(1.75f, 1.75f, 0f, isMoreThanHalf = true, isPositiveArc = false, 55.75f, 29f)
                arcToRelative(1.548f, 1.548f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.017f, -0.167f)
                arcTo(1.132f, 1.132f, 0f, isMoreThanHalf = false, isPositiveArc = true, 55.167f, 29f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFFFFFFF),
                        1f to Color(0xFFEDF1F2)
                    ),
                    start = Offset(46f, 26.932f),
                    end = Offset(46f, 31.224f)
                )
            ) {
                moveTo(46f, 29f)
                moveToRelative(-2.5f, 0f)
                arcToRelative(2.5f, 2.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 5f, 0f)
                arcToRelative(2.5f, 2.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, -5f, 0f)
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFF3E4154),
                        1f to Color(0xFF1B2129)
                    ),
                    start = Offset(46f, 27.651f),
                    end = Offset(46f, 30.563f)
                )
            ) {
                moveTo(47.167f, 29f)
                arcTo(1.166f, 1.166f, 0f, isMoreThanHalf = false, isPositiveArc = true, 46f, 27.833f)
                arcToRelative(1.132f, 1.132f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.167f, -0.566f)
                arcTo(1.523f, 1.523f, 0f, isMoreThanHalf = false, isPositiveArc = false, 46f, 27.25f)
                arcTo(1.75f, 1.75f, 0f, isMoreThanHalf = true, isPositiveArc = false, 47.75f, 29f)
                arcToRelative(1.548f, 1.548f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.017f, -0.167f)
                arcTo(1.132f, 1.132f, 0f, isMoreThanHalf = false, isPositiveArc = true, 47.167f, 29f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFF3E4154),
                        1f to Color(0xFF1B2129)
                    ),
                    start = Offset(22f, 39.5f),
                    end = Offset(22f, 60.322f)
                )
            ) {
                moveTo(26f, 40f)
                horizontalLineTo(18f)
                arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6f, 52f)
                verticalLineToRelative(3f)
                arcToRelative(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6f, 6f)
                horizontalLineTo(32f)
                arcToRelative(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6f, -6f)
                verticalLineTo(52f)
                arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 26f, 40f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFFF9E9E),
                        1f to Color(0xFFDB585B)
                    ),
                    start = Offset(22f, 47.536f),
                    end = Offset(22f, 60.241f)
                )
            ) {
                moveTo(23f, 46.828f)
                lineToRelative(1.831f, 10.985f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.559f, 1.743f)
                lineToRelative(-0.858f, 0.858f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2.828f, 0f)
                lineToRelative(-0.858f, -0.858f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.559f, -1.743f)
                lineTo(21f, 46.828f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFFF9E9E),
                        1f to Color(0xFFDB585B)
                    ),
                    start = Offset(22f, 45.49f),
                    end = Offset(22f, 47.777f)
                )
            ) {
                moveTo(26f, 44f)
                lineToRelative(-2.552f, 3.081f)
                arcToRelative(1.881f, 1.881f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2.9f, 0f)
                lineTo(18f, 44f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFF3E4154),
                        1f to Color(0xFF1B2129)
                    ),
                    start = Offset(22f, 39.429f),
                    end = Offset(22f, 45.583f)
                )
            ) {
                moveTo(27f, 46f)
                horizontalLineTo(17f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2f, -2f)
                horizontalLineToRelative(0f)
                arcToRelative(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6f, -6f)
                horizontalLineToRelative(2f)
                arcToRelative(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6f, 6f)
                horizontalLineToRelative(0f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 27f, 46f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFF1D2BD),
                        1f to Color(0xFFFEB592)
                    ),
                    start = Offset(22f, 39.21f),
                    end = Offset(22f, 45.478f)
                )
            ) {
                moveTo(17.5f, 37f)
                horizontalLineToRelative(9f)
                verticalLineToRelative(4.5f)
                arcTo(4.5f, 4.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22f, 46f)
                horizontalLineToRelative(0f)
                arcToRelative(4.5f, 4.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, -4.5f, -4.5f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFF1D2BD),
                        1f to Color(0xFFFEB592)
                    ),
                    start = Offset(22f, 22.983f),
                    end = Offset(22f, 40.745f)
                )
            ) {
                moveTo(22f, 40.8f)
                horizontalLineToRelative(0f)
                arcTo(10.079f, 10.079f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 30.644f)
                verticalLineTo(24.956f)
                arcTo(10.079f, 10.079f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22f, 14.8f)
                horizontalLineToRelative(0f)
                arcTo(10.079f, 10.079f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 24.956f)
                verticalLineToRelative(5.688f)
                arcTo(10.079f, 10.079f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22f, 40.8f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFF3E4154),
                        1f to Color(0xFF1B2129)
                    ),
                    start = Offset(21.575f, 12.021f),
                    end = Offset(21.575f, 26.986f)
                )
            ) {
                moveTo(11.814f, 29.035f)
                lineToRelative(1.711f, -3.42f)
                arcToRelative(6.075f, 6.075f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.13f, -3.42f)
                reflectiveCurveToRelative(3.42f, 3.42f, 11.97f, 3.42f)
                lineToRelative(1.71f, 3.42f)
                reflectiveCurveToRelative(5.192f, -6.413f, 0.581f, -12.673f)
                curveToRelative(-4.536f, -6.158f, -14.2f, -6.94f, -17.681f, -2.718f)
                curveToRelative(0f, 0f, -6.138f, -0.58f, -6.841f, 6.841f)
                curveTo(7.75f, 27.294f, 11.814f, 29.035f, 11.814f, 29.035f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFF1D2BD),
                        1f to Color(0xFFFEB592)
                    ),
                    start = Offset(33f, 23.304f),
                    end = Offset(33f, 39.7f)
                )
            ) {
                moveTo(33f, 27f)
                lineTo(33f, 27f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 36f, 30f)
                lineTo(36f, 30f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 33f, 33f)
                lineTo(33f, 33f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 30f, 30f)
                lineTo(30f, 30f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 33f, 27f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFF1D2BD),
                        1f to Color(0xFFFEB592)
                    ),
                    start = Offset(11f, 23.304f),
                    end = Offset(11f, 39.7f)
                )
            ) {
                moveTo(11f, 27f)
                lineTo(11f, 27f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14f, 30f)
                lineTo(14f, 30f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11f, 33f)
                lineTo(11f, 33f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 8f, 30f)
                lineTo(8f, 30f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11f, 27f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFDB686B),
                        1f to Color(0xFFC23542)
                    ),
                    start = Offset(22.075f, 33.364f),
                    end = Offset(22.075f, 36.547f)
                )
            ) {
                moveTo(19.237f, 33.4f)
                arcToRelative(0.805f, 0.805f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.684f, 1.223f)
                arcToRelative(4.126f, 4.126f, 0f, isMoreThanHalf = false, isPositiveArc = false, 7.044f, 0f)
                arcToRelative(0.805f, 0.805f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.684f, -1.223f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFFFFFFF),
                        1f to Color(0xFFEDF1F2)
                    ),
                    start = Offset(26f, 26.932f),
                    end = Offset(26f, 31.224f)
                )
            ) {
                moveTo(26f, 29f)
                moveToRelative(-2.5f, 0f)
                arcToRelative(2.5f, 2.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 5f, 0f)
                arcToRelative(2.5f, 2.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, -5f, 0f)
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFF3E4154),
                        1f to Color(0xFF1B2129)
                    ),
                    start = Offset(26f, 27.651f),
                    end = Offset(26f, 30.563f)
                )
            ) {
                moveTo(27.167f, 29f)
                arcTo(1.166f, 1.166f, 0f, isMoreThanHalf = false, isPositiveArc = true, 26f, 27.833f)
                arcToRelative(1.132f, 1.132f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.167f, -0.566f)
                arcTo(1.523f, 1.523f, 0f, isMoreThanHalf = false, isPositiveArc = false, 26f, 27.25f)
                arcTo(1.75f, 1.75f, 0f, isMoreThanHalf = true, isPositiveArc = false, 27.75f, 29f)
                arcToRelative(1.548f, 1.548f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.017f, -0.167f)
                arcTo(1.132f, 1.132f, 0f, isMoreThanHalf = false, isPositiveArc = true, 27.167f, 29f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFFFFFFF),
                        1f to Color(0xFFEDF1F2)
                    ),
                    start = Offset(18f, 26.932f),
                    end = Offset(18f, 31.224f)
                )
            ) {
                moveTo(18f, 29f)
                moveToRelative(-2.5f, 0f)
                arcToRelative(2.5f, 2.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 5f, 0f)
                arcToRelative(2.5f, 2.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, -5f, 0f)
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFF3E4154),
                        1f to Color(0xFF1B2129)
                    ),
                    start = Offset(18f, 27.651f),
                    end = Offset(18f, 30.563f)
                )
            ) {
                moveTo(19.167f, 29f)
                arcTo(1.166f, 1.166f, 0f, isMoreThanHalf = false, isPositiveArc = true, 18f, 27.833f)
                arcToRelative(1.132f, 1.132f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.167f, -0.566f)
                arcTo(1.523f, 1.523f, 0f, isMoreThanHalf = false, isPositiveArc = false, 18f, 27.25f)
                arcTo(1.75f, 1.75f, 0f, isMoreThanHalf = true, isPositiveArc = false, 19.75f, 29f)
                arcToRelative(1.548f, 1.548f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.017f, -0.167f)
                arcTo(1.132f, 1.132f, 0f, isMoreThanHalf = false, isPositiveArc = true, 19.167f, 29f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFDB686B),
                        1f to Color(0xFFC23542)
                    ),
                    start = Offset(36f, 42.286f),
                    end = Offset(36f, 62.644f)
                )
            ) {
                moveTo(36f, 43.354f)
                horizontalLineToRelative(0f)
                arcToRelative(7.614f, 7.614f, 0f, isMoreThanHalf = false, isPositiveArc = false, -10.769f, 0f)
                horizontalLineToRelative(0f)
                arcToRelative(7.616f, 7.616f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 10.769f)
                lineToRelative(5.385f, 5.385f)
                lineToRelative(2.154f, 2.154f)
                arcToRelative(4.57f, 4.57f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.462f, 0f)
                lineToRelative(2.154f, -2.154f)
                lineToRelative(5.385f, -5.385f)
                arcToRelative(7.616f, 7.616f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -10.769f)
                horizontalLineToRelative(0f)
                arcTo(7.614f, 7.614f, 0f, isMoreThanHalf = false, isPositiveArc = false, 36f, 43.354f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFF1D2BD),
                        1f to Color(0xFFFEB592)
                    ),
                    start = Offset(26.66f, 43.598f),
                    end = Offset(26.66f, 53.169f)
                ),
                fillAlpha = 0.5f,
                strokeAlpha = 0.5f
            ) {
                moveTo(27.318f, 52.682f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.707f, -0.293f)
                arcToRelative(5.505f, 5.505f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -7.778f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.414f, 1.414f)
                arcToRelative(3.5f, 3.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 4.95f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.707f, 1.707f)
                close()
            }
        }.build()

        return _Mariage!!
    }

@Suppress("ObjectPropertyName")
private var _Mariage: ImageVector? = null
