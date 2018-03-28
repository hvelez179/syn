//
// ModelMatcher.java
// app
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.testutils

import com.teva.devices.entities.ConnectionMeta
import com.teva.devices.entities.Device
import com.teva.devices.entities.InhaleEvent
import com.teva.environment.entities.DailyAirQuality
import com.teva.medication.entities.Medication
import com.teva.medication.entities.Prescription
import com.teva.notifications.entities.ReminderSetting
import com.teva.userfeedback.entities.DailyUserFeeling

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.threeten.bp.LocalDate

fun Prescription.matches(expectedPrescription: Prescription): Boolean {
    return expectedPrescription.dosesPerDay == dosesPerDay &&
            expectedPrescription.inhalesPerDose == inhalesPerDose &&
            expectedPrescription.prescriptionDate == prescriptionDate &&
            expectedPrescription.changeTime == changeTime &&
            expectedPrescription.hasChanged == hasChanged &&
            expectedPrescription.medication!!.drugUID == medication!!.drugUID
}

fun DailyUserFeeling.matches(expectedDailyUserFeeling: DailyUserFeeling): Boolean {
    return expectedDailyUserFeeling.time == time &&
            expectedDailyUserFeeling.userFeeling == userFeeling &&
            expectedDailyUserFeeling.date == date &&
            expectedDailyUserFeeling.changeTime == changeTime &&
            expectedDailyUserFeeling.hasChanged == hasChanged
}

fun List<Prescription>.matches(expectedPrescriptions: List<Prescription>?): Boolean {
    if (expectedPrescriptions == null) {
        return false
    }

    if (size != expectedPrescriptions.size) {
        return false
    }

    for (index in indices) {
        if (!get(index).matches(expectedPrescriptions[index])) {
            return false
        }
    }

    return true
}

fun Map<LocalDate, DailyUserFeeling>.matches(expectedDailyUserFeelings: Map<LocalDate, DailyUserFeeling>?): Boolean {

    if (expectedDailyUserFeelings == null) {
        return false
    }

    if (size != expectedDailyUserFeelings.size) {
        return false
    }

    for (date in expectedDailyUserFeelings.keys) {

        if (!containsKey(date))
            return false

        if (!get(date)!!.matches(expectedDailyUserFeelings[date]!!)) {
            return false
        }
    }

    return true
}

fun DailyAirQuality.matches(expectedDailyAirQuality: DailyAirQuality): Boolean {
            return expectedDailyAirQuality.airQualityIndex == airQualityIndex &&
                    expectedDailyAirQuality.airQuality == airQuality &&
                    expectedDailyAirQuality.date == date
}

fun Map<LocalDate, DailyAirQuality>.matchesAirQuality(expectedDailyAirQualityEntries: Map<LocalDate, DailyAirQuality>?): Boolean {
    if (expectedDailyAirQualityEntries == null) {
        return false
    }

    if (size != expectedDailyAirQualityEntries.size) {
        return false
    }

    for (key in expectedDailyAirQualityEntries.keys) {

        if (!containsKey(key)) {
            return false
        }

        if (!get(key)!!.matches(expectedDailyAirQualityEntries[key]!!)) {
            return false
        }
    }

    return true
}

/**
 * This class is used in unit tests for matching the model objects returned by fetch requests.
 */
object ModelMatcher {

    /**
     * Matcher for Device model

     * @param expectedDevice The device to be matched against
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching devices
     */
    @JvmStatic
    fun matchesDevice(expectedDevice: Device): Matcher<Device> {
        return object : BaseMatcher<Device>() {
            override fun matches(`object`: Any): Boolean {
                val actualDevice = `object` as Device

                return expectedDevice.dateCode == actualDevice.dateCode &&
                        expectedDevice.doseCount == actualDevice.doseCount &&
                        expectedDevice.expirationDate == actualDevice.expirationDate &&
                        expectedDevice.hardwareRevision == actualDevice.hardwareRevision &&
                        expectedDevice.softwareRevision == actualDevice.softwareRevision &&
                        expectedDevice.inhalerNameType == actualDevice.inhalerNameType &&
                        expectedDevice.lastConnection == actualDevice.lastConnection &&
                        expectedDevice.lotCode == actualDevice.lotCode &&
                        expectedDevice.lastRecordId == actualDevice.lastRecordId &&
                        expectedDevice.manufacturerName == actualDevice.manufacturerName &&
                        expectedDevice.nickname == actualDevice.nickname &&
                        expectedDevice.remainingDoseCount == actualDevice.remainingDoseCount &&
                        expectedDevice.serialNumber == actualDevice.serialNumber &&
                        expectedDevice.authenticationKey == actualDevice.authenticationKey &&
                        expectedDevice.changeTime == actualDevice.changeTime &&
                        expectedDevice.hasChanged == actualDevice.hasChanged &&
                        expectedDevice.medication!!.drugUID == actualDevice.medication!!.drugUID
            }

            override fun describeTo(description: Description) {
                description.appendText("Device fields should match")
            }
        }
    }

