package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class FeatureId(val value: String) {
    init {
        require(value.matches(Regex("[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+"))) { "Invalid feature id: $value" }
    }

    override fun toString(): String = value
}

object ContentPackVersionsSerializer : KSerializer<Map<FeatureId, Int>> {
    private val delegate = MapSerializer(String.serializer(), Int.serializer())

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: Map<FeatureId, Int>) {
        encoder.encodeSerializableValue(delegate, value.mapKeys { it.key.value })
    }

    override fun deserialize(decoder: Decoder): Map<FeatureId, Int> {
        return decoder.decodeSerializableValue(delegate).mapKeys { FeatureId(it.key) }
    }
}

@Serializable
data class FeatureDependency(
    val featureId: FeatureId,
    val minimumVersion: Int,
)

@Serializable
data class DeckDefinition(
    val id: DeckId,
    val type: BoardCardType,
    val order: Int,
)

@Serializable
data class FeatureDefinitions(
    val cellTypes: Set<CellTypeId> = emptySet(),
    val effectTypes: Set<EffectTypeId> = emptySet(),
    val decks: List<DeckDefinition> = emptyList(),
)

@Serializable
data class FeatureMigrationDescriptor(
    val id: String,
    val fromVersion: Int,
    val toVersion: Int,
)

@Serializable
data class FeatureManifest(
    val featureId: FeatureId,
    val version: Int,
    val dependencies: Set<FeatureDependency> = emptySet(),
    val definitions: FeatureDefinitions = FeatureDefinitions(),
    val migrations: List<FeatureMigrationDescriptor> = emptyList(),
)

class FeatureMigration(
    val descriptor: FeatureMigrationDescriptor,
    private val transform: (Board) -> Board,
) {
    fun migrate(board: Board): Board = transform(board)
}

data class FeaturePackage(
    val manifest: FeatureManifest,
    val cellRules: List<CellRule> = emptyList(),
    val effectHandlers: List<EffectHandler> = emptyList(),
    val migrations: List<FeatureMigration> = emptyList(),
)

data class FeatureRuntime(
    val manifests: List<FeatureManifest>,
    val cellRules: CellRuleRegistry,
    val effectHandlers: EffectHandlerRegistry,
    val decks: List<DeckDefinition>,
) {
    val cellTypes: Set<CellTypeId> = manifests.flatMap { it.definitions.cellTypes }.toSet()
    val effectTypes: Set<EffectTypeId> = manifests.flatMap { it.definitions.effectTypes }.toSet()
}

class FeatureRegistry(packages: Iterable<FeaturePackage>) {
    private val packagesById: Map<FeatureId, FeaturePackage>

    init {
        val packageList = packages.toList()
        packagesById = packageList.associateBy { it.manifest.featureId }
        require(packagesById.size == packageList.size) { "Duplicate feature packages" }
        require(packageList.all { it.manifest.version > 0 }) { "Feature versions must be positive" }
        require(packageList.all { feature ->
            feature.manifest.migrations == feature.migrations.map(FeatureMigration::descriptor)
        }) { "Manifest migration descriptors must match registered migrations" }
    }

    val currentVersions: Map<FeatureId, Int>
        get() = packagesById.mapValues { it.value.manifest.version }

    val knownCellTypes: Set<CellTypeId>
        get() = packagesById.values.flatMap { it.manifest.definitions.cellTypes }.toSet()

    fun runtime(versions: Map<FeatureId, Int>): FeatureRuntime {
        val selected = versions.entries.map { (id, version) ->
            val feature = packagesById[id] ?: error("Feature is not installed: $id")
            require(feature.manifest.version == version) {
                "Feature $id version $version is not installed; available ${feature.manifest.version}"
            }
            feature
        }
        selected.forEach { feature ->
            feature.manifest.dependencies.forEach { dependency ->
                val selectedVersion = versions[dependency.featureId]
                require(selectedVersion != null && selectedVersion >= dependency.minimumVersion) {
                    "Feature ${feature.manifest.featureId} requires ${dependency.featureId} >= ${dependency.minimumVersion}"
                }
            }
        }
        val manifests = selected.map(FeaturePackage::manifest).sortedBy { it.featureId.value }
        return FeatureRuntime(
            manifests = manifests,
            cellRules = CellRuleRegistry(selected.flatMap(FeaturePackage::cellRules)),
            effectHandlers = EffectHandlerRegistry(selected.flatMap(FeaturePackage::effectHandlers)),
            decks = manifests.flatMap { it.definitions.decks }.sortedBy(DeckDefinition::order),
        )
    }

    fun validate(board: Board): ValidationResult {
        val runtime = runCatching { runtime(board.resolvedContentPackVersions()) }
            .getOrElse { return ValidationResult.Invalid(listOf(it.message ?: "Invalid feature selection")) }
        val cells = board.resolvedTracks().flatMap(TrackDefinition::cells)
        val errors = buildList {
            when (val validation = runtime.cellRules.validate(cells)) {
                ValidationResult.Valid -> Unit
                is ValidationResult.Invalid -> addAll(validation.errors)
            }
            val missingCellHandlers = runtime.cellTypes - runtime.cellRules.registeredTypes
            if (missingCellHandlers.isNotEmpty()) {
                add("Missing cell handlers: ${missingCellHandlers.joinToString()}")
            }
            val missingEffectHandlers = runtime.effectTypes - runtime.effectHandlers.registeredTypes
            if (missingEffectHandlers.isNotEmpty()) {
                add("Missing effect handlers: ${missingEffectHandlers.joinToString()}")
            }
            val pendingEffects = board.pendingInteractions.flatMap { it.branches.values.flatten() }
            when (val validation = runtime.effectHandlers.validate(pendingEffects)) {
                ValidationResult.Valid -> Unit
                is ValidationResult.Invalid -> addAll(validation.errors)
            }
        }
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors.distinct())
    }

    fun migrate(board: Board, targetVersions: Map<FeatureId, Int>): Board {
        if (board.contentPackVersions.isEmpty()) {
            runtime(targetVersions)
            return board.copy(contentPackVersions = targetVersions)
        }
        var migrated = board
        targetVersions.forEach { (featureId, targetVersion) ->
            val feature = packagesById[featureId] ?: error("Feature is not installed: $featureId")
            var currentVersion = migrated.contentPackVersions[featureId] ?: 0
            require(currentVersion <= targetVersion) { "Feature downgrades are not supported: $featureId" }
            while (currentVersion < targetVersion) {
                val migration = feature.migrations.firstOrNull {
                    it.descriptor.fromVersion == currentVersion && it.descriptor.toVersion == currentVersion + 1
                } ?: error("Missing feature migration: $featureId $currentVersion -> ${currentVersion + 1}")
                migrated = migration.migrate(migrated)
                currentVersion++
            }
        }
        migrated = migrated.copy(contentPackVersions = targetVersions)
        runtime(targetVersions)
        return migrated
    }
}

