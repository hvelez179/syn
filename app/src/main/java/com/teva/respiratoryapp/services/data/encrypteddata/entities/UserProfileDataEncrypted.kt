//
// UserProfileDataEncrypted.kt
// app
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.services.data.encrypteddata.entities

import com.teva.respiratoryapp.services.data.encrypteddata.EncryptedEntity
import org.threeten.bp.Instant


/**
 * Represents user profile data stored in the database.
 */
class UserProfileDataEncrypted : EncryptedEntity() {

    // Properties

    var firstName: String?
        get() =  getStringProperty("firstName")
        set(newValue) {
            schemaMap.put("firstName", newValue)
        }

    var lastName: String?
        get() = getStringProperty("lastName")
        set(newValue) {
            schemaMap.put("lastName", newValue)
        }

    var profileId: String?
        get() = getStringProperty("profileId")
        set(newValue) {
            schemaMap.put("profileId", newValue)
        }

    var isAccountOwner: Int?
        get() = getIntProperty("isAccountOwner")
        set(newValue) {
            schemaMap.put("isAccountOwner", newValue)
        }

    var isActive: Int?
        get() = getIntProperty("isActive")
        set(newValue) {
            schemaMap.put("isActive", newValue)
        }

    var dateOfBirth: Instant?
        get() = getInstantProperty("dateOfBirth")
        set(newValue) = setInstantProperty("dateOfBirth", newValue)

    var isEmancipated: Int?
        get() = getIntProperty("isEmancipated")
        set(newValue) {
            schemaMap.put("isEmancipated", newValue)
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