package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.shared.Gender
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerStatus
import ua.vald_zx.game.rat.race.card.shared.status

@Composable
fun Player.statusLabel(): String {
    val female = card.gender == Gender.FEMALE
    return when (status()) {
        PlayerStatus.MAGNATE ->
            stringResource(if (female) Res.string.status_magnate_f else Res.string.status_magnate_m)
        PlayerStatus.MILLIONAIRE ->
            stringResource(if (female) Res.string.status_millionaire_f else Res.string.status_millionaire_m)
        PlayerStatus.BROKE -> stringResource(Res.string.status_broke)
        PlayerStatus.BUSINESSMAN ->
            stringResource(if (female) Res.string.status_businessman_f else Res.string.status_businessman_m)
        PlayerStatus.ENTREPRENEUR ->
            stringResource(if (female) Res.string.status_entrepreneur_f else Res.string.status_entrepreneur_m)
        PlayerStatus.RENTIER -> stringResource(Res.string.status_rentier)
        PlayerStatus.INVESTOR ->
            stringResource(if (female) Res.string.status_investor_f else Res.string.status_investor_m)
        PlayerStatus.FIRED ->
            stringResource(if (female) Res.string.status_fired_f else Res.string.status_fired_m)
        PlayerStatus.EMPLOYEE -> card.profession
    }
}
