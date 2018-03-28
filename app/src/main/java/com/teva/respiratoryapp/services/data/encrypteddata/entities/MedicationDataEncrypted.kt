package com.teva.respiratoryapp.services.data.encrypteddata.entities

import com.teva.respiratoryapp.services.data.encrypteddata.EncryptedEntity

/**
 * Represents medication information stored in the database.
 */
class MedicationDataEncrypted : EncryptedEntity() {

    /**
     * The brand name of the medication.
     */
    var brandName: String
        get() = getStringProperty("brandName")
        set(value) {
            schemaMap.put("brandName", value)
        }

    /**
     * The id of the medication.
     */
    var drugUID: String
        get() = getStringProperty("drugUID")
        set(value) {
            schemaMap.put("drugUID", value)
        }

    /**
     * The generic name of the medication.
     */
    var genericName: String
        get() = getStringProperty("genericName")
        set(value) {
            schemaMap.put("genericName", value)
        }

    /**
     * The classification of the medication.
     */
    var medicationClassification: Int
        get() = getIntProperty("medicationClassification")
        set(value) {
            schemaMap.put("medicationClassification", value)
        }

    /**
     * The minimum dose interval of the medication.
     */
    var minimumDoseInterval: Int
        get() = getIntProperty("minimumDoseInterval")
        set(value) {
            schemaMap.put("minimumDoseInterval", value)
        }

    /**
     * The minimum schedule interval of the medication.
     */
    var minimumScheduleInterval: Int
        get() = getIntProperty("minimumScheduleInterval")
        set(value) {
            schemaMap.put("minimumScheduleInterval", value)
        }

    /**
     * The overdose inhalation count of the medication.
     */
    var overdoseInhalationCount: Int
        get() = getIntProperty("overdoseInhalationCount")
        set(value) {
            schemaMap.put("overdoseInhalationCount", value)
        }

    /**
     * The expiration period, in months, of the medication.
     */
    var numberOfMonthsBeforeExpiration: Int
        get() = getIntProperty("numberOfMonthsBeforeExpiration")
        set(value) {
            schemaMap.put("numberOfMonthsBeforeExpiration", value)
        }
}
