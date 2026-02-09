package com.cocode.babakplayer.util

import java.io.File
import java.util.Locale

private val supportedExtensions = setOf("mp3", "mp4", "mkv", "mov", "webm")
private val supportedMimes = mapOf(
    "audio/mpeg" to "mp3",
    "audio/mp3" to "mp3",
    "video/mp4" to "mp4",
    "video/x-matroska" to "mkv",
    "video/quicktime" to "mov",
    "video/webm" to "webm",
)

data class MediaValidation(
    val isSupported: Boolean,
    val mimeType: String?,
    val extension: String?,
)

fun fileNameWithoutExtension(name: String): String {
    val dotIndex = name.lastIndexOf('.')
    return if (dotIndex <= 0) name else name.substring(0, dotIndex)
}

fun normalizeExtension(fileName: String?): String? {
    if (fileName.isNullOrBlank()) return null
    return fileName.substringAfterLast('.', missingDelimiterValue = "")
        .lowercase(Locale.US)
        .takeIf { it.isNotBlank() }
}

fun detectSupportedMedia(mimeType: String?, fileName: String?): MediaValidation {
    val normalizedMime = mimeType?.lowercase(Locale.US)
    val extFromMime = normalizedMime?.let { supportedMimes[it] }
    val extFromName = normalizeExtension(fileName)
    val chosenExt = extFromMime ?: extFromName
    val isSupported = chosenExt != null && supportedExtensions.contains(chosenExt)
    val resolvedMime = normalizedMime ?: chosenExt?.let { ext ->
        supportedMimes.entries.firstOrNull { it.value == ext }?.key
    }
    return MediaValidation(
        isSupported = isSupported,
        mimeType = resolvedMime,
        extension = chosenExt,
    )
}

fun safeImportFileName(index: Int, displayName: String, fallbackExt: String?): String {
    val cleanName = displayName
        .replace(Regex("[^A-Za-z0-9._-]"), "_")
        .trim('_')
        .ifBlank { "part_${index + 1}" }
    val ext = normalizeExtension(cleanName) ?: fallbackExt.orEmpty()
    val base = if (ext.isBlank()) cleanName else fileNameWithoutExtension(cleanName)
    val numbered = "%03d_%s".format(index + 1, base)
    return if (ext.isBlank()) numbered else "$numbered.$ext"
}

fun asReadableSize(bytes: Long): String {
    if (bytes < 1024L) return "$bytes B"
    val units = arrayOf("KB", "MB", "GB")
    var value = bytes.toDouble()
    var unitIndex = -1
    while (value >= 1024 && unitIndex < units.lastIndex) {
        value /= 1024
        unitIndex++
    }
    return "%.1f %s".format(Locale.getDefault(), value, units[unitIndex])
}

fun asDurationText(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(Locale.getDefault(), minutes, seconds)
}

fun extractDisplayName(pathOrName: String): String {
    return File(pathOrName).name.ifBlank { "Media file" }
}
