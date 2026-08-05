package ua.vald_zx.game.rat.race.server

import kotlin.test.Test
import kotlin.test.assertEquals

class LlmSettingsTest {

    @Test
    fun freeTierProvidersHaveWorkingDefaults() {
        val keys = mapOf(
            "GROQ_API_KEY" to "groq-key",
            "CEREBRAS_API_KEY" to "cerebras-key",
            "MISTRAL_API_KEY" to "mistral-key",
            "OPENROUTER_API_KEY" to "openrouter-key",
        )

        val providers = configuredLlmProviders(keys::get)

        assertEquals(listOf("groq", "cerebras", "mistral", "openrouter"), providers.map { it.name })
        assertEquals(
            listOf(
                "https://api.groq.com/openai/v1/chat/completions",
                "https://api.cerebras.ai/v1/chat/completions",
                "https://api.mistral.ai/v1/chat/completions",
                "https://openrouter.ai/api/v1/chat/completions",
            ),
            providers.map { it.url },
        )
        assertEquals(
            listOf("openai/gpt-oss-120b", "gpt-oss-120b", "mistral-small-latest", "openrouter/free"),
            providers.map { it.balanceModel },
        )
        assertEquals(
            listOf("openai/gpt-oss-20b", "gpt-oss-120b", "mistral-small-latest", "openrouter/free"),
            providers.map { it.textModel },
        )
    }

    @Test
    fun builtInProviderModelsCanBeOverridden() {
        val configuration = mapOf(
            "GROQ_API_KEY" to "groq-key",
            "GROQ_MODEL" to "custom-model",
        )

        val provider = configuredLlmProviders(configuration::get).single()

        assertEquals("custom-model", provider.balanceModel)
        assertEquals("custom-model", provider.textModel)
    }
}
