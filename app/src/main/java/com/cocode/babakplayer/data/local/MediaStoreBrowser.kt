package com.cocode.babakplayer.data.local

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class MediaBrowseCategory {
    AUDIO,
    WHATSAPP,
    DOWNLOADS,
}

data class BrowsableMedia(
    val uri: Uri,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val dateAddedSec: Long,
)

class MediaStoreBrowser(private val context: Context) {
    suspend fun query(category: MediaBrowseCategory): List<BrowsableMedia> {
        return withContext(Dispatchers.IO) {
            when (category) {
                MediaBrowseCategory.AUDIO -> queryAudio()
                MediaBrowseCategory.WHATSAPP -> queryByPathKeyword("whatsapp")
                MediaBrowseCategory.DOWNLOADS -> queryByPathKeyword("download")
            }
        }
    }

    private fun queryAudio(): List<BrowsableMedia> {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
        )

        val cursor = context.contentResolver.query(
            uri,
            projection,
            "${MediaStore.Audio.Media.SIZE} > 0",
            null,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC",
        ) ?: return emptyList()

        cursor.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val mimeCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val sizeCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            return buildList {
                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    add(
                        BrowsableMedia(
                            uri = ContentUris.withAppendedId(uri, id),
                            displayName = it.getString(nameCol) ?: "Audio",
                            mimeType = it.getString(mimeCol) ?: "audio/*",
                            sizeBytes = it.getLong(sizeCol),
                            dateAddedSec = it.getLong(dateCol),
                        ),
                    )
                }
            }
        }
    }

    private fun queryByPathKeyword(keyword: String): List<BrowsableMedia> {
        val uri = MediaStore.Files.getContentUri("external")
        val pathColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.FileColumns.RELATIVE_PATH
        } else {
            MediaStore.Files.FileColumns.DATA
        }
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
        )

        val selection = """
            (${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?)
            AND (${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR ${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ?)
            AND ${MediaStore.Files.FileColumns.SIZE} > 0
            AND lower(ifnull($pathColumn, '')) LIKE ?
        """.trimIndent()

        val args = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
            "audio/%",
            "video/%",
            "%$keyword%",
        )

        val cursor = context.contentResolver.query(
            uri,
            projection,
            selection,
            args,
            "${MediaStore.Files.FileColumns.DATE_ADDED} DESC",
        ) ?: return emptyList()

        cursor.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val mimeCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val sizeCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val dateCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)

            return buildList {
                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    add(
                        BrowsableMedia(
                            uri = ContentUris.withAppendedId(uri, id),
                            displayName = it.getString(nameCol) ?: "Media",
                            mimeType = it.getString(mimeCol) ?: "*/*",
                            sizeBytes = it.getLong(sizeCol),
                            dateAddedSec = it.getLong(dateCol),
                        ),
                    )
                }
            }
        }
    }
}
