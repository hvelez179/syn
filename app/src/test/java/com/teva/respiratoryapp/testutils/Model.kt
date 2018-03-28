//
// Model.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.testutils

import com.teva.devices.entities.ConnectionMeta
import com.teva.devices.entities.Device
import com.teva.devices.entities.InhaleEvent
import com.teva.devices.enumerations.InhalerNameType
import com.teva.environment.entities.DailyAirQuality
import com.teva.environment.enumerations.AirQuality
import com.teva.medication.entities.Medication
import com.teva.medication.entities.Prescription
import com.teva.medication.enumerations.MedicationClassification
import com.teva.notifications.entities.ReminderSetting
import com.teva.notifications.enumerations.RepeatType
import com.teva.userfeedback.entities.DailyUserFeeling
import com.teva.userfeedback.enumerations.UserFeeling

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

/**
 * Helper class to create EncryptedEntity objects for testing.
 */
object Model {
    fun Prescription(inhalesPerDose: Int,
                     dosesPerDay: Int,
                     prescriptionDate: Instant,
                     drugUID: String,
                     hasChanged: Boolean,
                     changedTime: Instant): Prescription {

        val model = Prescription()
        model.inhalesPerDose = inhalesPerDose
        model.dosesPerDay = dosesPerDay
        model.prescriptionDate = prescriptionDate
        model.hasChanged = hasChanged
        model.changeTime = changedTime
        model.serverTimeOffset = 0

        val medication = Medication()
        medication.drugUID = drugUID

        return model
    }

    fun Medication(drugUID: String,
                   brandName: String,
                   genericName: String,
                   medicationClassification: MedicationClassification,
                   minimumDoseInterval: Int,
                   minimumScheduleInterval: Int,
                   overdoseInhalationCount: Int,
                   initialDoseCount: Int,
                   numberOfMonthsBeforeExpiration: Int,
                   hasChanged: Boolean,
                   changedTime: Instant): Medication {

        val model = Medication()
        model.drugUID = drugUID
        model.brandName = brandName
        model.genericName = genericName
        model.medicationClassification = medicationClassification
        model.minimumDoseInterval = minimumDoseInterval
        model.minimumScheduleInterval = minimumScheduleInterval
        model.overdoseInhalationCount = overdoseInhalationCount
        model.initialDoseCount = initialDoseCount
        model.numberOfMonthsBeforeExpiration = numberOfMonthsBeforeExpiration
        model.hasChanged = hasChanged
        model.changeTime = changedTime
        model.serverTimeOffset = 0

        return model
    }

    fun Device(isActive: Boolean,
               dateCode: String,
               doseCount: Int,
               expirationDate: LocalDate,
               hardwareRevision: String,
               softwareRevision: String,
               inhalerNameType: InhalerNameType,
               lastConnection: Instant,
               lotCode: String,
               lastRecordId: Int,
               manufacturerName: String,
               nickname: String,
               remainingDoseCount: Int,
               serialNumber: String,
               authenticationKey: String,
               hasChanged: Boolean,
               changedTime: Instant,
               drugUID: String): Device {
        val model = Device()
        model.isActive = isActive
        model.dateCode = dateCode
        model.doseCount = doseCount
        model.expirationDate = expirationDate
        model.hardwareRevision = hardwareRevision
        model.softwareRevision = softwareRevision
        model.inhalerNameType = inhalerNameType
        model.lastConnection = lastConnection
        model.lotCode = lotCode
        model.lastRecordId = lastRecordId
        model.manufacturerName = manufacturerName
        model.nickname = nickname
        model.remainingDoseCount = remainingDoseCount
        model.serialNumber = serialNumber
        model.authenticationKey = authenticationKey
        model.changeTime = changedTime
        model.hasChanged = hasChanged
        model.serverTimeOffset = 0

        val medication = Medication()
        medication.drugUID = drugUID
        model.medication = medication

        return model
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
                    isValidInhale: Boolean,
                    hasChanged: Boolean,
                    changedTime: Instant,
                    deviceSerialNumber: String): InhaleEvent {
        val model = InhaleEvent()
        model.eventUID = eventUID
        model.eventTime = eventTime
        model.timezoneOffsetMinutes = timezoneOffset
        model.inhaleEventTime = inhaleEventTime

        model.inhaleDuration = duration
        model.inhalePeak = inhalePeak
        model.inhaleTimeToPeak = inhaleTimeToPeak
        model.status = status

        model.closeTime = closeTime
        model.doseId = doseId
        model.cartridgeUID = cartridgeUID
        model.upperThresholdTime = upperThresholdTime
        model.upperThresholdDuration = upperThresholdDuration
        model.isValidInhale = isValidInhale

        model.hasChanged = hasChanged
        model.changeTime = changedTime

        model.deviceSerialNumber = deviceSerialNumber
        model.serverTimeOffset = 0

        return model
    }

