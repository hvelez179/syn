//
// DatabaseEncryptionHandlerImpl.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.services.data.encrypteddata.entities

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.services.data.encrypteddata.DatabaseEncryptionHandler
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.Charset
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.interfaces.RSAPublicKey
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream

/**
 * This class implements the DatabaseEncryptionHandler interface for handling the password
 * used in database encryption. This class uses the Keystore for encrypting and decrypting
 * password.
 */

class DatabaseEncryptionHandlerImpl @Throws(KeyStoreException::class, IOException::class, NoSuchAlgorithmException::class, CertificateException::class)
constructor(private val dependencyProvider: DependencyProvider) : DatabaseEncryptionHandler {

    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore")

    init {
        keyStore.load(null)
    }

    private fun generateKey(keyAlias: String): Boolean {
        try {
            // Create new key if needed
            if (!keyStore.containsAlias(keyAlias)) {
                val start = Calendar.getInstance()
                val end = Calendar.getInstance()
                end.add(Calendar.YEAR, 5)

                val keyPairGenerator = KeyPairGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")
                keyPairGenerator.initialize(
                        KeyGenParameterSpec.Builder(
                                keyAlias,
                                KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT)
                                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                                .setUserAuthenticationRequired(true)
                                .setUserAuthenticationValidityDurationSeconds(72000)
                                .build())
                val keyPair = keyPairGenerator.generateKeyPair()



                /*KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(1024);            // initialize key generator
                KeyPair keyPair = keyPairGenerator.generateKeyPair(); // generate pair of keys
                X509Certificate certificate = X509Certificate.getInstance();
                KeyPairGeneratorSpec

                keyStore.setKeyEntry("AsthmaAppEncoder", keyPair.getPublic().getEncoded(), );*/



                return true
            } else {
                return false
            }
        } catch (e: Exception) {
            return false
        }

    }

    override fun encryptPassphrase(passphrase: String, keyAlias: String): String? {
        var encryptedPassphrase: String? = null
        try {
            if (!keyStore.containsAlias(keyAlias)) {
                generateKey(keyAlias)
            }
            val privateKeyEntry = keyStore.getEntry(keyAlias, null) as KeyStore.PrivateKeyEntry
            val publicKey = privateKeyEntry.certificate.publicKey as RSAPublicKey

            // Encrypt the text

            val input = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL")
            input.init(Cipher.ENCRYPT_MODE, publicKey)

            val outputStream = ByteArrayOutputStream()
            val cipherOutputStream = CipherOutputStream(
                    outputStream, input)
            cipherOutputStream.write(passphrase.toByteArray(charset("UTF-8")))
            cipherOutputStream.close()

            val encryptedBytes = outputStream.toByteArray()
            encryptedPassphrase = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: Exception) {

        }

        return encryptedPassphrase
    }

    override fun decryptPassphrase(encryptedPassphrase: String, keyAlias: String): String? {
        var passPhrase: String? = null
        try {
            val privateKeyEntry = keyStore.getEntry(keyAlias, null) as KeyStore.PrivateKeyEntry ?: return null

//RSAPrivateKey privateKey = (RSAPrivateKey) privateKeyEntry.getPrivateKey();

            val output = Cipher.getInstance("RSA/ECB/PKCS1Padding")

            output.init(Cipher.DECRYPT_MODE, privateKeyEntry.privateKey)

            val cipherInputStream = CipherInputStream(
                    ByteArrayInputStream(Base64.decode(encryptedPassphrase, Base64.DEFAULT)), output)
            val values = ArrayList<Byte>()
            var nextByte = cipherInputStream.read()
            while (nextByte != -1) {
                values.add(nextByte.toByte())
                nextByte = cipherInputStream.read()
            }

            val bytes = ByteArray(values.size)
            for (i in bytes.indices) {
                bytes[i] = values[i].toByte()
            }

            passPhrase = String(bytes, 0, bytes.size, Charset.forName("UTF-8"))
        } catch (e: Exception) {
            val s = e.message
            val i = 10
        }

        return passPhrase
    }
}
