package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.design.DesignShapes
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.shared.Gender
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerStatus
import ua.vald_zx.game.rat.race.card.shared.status

@Composable
fun Player.statusLabel(): String {
    val female = card.gender == Gender.FEMALE
    return when (status()) {
        PlayerStatus.FINALIST -> stringResource(Res.string.status_finalist)
        PlayerStatus.MULTIMILLIONAIRE ->
            stringResource(if (female) Res.string.status_multimillionaire_f else Res.string.status_multimillionaire_m)
        PlayerStatus.CORRUPT ->
            stringResource(if (female) Res.string.status_corrupt_f else Res.string.status_corrupt_m)
        PlayerStatus.MAGNATE ->
            stringResource(if (female) Res.string.status_magnate_f else Res.string.status_magnate_m)
        PlayerStatus.MILLIONAIRE ->
            stringResource(if (female) Res.string.status_millionaire_f else Res.string.status_millionaire_m)
        PlayerStatus.BROKE -> stringResource(Res.string.status_broke)
        PlayerStatus.BUSINESSMAN ->
            stringResource(if (female) Res.string.status_businessman_f else Res.string.status_businessman_m)
        PlayerStatus.ENTREPRENEUR ->
            stringResource(if (female) Res.string.status_entrepreneur_f else Res.string.status_entrepreneur_m)
        PlayerStatus.DEPUTY ->
            stringResource(if (female) Res.string.status_deputy_f else Res.string.status_deputy_m)
        PlayerStatus.FIRED ->
            stringResource(if (female) Res.string.status_fired_f else Res.string.status_fired_m)
        PlayerStatus.FAMILY ->
            stringResource(if (female) Res.string.status_family_f else Res.string.status_family_m)
        PlayerStatus.RENTIER -> stringResource(Res.string.status_rentier)
        PlayerStatus.MARRIED ->
            stringResource(if (female) Res.string.status_married_f else Res.string.status_married_m)
        PlayerStatus.INVESTOR ->
            stringResource(if (female) Res.string.status_investor_f else Res.string.status_investor_m)
        PlayerStatus.EMPLOYEE -> card.profession
    }
}

@Composable
fun PlayerStatus.tierColor(): Color {
    return when (this) {
        PlayerStatus.FINALIST, PlayerStatus.MULTIMILLIONAIRE -> Design.scaffold.brass
        PlayerStatus.CORRUPT -> Design.semantic.chance.edge
        PlayerStatus.MAGNATE -> Design.semantic.bigBusiness.edge
        PlayerStatus.MILLIONAIRE -> Design.semantic.positive.edge
        PlayerStatus.BROKE -> Design.semantic.negative.edge
        PlayerStatus.BUSINESSMAN -> Design.semantic.business.edge
        PlayerStatus.ENTREPRENEUR -> Design.semantic.smallBusiness.edge
        PlayerStatus.DEPUTY -> Design.semantic.deputy.edge
        PlayerStatus.FIRED -> Design.semantic.exaltation.edge
        PlayerStatus.FAMILY -> Design.semantic.family.edge
        PlayerStatus.RENTIER -> Design.semantic.desire.edge
        PlayerStatus.MARRIED -> Design.semantic.love.edge
        PlayerStatus.INVESTOR -> Design.semantic.funds.edge
        PlayerStatus.EMPLOYEE -> Design.scaffold.onSurfaceMuted
    }
}

private const val STATUS_PROGRESS_DOTS = 5

internal fun PlayerStatus.progressFilled(): Int {
    val fromTop = PlayerStatus.entries.size - tier
    return 1 + fromTop * (STATUS_PROGRESS_DOTS - 1) / (PlayerStatus.entries.size - 1)
}

@Composable
fun StatusProgress(status: PlayerStatus, modifier: Modifier = Modifier) {
    val colors = Design.colors
    val filled = status.progressFilled()
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(STATUS_PROGRESS_DOTS) { index ->
            Box(
                Modifier
                    .size(4.dp)
                    .clip(DesignShapes.full)
                    .background(if (index < filled) colors.scaffold.brass else colors.scaffold.outline)
            )
        }
    }
}
