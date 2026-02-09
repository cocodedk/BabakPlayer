package com.cocode.babakplayer.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.cocode.babakplayer.data.local.ImportService
import com.cocode.babakplayer.data.local.PlaylistStore
import com.cocode.babakplayer.domain.PlaylistAdjuster
import com.cocode.babakplayer.model.ImportResult
import com.cocode.babakplayer.model.ImportSummary
import com.cocode.babakplayer.model.ItemStatus
import com.cocode.babakplayer.model.Playlist
import com.cocode.babakplayer.model.PlaylistItem
import com.cocode.babakplayer.util.SharePayload
import com.cocode.babakplayer.util.TitleResolver
import java.io.File
import java.util.UUID

class PlaylistRepository(private val context: Context) {
    private val store = PlaylistStore(context)
    private val importService = ImportService(context)

    suspend fun loadPlaylists(): List<Playlist> {
        val current = store.loadPlaylists()
        val reconciled = current.mapNotNull { playlist ->
            PlaylistAdjuster.reconcile(playlist, ::storageReferenceExists)
        }

        if (reconciled != current) {
            store.replacePlaylists(reconciled)
        }
        return reconciled
    }

    suspend fun importPayload(payload: SharePayload): ImportResult {
        val createdAt = System.currentTimeMillis()
        val playlistId = UUID.randomUUID().toString()
        val draft = importService.importPayload(payload)
        val title = TitleResolver.resolve(
            firstDescription = payload.firstDescription,
            caption = payload.caption,
            firstFileName = draft.firstDisplayName,
            createdAtMs = createdAt,
        )

        if (draft.items.isEmpty()) {
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
        store.removePlaylist(playlistId)
    }

    suspend fun deleteItem(playlistId: String, itemId: String) {
        val playlists = loadPlaylists()
        val playlist = playlists.firstOrNull { it.playlistId == playlistId } ?: return
        val remaining = playlist.items.filterNot { it.itemId == itemId }

        if (remaining.isEmpty()) {
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
        val playlists = loadPlaylists()
        val target = playlists.firstOrNull { it.playlistId == playlistId } ?: return
        val updated = target.items.map {
            if (it.itemId == itemId) it.copy(status = ItemStatus.DECODE_FAILED) else it
        }
        store.upsertPlaylist(target.copy(items = updated))
    }

    suspend fun savePlaylistDurations(playlistId: String, durations: Map<String, Long>) {
        val playlists = loadPlaylists()
        val target = playlists.firstOrNull { it.playlistId == playlistId } ?: return
        val updatedItems = target.items.map { item ->
            val duration = durations[item.itemId] ?: return@map item
            item.copy(durationMs = duration)
        }
        store.upsertPlaylist(target.copy(items = updatedItems))
    }

    fun localFileExists(item: PlaylistItem): Boolean = storageReferenceExists(item)

    private fun storageReferenceExists(item: PlaylistItem): Boolean {
        val raw = item.localPath
        val parsed = Uri.parse(raw)

        return when (parsed.scheme?.lowercase()) {
            null, "" -> File(raw).exists()
            "file" -> parsed.path?.let { File(it).exists() } == true
            "content" -> contentUriExists(context.contentResolver, parsed)
            else -> false
        }
    }

    private fun contentUriExists(resolver: ContentResolver, uri: Uri): Boolean {
        val byDescriptor = runCatching {
            resolver.openAssetFileDescriptor(uri, "r")?.use { descriptor ->
                descriptor.length != 0L
            }
        }.getOrNull()

        if (byDescriptor != null) return byDescriptor

        return runCatching {
            resolver.openInputStream(uri)?.use { true } ?: false
        }.getOrDefault(false)
    }
}
