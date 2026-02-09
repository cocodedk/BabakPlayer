package com.cocode.babakplayer.data.local

import android.content.Context
import com.cocode.babakplayer.model.Playlist
import java.io.File
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PlaylistStore(context: Context) {
    private val rootDir = File(context.filesDir, "playlists").apply { mkdirs() }
    private val indexFile = File(rootDir, "index.json")
    private val lock = Mutex()

    suspend fun loadPlaylists(): List<Playlist> = lock.withLock {
        if (!indexFile.exists()) return emptyList()
        val content = runCatching { indexFile.readText() }.getOrDefault("")
        return PlaylistJsonCodec.decode(content)
            .sortedByDescending { it.createdAt }
    }

    suspend fun upsertPlaylist(playlist: Playlist) = lock.withLock {
        val current = loadUnsafe().toMutableList()
        current.removeAll { it.playlistId == playlist.playlistId }
        current.add(playlist)
        saveUnsafe(current)
    }

    suspend fun removePlaylist(playlistId: String) = lock.withLock {
        val current = loadUnsafe().toMutableList()
        current.removeAll { it.playlistId == playlistId }
        saveUnsafe(current)
    }

    suspend fun replacePlaylists(playlists: List<Playlist>) = lock.withLock {
        saveUnsafe(playlists)
    }

    fun playlistDir(playlistId: String): File = File(rootDir, playlistId).apply { mkdirs() }

    private fun loadUnsafe(): List<Playlist> {
        if (!indexFile.exists()) return emptyList()
        return PlaylistJsonCodec.decode(indexFile.readText())
    }

    private fun saveUnsafe(playlists: List<Playlist>) {
        val parent = indexFile.parentFile
        if (parent != null && !parent.exists()) parent.mkdirs()
        indexFile.writeText(PlaylistJsonCodec.encode(playlists))
    }
}
