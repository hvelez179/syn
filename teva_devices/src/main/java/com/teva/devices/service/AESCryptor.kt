//
// AESCryptor.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.service

import java.io.UnsupportedEncodingException
import java.security.GeneralSecurityException

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor

/**
 * Encrypts and Decrypts byte arrays using the AES algorithm.
 * @param key The AES encryption key
 */
class AESCryptor
@Throws(GeneralSecurityException::class)
constructor(key: String) {

    private val encryptCipher: Cipher
    private val decryptCipher: Cipher

    init {
        val keyData = parseKey(key)

        val keySpec = SecretKeySpec(keyData, "AES")
        encryptCipher = Cipher.getInstance("AES/ECB/NoPadding")
        encryptCipher.init(Cipher.ENCRYPT_MODE, keySpec)

        decryptCipher = Cipher.getInstance("AES/ECB/NoPadding")
        decryptCipher.init(Cipher.DECRYPT_MODE, keySpec)

    }

    /**
     * Decrypts a byte array.
     */
    @Throws(GeneralSecurityException::class)
    fun decrypt(buffer: ByteArray): ByteArray {
        return decryptCipher.doFinal(buffer)
    }

    /**
     * Encrypts a byte array.
     */
    @Throws(GeneralSecurityException::class)
    fun encrypt(buffer: ByteArray): ByteArray {
        return encryptCipher.doFinal(buffer)
    }

    /**
     * Decrypts a buffer using a keystream algorithm. This method allows the encryption
     * and decryption of data that is not an even multiple of 16 bytes in length.
     * The algorithm encrypts the 16-byte "nonce" buffer and then exclusive-OR's the
     * input buffer with the nonce. After each 16 bytes of input buffer, the nonce is incremented

     * @param buffer The input buffer.
     * *
     * @param nonce  The 16-byte nonce that is used to create the keystream.
     * *
     * @return The decrypted data.
     */
    @Throws(GeneralSecurityException::class)
    fun decryptWithKeyStream(buffer: ByteArray, nonce: ByteArray): ByteArray {
        val decryptedData = ByteArray(buffer.size)

        var keyStream: ByteArray? = null
        for (i in buffer.indices) {
            if (i % 16 == 0) {
                keyStream = encrypt(nonce)
                incrementNonce(nonce)
            }

            decryptedData[i] = buffer[i] xor keyStream!![i % 16]
        }

        return decryptedData
    }

    /**
     * Increments a 16-byte nonce as if it were a 16-byte integer.
     */
    private fun incrementNonce(nonce: ByteArray) {
        for (i in 15 downTo 0) {
            // byte is signed, so convert to int first
            // before checking for an unsigned value.
            var nonceByte = nonce[i].toInt() and 0xff
            if (nonceByte < 255) {
                nonceByte += 1
                nonce[i] = nonceByte.toByte()
                break
            }

            nonce[i] = 0
        }
    }

    companion object {
        /**
         * Parses an authentication key into a byte array from either a 16-character ascii string or
         * a 32 character hex string.
         */
        fun parseKey(key: String): ByteArray? {
            var keyData: ByteArray? = null

            try {
                if (key.length == 16) {
                    keyData = key.toByteArray(charset("US-ASCII"))
                } else if (key.length == 32) {
                    keyData = ByteArray(16)
                    var i = 0
                    while (i < 32) {
                        keyData[i / 2] = Integer.parseInt(key.substring(i, i + 2), 16).toByte()
                        i += 2
                    }
                }
            } catch (e: UnsupportedEncodingException) {
                // This exception can't happen.
                // We know that "US-ASCII" is a valid encoding.
            }

            return keyData
        }
    }
}
