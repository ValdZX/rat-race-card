package ua.vald_zx.game.rat.race.server

import ua.vald_zx.game.rat.race.card.shared.BoardLayer
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.shared.placeTypeOfCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BoardLayoutGeneratorTest {

    private fun world(seed: Long = 42) = BoardGeneration(
        enabled = true,
        theme = "космічна колонія",
        seed = seed,
    )

    private fun places(seed: Long = 42, layer: BoardLayer = BoardLayer.INNER): List<PlaceType> =
        BoardGenerator(world(seed), testBalance()).generatePlaces()
            .getValue(layer)
            .map { code -> assertNotNull(placeTypeOfCode(code), "невідомий код клітинки $code") }

    @Test
    fun everyLayerKeepsItsLength() {
        val generated = BoardGenerator(world(), testBalance()).generatePlaces()
        BoardLayer.entries.forEach { layer ->
            assertEquals(layer.places.size, generated.getValue(layer).size, "довжина кола $layer змінилась")
        }
    }

    @Test
    fun theSetOfCellsIsUntouched() {
        BoardLayer.entries.forEach { layer ->
            val generated = places(layer = layer)
            assertEquals(
                layer.places.groupingBy { it }.eachCount(),
                generated.groupingBy { it }.eachCount(),
                "склад клітинок кола $layer змінився",
            )
        }
    }

    @Test
    fun bigCellsAndStartKeepTheirSlots() {
        BoardLayer.entries.forEach { layer ->
            val generated = places(layer = layer)
            layer.places.forEachIndexed { index, place ->
                if (place.isBig || place == PlaceType.Start) {
                    assertEquals(place, generated[index], "клітинка $place на $index кола $layer поїхала")
                }
            }
        }
    }

    @Test
    fun desireSlotsStayDesireSlots() {
        val layer = BoardLayer.OUTER
        val generated = places(layer = layer)
        layer.places.forEachIndexed { index, place ->
            assertEquals(
                place is PlaceType.Desire,
                generated[index] is PlaceType.Desire,
                "мрія на позиції $index перестала бути мрією",
            )
        }
    }

    @Test
    fun theOrderActuallyChanges() {
        BoardLayer.entries.forEach { layer ->
            assertTrue(places(layer = layer) != layer.places, "коло $layer не перемішалось")
        }
    }

    @Test
    fun theSameSeedRebuildsTheSameBoard() {
        assertEquals(places(seed = 7), places(seed = 7))
        assertTrue(places(seed = 7) != places(seed = 8), "seed не впливає на розкладку")
    }

    @Test
    fun switchingGenerationOffKeepsTheStaticBoard() {
        val off = BoardGenerator(world().copy(enabled = false), testBalance()).generatePlaces()
        assertTrue(off.isEmpty(), "вимкнена генерація все одно перемішала дошку")
    }
}
