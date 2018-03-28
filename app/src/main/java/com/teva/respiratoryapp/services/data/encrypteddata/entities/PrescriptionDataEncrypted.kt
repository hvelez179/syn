package com.teva.respiratoryapp.services.data.encrypteddata.entities

import com.teva.respiratoryapp.services.data.encrypteddata.EncryptedEntity

import org.threeten.bp.Instant

/**
 * This class provides prescription information.
 */
class PrescriptionDataEncrypted : EncryptedEntity() {

    /**
     * The number of doses to take in a day.
     */
    var dosesPerDay: Int
        get() = getIntProperty("dosesPerDay")
        set(value) {
            schemaMap.put("dosesPerDay", value)
        }

    /**
     * The number of inhalations that make up a dose.
     */
    var inhalesPerDose: Int
        get() = getIntProperty("inhalesPerDose")
        set(value) {
            schemaMap.put("inhalesPerDose", value)
        }

    /**
     * The date the prescription object created.
     */
    var prescriptionDate: Instant?
        get() = getInstantProperty("prescriptionDate")
        set(value) = setInstantProperty("prescriptionDate", value)

    /**
     * This property is the difference in seconds between the real server time and the local device time setting.
     */
    var serverTimeOffset: Int?
        get() = getNullableIntProperty("serverTimeOffset")
        set(newValue) {
            schemaMap.put("serverTimeOffset", newValue)
        }



    /**
     * The medication associated with the prescription.
     */
    var medication: MedicationDataEncrypted?
        get() {
            val innerValue = schemaMap["medication"]
            if (innerValue is MedicationDataEncrypted) {
                return innerValue
            } else if (innerValue is Int) {
                val med = MedicationDataEncrypted()
                med.primaryKeyId = innerValue
                return med
            }
            return null
        }
        set(entity) {
            var pk: Int? = null
            if (entity != null) {
                pk = entity.primaryKeyId
            }

            schemaMap.put("medication", pk)
        }
}
