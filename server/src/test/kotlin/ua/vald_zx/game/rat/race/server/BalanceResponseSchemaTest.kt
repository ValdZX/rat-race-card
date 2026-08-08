package ua.vald_zx.game.rat.race.server

import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.serializer
import ua.vald_zx.game.rat.race.card.shared.GeneratedBalance
import ua.vald_zx.game.rat.race.card.shared.ShopType
import ua.vald_zx.game.rat.race.card.shared.generatedLocales
import ua.vald_zx.game.rat.race.server.generation.SALARY_SCALE_FIELDS
import ua.vald_zx.game.rat.race.server.generation.balanceResponseFormat
import ua.vald_zx.game.rat.race.server.generation.salaryScaleResponseFormat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BalanceResponseSchemaTest {

    private val JsonObject.schema: JsonObject get() = getValue("json_schema").jsonObject.getValue("schema").jsonObject
    private val JsonObject.properties: JsonObject get() = getValue("properties").jsonObject
    private val JsonObject.required: List<String>
        get() = getValue("required").jsonArray.map { it.jsonPrimitive.content }

    @Test
    fun theEconomySchemaCoversEveryGeneratedFieldExceptTheFixedScale() {
        val expected = serializer<GeneratedBalance>().descriptor.elementNames
            .filterNot { it in SALARY_SCALE_FIELDS }

        val schema = balanceResponseFormat.schema

        assertEquals(expected, schema.properties.keys.toList())
        assertEquals(expected, schema.required)
        assertEquals("json_schema", balanceResponseFormat.getValue("type").jsonPrimitive.content)
        assertTrue(balanceResponseFormat.getValue("json_schema").jsonObject.getValue("strict").jsonPrimitive.content.toBoolean())
    }

    @Test
    fun theScaleSchemaAsksOnlyForSalariesAndCurrency() {
        val schema = salaryScaleResponseFormat.schema

        assertEquals(SALARY_SCALE_FIELDS, schema.properties.keys.toList())
        assertEquals("array", schema.properties.getValue("salaries").jsonObject.getValue("type").jsonPrimitive.content)
        assertEquals(
            "integer",
            schema.properties.getValue("salaries").jsonObject.getValue("items").jsonObject
                .getValue("type").jsonPrimitive.content,
        )
        assertEquals("string", schema.properties.getValue("currency").jsonObject.getValue("type").jsonPrimitive.content)
    }

    @Test
    fun enumKeyedAndLocaleKeyedMapsBecomeClosedObjects() {
        val properties = balanceResponseFormat.schema.properties

        val shoppingPrices = properties.getValue("shoppingPrices").jsonObject
        assertEquals(ShopType.entries.map { it.name }, shoppingPrices.properties.keys.toList())
        assertEquals(ShopType.entries.map { it.name }, shoppingPrices.required)

        val shareNames = properties.getValue("shares").jsonObject
            .getValue("items").jsonObject.properties
            .getValue("names").jsonObject
        assertEquals(generatedLocales, shareNames.properties.keys.toList())
        assertEquals(false, shareNames.getValue("additionalProperties").jsonPrimitive.content.toBoolean())
    }
}
