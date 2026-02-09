package com.cocode.babakplayer.domain

import com.cocode.babakplayer.model.Playlist
import com.cocode.babakplayer.model.PlaylistItem

object PlaylistAdjuster {
    fun reconcile(
        playlist: Playlist,
        exists: (PlaylistItem) -> Boolean,
    ): Playlist? {
        val kept = playlist.items
            .filter(exists)
            .sortedBy { it.importOrderIndex }

        if (kept.isEmpty()) return null

        val resequenced = kept.mapIndexed { index, item ->
            if (item.importOrderIndex == index) item else item.copy(importOrderIndex = index)
        }

        val updatedCount = resequenced.size
        val updatedBytes = resequenced.sumOf { it.bytes }
        val unchanged = updatedCount == playlist.itemCount &&
            updatedBytes == playlist.totalBytes &&
            resequenced == playlist.items

        return if (unchanged) playlist else {
            playlist.copy(
                itemCount = updatedCount,
                totalBytes = updatedBytes,
                items = resequenced,
            )
        }
    }
}
