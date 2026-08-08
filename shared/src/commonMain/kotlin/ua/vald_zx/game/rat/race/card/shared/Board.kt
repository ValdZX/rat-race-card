package ua.vald_zx.game.rat.race.card.shared

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BoardId(
    val id: String,
    val name: String,
    val createDateTime: LocalDateTime,
    val activePlayerCount: Int = 0,
    val inactivePlayerCount: Int = 0,
    val deletableAfterEpochMs: Long? = null,
    val canDelete: Boolean = false,
)

@Serializable
data class Board(
    @SerialName("_id")
    val id: String,
    val name: String,
    val loanLimit: Long,
    val businessLimit: Long,
    val createDateTime: LocalDateTime,
    val cards: Map<BoardCardType, List<Int>>,
    val canTakeCard: List<BoardCardType> = emptyList(),
    val takenCard: CardLink? = null,
    val activeCardDefinitionId: String? = null,
    val pendingInteractions: List<PendingInteraction> = emptyList(),
    val sharesCount: Long? = null,
    val discard: Map<BoardCardType, List<Int>> = emptyMap(),
    val playerIds: Set<String> = emptySet(),
    val activePlayerId: String = "",
    val moveCount: Int = 0,
    val canRoll: Boolean = true,
    val dice: Int = 6,
    val diceRolling: Boolean = false,
    val processedPlayerIds: Set<String> = emptySet(),
    val auction: Auction? = null,
    val bidList: List<Bid> = emptyList(),
    val allInactiveSinceEpochMs: Long? = null,
    val outerCircleConditions: OuterCircleConditions = OuterCircleConditions(),
    val victoryConditions: VictoryConditions = VictoryConditions(),
    val transportMovementBonusEnabled: Boolean = true,
    val inflation: InflationSettings = InflationSettings(),
    val economy: EconomyIndex = EconomyIndex(),
    val economyLapPlayerIds: Set<String> = emptySet(),
    val winnerId: String? = null,
    val dreams: List<Dream> = ratRaceDreams,
    val purchasedDreamIds: Set<String> = emptySet(),
    val generation: BoardGeneration = BoardGeneration(),
    val generatedCards: Map<BoardCardType, Map<Int, BoardCard>> = emptyMap(),
    val generatedProfessions: List<ProfessionCard> = emptyList(),
    val generatedPlaces: Map<BoardLayer, List<String>> = emptyMap(),
    val trackDefinitions: Map<BoardLayer, TrackDefinition> = emptyMap(),
    val tracks: List<TrackDefinition> = emptyList(),
    val transitions: List<TrackTransition> = emptyList(),
    val objectives: List<ObjectiveDefinition> = emptyList(),
    val generatedTexts: Map<String, GeneratedText> = emptyMap(),
    val generationProgress: BoardGenerationProgress = BoardGenerationProgress(),
    val generatedBalance: GeneratedBalance? = null,
    @EncodeDefault
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    @EncodeDefault
    val rulesVersion: Int = CURRENT_RULES_VERSION,
    @EncodeDefault
    @Serializable(with = ContentPackVersionsSerializer::class)
    val contentPackVersions: Map<FeatureId, Int> = emptyMap(),
    @EncodeDefault
    val revision: Long = 0,
    val processedCommandIds: List<String> = emptyList(),
)

const val CURRENT_RULES_VERSION = 1

val Board.currency: String
    get() = generatedBalance?.currency?.takeIf { it.isNotBlank() } ?: DEFAULT_CURRENCY

fun Board.cardOrNull(link: CardLink, locale: String = DEFAULT_LOCALE): BoardCard? {
    val card = generatedCards[link.type]?.get(link.id) ?: return null
    val text = textsFor(locale).cards[link.type]?.get(link.id) ?: return card
    return card.withText(text)
}

fun Board.professionsFor(gender: Gender, locale: String = DEFAULT_LOCALE): List<ProfessionCard> {
    val texts = textsFor(locale)
    return generatedProfessions
        .filter { it.gender == gender }
        .map { profession ->
            profession.copy(
                name = texts.professions[profession.id] ?: profession.name,
                description = texts.professionDescriptions[profession.id] ?: profession.description,
            )
        }
}

