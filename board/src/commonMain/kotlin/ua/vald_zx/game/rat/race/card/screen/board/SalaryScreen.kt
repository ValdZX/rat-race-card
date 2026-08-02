package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.design.DesignSalarySheet

class SalaryScreen(private val vm: BoardViewModel) : Screen {
    @Composable
    override fun Content() {
        if (designV2Enabled.value) DesignSalarySheet(vm) else LegacySalaryScreen(vm)
    }
}
