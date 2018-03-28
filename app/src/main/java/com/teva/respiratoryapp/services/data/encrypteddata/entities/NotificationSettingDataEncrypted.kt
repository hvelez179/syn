//
// NotificationSettingDataEncrypted.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.services.data.encrypteddata.entities

import com.teva.respiratoryapp.services.data.encrypteddata.EncryptedEntity

/**
 * This class represents the notification setting information stored in the database.
 */

class NotificationSettingDataEncrypted : EncryptedEntity() {

    /**
     * The notification enabled flag from the schema map.
     */
    var isEnabled: Int
        get() = getIntProperty("enabled")
        set(enabled) {
            schemaMap.put("enabled", enabled)
        }

    /**
     * The name of the notification from the schema map.
     */
    var name: String
        get() = getStringProperty("name")
        set(name) {
            schemaMap.put("name", name)
        }

    /**
     * The repeat type from the schema map.
     */
    var repeatType: Int
        get() = getIntProperty("repeatType")
        set(repeatType) {
            schemaMap.put("repeatType", repeatType)
        }

    /**
     * The repeat type data from the schema map.
     */
    var repeatTypeData: Int
        get() = getIntProperty("repeatTypeData")
        set(repeatTypeData) {
            schemaMap.put("repeatTypeData", repeatTypeData)
        }

    /**
     * This property is the difference in seconds between the real server time and the local device time setting.
     */
    var serverTimeOffset: Int?
        get() = getNullableIntProperty("serverTimeOffset")
        set(newValue) {
            schemaMap.put("serverTimeOffset", newValue)
        }


}