fun Board.shareName(id: String, locale: String = DEFAULT_LOCALE): String {
    val share = generatedBalance?.shares?.firstOrNull { it.id == id }
    return share?.names?.get(locale)
        ?: share?.names?.get(locale.take(2))
        ?: share?.names?.get(DEFAULT_LOCALE)
        ?: share?.names?.values?.firstOrNull()
        ?: shareTicker(id)
}

fun Board.shareTicker(id: String): String = generatedBalance?.shares
    ?.firstOrNull { it.id == id }
    ?.ticker
    ?: id.replace(SharesType.ShchHP, "ЩГП")

internal fun Board.textsFor(locale: String): GeneratedText =
    generatedTexts[locale]
        ?: generatedTexts[locale.take(2)]
        ?: generatedTexts[DEFAULT_LOCALE]
        ?: GeneratedText()

fun Board.placesOf(layer: BoardLayer): List<PlaceType> {
    val dynamic = tracks.firstOrNull { it.id == layer.trackId }
    if (dynamic != null) return dynamic.cells.map(CellInstance::toPlaceType)
    val track = trackDefinitions[layer]
    if (track != null && track.cells.size == layer.places.size) return track.cells.map(CellInstance::toPlaceType)
    val codes = generatedPlaces[layer] ?: return layer.places
    val decoded = codes.mapNotNull(::placeTypeOfCode)
    return if (decoded.size == layer.places.size) decoded else layer.places
}

fun Board.placesAt(level: Int): List<PlaceType> = placesOf(level.toLayer())

fun Board.placesOf(trackId: TrackId): List<PlaceType> = track(trackId).cells.map(CellInstance::toPlaceType)

fun Board.placesOf(location: PlayerLocation): List<PlaceType> = placesOf(location.trackId)

fun Board.cellsOf(layer: BoardLayer): List<CellInstance> {
    val dynamic = tracks.firstOrNull { it.id == layer.trackId }
    if (dynamic != null) return dynamic.cells
    val track = trackDefinitions[layer]
    if (track != null && track.cells.size == layer.places.size) return track.cells
    return placesOf(layer).mapIndexed { index, place ->
        place.toCellInstance("${layer.name.lowercase()}-$index")
    }
}

fun Board.cellsAt(level: Int): List<CellInstance> = cellsOf(level.toLayer())

fun Board.cellsOf(trackId: TrackId): List<CellInstance> = track(trackId).cells

fun Board.cellsOf(location: PlayerLocation): List<CellInstance> = cellsOf(location.trackId)

fun Board.resolvedTracks(): List<TrackDefinition> {
    val definitions = if (tracks.isNotEmpty()) tracks.sortedBy(TrackDefinition::order) else BoardLayer.entries.map { layer ->
        trackDefinitions[layer]?.takeIf { it.cells.size == layer.places.size }
            ?: layer.defaultTrackDefinition().copy(
                cells = placesOf(layer).mapIndexed { index, place ->
                    place.toCellInstance("${layer.name.lowercase()}-$index")
                },
            )
    }
    val features = standardFeatureRegistry()
    val activeTypes = features.runtime(resolvedContentPackVersions()).cellTypes
    return definitions.map { track ->
        track.copy(cells = track.cells.filter { it.type !in features.knownCellTypes || it.type in activeTypes })
    }
}

fun Board.track(trackId: TrackId): TrackDefinition = resolvedTracks().firstOrNull { it.id == trackId }
    ?: error("Unknown track id: ${trackId.value}")

fun Board.resolvedTransitions(): List<TrackTransition> = transitions.ifEmpty {
    listOf(
        TrackTransition(
            id = "inner-to-outer",
            from = CoreTrackIds.Inner,
            to = CoreTrackIds.Outer,
            conditions = buildList {
                add(ProgressCondition.MinimumCashFlow(outerCircleConditions.minimumCashFlow))
                add(ProgressCondition.MinimumBalance(outerCircleConditions.minimumAccountBalance))
                if (outerCircleConditions.apartmentRequired) add(ProgressCondition.RequiresApartment)
                if (outerCircleConditions.carRequired) add(ProgressCondition.RequiresCar)
            },
        ),
    ).filter { transition ->
        resolvedTracks().any { it.id == transition.from } && resolvedTracks().any { it.id == transition.to }
    }
}

