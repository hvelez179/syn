//
// DatabaseInfoEncrypted.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.services.data.encrypteddata.entities

import com.teva.respiratoryapp.services.data.encrypteddata.EncryptedEntity

import org.threeten.bp.Instant

/**
 * Represents the database name, version, and date modified.
 */
class DatabaseInfoEncrypted : EncryptedEntity() {
    /**
     * The version of the database.
     */
    var version: String
        get() = getStringProperty("version")
        set(value) {
            schemaMap.put("version", value)
        }

    /**
     * The name of the database.
     */
    var databaseName: String
        get() = getStringProperty("databaseName")
        set(value) {
            schemaMap.put("databaseName", value)
        }

    /**
     * The date when the database schema was last changed.
     */
    var dateModified: Instant?
        get() = getInstantProperty("dateModified")
        set(value) = setInstantProperty("dateModified", value)
}
