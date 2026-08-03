package ua.vald_zx.game.rat.race.card.screen.board.cards

import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.CardLink
import ua.vald_zx.game.rat.race.card.shared.cardOrNull

val decks = BoardCardType.entries.associate { type ->
    when (type) {
        BoardCardType.Chance -> type to chanceCards
        BoardCardType.SmallBusiness -> type to smallBusinessCards
        BoardCardType.MediumBusiness -> type to mediumBusinessCards
        BoardCardType.BigBusiness -> type to bigBusinessCards
        BoardCardType.Expenses -> type to expensesCards
        BoardCardType.EventStore -> type to eventStoreCards
        BoardCardType.Shopping -> type to shoppingCards
        BoardCardType.Deputy -> type to deputyCards
    }
}

fun Board.cardOf(link: CardLink): BoardCard? = cardOrNull(link) ?: decks[link.type]?.get(link.id)
