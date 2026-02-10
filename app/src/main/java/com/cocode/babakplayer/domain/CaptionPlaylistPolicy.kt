package com.cocode.babakplayer.domain

import com.cocode.babakplayer.model.Playlist
import com.cocode.babakplayer.model.PlaylistItem
import java.util.UUID
import java.util.Locale

data class CaptionMergeResult(
    val playlist: Playlist,
    val addedCount: Int,
    val duplicateCount: Int,
    val addedBytes: Long,
)

object CaptionPlaylistPolicy {
    fun captionKey(caption: String?): String? {
        val normalized = caption?.trim()
            ?.replace(Regex("\\s+"), " ")
            ?.lowercase(Locale.ROOT)
        return normalized?.takeIf { it.isNotBlank() }
    }

    fun mergeIntoCaptionPlaylist(
        existingPlaylists: List<Playlist>,
        incomingItems: List<PlaylistItem>,
        caption: String?,
        createdAt: Long,
        sourceApp: String?,
    ): CaptionMergeResult? {
        val key = captionKey(caption) ?: return null
        val title = caption?.trim()?.takeIf { it.isNotBlank() } ?: "Playlist"
        val existing = existingPlaylists.firstOrNull { it.captionKey == key }
        val keptNames = existing?.items
            ?.map { normalizedFileName(it.originalDisplayName) }
            ?.toMutableSet()
            ?: mutableSetOf()

        val uniqueIncoming = mutableListOf<PlaylistItem>()
        var duplicates = 0
        incomingItems.forEach { item ->
            val name = normalizedFileName(item.originalDisplayName)
            if (keptNames.contains(name)) {
                duplicates++
            } else {
                keptNames.add(name)
                uniqueIncoming += item
            }
        }

        val mergedItems = (existing?.items.orEmpty() + uniqueIncoming)
            .sortedWith(compareByNaturalName())
            .mapIndexed { index, item ->
                item.copy(importOrderIndex = index)
            }

        if (existing != null && uniqueIncoming.isEmpty()) {
            return CaptionMergeResult(
                playlist = existing,
                addedCount = 0,
                duplicateCount = duplicates,
                addedBytes = 0L,
            )
        }

        val playlist = if (existing != null) {
            existing.copy(
                title = title,
                createdAt = existing.createdAt,
                updatedAt = createdAt,
                sourceApp = sourceApp ?: existing.sourceApp,
                captionKey = key,
                itemCount = mergedItems.size,
                totalBytes = mergedItems.sumOf { it.bytes },
                items = mergedItems,
            )
        } else {
            Playlist(
                playlistId = UUID.randomUUID().toString(),
                title = title,
                createdAt = createdAt,
                sourceApp = sourceApp,
                captionKey = key,
                itemCount = mergedItems.size,
                totalBytes = mergedItems.sumOf { it.bytes },
                items = mergedItems,
            )
        }

        return CaptionMergeResult(
            playlist = playlist,
            addedCount = uniqueIncoming.size,
            duplicateCount = duplicates,
            addedBytes = uniqueIncoming.sumOf { it.bytes },
        )
    }

    private fun normalizedFileName(name: String): String {
        return name.trim().lowercase(Locale.ROOT)
    }

    private fun compareByNaturalName(): Comparator<PlaylistItem> {
        return Comparator { left, right ->
            val byName = compareNatural(
                left.originalDisplayName.lowercase(Locale.ROOT),
                right.originalDisplayName.lowercase(Locale.ROOT),
            )
            if (byName != 0) byName else left.importOrderIndex.compareTo(right.importOrderIndex)
        }
    }

    private fun compareNatural(a: String, b: String): Int {
        var i = 0
        var j = 0
        while (i < a.length && j < b.length) {
            val ca = a[i]
            val cb = b[j]
            if (ca.isDigit() && cb.isDigit()) {
                val iEnd = digitRunEnd(a, i)
                val jEnd = digitRunEnd(b, j)
                val na = a.substring(i, iEnd).trimStart('0')
                val nb = b.substring(j, jEnd).trimStart('0')
                val byLength = na.length.compareTo(nb.length)
                if (byLength != 0) return byLength
                val byNumber = na.compareTo(nb)
                if (byNumber != 0) return byNumber
                val byOriginalLength = (iEnd - i).compareTo(jEnd - j)
                if (byOriginalLength != 0) return byOriginalLength
                i = iEnd
                j = jEnd
                continue
            }
            if (ca != cb) return ca.compareTo(cb)
            i++
            j++
        }
        return a.length.compareTo(b.length)
    }

    private fun digitRunEnd(value: String, start: Int): Int {
        var index = start
        while (index < value.length && value[index].isDigit()) index++
        return index
    }
}
