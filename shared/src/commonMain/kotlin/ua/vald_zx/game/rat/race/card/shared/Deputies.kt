package ua.vald_zx.game.rat.race.card.shared

const val DEPUTY_CARD_PRICE = 50_000L

val corruptDeputyIds = setOf(
    39, 40, 41, 42, 43, 44, 45, 46, 47, 48,
    49, 50, 51, 52, 53, 54, 55, 56, 57, 58,
    59, 60, 61, 62, 63, 64, 65, 66, 67, 68,
    69, 70, 71, 72, 73, 74, 75,
)

fun Board.deputyIsCorrupt(cardId: Int): Boolean =
    (cardOrNull(CardLink(BoardCardType.Deputy, cardId)) as? BoardCard.Deputy)?.corrupt
        ?: (cardId in corruptDeputyIds)
