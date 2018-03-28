//
// HistoryDataMatcher.kt
// teva_analysis
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.analysis.utils

import com.teva.analysis.entities.HistoryDose
import com.teva.analysis.model.HistoryDay
import com.teva.devices.entities.InhaleEvent
import com.teva.medication.entities.Prescription
import com.teva.userfeedback.entities.DailyUserFeeling
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

/**
 * This class defines functions for matching different history data objects.
 */

object HistoryDataMatcher {
    /**
     * Matcher for HistoryDay

     * @param expectedHistoryDay The HistoryDay to be matched against.
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching HistoryDay
     */
    fun matchesHistoryDay(expectedHistoryDay: HistoryDay): Matcher<HistoryDay> {
        return object : BaseMatcher<HistoryDay>() {
            override fun matches(obj: Any): Boolean {
                val actualHistoryDay = obj as HistoryDay

                return actualHistoryDay.day == expectedHistoryDay.day &&
                        actualHistoryDay.connectedInhalerCount == expectedHistoryDay.connectedInhalerCount &&
                        (actualHistoryDay.dailyUserFeeling == expectedHistoryDay.dailyUserFeeling || matchesDailyUserFeeling(actualHistoryDay.dailyUserFeeling!!).matches(expectedHistoryDay.dailyUserFeeling)) &&
                        actualHistoryDay.pif == expectedHistoryDay.pif &&
                        matchesHistoryDoseList(expectedHistoryDay.invalidDoses).matches(actualHistoryDay.invalidDoses) &&
                        matchesHistoryDoseList(expectedHistoryDay.relieverDoses).matches(actualHistoryDay.relieverDoses) &&
                        matchesPrescriptionList(expectedHistoryDay.prescriptions).matches(actualHistoryDay.prescriptions)
            }

            override fun describeTo(description: Description) {
                description.appendText("HistoryDay fields should match")
            }
        }
    }

    /**
     * Matcher for List of HistoryDay objects

     * @param expectedHistoryDays The HistoryDay list to be matched against
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching list of historyDay objects
     */
    fun matchesHistoryDayList(expectedHistoryDays: List<HistoryDay>?): Matcher<List<HistoryDay>> {
        return object : BaseMatcher<List<HistoryDay>>() {
            override fun matches(obj: Any): Boolean {
                val actualHistoryDays = obj as? List<HistoryDay>

                if (expectedHistoryDays == null || actualHistoryDays == null) {
                    return false
                }

                if (actualHistoryDays.size != expectedHistoryDays.size) {
                    return false
                }

                for (index in actualHistoryDays.indices) {
                    if (!matchesHistoryDay(expectedHistoryDays[index]).matches(actualHistoryDays[index])) {
                        return false
                    }
                }

                return true
            }

            override fun describeTo(description: Description) {
                description.appendText("Number of HistoryDays and fields of each history day should match")
            }
        }
    }

    /**
     * Matcher for HistoryDose

     * @param expectedHistoryDose The HistoryDose to be matched against.
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching HistoryDose
     */
    fun matchesHistoryDose(expectedHistoryDose: HistoryDose): Matcher<HistoryDose> {
        return object : BaseMatcher<HistoryDose>() {
            override fun matches(obj: Any): Boolean {
                val actualHistoryDose = obj as HistoryDose

                return actualHistoryDose.drugUID == expectedHistoryDose.drugUID &&
                        actualHistoryDose.hasIssues == expectedHistoryDose.hasIssues &&
                        actualHistoryDose.isComplete == expectedHistoryDose.isComplete &&
                        actualHistoryDose.isController == expectedHistoryDose.isController &&
                        actualHistoryDose.isReliever == expectedHistoryDose.isReliever &&
                        actualHistoryDose.isTooSoon == expectedHistoryDose.isTooSoon &&
                        matchesInhaleEventList(expectedHistoryDose.events).matches(actualHistoryDose.events)

            }

            override fun describeTo(description: Description) {
                description.appendText("HistoryDose fields should match")
            }
        }
    }

    /**
     * Matcher for List of HistoryDose objects

     * @param expectedHistoryDoses The HistoryDose list to be matched against
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching list of historyDose objects
     */
    fun matchesHistoryDoseList(expectedHistoryDoses: List<HistoryDose>?): Matcher<List<HistoryDose>> {
        return object : BaseMatcher<List<HistoryDose>>() {
            override fun matches(obj: Any): Boolean {
                val actualHistoryDoses = obj as? List<HistoryDose>

                if (expectedHistoryDoses == null || actualHistoryDoses == null) {
                    return false
                }

                if (actualHistoryDoses.size != expectedHistoryDoses.size) {
                    return false
                }

                for (index in actualHistoryDoses.indices) {
                    if (!matchesHistoryDose(expectedHistoryDoses[index]).matches(actualHistoryDoses[index])) {
                        return false
                    }
                }

                return true
            }

            override fun describeTo(description: Description) {
                description.appendText("Number of HistoryDoses and fields of each history dose should match")
            }
        }
    }

    /**
     * Matcher for Prescription model

     * @param expectedPrescription The prescription to be matched against
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching prescriptions
     */
    fun matchesPrescription(expectedPrescription: Prescription): Matcher<Prescription> {
        return object : BaseMatcher<Prescription>() {
            override fun matches(obj: Any): Boolean {
                val actualPrescription = obj as Prescription

                return expectedPrescription.dosesPerDay == actualPrescription.dosesPerDay &&
                        expectedPrescription.inhalesPerDose == actualPrescription.inhalesPerDose &&
                        expectedPrescription.prescriptionDate == actualPrescription.prescriptionDate &&
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
    fun matchesPrescriptionList(expectedPrescriptions: List<Prescription>?): Matcher<List<Prescription>> {
        return object : BaseMatcher<List<Prescription>>() {
            override fun matches(obj: Any): Boolean {
                val actualPrescriptions = obj as? List<Prescription>

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
    fun matchesInhaleEvent(expectedInhaleEvent: InhaleEvent): Matcher<InhaleEvent> {
        return object : BaseMatcher<InhaleEvent>() {
            override fun matches(obj: Any): Boolean {
                val actualInhaleEvent = obj as InhaleEvent


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
    fun matchesInhaleEventList(expectedInhaleEvents: List<InhaleEvent>?): Matcher<List<InhaleEvent>> {
        return object : BaseMatcher<List<InhaleEvent>>() {
            override fun matches(obj: Any): Boolean {
                val actualInhaleEvents = obj as? List<InhaleEvent>

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
                        expectedDailyUserFeeling.date == actualDailyUserFeeling.date
            }

            override fun describeTo(description: Description) {
                description.appendText("DailyUserFeeling fields should match")
            }
        }
    }
}
