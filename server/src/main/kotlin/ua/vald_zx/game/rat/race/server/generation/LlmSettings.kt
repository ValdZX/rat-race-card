package ua.vald_zx.game.rat.race.server.generation

import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import ua.vald_zx.game.rat.race.server.data.Env

internal val llmLogger = KtorSimpleLogger("BoardGenerationLlm")

internal object LlmSettings {
    val providers: List<LlmProviderSettings> by lazy { configuredLlmProviders { Env[it] } }
    val textReviewer: LlmProviderSettings? by lazy { configuredTextReviewer { Env[it] } }

    val enabled: Boolean get() = providers.isNotEmpty()

    fun logConfiguration() {
        if (providers.isEmpty()) {
            llmLogger.warn("No LLM provider is configured, board generation is unavailable")
        } else {
            llmLogger.info(
                "LLM pool of ${providers.size}: " + providers.joinToString {
                    "${it.name} balance=${it.balanceModel} text=${it.textModel}"
                }
            )
        }
        textReviewer?.let { reviewer ->
            llmLogger.info("LLM text reviewer: ${reviewer.name}/${reviewer.textModel}")
        } ?: llmLogger.info("LLM text reviewer is not configured")
    }

    fun balanceChat(
        responseFormat: JsonObject? = null,
        onUsage: suspend (LlmTokenUsage) -> Unit = {},
        onQuota: suspend (LlmQuotaSnapshot) -> Unit = {},
        onRetry: suspend (LlmRetryWait) -> Unit = {},
        onFailed: suspend () -> Unit = {},
    ): ChatCompletion = pool(
        model = { it.balanceModel },
        extraBody = { it.balanceExtra },
        responseFormat = responseFormat,
        maxOutputTokens = MAX_BALANCE_COMPLETION_TOKENS,
        onUsage = onUsage,
        onQuota = onQuota,
        onRetry = onRetry,
        onFailed = onFailed,
    )

    fun textChat(
        onUsage: suspend (LlmTokenUsage) -> Unit = {},
        onQuota: suspend (LlmQuotaSnapshot) -> Unit = {},
        onRetry: suspend (LlmRetryWait) -> Unit = {},
        onFailed: suspend () -> Unit = {},
    ): ChatCompletion = pool(
        model = { it.textModel },
        extraBody = { it.textExtra },
        maxOutputTokens = DEFAULT_TEXT_BATCH_SIZE * TEXT_COMPLETION_TOKENS_PER_ITEM,
        onUsage = onUsage,
        onQuota = onQuota,
        onRetry = onRetry,
        onFailed = onFailed,
    )

    fun textReviewer(
        onUsage: suspend (LlmTokenUsage) -> Unit = {},
        onQuota: suspend (LlmQuotaSnapshot) -> Unit = {},
    ): TextReviewer = textReviewer?.let { reviewer ->
        LlmTextReviewer(
            HttpChatCompletion(
                provider = reviewer,
                model = reviewer.textModel,
                extraBody = reviewer.textExtra,
                maxOutputTokens = MAX_REVIEW_COMPLETION_TOKENS,
                onUsage = onUsage,
                onQuota = onQuota,
            )
        )
    } ?: AcceptAllTextReviewer

    private fun pool(
        model: (LlmProviderSettings) -> String,
        extraBody: (LlmProviderSettings) -> JsonObject?,
        responseFormat: JsonObject? = null,
        maxOutputTokens: Int,
        onUsage: suspend (LlmTokenUsage) -> Unit,
        onQuota: suspend (LlmQuotaSnapshot) -> Unit,
        onRetry: suspend (LlmRetryWait) -> Unit,
        onFailed: suspend () -> Unit = {},
    ): ChatCompletion = LlmProviderPool(
        members = providers.map { provider ->
            LlmProviderPool.Member(
                provider = provider.name,
                model = model(provider),
                fallbackOnly = provider.fallbackOnly,
                completion = HttpChatCompletion(
                    provider = provider,
                    model = model(provider),
                    extraBody = extraBody(provider),
                    responseFormat = responseFormat,
                    maxOutputTokens = maxOutputTokens,
                    onUsage = onUsage,
                    onQuota = onQuota,
                    onFailed = onFailed,
                ),
            )
        },
        onRetry = onRetry,
    )
}

internal fun configuredLlmProviders(
    configuration: (String) -> String?,
): List<LlmProviderSettings> = buildList {
    (listOf(PRIMARY_LLM_PROVIDER) + FREE_LLM_PROVIDERS).mapNotNullTo(this) { defaults ->
        provider(configuration, defaults)
    }
    (1..MAX_LLM_FALLBACKS).mapNotNullTo(this) { index ->
        provider(
            configuration,
            LlmProviderDefaults(
                prefix = "LLM_FALLBACK_${index}_",
                apiKey = "LLM_FALLBACK_${index}_API_KEY",
                name = "fallback-$index",
            ),
        )
    }
}

internal fun configuredTextReviewer(
    configuration: (String) -> String?,
): LlmProviderSettings? {
    val key = configuration("MISTRAL_API_KEY").orEmpty()
    if (key.isBlank()) return null
    val model = configuration("MISTRAL_REVIEW_MODEL")
        ?: configuration("MISTRAL_MODEL")
        ?: "mistral-small-2603"
    return LlmProviderSettings(
        name = configuration("MISTRAL_REVIEW_PROVIDER_NAME") ?: "mistral-reviewer",
        url = configuration("MISTRAL_API_URL") ?: "https://api.mistral.ai/v1/chat/completions",
        key = key,
        balanceModel = model,
        textModel = model,
        textExtra = extraBody(
            configuration("MISTRAL_REVIEW_EXTRA")
                ?: """{"temperature":0,"response_format":{"type":"json_object"}}""",
            "MISTRAL_REVIEW_EXTRA",
        ),
    )
}

