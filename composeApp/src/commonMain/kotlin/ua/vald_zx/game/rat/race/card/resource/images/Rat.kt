package ua.vald_zx.game.rat.race.card.resource.images

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.resource.Images

val Images.Rat: ImageVector
    get() {
        if (_Rat != null) {
            return _Rat!!
        }
        _Rat = ImageVector.Builder(
            name = "Rat",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 64f,
            viewportHeight = 64f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(56f, 13.641f)
                curveTo(56f, 7.222f, 50.644f, 2f, 44.062f, 2f)
                curveToRelative(-5.25f, 0f, -9.835f, 3.388f, -11.376f, 8.14f)
                arcToRelative(9.768f, 9.768f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.371f, 0f)
                curveTo(29.772f, 5.388f, 25.187f, 2f, 19.938f, 2f)
                curveTo(13.355f, 2f, 8f, 7.222f, 8f, 13.641f)
                curveToRelative(0f, 5.455f, 3.885f, 10.057f, 9.1f, 11.305f)
                curveToRelative(-1.628f, 3.754f, -4.579f, 11.734f, -4.579f, 20.273f)
                curveToRelative(0f, 0.158f, 0.007f, 0.305f, 0.009f, 0.457f)
                curveToRelative(-0.379f, 0.316f, -0.75f, 0.641f, -1.095f, 0.998f)
                curveToRelative(-0.703f, 0.73f, -1.344f, 1.547f, -1.853f, 2.486f)
                arcToRelative(8.282f, 8.282f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.633f, 1.508f)
                arcToRelative(6.496f, 6.496f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.277f, 1.68f)
                curveToRelative(-0.016f, 0.582f, 0.057f, 1.17f, 0.216f, 1.729f)
                arcToRelative(5.99f, 5.99f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.694f, 1.553f)
                curveToRelative(0.589f, 0.943f, 1.363f, 1.67f, 2.164f, 2.264f)
                curveToRelative(0.401f, 0.297f, 0.819f, 0.566f, 1.232f, 0.803f)
                arcToRelative(15.954f, 15.954f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.994f, 1.637f)
                curveToRelative(1.851f, 0.48f, 3.707f, 0.635f, 5.518f, 0.717f)
                curveToRelative(1.813f, 0.082f, 3.602f, 0.094f, 5.389f, 0.146f)
                curveToRelative(1.788f, 0.047f, 3.57f, 0.123f, 5.352f, 0.256f)
                arcToRelative(92.14f, 92.14f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5.34f, 0.549f)
                arcToRelative(94.415f, 94.415f, 0f, isMoreThanHalf = false, isPositiveArc = false, -5.275f, -1.053f)
                arcToRelative(116.259f, 116.259f, 0f, isMoreThanHalf = false, isPositiveArc = false, -5.335f, -0.766f)
                curveToRelative(-1.784f, -0.221f, -3.57f, -0.402f, -5.322f, -0.646f)
                curveToRelative(-1.748f, -0.248f, -3.466f, -0.566f, -5.033f, -1.139f)
                arcToRelative(13.71f, 13.71f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2.236f, -1.047f)
                curveToRelative(-0.35f, -0.207f, -0.698f, -0.432f, -1.025f, -0.666f)
                arcToRelative(9.985f, 9.985f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.928f, -0.742f)
                curveToRelative(-0.573f, -0.516f, -1.042f, -1.084f, -1.323f, -1.668f)
                curveToRelative(-0.282f, -0.582f, -0.373f, -1.156f, -0.292f, -1.732f)
                reflectiveCurveToRelative(0.336f, -1.168f, 0.724f, -1.736f)
                curveToRelative(0.132f, -0.193f, 0.294f, -0.379f, 0.455f, -0.564f)
                curveToRelative(0.887f, 4.176f, 3.093f, 6.426f, 6.834f, 7.053f)
                curveToRelative(0.739f, 1.016f, 1.965f, 1.656f, 3.319f, 1.656f)
                horizontalLineToRelative(1.475f)
                curveToRelative(1.42f, 0f, 2.7f, -0.723f, 3.423f, -1.818f)
                curveToRelative(1.248f, -0.098f, 2.593f, -0.184f, 3.97f, -0.184f)
                curveToRelative(1.265f, 0f, 2.513f, 0.074f, 3.772f, 0.17f)
                curveToRelative(0.72f, 1.105f, 2.003f, 1.832f, 3.433f, 1.832f)
                horizontalLineToRelative(1.476f)
                arcToRelative(4.107f, 4.107f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.294f, -1.625f)
                curveToRelative(5.179f, -0.775f, 7.505f, -4.561f, 7.505f, -12.105f)
                curveToRelative(0f, -8.547f, -2.951f, -16.523f, -4.579f, -20.274f)
                curveTo(52.115f, 23.697f, 56f, 19.096f, 56f, 13.641f)
                moveToRelative(-44.161f, 1.165f)
                curveToRelative(0f, -3.863f, 3.233f, -7.006f, 7.207f, -7.006f)
                curveToRelative(3.09f, 0f, 5.794f, 1.914f, 6.793f, 4.678f)
                curveToRelative(-2.635f, 2.011f, -5.06f, 5.139f, -7.228f, 9.322f)
                curveToRelative(-3.772f, -0.219f, -6.772f, -3.273f, -6.772f, -6.994f)
                moveTo(49.56f, 45.219f)
                curveToRelative(0f, 8.066f, -2.672f, 10.072f, -6.818f, 10.346f)
                curveToRelative(-0.233f, 0.861f, -1.063f, 1.502f, -2.061f, 1.502f)
                horizontalLineToRelative(-1.476f)
                curveToRelative(-1.082f, 0f, -1.967f, -0.756f, -2.104f, -1.732f)
                curveToRelative(-1.615f, -0.133f, -3.327f, -0.268f, -5.102f, -0.268f)
                curveToRelative(-1.844f, 0f, -3.621f, 0.145f, -5.291f, 0.283f)
                curveToRelative(-0.144f, 0.969f, -1.025f, 1.717f, -2.103f, 1.717f)
                horizontalLineTo(23.13f)
                curveToRelative(-1.001f, 0f, -1.837f, -0.646f, -2.064f, -1.516f)
                curveToRelative(-4.037f, -0.322f, -6.626f, -2.391f, -6.626f, -10.332f)
                curveToRelative(0f, -9.559f, 2.548f, -16.237f, 5.092f, -21.003f)
                curveToRelative(1.708f, -3.199f, 5.745f, -9.833f, 9.042f, -11.342f)
                curveToRelative(2.075f, -0.949f, 4.616f, -1.033f, 6.849f, -0.001f)
                curveToRelative(3.293f, 1.522f, 7.337f, 8.146f, 9.044f, 11.346f)
                curveToRelative(2.544f, 4.766f, 5.093f, 11.445f, 5.093f, 21f)
                moveTo(45.388f, 21.8f)
                curveToRelative(-2.19f, -4.24f, -4.568f, -7.31f, -7.228f, -9.327f)
                curveToRelative(1f, -2.761f, 3.704f, -4.673f, 6.792f, -4.673f)
                curveToRelative(3.975f, 0f, 7.208f, 3.143f, 7.208f, 7.006f)
                curveToRelative(0f, 3.721f, -2.999f, 6.775f, -6.772f, 6.994f)
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(32.001f, 33.152f)
                curveToRelative(-0.51f, 0f, -2.214f, 0.045f, -2.77f, 0.619f)
                curveToRelative(-0.395f, 0.408f, -0.089f, 1.422f, 0.962f, 2.295f)
                curveToRelative(0.665f, 0.553f, 1.298f, 0.727f, 1.808f, 0.727f)
                reflectiveCurveToRelative(1.144f, -0.174f, 1.807f, -0.727f)
                curveToRelative(1.052f, -0.873f, 1.357f, -1.887f, 0.962f, -2.295f)
                curveToRelative(-0.555f, -0.574f, -2.26f, -0.619f, -2.769f, -0.619f)
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(22.548f, 27.143f)
                arcToRelative(2.881f, 2.823f, 0f, isMoreThanHalf = true, isPositiveArc = false, 5.762f, 0f)
                arcToRelative(2.881f, 2.823f, 0f, isMoreThanHalf = true, isPositiveArc = false, -5.762f, 0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(35.69f, 27.143f)
                arcToRelative(2.88f, 2.823f, 0f, isMoreThanHalf = true, isPositiveArc = false, 5.76f, 0f)
                arcToRelative(2.88f, 2.823f, 0f, isMoreThanHalf = true, isPositiveArc = false, -5.76f, 0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(32.975f, 37.67f)
                arcToRelative(2.51f, 2.51f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.792f, 0.189f)
                curveToRelative(-0.242f, 0.111f, -0.476f, 0.24f, -0.679f, 0.42f)
                curveToRelative(-0.198f, 0.172f, -0.405f, 0.395f, -0.461f, 0.688f)
                curveToRelative(0.259f, -0.102f, 0.51f, -0.127f, 0.741f, -0.213f)
                curveToRelative(0.236f, -0.068f, 0.459f, -0.16f, 0.683f, -0.246f)
                curveToRelative(0.219f, -0.1f, 0.436f, -0.193f, 0.646f, -0.303f)
                curveToRelative(0.219f, -0.09f, 0.4f, -0.248f, 0.678f, -0.279f)
                curveToRelative(-0.197f, -0.242f, -0.552f, -0.272f, -0.816f, -0.256f)
            }
        }.build()

        return _Rat!!
    }

@Suppress("ObjectPropertyName")
private var _Rat: ImageVector? = null