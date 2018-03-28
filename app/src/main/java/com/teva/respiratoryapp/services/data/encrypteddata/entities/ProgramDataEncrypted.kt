//
// ProgramDataEncrypted.kt
// app
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.services.data.encrypteddata.entities

import com.teva.respiratoryapp.services.data.encrypteddata.EncryptedEntity


/**
 * Represents program data stored in the database.
 */
class ProgramDataEncrypted : EncryptedEntity() {
    override var isNew: Boolean = true

    var programName: String?
        get() = getStringProperty("programName")
        set(newValue) {
            schemaMap.put("programName", newValue)
        }


    var programId: String?
        get() =  getStringProperty("programId")
        set(newValue) {
            schemaMap.put("programId", newValue)
        }

    var profileId: String?
        get() =  getStringProperty("profileId")
        set(newValue) {
            schemaMap.put("profileId", newValue)
        }

    var invitationCode: String?
        get() =  getStringProperty("invitationCode")
        set(newValue) {
            schemaMap.put("invitationCode", newValue)
        }

    var consentedApps: String?
        get() =  getStringProperty("consentedApps")
        set(newValue) {
            schemaMap.put("consentedApps", newValue)
        }
}