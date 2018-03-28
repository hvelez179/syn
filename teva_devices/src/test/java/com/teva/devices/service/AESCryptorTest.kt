//
// AESCryptorTest.java
// teva_devices
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.devices.service


import org.junit.Test

import java.lang.reflect.Method
import java.security.GeneralSecurityException
import java.util.Arrays

import org.junit.Assert.assertTrue

class AESCryptorTest {
    @Test
    fun test_Given_16CharacterKey_Then_ParseKey() {
        val key = "1234567890123456"
        val expectedData = parseHex("31323334353637383930313233343536")

        val keyData = AESCryptor.parseKey(key)

        assertTrue(Arrays.equals(expectedData, keyData))
    }

    @Test
    fun test_Given_32CharacterKey_Then_ParseKey() {
        val key = "9d9a5098f94d6dabceeb4117a1cc738c"
        val expectedData = parseHex("9d9a5098f94d6dabceeb4117a1cc738c")

        val keyData = AESCryptor.parseKey(key)

        assertTrue(Arrays.equals(expectedData, keyData))
    }

    @Test
    @Throws(GeneralSecurityException::class)
    fun test_Given_Buffer_When_CallingEncrypt_Then_EncryptedBufferIsReturned() {
        val key = "30313230393334383730313230393334"
        val buffer = parseHex("9d9a5098f94d6dabceeb4117a1cc738c")

        val expectedBytes = parseHex("47d2a81ba97cd287915521e51394e09c")

        val cryptor = AESCryptor(key)

        val encryptedBuffer = cryptor.encrypt(buffer)

        assertTrue(Arrays.equals(expectedBytes, encryptedBuffer))
    }

    @Test
    @Throws(GeneralSecurityException::class)
    fun test_Given_Buffer_When_CallingDecrypt_Then_DecryptedBufferIsReturned() {
        val key = "1234567890123456"
        val buffer = parseHex("40f6fc27c44c9593cd558590878f12db")
        val expectedBytes = parseHex("123456789aabbccddeeff00ffeeddccb")

        val cryptor = AESCryptor(key)

        val decryptedBuffer = cryptor.decrypt(buffer)

        assertTrue(Arrays.equals(expectedBytes, decryptedBuffer))
    }

    private fun parseHex(hex: String): ByteArray {
        val count = hex.length / 2
        val buf = ByteArray(count)

        for (i in 0..count - 1) {
            val pos = i * 2
            buf[i] = Integer.parseInt(hex.substring(pos, pos + 2), 16).toByte()
        }

        return buf
    }

    @Test
    @Throws(GeneralSecurityException::class)
    fun test_Given_EncryptedBufferAndNonce_When_CallingDecryptWithKeyStream_Then_DecryptedBufferIsReturned() {
        val key = "0120934870120934"

        val nonce = parseHex("d2a028f9ca52c5f7870d274fb0e8b5aa")
        val buffer = parseHex("b1c98c74a32f40e245310055ff2300846b4c4f")
        val expectedBytes = parseHex("000044ef0b000f00c401bb012201000200dbc7")

        val cryptor = AESCryptor(key)

        val decryptedBuffer = cryptor.decryptWithKeyStream(buffer, nonce)

        assertTrue(Arrays.equals(expectedBytes, decryptedBuffer))
    }

    @Test
    @Throws(Exception::class)
    fun testIncrementNonceIncrementsANonceWithoutCarry() {
        val key = "0120934870120934"

        // nonce where the right most byte can be incremented without needing to carry the the next byte.
        val nonce = parseHex("d2a028f9ca52c5f7870d274fb0e8b5aa")
        val expectedNonce = parseHex("d2a028f9ca52c5f7870d274fb0e8b5ab")

        val cryptor = AESCryptor(key)

        // call private incrementNonce() using reflection
        val incrementNonceMethod = AESCryptor::class.java.getDeclaredMethod("incrementNonce", ByteArray::class.java)
        incrementNonceMethod.isAccessible = true
        incrementNonceMethod.invoke(cryptor, nonce as Any)

        // verify that the nonce now equals the expected value.
        assertTrue(Arrays.equals(expectedNonce, nonce))
    }

    @Test
    @Throws(Exception::class)
    fun testIncrementNonceIncrementsANonceWithCarry() {
        val key = "0120934870120934"

        // nonce where the right 3 bytes will carry to the next when incremented.
        val nonce = parseHex("d2a028f9ca52c5f7870d274fb0ffffff")
        val expectedNonce = parseHex("d2a028f9ca52c5f7870d274fb1000000")

        val cryptor = AESCryptor(key)

        // call private incrementNonce() using reflection
        val incrementNonceMethod = AESCryptor::class.java.getDeclaredMethod("incrementNonce", ByteArray::class.java)
        incrementNonceMethod.isAccessible = true
        incrementNonceMethod.invoke(cryptor, nonce as Any)

        // verify that the nonce now equals the expected value.
        assertTrue(Arrays.equals(expectedNonce, nonce))
    }

    @Test
    @Throws(Exception::class)
    fun testIncrementNonceRollsOverToZero() {
        val key = "0120934870120934"

        // nonce that will roll over to all zeros when incremented
        val nonce = parseHex("ffffffffffffffffffffffffffffffff")
        val expectedNonce = parseHex("00000000000000000000000000000000")

        val cryptor = AESCryptor(key)

        // call private incrementNonce() using reflection
        val incrementNonceMethod = AESCryptor::class.java.getDeclaredMethod("incrementNonce", ByteArray::class.java)
        incrementNonceMethod.isAccessible = true
        incrementNonceMethod.invoke(cryptor, nonce as Any)

        // verify that the nonce now equals the expected value.
        assertTrue(Arrays.equals(expectedNonce, nonce))
    }
}