package com.cocode.babakplayer.domain

import com.cocode.babakplayer.model.Playlist
import com.cocode.babakplayer.model.PlaylistItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaylistAdjusterTest {
    @Test
    fun reconcile_removes_missing_items_and_reindexes() {
        val playlist = playlistOf(
            item(order = 0, id = "a", bytes = 100),
            item(order = 1, id = "b", bytes = 200),
            item(order = 2, id = "c", bytes = 300),
        )

        val adjusted = PlaylistAdjuster.reconcile(playlist) { it.itemId != "b" }

        assertEquals(listOf("a", "c"), adjusted?.items?.map { it.itemId })
        assertEquals(listOf(0, 1), adjusted?.items?.map { it.importOrderIndex })
        assertEquals(2, adjusted?.itemCount)
        assertEquals(400L, adjusted?.totalBytes)
    }

    @Test
    fun reconcile_returns_null_when_all_items_missing() {
        val playlist = playlistOf(item(order = 0, id = "x", bytes = 42))
        val adjusted = PlaylistAdjuster.reconcile(playlist) { false }
        assertNull(adjusted)
    }

    private fun playlistOf(vararg items: PlaylistItem): Playlist {
        return Playlist(
            playlistId = "p1",
            title = "P",
            createdAt = 1L,
            itemCount = items.size,
            totalBytes = items.sumOf { it.bytes },
            items = items.toList(),
        )
    }

    private fun item(order: Int, id: String, bytes: Long): PlaylistItem {
        return PlaylistItem(
            itemId = id,
            importOrderIndex = order,
            originalDisplayName = "$id.mp4",
            mimeType = "video/mp4",
            localPath = "content://media/$id",
            bytes = bytes,
        )
    }
}
