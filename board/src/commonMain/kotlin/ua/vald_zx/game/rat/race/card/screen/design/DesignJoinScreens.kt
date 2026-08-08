package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.FemaleShape
import ua.vald_zx.game.rat.race.card.components.GenderOption
import ua.vald_zx.game.rat.race.card.components.GenderOptionStyle
import ua.vald_zx.game.rat.race.card.components.MaleShape
import ua.vald_zx.game.rat.race.card.components.NavigationBackButton
import ua.vald_zx.game.rat.race.card.components.clickableSingle
import ua.vald_zx.game.rat.race.card.design.*
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.shared.Gender
import ua.vald_zx.game.rat.race.card.shared.pointerColors
import ua.vald_zx.game.rat.race.card.shared.ProfessionCard
import ua.vald_zx.game.rat.race.card.shared.BoardGenerationProgress
import ua.vald_zx.game.rat.race.card.shared.BoardGenerationStage
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.GenerationQuotaType
import ua.vald_zx.game.rat.race.card.splitDecimal
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import ua.vald_zx.game.rat.race.card.formatAmount

@OptIn(ExperimentalTime::class)
@Composable
fun DesignBoardGenerationContent(
    progress: BoardGenerationProgress,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onRestart: () -> Unit,
) {
    val colors = Design.colors
    var nowEpochMs by remember { mutableLongStateOf(Clock.System.now().toEpochMilliseconds()) }
    LaunchedEffect(progress.isRunning, progress.activeSinceEpochMs) {
        nowEpochMs = Clock.System.now().toEpochMilliseconds()
        while (progress.isRunning) {
            delay(1_000)
            nowEpochMs = Clock.System.now().toEpochMilliseconds()
        }
    }
    val elapsed = progress.elapsedMillisAt(nowEpochMs).formatElapsedTime()
    val estimatedRemaining = progress.estimatedRemainingMillisAt(nowEpochMs)?.formatElapsedTime()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.scaffold.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        NavigationBackButton(onClick = onBack)
        Spacer(Modifier.weight(1f))
        Text(
            text = stringResource(
                if (progress.isRunning) Res.string.generating_board_title
                else Res.string.generation_waiting_title
            ),
            style = Design.type.title,
            color = colors.scaffold.onSurface,
        )
        Text(
            text = generationStageText(progress),
            style = Design.type.body,
            color = colors.scaffold.onSurfaceMuted,
        )
        progress.retryAtEpochMs?.let { retryAt ->
            val remainingSeconds = ((retryAt - nowEpochMs).coerceAtLeast(0) + 999) / 1_000
            Text(
                text = stringResource(
                    Res.string.generation_rate_limit_wait,
                    progress.retryProvider,
                    remainingSeconds.times(1_000).formatElapsedTime(),
                ),
                style = Design.type.body,
                color = colors.scaffold.accent,
            )
        }
        LinearProgressIndicator(
            progress = { progress.fraction },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(DesignShapes.xs),
            color = colors.scaffold.accent,
            trackColor = colors.scaffold.surface2,
        )
        if (progress.total > 1) {
            Text(
                text = "${progress.completed} / ${progress.total}",
                style = Design.type.monoMeta,
                color = colors.scaffold.onSurfaceMuted,
            )
        }
        Text(
            text = stringResource(Res.string.generation_elapsed_time, elapsed),
            style = Design.type.monoMeta,
            color = colors.scaffold.onSurfaceMuted,
        )
        estimatedRemaining?.let { remaining ->
            Text(
                text = stringResource(Res.string.generation_estimated_remaining, remaining),
                style = Design.type.monoMeta,
                color = colors.scaffold.onSurfaceMuted,
            )
        }
        Text(
            text = stringResource(
                Res.string.generation_token_usage,
                progress.totalTokens.splitDecimal(),
                progress.inputTokens.splitDecimal(),
                progress.outputTokens.splitDecimal(),
            ),
            style = Design.type.monoMeta,
            color = colors.scaffold.onSurfaceMuted,
        )
        Text(
            text = stringResource(Res.string.generation_request_count, progress.requestCount),
            style = Design.type.monoMeta,
            color = colors.scaffold.onSurfaceMuted,
        )
        val quotaResetAt = progress.quotaResetAtEpochMs
        if (
            progress.quotaLimit > 0 &&
            progress.quotaType != GenerationQuotaType.UNKNOWN &&
            (quotaResetAt == null || quotaResetAt > nowEpochMs)
        ) {
            Text(
                text = stringResource(
                    Res.string.generation_quota_usage,
                    generationQuotaName(progress.quotaType),
                    progress.quotaRemaining.splitDecimal(),
                    progress.quotaLimit.splitDecimal(),
                ),
                style = Design.type.monoMeta,
                color = colors.scaffold.onSurfaceMuted,
            )
            quotaResetAt?.let { resetAt ->
                val resetIn = (resetAt - nowEpochMs).coerceAtLeast(0).formatElapsedTime()
                Text(
                    text = stringResource(Res.string.generation_quota_reset_in, resetIn),
                    style = Design.type.monoMeta,
                    color = colors.scaffold.onSurfaceMuted,
                )
            }
        }
        if (progress.isFailed) {
            Text(
                text = progress.error.ifBlank { stringResource(Res.string.generation_failed) },
                style = Design.type.body,
                color = Design.semantic.negative.edge,
            )
        }
        if (!progress.isRunning) {
            DesignButton(
                text = stringResource(Res.string.continue_generation),
                modifier = Modifier.fillMaxWidth(),
                height = 52.dp,
                onClick = onContinue,
            )
            DesignButton(
                text = stringResource(Res.string.restart_generation),
                modifier = Modifier.fillMaxWidth(),
                kind = DesignButtonKind.Tonal,
                height = 52.dp,
                onClick = onRestart,
            )
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun generationQuotaName(type: GenerationQuotaType): String = stringResource(when (type) {
    GenerationQuotaType.REQUESTS_PER_MINUTE -> Res.string.generation_quota_requests_minute
    GenerationQuotaType.INPUT_TOKENS_PER_MINUTE -> Res.string.generation_quota_tokens_minute
    GenerationQuotaType.REQUESTS_PER_DAY -> Res.string.generation_quota_requests_day
    GenerationQuotaType.INPUT_TOKENS_PER_DAY -> Res.string.generation_quota_tokens_day
    GenerationQuotaType.SPEND_PER_TEN_MINUTES -> Res.string.generation_quota_spend
    GenerationQuotaType.UNKNOWN -> Res.string.generation_quota_unknown
})

private fun Long.formatElapsedTime(): String {
    val totalSeconds = div(1_000).coerceAtLeast(0)
    val seconds = totalSeconds.rem(60).toString().padStart(2, '0')
    val totalMinutes = totalSeconds.div(60)
    val minutes = totalMinutes.rem(60).toString().padStart(2, '0')
    val hours = totalMinutes.div(60)
    return if (hours > 0) "$hours:$minutes:$seconds" else "$minutes:$seconds"
}

@Composable
private fun generationStageText(progress: BoardGenerationProgress): String {
    val stage = stringResource(when (progress.stage) {
        BoardGenerationStage.READY -> Res.string.generation_stage_ready
        BoardGenerationStage.PREPARING -> Res.string.generation_stage_preparing
        BoardGenerationStage.BALANCE -> Res.string.generation_stage_balance
        BoardGenerationStage.PROFESSIONS -> Res.string.generation_stage_professions
        BoardGenerationStage.CARDS -> Res.string.generation_stage_cards
        BoardGenerationStage.PLACES -> Res.string.generation_stage_places
        BoardGenerationStage.TEXTS -> Res.string.generation_stage_texts
        BoardGenerationStage.FAILED -> Res.string.generation_failed
    })
    if (progress.stage != BoardGenerationStage.CARDS) return stage
    val type = runCatching { BoardCardType.valueOf(progress.detail) }.getOrNull() ?: return stage
    val deck = stringResource(when (type) {
        BoardCardType.Chance -> Res.string.chance
        BoardCardType.Expenses -> Res.string.expenses
        BoardCardType.Shopping -> Res.string.shopping
        BoardCardType.EventStore -> Res.string.store
        BoardCardType.Deputy -> Res.string.deputy
        BoardCardType.SmallBusiness -> Res.string.small_business
        BoardCardType.MediumBusiness -> Res.string.medium_business
        BoardCardType.BigBusiness -> Res.string.big_business
    })
    return "$stage: $deck"
}

@Composable
fun DesignInitPlayerContent(
    colorState: MutableState<Long>,
    playerName: String,
    onNameChange: (String) -> Unit,
    gender: Gender,
    onGenderChange: (Gender) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    val colors = Design.colors
    LaunchedEffect(Unit) {
        if (colorState.value !in pointerColors) colorState.value = pointerColors.first()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.scaffold.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavigationBackButton(onClick = onBack)
            Text(
                text = stringResource(Res.string.create_player_title),
                style = Design.type.title,
                color = colors.scaffold.onSurface,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        PlayerPreview(
            color = Color(colorState.value),
            playerName = playerName,
            gender = gender,
        )
        DesignTextField(
            value = playerName,
            onValueChange = onNameChange,
            label = stringResource(Res.string.player_name),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
        DesignSectionTitle(stringResource(Res.string.player_token_color))
        ColorSwatches(
            selected = colorState.value,
            onSelect = { colorState.value = it },
        )
        DesignSectionTitle(stringResource(Res.string.gender_label))
        GenderTiles(
            gender = gender,
            color = Color(colorState.value),
            onGenderChange = onGenderChange,
        )
        Spacer(Modifier.height(2.dp))
        DesignButton(
            text = stringResource(Res.string.next),
            enabled = playerName.isNotEmpty(),
            disabledReason = stringResource(Res.string.enter_name_first),
            modifier = Modifier.fillMaxWidth(),
            height = 52.dp,
            onClick = onNext,
        )
    }
}

@Composable
private fun PlayerPreview(color: Color, playerName: String, gender: Gender) {
    val colors = Design.colors
    val ink = if (color.luminance() > 0.45f) Color(0xFF10160F) else Color(0xFFF6FAF7)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .levelCard(colors, DesignShapes.lg)
            .clip(DesignShapes.lg)
            .background(colors.scaffold.surface1)
            .border(1.dp, colors.scaffold.outline, DesignShapes.lg)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .plinth(colors.scaffold.outlineStrong, 4.dp, DesignShapes.full)
                .clip(DesignShapes.full)
                .background(color)
                .border(3.dp, colors.scaffold.onSurface, DesignShapes.full),
            contentAlignment = Alignment.Center,
        ) {
            GenderGlyph(gender = gender, tint = ink, size = 44.dp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(Res.string.your_token),
                style = Design.type.micro,
                color = colors.scaffold.onSurfaceMuted,
            )
            Text(
                text = playerName.ifBlank { stringResource(Res.string.player_name) },
                style = Design.type.title,
                color = if (playerName.isBlank()) {
                    colors.scaffold.onSurfaceMuted
                } else {
                    colors.scaffold.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ColorSwatches(selected: Long, onSelect: (Long) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        pointerColors.forEach { color ->
            ColorSwatch(
                color = color,
                selected = color == selected,
                onClick = { onSelect(color) },
            )
        }
    }
}

@Composable
private fun ColorSwatch(color: Long, selected: Boolean, onClick: () -> Unit) {
    val colors = Design.colors
    val diameter by animateDpAsState(
        targetValue = if (selected) 44.dp else 34.dp,
        animationSpec = tween(140),
        label = "SwatchSize",
    )
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(DesignShapes.full)
            .testTag("swatch_$color")
            .clickableSingle(role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(diameter)
                .plinth(colors.scaffold.outlineStrong, if (selected) 3.dp else 2.dp, DesignShapes.full)
                .clip(DesignShapes.full)
                .background(Color(color))
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) colors.scaffold.onSurface else colors.scaffold.outline,
                    shape = DesignShapes.full,
                )
        )
    }
}

@Composable
private fun GenderTiles(gender: Gender, color: Color, onGenderChange: (Gender) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GenderTile(
            gender = Gender.FEMALE,
            selected = gender == Gender.FEMALE,
            color = color,
            modifier = Modifier.weight(1f),
            onClick = { onGenderChange(Gender.FEMALE) },
        )
        GenderTile(
            gender = Gender.MALE,
            selected = gender == Gender.MALE,
            color = color,
            modifier = Modifier.weight(1f),
            onClick = { onGenderChange(Gender.MALE) },
        )
    }
}

@Composable
private fun GenderTile(
    gender: Gender,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = Design.colors
    Box(
        modifier = modifier
            .plinth(
                color = if (selected) colors.scaffold.accentDim else colors.scaffold.outline,
                depth = if (selected) 4.dp else 2.dp,
                shape = DesignShapes.lg,
            )
            .clip(DesignShapes.lg)
            .background(if (selected) colors.scaffold.surface3 else colors.scaffold.surface2)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) colors.scaffold.accent else colors.scaffold.outline,
                shape = DesignShapes.lg,
            )
            .testTag("gender_${gender.name}")
            .clickableSingle(role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        GenderGlyph(
            gender = gender,
            tint = if (selected) color else colors.scaffold.onSurfaceMuted,
            size = 84.dp,
        )
    }
}

@Composable
private fun GenderGlyph(gender: Gender, tint: Color, size: Dp) {
    GenderOption(
        shape = if (gender == Gender.FEMALE) FemaleShape else MaleShape,
        selected = true,
        style = GenderOptionStyle(
            backgroundColor = tint,
            fillColor = tint,
            effectOrigin = TransformOrigin.Center,
        ),
        modifier = Modifier.size(size),
    )
}

@Composable
fun DesignProfessionContent(
    card: ProfessionCard,
    isLoading: Boolean = false,
    onBack: () -> Unit = {},
    onNext: () -> Unit,
) {
    val colors = Design.colors
    val expenses = card.rent + card.food + card.cloth + card.transport + card.phone
    val cashFlow = card.salary - expenses
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.scaffold.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        NavigationBackButton(onClick = onBack)
        ProfessionHeader(card, cashFlow)

        DesignSectionTitle(stringResource(Res.string.expenses))
        ExpenseRow(stringResource(Res.string.rent), card.rent)
        ExpenseRow(stringResource(Res.string.food), card.food)
        ExpenseRow(stringResource(Res.string.cloth), card.cloth)
        ExpenseRow(stringResource(Res.string.transport), card.transport)
        ExpenseRow(stringResource(Res.string.phone), card.phone)

        Spacer(Modifier.height(4.dp))
        DesignButton(
            text = stringResource(if (isLoading) Res.string.connecting_to_server else Res.string.next),
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            height = 52.dp,
            onClick = onNext,
        )
    }
}

