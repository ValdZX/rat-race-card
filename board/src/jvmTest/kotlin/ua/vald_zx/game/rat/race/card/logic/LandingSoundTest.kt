package ua.vald_zx.game.rat.race.card.logic

import ua.vald_zx.game.rat.race.card.GameSound
import ua.vald_zx.game.rat.race.card.shared.BoardLayer
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.shared.PlayerLocation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LandingSoundTest {

    @Test
    fun everyPlaceOnBothLayersHasASound() {
        BoardLayer.entries.forEach { layer ->
            layer.places.forEachIndexed { index, place ->
                val sound = landingSound(PlayerLocation(position = index, level = layer.level))
                assertTrue(
                    sound != GameSound.TokenStep,
                    "клітинка $index (${place}) на $layer лишилась без звуку",
                )
            }
        }
    }

    @Test
    fun familiesKeepTheirOwnSound() {
        assertEquals(GameSound.PlaceService, PlaceType.Salary.sound())
        assertEquals(GameSound.PlaceCard, PlaceType.Chance.sound())
        assertEquals(GameSound.PlaceLoss, PlaceType.Bankruptcy.sound())
        assertEquals(GameSound.PlaceAsset, PlaceType.BigBusiness.sound())
        assertEquals(GameSound.PlaceLife, PlaceType.Love.sound())
    }

    @Test
    fun positionOutsideTheLayerFallsBackToTheQuietTick() {
        val beyond = BoardLayer.INNER.places.size + 5
        assertEquals(
            GameSound.TokenStep,
            landingSound(PlayerLocation(position = beyond, level = BoardLayer.INNER.level)),
        )
    }
}
