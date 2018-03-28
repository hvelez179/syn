//
// Entity.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.testutils

import com.teva.respiratoryapp.services.data.encrypteddata.entities.ConnectionMetaDataEncrypted
import com.teva.respiratoryapp.services.data.encrypteddata.entities.DailyAirQualityDataEncrypted
import com.teva.respiratoryapp.services.data.encrypteddata.entities.DailyUserFeelingDataEncrypted
import com.teva.respiratoryapp.services.data.encrypteddata.entities.DeviceDataEncrypted
import com.teva.respiratoryapp.services.data.encrypteddata.entities.InhaleEventDataEncrypted
import com.teva.respiratoryapp.services.data.encrypteddata.entities.MedicationDataEncrypted
import com.teva.respiratoryapp.services.data.encrypteddata.entities.NotificationSettingDataEncrypted
import com.teva.respiratoryapp.services.data.encrypteddata.entities.PrescriptionDataEncrypted
import com.teva.devices.enumerations.InhalerNameType
import com.teva.medication.enumerations.MedicationClassification
import com.teva.respiratoryapp.models.dataquery.encrypted.EncryptedInhaleEventMapper

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset

/**
 * Helper class to create EncryptedEntity objects for testing.
 */
object Entity {
    fun Prescription(isNew: Boolean,
                     inhalesPerDose: Int,
                     dosesPerDay: Int,
                     prescriptionDate: Instant,
                     medicationPrimaryKey: Int,
                     hasChanged: Boolean,
                     changedTime: Instant): PrescriptionDataEncrypted {
        val entity = PrescriptionDataEncrypted()
        entity.isNew = isNew
        entity.inhalesPerDose = inhalesPerDose
        entity.dosesPerDay = dosesPerDay
        entity.prescriptionDate = prescriptionDate
        entity.hasChanged = if (hasChanged) 1 else 0
        entity.changedTime = changedTime
        val medicationEntity = MedicationDataEncrypted()
        medicationEntity.primaryKeyId = medicationPrimaryKey
        entity.medication = medicationEntity
        entity.serverTimeOffset = 0

        return entity
    }

    fun Medication(isNew: Boolean,
                   drugUID: String,
                   brandName: String,
                   genericName: String,
                   medicationClassification: MedicationClassification,
                   minimumDoseInterval: Int,
                   minimumScheduleInterval: Int,
                   overdoseInhalationCount: Int,
                   initialDoseCount: Int,
                   numberOfMonthsBeforeExpiration: Int,
                   hasChanged: Boolean,
                   changedTime: Instant): MedicationDataEncrypted {
        val entity = MedicationDataEncrypted()
        entity.isNew = isNew
        entity.drugUID = drugUID
        entity.brandName = brandName
        entity.genericName = genericName
        entity.medicationClassification = medicationClassification.value
        entity.minimumDoseInterval = minimumDoseInterval
        entity.minimumScheduleInterval = minimumScheduleInterval
        entity.overdoseInhalationCount = overdoseInhalationCount
        //entity.initialDoseCount = initialDoseCount
        entity.numberOfMonthsBeforeExpiration = numberOfMonthsBeforeExpiration
        entity.hasChanged = if (hasChanged) 1 else 0
        entity.changedTime = changedTime

        return entity
    }

    fun Device(isActive: Boolean,
               dateCode: String,
               doseCount: Int,
               expirationDate: LocalDate,
               hardwareRevision: String,
               softwareRevision: String,
               inhalerNameType: InhalerNameType,
               lastConnection: Instant?,
               lotCode: String,
               lastRecordId: Int,
               manufacturerName: String,
               nickname: String,
               remainingDoseCount: Int,
               serialNumber: String,
               authenticationKey: String,
               hasChanged: Boolean,
               changedTime: Instant,
               medicationPrimaryKey: Int): DeviceDataEncrypted {
        val entity = DeviceDataEncrypted()
        entity.isActive = if (isActive) 1 else 0
        entity.dateCode = dateCode
        entity.doseCount = doseCount
        entity.expirationDate = expirationDate
        entity.hardwareRevision = hardwareRevision
        entity.softwareRevision = softwareRevision
        entity.inhalerNameType = inhalerNameType.ordinal
        entity.lastConnection = lastConnection
        entity.lotCode = lotCode
        entity.lastRecordId = lastRecordId
        entity.manufacturerName = manufacturerName
        entity.nickname = nickname
        entity.remainingDoseCount = remainingDoseCount
        entity.serialNumber = serialNumber
        entity.sharedKey = authenticationKey
        entity.changedTime = changedTime
        entity.hasChanged = if (hasChanged) 1 else 0
        entity.serverTimeOffset = 0

        val medicationEntity = MedicationDataEncrypted()
        medicationEntity.primaryKeyId = medicationPrimaryKey
        entity.medication = medicationEntity

        return entity
    }

