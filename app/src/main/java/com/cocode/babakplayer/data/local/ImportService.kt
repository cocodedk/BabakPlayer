package com.cocode.babakplayer.data.local

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.cocode.babakplayer.model.PlaylistItem
import com.cocode.babakplayer.util.SharePayload
import com.cocode.babakplayer.util.detectSupportedMedia
import com.cocode.babakplayer.util.extractDisplayName
import com.cocode.babakplayer.util.safeImportFileName
import java.io.File
import java.io.FileOutputStream

class ImportService(private val context: Context) {
    data class ImportDraft(
        val items: List<PlaylistItem>,
        val importedCount: Int,
        val skippedCount: Int,
        val unsupportedCount: Int,
        val totalBytes: Long,
        val firstDisplayName: String?,
    )

    suspend fun importPayload(
        payload: SharePayload,
        playlistDir: File,
    ): ImportDraft {
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
            if (!validation.isSupported || validation.mimeType == null || validation.extension == null) {
                skipped++
                unsupported++
                return@forEachIndexed
            }

            val targetName = safeImportFileName(index, displayName, validation.extension)
            val targetFile = File(playlistDir, targetName)
            val copiedBytes = copyUriToFile(resolver, uri, targetFile)
            if (copiedBytes <= 0L) {
                targetFile.delete()
                skipped++
                return@forEachIndexed
            }

            totalBytes += copiedBytes
            imported += PlaylistItem(
                importOrderIndex = index,
                originalDisplayName = displayName,
                mimeType = validation.mimeType,
                localPath = targetFile.absolutePath,
                bytes = copiedBytes,
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

    private fun copyUriToFile(resolver: ContentResolver, uri: Uri, targetFile: File): Long {
        val input = runCatching { resolver.openInputStream(uri) }.getOrNull() ?: return 0L
        input.use { stream ->
            FileOutputStream(targetFile).use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var total = 0L
                while (true) {
                    val read = stream.read(buffer)
                    if (read <= 0) break
                    output.write(buffer, 0, read)
                    total += read
                }
                return total
            }
        }
    }

    private fun queryDisplayName(resolver: ContentResolver, uri: Uri): String? {
        val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
        val cursor = runCatching {
            resolver.query(uri, projection, null, null, null)
        }.getOrNull() ?: return null
        cursor.use {
            if (!it.moveToFirst()) return null
            val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (columnIndex < 0) return null
            return it.getString(columnIndex)
        }
    }
}
