package com.teva.respiratoryapp.services.data.encrypteddata

import android.annotation.TargetApi
import android.content.Context
import android.security.keystore.KeyProperties
import com.teva.utilities.services.DependencyProvider
import java.util.*
import javax.crypto.Cipher
import android.security.keystore.KeyGenParameterSpec
import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.*
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import android.os.Build
import com.teva.utilities.utilities.Logger
import java.nio.charset.Charset
import java.security.spec.RSAKeyGenParameterSpec
import java.security.spec.RSAKeyGenParameterSpec.F4


/**
 * This class provides functionality for encrypting and decrypting data
 * using keys stored in the key store.
 */
class EncryptionHandler(dependencyProvider: DependencyProvider) {

    private val RSA_MODE = "RSA/ECB/PKCS1Padding"
    private val AndroidKeyStore = "AndroidKeyStore"
    private val AndroidOpenSSL = "AndroidOpenSSL"
    private val keyAlias = "EncryptionKey"
    private val keyStore: KeyStore
    private val context: Context = dependencyProvider.resolve<Context>()
    private val logger = Logger(EncryptionHandler::class)

    init {
        keyStore = KeyStore.getInstance(AndroidKeyStore)
        keyStore.load(null)
        checkAndGenerateRSAKeyPair()
    }

    /**
     * This function checks and generates the asymmetric key pair
     * used for encryption if it does not already exist.
     */
    private fun checkAndGenerateRSAKeyPair() {
        // Generate the RSA key pairs
        if (!keyStore.containsAlias(keyAlias)) {
            createKeys()

        }
    }
/**
     * This function creates keys for encryption and decryption on Marshmallow
     * and higher versions. It uses KeyGenParameterSpec which is available in Api 23
     * and higher.
     */
    @TargetApi(Build.VERSION_CODES.M)
    @Throws(NoSuchProviderException::class, NoSuchAlgorithmException::class, InvalidAlgorithmParameterException::class)
    private fun createKeys() {
        logger.log(Logger.Level.INFO, "Creating key for Api >= 23")
        val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA, AndroidKeyStore)
        keyPairGenerator.initialize(
                KeyGenParameterSpec.Builder(
                        keyAlias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                        .setAlgorithmParameterSpec(RSAKeyGenParameterSpec(1024, F4))
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                        .setUserAuthenticationRequired(false)
                        .build())
        keyPairGenerator.generateKeyPair()
    }


    /**
     * This function encrypts the passed data using the public key
     * and returns the encrypted data.
     */
    @Throws(Exception::class)
    private fun rsaEncrypt(inputBytes: ByteArray): ByteArray {
        val privateKeyEntry = getPrivateKey() ?: return ByteArray(0)
        // Encrypt the text
        val inputCipher = Cipher.getInstance(RSA_MODE, AndroidOpenSSL)
        inputCipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.certificate.publicKey)

        val outputStream = ByteArrayOutputStream()
        val cipherOutputStream = CipherOutputStream(outputStream, inputCipher)
        cipherOutputStream.write(inputBytes)
        cipherOutputStream.close()

        val encryptedBytes = outputStream.toByteArray()
        return encryptedBytes
    }

    /**
     * This function decrypts the passed data using the private key
     * and returns the decrypted data.
     */
    @Throws(Exception::class)
    private fun rsaDecrypt(encryptedBytes: ByteArray): ByteArray {
        val privateKeyEntry = getPrivateKey() ?: return ByteArray(0)

        val output = Cipher.getInstance(RSA_MODE)
        output.init(Cipher.DECRYPT_MODE, privateKeyEntry.privateKey)
        val cipherInputStream = CipherInputStream(
                ByteArrayInputStream(encryptedBytes), output)
        val values = ArrayList<Byte>()
        var nextByte = cipherInputStream.read()
        while (nextByte != -1) {
            values.add(nextByte.toByte())
            nextByte = cipherInputStream.read()
        }

        return values.toByteArray()
    }

    /**
     * This function retrieves the key for encryption and decryption.
     */
    private fun getPrivateKey(): KeyStore.PrivateKeyEntry? {

        // After an upgrade, the keystore might be locked
        // and might throw an exception when trying to
        // retrieve the key. This scenario requires a retry
        // as the key is available the second time.
        var retryCount = 2
        var privateKeyEntry: KeyStore.PrivateKeyEntry? = null

        while (retryCount > 0 && privateKeyEntry == null) {
            try {
                retryCount--
                privateKeyEntry = keyStore.getEntry(keyAlias, null) as KeyStore.PrivateKeyEntry
            } catch(ex: Exception) {
                logger.logException(Logger.Level.ERROR, "Failed to retrieve encryption key", ex)
            }
        }

        return privateKeyEntry
    }

    /**
     * This function encrypts the passed string and returns the encrypted string.
     */
    fun encrypt(input: String): String {
        val encodedBytes = rsaEncrypt(input.toByteArray())
        val encryptedBase64Encoded = Base64.encodeToString(encodedBytes, Base64.DEFAULT)
        return encryptedBase64Encoded
    }


    /**
     * This function decrypts the passed string and returns the decrypted string.
     */
    fun decrypt(encrypted: String): String {
        val decodedBytes = rsaDecrypt(Base64.decode(encrypted, 0))
        return String(decodedBytes, Charset.forName("UTF-8"))
    }

}