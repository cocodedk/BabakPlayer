package com.cocode.babakplayer.data.local

import com.cocode.babakplayer.model.ItemStatus
import com.cocode.babakplayer.model.Playlist
import com.cocode.babakplayer.model.PlaylistItem
import org.json.JSONArray
import org.json.JSONObject

object PlaylistJsonCodec {
    fun encode(playlists: List<Playlist>): String {
        val root = JSONObject()
        root.put("playlists", JSONArray().apply {
            playlists.forEach { put(it.toJson()) }
        })
        return root.toString(2)
    }

    fun decode(raw: String): List<Playlist> {
        val root = runCatching { JSONObject(raw) }.getOrNull() ?: return emptyList()
        val array = root.optJSONArray("playlists") ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val playlist = array.optJSONObject(index)?.toPlaylist() ?: continue
                add(playlist)
            }
        }
    }

    private fun Playlist.toJson(): JSONObject {
        return JSONObject().apply {
            put("playlistId", playlistId)
            put("title", title)
            put("createdAt", createdAt)
            put("updatedAt", updatedAt)
            put("sourceApp", sourceApp)
            put("captionKey", captionKey)
            put("itemCount", itemCount)
            put("totalBytes", totalBytes)
            put("items", JSONArray().apply {
                items.forEach { put(it.toJson()) }
            })
        }
    }

    private fun PlaylistItem.toJson(): JSONObject {
        return JSONObject().apply {
            put("itemId", itemId)
            put("importOrderIndex", importOrderIndex)
            put("originalDisplayName", originalDisplayName)
            put("mimeType", mimeType)
            put("localPath", localPath)
            put("bytes", bytes)
            put("durationMs", durationMs)
            put("status", status.name)
        }
    }

    private fun JSONObject.toPlaylist(): Playlist? {
        val playlistId = optString("playlistId")
        val title = optString("title")
        if (playlistId.isBlank() || title.isBlank()) return null

        val itemsArray = optJSONArray("items") ?: JSONArray()
        val items = buildList {
            for (index in 0 until itemsArray.length()) {
                val item = itemsArray.optJSONObject(index)?.toPlaylistItem() ?: continue
                add(item)
            }
        }

        return Playlist(
            playlistId = playlistId,
            title = title,
            createdAt = optLong("createdAt"),
            updatedAt = if (isNull("updatedAt")) null else optLong("updatedAt").takeIf { it > 0L },
            sourceApp = optString("sourceApp").takeIf { it.isNotBlank() && it != "null" },
            captionKey = optString("captionKey").takeIf { it.isNotBlank() && it != "null" },
            itemCount = optInt("itemCount", items.size),
            totalBytes = optLong("totalBytes"),
            items = items,
        )
    }

    private fun JSONObject.toPlaylistItem(): PlaylistItem? {
        val itemId = optString("itemId")
        val localPath = optString("localPath")
        if (itemId.isBlank() || localPath.isBlank()) return null

        val status = runCatching { ItemStatus.valueOf(optString("status")) }
            .getOrDefault(ItemStatus.READY)

        return PlaylistItem(
            itemId = itemId,
            importOrderIndex = optInt("importOrderIndex"),
            originalDisplayName = optString("originalDisplayName").ifBlank { "Media file" },
            mimeType = optString("mimeType").ifBlank { "application/octet-stream" },
            localPath = localPath,
            bytes = optLong("bytes"),
            durationMs = if (isNull("durationMs")) null else optLong("durationMs"),
            status = status,
        )
    }
}
