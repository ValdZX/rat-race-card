package ua.vald_zx.game.rat.race.card.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicMaterialThemeState
import ua.vald_zx.game.rat.race.card.design.DarkDesignColors
import ua.vald_zx.game.rat.race.card.design.DesignSeed
import ua.vald_zx.game.rat.race.card.design.LightDesignColors
import ua.vald_zx.game.rat.race.card.design.LocalDesignColors
import ua.vald_zx.game.rat.race.card.design.LocalDesignTypography
import ua.vald_zx.game.rat.race.card.design.designTypography
import ua.vald_zx.game.rat.race.card.designV2Enabled

val Primary = Color(0xFFA2FF82)

val LocalThemeIsDark = compositionLocalOf { mutableStateOf(true) }

@Composable
fun AppTheme(
    forceDark: Boolean? = null,
    content: @Composable () -> Unit
) {
    val systemInDarkTheme = isSystemInDarkTheme()
    val isDarkState = remember { mutableStateOf(forceDark ?: systemInDarkTheme) }
    val isDark by isDarkState
    val designV2 = designV2Enabled.value

    val typography = montserratTypography()
    val colors = if (isDark) DarkIfobsColors else LightIfobsColors
    val colorPalette = remember { colors.copy() }
    colorPalette.update(colors)
    val designColors = if (isDark) DarkDesignColors else LightDesignColors

    val dynamicThemeState = rememberDynamicMaterialThemeState(
        isDark = isDark,
        style = PaletteStyle.TonalSpot,
        primary = if (designV2) DesignSeed else Primary,
    )
    CompositionLocalProvider(
        LocalThemeIsDark provides isDarkState,
        LocalAppColors provides colorPalette,
        LocalDesignColors provides designColors,
        LocalDesignTypography provides designTypography(),
    ) {
        DynamicMaterialTheme(
            state = dynamicThemeState,
            typography = typography,
            animate = true,
            content = {
                if (designV2) {
                    Surface(color = designColors.scaffold.background, content = content)
                } else {
                    Surface(content = content)
                }
            },
        )
    }
}

object AppTheme {
    val colors: AppColors
        @Composable
        get() = LocalAppColors.current
}

@Composable
internal expect fun SystemAppearance(isDark: Boolean)
