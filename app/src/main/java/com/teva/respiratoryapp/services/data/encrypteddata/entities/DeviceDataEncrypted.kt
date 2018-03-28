//
// DeviceDataEncrypted.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.services.data.encrypteddata.entities

import com.teva.respiratoryapp.services.data.encrypteddata.EncryptedEntity

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

/**
 * This class represents device information stored in the database.
 */
class DeviceDataEncrypted : EncryptedEntity() {

    /**
     * The expiration date of the device.
     */
    var expirationDate: LocalDate?
        get() = getLocalDateProperty("expirationDate")
        set(expirationDate) = setLocalDateProperty("expirationDate", expirationDate)

    /**
     * The time of the last activity with the device.
     */
    var lastConnection: Instant?
        get() = getInstantProperty("lastConnection")
        set(lastConnection) = setInstantProperty("lastConnection", lastConnection)

    /**
     * The hardware revision
     */
    var hardwareRevision: String
        get() = getStringProperty("hardwareRevision")
        set(hardwareRevision) {
            schemaMap.put("hardwareRevision", hardwareRevision)
        }

    /**
     * The software revision
     */
    var softwareRevision: String
        get() = getStringProperty("softwareRevision")
        set(softwareRevision) {
            schemaMap.put("softwareRevision", softwareRevision)
        }

    /**
     * The date code
     */
    var dateCode: String
        get() = getStringProperty("dateCode")
        set(dateCode) {
            schemaMap.put("dateCode", dateCode)
        }

    /**
     * The initial dose capacity of the Device.
     */
    var doseCount: Int
        get() = getIntProperty("doseCount")
        set(doseCount) {
            schemaMap.put("doseCount", doseCount)
        }

    /**
     * A value indicating whether the device is currently active or has been deleted.
     */
    var isActive: Int
        get() = getIntProperty("isActive")
        set(isActive) {
            schemaMap.put("isActive", isActive)
        }

    /**
     * The id of the last record read from the device.
     */
    var lastRecordId: Int
        get() = getIntProperty("lastRecordId")
        set(lastRecordId) {
            schemaMap.put("lastRecordId", lastRecordId)
        }

    /**
     * The lot code.
     */
    var lotCode: String
        get() = getStringProperty("lotCode")
        set(lotCode) {
            schemaMap.put("lotCode", lotCode)
        }

    /**
     * The name of the device manufacturer.
     */
    var manufacturerName: String
        get() = getStringProperty("manufacturerName")
        set(manufacturerName) {
            schemaMap.put("manufacturerName", manufacturerName)
        }

    /**
     * The device nickname.
     */
    var nickname: String
        get() = getStringProperty("nickname")
        set(nickname) {
            schemaMap.put("nickname", nickname)
        }

    /**
     * The number of doses remaining before the device is empty.
     */
    var remainingDoseCount: Int
        get() = getIntProperty("remainingDoseCount")
        set(remainingDoseCount) {
            schemaMap.put("remainingDoseCount", remainingDoseCount)
        }

    /**
     * The serial number of the device.
     */
    var serialNumber: String
        get() = getStringProperty("serialNumber")
        set(serialNumber) {
            schemaMap.put("serialNumber", serialNumber)
        }

    /**
     * The authentication key used to authentication the device.
     */
    var sharedKey: String
        get() = getStringProperty("sharedKey")
        set(sharedKey) {
            schemaMap.put("sharedKey", sharedKey)
        }

    /**
     * The nickname type.
     */
    var inhalerNameType: Int
        get() = getIntProperty("inhalerNameType")
        set(inhalerNameType) {
            schemaMap.put("inhalerNameType", inhalerNameType)
        }

    /**
     * This property is the difference in seconds between the real server time and the local device time setting.
     */
    var serverTimeOffset: Int?
        get() = getNullableIntProperty("serverTimeOffset")
        set(newValue) {
            schemaMap.put("serverTimeOffset", newValue)
        }

    /**
     * The medication of the device.
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
        set(medication) {
            var primaryKey: Int? = null
            if (medication != null) {
                primaryKey = medication.primaryKeyId
            }
            schemaMap.put("medication", primaryKey)
        }

}
