package com.cocode.babakplayer.data.local

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import com.cocode.babakplayer.domain.StorageReferencePolicy
import com.cocode.babakplayer.model.PlaylistItem
import com.cocode.babakplayer.util.SharePayload
import com.cocode.babakplayer.util.detectSupportedMedia
import com.cocode.babakplayer.util.extractDisplayName
import java.io.File

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

            persistReadPermissionIfPossible(resolver, uri)
            val referencePath = StorageReferencePolicy.referencePathFromSource(uri.toString())
            totalBytes += bytes
            imported += PlaylistItem(
                importOrderIndex = index,
                originalDisplayName = displayName,
                mimeType = validation.mimeType,
                localPath = referencePath,
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

    private fun persistReadPermissionIfPossible(resolver: ContentResolver, uri: Uri) {
        runCatching {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
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
}
