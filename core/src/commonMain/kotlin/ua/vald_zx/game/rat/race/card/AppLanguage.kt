package ua.vald_zx.game.rat.race.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.intl.Locale
import io.github.sudarshanmhasrup.localina.api.LocaleUpdater
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.FlagGb
import ua.vald_zx.game.rat.race.card.resource.images.FlagUa

enum class AppLanguage(val code: String, val label: String, val title: String) {
    Ukrainian("uk", "UA", "Українська"),
    English("en", "EN", "English");

    val flag: ImageVector
        get() = when (this) {
            Ukrainian -> Images.FlagUa
            English -> Images.FlagGb
        }

    companion object {
        fun ofCode(code: String?): AppLanguage =
            entries.firstOrNull { code?.startsWith(it.code, ignoreCase = true) == true } ?: English
    }
}

val currentAppLanguage: AppLanguage
    @Composable get() = AppLanguage.ofCode(Locale.current.language)

suspend fun applyAppLanguage(language: AppLanguage) {
    LocaleUpdater.updateLocale(language.code)
    appKStore.update { stored ->
        (stored ?: AppDataStorageBean("", null)).copy(language = language.code)
    }
}

fun restoreAppLanguage(stored: AppDataStorageBean?) {
    val code = stored?.language ?: return
    LocaleUpdater.updateLocale(code)
}
