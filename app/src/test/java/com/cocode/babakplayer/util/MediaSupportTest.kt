package com.cocode.babakplayer.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaSupportTest {

    // --- normalizeExtension ---

    @Test
    fun normalizeExtension_extracts_lowercase_extension() {
        assertEquals("mp3", normalizeExtension("track.MP3"))
        assertEquals("mp4", normalizeExtension("video.Mp4"))
        assertEquals("mkv", normalizeExtension("file.MKV"))
    }

    @Test
    fun normalizeExtension_returns_null_for_blank_or_null() {
        assertNull(normalizeExtension(null))
        assertNull(normalizeExtension(""))
        assertNull(normalizeExtension("   "))
    }

    @Test
    fun normalizeExtension_returns_null_for_no_extension() {
        assertNull(normalizeExtension("noextension"))
    }

    @Test
    fun normalizeExtension_uses_last_dot() {
        assertEquals("mp3", normalizeExtension("file.backup.mp3"))
    }

    // --- fileNameWithoutExtension ---

    @Test
    fun fileNameWithoutExtension_strips_extension() {
        assertEquals("song", fileNameWithoutExtension("song.mp3"))
        assertEquals("my.video", fileNameWithoutExtension("my.video.mp4"))
    }

    @Test
    fun fileNameWithoutExtension_returns_name_when_no_extension() {
        assertEquals("noext", fileNameWithoutExtension("noext"))
    }

    @Test
    fun fileNameWithoutExtension_handles_dot_at_start() {
        assertEquals(".hidden", fileNameWithoutExtension(".hidden"))
    }

    // --- detectSupportedMedia ---

    @Test
    fun detectSupportedMedia_recognizes_supported_mime() {
        val result = detectSupportedMedia("audio/mpeg", "song.mp3")
        assertTrue(result.isSupported)
        assertEquals("audio/mpeg", result.mimeType)
        assertEquals("mp3", result.extension)
    }

    @Test
    fun detectSupportedMedia_recognizes_by_extension_when_mime_null() {
        val result = detectSupportedMedia(null, "video.mkv")
        assertTrue(result.isSupported)
        assertEquals("mkv", result.extension)
    }

    @Test
    fun detectSupportedMedia_rejects_unsupported_format() {
        val result = detectSupportedMedia("image/png", "photo.png")
        assertFalse(result.isSupported)
    }

    @Test
    fun detectSupportedMedia_handles_both_null() {
        val result = detectSupportedMedia(null, null)
        assertFalse(result.isSupported)
    }

    @Test
    fun detectSupportedMedia_mime_takes_precedence_over_extension() {
        val result = detectSupportedMedia("audio/mpeg", "file.wav")
        assertTrue(result.isSupported)
        assertEquals("mp3", result.extension)
    }

    @Test
    fun detectSupportedMedia_case_insensitive_mime() {
        val result = detectSupportedMedia("Audio/MPEG", "track.mp3")
        assertTrue(result.isSupported)
        assertEquals("audio/mpeg", result.mimeType)
        assertEquals("mp3", result.extension)
    }

    // --- safeImportFileName ---

    @Test
    fun safeImportFileName_sanitizes_and_numbers() {
        val result = safeImportFileName(0, "My Song!.mp3", "mp3")
        assertEquals("001_My_Song_.mp3", result)
    }

    @Test
    fun safeImportFileName_uses_fallback_ext_when_missing() {
        val result = safeImportFileName(2, "noext", "mp4")
        assertEquals("003_noext.mp4", result)
    }

    @Test
    fun safeImportFileName_uses_blank_display_name_fallback() {
        val result = safeImportFileName(4, "!!!", "mp3")
        assertEquals("005_part_5.mp3", result)
    }

    // --- asReadableSize ---

    @Test
    fun asReadableSize_formats_bytes() {
        assertEquals("512 B", asReadableSize(512L))
    }

    @Test
    fun asReadableSize_formats_megabytes() {
        val twoMb = 2L * 1024 * 1024
        val result = asReadableSize(twoMb)
        assertTrue("Expected '2.0 MB' pattern, got: $result", result.matches(Regex("2[.,]0 MB")))
    }

    @Test
    fun asReadableSize_zero_bytes() {
        assertEquals("0 B", asReadableSize(0L))
    }

    // --- asDurationText ---

    @Test
    fun asDurationText_formats_minutes_and_seconds() {
        assertEquals("1:30", asDurationText(90_000L))
        assertEquals("0:00", asDurationText(0L))
        assertEquals("10:05", asDurationText(605_000L))
    }

    // --- extractDisplayName ---

    @Test
    fun extractDisplayName_extracts_filename_from_path() {
        assertEquals("song.mp3", extractDisplayName("/some/path/song.mp3"))
    }

    @Test
    fun extractDisplayName_returns_name_if_no_path() {
        assertEquals("track.mp4", extractDisplayName("track.mp4"))
    }
}
