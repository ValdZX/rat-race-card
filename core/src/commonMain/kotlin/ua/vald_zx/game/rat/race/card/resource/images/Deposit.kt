package ua.vald_zx.game.rat.race.card.resource.images

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.resource.Images

val Images.Deposit: ImageVector
    get() {
        if (_Deposit != null) {
            return _Deposit!!
        }
        _Deposit = ImageVector.Builder(
            name = "Deposit",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF333333))) {
                moveTo(20f, 20f)
                horizontalLineTo(4f)
                arcToRelative(2.003f, 2.003f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2f, -2f)
                verticalLineTo(6f)
                arcTo(2.003f, 2.003f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, 4f)
                horizontalLineTo(20f)
                arcToRelative(2.003f, 2.003f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, 2f)
                verticalLineTo(8.285f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2f, 0f)
                verticalLineTo(6f)
                horizontalLineTo(4f)
                verticalLineTo(18f)
                horizontalLineTo(20f)
                verticalLineTo(12.285f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, 0f)
                verticalLineTo(18f)
                arcTo(2.003f, 2.003f, 0f, isMoreThanHalf = false, isPositiveArc = true, 20f, 20f)
                close()
            }
            path(fill = SolidColor(Color(0xFF333333))) {
                moveTo(7f, 14f)
                arcToRelative(0.999f, 0.999f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1f, -1f)
                verticalLineTo(11f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, 0f)
                verticalLineToRelative(2f)
                arcTo(0.999f, 0.999f, 0f, isMoreThanHalf = false, isPositiveArc = true, 7f, 14f)
                close()
            }
            path(fill = SolidColor(Color(0xFF333333))) {
                moveTo(14f, 16f)
                arcToRelative(4.019f, 4.019f, 0f, isMoreThanHalf = false, isPositiveArc = true, -3.96f, -3.431f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.98f, -0.283f)
                arcTo(2f, 2f, 0f, isMoreThanHalf = true, isPositiveArc = false, 14f, 10f)
                horizontalLineToRelative(-0.005f)
                arcToRelative(1.97f, 1.97f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.738f, 0.142f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = true, -0.746f, -1.855f)
                arcTo(3.963f, 3.963f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14.003f, 8f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14f, 16f)
                close()
            }
            path(fill = SolidColor(Color(0xFF333333))) {
                moveTo(22f, 9.25f)
                horizontalLineTo(21f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -2f)
                horizontalLineToRelative(1f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 2f)
                close()
            }
            path(fill = SolidColor(Color(0xFF333333))) {
                moveTo(22f, 17f)
                horizontalLineTo(21f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -2f)
                horizontalLineToRelative(1f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 2f)
                close()
            }
        }.build()

        return _Deposit!!
    }

@Suppress("ObjectPropertyName")
private var _Deposit: ImageVector? = null
