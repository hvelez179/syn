//
// ConsentDataEncrypted.kt
// app
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.services.data.encrypteddata.entities

import com.teva.respiratoryapp.services.data.encrypteddata.EncryptedEntity
import org.threeten.bp.Instant


/**
 * Represents consent data stored in the database.
 */
class ConsentDataEncrypted : EncryptedEntity() {
    override var isNew: Boolean = true

    var hasConsented: Int
        get() = getIntProperty("hasConsented")
        set(newValue) {
            schemaMap.put("hasConsented", newValue)
        }

    var status: String?
        get() =  getStringProperty("status")
        set(newValue) {
            schemaMap.put("status", newValue)
        }

    var termsAndConditions: String?
        get() =  getStringProperty("termsAndConditions")
        set(newValue) {
            schemaMap.put("termsAndConditions", newValue)
        }

    var privacyNotice: String?
        get() = getStringProperty("privacyNotice")
        set(newValue) {
            schemaMap.put("privacyNotice", newValue)
        }

    var consentStartDate: Instant?
        get() = getInstantProperty("consentStartDate")
        set(newValue) = setInstantProperty("consentStartDate", newValue)

    var consentEndDate: Instant?
        get() = getInstantProperty("consentEndDate")
        set(newValue) = setInstantProperty("consentEndDate", newValue)

    var addressCountry: String?
        get() = getStringProperty("addressCountry")
        set(newValue) {
            schemaMap.put("addressCountry", newValue)
        }

    var patientDOB: Instant?
        get() = getInstantProperty("patientDOB")
        set(newValue) = setInstantProperty("patientDOB", newValue)

}