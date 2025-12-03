package ua.vald_zx.game.rat.race.card.screen.board.visualize

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.PayerType
import ua.vald_zx.game.rat.race.card.theme.AppTheme


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

fun PayerType.getLocal(): String {
    return when (this) {
        PayerType.ALL -> "Обов’язково для всіх"
        PayerType.FREE_W_OR_MARRIED_M -> "Платять тільки незаміжні жінки й одружені чоловіки"
        PayerType.AUTO_OWNER -> "Платять тільки власники авто"
        PayerType.MEN -> "Обов’язково для гравців-чоловіків"
        PayerType.PARENT -> "Платять гравці, які мають дитину"
        PayerType.MARRIED_M -> "Платять тільки одружені чоловіки"
        PayerType.APARTMENT_OWNER -> "Платять тільки власники квартир"
        PayerType.APARTMENT_OR_HOUSE_OWNER -> "Платять тільки власники квартир або будинків"
        PayerType.ANIMAL_OWNER -> "Обов’язково для всіх, хто має домашніх тварин"
    }
}