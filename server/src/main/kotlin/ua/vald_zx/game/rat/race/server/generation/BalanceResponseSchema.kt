package ua.vald_zx.game.rat.race.server.generation

import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.serializer
import ua.vald_zx.game.rat.race.card.shared.GeneratedBalance
import ua.vald_zx.game.rat.race.card.shared.generatedLocales

internal val salaryScaleResponseFormat: JsonObject by lazy {
    responseFormat("salary_scale", balanceDescriptor.objectSchema(SALARY_SCALE_FIELDS))
}

internal val balanceResponseFormat: JsonObject by lazy {
    val economyFields = balanceDescriptor.elementNames.filterNot { it in SALARY_SCALE_FIELDS }
    responseFormat("board_balance", balanceDescriptor.objectSchema(economyFields))
}

internal val SALARY_SCALE_FIELDS = listOf("salaries", "currency")

private val balanceDescriptor: SerialDescriptor get() = serializer<GeneratedBalance>().descriptor

private fun responseFormat(name: String, schema: JsonObject) = buildJsonObject {
    put("type", "json_schema")
    putJsonObject("json_schema") {
        put("name", name)
        put("strict", true)
        put("schema", schema)
    }
}

private fun SerialDescriptor.objectSchema(fields: Collection<String>): JsonObject {
    val names = elementNames.filter { it in fields }
    return objectOf(names) { name -> getElementDescriptor(getElementIndex(name)).jsonSchema() }
}

private fun SerialDescriptor.jsonSchema(): JsonObject = when (kind) {
    StructureKind.LIST -> buildJsonObject {
        put("type", "array")
        put("items", getElementDescriptor(0).jsonSchema())
    }

    StructureKind.MAP -> {
        val value = getElementDescriptor(1).jsonSchema()
        objectOf(keyNames()) { value }
    }

    StructureKind.CLASS, StructureKind.OBJECT -> objectSchema(elementNames.toList())

    SerialKind.ENUM -> buildJsonObject {
        put("type", "string")
        put("enum", JsonArray(elementNames.map(::JsonPrimitive)))
    }

    PrimitiveKind.BOOLEAN -> typeOf("boolean")
    PrimitiveKind.STRING, PrimitiveKind.CHAR -> typeOf("string")
    PrimitiveKind.FLOAT, PrimitiveKind.DOUBLE -> typeOf("number")
    else -> typeOf("integer")
}

private fun SerialDescriptor.keyNames(): List<String> = getElementDescriptor(0)
    .takeIf { it.kind == SerialKind.ENUM }
    ?.elementNames
    ?.toList()
    ?: generatedLocales

private fun objectOf(names: List<String>, property: (String) -> JsonObject) = buildJsonObject {
    put("type", "object")
    putJsonObject("properties") {
        names.forEach { name -> put(name, property(name)) }
    }
    put("required", JsonArray(names.map(::JsonPrimitive)))
    put("additionalProperties", false)
}

private fun typeOf(type: String) = buildJsonObject { put("type", type) }
