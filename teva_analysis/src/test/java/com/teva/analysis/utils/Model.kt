//
// Model.kt
// teva_analysis
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.analysis.utils

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

    /**
     * This method is used in unit tests to create a Prescription object.
     *
     * @param inhalesPerDose - the number of inhales per dose.
     * @param dosesPerDay - the number of doses per day.
     * @param prescriptionDate - the prescription date.
     * @param drugUID - the drug UID.
     * @return - the Prescription object created with the data passed to the method.
     */
    fun Prescription(inhalesPerDose: Int,
                     dosesPerDay: Int,
                     prescriptionDate: Instant,
                     drugUID: String): Prescription {

        val medication = Medication()
        medication.drugUID = drugUID

        val model = Prescription(
                dosesPerDay,
                inhalesPerDose,
                prescriptionDate,
                medication)



        return model
    }

    /**
     * This method creates a Medication object to be used in unit tests.
     *
     * @param drugUID - the drug UID.
     * @param brandName -  the brand name of the medication.
     * @param genericName - the generic name of the medication.
     * @param medicationClassification - the medication classification (controller / reliever).
     * @param minimumDoseInterval - the minimum dose interval.
     * @param minimumScheduleInterval - the minimum schedule interval.
     * @param overdoseInhalationCount - the overdose inhalation count.
     * @param initialDoseCount - the initial number of doses.
     * @param numberOfMonthsBeforeExpiration - the number of months before expiration.
     * @return - the Medication object created with the data passed to the method.
     */
    fun Medication(drugUID: String,
                   brandName: String,
                   genericName: String,
                   medicationClassification: MedicationClassification,
                   minimumDoseInterval: Int,
                   minimumScheduleInterval: Int,
                   overdoseInhalationCount: Int,
                   initialDoseCount: Int,
                   numberOfMonthsBeforeExpiration: Int): Medication {

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

        return model
    }

    /**
     * This method creates an InhaleEvent object to be used in unit tests.
     *
     * @param eventUID - the event UID.
     * @param openTime - the open time.
     * @param timezoneOffset - the time zone offset.
     * @param eventTime - the event time.
     * @param duration - the duration.
     * @param inhalePeak - the inhale peak.
     * @param inhaleTimeToPeak - the time to reach peak inhalation.
     * @param status - the status of the inhalation.
     * @param closeTime - the close time.
     * @param doseId - the dose Id.
     * @param cartridgeUID - the cartridge UID.
     * @param upperThresholdTime - the upper threshold time.
     * @param upperThresholdDuration - the upper threshold duration.
     * @param isValidInhale - flag to indicate if the inhalation is valid.
     * @param deviceSerialNumber - the device serial number.
     * @return - the InhaleEvent object created with the data passed to the method.
     */
    fun InhaleEvent(eventUID: Int,
                    openTime: Instant,
                    timezoneOffset: Int,
                    eventTime: Int,
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
                    deviceSerialNumber: String): InhaleEvent {
        val model = InhaleEvent()
        model.eventUID = eventUID
        model.eventTime = openTime
        model.timezoneOffsetMinutes = timezoneOffset
        model.inhaleEventTime = eventTime

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

        model.deviceSerialNumber = deviceSerialNumber

        return model
    }

    /**
     * This method is used in the unit tests to create a ConnectionMeta object.
     *
     * @param connectionDate - the connection date.
     * @param serialNumber   - the device serial number.
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
     *
     * @param airQualityIndex - the air quality index.
     * @param airQuality      - the air quality.
     * @param date            - the date to which the air quality information belongs.
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
     *
     * @param name       -  the name of the reminder.
     * @param enabled    - the flag to indicate if the reminder should be enabled.
     * @param repeatType - the repeat type of the reminder.
     * @param timeOfDay  - the time of the day for the reminder.
     * @return - the ReminderSetting object created with the specified values.
     */
    fun ReminderSetting(name: String,
                        enabled: Boolean,
                        repeatType: RepeatType,
                        timeOfDay: Int): ReminderSetting {
        val model = ReminderSetting()
        model.name = name
        model.isEnabled = enabled
        model.repeatType = repeatType
        model.timeOfDay = LocalTime.ofSecondOfDay(timeOfDay.toLong())

        return model
    }

    /**
     * This method is used in unit testing. This method creates a DailyUserFeeling
     * model object from the specified values.
     *
     * @param date        - the daily user feeling date.
     * @param time        - the daily user feeling time.
     * @param userFeeling - the user feeling value.
     * @return - the newly created DailyUserFeeling object.
     */
    fun DailyUserFeeling(date: LocalDate,
                         time: Instant,
                         userFeeling: UserFeeling): DailyUserFeeling {
        val model = DailyUserFeeling()
        model.time = time
        model.date = date
        model.userFeeling = userFeeling

        return model
    }

    /**
     * This method creates a Device object from the passed parameters.
     *
     * @param isActive           - indicates if the device is active.
     * @param dateCode           - the date code.
     * @param doseCount          - the dose count.
     * @param expirationDate     - the expiration date.
     * @param hardwareRevision   - the hardware revision of the device.
     * @param softwareRevision   - the software revision of the device.
     * @param inhalerNameType    - the type of the inhaler name.
     * @param lastConnection     - the last connection information.
     * @param lotCode            - the lot code.
     * @param lastRecordId       - the last record Id.
     * @param manufacturerName   - the manufacturer name of the device.
     * @param nickname           - the device nickname.
     * @param remainingDoseCount - the remaining doses.
     * @param serialNumber       - the device serial number.
     * @param authenticationKey  - the authentication key.
     * @param hasChanged         - flag to indicate if the device has changed.
     * @param changedTime        - the changed time.
     * @param drugUID            - the drug UID.
     * @return - the device object created with the passed parameters.
     */
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

        val medication = Medication()
        medication.drugUID = drugUID
        model.medication = medication

        return model
    }
}
