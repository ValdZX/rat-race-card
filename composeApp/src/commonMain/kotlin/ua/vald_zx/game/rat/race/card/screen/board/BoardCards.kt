package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.theme.AppTheme

sealed class BoardCard(val type: BoardCardType) {
    data class SmallBusiness(
        val title: String,
        val description: String,
        val price: Long,
        val profit: Long,
    ) : BoardCard(BoardCardType.SmallBusiness)

    data class MediumBusiness(
        val title: String,
        val description: String,
        val price: Long,
        val profit: Long,
    ) : BoardCard(BoardCardType.MediumBusiness)

    data class BigBusiness(
        val title: String,
        val description: String,
        val price: Long,
        val profit: Long,
    ) : BoardCard(BoardCardType.BigBusiness)
}

@Composable
fun BoardCardType.color(): Color {
    return when (this) {
        BoardCardType.Chance -> AppTheme.colors.chance
        BoardCardType.SmallBusiness -> AppTheme.colors.smallBusiness
        BoardCardType.MediumBusiness -> AppTheme.colors.business
        BoardCardType.BigBusiness -> AppTheme.colors.bigBusiness
        BoardCardType.Expenses -> AppTheme.colors.expenses
        BoardCardType.EventStore -> AppTheme.colors.store
        BoardCardType.Shopping -> AppTheme.colors.shopping
        BoardCardType.Deputy -> AppTheme.colors.deputy
    }
}