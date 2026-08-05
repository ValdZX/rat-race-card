package ua.vald_zx.game.rat.race.server

import ua.vald_zx.game.rat.race.server.generation.BoardGenerator

import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import ua.vald_zx.game.rat.race.card.shared.Gender
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProfessionGeneratorTest {

    private fun world(seed: Long = 42) = BoardGeneration(
        enabled = true,
        theme = "космічна колонія",
        seed = seed,
    )

    private fun professions(seed: Long = 42) = BoardGenerator(world(seed), testBalance()).generateProfessions()

    @Test
    fun bothGendersGetTheSameChoice() {
        val byGender = professions().groupBy { it.gender }
        assertEquals(Gender.entries.toSet(), byGender.keys)
        assertEquals(1, byGender.values.map { it.size }.toSet().size, "у статей різна кількість професій")
    }

    @Test
    fun idsAreUniqueAcrossGenders() {
        val generated = professions()
        assertEquals(generated.size, generated.map { it.id }.toSet().size, "id професій повторюються")
    }

    @Test
    fun nobodyStartsInTheRed() {
        professions().forEach { card ->
            val expenses = card.rent + card.food + card.cloth + card.transport + card.phone
            assertTrue(
                card.salary > expenses,
                "професія ${card.name} витрачає $expenses при зарплаті ${card.salary}",
            )
        }
    }

    @Test
    fun theSameSeedRebuildsTheSameProfessions() {
        assertEquals(professions(seed = 7), professions(seed = 7))
        assertTrue(professions(seed = 7) != professions(seed = 8), "seed не впливає на професії")
    }

    @Test
    fun switchingGenerationOffKeepsTheStaticProfessions() {
        assertTrue(BoardGenerator(world().copy(enabled = false), testBalance()).generateProfessions().isEmpty())
    }
}
