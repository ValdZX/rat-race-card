package ua.vald_zx.game.rat.race.server

import ua.vald_zx.game.rat.race.server.generation.configuredLlmProviders
import ua.vald_zx.game.rat.race.server.generation.configuredTextReviewer

import kotlin.test.Test
import kotlin.test.assertEquals

class LlmSettingsTest {

    @Test
    fun generationProvidersHaveWorkingDefaults() {
        val keys = mapOf(
            "GROQ_API_KEY" to "groq-key",
            "CEREBRAS_API_KEY" to "cerebras-key",
            "NVIDIA_API_KEY" to "nvidia-key",
            "MISTRAL_API_KEY" to "mistral-key",
            "OPENROUTER_API_KEY" to "openrouter-key",
        )

        val providers = configuredLlmProviders(keys::get)

        assertEquals(listOf("cerebras", "groq", "nvidia", "openrouter"), providers.map { it.name })
        assertEquals(
            listOf(
                "https://api.cerebras.ai/v1/chat/completions",
                "https://api.groq.com/openai/v1/chat/completions",
                "https://integrate.api.nvidia.com/v1/chat/completions",
                "https://openrouter.ai/api/v1/chat/completions",
            ),
            providers.map { it.url },
        )
        assertEquals(
            listOf(
                "gpt-oss-120b",
                "openai/gpt-oss-120b",
                "meta/llama-3.3-70b-instruct",
                "nvidia/nemotron-3-super-120b-a12b:free",
            ),
            providers.map { it.balanceModel },
        )
        assertEquals(
            listOf(
                "gpt-oss-120b",
                "qwen/qwen3.6-27b",
                "meta/llama-3.3-70b-instruct",
                "google/gemma-4-31b-it:free",
            ),
            providers.map { it.textModel },
        )
        assertEquals(listOf(false, false, false, true), providers.map { it.fallbackOnly })
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
