package ua.vald_zx.game.rat.race.card.resource.images

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.resource.Images

val Images.Car: ImageVector
    get() {
        if (_Car != null) {
            return _Car!!
        }
        _Car = ImageVector.Builder(
            name = "Car",
            defaultWidth = 48.dp,
            defaultHeight = 48.dp,
            viewportWidth = 64f,
            viewportHeight = 64f
        ).apply {
            path(fill = SolidColor(Color(0xFFFF5023))) {
                moveTo(61f, 32f)
                verticalLineTo(37f)
                lineTo(54.83f, 40.41f)
                lineTo(44.21f, 42.61f)
                lineTo(22.51f, 43.86f)
                lineTo(15.69f, 44f)
                horizontalLineTo(7.19f)
                curveTo(6.829f, 44f, 6.475f, 43.902f, 6.165f, 43.718f)
                curveTo(5.855f, 43.533f, 5.601f, 43.268f, 5.43f, 42.95f)
                lineTo(3.84f, 40f)
                lineTo(3.23f, 38.88f)
                curveTo(2.75f, 38f, 2.618f, 36.972f, 2.86f, 36f)
                curveTo(2.946f, 35.648f, 3.084f, 35.311f, 3.27f, 35f)
                curveTo(3.733f, 34.164f, 4.488f, 33.527f, 5.39f, 33.21f)
                lineTo(20.16f, 27.89f)
                lineTo(31.53f, 21.27f)
                curveTo(31.837f, 21.093f, 32.185f, 20.999f, 32.54f, 21f)
                horizontalLineTo(41.78f)
                curveTo(41.925f, 21.001f, 42.069f, 21.018f, 42.21f, 21.05f)
                lineTo(58.69f, 24.71f)
                curveTo(58.842f, 24.744f, 58.984f, 24.812f, 59.104f, 24.911f)
                curveTo(59.224f, 25.009f, 59.32f, 25.134f, 59.383f, 25.276f)
                curveTo(59.446f, 25.418f, 59.475f, 25.573f, 59.468f, 25.728f)
                curveTo(59.46f, 25.883f, 59.417f, 26.035f, 59.34f, 26.17f)
                lineTo(57.58f, 29.32f)
                curveTo(57.448f, 29.462f, 57.36f, 29.64f, 57.327f, 29.832f)
                curveTo(57.295f, 30.023f, 57.319f, 30.22f, 57.396f, 30.399f)
                curveTo(57.473f, 30.577f, 57.601f, 30.729f, 57.764f, 30.835f)
                curveTo(57.926f, 30.942f, 58.116f, 30.999f, 58.31f, 31f)
                horizontalLineTo(60f)
                curveTo(60.265f, 31f, 60.52f, 31.105f, 60.707f, 31.293f)
                curveTo(60.895f, 31.48f, 61f, 31.735f, 61f, 32f)
                close()
            }
            path(fill = SolidColor(Color(0xFFE64820))) {
                moveTo(61f, 35f)
                verticalLineTo(37f)
                lineTo(54.83f, 40.41f)
                lineTo(44.21f, 42.61f)
                lineTo(22.51f, 43.86f)
                lineTo(15.69f, 44f)
                horizontalLineTo(7.19f)
                curveTo(6.829f, 44f, 6.475f, 43.902f, 6.165f, 43.718f)
                curveTo(5.855f, 43.533f, 5.601f, 43.268f, 5.43f, 42.95f)
                lineTo(3.84f, 40f)
                lineTo(3.23f, 38.88f)
                curveTo(2.75f, 38f, 2.618f, 36.972f, 2.86f, 36f)
                curveTo(2.946f, 35.648f, 3.084f, 35.311f, 3.27f, 35f)
                horizontalLineTo(61f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFF7256))) {
                moveTo(2.86f, 36f)
                horizontalLineTo(8f)
                lineTo(6f, 40f)
                horizontalLineTo(3.84f)
                lineTo(3.23f, 38.88f)
                curveTo(2.75f, 38f, 2.618f, 36.972f, 2.86f, 36f)
                verticalLineTo(36f)
                close()
            }
            path(fill = SolidColor(Color(0xFFE6E7E8))) {
                moveTo(45.98f, 27f)
                lineTo(45.89f, 27.03f)
                lineTo(38f, 29f)
                lineTo(39.2f, 27f)
                lineTo(41f, 24f)
                lineTo(45.86f, 25.08f)
                curveTo(46.072f, 25.126f, 46.264f, 25.241f, 46.406f, 25.406f)
                curveTo(46.548f, 25.571f, 46.632f, 25.778f, 46.645f, 25.995f)
                curveTo(46.659f, 26.212f, 46.601f, 26.427f, 46.481f, 26.609f)
                curveTo(46.361f, 26.79f, 46.185f, 26.928f, 45.98f, 27f)
                close()
            }
            path(fill = SolidColor(Color(0xFFE6E7E8))) {
                moveTo(41f, 24f)
                lineTo(39.2f, 27f)
                lineTo(38f, 29f)
                lineTo(27.14f, 29.72f)
                curveTo(26.918f, 29.732f, 26.698f, 29.67f, 26.515f, 29.544f)
                curveTo(26.332f, 29.417f, 26.197f, 29.233f, 26.13f, 29.021f)
                curveTo(26.063f, 28.808f, 26.07f, 28.58f, 26.147f, 28.372f)
                curveTo(26.225f, 28.163f, 26.371f, 27.987f, 26.56f, 27.87f)
                lineTo(28.02f, 27f)
                lineTo(32.75f, 24.17f)
                curveTo(32.912f, 24.06f, 33.104f, 24.001f, 33.3f, 24f)
                horizontalLineTo(41f)
                close()
            }
            path(fill = SolidColor(Color(0xFF57565C))) {
                moveTo(19f, 33f)
                verticalLineTo(37f)
                curveTo(18.737f, 36.999f, 18.477f, 37.05f, 18.235f, 37.152f)
                curveTo(17.993f, 37.253f, 17.773f, 37.402f, 17.59f, 37.59f)
                lineTo(14.76f, 34.76f)
                curveTo(15.315f, 34.2f, 15.975f, 33.756f, 16.703f, 33.454f)
                curveTo(17.431f, 33.152f, 18.212f, 32.998f, 19f, 33f)
                verticalLineTo(33f)
                close()
            }
            path(fill = SolidColor(Color(0xFF57565C))) {
                moveTo(17.59f, 40.41f)
                lineTo(14.76f, 43.24f)
                curveTo(14.2f, 42.685f, 13.756f, 42.025f, 13.454f, 41.297f)
                curveTo(13.152f, 40.569f, 12.998f, 39.788f, 13f, 39f)
                horizontalLineTo(17f)
                curveTo(16.999f, 39.263f, 17.05f, 39.523f, 17.152f, 39.765f)
                curveTo(17.253f, 40.007f, 17.402f, 40.227f, 17.59f, 40.41f)
                verticalLineTo(40.41f)
                close()
            }
            path(fill = SolidColor(Color(0xFF57565C))) {
                moveTo(14.76f, 43.24f)
                lineTo(17.59f, 40.41f)
                curveTo(17.773f, 40.598f, 17.993f, 40.747f, 18.235f, 40.848f)
                curveTo(18.477f, 40.95f, 18.737f, 41.001f, 19f, 41f)
                verticalLineTo(45f)
                curveTo(17.822f, 45.005f, 16.669f, 44.656f, 15.69f, 44f)
                curveTo(15.354f, 43.781f, 15.042f, 43.526f, 14.76f, 43.24f)
                verticalLineTo(43.24f)
                close()
            }
            path(fill = SolidColor(Color(0xFF57565C))) {
                moveTo(20.41f, 40.41f)
                lineTo(23.24f, 43.24f)
                curveTo(23.016f, 43.468f, 22.771f, 43.676f, 22.51f, 43.86f)
                curveTo(21.492f, 44.605f, 20.262f, 45.004f, 19f, 45f)
                verticalLineTo(41f)
                curveTo(19.263f, 41.001f, 19.523f, 40.95f, 19.765f, 40.848f)
                curveTo(20.007f, 40.747f, 20.227f, 40.598f, 20.41f, 40.41f)
                verticalLineTo(40.41f)
                close()
            }
            path(fill = SolidColor(Color(0xFF57565C))) {
                moveTo(25f, 39f)
                horizontalLineTo(21f)
                curveTo(21.001f, 38.737f, 20.95f, 38.477f, 20.848f, 38.235f)
                curveTo(20.747f, 37.993f, 20.598f, 37.773f, 20.41f, 37.59f)
                lineTo(23.24f, 34.76f)
                curveTo(23.8f, 35.315f, 24.244f, 35.975f, 24.546f, 36.703f)
                curveTo(24.848f, 37.431f, 25.002f, 38.212f, 25f, 39f)
                close()
            }
            path(fill = SolidColor(Color(0xFF57565C))) {
                moveTo(21f, 39f)
                horizontalLineTo(25f)
                curveTo(25.002f, 39.788f, 24.848f, 40.569f, 24.546f, 41.297f)
                curveTo(24.244f, 42.025f, 23.8f, 42.685f, 23.24f, 43.24f)
                lineTo(20.41f, 40.41f)
                curveTo(20.598f, 40.227f, 20.747f, 40.007f, 20.848f, 39.765f)
                curveTo(20.95f, 39.523f, 21.001f, 39.263f, 21f, 39f)
                verticalLineTo(39f)
                close()
            }
            path(fill = SolidColor(Color(0xFF57565C))) {
                moveTo(14.76f, 34.76f)
                lineTo(17.59f, 37.59f)
                curveTo(17.402f, 37.773f, 17.253f, 37.993f, 17.152f, 38.235f)
                curveTo(17.05f, 38.477f, 16.999f, 38.737f, 17f, 39f)
                horizontalLineTo(13f)
                curveTo(12.998f, 38.212f, 13.152f, 37.431f, 13.454f, 36.703f)
                curveTo(13.756f, 35.975f, 14.2f, 35.315f, 14.76f, 34.76f)
                verticalLineTo(34.76f)
                close()
            }
            path(fill = SolidColor(Color(0xFF57565C))) {
                moveTo(23.24f, 34.76f)
                lineTo(20.41f, 37.59f)
                curveTo(20.227f, 37.402f, 20.007f, 37.253f, 19.765f, 37.152f)
                curveTo(19.523f, 37.05f, 19.263f, 36.999f, 19f, 37f)
                verticalLineTo(33f)
                curveTo(19.788f, 32.998f, 20.569f, 33.152f, 21.297f, 33.454f)
                curveTo(22.025f, 33.756f, 22.685f, 34.2f, 23.24f, 34.76f)
                verticalLineTo(34.76f)
                close()
            }
            path(fill = SolidColor(Color(0xFF57565C))) {
                moveTo(49f, 41f)
                verticalLineTo(45f)
                curveTo(48.212f, 45.002f, 47.431f, 44.848f, 46.703f, 44.546f)
                curveTo(45.975f, 44.244f, 45.315f, 43.8f, 44.76f, 43.24f)
                lineTo(47.59f, 40.41f)
                curveTo(47.773f, 40.598f, 47.993f, 40.747f, 48.235f, 40.848f)
                curveTo(48.477f, 40.95f, 48.737f, 41.001f, 49f, 41f)
                close()
            }
            path(fill = SolidColor(Color(0xFF57565C))) {
                moveTo(55f, 39f)
                horizontalLineTo(51f)
                curveTo(51.001f, 38.737f, 50.95f, 38.477f, 50.848f, 38.235f)
                curveTo(50.747f, 37.993f, 50.598f, 37.773f, 50.41f, 37.59f)
                lineTo(53.19f, 34.81f)
                lineTo(53.24f, 34.76f)
                curveTo(53.8f, 35.315f, 54.244f, 35.975f, 54.546f, 36.703f)
                curveTo(54.848f, 37.431f, 55.002f, 38.212f, 55f, 39f)
                verticalLineTo(39f)
                close()
            }
            path(fill = SolidColor(Color(0xFF57565C))) {
                moveTo(53.24f, 34.76f)
                lineTo(53.19f, 34.81f)
                lineTo(50.41f, 37.59f)
                curveTo(50.227f, 37.402f, 50.007f, 37.253f, 49.765f, 37.152f)
                curveTo(49.523f, 37.05f, 49.263f, 36.999f, 49f, 37f)
                verticalLineTo(33f)
                curveTo(49.788f, 32.998f, 50.569f, 33.152f, 51.297f, 33.454f)
                curveTo(52.025f, 33.756f, 52.685f, 34.2f, 53.24f, 34.76f)
                verticalLineTo(34.76f)
                close()
            }
            path(fill = SolidColor(Color(0xFF57565C))) {
                moveTo(49f, 33f)
                verticalLineTo(37f)
                curveTo(48.737f, 36.999f, 48.477f, 37.05f, 48.235f, 37.152f)
                curveTo(47.993f, 37.253f, 47.773f, 37.402f, 47.59f, 37.59f)
                lineTo(44.76f, 34.76f)
                curveTo(45.315f, 34.2f, 45.975f, 33.756f, 46.703f, 33.454f)
                curveTo(47.431f, 33.152f, 48.212f, 32.998f, 49f, 33f)
                verticalLineTo(33f)
                close()
            }
            path(fill = SolidColor(Color(0xFF57565C))) {
                moveTo(47.59f, 40.41f)
                lineTo(44.76f, 43.24f)
                curveTo(44.563f, 43.042f, 44.38f, 42.832f, 44.21f, 42.61f)
                curveTo(43.422f, 41.572f, 42.996f, 40.304f, 43f, 39f)
                horizontalLineTo(47f)
                curveTo(46.999f, 39.263f, 47.05f, 39.523f, 47.152f, 39.765f)
                curveTo(47.253f, 40.007f, 47.402f, 40.227f, 47.59f, 40.41f)
                verticalLineTo(40.41f)
                close()
            }
            path(fill = SolidColor(Color(0xFF57565C))) {
                moveTo(53.19f, 43.19f)
                lineTo(53.24f, 43.24f)
                curveTo(52.685f, 43.8f, 52.025f, 44.244f, 51.297f, 44.546f)
                curveTo(50.569f, 44.848f, 49.788f, 45.002f, 49f, 45f)
                verticalLineTo(41f)
                curveTo(49.263f, 41.001f, 49.523f, 40.95f, 49.765f, 40.848f)
                curveTo(50.007f, 40.747f, 50.227f, 40.598f, 50.41f, 40.41f)
                lineTo(53.19f, 43.19f)
                close()
            }
            path(fill = SolidColor(Color(0xFF57565C))) {
                moveTo(44.76f, 34.76f)
                lineTo(47.59f, 37.59f)
                curveTo(47.402f, 37.773f, 47.253f, 37.993f, 47.152f, 38.235f)
                curveTo(47.05f, 38.477f, 46.999f, 38.737f, 47f, 39f)
                horizontalLineTo(43f)
                curveTo(42.998f, 38.212f, 43.152f, 37.431f, 43.454f, 36.703f)
                curveTo(43.756f, 35.975f, 44.2f, 35.315f, 44.76f, 34.76f)
                verticalLineTo(34.76f)
                close()
            }
            path(fill = SolidColor(Color(0xFF57565C))) {
                moveTo(51f, 39f)
                horizontalLineTo(55f)
                curveTo(54.998f, 40.59f, 54.365f, 42.115f, 53.24f, 43.24f)
                lineTo(53.19f, 43.19f)
                lineTo(50.41f, 40.41f)
                curveTo(50.598f, 40.227f, 50.747f, 40.007f, 50.848f, 39.765f)
                curveTo(50.95f, 39.523f, 51.001f, 39.263f, 51f, 39f)
                verticalLineTo(39f)
                close()
            }
            path(fill = SolidColor(Color(0xFF57565C))) {
                moveTo(50.41f, 37.59f)
                curveTo(50.731f, 37.916f, 50.93f, 38.343f, 50.972f, 38.798f)
                curveTo(51.014f, 39.254f, 50.896f, 39.71f, 50.64f, 40.089f)
                curveTo(50.384f, 40.468f, 50.005f, 40.746f, 49.566f, 40.877f)
                curveTo(49.128f, 41.008f, 48.658f, 40.983f, 48.236f, 40.806f)
                curveTo(47.814f, 40.63f, 47.466f, 40.313f, 47.251f, 39.909f)
                curveTo(47.037f, 39.505f, 46.969f, 39.039f, 47.059f, 38.59f)
                curveTo(47.149f, 38.142f, 47.391f, 37.738f, 47.745f, 37.448f)
                curveTo(48.099f, 37.158f, 48.542f, 37f, 49f, 37f)
                curveTo(49.263f, 36.999f, 49.523f, 37.05f, 49.765f, 37.152f)
                curveTo(50.007f, 37.253f, 50.227f, 37.402f, 50.41f, 37.59f)
                verticalLineTo(37.59f)
                close()
            }
            path(fill = SolidColor(Color(0xFF57565C))) {
                moveTo(20.41f, 37.59f)
                curveTo(20.731f, 37.916f, 20.93f, 38.343f, 20.972f, 38.798f)
                curveTo(21.014f, 39.254f, 20.896f, 39.71f, 20.64f, 40.089f)
                curveTo(20.384f, 40.468f, 20.004f, 40.746f, 19.566f, 40.877f)
                curveTo(19.128f, 41.008f, 18.658f, 40.983f, 18.236f, 40.806f)
                curveTo(17.814f, 40.63f, 17.466f, 40.313f, 17.251f, 39.909f)
                curveTo(17.037f, 39.505f, 16.969f, 39.039f, 17.059f, 38.59f)
                curveTo(17.149f, 38.142f, 17.391f, 37.738f, 17.745f, 37.448f)
                curveTo(18.099f, 37.158f, 18.542f, 37f, 19f, 37f)
                curveTo(19.263f, 36.999f, 19.523f, 37.05f, 19.765f, 37.152f)
                curveTo(20.007f, 37.253f, 20.227f, 37.402f, 20.41f, 37.59f)
                verticalLineTo(37.59f)
                close()
            }
            path(fill = SolidColor(Color(0xFFD8D7DA))) {
                moveTo(45.98f, 27f)
                lineTo(45.89f, 27.03f)
                lineTo(38f, 29f)
                lineTo(27.14f, 29.72f)
                curveTo(26.918f, 29.732f, 26.698f, 29.67f, 26.515f, 29.544f)
                curveTo(26.332f, 29.417f, 26.197f, 29.233f, 26.13f, 29.021f)
                curveTo(26.063f, 28.808f, 26.07f, 28.58f, 26.147f, 28.372f)
                curveTo(26.225f, 28.163f, 26.371f, 27.987f, 26.56f, 27.87f)
                lineTo(28.02f, 27f)
                horizontalLineTo(45.98f)
                close()
            }
            path(fill = SolidColor(Color(0xFFE64820))) {
                moveTo(38.906f, 31.83f)
                lineTo(34.921f, 32.177f)
                lineTo(35.095f, 34.17f)
                lineTo(39.08f, 33.822f)
                lineTo(38.906f, 31.83f)
                close()
            }
            path(fill = SolidColor(Color(0xFF787680))) {
                moveTo(20f, 41f)
                horizontalLineTo(18f)
                verticalLineTo(45f)
                horizontalLineTo(20f)
                verticalLineTo(41f)
                close()
            }
            path(fill = SolidColor(Color(0xFF787680))) {
                moveTo(20f, 33f)
                horizontalLineTo(18f)
                verticalLineTo(37f)
                horizontalLineTo(20f)
                verticalLineTo(33f)
                close()
            }
            path(fill = SolidColor(Color(0xFF787680))) {
                moveTo(21.118f, 39.703f)
                lineTo(19.703f, 41.117f)
                lineTo(22.533f, 43.947f)
                lineTo(23.947f, 42.533f)
                lineTo(21.118f, 39.703f)
                close()
            }
            path(fill = SolidColor(Color(0xFF787680))) {
                moveTo(15.467f, 34.053f)
                lineTo(14.053f, 35.467f)
                lineTo(16.883f, 38.297f)
                lineTo(18.297f, 36.883f)
                lineTo(15.467f, 34.053f)
                close()
            }
            path(fill = SolidColor(Color(0xFF787680))) {
                moveTo(25f, 38f)
                horizontalLineTo(21f)
                verticalLineTo(40f)
                horizontalLineTo(25f)
                verticalLineTo(38f)
                close()
            }
            path(fill = SolidColor(Color(0xFF787680))) {
                moveTo(17f, 38f)
                horizontalLineTo(13f)
                verticalLineTo(40f)
                horizontalLineTo(17f)
                verticalLineTo(38f)
                close()
            }
            path(fill = SolidColor(Color(0xFF787680))) {
                moveTo(22.533f, 34.053f)
                lineTo(19.703f, 36.883f)
                lineTo(21.117f, 38.297f)
                lineTo(23.947f, 35.467f)
                lineTo(22.533f, 34.053f)
                close()
            }
            path(fill = SolidColor(Color(0xFF787680))) {
                moveTo(16.883f, 39.703f)
                lineTo(14.053f, 42.533f)
                lineTo(15.467f, 43.947f)
                lineTo(18.297f, 41.118f)
                lineTo(16.883f, 39.703f)
                close()
            }
            path(fill = SolidColor(Color(0xFF3E3D42))) {
                moveTo(19f, 42f)
                curveTo(18.407f, 42f, 17.827f, 41.824f, 17.333f, 41.494f)
                curveTo(16.84f, 41.165f, 16.455f, 40.696f, 16.228f, 40.148f)
                curveTo(16.001f, 39.6f, 15.942f, 38.997f, 16.058f, 38.415f)
                curveTo(16.173f, 37.833f, 16.459f, 37.298f, 16.879f, 36.879f)
                curveTo(17.298f, 36.459f, 17.833f, 36.173f, 18.415f, 36.058f)
                curveTo(18.997f, 35.942f, 19.6f, 36.001f, 20.148f, 36.228f)
                curveTo(20.696f, 36.455f, 21.165f, 36.84f, 21.494f, 37.333f)
                curveTo(21.824f, 37.827f, 22f, 38.407f, 22f, 39f)
                curveTo(22f, 39.796f, 21.684f, 40.559f, 21.121f, 41.121f)
                curveTo(20.559f, 41.684f, 19.796f, 42f, 19f, 42f)
                close()
                moveTo(19f, 38f)
                curveTo(18.802f, 38f, 18.609f, 38.059f, 18.444f, 38.168f)
                curveTo(18.28f, 38.278f, 18.152f, 38.435f, 18.076f, 38.617f)
                curveTo(18f, 38.8f, 17.981f, 39.001f, 18.019f, 39.195f)
                curveTo(18.058f, 39.389f, 18.153f, 39.567f, 18.293f, 39.707f)
                curveTo(18.433f, 39.847f, 18.611f, 39.942f, 18.805f, 39.981f)
                curveTo(18.999f, 40.019f, 19.2f, 40f, 19.383f, 39.924f)
                curveTo(19.565f, 39.848f, 19.722f, 39.72f, 19.831f, 39.556f)
                curveTo(19.941f, 39.391f, 20f, 39.198f, 20f, 39f)
                curveTo(20f, 38.735f, 19.895f, 38.48f, 19.707f, 38.293f)
                curveTo(19.52f, 38.105f, 19.265f, 38f, 19f, 38f)
                close()
            }
            path(fill = SolidColor(Color(0xFF787680))) {
                moveTo(50f, 41f)
                horizontalLineTo(48f)
                verticalLineTo(45f)
                horizontalLineTo(50f)
                verticalLineTo(41f)
                close()
            }
            path(fill = SolidColor(Color(0xFF787680))) {
                moveTo(50f, 33f)
                horizontalLineTo(48f)
                verticalLineTo(37f)
                horizontalLineTo(50f)
                verticalLineTo(33f)
                close()
            }
            path(fill = SolidColor(Color(0xFF787680))) {
                moveTo(51.118f, 39.703f)
                lineTo(49.703f, 41.117f)
                lineTo(52.483f, 43.898f)
                lineTo(53.898f, 42.484f)
                lineTo(51.118f, 39.703f)
                close()
            }
            path(fill = SolidColor(Color(0xFF787680))) {
                moveTo(45.467f, 34.053f)
                lineTo(44.053f, 35.467f)
                lineTo(46.883f, 38.297f)
                lineTo(48.297f, 36.883f)
                lineTo(45.467f, 34.053f)
                close()
            }
            path(fill = SolidColor(Color(0xFF787680))) {
                moveTo(55f, 38f)
                horizontalLineTo(51f)
                verticalLineTo(40f)
                horizontalLineTo(55f)
                verticalLineTo(38f)
                close()
            }
            path(fill = SolidColor(Color(0xFF787680))) {
                moveTo(47f, 38f)
                horizontalLineTo(43f)
                verticalLineTo(40f)
                horizontalLineTo(47f)
                verticalLineTo(38f)
                close()
            }
            path(fill = SolidColor(Color(0xFF787680))) {
                moveTo(52.484f, 34.103f)
                lineTo(49.703f, 36.883f)
                lineTo(51.117f, 38.297f)
                lineTo(53.898f, 35.517f)
                lineTo(52.484f, 34.103f)
                close()
            }
            path(fill = SolidColor(Color(0xFF787680))) {
                moveTo(46.882f, 39.703f)
                lineTo(44.052f, 42.533f)
                lineTo(45.467f, 43.947f)
                lineTo(48.296f, 41.117f)
                lineTo(46.882f, 39.703f)
                close()
            }
            path(fill = SolidColor(Color(0xFF3E3D42))) {
                moveTo(49f, 42f)
                curveTo(48.407f, 42f, 47.827f, 41.824f, 47.333f, 41.494f)
                curveTo(46.84f, 41.165f, 46.455f, 40.696f, 46.228f, 40.148f)
                curveTo(46.001f, 39.6f, 45.942f, 38.997f, 46.058f, 38.415f)
                curveTo(46.173f, 37.833f, 46.459f, 37.298f, 46.879f, 36.879f)
                curveTo(47.298f, 36.459f, 47.833f, 36.173f, 48.415f, 36.058f)
                curveTo(48.997f, 35.942f, 49.6f, 36.001f, 50.148f, 36.228f)
                curveTo(50.696f, 36.455f, 51.165f, 36.84f, 51.494f, 37.333f)
                curveTo(51.824f, 37.827f, 52f, 38.407f, 52f, 39f)
                curveTo(52f, 39.796f, 51.684f, 40.559f, 51.121f, 41.121f)
                curveTo(50.559f, 41.684f, 49.796f, 42f, 49f, 42f)
                close()
                moveTo(49f, 38f)
                curveTo(48.802f, 38f, 48.609f, 38.059f, 48.444f, 38.168f)
                curveTo(48.28f, 38.278f, 48.152f, 38.435f, 48.076f, 38.617f)
                curveTo(48f, 38.8f, 47.981f, 39.001f, 48.019f, 39.195f)
                curveTo(48.058f, 39.389f, 48.153f, 39.567f, 48.293f, 39.707f)
                curveTo(48.433f, 39.847f, 48.611f, 39.942f, 48.805f, 39.981f)
                curveTo(48.999f, 40.019f, 49.2f, 40f, 49.383f, 39.924f)
                curveTo(49.565f, 39.848f, 49.722f, 39.72f, 49.832f, 39.556f)
                curveTo(49.941f, 39.391f, 50f, 39.198f, 50f, 39f)
                curveTo(50f, 38.735f, 49.895f, 38.48f, 49.707f, 38.293f)
                curveTo(49.52f, 38.105f, 49.265f, 38f, 49f, 38f)
                close()
            }
            path(fill = SolidColor(Color(0xFF3E3D42))) {
                moveTo(19f, 46f)
                curveTo(17.615f, 46f, 16.262f, 45.59f, 15.111f, 44.82f)
                curveTo(13.96f, 44.051f, 13.063f, 42.958f, 12.533f, 41.679f)
                curveTo(12.003f, 40.4f, 11.864f, 38.992f, 12.134f, 37.634f)
                curveTo(12.405f, 36.277f, 13.071f, 35.029f, 14.05f, 34.05f)
                curveTo(15.029f, 33.071f, 16.277f, 32.405f, 17.634f, 32.134f)
                curveTo(18.992f, 31.864f, 20.4f, 32.003f, 21.679f, 32.533f)
                curveTo(22.958f, 33.063f, 24.051f, 33.96f, 24.82f, 35.111f)
                curveTo(25.59f, 36.262f, 26f, 37.616f, 26f, 39f)
                curveTo(25.998f, 40.856f, 25.26f, 42.635f, 23.947f, 43.947f)
                curveTo(22.635f, 45.26f, 20.856f, 45.998f, 19f, 46f)
                verticalLineTo(46f)
                close()
                moveTo(19f, 34f)
                curveTo(18.011f, 34f, 17.044f, 34.293f, 16.222f, 34.843f)
                curveTo(15.4f, 35.392f, 14.759f, 36.173f, 14.381f, 37.087f)
                curveTo(14.002f, 38f, 13.903f, 39.006f, 14.096f, 39.975f)
                curveTo(14.289f, 40.945f, 14.765f, 41.836f, 15.465f, 42.535f)
                curveTo(16.164f, 43.235f, 17.055f, 43.711f, 18.025f, 43.904f)
                curveTo(18.994f, 44.097f, 20f, 43.998f, 20.913f, 43.619f)
                curveTo(21.827f, 43.241f, 22.608f, 42.6f, 23.157f, 41.778f)
                curveTo(23.707f, 40.956f, 24f, 39.989f, 24f, 39f)
                curveTo(23.998f, 37.674f, 23.471f, 36.404f, 22.534f, 35.466f)
                curveTo(21.596f, 34.529f, 20.326f, 34.002f, 19f, 34f)
                verticalLineTo(34f)
                close()
            }
            path(fill = SolidColor(Color(0xFF3E3D42))) {
                moveTo(49f, 46f)
                curveTo(47.616f, 46f, 46.262f, 45.59f, 45.111f, 44.82f)
                curveTo(43.96f, 44.051f, 43.063f, 42.958f, 42.533f, 41.679f)
                curveTo(42.003f, 40.4f, 41.864f, 38.992f, 42.134f, 37.634f)
                curveTo(42.405f, 36.277f, 43.071f, 35.029f, 44.05f, 34.05f)
                curveTo(45.029f, 33.071f, 46.277f, 32.405f, 47.634f, 32.134f)
                curveTo(48.992f, 31.864f, 50.4f, 32.003f, 51.679f, 32.533f)
                curveTo(52.958f, 33.063f, 54.051f, 33.96f, 54.82f, 35.111f)
                curveTo(55.59f, 36.262f, 56f, 37.616f, 56f, 39f)
                curveTo(55.998f, 40.856f, 55.26f, 42.635f, 53.947f, 43.947f)
                curveTo(52.635f, 45.26f, 50.856f, 45.998f, 49f, 46f)
                verticalLineTo(46f)
                close()
                moveTo(49f, 34f)
                curveTo(48.011f, 34f, 47.044f, 34.293f, 46.222f, 34.843f)
                curveTo(45.4f, 35.392f, 44.759f, 36.173f, 44.381f, 37.087f)
                curveTo(44.002f, 38f, 43.903f, 39.006f, 44.096f, 39.975f)
                curveTo(44.289f, 40.945f, 44.765f, 41.836f, 45.465f, 42.535f)
                curveTo(46.164f, 43.235f, 47.055f, 43.711f, 48.025f, 43.904f)
                curveTo(48.994f, 44.097f, 50f, 43.998f, 50.913f, 43.619f)
                curveTo(51.827f, 43.241f, 52.608f, 42.6f, 53.157f, 41.778f)
                curveTo(53.707f, 40.956f, 54f, 39.989f, 54f, 39f)
                curveTo(53.998f, 37.674f, 53.471f, 36.404f, 52.534f, 35.466f)
                curveTo(51.596f, 34.529f, 50.326f, 34.002f, 49f, 34f)
                verticalLineTo(34f)
                close()
            }
        }.build()

        return _Car!!
    }

@Suppress("ObjectPropertyName")
private var _Car: ImageVector? = null
