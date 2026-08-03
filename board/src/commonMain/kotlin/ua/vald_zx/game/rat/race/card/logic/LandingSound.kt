package ua.vald_zx.game.rat.race.card.logic

import ua.vald_zx.game.rat.race.card.GameSound
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.shared.PlayerLocation
import ua.vald_zx.game.rat.race.card.shared.toLayer

internal fun landingSound(location: PlayerLocation): GameSound {
    val places = location.level.toLayer().places
    val place = places.getOrNull(location.position) ?: return GameSound.TokenStep
    return place.sound()
}

internal fun PlaceType.sound(): GameSound = when (this) {
    PlaceType.Salary, PlaceType.Start, PlaceType.Rest, PlaceType.TaxInspection -> GameSound.PlaceService
    PlaceType.Chance, PlaceType.Store, PlaceType.Shopping, PlaceType.Deputy -> GameSound.PlaceCard
    PlaceType.Expenses, PlaceType.Bankruptcy, PlaceType.Divorce -> GameSound.PlaceLoss
    PlaceType.Business, PlaceType.BigBusiness -> GameSound.PlaceAsset
    PlaceType.Child, PlaceType.Love, PlaceType.Resignation -> GameSound.PlaceLife
    is PlaceType.Desire -> GameSound.PlaceLife
}
