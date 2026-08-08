package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import io.github.sudarshanmhasrup.localina.api.LocaleUpdater
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.AppDataStorageBean
import ua.vald_zx.game.rat.race.card.appKStore
import ua.vald_zx.game.rat.race.card.clientVersion
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.soundEnabled
import ua.vald_zx.game.rat.race.card.vividPaletteEnabled
import ua.vald_zx.game.rat.race.card.components.ClosableBottomSheetContainer
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.design.DesignButton
import ua.vald_zx.game.rat.race.card.design.DesignButtonKind
import ua.vald_zx.game.rat.race.card.design.DesignToggleRow
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.logic.players
import ua.vald_zx.game.rat.race.card.resources.Res
import ua.vald_zx.game.rat.race.card.resources.dark_theme
import ua.vald_zx.game.rat.race.card.resources.language
import ua.vald_zx.game.rat.race.card.resources.new_design
import ua.vald_zx.game.rat.race.card.resources.online_settings
import ua.vald_zx.game.rat.race.card.resources.player_token_color
import ua.vald_zx.game.rat.race.card.resources.sound
import ua.vald_zx.game.rat.race.card.resources.vivid_palette
import ua.vald_zx.game.rat.race.card.shared.pointerColors
import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark

class OnlineSettingsScreen(private val vm: BoardViewModel) : Screen {
    @Composable
    override fun Content() {
        val state by vm.uiState.collectAsState()
        val allPlayers by players.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        var isDark by LocalThemeIsDark.current
        var selectedColor by remember(state.player.id) {
            mutableStateOf(state.player.attrs.color)
        }
        LaunchedEffect(state.player.attrs.color) {
            selectedColor = state.player.attrs.color
        }

        ClosableBottomSheetContainer {
            Text(
                text = stringResource(Res.string.online_settings),
                style = Design.type.title,
                color = Design.scaffold.onSurface,
            )

            DesignToggleRow(
                label = stringResource(Res.string.dark_theme),
                checked = isDark,
                onCheckedChange = { enabled ->
                    isDark = enabled
                    coroutineScope.launch {
                        appKStore.update { stored ->
                            (stored ?: AppDataStorageBean("", null)).copy(theme = enabled)
                        }
                    }
                },
            )

            DesignToggleRow(
                label = stringResource(Res.string.sound),
                checked = soundEnabled.value,
                onCheckedChange = { enabled ->
                    soundEnabled.value = enabled
                    coroutineScope.launch {
                        appKStore.update { stored ->
                            (stored ?: AppDataStorageBean("", null)).copy(sound = enabled)
                        }
                    }
                },
            )

            DesignToggleRow(
                label = stringResource(Res.string.new_design),
                checked = designV2Enabled.value,
                onCheckedChange = { enabled ->
                    designV2Enabled.value = enabled
                    coroutineScope.launch {
                        appKStore.update { stored ->
                            (stored ?: AppDataStorageBean("", null)).copy(designV2 = enabled)
                        }
                    }
                },
            )

            DesignToggleRow(
                label = stringResource(Res.string.vivid_palette),
                checked = vividPaletteEnabled.value,
                onCheckedChange = { enabled ->
                    vividPaletteEnabled.value = enabled
                    coroutineScope.launch {
                        appKStore.update { stored ->
                            (stored ?: AppDataStorageBean("", null)).copy(vividPalette = enabled)
                        }
                    }
                },
            )

            val language = Locale.current.language
            SettingsRow(label = stringResource(Res.string.language)) {
                DesignButton(
                    text = if (language.startsWith("uk", ignoreCase = true)) "Українська" else "English",
                    kind = DesignButtonKind.Tonal,
                    height = 40.dp,
                    onClick = {
                        LocaleUpdater.updateLocale(
                            if (language.startsWith("uk", ignoreCase = true)) "en" else "uk"
                        )
                    },
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Design.scaffold.outline,
            )
            Text(
                text = stringResource(Res.string.player_token_color),
                style = Design.type.subtitle,
                color = Design.scaffold.onSurface,
                modifier = Modifier.fillMaxWidth(),
            )
            val usedColors = allPlayers
                .filter { it.id != state.player.id }
                .map { it.attrs.color }
                .toSet()
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                pointerColors.forEach { color ->
                    RadioButton(
                        selected = selectedColor == color,
                        enabled = selectedColor == color || color !in usedColors,
                        onClick = {
                            selectedColor = color
                            vm.changePlayerColor(color)
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(color),
                            unselectedColor = Color(color),
                            disabledSelectedColor = Color(color).copy(alpha = 0.5f),
                            disabledUnselectedColor = Color(color).copy(alpha = 0.25f),
                        ),
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Design.scaffold.outline,
            )
            Text(
                text = clientVersion.details,
                style = Design.type.label,
                color = Design.scaffold.onSurfaceMuted,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SettingsRow(
    label: String,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = Design.type.body,
            color = Design.scaffold.onSurface,
        )
        content()
    }
}
