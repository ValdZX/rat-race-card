package ua.vald_zx.game.rat.race.card.design

import androidx.compose.runtime.Composable

object Design {
    val colors: DesignColors
        @Composable get() = LocalDesignColors.current
    val scaffold: ScaffoldColors
        @Composable get() = LocalDesignColors.current.scaffold
    val semantic: SemanticColors
        @Composable get() = LocalDesignColors.current.semantic
    val type: DesignTypography
        @Composable get() = LocalDesignTypography.current
}
