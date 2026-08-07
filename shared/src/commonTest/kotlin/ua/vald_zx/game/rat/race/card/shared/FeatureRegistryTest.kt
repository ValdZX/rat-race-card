package ua.vald_zx.game.rat.race.card.shared

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class FeatureRegistryTest {
    @Test
    fun standardPackagesExposeVersionedManifestsAndDependencies() {
        val registry = standardFeatureRegistry()
        val runtime = registry.runtime(standardContentPackVersions())

        assertEquals(StandardFeatures.all, runtime.manifests.map { it.featureId }.toSet())
        assertTrue(runtime.manifests.all { it.version == 1 })
        assertTrue(runtime.manifests.all { manifest ->
            manifest.dependencies.all { it.featureId in registry.currentVersions }
        })
    }

    @Test
    fun coreOnlyBoardContainsOnlyCoreCellsDecksAndObjectives() {
        val board = board(contentPackVersions = coreContentPackVersions())

        assertEquals(
            listOf(
                BoardCardType.Chance,
                BoardCardType.BigBusiness,
                BoardCardType.MediumBusiness,
                BoardCardType.SmallBusiness,
                BoardCardType.Expenses,
                BoardCardType.Shopping,
            ),
            board.activeDeckTypes(),
        )
        assertTrue(board.resolvedTracks().flatMap { it.cells }.all { it.type.value.startsWith("core.") })
        assertFalse(board.resolvedObjectives().flatMap { it.conditions }.contains(ProgressCondition.RequiresSelectedDream))
        assertEquals(ValidationResult.Valid, board.validateFeatures())
    }

    @Test
    fun snapshotPersistsTheExactContentPackVersions() {
        val board = board(contentPackVersions = coreContentPackVersions())

        val restored = Json.decodeFromString<Board>(Json.encodeToString(board))

        assertEquals(coreContentPackVersions(), restored.contentPackVersions)
        assertEquals(coreContentPackVersions(), restored.resolvedContentPackVersions())
    }

    @Test
    fun missingHandlersAreRejectedBeforeOpeningBoard() {
        val registry = FeatureRegistry(
            listOf(
                FeaturePackage(
                    FeatureManifest(
                        featureId = StandardFeatures.Core,
                        version = 1,
                        definitions = FeatureDefinitions(cellTypes = setOf(CoreCellTypes.Start)),
                    ),
                ),
            ),
        )

        val invalid = assertIs<ValidationResult.Invalid>(registry.validate(board(coreContentPackVersions())))

        assertTrue(invalid.errors.any { it.contains("Missing cell handlers") })
    }

    @Test
    fun dependencyClosureIsRequired() {
        assertFailsWith<IllegalArgumentException> {
            standardFeatureRegistry().runtime(mapOf(StandardFeatures.Corruption to 1))
        }
    }

    @Test
    fun activeBoardsResolveTheirPinnedFeatureVersionInsteadOfLatest() {
        val featureId = FeatureId("test.versioned")
        val registry = FeatureRegistry(
            listOf(
                FeaturePackage(FeatureManifest(featureId, version = 1)),
                FeaturePackage(FeatureManifest(featureId, version = 2)),
            ),
        )

        assertEquals(2, registry.currentVersions[featureId])
        assertEquals(1, registry.runtime(mapOf(featureId to 1)).manifests.single().version)
        assertEquals(2, registry.runtime(mapOf(featureId to 2)).manifests.single().version)
    }

    @Test
    fun migrationsAreSequentialPureTransforms() {
        val descriptor = FeatureMigrationDescriptor("core-1-to-2", 1, 2)
        val registry = FeatureRegistry(
            listOf(
                FeaturePackage(
                    manifest = FeatureManifest(
                        featureId = StandardFeatures.Core,
                        version = 2,
                        migrations = listOf(descriptor),
                    ),
                    migrations = listOf(FeatureMigration(descriptor) { it.copy(name = "Migrated") }),
                ),
            ),
        )
        val original = board(mapOf(StandardFeatures.Core to 1))

        val migrated = registry.migrate(original, mapOf(StandardFeatures.Core to 2))

        assertEquals("Board", original.name)
        assertEquals("Migrated", migrated.name)
        assertEquals(mapOf(StandardFeatures.Core to 2), migrated.contentPackVersions)
    }

    private fun board(contentPackVersions: Map<FeatureId, Int>) = Board(
        id = "board",
        name = "Board",
        loanLimit = 10_000,
        businessLimit = 3,
        createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
        cards = emptyMap(),
        contentPackVersions = contentPackVersions,
    )
}
