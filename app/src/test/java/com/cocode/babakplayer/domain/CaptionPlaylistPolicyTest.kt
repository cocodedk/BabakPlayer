package com.cocode.babakplayer.domain

import com.cocode.babakplayer.model.Playlist
import com.cocode.babakplayer.model.PlaylistItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CaptionPlaylistPolicyTest {
    @Test
    fun caption_key_normalizes_case_and_spacing() {
        assertEquals("my shared list", CaptionPlaylistPolicy.captionKey("  My   Shared   List "))
        assertNull(CaptionPlaylistPolicy.captionKey("   "))
        assertNull(CaptionPlaylistPolicy.captionKey(null))
    }

    @Test
    fun merge_existing_caption_playlist_skips_duplicate_filenames_and_sorts() {
        val existing = playlist(
            captionKey = "my list",
            items = listOf(
                item(id = "e1", name = "part1.mp4", order = 0, bytes = 10),
                item(id = "e2", name = "part2.mp4", order = 1, bytes = 20),
            ),
        )
        val incoming = listOf(
            item(id = "n1", name = "PART2.mp4", order = 0, bytes = 20),
            item(id = "n2", name = "part10.mp4", order = 1, bytes = 100),
            item(id = "n3", name = "part3.mp4", order = 2, bytes = 30),
        )

        val merged = CaptionPlaylistPolicy.mergeIntoCaptionPlaylist(
            existingPlaylists = listOf(existing),
            incomingItems = incoming,
            caption = " My   List ",
            createdAt = 111L,
            sourceApp = "whatsapp",
        )

        assertNotNull(merged)
        assertEquals(2, merged?.addedCount)
        assertEquals(1, merged?.duplicateCount)
        assertEquals(
            listOf("part1.mp4", "part2.mp4", "part3.mp4", "part10.mp4"),
            merged?.playlist?.items?.map { it.originalDisplayName },
        )
        assertEquals(listOf(0, 1, 2, 3), merged?.playlist?.items?.map { it.importOrderIndex })
    }

    @Test
    fun merge_creates_new_playlist_for_new_caption_and_dedupes_incoming_filenames() {
        val incoming = listOf(
            item(id = "a1", name = "clip2.mp4", order = 0, bytes = 200),
            item(id = "a2", name = "clip1.mp4", order = 1, bytes = 100),
            item(id = "a3", name = "CLIP1.mp4", order = 2, bytes = 100),
        )

        val merged = CaptionPlaylistPolicy.mergeIntoCaptionPlaylist(
            existingPlaylists = emptyList(),
            incomingItems = incoming,
            caption = "Road Trip",
            createdAt = 999L,
            sourceApp = "telegram",
        )

        assertNotNull(merged)
        assertEquals(2, merged?.addedCount)
        assertEquals(1, merged?.duplicateCount)
        assertEquals("road trip", merged?.playlist?.captionKey)
        assertEquals(listOf("clip1.mp4", "clip2.mp4"), merged?.playlist?.items?.map { it.originalDisplayName })
    }

    @Test
    fun merge_returns_null_when_caption_is_blank() {
        val merged = CaptionPlaylistPolicy.mergeIntoCaptionPlaylist(
            existingPlaylists = emptyList(),
            incomingItems = listOf(item(id = "x", name = "a.mp4", order = 0, bytes = 1)),
            caption = "   ",
            createdAt = 1L,
            sourceApp = "whatsapp",
        )
        assertNull(merged)
    }

    private fun playlist(captionKey: String, items: List<PlaylistItem>): Playlist {
        return Playlist(
            playlistId = "p1",
            title = "My list",
            createdAt = 100L,
            sourceApp = "whatsapp",
            captionKey = captionKey,
            itemCount = items.size,
            totalBytes = items.sumOf { it.bytes },
            items = items,
        )
    }

    private fun item(id: String, name: String, order: Int, bytes: Long): PlaylistItem {
        return PlaylistItem(
            itemId = id,
            importOrderIndex = order,
            originalDisplayName = name,
            mimeType = "video/mp4",
            localPath = "content://media/$id",
            bytes = bytes,
        )
    }
}
