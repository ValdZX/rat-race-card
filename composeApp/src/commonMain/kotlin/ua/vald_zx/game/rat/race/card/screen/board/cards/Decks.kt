package ua.vald_zx.game.rat.race.card.screen.board.cards

import ua.vald_zx.game.rat.race.card.shared.BoardCardType

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