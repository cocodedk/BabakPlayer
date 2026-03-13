package com.cocode.babakplayer.cast

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.InetAddress

class LocalMediaServerTest {

    @Test
    fun getDeviceIpAddress_returns_valid_ipv4_or_null() {
        val ip = LocalMediaServer.getDeviceIpAddress()
        // On CI or machines without network, this can be null -- that's valid.
        // When non-null, it must be a valid IPv4 address.
        if (ip != null) {
            val parts = ip.split(".")
            assertTrue("Expected IPv4 format, got: $ip", parts.size == 4)
            parts.forEach { part ->
                val num = part.toIntOrNull()
                assertNotNull("Each IPv4 octet must be numeric, got: $part", num)
                assertTrue("Octet out of range: $num", num!! in 0..255)
            }
        }
    }

    @Test
    fun getDeviceIpAddress_does_not_return_loopback() {
        val ip = LocalMediaServer.getDeviceIpAddress()
        if (ip != null) {
            val addr = InetAddress.getByName(ip)
            assertTrue("Should not return loopback address, got: $ip", !addr.isLoopbackAddress)
        }
    }
}
