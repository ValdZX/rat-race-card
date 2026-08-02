package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.design.DesignAuctionSheet
import ua.vald_zx.game.rat.race.card.shared.Auction

class AuctionScreen(private val vm: BoardViewModel, private val auction: Auction) : Screen {
    @Composable
    override fun Content() {
        if (designV2Enabled.value) {
            DesignAuctionSheet(vm, auction)
        } else {
            LegacyAuctionScreen(vm, auction)
        }
    }
}
