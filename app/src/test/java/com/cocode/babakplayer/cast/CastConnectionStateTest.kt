package com.cocode.babakplayer.cast

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CastConnectionStateTest {

    @Test
    fun all_states_are_defined() {
        val states = CastConnectionState.entries
        assertEquals(4, states.size)
        assertEquals(
            setOf("NOT_AVAILABLE", "NOT_CONNECTED", "CONNECTING", "CONNECTED"),
            states.map { it.name }.toSet(),
        )
    }

    @Test
    fun states_are_distinct() {
        assertNotEquals(CastConnectionState.CONNECTED, CastConnectionState.CONNECTING)
        assertNotEquals(CastConnectionState.NOT_CONNECTED, CastConnectionState.NOT_AVAILABLE)
    }
}
