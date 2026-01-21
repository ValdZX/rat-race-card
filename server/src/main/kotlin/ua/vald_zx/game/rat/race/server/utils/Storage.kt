package ua.vald_zx.game.rat.race.server.utils

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.StorageOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer
import ua.vald_zx.game.rat.race.server.DefaultScope
import ua.vald_zx.game.rat.race.server.LOGGER
import java.io.File

private val storage by lazy {
    StorageOptions.getDefaultInstance().service
}

private val hasGoogleStorage by lazy {
    try {
        storage.list().iterateAll().forEach { bucket ->
            LOGGER.debug("Знайдено бакет: " + bucket.name)
        }
        true
    } catch (e: Exception) {
        false
    }
}

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
    if (hasGoogleStorage) {
        try {
            val blobId = BlobId.of("Board", key)
            val blob = storage.get(blobId)
            return blob != null && blob.exists()
        } catch (e: Exception) {
            LOGGER.error("Failed to read Blob from $key", e)
            return false
        }
    } else {
        return getFile(key).exists()
    }
}

fun getFromStorage(key: String): String? {
    if (hasGoogleStorage) {
        try {
            val blobId = BlobId.of("Board", key)
            val content: ByteArray = storage.readAllBytes(blobId) ?: return null
            return String(content, Charsets.UTF_8)
        } catch (e: Exception) {
            LOGGER.error("Failed to read Blob from $key", e)
            return null
        }
    } else {
        if (!existInStorage(key)) return null
        val content: ByteArray = getFile(key).readBytes()
        return String(content, Charsets.UTF_8)
    }
}

fun setToStorage(key: String, data: String) {
    if (hasGoogleStorage) {
        try {
            val blobInfo = BlobInfo.newBuilder("Board", key).setContentType("text/plain").build()
            storage.create(blobInfo, data.toByteArray())
        } catch (e: Exception) {
            LOGGER.error("Failed to set the board to $key", e)
        }
    } else {
        val file = getFile(key)
        file.writeBytes(data.toByteArray())
    }
}

fun removeFromStorage(key: String) {
    if (hasGoogleStorage) {
        try {
            val blobId = BlobId.of("Board", key)
            storage.delete(blobId)
        } catch (e: Exception) {
            LOGGER.error("Failed to set the board to $key", e)
        }
    } else {
        val file = getFile(key)
        file.delete()
    }
}