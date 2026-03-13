package com.cocode.babakplayer.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackUiStateTest {

    @Test
    fun default_state_is_idle() {
        val state = PlaybackUiState()
        assertFalse(state.isPlaying)
        assertFalse(state.isBuffering)
        assertFalse(state.isCasting)
        assertNull(state.currentItemId)
        assertEquals(0L, state.positionMs)
        assertEquals(0L, state.durationMs)
    }

    @Test
    fun casting_state_is_represented() {
        val state = PlaybackUiState(
            isPlaying = true,
            isCasting = true,
            currentItemId = "item-1",
            positionMs = 5000L,
            durationMs = 120000L,
        )
        assertTrue(state.isCasting)
        assertTrue(state.isPlaying)
        assertEquals("item-1", state.currentItemId)
    }

    @Test
    fun copy_preserves_casting_flag() {
        val original = PlaybackUiState(isCasting = true)
        val updated = original.copy(isPlaying = true)
        assertTrue(updated.isCasting)
        assertTrue(updated.isPlaying)
    }
}
