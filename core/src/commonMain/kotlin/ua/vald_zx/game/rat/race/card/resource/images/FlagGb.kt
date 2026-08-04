package ua.vald_zx.game.rat.race.card.resource.images

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.resource.Images

private val UnionBlue = Color(0xFF012169)
private val UnionRed = Color(0xFFC8102E)
private val UnionWhite = Color(0xFFFFFFFF)

val Images.FlagGb: ImageVector
    get() {
        if (_flagGb != null) {
            return _flagGb!!
        }
        _flagGb = Builder(
            name = "FlagGb", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
            viewportWidth = 60.0f, viewportHeight = 40.0f
        ).apply {
            path(fill = SolidColor(UnionBlue)) {
                moveTo(0.0f, 0.0f)
                lineTo(60.0f, 0.0f)
                lineTo(60.0f, 40.0f)
                lineTo(0.0f, 40.0f)
                close()
            }
            diagonals(UnionWhite, 8.0f)
            diagonals(UnionRed, 3.5f)
            cross(UnionWhite, 13.0f)
            cross(UnionRed, 7.0f)
        }.build()
        return _flagGb!!
    }

private fun Builder.diagonals(color: Color, width: Float) {
    path(stroke = SolidColor(color), strokeLineWidth = width) {
        moveTo(0.0f, 0.0f)
        lineTo(60.0f, 40.0f)
        moveTo(60.0f, 0.0f)
        lineTo(0.0f, 40.0f)
    }
}

private fun Builder.cross(color: Color, width: Float) {
    path(stroke = SolidColor(color), strokeLineWidth = width) {
        moveTo(30.0f, 0.0f)
        lineTo(30.0f, 40.0f)
        moveTo(0.0f, 20.0f)
        lineTo(60.0f, 20.0f)
    }
}

private var _flagGb: ImageVector? = null