private fun provider(
    configuration: (String) -> String?,
    defaults: LlmProviderDefaults,
): LlmProviderSettings? {
    val key = configuration(defaults.apiKey).orEmpty()
    if (key.isBlank()) return null
    val model = configuration("${defaults.prefix}MODEL")
    val url = configuration("${defaults.prefix}API_URL") ?: defaults.url
    val balanceModel = configuration("${defaults.prefix}BALANCE_MODEL") ?: model ?: defaults.balanceModel
    val textModel = configuration("${defaults.prefix}TEXT_MODEL") ?: model ?: defaults.textModel
    val missing = buildList {
        if (url == null) add("${defaults.prefix}API_URL")
        if (balanceModel == null) add("${defaults.prefix}BALANCE_MODEL or ${defaults.prefix}MODEL")
        if (textModel == null) add("${defaults.prefix}TEXT_MODEL or ${defaults.prefix}MODEL")
    }
    if (missing.isNotEmpty()) {
        llmLogger.warn(
            "${defaults.apiKey} is set but the provider is ignored, missing: ${missing.joinToString()}"
        )
        return null
    }
    return LlmProviderSettings(
        name = configuration("${defaults.prefix}PROVIDER_NAME") ?: defaults.name,
        url = requireNotNull(url),
        key = key,
        balanceModel = requireNotNull(balanceModel),
        textModel = requireNotNull(textModel),
        balanceExtra = extraBody(
            configuration("${defaults.prefix}BALANCE_EXTRA") ?: defaults.balanceExtra,
            "${defaults.prefix}BALANCE_EXTRA",
        ),
        textExtra = extraBody(
            configuration("${defaults.prefix}TEXT_EXTRA") ?: defaults.textExtra,
            "${defaults.prefix}TEXT_EXTRA",
        ),
        fallbackOnly = defaults.fallbackOnly,
    )
}

private fun extraBody(raw: String?, key: String): JsonObject? {
    val declared = raw?.takeIf { it.isNotBlank() } ?: return null
    return runCatching { Json.parseToJsonElement(declared).jsonObject }
        .onFailure { llmLogger.warn("$key is not a JSON object and is ignored: ${it.message}") }
        .getOrNull()
}

private data class LlmProviderDefaults(
    val prefix: String,
    val apiKey: String,
    val name: String,
    val url: String? = null,
    val balanceModel: String? = null,
    val textModel: String? = null,
    val balanceExtra: String? = null,
    val textExtra: String? = null,
    val fallbackOnly: Boolean = false,
)

private val PRIMARY_LLM_PROVIDER = LlmProviderDefaults(
    prefix = "LLM_",
    apiKey = "LLM_API_KEY",
    name = "gemini",
    url = "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions",
    balanceModel = "gemini-3.6-flash",
    textModel = "gemini-3.5-flash-lite",
    textExtra = """{"reasoning_effort":"low"}""",
)

private val FREE_LLM_PROVIDERS = listOf(
    LlmProviderDefaults(
        prefix = "CEREBRAS_",
        apiKey = "CEREBRAS_API_KEY",
        name = "cerebras",
        url = "https://api.cerebras.ai/v1/chat/completions",
        balanceModel = "gpt-oss-120b",
        textModel = "gpt-oss-120b",
    ),
    LlmProviderDefaults(
        prefix = "GROQ_",
        apiKey = "GROQ_API_KEY",
        name = "groq",
        url = "https://api.groq.com/openai/v1/chat/completions",
        balanceModel = "openai/gpt-oss-120b",
        textModel = "qwen/qwen3.6-27b",
        textExtra = """{"reasoning_effort":"none"}""",
    ),
    LlmProviderDefaults(
        prefix = "NVIDIA_",
        apiKey = "NVIDIA_API_KEY",
        name = "nvidia",
        url = "https://integrate.api.nvidia.com/v1/chat/completions",
        balanceModel = "meta/llama-3.3-70b-instruct",
        textModel = "meta/llama-3.3-70b-instruct",
    ),
    LlmProviderDefaults(
        prefix = "OPENROUTER_",
        apiKey = "OPENROUTER_API_KEY",
        name = "openrouter",
        url = "https://openrouter.ai/api/v1/chat/completions",
        balanceModel = "nvidia/nemotron-3-super-120b-a12b:free",
        textModel = "google/gemma-4-31b-it:free",
        balanceExtra = """{"reasoning":{"enabled":false},"response_format":{"type":"json_object"}}""",
        textExtra = """{"reasoning":{"enabled":false}}""",
        fallbackOnly = true,
    ),
)

internal data class LlmProviderSettings(
    val name: String,
    val url: String,
    val key: String,
    val balanceModel: String,
    val textModel: String,
    val balanceExtra: JsonObject? = null,
    val textExtra: JsonObject? = null,
    val fallbackOnly: Boolean = false,
)

private const val MAX_BALANCE_COMPLETION_TOKENS = 8_000
private const val MAX_REVIEW_COMPLETION_TOKENS = 800
private const val TEXT_COMPLETION_TOKENS_PER_ITEM = 140
private const val MAX_LLM_FALLBACKS = 3
internal const val DEFAULT_TEXT_BATCH_SIZE = 24