object StandardFeatures {
    val Core = FeatureId("game.core")
    val Family = FeatureId("game.family")
    val Investments = FeatureId("game.investments")
    val Corruption = FeatureId("game.corruption")
    val Dreams = FeatureId("game.dreams")

    val all: Set<FeatureId> = setOf(Core, Family, Investments, Corruption, Dreams)
}

private fun dependency(featureId: FeatureId) = FeatureDependency(featureId, minimumVersion = 1)

private fun deck(type: BoardCardType, order: Int) = DeckDefinition(type.deckId(), type, order)

private fun coreFeaturePackage() = FeaturePackage(
    manifest = FeatureManifest(
        featureId = StandardFeatures.Core,
        version = 1,
        definitions = FeatureDefinitions(
            cellTypes = coreCellRules().map(CellRule::type).toSet(),
            effectTypes = standardEffectHandlers().map(EffectHandler::type).toSet(),
            decks = listOf(
                deck(BoardCardType.Chance, 0),
                deck(BoardCardType.BigBusiness, 1),
                deck(BoardCardType.MediumBusiness, 2),
                deck(BoardCardType.SmallBusiness, 3),
                deck(BoardCardType.Expenses, 4),
                deck(BoardCardType.Shopping, 7),
            ),
        ),
    ),
    cellRules = coreCellRules(),
    effectHandlers = standardEffectHandlers(),
)

private fun familyFeaturePackage() = FeaturePackage(
    manifest = FeatureManifest(
        featureId = StandardFeatures.Family,
        version = 1,
        dependencies = setOf(dependency(StandardFeatures.Core)),
        definitions = FeatureDefinitions(cellTypes = familyCellRules().map(CellRule::type).toSet()),
    ),
    cellRules = familyCellRules(),
)

private fun investmentsFeaturePackage() = FeaturePackage(
    manifest = FeatureManifest(
        featureId = StandardFeatures.Investments,
        version = 1,
        dependencies = setOf(dependency(StandardFeatures.Core)),
        definitions = FeatureDefinitions(
            cellTypes = investmentCellRules().map(CellRule::type).toSet(),
            decks = listOf(deck(BoardCardType.EventStore, 6)),
        ),
    ),
    cellRules = investmentCellRules(),
)

private fun corruptionFeaturePackage() = FeaturePackage(
    manifest = FeatureManifest(
        featureId = StandardFeatures.Corruption,
        version = 1,
        dependencies = setOf(
            dependency(StandardFeatures.Core),
            dependency(StandardFeatures.Investments),
        ),
        definitions = FeatureDefinitions(
            cellTypes = corruptionCellRules().map(CellRule::type).toSet(),
            decks = listOf(deck(BoardCardType.Deputy, 5)),
        ),
    ),
    cellRules = corruptionCellRules(),
)

private fun dreamsFeaturePackage() = FeaturePackage(
    manifest = FeatureManifest(
        featureId = StandardFeatures.Dreams,
        version = 1,
        dependencies = setOf(dependency(StandardFeatures.Core)),
        definitions = FeatureDefinitions(cellTypes = dreamCellRules().map(CellRule::type).toSet()),
    ),
    cellRules = dreamCellRules(),
)

private val standardFeatureRegistryInstance by lazy {
    FeatureRegistry(
        listOf(
            coreFeaturePackage(),
            familyFeaturePackage(),
            investmentsFeaturePackage(),
            corruptionFeaturePackage(),
            dreamsFeaturePackage(),
        ),
    )
}

fun standardFeatureRegistry(): FeatureRegistry = standardFeatureRegistryInstance

fun standardContentPackVersions(): Map<FeatureId, Int> = standardFeatureRegistry().currentVersions

fun coreContentPackVersions(): Map<FeatureId, Int> = mapOf(StandardFeatures.Core to 1)

fun Board.resolvedContentPackVersions(): Map<FeatureId, Int> =
    contentPackVersions.ifEmpty(::standardContentPackVersions)

fun Board.hasFeature(featureId: FeatureId): Boolean = featureId in resolvedContentPackVersions()

fun Board.activeDeckTypes(): List<BoardCardType> = standardFeatureRegistry()
    .runtime(resolvedContentPackVersions())
    .decks
    .map(DeckDefinition::type)

fun Board.validateFeatures(): ValidationResult = standardFeatureRegistry().validate(this)

fun Board.requireValidFeatures(): Board {
    val result = validateFeatures()
    require(result is ValidationResult.Valid) {
        (result as ValidationResult.Invalid).errors.joinToString(prefix = "Invalid feature runtime: ")
    }
    return this
}
