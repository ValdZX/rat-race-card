package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.GenderOptionStyle
import ua.vald_zx.game.rat.race.card.components.GenderSelector
import ua.vald_zx.game.rat.race.card.design.*
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Back
import ua.vald_zx.game.rat.race.card.screen.board.ColorsSelector
import ua.vald_zx.game.rat.race.card.shared.Gender
import ua.vald_zx.game.rat.race.card.shared.ProfessionCard
import ua.vald_zx.game.rat.race.card.splitDecimal

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.scaffold.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Images.Back, contentDescription = null)
            }
            Text(
                text = stringResource(Res.string.player_name_label),
                style = Design.type.title,
                color = colors.scaffold.onSurface,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ColorsSelector(colorState)
        }
        DesignTextField(
            value = playerName,
            onValueChange = onNameChange,
            label = stringResource(Res.string.player_name),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
        GenderSelector(
            selected = gender,
            iconSize = 100.dp,
            femaleStyle = GenderOptionStyle.FemaleDefault.copy(
                effectOrigin = TransformOrigin(1f, 0.5f),
                fillColor = Color(colorState.value),
            ),
            maleStyle = GenderOptionStyle.MaleDefault.copy(
                effectOrigin = TransformOrigin(0f, 0.5f),
                fillColor = Color(colorState.value),
            ),
            onGenderChange = onGenderChange,
        )
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
fun DesignProfessionContent(
    card: ProfessionCard,
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
        IconButton(onClick = onBack) {
            Icon(Images.Back, contentDescription = null)
        }
        ProfessionHeader(card, cashFlow)

        DesignSectionTitle(stringResource(Res.string.expenses))
        ExpenseRow(stringResource(Res.string.rent), card.rent)
        ExpenseRow(stringResource(Res.string.food), card.food)
        ExpenseRow(stringResource(Res.string.cloth), card.cloth)
        ExpenseRow(stringResource(Res.string.transport), card.transport)
        ExpenseRow(stringResource(Res.string.phone), card.phone)

        Spacer(Modifier.height(4.dp))
        DesignButton(
            text = stringResource(Res.string.next),
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
            text = "−${amount.splitDecimal()}",
            style = Design.type.amountMd,
            color = Design.semantic.expenses.edge,
            maxLines = 1,
            softWrap = false,
        )
    }
}
