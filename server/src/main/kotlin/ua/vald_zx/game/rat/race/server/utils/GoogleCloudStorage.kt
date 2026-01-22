package ua.vald_zx.game.rat.race.server.utils

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.StorageOptions
import ua.vald_zx.game.rat.race.server.LOGGER

private val bucketName = "board_race"
private val storage by lazy {
    StorageOptions.getDefaultInstance().service
}

val hasGoogleStorage by lazy {
    try {
        storage.list().iterateAll().forEach { bucket ->
            LOGGER.debug("Знайдено бакет: " + bucket.name)
        }
        true
    } catch (e: Exception) {
        false
    }
}

fun existInCloudStorage(key: String): Boolean {
    try {
        val blobId = BlobId.of(bucketName, key)
        val blob = storage.get(blobId)
        return blob != null && blob.exists()
    } catch (e: Exception) {
        LOGGER.error("Failed to read Blob from $key", e)
        return false
    }
}


fun getDataFromCloud(key: String): String? {
    try {
        val blobId = BlobId.of(bucketName, key)
        val content: ByteArray = storage.readAllBytes(blobId) ?: return null
        return String(content, Charsets.UTF_8)
    } catch (e: Exception) {
        LOGGER.error("Failed to read Blob from $key", e)
        return null
    }
}

fun setDataToCloud(key: String, data: String) {
    try {
        val blobInfo = BlobInfo.newBuilder(bucketName, key).setContentType("text/plain").build()
        storage.create(blobInfo, data.toByteArray())
    } catch (e: Exception) {
        LOGGER.error("Failed to set the board to $key", e)
    }
}

fun removeFromCloud(key: String) {
    try {
        val blobId = BlobId.of(bucketName, key)
        storage.delete(blobId)
    } catch (e: Exception) {
        LOGGER.error("Failed to set the board to $key", e)
    }
}