    /**
     * Matcher for List of Device models

     * @param expectedDevices The device list to be matched against
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching list of devices
     */
    @JvmStatic
    fun matchesDeviceList(expectedDevices: List<Device>?): Matcher<List<Device>> {
        return object : BaseMatcher<List<Device>>() {
            override fun matches(`object`: Any): Boolean {
                val actualDevices = `object` as List<Device>

                if (expectedDevices == null || actualDevices == null) {
                    return false
                }

                if (actualDevices.size != expectedDevices.size) {
                    return false
                }

                for (index in actualDevices.indices) {
                    if (!matchesDevice(expectedDevices[index]).matches(actualDevices[index])) {
                        return false
                    }
                }

                return true
            }

            override fun describeTo(description: Description) {
                description.appendText("Number of Devices and fields of each device should match")
            }
        }
    }

    /**
     * Matcher for Medication model

     * @param expectedMedication The medication to be matched against
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching medications
     */
    @JvmStatic
    fun matchesMedication(expectedMedication: Medication): Matcher<Medication> {
        return object : BaseMatcher<Medication>() {
            override fun matches(`object`: Any): Boolean {
                val actualMedication = `object` as Medication

                return expectedMedication.drugUID == actualMedication.drugUID &&
                        expectedMedication.brandName == actualMedication.brandName &&
                        expectedMedication.genericName == actualMedication.genericName &&
                        expectedMedication.medicationClassification == actualMedication.medicationClassification &&
                        expectedMedication.overdoseInhalationCount == actualMedication.overdoseInhalationCount &&
                        expectedMedication.minimumDoseInterval == actualMedication.minimumDoseInterval &&
                        expectedMedication.minimumScheduleInterval == actualMedication.minimumScheduleInterval &&
                        expectedMedication.numberOfMonthsBeforeExpiration == actualMedication.numberOfMonthsBeforeExpiration &&
                        expectedMedication.changeTime == actualMedication.changeTime &&
                        expectedMedication.hasChanged == actualMedication.hasChanged &&
                        matchesPrescriptionList(expectedMedication.prescriptions).matches(actualMedication.prescriptions)
            }

            override fun describeTo(description: Description) {
                description.appendText("Medication fields should match")
            }
        }
    }

    /**
     * Matcher for List of Medication models

     * @param expectedMedications The medication list to be matched against
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching list of medications
     */
    @JvmStatic
    fun matchesMedicationList(expectedMedications: List<Medication>?): Matcher<List<Medication>> {
        return object : BaseMatcher<List<Medication>>() {
            override fun matches(`object`: Any): Boolean {
                val actualMedications = `object` as List<Medication>

                if (expectedMedications == null || actualMedications == null) {
                    return false
                }

                if (actualMedications.size != expectedMedications.size) {
                    return false
                }

                for (index in actualMedications.indices) {
                    if (!matchesMedication(expectedMedications[index]).matches(actualMedications[index])) {
                        return false
                    }
                }

                return true
            }

            override fun describeTo(description: Description) {
                description.appendText("Number of Medications and fields of each Medication should match")
            }
        }
    }

