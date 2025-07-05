package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.ui.graphics.Color
import ua.vald_zx.game.rat.race.card.shared.BoardCardType

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

val BoardCardType.color: Color
    get() {
        return when (this) {
            BoardCardType.Chance -> cnanceColor
            BoardCardType.SmallBusiness -> smallBusinessColor
            BoardCardType.MediumBusiness -> businessColor
            BoardCardType.BigBusiness -> bigBusinessColor
            BoardCardType.Expenses -> expensesColor
            BoardCardType.EventStore -> storeColor
            BoardCardType.Shopping -> shoppingColor
            BoardCardType.Deputy -> deputyColor
        }
    }