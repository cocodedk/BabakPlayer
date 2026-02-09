package com.cocode.babakplayer.domain

import com.cocode.babakplayer.model.PlaylistItem

object StorageReferencePolicy {
    fun referencePathFromSource(sourceUriOrPath: String): String = sourceUriOrPath

    fun physicalDeleteTargetsOnPlaylistDelete(items: List<PlaylistItem>): List<String> = emptyList()

    fun physicalDeleteTargetsOnItemDelete(item: PlaylistItem): List<String> = emptyList()
}