    /**
     * Matcher for Prescription model

     * @param expectedPrescription The prescription to be matched against
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching prescriptions
     */
    @JvmStatic
    fun matchesPrescription(expectedPrescription: Prescription): Matcher<Prescription> {
        return object : BaseMatcher<Prescription>() {
            override fun matches(`object`: Any): Boolean {
                val actualPrescription = `object` as Prescription

                return expectedPrescription.dosesPerDay == actualPrescription.dosesPerDay &&
                        expectedPrescription.inhalesPerDose == actualPrescription.inhalesPerDose &&
                        expectedPrescription.prescriptionDate == actualPrescription.prescriptionDate &&
                        expectedPrescription.changeTime == actualPrescription.changeTime &&
                        expectedPrescription.hasChanged == actualPrescription.hasChanged &&
                        expectedPrescription.medication!!.drugUID == actualPrescription.medication!!.drugUID
            }

            override fun describeTo(description: Description) {
                description.appendText("Prescription fields should match")
            }
        }
    }

    /**
     * Matcher for List of Prescription models

     * @param expectedPrescriptions The prescription list to be matched against
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching list of prescriptions
     */
    @JvmStatic
    fun matchesPrescriptionList(expectedPrescriptions: List<Prescription>?): Matcher<List<Prescription>> {
        return object : BaseMatcher<List<Prescription>>() {
            override fun matches(`object`: Any): Boolean {
                val actualPrescriptions = `object` as List<Prescription>

                if (expectedPrescriptions == null || actualPrescriptions == null) {
                    return false
                }

                if (actualPrescriptions.size != expectedPrescriptions.size) {
                    return false
                }

                for (index in actualPrescriptions.indices) {
                    if (!matchesPrescription(expectedPrescriptions[index]).matches(actualPrescriptions[index])) {
                        return false
                    }
                }

                return true
            }

            override fun describeTo(description: Description) {
                description.appendText("Number of Prescriptions and fields of each prescription should match")
            }
        }
    }

    /**
     * Matcher for InhaleEvent model

     * @param expectedInhaleEvent The inhaleevent to be matched against
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching inhaleevents
     */
    @JvmStatic
    fun matchesInhaleEvent(expectedInhaleEvent: InhaleEvent): Matcher<InhaleEvent> {
        return object : BaseMatcher<InhaleEvent>() {
            override fun matches(`object`: Any): Boolean {
                val actualInhaleEvent = `object` as InhaleEvent


                return expectedInhaleEvent.eventUID == actualInhaleEvent.eventUID &&
                        expectedInhaleEvent.eventTime == actualInhaleEvent.eventTime &&
                        expectedInhaleEvent.timezoneOffsetMinutes == actualInhaleEvent.timezoneOffsetMinutes &&
                        expectedInhaleEvent.inhaleEventTime == actualInhaleEvent.inhaleEventTime &&
                        expectedInhaleEvent.inhaleDuration == actualInhaleEvent.inhaleDuration &&
                        expectedInhaleEvent.inhalePeak == actualInhaleEvent.inhalePeak &&
                        expectedInhaleEvent.inhaleTimeToPeak == actualInhaleEvent.inhaleTimeToPeak &&
                        expectedInhaleEvent.status == actualInhaleEvent.status &&
                        expectedInhaleEvent.closeTime == actualInhaleEvent.closeTime &&
                        expectedInhaleEvent.doseId == actualInhaleEvent.doseId &&
                        expectedInhaleEvent.cartridgeUID == actualInhaleEvent.cartridgeUID &&
                        expectedInhaleEvent.upperThresholdTime == actualInhaleEvent.upperThresholdTime &&
                        expectedInhaleEvent.upperThresholdDuration == actualInhaleEvent.upperThresholdDuration &&
                        expectedInhaleEvent.isValidInhale == actualInhaleEvent.isValidInhale &&
                        expectedInhaleEvent.changeTime == actualInhaleEvent.changeTime &&
                        expectedInhaleEvent.hasChanged == actualInhaleEvent.hasChanged &&
                        expectedInhaleEvent.deviceSerialNumber == actualInhaleEvent.deviceSerialNumber &&
                        expectedInhaleEvent.drugUID == actualInhaleEvent.drugUID
            }

            override fun describeTo(description: Description) {
                description.appendText("InhaleEvent fields should match")
            }
        }
    }

