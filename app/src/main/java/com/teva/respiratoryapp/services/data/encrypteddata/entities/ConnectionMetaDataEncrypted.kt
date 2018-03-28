//
// ConnectionMetaDataEncrypted.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.services.data.encrypteddata.entities

import com.teva.respiratoryapp.services.data.encrypteddata.EncryptedEntity

import org.threeten.bp.LocalDate

/**
 * Represents device connection information stored in the database.
 */

class ConnectionMetaDataEncrypted : EncryptedEntity() {

    /**
     * The connection date information stored in the schema map.
     */
    var connectionDate: LocalDate?
        get() = getLocalDateProperty("connectionDate")
        set(connectionDate) = setLocalDateProperty("connectionDate", connectionDate)

    /**
     * A DeviceDataEncrypted object representing the device
     * which was connected, from the schema map.
     */
    var device: DeviceDataEncrypted?
        get() {
            val innerValue = schemaMap["device"]
            if (innerValue is DeviceDataEncrypted) {
                return innerValue
            } else if (innerValue is Int) {
                val device = DeviceDataEncrypted()
                device.primaryKeyId = innerValue
                return device
            }
            return null
        }
        set(device) {
            schemaMap.put("device", device!!.primaryKeyId)
        }
}
