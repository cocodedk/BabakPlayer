package com.cocode.babakplayer.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cocode.babakplayer.data.local.ImportService
import com.cocode.babakplayer.data.local.PlaylistStore
import com.cocode.babakplayer.domain.CaptionPlaylistPolicy
import com.cocode.babakplayer.domain.PlaylistAdjuster
import com.cocode.babakplayer.model.ImportResult
import com.cocode.babakplayer.model.ImportSummary
import com.cocode.babakplayer.model.ItemStatus
import com.cocode.babakplayer.model.Playlist
import com.cocode.babakplayer.model.PlaylistItem
import com.cocode.babakplayer.util.SharePayload
import com.cocode.babakplayer.util.TitleResolver
import java.io.File

class PlaylistRepository(private val context: Context) {
    private val store = PlaylistStore(context)
    private val importService = ImportService(context)

    suspend fun loadPlaylists(): List<Playlist> {
        val (reconciled, changed) = loadReconciledPlaylists()
        if (changed) {
            store.replacePlaylists(reconciled)
        }
        return reconciled
    }

    private suspend fun loadPlaylistsForMutation(): List<Playlist> {
        val (reconciled, _) = loadReconciledPlaylists()
        return reconciled
    }

    private suspend fun loadReconciledPlaylists(): Pair<List<Playlist>, Boolean> {
        val current = store.loadPlaylists()
        val reconciled = current.mapNotNull { playlist ->
            PlaylistAdjuster.reconcile(playlist, ::storageReferenceExists)
        }
        return reconciled to (reconciled != current)
    }

    suspend fun importPayload(payload: SharePayload): ImportResult {
        val createdAt = System.currentTimeMillis()
        val draft = importService.importPayload(payload)
        val resolvedTitle = TitleResolver.resolve(
            firstDescription = payload.firstDescription,
            caption = payload.caption,
            firstFileName = draft.firstDisplayName,
            createdAtMs = createdAt,
        )
        val current = loadPlaylistsForMutation()
        val grouped = CaptionPlaylistPolicy.mergeIntoCaptionPlaylist(
            existingPlaylists = current,
            incomingItems = draft.items,
            caption = payload.caption,
            createdAt = createdAt,
            sourceApp = payload.sourceApp,
        )
        val summaryTitle = grouped?.playlist?.title ?: resolvedTitle

        if (draft.items.isEmpty()) {
            return ImportResult(
                playlist = null,
                summary = ImportSummary(
                    title = summaryTitle,
                    importedCount = 0,
                    skippedCount = draft.skippedCount,
                    unsupportedCount = draft.unsupportedCount,
                    totalBytes = 0L,
                ),
            )
        }

        val playlist = grouped?.playlist ?: Playlist(
            title = resolvedTitle,
            createdAt = createdAt,
            sourceApp = payload.sourceApp,
            itemCount = draft.items.size,
            totalBytes = draft.totalBytes,
            items = draft.items.sortedBy { it.importOrderIndex },
        )
        val shouldPersist = grouped == null || grouped.addedCount > 0 || current.none { it.playlistId == playlist.playlistId }
        if (shouldPersist) store.upsertPlaylist(playlist)

        val duplicateSkips = grouped?.duplicateCount ?: 0
        val importedCount = grouped?.addedCount ?: draft.importedCount
        val totalBytes = grouped?.addedBytes ?: draft.totalBytes
        return ImportResult(
            playlist = playlist,
            summary = ImportSummary(
                title = summaryTitle,
                importedCount = importedCount,
                skippedCount = draft.skippedCount + duplicateSkips,
                unsupportedCount = draft.unsupportedCount,
                totalBytes = totalBytes,
            ),
        )
    }

    suspend fun deletePlaylist(playlistId: String) {
        store.removePlaylist(playlistId)
    }

    suspend fun deleteItem(playlistId: String, itemId: String) {
        val playlists = loadPlaylistsForMutation()
        val playlist = playlists.firstOrNull { it.playlistId == playlistId } ?: return
        val adjusted = PlaylistAdjuster.reconcile(playlist) { it.itemId != itemId }
        if (adjusted == null) {
            store.removePlaylist(playlistId)
            return
        }
        store.upsertPlaylist(adjusted)
    }

    suspend fun markItemDecodeFailed(playlistId: String, itemId: String) {
        val playlists = loadPlaylistsForMutation()
        val target = playlists.firstOrNull { it.playlistId == playlistId } ?: return
        val updated = target.items.map {
            if (it.itemId == itemId) it.copy(status = ItemStatus.DECODE_FAILED) else it
        }
        store.upsertPlaylist(target.copy(items = updated))
    }

    suspend fun savePlaylistDurations(playlistId: String, durations: Map<String, Long>) {
        val playlists = loadPlaylistsForMutation()
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
            "content" -> contentReferenceExists(parsed)
            else -> false
        }
    }

    private fun contentReferenceExists(uri: Uri): Boolean {
        val authority = uri.authority ?: return false
        val providerExists = context.packageManager.resolveContentProvider(authority, 0) != null
        if (!providerExists) {
            Log.w(TAG, "Dropping inaccessible content URI (provider missing): authority=$authority path=${redactPath(uri)}")
            return false
        }

        if (isUnstableExternalContentAuthority(authority)) {
            val readable = runCatching {
                context.contentResolver.openAssetFileDescriptor(uri, "r")?.use {
                    true
                } ?: false
            }.getOrElse { error ->
                Log.w(
                    TAG,
                    "Dropping inaccessible unstable content URI: authority=$authority path=${redactPath(uri)}. error=${error.message}",
                    error,
                )
                false
            }
            return readable
        }

        // For stable providers (MediaStore/DocumentsProvider), avoid aggressive probing.
        return true
    }

    private fun isUnstableExternalContentAuthority(authority: String): Boolean {
        val normalized = authority.lowercase()
        return normalized == "com.whatsapp.provider.media" ||
            normalized.startsWith("com.whatsapp.provider.media.") ||
            normalized == "com.whatsapp.w4b.provider.media" ||
            normalized.startsWith("com.whatsapp.w4b.provider.media.")
    }

    private fun redactPath(uri: Uri): String {
        val path = uri.path ?: return "?"
        val hash = path.hashCode().toUInt().toString(16)
        val truncated = if (path.length > 12) path.take(12) + "..." else path
        return "$truncated[#$hash]"
    }

    private companion object {
        private const val TAG = "PlaylistRepository"
    }
}
