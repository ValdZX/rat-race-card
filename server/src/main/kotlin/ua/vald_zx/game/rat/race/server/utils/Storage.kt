package ua.vald_zx.game.rat.race.server.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer
import ua.vald_zx.game.rat.race.server.DefaultScope
import java.io.File

inline fun <reified T> savedMutableStateFlow(
    initialValue: () -> T,
    key: String,
): MutableStateFlow<T> {
    if (key.isEmpty()) error("key is empty")
    val json = Json { ignoreUnknownKeys = true }
    val serializer = serializer<T>()
    val initial: T = getFromStorage(key)?.let { stored ->
        runCatching { json.decodeFromString(serializer, stored) }
            .getOrElse { initialValue() }
    } ?: initialValue()
    return MutableStateFlow(initial).also { flow ->
        flow.onEach { value ->
            val element = json.encodeToJsonElement(serializer, value)
            val cleanedObj = (element as? JsonObject) ?: element
            setToStorage(key, cleanedObj.toString())
        }.launchIn(DefaultScope())
    }
}

private fun getFile(key: String): File = File("Board_$key.json")

fun existInStorage(key: String): Boolean {
    return if (hasGoogleStorage) {
        existInCloudStorage(key)
    } else {
        getFile(key).exists()
    }
}

fun getFromStorage(key: String): String? {
    if (hasGoogleStorage) {
        return getDataFromCloud(key)
    } else {
        if (!existInStorage(key)) return null
        val content: ByteArray = getFile(key).readBytes()
        return String(content, Charsets.UTF_8)
    }
}

fun setToStorage(key: String, data: String) {
    if (hasGoogleStorage) {
        setDataToCloud(key, data)
    } else {
        val file = getFile(key)
        file.writeBytes(data.toByteArray())
    }
}

fun removeFromStorage(key: String) {
    if (hasGoogleStorage) {
        removeFromCloud(key)
    } else {
        val file = getFile(key)
        file.delete()
    }
}