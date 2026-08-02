package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
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
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import io.github.sudarshanmhasrup.localina.api.LocaleUpdater
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.AppDataStorageBean
import ua.vald_zx.game.rat.race.card.appKStore
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.logic.players
import ua.vald_zx.game.rat.race.card.resources.Res
import ua.vald_zx.game.rat.race.card.resources.dark_theme
import ua.vald_zx.game.rat.race.card.resources.language
import ua.vald_zx.game.rat.race.card.resources.new_design
import ua.vald_zx.game.rat.race.card.resources.online_settings
import ua.vald_zx.game.rat.race.card.resources.player_token_color
import ua.vald_zx.game.rat.race.card.shared.pointerColors
import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark

class OnlineSettingsScreen(private val vm: BoardViewModel) : Screen {
    @Composable
    override fun Content() {
        val state by vm.uiState.collectAsState()
        val allPlayers by players.collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val coroutineScope = rememberCoroutineScope()
        var isDark by LocalThemeIsDark.current
        var selectedColor by remember(state.player.id) {
            mutableStateOf(state.player.attrs.color)
        }
        LaunchedEffect(state.player.attrs.color) {
            selectedColor = state.player.attrs.color
        }

        BottomSheetContainer {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.online_settings),
                    style = MaterialTheme.typography.headlineSmall,
                )
                IconButton(onClick = { bottomSheetNavigator.hide() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.online_settings),
                    )
                }
            }

            SettingsRow(label = stringResource(Res.string.dark_theme)) {
                Switch(
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
            }

            SettingsRow(label = stringResource(Res.string.new_design)) {
                Switch(
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
            }

            val language = Locale.current.language
            SettingsRow(label = stringResource(Res.string.language)) {
                OutlinedButton(
                    onClick = {
                        LocaleUpdater.updateLocale(
                            if (language.startsWith("uk", ignoreCase = true)) "en" else "uk"
                        )
                    }
                ) {
                    Text(
                        if (language.startsWith("uk", ignoreCase = true)) {
                            "Українська"
                        } else {
                            "English"
                        }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = stringResource(Res.string.player_token_color),
                style = MaterialTheme.typography.titleMedium,
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
        Text(label)
        content()
    }
}
