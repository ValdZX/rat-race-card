package ua.vald_zx.game.rat.race.server.data

import java.io.File

object Env {
    private val dotEnvValues: Map<String, String> by lazy { loadDotEnv() }

    operator fun get(key: String): String? =
        System.getenv(key)
            ?: System.getProperty(key)
            ?: dotEnvValues[key]

    fun require(key: String): String =
        get(key) ?: error("Missing required configuration: $key (set it as an environment variable or in .env)")

    private fun loadDotEnv(): Map<String, String> {
        val file = findDotEnvFile() ?: return emptyMap()
        return file.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains('=') }
            .associate { line ->
                val key = line.substringBefore('=').trim()
                val rawValue = line.substringAfter('=').trim()
                key to rawValue.unquote()
            }
    }

    private fun findDotEnvFile(): File? {
        var directory: File? = File(System.getProperty("user.dir"))
        while (directory != null) {
            val candidate = File(directory, ".env")
            if (candidate.isFile) return candidate
            directory = directory.parentFile
        }
        return null
    }

    private fun String.unquote(): String =
        when {
            length >= 2 && startsWith('"') && endsWith('"') -> substring(1, length - 1)
            length >= 2 && startsWith('\'') && endsWith('\'') -> substring(1, length - 1)
            else -> this
        }
}