    /**
     * Matcher for List of InhaleEvent models

     * @param expectedInhaleEvents The list of inhaleevents to be matched against
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching list of inhaleevents
     */
    @JvmStatic
    fun matchesInhaleEventList(expectedInhaleEvents: List<InhaleEvent>?): Matcher<List<InhaleEvent>> {
        return object : BaseMatcher<List<InhaleEvent>>() {
            override fun matches(`object`: Any): Boolean {
                val actualInhaleEvents = `object` as List<InhaleEvent>

                if (expectedInhaleEvents == null || actualInhaleEvents == null) {
                    return false
                }

                if (actualInhaleEvents.size != expectedInhaleEvents.size) {
                    return false
                }

                for (index in actualInhaleEvents.indices) {
                    if (!matchesInhaleEvent(expectedInhaleEvents[index]).matches(actualInhaleEvents[index])) {
                        return false
                    }
                }

                return true
            }

            override fun describeTo(description: Description) {
                description.appendText("Number of InhaleEvents and fields of each InhaleEvent should match")
            }
        }
    }

    /**
     * Matcher for ConnectionMeta model

     * @param expectedConnectionMeta The ConnectionMeta to be matched against
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching ConnectionMeta
     */
    @JvmStatic
    fun matchesConnectionMeta(expectedConnectionMeta: ConnectionMeta): Matcher<ConnectionMeta> {
        return object : BaseMatcher<ConnectionMeta>() {
            override fun matches(o: Any): Boolean {
                val (connectionDate, serialNumber) = o as ConnectionMeta

                return expectedConnectionMeta.connectionDate == connectionDate && expectedConnectionMeta.serialNumber == serialNumber
            }

            override fun describeTo(description: Description) {
                description.appendText("ConnectionMeta fields should match")
            }
        }
    }

    /**
     * Matcher for DailyAirQuality model

     * @param expectedDailyAirQuality The DailyAirQuality to be matched against
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching DailyAirQuality
     */
    @JvmStatic
    fun matchesDailyAirQuality(expectedDailyAirQuality: DailyAirQuality): Matcher<DailyAirQuality> {
        return object : BaseMatcher<DailyAirQuality>() {
            override fun matches(o: Any): Boolean {
                val actualDailyAirQuality = o as DailyAirQuality

                return expectedDailyAirQuality.airQualityIndex == actualDailyAirQuality.airQualityIndex &&
                        expectedDailyAirQuality.airQuality == actualDailyAirQuality.airQuality &&
                        expectedDailyAirQuality.date == actualDailyAirQuality.date
            }

            override fun describeTo(description: Description) {
                description.appendText("DailyAirQuality fields should match")
            }
        }
    }

    /**
     * Matcher for List of DailyAirQuality models

     * @param expectedDailyAirQualityEntries The DailyAirQuality entries to be matched against
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching DailyAirQuality entries
     */
    @JvmStatic
    fun matchesDailyAirQualityMap(expectedDailyAirQualityEntries: Map<LocalDate, DailyAirQuality>?): Matcher<Map<LocalDate, DailyAirQuality>> {
        return object : BaseMatcher<Map<LocalDate, DailyAirQuality>>() {
            override fun matches(o: Any): Boolean {
                val actualDailyAirQualityEntries = o as Map<LocalDate, DailyAirQuality>

                if (expectedDailyAirQualityEntries == null || actualDailyAirQualityEntries == null) {
                    return false
                }

                if (actualDailyAirQualityEntries.size != expectedDailyAirQualityEntries.size) {
                    return false
                }

                for (key in expectedDailyAirQualityEntries.keys) {

                    if (!actualDailyAirQualityEntries.containsKey(key)) {
                        return false
                    }

                    if (!matchesDailyAirQuality(expectedDailyAirQualityEntries[key]!!).matches(actualDailyAirQualityEntries[key])) {
                        return false
                    }
                }

                return true
            }

            override fun describeTo(description: Description) {
                description.appendText("Number of DailyAirQuality entries and fields of each DailyAirQuality entry should match")
            }
        }
    }