    /**
     * This method is used in the unit tests to create a ConnectionMeta object.

     * @param connectionDate - the connection date.
     * *
     * @param serialNumber   - the device serial number.
     * *
     * @return - a ConnectionMeta object with the specified information.
     */
    fun ConnectionMeta(connectionDate: LocalDate, serialNumber: String): ConnectionMeta {
        val model = ConnectionMeta()
        model.connectionDate = connectionDate
        model.serialNumber = serialNumber
        return model
    }

    /**
     * This method is used in unit tests to create a DailyAirQuality object with the specified values.

     * @param airQualityIndex - the air quality index.
     * *
     * @param airQuality      - the air quality.
     * *
     * @param date            - the date to which the air quality information belongs.
     * *
     * @return -  the created DailyAirQuality object.
     */
    fun DailyAirQuality(airQualityIndex: Int, airQuality: AirQuality, date: LocalDate): DailyAirQuality {
        val model = DailyAirQuality()
        model.airQuality = airQuality
        model.airQualityIndex = airQualityIndex
        model.date = date

        return model
    }

    /**
     * This method is used by the unit tests and creates a ReminderSetting model object.

     * @param name       -  the name of the reminder.
     * *
     * @param enabled    - the flag to indicate if the reminder should be enabled.
     * *
     * @param repeatType - the repeat type of the reminder.
     * *
     * @param timeOfDay  - the time of the day for the reminder.
     * *
     * @return - the ReminderSetting object created with the specified values.
     */
    fun ReminderSetting(name: String,
                        enabled: Boolean,
                        repeatType: RepeatType,
                        timeOfDay: Int,
                        hasChanged: Boolean): ReminderSetting {
        val model = ReminderSetting()
        model.name = name
        model.isEnabled = enabled
        model.repeatType = repeatType
        model.timeOfDay = LocalTime.ofSecondOfDay(timeOfDay.toLong())
        model.serverTimeOffset = 0
        model.hasChanged = hasChanged

        return model
    }

    /**
     * This method is used in unit testing. This method creates a DailyUserFeeling
     * model object from the specified values.

     * @param date        - the daily user feeling date.
     * *
     * @param time        - the daily user feeling time.
     * *
     * @param userFeeling - the user feeling value.
     * *
     * @param hasChanged  - flag to indicate if the model has changed.
     * *
     * @param changeTime  - the change time.
     * *
     * @return - the newly created DailyUserFeeling object.
     */
    fun DailyUserFeeling(date: LocalDate,
                         time: Instant,
                         userFeeling: UserFeeling,
                         hasChanged: Boolean,
                         changeTime: Instant): DailyUserFeeling {
        val model = DailyUserFeeling()
        model.time = time
        model.date = date
        model.userFeeling = userFeeling
        model.hasChanged = hasChanged
        model.changeTime = changeTime
        model.serverTimeOffset = 0

        return model
    }
}
