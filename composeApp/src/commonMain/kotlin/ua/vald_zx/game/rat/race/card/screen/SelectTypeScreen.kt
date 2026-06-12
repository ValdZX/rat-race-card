package ua.vald_zx.game.rat.race.card.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.appKStore
import ua.vald_zx.game.rat.race.card.components.GoldRainbow
import ua.vald_zx.game.rat.race.card.components.SmoothRainbowText
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardStore
import ua.vald_zx.game.rat.race.card.raceRate2KStore
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Dice
import ua.vald_zx.game.rat.race.card.resource.images.Money
import ua.vald_zx.game.rat.race.card.screen.second.PersonCard2Screen
import ua.vald_zx.game.rat.race.card.screen.second.RaceRate2Screen
import ua.vald_zx.game.rat.race.card.theme.AppTheme

class SelectTypeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val raceRate2store = koinInject<RatRace2CardStore>()
        val coroutineScope = rememberCoroutineScope()
        var offlineReady by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            val savedState = runCatching { raceRate2KStore.get() }.getOrNull()
            if (savedState != null) {
                raceRate2store.dispatch(RatRace2CardAction.LoadState(savedState))
            }
            offlineReady = true
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBackground())
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 440.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                AppHeader()
                ModeCard(
                    icon = Images.Money,
                    accent = AppTheme.colors.cash,
                    title = stringResource(Res.string.offline_mode),
                    subtitle = stringResource(Res.string.offline_mode_desc),
                    enabled = offlineReady,
                    loading = !offlineReady
                ) {
                    coroutineScope.launch {
                        val state = raceRate2store.observeState().value
                        runCatching { appKStore.get() }.onSuccess {
                            if (state.playerCard.profession.isNotEmpty()) {
                                navigator.push(RaceRate2Screen())
                            } else {
                                navigator.push(PersonCard2Screen())
                            }
                        }.onFailure {
                            navigator.push(PersonCard2Screen())
                        }
                    }
                }
                ModeCard(
                    icon = Images.Dice,
                    accent = AppTheme.colors.family,
                    title = stringResource(Res.string.online_mode),
                    subtitle = stringResource(Res.string.online_mode_desc),
                    enabled = true,
                    loading = false
                ) {
                    navigator.push(LoadOnlineScreen())
                }
            }
        }
    }
}

@Composable
private fun AppHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(Res.drawable.logo),
            contentDescription = null,
            modifier = Modifier.size(112.dp)
        )
        SmoothRainbowText(
            text = stringResource(Res.string.app_name),
            rainbow = GoldRainbow,
            style = LocalTextStyle.current.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
        Text(
            text = stringResource(Res.string.select_mode_title),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ModeCard(
    icon: ImageVector,
    accent: Color,
    title: String,
    subtitle: String,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(30.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun screenBackground(): Brush = Brush.verticalGradient(
    listOf(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    )
)
