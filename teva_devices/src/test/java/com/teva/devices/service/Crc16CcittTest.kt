//
// Crc16CcittTest.java
// teva_devices
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.devices.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class Crc16CcittTest {

    @Test
    @Throws(Exception::class)
    fun test_Given_BufferWithoutCRC_Then_ComputeResultIsCRC() {
        val buffer = byteArrayOf(39, 38, 37, 36, 35, 34, 33, 32, 31)
        val expectedCrc = 10365

        val crc = Crc16Ccitt.compute(buffer, buffer.size)

        assertEquals(expectedCrc.toLong(), crc.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun test_Given_BufferWithoutCRC_Then_ComputeResultIsZero() {
        val buffer = byteArrayOf(31, 32, 33, 34, 35, 36, 37, 38, 39, 228.toByte(), 242.toByte())
        val expectedCrc = 0

        val crc = Crc16Ccitt.compute(buffer, buffer.size)

        assertEquals(expectedCrc.toLong(), crc.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun test_Given_BufferWithoutCRC_Then_GetBytesResultIsCRCBytes() {
        val buffer = byteArrayOf(13, 23, 33, 43, 53, 63, 73, 83, 93)
        val expectedBytes = byteArrayOf((-13).toByte(), 91.toByte())

        val bytes = Crc16Ccitt.getBytes(buffer, buffer.size)

        assertTrue(Arrays.equals(expectedBytes, bytes))
    }
}
