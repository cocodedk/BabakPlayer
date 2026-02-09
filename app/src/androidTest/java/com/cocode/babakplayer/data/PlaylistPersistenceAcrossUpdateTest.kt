package com.cocode.babakplayer.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cocode.babakplayer.data.local.PlaylistStore
import com.cocode.babakplayer.model.ItemStatus
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaylistPersistenceAcrossUpdateTest {
    private val context by lazy { InstrumentationRegistry.getInstrumentation().targetContext }
    private val playlistsRoot by lazy { File(context.filesDir, "playlists") }

    @Before
    fun setUp() {
        resetPlaylistsRoot()
    }

    @After
    fun tearDown() {
        resetPlaylistsRoot()
    }

    @Test
    fun existing_playlists_survive_app_update_startup_path() = runBlocking {
        val existingMediaFile = File(context.filesDir, "update-survival-media.mp4").apply {
            writeBytes(byteArrayOf(1, 2, 3))
        }

        val indexFile = File(playlistsRoot, "index.json").apply {
            parentFile?.mkdirs()
            writeText(legacyIndexJson(existingMediaFile.absolutePath))
        }

        // Simulate opening the app after an update (fresh repository instance, same app files).
        val firstBoot = PlaylistRepository(context).loadPlaylists()
        val secondBoot = PlaylistRepository(context).loadPlaylists()

        assertEquals(1, firstBoot.size)
        assertEquals(firstBoot, secondBoot)

        val restored = firstBoot.first()
        assertEquals("legacy-playlist", restored.playlistId)
        assertEquals(2, restored.items.size)
        assertEquals(listOf("legacy-content-item", "legacy-file-item"), restored.items.map { it.itemId })
        assertEquals(ItemStatus.READY, restored.items.first().status)
        assertTrue(indexFile.exists())

        // Ensure on-disk metadata remains readable after repository reconciliation pass.
        val persisted = PlaylistStore(context).loadPlaylists()
        assertEquals(1, persisted.size)
        assertEquals("legacy-playlist", persisted.first().playlistId)
        assertEquals(2, persisted.first().items.size)

        existingMediaFile.delete()
    }

    private fun legacyIndexJson(filePath: String): String {
        val escapedPath = filePath.replace("\\", "\\\\").replace("\"", "\\\"")
        return """
        {
          "playlists": [
            {
              "playlistId": "legacy-playlist",
              "title": "Legacy Playlist",
              "createdAt": 1735689600000,
              "itemCount": 2,
              "totalBytes": 13,
              "items": [
                {
                  "itemId": "legacy-content-item",
                  "importOrderIndex": 0,
                  "originalDisplayName": "part-1.mp4",
                  "mimeType": "video/mp4",
                  "localPath": "content://media/external/video/media/123",
                  "bytes": 10
                },
                {
                  "itemId": "legacy-file-item",
                  "importOrderIndex": 1,
                  "originalDisplayName": "part-2.mp4",
                  "mimeType": "video/mp4",
                  "localPath": "$escapedPath",
                  "bytes": 3
                }
              ]
            }
          ]
        }
        """.trimIndent()
    }

    private fun resetPlaylistsRoot() {
        if (playlistsRoot.exists()) playlistsRoot.deleteRecursively()
    }
}
