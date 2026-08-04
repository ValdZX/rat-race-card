package ua.vald_zx.game.rat.race.card.screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import ua.vald_zx.game.rat.race.card.appKStore
import ua.vald_zx.game.rat.race.card.applyAppLanguage
import ua.vald_zx.game.rat.race.card.components.GoldRainbow
import ua.vald_zx.game.rat.race.card.components.SmoothRainbowText
import ua.vald_zx.game.rat.race.card.components.clickableSingle
import ua.vald_zx.game.rat.race.card.currentAppLanguage
import ua.vald_zx.game.rat.race.card.design.*
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardStore
import ua.vald_zx.game.rat.race.card.raceRate2KStore
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Deposit
import ua.vald_zx.game.rat.race.card.resource.images.Dice
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.screen.second.PersonCard2Screen
import ua.vald_zx.game.rat.race.card.screen.second.RaceRate2Screen

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
                .background(Design.colors.scaffold.background)
                .background(screenBackground()),
            contentAlignment = Alignment.Center
        ) {
            DesignIconDrift(Modifier.matchParentSize())
            Column(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(24.dp)
                    .widthIn(max = 440.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                val language = currentAppLanguage
                DesignLanguagePicker(
                    selected = language,
                    modifier = Modifier.align(Alignment.End),
                    onSelect = { selected ->
                        if (selected != language) {
                            coroutineScope.launch { applyAppLanguage(selected) }
                        }
                    },
                )
                AppHeader()
                ModeCard(
                    icon = Images.Deposit,
                    tone = Design.semantic.cash,
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
                    tone = Design.semantic.family,
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
    val colors = Design.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(168.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                colors.scaffold.accent.copy(alpha = 0.22f),
                                colors.scaffold.background.copy(alpha = 0f),
                            )
                        ),
                        CircleShape,
                    )
            )
            Box(
                modifier = Modifier
                    .plinth(colors.scaffold.outlineStrong, 4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(colors.scaffold.surface2)
                    .border(1.dp, colors.scaffold.outline, CircleShape)
                    .padding(14.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(96.dp)
                )
            }
        }
        SmoothRainbowText(
            text = stringResource(Res.string.app_name),
            rainbow = GoldRainbow,
            style = LocalTextStyle.current.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            horizontalArrangement = Arrangement.Center
        )
        Text(
            text = stringResource(Res.string.select_mode_title),
            style = Design.type.label,
            color = colors.scaffold.onSurfaceMuted,
        )
    }
}

@Composable
private fun ModeCard(
    icon: ImageVector,
    tone: SemanticTone,
    title: String,
    subtitle: String,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit
) {
    val colors = Design.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val depth by animateDpAsState(
        targetValue = if (!enabled) 0.dp else if (pressed) 1.dp else 5.dp,
        animationSpec = tween(90),
        label = "ModeCardDepth",
    )
    val shift by animateDpAsState(
        targetValue = if (enabled && pressed) 4.dp else 0.dp,
        animationSpec = tween(90),
        label = "ModeCardShift",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = shift)
            .plinth(tone.edge, depth, DesignShapes.lg)
            .clip(DesignShapes.lg)
            .background(colors.scaffold.surface2)
            .border(1.dp, colors.scaffold.outline, DesignShapes.lg)
            .clickableSingle(enabled = enabled, interactionSource = interaction, onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(DesignShapes.md)
                .background(tone.fill)
                .border(1.5.dp, tone.edge, DesignShapes.md),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tone.edge,
                modifier = Modifier.size(30.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = Design.type.subtitle,
                color = colors.scaffold.onSurface,
            )
            Text(
                text = subtitle,
                style = Design.type.body,
                color = colors.scaffold.onSurfaceMuted,
            )
        }
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = colors.scaffold.accent,
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = tone.edge,
            )
        }
    }
}

@Composable
private fun screenBackground(): Brush {
    val colors = Design.colors
    return Brush.verticalGradient(
        listOf(
            colors.scaffold.surface1,
            colors.scaffold.background,
            colors.scaffold.accentDim.copy(alpha = if (colors.isDark) 0.18f else 0.12f),
        )
    )
}