    /**
     * Matcher for ReminderSetting model

     * @param expectedReminderSetting The ReminderSetting to be matched against
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching ReminderSettings
     */
    @JvmStatic
    fun matchesReminderSetting(expectedReminderSetting: ReminderSetting): Matcher<ReminderSetting> {
        return object : BaseMatcher<ReminderSetting>() {
            override fun matches(`object`: Any): Boolean {
                val actualReminderSetting = `object` as ReminderSetting


                return expectedReminderSetting.name == actualReminderSetting.name &&
                        expectedReminderSetting.isEnabled == actualReminderSetting.isEnabled &&
                        expectedReminderSetting.repeatType === actualReminderSetting.repeatType &&
                        expectedReminderSetting.timeOfDay === actualReminderSetting.timeOfDay
            }

            override fun describeTo(description: Description) {
                description.appendText("ReminderSetting fields should match")
            }
        }
    }

    /**
     * Matcher for List of ReminderSetting models

     * @param expectedReminderSettings The list of ReminderSettings to be matched against
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching list of ReminderSettings
     */
    @JvmStatic
    fun matchesReminderSettingList(expectedReminderSettings: List<ReminderSetting>?): Matcher<List<ReminderSetting>> {
        return object : BaseMatcher<List<ReminderSetting>>() {
            override fun matches(`object`: Any): Boolean {
                val actualReminderSettings = `object` as List<ReminderSetting>

                if (expectedReminderSettings == null || actualReminderSettings == null) {
                    return false
                }

                if (actualReminderSettings.size != expectedReminderSettings.size) {
                    return false
                }

                for (index in actualReminderSettings.indices) {
                    if (!matchesReminderSetting(expectedReminderSettings[index]).matches(actualReminderSettings[index])) {
                        return false
                    }
                }

                return true
            }

            override fun describeTo(description: Description) {
                description.appendText("Number of ReminderSettings and fields of each ReminderSetting should match")
            }
        }
    }

    /**
     * Matcher for DailyUserFeeling model

     * @param expectedDailyUserFeeling The DailyUserFeeling to be matched against
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching DailyUserFeelings
     */
    fun matchesDailyUserFeeling(expectedDailyUserFeeling: DailyUserFeeling): Matcher<DailyUserFeeling> {
        return object : BaseMatcher<DailyUserFeeling>() {
            override fun matches(o: Any): Boolean {
                val actualDailyUserFeeling = o as DailyUserFeeling

                return expectedDailyUserFeeling.time == actualDailyUserFeeling.time &&
                        expectedDailyUserFeeling.userFeeling == actualDailyUserFeeling.userFeeling &&
                        expectedDailyUserFeeling.date == actualDailyUserFeeling.date &&
                        expectedDailyUserFeeling.changeTime == actualDailyUserFeeling.changeTime &&
                        expectedDailyUserFeeling.hasChanged == actualDailyUserFeeling.hasChanged
            }

            override fun describeTo(description: Description) {
                description.appendText("DailyUserFeeling fields should match")
            }
        }
    }

    /**
     * Matcher for a collection of DailyUserFeeling models

     * @param expectedDailyUserFeelings The collection of DailyUserFeelings to be matched against
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching collection of DailyUserFeelings
     */
    fun matchesDailyUserFeelingCollection(expectedDailyUserFeelings: Map<LocalDate, DailyUserFeeling>?): Matcher<Map<LocalDate, DailyUserFeeling>> {
        return object : BaseMatcher<Map<LocalDate, DailyUserFeeling>>() {
            override fun matches(`object`: Any): Boolean {
                val actualDailyUserFeelings = `object` as Map<LocalDate, DailyUserFeeling>

                if (expectedDailyUserFeelings == null || actualDailyUserFeelings == null) {
                    return false
                }

                if (actualDailyUserFeelings.size != expectedDailyUserFeelings.size) {
                    return false
                }

                for (date in expectedDailyUserFeelings.keys) {

                    if (!actualDailyUserFeelings.containsKey(date))
                        return false

                    if (!matchesDailyUserFeeling(expectedDailyUserFeelings[date]!!).matches(actualDailyUserFeelings[date])) {
                        return false
                    }
                }

                return true
            }

            override fun describeTo(description: Description) {
                description.appendText("Number of DailyUserFeelings and fields of each DailyUserFeeling should match")
            }
        }
    }
}
