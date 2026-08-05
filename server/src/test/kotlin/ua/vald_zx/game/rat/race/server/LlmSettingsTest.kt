package ua.vald_zx.game.rat.race.server

import kotlin.test.Test
import kotlin.test.assertEquals

class LlmSettingsTest {

    @Test
    fun generationProvidersHaveWorkingDefaults() {
        val keys = mapOf(
            "GROQ_API_KEY" to "groq-key",
            "CEREBRAS_API_KEY" to "cerebras-key",
            "MISTRAL_API_KEY" to "mistral-key",
            "OPENROUTER_API_KEY" to "openrouter-key",
        )

        val providers = configuredLlmProviders(keys::get)

        assertEquals(listOf("groq", "cerebras", "openrouter"), providers.map { it.name })
        assertEquals(
            listOf(
                "https://api.groq.com/openai/v1/chat/completions",
                "https://api.cerebras.ai/v1/chat/completions",
                "https://openrouter.ai/api/v1/chat/completions",
            ),
            providers.map { it.url },
        )
        assertEquals(
            listOf("openai/gpt-oss-120b", "gpt-oss-120b", "nvidia/nemotron-3-super-120b-a12b:free"),
            providers.map { it.balanceModel },
        )
        assertEquals(
            listOf("qwen/qwen3.6-27b", "gpt-oss-120b", "google/gemma-4-31b-it:free"),
            providers.map { it.textModel },
        )
        assertEquals(listOf(false, false, true), providers.map { it.fallbackOnly })
    }

    @Test
    fun mistralIsConfiguredOnlyAsTheTextReviewer() {
        val configuration = mapOf(
            "MISTRAL_API_KEY" to "mistral-key",
        )

        assertEquals(emptyList(), configuredLlmProviders(configuration::get))
        val reviewer = configuredTextReviewer(configuration::get)

        assertEquals("mistral-reviewer", reviewer?.name)
        assertEquals("https://api.mistral.ai/v1/chat/completions", reviewer?.url)
        assertEquals("mistral-small-2603", reviewer?.textModel)
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
