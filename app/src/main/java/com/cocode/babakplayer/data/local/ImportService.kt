package com.cocode.babakplayer.data.local

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.cocode.babakplayer.domain.StorageReferencePolicy
import com.cocode.babakplayer.model.PlaylistItem
import com.cocode.babakplayer.util.SharePayload
import com.cocode.babakplayer.util.detectSupportedMedia
import com.cocode.babakplayer.util.extractDisplayName
import java.io.File
import java.util.UUID

class ImportService(private val context: Context) {
    data class ImportDraft(
        val items: List<PlaylistItem>,
        val importedCount: Int,
        val skippedCount: Int,
        val unsupportedCount: Int,
        val totalBytes: Long,
        val firstDisplayName: String?,
    )

    suspend fun importPayload(payload: SharePayload): ImportDraft {
        val resolver = context.contentResolver
        val imported = mutableListOf<PlaylistItem>()
        var skipped = 0
        var unsupported = 0
        var totalBytes = 0L
        var firstDisplayName: String? = null

        payload.uris.forEachIndexed { index, uri ->
            val displayName = queryDisplayName(resolver, uri) ?: extractDisplayName(uri.toString())
            if (firstDisplayName == null) firstDisplayName = displayName

            val mimeType = resolver.getType(uri)
            val validation = detectSupportedMedia(mimeType = mimeType, fileName = displayName)
            if (!validation.isSupported || validation.mimeType == null) {
                skipped++
                unsupported++
                return@forEachIndexed
            }

            val bytes = querySize(resolver, uri)
            if (bytes <= 0L) {
                skipped++
                return@forEachIndexed
            }

            val localPath = resolveLocalPath(resolver, uri, displayName)
            if (localPath == null) {
                skipped++
                return@forEachIndexed
            }

            totalBytes += bytes
            imported += PlaylistItem(
                importOrderIndex = index,
                originalDisplayName = displayName,
                mimeType = validation.mimeType,
                localPath = localPath,
                bytes = bytes,
            )
        }

        return ImportDraft(
            items = imported,
            importedCount = imported.size,
            skippedCount = skipped,
            unsupportedCount = unsupported,
            totalBytes = totalBytes,
            firstDisplayName = firstDisplayName,
        )
    }

    private fun resolveLocalPath(
        resolver: ContentResolver,
        uri: Uri,
        displayName: String,
    ): String? {
        if (persistReadPermissionIfPossible(resolver, uri)) {
            return StorageReferencePolicy.referencePathFromSource(uri.toString())
        }
        return copyUriIntoAppStorage(resolver, uri, displayName)
    }

    private fun persistReadPermissionIfPossible(resolver: ContentResolver, uri: Uri): Boolean {
        return runCatching {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            true
        }.onFailure { error ->
            Log.w(
                TAG,
                "Failed to persist read permission for uri=$uri. " +
                    "Item may be removed later during PlaylistRepository.loadPlaylists() " +
                    "reconciliation unless fallback copy succeeds. error=${error.message}",
                error,
            )
        }.getOrDefault(false)
    }

    private fun copyUriIntoAppStorage(
        resolver: ContentResolver,
        uri: Uri,
        displayName: String,
    ): String? {
        val fallbackDir = File(context.filesDir, FALLBACK_IMPORT_DIR).apply { mkdirs() }
        val safeName = sanitizeFileName(displayName)
        val target = File(
            fallbackDir,
            "${System.currentTimeMillis()}-${UUID.randomUUID()}-$safeName",
        )

        return runCatching {
            val input = resolver.openInputStream(uri)
                ?: error("openInputStream returned null")
            input.use { source ->
                target.outputStream().use { destination ->
                    source.copyTo(destination)
                }
            }
            target.absolutePath
        }.onSuccess {
            Log.w(
                TAG,
                "Persistable URI permission unavailable for uri=$uri. " +
                    "Stored fallback copy at ${target.absolutePath}",
            )
        }.onFailure { error ->
            target.delete()
            Log.e(
                TAG,
                "Failed fallback copy for uri=$uri. " +
                    "Import entry will be skipped to avoid later broken references. " +
                    "error=${error.message}",
                error,
            )
        }.getOrNull()
    }

    private fun sanitizeFileName(name: String): String {
        return name
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .trim()
            .ifEmpty { "imported-media" }
    }

    private fun queryDisplayName(resolver: ContentResolver, uri: Uri): String? {
        val cursor = runCatching {
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        }.getOrNull() ?: return null

        cursor.use {
            if (!it.moveToFirst()) return null
            val column = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (column < 0) return null
            return it.getString(column)
        }
    }

    private fun querySize(resolver: ContentResolver, uri: Uri): Long {
        val fromMetadata = runCatching {
            resolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
        }.getOrNull()?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            val column = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (column < 0 || cursor.isNull(column)) return@use null
            cursor.getLong(column)
        }

        if (fromMetadata != null && fromMetadata > 0L) return fromMetadata

        val fromAssetDescriptor = runCatching {
            resolver.openAssetFileDescriptor(uri, "r")?.use { descriptor ->
                descriptor.length.takeIf { it > 0L }
            }
        }.getOrNull()

        if (fromAssetDescriptor != null) return fromAssetDescriptor

        if (uri.scheme == "file") {
            val path = uri.path ?: return 0L
            return File(path).length().takeIf { it > 0L } ?: 0L
        }

        return 0L
    }

    companion object {
        private const val TAG = "ImportService"
        private const val FALLBACK_IMPORT_DIR = "imported_media"
    }
}
