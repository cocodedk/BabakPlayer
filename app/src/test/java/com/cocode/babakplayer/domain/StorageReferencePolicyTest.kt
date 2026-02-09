package com.cocode.babakplayer.domain

import com.cocode.babakplayer.model.PlaylistItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StorageReferencePolicyTest {
    @Test
    fun import_keeps_source_uri_without_duplication() {
        val source = "content://com.android.providers.media.documents/document/video%3A1234"
        assertEquals(source, StorageReferencePolicy.referencePathFromSource(source))
    }

    @Test
    fun delete_playlist_has_no_file_purge_targets() {
        val items = listOf(
            PlaylistItem(
                itemId = "1",
                importOrderIndex = 0,
                originalDisplayName = "part1.mp4",
                mimeType = "video/mp4",
                localPath = "content://media/1",
                bytes = 10,
            ),
        )

        val targets = StorageReferencePolicy.physicalDeleteTargetsOnPlaylistDelete(items)
        assertTrue(targets.isEmpty())
    }
}