fun Board.resolvedObjectives(): List<ObjectiveDefinition> = objectives.ifEmpty {
    listOf(
        ObjectiveDefinition(
            id = "outer-victory",
            trackId = CoreTrackIds.Outer,
            conditions = buildList {
                add(ProgressCondition.MinimumBalance(victoryConditions.minimumAccountBalance))
                if (victoryConditions.dreamRequired && hasFeature(StandardFeatures.Dreams)) {
                    add(ProgressCondition.RequiresSelectedDream)
                }
                if (victoryConditions.planeRequired) add(ProgressCondition.RequiresPlane)
                if (victoryConditions.estateRequired) add(ProgressCondition.RequiresEstate)
            },
        ),
    ).filter { objective -> resolvedTracks().any { it.id == objective.trackId } }
}

fun Board.availableTransition(player: Player, canRoll: Boolean): TrackTransition? {
    if (!canRoll) return null
    return resolvedTransitions().firstOrNull { transition ->
        transition.from == player.location.trackId && transition.conditions.all { it.matches(player) }
    }
}

fun Board.hasCompletedObjective(player: Player): Boolean = resolvedObjectives().any { objective ->
    objective.trackId == player.location.trackId && objective.conditions.all { it.matches(player) }
}

fun Board.validateTracks(players: List<Player>): ValidationResult {
    val definitions = resolvedTracks()
    val ids = definitions.map(TrackDefinition::id)
    val errors = buildList {
        if (ids.distinct().size != ids.size) add("Track ids must be unique")
        definitions.forEach { track ->
            if (track.cells.isEmpty()) add("${track.id}: cells must not be empty")
            if (track.visual.horizontalCells <= 0 || track.visual.verticalCells <= 0) {
                add("${track.id}: visual dimensions must be positive")
            }
        }
        resolvedTransitions().forEach { transition ->
            if (transition.from !in ids) add("${transition.id}: source track is missing")
            val target = definitions.firstOrNull { it.id == transition.to }
            if (target == null) {
                add("${transition.id}: target track is missing")
            } else if (transition.entryCellIndex !in target.cells.indices) {
                add("${transition.id}: entry cell is outside target track")
            }
        }
        resolvedObjectives().forEach { objective ->
            if (objective.trackId !in ids) add("${objective.id}: objective track is missing")
        }
        players.forEach { player ->
            val track = definitions.firstOrNull { it.id == player.location.trackId }
            if (track == null) {
                add("${player.id}: unknown track ${player.location.trackId}")
            } else if (player.location.position !in track.cells.indices) {
                add("${player.id}: position is outside track ${track.id}")
            }
        }
    }
    return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
}

private fun ProgressCondition.matches(player: Player): Boolean = when (this) {
    is ProgressCondition.MinimumCashFlow -> player.cashFlow() >= amount
    is ProgressCondition.MinimumBalance -> player.balance() >= amount
    ProgressCondition.RequiresApartment -> player.apartment > 0
    ProgressCondition.RequiresCar -> player.cars > 0
    ProgressCondition.RequiresPlane -> player.flight > 0
    ProgressCondition.RequiresEstate -> player.cottage > 0
    ProgressCondition.RequiresSelectedDream -> player.selectedDreamId != null &&
            player.selectedDreamId in player.purchasedDreamIds
}

@Serializable
data class OuterCircleConditions(
    val minimumCashFlow: Long = 50_000,
    val apartmentRequired: Boolean = true,
    val carRequired: Boolean = true,
    val minimumAccountBalance: Long = 200_000,
)

@Serializable
data class VictoryConditions(
    val dreamRequired: Boolean = true,
    val planeRequired: Boolean = true,
    val estateRequired: Boolean = true,
    val minimumAccountBalance: Long = 10_000_000,
)

fun Board.toBoardId(): BoardId {
    return BoardId(
        id = id,
        createDateTime = createDateTime,
        name = name,
    )
}

fun moveTo(position: Int, cellCount: Int, toMove: Int): Int {
    require(cellCount > 0) { "Track must contain at least one cell" }
    return ((position + toMove) % cellCount + cellCount) % cellCount
}
