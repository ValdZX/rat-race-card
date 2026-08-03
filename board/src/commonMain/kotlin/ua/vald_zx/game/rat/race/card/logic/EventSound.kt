package ua.vald_zx.game.rat.race.card.logic

import ua.vald_zx.game.rat.race.card.GameSound
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.Player

internal fun moveSound(old: Player?, changed: Player): GameSound? {
    if (old == null || old.location == changed.location) return null
    return GameSound.TokenStep
}

internal fun cardTakenSound(previous: Board, next: Board): GameSound? {
    return GameSound.PlaceCard.takeIf { previous.takenCard == null && next.takenCard != null }
}
