//
// Crc16Ccitt.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.service

/**
 * Calculates CRC values for byte arrays.
 */
internal object Crc16Ccitt {
    private val POLY = 0x5935
    private val INITIAL_VALUE = 0
    private val table = init()

    /**
     * Computes the CRC for a byte array.

     * @return The CRC as an integer
     */
    fun compute(buffer: ByteArray, length: Int): Int {
        var crc = INITIAL_VALUE
        for (i in 0..length - 1) {
            val b = buffer[i].toInt() and 0xff
            val tableIndex = (crc shr 8) xor (0xff and b)
            crc = ((crc shl 8) xor table[tableIndex]) and 0xffff
        }

        return crc
    }

    /**
     * Computes the CRC for an array of bytes

     * @return The CRC as an array of 2 bytes.
     */
    fun getBytes(buffer: ByteArray, length: Int): ByteArray {
        val crc = compute(buffer, length)

        return byteArrayOf((crc shr 8).toByte(), (crc and 0xff).toByte())
    }

    /**
     * Initializes the static table used to optimize the CRC calculation.
     */
    private fun init(): IntArray {
        var temp: Int
        var a: Int

        val tbl = IntArray(256)

        for (i in 0..255) {
            temp = 0
            a = i shl 8
            for (j in 0..7) {
                if ((temp xor a) and 0x8000 != 0) {
                    temp = ((temp shl 1) xor POLY) and 0xffff
                } else {
                    temp = temp shl 1
                }

                a = a shl 1
            }

            tbl[i] = temp and 0xffff
        }

        return tbl
    }
}
