package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class CellTypeId(val value: String) {
    init {
        require(value.matches(TYPE_ID_PATTERN)) { "Invalid cell type: $value" }
    }

    override fun toString(): String = value

    private companion object {
        val TYPE_ID_PATTERN = Regex("[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+")
    }
}

@JvmInline
@Serializable
value class TrackId(val value: String)

@Serializable
data class CellInstance(
    val id: String,
    val type: CellTypeId,
    val parameters: JsonObject = JsonObject(emptyMap()),
)

@Serializable
enum class TrackTopology {
    LOOP,
    PATH,
}

@Serializable
data class TrackVisualHint(
    val horizontalCells: Int,
    val verticalCells: Int,
)

@Serializable
data class TrackDefinition(
    val id: TrackId,
    val order: Int,
    val topology: TrackTopology = TrackTopology.LOOP,
    val cells: List<CellInstance>,
    val visual: TrackVisualHint,
)

@Serializable
data class BoardDefinition(
    val id: String,
    val rulesVersion: Int,
    val tracks: List<TrackDefinition>,
)

object CoreCellTypes {
    val Start = CellTypeId("core.start")
    val Salary = CellTypeId("core.salary")
    val Business = CellTypeId("core.business")
    val BigBusiness = CellTypeId("core.big_business")
    val Shopping = CellTypeId("core.shopping")
    val Chance = CellTypeId("core.chance")
    val Expenses = CellTypeId("core.expenses")
    val Store = CellTypeId("core.store")
    val Bankruptcy = CellTypeId("core.bankruptcy")
    val Child = CellTypeId("family.child")
    val Love = CellTypeId("family.love")
    val Rest = CellTypeId("core.rest")
    val Divorce = CellTypeId("family.divorce")
    val Desire = CellTypeId("dreams.desire")
    val Deputy = CellTypeId("corruption.deputy")
    val TaxInspection = CellTypeId("corruption.tax_inspection")
    val Resignation = CellTypeId("core.resignation")

    val all: Set<CellTypeId> = setOf(
        Start,
        Salary,
        Business,
        BigBusiness,
        Shopping,
        Chance,
        Expenses,
        Store,
        Bankruptcy,
        Child,
        Love,
        Rest,
        Divorce,
        Desire,
        Deputy,
        TaxInspection,
        Resignation,
    )
}

fun PlaceType.toCellInstance(id: String): CellInstance = when (this) {
    PlaceType.Start -> CellInstance(id, CoreCellTypes.Start)
    PlaceType.Salary -> CellInstance(id, CoreCellTypes.Salary)
    PlaceType.Business -> CellInstance(id, CoreCellTypes.Business)
    PlaceType.BigBusiness -> CellInstance(id, CoreCellTypes.BigBusiness)
    PlaceType.Shopping -> CellInstance(id, CoreCellTypes.Shopping)
    PlaceType.Chance -> CellInstance(id, CoreCellTypes.Chance)
    PlaceType.Expenses -> CellInstance(id, CoreCellTypes.Expenses)
    PlaceType.Store -> CellInstance(id, CoreCellTypes.Store)
    PlaceType.Bankruptcy -> CellInstance(id, CoreCellTypes.Bankruptcy)
    PlaceType.Child -> CellInstance(id, CoreCellTypes.Child)
    PlaceType.Love -> CellInstance(id, CoreCellTypes.Love)
    PlaceType.Rest -> CellInstance(id, CoreCellTypes.Rest)
    PlaceType.Divorce -> CellInstance(id, CoreCellTypes.Divorce)
    is PlaceType.Desire -> CellInstance(
        id = id,
        type = CoreCellTypes.Desire,
        parameters = JsonObject(mapOf(DREAM_ID_PARAMETER to JsonPrimitive(dreamId))),
    )

    PlaceType.Deputy -> CellInstance(id, CoreCellTypes.Deputy)
    PlaceType.TaxInspection -> CellInstance(id, CoreCellTypes.TaxInspection)
    PlaceType.Resignation -> CellInstance(id, CoreCellTypes.Resignation)
    is PlaceType.Custom -> CellInstance(id, type, parameters)
}

fun CellInstance.toPlaceType(): PlaceType = when (type) {
    CoreCellTypes.Start -> PlaceType.Start
    CoreCellTypes.Salary -> PlaceType.Salary
    CoreCellTypes.Business -> PlaceType.Business
    CoreCellTypes.BigBusiness -> PlaceType.BigBusiness
    CoreCellTypes.Shopping -> PlaceType.Shopping
    CoreCellTypes.Chance -> PlaceType.Chance
    CoreCellTypes.Expenses -> PlaceType.Expenses
    CoreCellTypes.Store -> PlaceType.Store
    CoreCellTypes.Bankruptcy -> PlaceType.Bankruptcy
    CoreCellTypes.Child -> PlaceType.Child
    CoreCellTypes.Love -> PlaceType.Love
    CoreCellTypes.Rest -> PlaceType.Rest
    CoreCellTypes.Divorce -> PlaceType.Divorce
    CoreCellTypes.Desire -> PlaceType.Desire((parameters[DREAM_ID_PARAMETER] as? JsonPrimitive)?.content.orEmpty())
    CoreCellTypes.Deputy -> PlaceType.Deputy
    CoreCellTypes.TaxInspection -> PlaceType.TaxInspection
    CoreCellTypes.Resignation -> PlaceType.Resignation
    else -> PlaceType.Custom(type, parameters)
}

fun BoardLayer.defaultTrackDefinition(): TrackDefinition {
    val visual = when (this) {
        BoardLayer.INNER -> TrackVisualHint(horizontalCells = 28, verticalCells = 18)
        BoardLayer.OUTER -> TrackVisualHint(horizontalCells = 26, verticalCells = 18)
    }
    return TrackDefinition(
        id = TrackId(name.lowercase()),
        order = level,
        cells = places.mapIndexed { index, place -> place.toCellInstance("${name.lowercase()}-$index") },
        visual = visual,
    )
}

const val DREAM_ID_PARAMETER = "dreamId"
