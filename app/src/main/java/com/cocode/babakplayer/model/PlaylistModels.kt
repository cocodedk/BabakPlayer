package com.cocode.babakplayer.model

import java.util.UUID

enum class ItemStatus {
    READY,
    DECODE_FAILED,
    DELETED,
}

data class PlaylistItem(
    val itemId: String = UUID.randomUUID().toString(),
    val importOrderIndex: Int,
    val originalDisplayName: String,
    val mimeType: String,
    val localPath: String,
    val bytes: Long,
    val durationMs: Long? = null,
    val status: ItemStatus = ItemStatus.READY,
)

data class Playlist(
    val playlistId: String = UUID.randomUUID().toString(),
    val title: String,
    val createdAt: Long,
    val sourceApp: String? = null,
    val captionKey: String? = null,
    val itemCount: Int,
    val totalBytes: Long,
    val items: List<PlaylistItem>,
)

data class ImportSummary(
    val title: String,
    val importedCount: Int,
    val skippedCount: Int,
    val unsupportedCount: Int,
    val totalBytes: Long,
)

data class ImportResult(
    val playlist: Playlist?,
    val summary: ImportSummary,
)