    fun InhaleEvent(eventUID: Int,
                    eventTime: Instant,
                    timezoneOffset: Int,
                    inhaleEventTime: Int,
                    duration: Int,
                    inhalePeak: Int,
                    inhaleTimeToPeak: Int,
                    status: Int,
                    closeTime: Int,
                    doseId: Int,
                    cartridgeUID: String,
                    upperThresholdTime: Int,
                    upperThresholdDuration: Int,
                    isValidInhale: Int,
                    hasChanged: Int,
                    changeTime: Instant,
                    deviceSerialNumber: String,
                    devicePrimaryKeyId: Int): InhaleEventDataEncrypted {
        val entity = InhaleEventDataEncrypted()
        entity.eventUID = eventUID

        // Inhale dose event time is stored as normalizedDate, time of day and timezone in the database
        entity.eventTime = eventTime
        entity.timezoneOffset = timezoneOffset

        entity.inhaleEventTime = inhaleEventTime
        entity.duration = duration
        entity.inhalePeak = inhalePeak
        entity.inhaleTimeToPeak = inhaleTimeToPeak
        entity.status = status
        entity.closeTime = closeTime
        entity.doseId = doseId
        entity.cartridgeUID = cartridgeUID
        entity.upperThresholdTime = upperThresholdTime
        entity.upperThresholdDuration = upperThresholdDuration
        entity.isValidInhale = isValidInhale

        entity.changedTime = changeTime
        entity.hasChanged = hasChanged
        entity.serverTimeOffset = 0

        val device = DeviceDataEncrypted()
        device.serialNumber = deviceSerialNumber
        device.primaryKeyId = devicePrimaryKeyId
        entity.device = device

        val timezoneOffsetMinutes = timezoneOffset
        val zoneOffset = ZoneOffset.ofHoursMinutes(timezoneOffsetMinutes / 60,
                Math.abs(timezoneOffsetMinutes) % 60)
        entity.date = eventTime.atOffset(zoneOffset).toLocalDate()

        return entity
    }

    /**
     * This method is used in the unit tests for creating a ConnectionMetaDataEncrypted object.

     * @param connectionDate - the connection date.
     * *
     * @param device         - the device serial number.
     * *
     * @return - a ConnectionMetaDataEncrypted object.
     */
    fun ConnectionMeta(connectionDate: LocalDate, device: DeviceDataEncrypted): ConnectionMetaDataEncrypted {
        val entity = ConnectionMetaDataEncrypted()
        entity.connectionDate = connectionDate
        entity.device = device
        return entity
    }

    /**
     * This is a helper method used in unit tests for creating a DailyAirQuality entity object.

     * @param airQualityIndex - the airQualityIndex.
     * *
     * @param airQuality      - the airQuality
     * *
     * @param date            - the date associated with the DailyAirQuality information.
     * *
     * @return - the DailyAirQuality entity object.
     */
    fun DailyAirQuality(airQualityIndex: Int, airQuality: Int, date: LocalDate): DailyAirQualityDataEncrypted {
        val entity = DailyAirQualityDataEncrypted()
        entity.airQuality = airQuality
        entity.airQualityIndex = airQualityIndex
        entity.date = date

        return entity
    }

    /**
     * This method is used by the unit tests and creates a NotificationSettingDataEncrypted object.

     * @param name           - the name of the notification setting.
     * *
     * @param enabled        -  the flag to indicate if the notification setting should be enabled.
     * *
     * @param repeatType     - the repeat type.
     * *
     * @param repeatTypeData - the repeat type data.
     * *
     * @return - the NotificationSettingDataEncrypted object created with the specified values.
     */
    fun NotificationSettingDataEncrypted(name: String,
                                         enabled: Int,
                                         repeatType: Int,
                                         repeatTypeData: Int,
                                         hasChanged: Boolean): NotificationSettingDataEncrypted {
        val entity = NotificationSettingDataEncrypted()
        entity.name = name
        entity.isEnabled = enabled
        entity.repeatType = repeatType
        entity.repeatTypeData = repeatTypeData
        entity.serverTimeOffset = 0
        entity.hasChanged = if(hasChanged) 1 else 0

        return entity
    }

    /**
     * This method creates a DailyUserFeelingDataEncrypted object from the
     * specified values. This method is used in unit testing.
     *
     * @param date        - the daily user feeling date.
     * @param time        - the daily user feeling time.
     * @param userFeeling - the user feeling value.
     * @param hasChanged  - flag to indicate if the model has changed.
     * @param changeTime  - the change time.
     * @return - the newly created DailyUserFeelingDataEncrypted objects.
     */
    fun DailyUserFeeling(date: LocalDate,
                         time: Instant,
                         userFeeling: Int,
                         hasChanged: Int,
                         changeTime: Instant): DailyUserFeelingDataEncrypted {
        val entity = DailyUserFeelingDataEncrypted()
        entity.time = time
        entity.date = date
        entity.userFeeling = userFeeling
        entity.hasChanged = hasChanged
        entity.changedTime = changeTime
        entity.serverTimeOffset = 0

        return entity
    }
}
