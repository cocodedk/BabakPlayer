package com.cocode.babakplayer.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TitleResolverTest {

    @Test
    fun resolve_prefers_description_over_caption() {
        val result = TitleResolver.resolve(
            firstDescription = "Episode 1 recap",
            caption = "My Playlist",
            firstFileName = "file.mp3",
            createdAtMs = 1000L,
        )
        assertEquals("Episode 1 recap", result)
    }

    @Test
    fun resolve_falls_back_to_caption() {
        val result = TitleResolver.resolve(
            firstDescription = null,
            caption = "Road Trip Mix",
            firstFileName = "file.mp3",
            createdAtMs = 1000L,
        )
        assertEquals("Road Trip Mix", result)
    }

    @Test
    fun resolve_falls_back_to_filename_without_extension() {
        val result = TitleResolver.resolve(
            firstDescription = null,
            caption = null,
            firstFileName = "cool_song.mp3",
            createdAtMs = 1000L,
        )
        assertEquals("cool_song", result)
    }

    @Test
    fun resolve_falls_back_to_timestamp() {
        val result = TitleResolver.resolve(
            firstDescription = null,
            caption = null,
            firstFileName = null,
            createdAtMs = 1000L,
        )
        assertTrue(result.contains("playlist") || result.contains("پلی‌لیست"))
    }

    @Test
    fun resolve_trims_whitespace_from_description() {
        val result = TitleResolver.resolve(
            firstDescription = "  padded  ",
            caption = "caption",
            firstFileName = null,
            createdAtMs = 1000L,
        )
        assertEquals("padded", result)
    }

    @Test
    fun resolve_skips_blank_description() {
        val result = TitleResolver.resolve(
            firstDescription = "   ",
            caption = "My Caption",
            firstFileName = null,
            createdAtMs = 1000L,
        )
        assertEquals("My Caption", result)
    }

    @Test
    fun resolve_skips_blank_caption_and_blank_description() {
        val result = TitleResolver.resolve(
            firstDescription = "",
            caption = "  ",
            firstFileName = "track.mp4",
            createdAtMs = 1000L,
        )
        assertEquals("track", result)
    }
}
