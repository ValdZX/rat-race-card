package ua.vald_zx.game.rat.race.card.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object DesignRadius {
    val cell = 6.dp
    val xs = 10.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 22.dp
    val sheet = 28.dp
}

object DesignShapes {
    val cell = RoundedCornerShape(DesignRadius.cell)
    val xs = RoundedCornerShape(DesignRadius.xs)
    val sm = RoundedCornerShape(DesignRadius.sm)
    val md = RoundedCornerShape(DesignRadius.md)
    val lg = RoundedCornerShape(DesignRadius.lg)
    val xl = RoundedCornerShape(DesignRadius.xl)
    val dialog = RoundedCornerShape(DesignRadius.sheet)
    val sheetTop = RoundedCornerShape(topStart = DesignRadius.sheet, topEnd = DesignRadius.sheet)
    val full = RoundedCornerShape(percent = 50)
}
