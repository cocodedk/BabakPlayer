package com.cocode.babakplayer.data

import android.content.Context
import com.cocode.babakplayer.data.local.ImportService
import com.cocode.babakplayer.data.local.PlaylistStore
import com.cocode.babakplayer.model.ImportResult
import com.cocode.babakplayer.model.ImportSummary
import com.cocode.babakplayer.model.ItemStatus
import com.cocode.babakplayer.model.Playlist
import com.cocode.babakplayer.model.PlaylistItem
import com.cocode.babakplayer.util.SharePayload
import com.cocode.babakplayer.util.TitleResolver
import java.io.File
import java.util.UUID

class PlaylistRepository(context: Context) {
    private val store = PlaylistStore(context)
    private val importService = ImportService(context)

    suspend fun loadPlaylists(): List<Playlist> = store.loadPlaylists()

    suspend fun importPayload(payload: SharePayload): ImportResult {
        val createdAt = System.currentTimeMillis()
        val playlistId = UUID.randomUUID().toString()
        val playlistDir = store.playlistDir(playlistId)
        val draft = importService.importPayload(payload, playlistDir)
        val title = TitleResolver.resolve(
            firstDescription = payload.firstDescription,
            caption = payload.caption,
            firstFileName = draft.firstDisplayName,
            createdAtMs = createdAt,
        )

        if (draft.items.isEmpty()) {
            playlistDir.deleteRecursively()
            return ImportResult(
                playlist = null,
                summary = ImportSummary(
                    title = title,
                    importedCount = 0,
                    skippedCount = draft.skippedCount,
                    unsupportedCount = draft.unsupportedCount,
                    totalBytes = 0L,
                ),
            )
        }

        val playlist = Playlist(
            playlistId = playlistId,
            title = title,
            createdAt = createdAt,
            sourceApp = payload.sourceApp,
            itemCount = draft.items.size,
            totalBytes = draft.totalBytes,
            items = draft.items.sortedBy { it.importOrderIndex },
        )
        store.upsertPlaylist(playlist)

        return ImportResult(
            playlist = playlist,
            summary = ImportSummary(
                title = title,
                importedCount = draft.importedCount,
                skippedCount = draft.skippedCount,
                unsupportedCount = draft.unsupportedCount,
                totalBytes = draft.totalBytes,
            ),
        )
    }

    suspend fun deletePlaylist(playlistId: String) {
        val playlists = store.loadPlaylists()
        val target = playlists.firstOrNull { it.playlistId == playlistId } ?: return
        target.items.forEach { File(it.localPath).delete() }
        store.playlistDir(playlistId).deleteRecursively()
        store.removePlaylist(playlistId)
    }

    suspend fun deleteItem(playlistId: String, itemId: String) {
        val playlists = store.loadPlaylists()
        val playlist = playlists.firstOrNull { it.playlistId == playlistId } ?: return
        val item = playlist.items.firstOrNull { it.itemId == itemId } ?: return
        File(item.localPath).delete()

        val remaining = playlist.items.filterNot { it.itemId == itemId }
        if (remaining.isEmpty()) {
            store.playlistDir(playlistId).deleteRecursively()
            store.removePlaylist(playlistId)
            return
        }

        val updated = playlist.copy(
            itemCount = remaining.size,
            totalBytes = remaining.sumOf { it.bytes },
            items = remaining,
        )
        store.upsertPlaylist(updated)
    }

    suspend fun markItemDecodeFailed(playlistId: String, itemId: String) {
        val playlists = store.loadPlaylists()
        val target = playlists.firstOrNull { it.playlistId == playlistId } ?: return
        val updated = target.items.map {
            if (it.itemId == itemId) it.copy(status = ItemStatus.DECODE_FAILED) else it
        }
        store.upsertPlaylist(target.copy(items = updated))
    }

    suspend fun savePlaylistDurations(playlistId: String, durations: Map<String, Long>) {
        val playlists = store.loadPlaylists()
        val target = playlists.firstOrNull { it.playlistId == playlistId } ?: return
        val updatedItems = target.items.map { item ->
            val duration = durations[item.itemId] ?: return@map item
            item.copy(durationMs = duration)
        }
        store.upsertPlaylist(target.copy(items = updatedItems))
    }

    fun localFileExists(item: PlaylistItem): Boolean = File(item.localPath).exists()
}
