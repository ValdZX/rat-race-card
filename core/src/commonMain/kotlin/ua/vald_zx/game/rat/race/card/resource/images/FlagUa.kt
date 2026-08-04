package ua.vald_zx.game.rat.race.card.resource.images

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.resource.Images

val Images.FlagUa: ImageVector
    get() {
        if (_flagUa != null) {
            return _flagUa!!
        }
        _flagUa = Builder(
            name = "FlagUa", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
            viewportWidth = 60.0f, viewportHeight = 40.0f
        ).apply {
            path(fill = SolidColor(Color(0xFF0057B7))) {
                moveTo(0.0f, 0.0f)
                lineTo(60.0f, 0.0f)
                lineTo(60.0f, 20.0f)
                lineTo(0.0f, 20.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFD700))) {
                moveTo(0.0f, 20.0f)
                lineTo(60.0f, 20.0f)
                lineTo(60.0f, 40.0f)
                lineTo(0.0f, 40.0f)
                close()
            }
        }.build()
        return _flagUa!!
    }

private var _flagUa: ImageVector? = null