@Composable
private fun ProfessionHeader(card: ProfessionCard, cashFlow: Long) {
    val colors = Design.colors
    val type = Design.type
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .levelCard(colors, DesignShapes.lg)
            .clip(DesignShapes.lg)
            .background(colors.scaffold.surface1)
            .border(1.dp, colors.scaffold.outline, DesignShapes.lg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(Res.string.work),
            style = type.micro,
            color = colors.scaffold.onSurfaceMuted,
        )
        Text(
            text = card.name,
            style = type.title,
            color = colors.scaffold.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (card.description.isNotBlank()) {
            Text(
                text = card.description,
                style = type.body,
                color = colors.scaffold.onSurfaceMuted,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ValueField(
                label = stringResource(Res.string.salary),
                amount = card.salary,
                tone = Design.semantic.salary,
                modifier = Modifier.weight(1f),
            )
            ValueField(
                label = stringResource(Res.string.cash_flow),
                amount = cashFlow,
                tone = if (cashFlow >= 0) Design.semantic.positive else Design.semantic.negative,
                signed = true,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ExpenseRow(label: String, amount: Long) {
    val colors = Design.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DesignShapes.sm)
            .background(colors.scaffold.surface2)
            .border(1.dp, colors.scaffold.outline, DesignShapes.sm)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .width(4.dp)
                .height(20.dp)
                .clip(DesignShapes.xs)
                .background(Design.semantic.expenses.edge)
        )
        Text(
            text = label,
            style = Design.type.body,
            color = colors.scaffold.onSurface,
            modifier = Modifier.weight(1f).padding(start = 10.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "−${amount.formatAmount()}",
            style = Design.type.amountMd,
            color = Design.semantic.expenses.edge,
            maxLines = 1,
            softWrap = false,
        )
    }
}
