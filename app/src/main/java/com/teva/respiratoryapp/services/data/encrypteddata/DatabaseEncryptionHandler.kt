//
// DatabaseEncryptionHandler.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.services.data.encrypteddata

/**
 * This interface defines methods for handling the password used for database encryption.
 */

interface DatabaseEncryptionHandler {
    fun encryptPassphrase(passphrase: String, keyAlias: String): String?
    fun decryptPassphrase(encryptedPassphrase: String, keyAlias: String): String?
}
