package ua.vald_zx.game.rat.race.card.screen.board.cards

import ua.vald_zx.game.rat.race.card.shared.BoardCardType

val BoardCardType.title: String
get() = when (this) {
    BoardCardType.SmallBusiness -> "У тебе є можливість створити малий бізнес:"
    BoardCardType.MediumBusiness -> "У тебе є можливість створити середній бізнес:"
    BoardCardType.BigBusiness -> "У тебе є можливість створити великий бізнес:"
    BoardCardType.Shopping -> "У тебе є можливість придбати:"
    else -> ""
}