//
// HistoryTestDataCreator.kt
// teva_analysis
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.analysis.utils

import com.teva.analysis.entities.HistoryDose
import com.teva.analysis.model.HistoryDay
import com.teva.devices.entities.InhaleEvent
import com.teva.medication.entities.Medication
import com.teva.medication.entities.Prescription
import com.teva.medication.enumerations.MedicationClassification
import com.teva.userfeedback.entities.DailyUserFeeling
import com.teva.userfeedback.enumerations.UserFeeling
import org.threeten.bp.*
import java.util.*

/**
 * This class is a helper class which creates history data used for testing the
 * HistoryCollator class.
 */

object HistoryTestDataCreator {

    private val GMT_ZONE_ID = ZoneId.ofOffset("GMT", ZoneOffset.UTC)

    /**
     * This method creates medication data to be used by the tests.

     * @return -  a list of medications.
     */
    private fun createRelieverMedications(): List<Medication> {
        val medicationHistory: MutableList<Medication>

        // Medication data.
        val medication1 = Model.Medication("745750", "ProAir1", "ProAir1", MedicationClassification.RELIEVER, 300, 300, 2, 100, 12)
        val medication2 = Model.Medication("745751", "ProAir2", "ProAir2", MedicationClassification.RELIEVER, 240, 240, 3, 120, 12)

        medicationHistory = ArrayList<Medication>()
        medicationHistory.add(medication1)
        medicationHistory.add(medication2)

        return medicationHistory
    }

    /**
     * This method creates  prescription data to be used by the tests.

     * @param medicationHistory - the list of medications
     * *
     * @return - a list of prescriptions.
     */
    private fun createRelieverPrescriptions(medicationHistory: List<Medication>): List<Prescription> {
        val prescriptionHistory: MutableList<Prescription>

        val prescription1Time = Instant.from(ZonedDateTime.of(2020, 1, 20, 13, 0, 0, 0, GMT_ZONE_ID))
        val prescription2Time = Instant.from(ZonedDateTime.of(2020, 1, 5, 11, 0, 0, 0, GMT_ZONE_ID))

        // Prescription data.
        val prescription1 = Model.Prescription(1, 3, prescription1Time, medicationHistory[0].drugUID)
        prescription1.medication = medicationHistory[0]

        val prescription2 = Model.Prescription(1, 4, prescription2Time, medicationHistory[1].drugUID)
        prescription2.medication = medicationHistory[1]

        prescriptionHistory = ArrayList<Prescription>()
        prescriptionHistory.add(prescription1)
        prescriptionHistory.add(prescription2)

        return prescriptionHistory
    }

    /**
     * This method creates user feeling data to be used by tests.
     */
    private fun createUserFeelingHistory(startDate: LocalDate, startTime: Instant): Map<LocalDate, DailyUserFeeling> {
        val SECONDS_PER_DAY = 86400
        val dailyUserFeelingHistory: MutableMap<LocalDate, DailyUserFeeling>

        // Daily user feeling data.
        val dailyUserFeeling1 = Model.DailyUserFeeling(startDate, startTime, UserFeeling.GOOD)
        val dailyUserFeeling2 = Model.DailyUserFeeling(startDate.minusDays(1), startTime.minusSeconds(SECONDS_PER_DAY.toLong()), UserFeeling.GOOD)
        val dailyUserFeeling3 = Model.DailyUserFeeling(startDate.minusDays(2), startTime.minusSeconds((SECONDS_PER_DAY * 2).toLong()), UserFeeling.GOOD)
        val dailyUserFeeling5 = Model.DailyUserFeeling(startDate.minusDays(4), startTime.minusSeconds((SECONDS_PER_DAY * 4).toLong()), UserFeeling.GOOD)
        val dailyUserFeeling6 = Model.DailyUserFeeling(startDate.minusDays(5), startTime.minusSeconds((SECONDS_PER_DAY * 5).toLong()), UserFeeling.POOR)
        val dailyUserFeeling7 = Model.DailyUserFeeling(startDate.minusDays(6), startTime.minusSeconds((SECONDS_PER_DAY * 6).toLong()), UserFeeling.BAD)
        val dailyUserFeeling9 = Model.DailyUserFeeling(startDate.minusDays(8), startTime.minusSeconds((SECONDS_PER_DAY * 8).toLong()), UserFeeling.POOR)
        val dailyUserFeeling10 = Model.DailyUserFeeling(startDate.minusDays(9), startTime.minusSeconds((SECONDS_PER_DAY * 9).toLong()), UserFeeling.GOOD)
        val dailyUserFeeling11 = Model.DailyUserFeeling(startDate.minusDays(10), startTime.minusSeconds((SECONDS_PER_DAY * 10).toLong()), UserFeeling.GOOD)
        val dailyUserFeeling12 = Model.DailyUserFeeling(startDate.minusDays(11), startTime.minusSeconds((SECONDS_PER_DAY * 11).toLong()), UserFeeling.GOOD)
        val dailyUserFeeling13 = Model.DailyUserFeeling(startDate.minusDays(12), startTime.minusSeconds((SECONDS_PER_DAY * 12).toLong()), UserFeeling.POOR)
        val dailyUserFeeling14 = Model.DailyUserFeeling(startDate.minusDays(13), startTime.minusSeconds((SECONDS_PER_DAY * 13).toLong()), UserFeeling.GOOD)
        val dailyUserFeeling15 = Model.DailyUserFeeling(startDate.minusDays(14), startTime.minusSeconds((SECONDS_PER_DAY * 4).toLong()), UserFeeling.GOOD)
        val dailyUserFeeling17 = Model.DailyUserFeeling(startDate.minusDays(16), startTime.minusSeconds((SECONDS_PER_DAY * 6).toLong()), UserFeeling.BAD)
        val dailyUserFeeling18 = Model.DailyUserFeeling(startDate.minusDays(17), startTime.minusSeconds((SECONDS_PER_DAY * 7).toLong()), UserFeeling.GOOD)
        val dailyUserFeeling19 = Model.DailyUserFeeling(startDate.minusDays(18), startTime.minusSeconds((SECONDS_PER_DAY * 8).toLong()), UserFeeling.POOR)
        val dailyUserFeeling21 = Model.DailyUserFeeling(startDate.minusDays(20), startTime.minusSeconds((SECONDS_PER_DAY * 10).toLong()), UserFeeling.GOOD)

        dailyUserFeelingHistory = HashMap<LocalDate, DailyUserFeeling>()
        dailyUserFeelingHistory.put(dailyUserFeeling1.date!!, dailyUserFeeling1)
        dailyUserFeelingHistory.put(dailyUserFeeling2.date!!, dailyUserFeeling2)
        dailyUserFeelingHistory.put(dailyUserFeeling3.date!!, dailyUserFeeling3)
        dailyUserFeelingHistory.put(dailyUserFeeling5.date!!, dailyUserFeeling5)
        dailyUserFeelingHistory.put(dailyUserFeeling6.date!!, dailyUserFeeling6)
        dailyUserFeelingHistory.put(dailyUserFeeling7.date!!, dailyUserFeeling7)
        dailyUserFeelingHistory.put(dailyUserFeeling9.date!!, dailyUserFeeling9)
        dailyUserFeelingHistory.put(dailyUserFeeling10.date!!, dailyUserFeeling10)
        dailyUserFeelingHistory.put(dailyUserFeeling11.date!!, dailyUserFeeling11)
        dailyUserFeelingHistory.put(dailyUserFeeling12.date!!, dailyUserFeeling12)
        dailyUserFeelingHistory.put(dailyUserFeeling13.date!!, dailyUserFeeling13)
        dailyUserFeelingHistory.put(dailyUserFeeling14.date!!, dailyUserFeeling14)
        dailyUserFeelingHistory.put(dailyUserFeeling15.date!!, dailyUserFeeling15)
        dailyUserFeelingHistory.put(dailyUserFeeling17.date!!, dailyUserFeeling17)
        dailyUserFeelingHistory.put(dailyUserFeeling18.date!!, dailyUserFeeling18)
        dailyUserFeelingHistory.put(dailyUserFeeling19.date!!, dailyUserFeeling19)
        dailyUserFeelingHistory.put(dailyUserFeeling21.date!!, dailyUserFeeling21)

        return dailyUserFeelingHistory
    }

    private fun createConnectionHistory(startDate: LocalDate): Map<LocalDate, Int> {

        // Connection meta data.

        val connectionMetaHistory = HashMap<LocalDate, Int>()
        connectionMetaHistory.put(startDate, 1)
        connectionMetaHistory.put(startDate.minusDays(1), 2)
        connectionMetaHistory.put(startDate.minusDays(2), 1)
        connectionMetaHistory.put(startDate.minusDays(3), 2)
        connectionMetaHistory.put(startDate.minusDays(4), 2)
        connectionMetaHistory.put(startDate.minusDays(5), 0)
        connectionMetaHistory.put(startDate.minusDays(6), 3)
        connectionMetaHistory.put(startDate.minusDays(7), 3)
        connectionMetaHistory.put(startDate.minusDays(8), 1)
        connectionMetaHistory.put(startDate.minusDays(9), 1)
        connectionMetaHistory.put(startDate.minusDays(10), 0)
        connectionMetaHistory.put(startDate.minusDays(11), 1)
        connectionMetaHistory.put(startDate.minusDays(12), 2)
        connectionMetaHistory.put(startDate.minusDays(13), 1)
        connectionMetaHistory.put(startDate.minusDays(14), 1)
        connectionMetaHistory.put(startDate.minusDays(15), 2)
        connectionMetaHistory.put(startDate.minusDays(16), 2)
        connectionMetaHistory.put(startDate.minusDays(17), 0)
        connectionMetaHistory.put(startDate.minusDays(18), 1)
        connectionMetaHistory.put(startDate.minusDays(19), 1)
        connectionMetaHistory.put(startDate.minusDays(20), 2)

        return connectionMetaHistory
    }

    /**
     * This method creates historical data for analysis and returns the expected collated history
     * for the historical data along with the historical data.
     * This method creates inhalation events to simulate -
     * no, low and good inhalations
     * exhalation
     * error
     * bad data error,
     * time stamp error and
     * parameter error
     *
     * @param startDate - the start date.
     * @param startTime - the start time.
     * @param prescriptionHistory - the created prescription history.
     * @param dailyUserFeelingHistory - the created daily user feeling history.
     * @param connectionMetaHistory - the created connection meta history.
     * @param relieverInhaleEventHistory - the created inhale event history.
     * @param medicationHistory - the medication history.
     * @return - the collated list of history days for the created historical data.
     */
    fun createHistoricalDataAndReturnExpectedCollatedHistory(startDate: LocalDate, startTime: Instant, prescriptionHistory: MutableList<Prescription>,
                                                             dailyUserFeelingHistory: MutableMap<LocalDate, DailyUserFeeling>,
                                                             connectionMetaHistory: MutableMap<LocalDate, Int>,
                                                             relieverInhaleEventHistory: MutableList<InhaleEvent>,
                                                             medicationHistory: MutableList<Medication>): List<HistoryDay> {
        medicationHistory.addAll(createRelieverMedications())
        prescriptionHistory.addAll(createRelieverPrescriptions(medicationHistory))
        dailyUserFeelingHistory.putAll(createUserFeelingHistory(startDate, startTime))
        connectionMetaHistory.putAll(createConnectionHistory(startDate))

        val newPrescriptions = ArrayList<Prescription>()
        val oldPrescriptions = ArrayList<Prescription>()
        newPrescriptions.add(prescriptionHistory[0])
        newPrescriptions.add(prescriptionHistory[1])
        oldPrescriptions.add(prescriptionHistory[1])

        val historyDays = ArrayList<HistoryDay>()

        // 1/31/2020
        // bad data
        var currentDate = startDate
        var currentTime = LocalTime.of(9, 0, 0)
        var timezoneOffset = -300
        var eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day1Event1 = Model.InhaleEvent(1, eventTime, timezoneOffset, 20, 1065, 500, 400, 2, 6,
                1, "Cartridge4", 0, 0, false, "123454321")
        day1Event1.drugUID = "745750"

        currentTime = LocalTime.of(15, 0, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day1Event2 = Model.InhaleEvent(1, eventTime, timezoneOffset, 19, 965, 450, 410, 0, 4,
                2, "Cartridge4", 0, 0, true, "123454321") // low inhalation
        day1Event2.drugUID = "745750"

        relieverInhaleEventHistory.add(day1Event1)
        relieverInhaleEventHistory.add(day1Event2)

        val day1 = HistoryDay(currentDate)
        val day1Dose2 = HistoryDose("745750", listOf(day1Event2))
        day1Dose2.isReliever = true
        day1.relieverDoses.add(day1Dose2)
        day1.dailyUserFeeling = dailyUserFeelingHistory[currentDate]
        day1.connectedInhalerCount = connectionMetaHistory[currentDate]!!
        day1.prescriptions = newPrescriptions

        // 1/30/2020
        currentDate = startDate.minusDays(1)
        currentTime = LocalTime.of(10, 30, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day2Event1 = Model.InhaleEvent(1, eventTime, timezoneOffset, 21, 1165, 451, 390, 0, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        day2Event1.drugUID = "745750"

        currentTime = LocalTime.of(14, 0, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day2Event2 = Model.InhaleEvent(1, eventTime, timezoneOffset, 23, 1105, 610, 440, 0, 4,
                2, "Cartridge4", 0, 0, true, "123454321") // too soon
        day2Event2.drugUID = "745750"

        relieverInhaleEventHistory.add(day2Event1)
        relieverInhaleEventHistory.add(day2Event2)

        val day2 = HistoryDay(currentDate)
        val day2Dose1 = HistoryDose("745750", listOf(day2Event1))
        day2Dose1.isReliever = true
        val day2Dose2 = HistoryDose("745750", listOf(day2Event2))
        day2Dose2.isReliever = true
        day2Dose2.isTooSoon = true
        day2.relieverDoses.add(day2Dose1)
        day2.relieverDoses.add(day2Dose2)
        day2.dailyUserFeeling = dailyUserFeelingHistory[currentDate]
        day2.connectedInhalerCount = connectionMetaHistory[currentDate]!!
        day2.prescriptions = newPrescriptions

        // 1/29/2020
        currentDate = startDate.minusDays(2)
        currentTime = LocalTime.of(8, 30, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day3Event1 = Model.InhaleEvent(1, eventTime, timezoneOffset, 25, 1265, 550, 420, 0, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        day3Event1.drugUID = "745750"

        relieverInhaleEventHistory.add(day3Event1)

        val day3 = HistoryDay(currentDate)
        val day3Dose1 = HistoryDose("745750", listOf(day3Event1))
        day3Dose1.isReliever = true
        day3.relieverDoses.add(day3Dose1)
        day3.dailyUserFeeling = dailyUserFeelingHistory[currentDate]
        day3.connectedInhalerCount = connectionMetaHistory[currentDate]!!
        day3.prescriptions = newPrescriptions

        // 1/28/2020
        currentDate = startDate.minusDays(3)
        currentTime = LocalTime.of(9, 45, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day4Event1 = Model.InhaleEvent(1, eventTime, timezoneOffset, 22, 1100, 90, 0, 32, 6,
                1, "Cartridge4", 0, 0, false, "123454321") // no inhalation
        day4Event1.drugUID = "745750"

        relieverInhaleEventHistory.add(day4Event1)

        val day4 = HistoryDay(currentDate)
        val day4Dose1 = HistoryDose("745750", listOf(day4Event1))
        day4Dose1.isReliever = true
        day4Dose1.hasIssues = true
        day4.invalidDoses.add(day4Dose1)
        day4.dailyUserFeeling = dailyUserFeelingHistory[currentDate]
        day4.connectedInhalerCount = connectionMetaHistory[currentDate]!!
        day4.prescriptions = newPrescriptions

        // 1/27/2020
        currentDate = startDate.minusDays(4)
        currentTime = LocalTime.of(11, 0, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day5Event1 = Model.InhaleEvent(1, eventTime, timezoneOffset, 21, 1165, 651, 390, 0, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        day5Event1.drugUID = "745750"

        currentTime = LocalTime.of(17, 30, 0)
        timezoneOffset = 0
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day5Event2 = Model.InhaleEvent(1, eventTime, timezoneOffset, 23, 1105, 610, 440, 0, 4,
                2, "Cartridge4", 0, 0, true, "123454321") // different time zone
        day5Event2.drugUID = "745750"

        relieverInhaleEventHistory.add(day5Event1)
        relieverInhaleEventHistory.add(day5Event2)

        val day5 = HistoryDay(currentDate)
        val day5Dose1 = HistoryDose("745750", listOf(day5Event1))
        day5Dose1.isReliever = true
        val day5Dose2 = HistoryDose("745750", listOf(day5Event2))
        day5Dose2.isReliever = true
        day5.relieverDoses.add(day5Dose1)
        day5.relieverDoses.add(day5Dose2)
        day5.dailyUserFeeling = dailyUserFeelingHistory[currentDate]
        day5.connectedInhalerCount = connectionMetaHistory[currentDate]!!
        day5.prescriptions = newPrescriptions

        // 1/26/2020
        // no data
        currentDate = startDate.minusDays(5)
        val day6 = HistoryDay(currentDate)
        day6.dailyUserFeeling = dailyUserFeelingHistory[currentDate]
        day6.connectedInhalerCount = connectionMetaHistory[currentDate]!!
        day6.prescriptions = newPrescriptions

        // 1/25/2020
        timezoneOffset = -300
        currentDate = startDate.minusDays(6)
        currentTime = LocalTime.of(9, 12, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day7Event1 = Model.InhaleEvent(1, eventTime, timezoneOffset, 21, 1165, 601, 390, 0, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        day7Event1.drugUID = "745750"

        relieverInhaleEventHistory.add(day7Event1)

        val day7 = HistoryDay(currentDate)
        val day7Dose1 = HistoryDose("745750", listOf(day7Event1))
        day7Dose1.isReliever = true
        day7.relieverDoses.add(day7Dose1)
        day7.dailyUserFeeling = dailyUserFeelingHistory[currentDate]
        day7.connectedInhalerCount = connectionMetaHistory[currentDate]!!
        day7.prescriptions = newPrescriptions

        // 1/24/2020
        currentDate = startDate.minusDays(7)
        currentTime = LocalTime.of(7, 43, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day8Event1 = Model.InhaleEvent(1, eventTime, timezoneOffset, 22, 1135, 702, 380, 0, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        day8Event1.drugUID = "745750"

        currentTime = LocalTime.of(13, 30, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day8Event2 = Model.InhaleEvent(1, eventTime, timezoneOffset, 24, 1115, 653, 412, 0, 4,
                2, "Cartridge4", 0, 0, true, "123454321")
        day8Event2.drugUID = "745750"

        relieverInhaleEventHistory.add(day8Event1)
        relieverInhaleEventHistory.add(day8Event2)

        val day8 = HistoryDay(currentDate)
        val day8Dose1 = HistoryDose("745750", listOf(day8Event1))
        day8Dose1.isReliever = true
        val day8Dose2 = HistoryDose("745750", listOf(day8Event2))
        day8Dose2.isReliever = true
        day8.relieverDoses.add(day8Dose1)
        day8.relieverDoses.add(day8Dose2)
        day8.dailyUserFeeling = dailyUserFeelingHistory[currentDate]
        day8.connectedInhalerCount = connectionMetaHistory[currentDate]!!
        day8.prescriptions = newPrescriptions

        // 1/23/2020
        currentDate = startDate.minusDays(8)
        currentTime = LocalTime.of(10, 19, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day9Event1 = Model.InhaleEvent(1, eventTime, timezoneOffset, 19, 1365, 2001, 600, 0, 7,
                1, "Cartridge4", 0, 0, true, "123454321") // high inhalation
        day9Event1.drugUID = "745750"

        relieverInhaleEventHistory.add(day9Event1)

        val day9 = HistoryDay(currentDate)
        val day9Dose1 = HistoryDose("745750", listOf(day9Event1))
        day9Dose1.isReliever = true
        day9.invalidDoses.add(day9Dose1)
        day9.dailyUserFeeling = dailyUserFeelingHistory[currentDate]
        day9.connectedInhalerCount = connectionMetaHistory[currentDate]!!
        day9.prescriptions = newPrescriptions

        // 1/22/2020
        currentDate = startDate.minusDays(9)
        currentTime = LocalTime.of(11, 5, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day10Event1 = Model.InhaleEvent(1, eventTime, timezoneOffset, 26, 865, 601, 390, 0, 7,
                1, "Cartridge4", 0, 0, true, "123454321")
        day10Event1.drugUID = "745750"

        relieverInhaleEventHistory.add(day10Event1)

        val day10 = HistoryDay(currentDate)
        val day10Dose1 = HistoryDose("745750", listOf(day10Event1))
        day10Dose1.isReliever = true
        day10.relieverDoses.add(day10Dose1)
        day10.dailyUserFeeling = dailyUserFeelingHistory[currentDate]
        day10.connectedInhalerCount = connectionMetaHistory[currentDate]!!
        day10.prescriptions = newPrescriptions

        // 1/21/2020
        currentDate = startDate.minusDays(10)
        currentTime = LocalTime.of(12, 17, 13)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day11Event1 = Model.InhaleEvent(1, eventTime, timezoneOffset, 26, 965, 582, 570, 64, 6,
                1, "Cartridge3", 0, 0, false, "234565432") // parameter error
        day11Event1.drugUID = "745751"

        relieverInhaleEventHistory.add(day11Event1)

        val day11 = HistoryDay(currentDate)
        day11.dailyUserFeeling = dailyUserFeelingHistory[currentDate]
        day11.connectedInhalerCount = connectionMetaHistory[currentDate]!!
        day11.prescriptions = newPrescriptions

        // 1/20/2020
        currentDate = startDate.minusDays(11)
        currentTime = LocalTime.of(12, 17, 13)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day12Event1 = Model.InhaleEvent(1, eventTime, timezoneOffset, 26, 965, 582, 570, 4, 6,
                1, "Cartridge3", 0, 0, false, "234565432") // multiple inhalations
        day12Event1.drugUID = "745751"

        relieverInhaleEventHistory.add(day12Event1)

        val day12 = HistoryDay(currentDate)
        val day12Dose1 = HistoryDose("745751", listOf(day12Event1))
        day12Dose1.isReliever = true
        day12Dose1.hasIssues = true
        day12.invalidDoses.add(day12Dose1)
        day12.dailyUserFeeling = dailyUserFeelingHistory[currentDate]
        day12.connectedInhalerCount = connectionMetaHistory[currentDate]!!
        day12.prescriptions = oldPrescriptions

        // 1/19/2020
        currentDate = startDate.minusDays(12)
        currentTime = LocalTime.of(8, 45, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day13Event1 = Model.InhaleEvent(1, eventTime, timezoneOffset, 26, 1035, 602, 380, 0, 6,
                1, "Cartridge3", 0, 0, true, "234565432")
        day13Event1.drugUID = "745751"

        currentTime = LocalTime.of(16, 22, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day13Event2 = Model.InhaleEvent(1, eventTime, timezoneOffset, 20, 1115, 753, 432, 0, 5,
                2, "Cartridge3", 0, 0, true, "234565432")
        day13Event2.drugUID = "745751"

        relieverInhaleEventHistory.add(day13Event1)
        relieverInhaleEventHistory.add(day13Event2)

        val day13 = HistoryDay(currentDate)
        val day13Dose1 = HistoryDose("745751", listOf(day13Event1))
        day13Dose1.isReliever = true
        val day13Dose2 = HistoryDose("745751", listOf(day13Event2))
        day13Dose2.isReliever = true
        day13.relieverDoses.add(day13Dose1)
        day13.relieverDoses.add(day13Dose2)
        day13.dailyUserFeeling = dailyUserFeelingHistory[currentDate]
        day13.connectedInhalerCount = connectionMetaHistory[currentDate]!!
        day13.prescriptions = oldPrescriptions

        // 1/18/2020
        // too many events
        currentDate = startDate.minusDays(13)
        currentTime = LocalTime.of(6, 15, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day14Event1 = Model.InhaleEvent(1, eventTime, timezoneOffset, 21, 1235, 502, 390, 0, 6,
                1, "Cartridge3", 0, 0, true, "234565432")
        day14Event1.drugUID = "745751"

        currentTime = LocalTime.of(10, 15, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day14Event2 = Model.InhaleEvent(1, eventTime, timezoneOffset, 23, 1015, 863, 456, 0, 5,
                2, "Cartridge3", 0, 0, true, "234565432")
        day14Event2.drugUID = "745751"

        currentTime = LocalTime.of(14, 15, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day14Event3 = Model.InhaleEvent(1, eventTime, timezoneOffset, 21, 1178, 912, 430, 0, 6,
                3, "Cartridge3", 0, 0, true, "234565432")
        day14Event3.drugUID = "745751"

        currentTime = LocalTime.of(17, 15, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day14Event4 = Model.InhaleEvent(1, eventTime, timezoneOffset, 19, 1315, 831, 491, 0, 7,
                4, "Cartridge3", 0, 0, true, "234565432")
        day14Event4.drugUID = "745751"

        currentTime = LocalTime.of(20, 15, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day14Event5 = Model.InhaleEvent(1, eventTime, timezoneOffset, 26, 1068, 702, 399, 0, 8,
                5, "Cartridge3", 0, 0, true, "234565432")
        day14Event5.drugUID = "745751"

        currentTime = LocalTime.of(23, 45, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day14Event6 = Model.InhaleEvent(1, eventTime, timezoneOffset, 22, 1413, 553, 532, 0, 6,
                6, "Cartridge3", 0, 0, true, "234565432")
        day14Event6.drugUID = "745751"

        relieverInhaleEventHistory.add(day14Event1)
        relieverInhaleEventHistory.add(day14Event2)
        relieverInhaleEventHistory.add(day14Event3)
        relieverInhaleEventHistory.add(day14Event4)
        relieverInhaleEventHistory.add(day14Event5)
        relieverInhaleEventHistory.add(day14Event6)

        val day14 = HistoryDay(currentDate)
        val day14Dose1 = HistoryDose("745751", listOf(day14Event1))
        day14Dose1.isReliever = true
        val day14Dose2 = HistoryDose("745751", listOf(day14Event2))
        day14Dose2.isReliever = true
        val day14Dose3 = HistoryDose("745751", listOf(day14Event3))
        day14Dose3.isReliever = true
        val day14Dose4 = HistoryDose("745751", listOf(day14Event4))
        day14Dose4.isReliever = true
        day14Dose4.isTooSoon = true
        val day14Dose5 = HistoryDose("745751", listOf(day14Event5))
        day14Dose5.isReliever = true
        day14Dose5.isTooSoon = true
        val day14Dose6 = HistoryDose("745751", listOf(day14Event6))
        day14Dose6.isReliever = true
        day14Dose6.isTooSoon = true
        day14.relieverDoses.add(day14Dose1)
        day14.relieverDoses.add(day14Dose2)
        day14.relieverDoses.add(day14Dose3)
        day14.relieverDoses.add(day14Dose4)
        day14.relieverDoses.add(day14Dose5)
        day14.relieverDoses.add(day14Dose6)
        day14.dailyUserFeeling = dailyUserFeelingHistory[currentDate]
        day14.connectedInhalerCount = connectionMetaHistory[currentDate]!!
        day14.prescriptions = oldPrescriptions

        // 1/17/2020
        currentDate = startDate.minusDays(14)
        currentTime = LocalTime.of(10, 51, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day15Event1 = Model.InhaleEvent(1, eventTime, timezoneOffset, 15, 935, 502, 439, 8, 6,
                1, "Cartridge3", 0, 0, false, "234565432") // exhalation
        day15Event1.drugUID = "745751"

        relieverInhaleEventHistory.add(day15Event1)

        val day15 = HistoryDay(currentDate)
        val day15Dose1 = HistoryDose("745751", listOf(day15Event1))
        day15Dose1.isReliever = true
        day15Dose1.hasIssues = true
        day15.invalidDoses.add(day15Dose1)
        day15.dailyUserFeeling = dailyUserFeelingHistory[currentDate]
        day15.connectedInhalerCount = connectionMetaHistory[currentDate]!!
        day15.prescriptions = oldPrescriptions

        // 1/16/2020
        currentDate = startDate.minusDays(15)
        currentTime = LocalTime.of(9, 27, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day16Event1 = Model.InhaleEvent(1, eventTime, timezoneOffset, 22, 1535, 1002, 769, 0, 7,
                1, "Cartridge3", 0, 0, true, "234565432")
        day16Event1.drugUID = "745751"

        relieverInhaleEventHistory.add(day16Event1)

        val day16 = HistoryDay(currentDate)
        val day16Dose1 = HistoryDose("745751", listOf(day16Event1))
        day16Dose1.isReliever = true
        day16.relieverDoses.add(day16Dose1)
        day16.dailyUserFeeling = dailyUserFeelingHistory[currentDate]
        day16.connectedInhalerCount = connectionMetaHistory[currentDate]!!
        day16.prescriptions = oldPrescriptions

        // 1/15/2020
        currentDate = startDate.minusDays(16)
        currentTime = LocalTime.of(10, 17, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day17Event1 = Model.InhaleEvent(1, eventTime, timezoneOffset, 28, 1562, 903, 736, 0, 8,
                1, "Cartridge3", 0, 0, true, "234565432")
        day17Event1.drugUID = "745751"

        currentTime = LocalTime.of(17, 34, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day17Event2 = Model.InhaleEvent(1, eventTime, timezoneOffset, 21, 1143, 802, 766, 0, 6,
                2, "Cartridge3", 0, 0, true, "234565432")
        day17Event2.drugUID = "745751"

        relieverInhaleEventHistory.add(day17Event1)
        relieverInhaleEventHistory.add(day17Event2)

        val day17 = HistoryDay(currentDate)
        val day17Dose1 = HistoryDose("745751", listOf(day17Event1))
        day17Dose1.isReliever = true
        val day17Dose2 = HistoryDose("745751", listOf(day17Event2))
        day17Dose2.isReliever = true
        day17.relieverDoses.add(day17Dose1)
        day17.relieverDoses.add(day17Dose2)
        day17.dailyUserFeeling = dailyUserFeelingHistory[currentDate]
        day17.connectedInhalerCount = connectionMetaHistory[currentDate]!!
        day17.prescriptions = oldPrescriptions

        // 1/14/2020
        // no data
        currentDate = startDate.minusDays(17)
        val day18 = HistoryDay(currentDate)
        day18.dailyUserFeeling = dailyUserFeelingHistory[currentDate]
        day18.connectedInhalerCount = connectionMetaHistory[currentDate]!!
        day18.prescriptions = oldPrescriptions

        // 1/13/2020
        // bad data
        currentDate = startDate.minusDays(18)
        currentTime = LocalTime.of(11, 3, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day19Event1 = Model.InhaleEvent(1, eventTime, timezoneOffset, 24, 1111, 500, 200, 2, 8,
                1, "Cartridge3", 0, 0, false, "234565432")
        day19Event1.drugUID = "745751"

        relieverInhaleEventHistory.add(day19Event1)

        val day19 = HistoryDay(currentDate)
        day19.dailyUserFeeling = dailyUserFeelingHistory[currentDate]
        day19.connectedInhalerCount = connectionMetaHistory[currentDate]!!
        day19.prescriptions = oldPrescriptions

        // 1/12/2020
        currentDate = startDate.minusDays(19)
        currentTime = LocalTime.of(8, 55, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day20Event1 = Model.InhaleEvent(1, eventTime, timezoneOffset, 21, 1235, 702, 469, 0, 6,
                1, "Cartridge3", 0, 0, true, "234565432")
        day20Event1.drugUID = "745751"

        relieverInhaleEventHistory.add(day20Event1)

        val day20 = HistoryDay(currentDate)
        val day20Dose1 = HistoryDose("745751", listOf(day20Event1))
        day20Dose1.isReliever = true
        day20.relieverDoses.add(day20Dose1)
        day20.dailyUserFeeling = dailyUserFeelingHistory[currentDate]
        day20.connectedInhalerCount = connectionMetaHistory[currentDate]!!
        day20.prescriptions = oldPrescriptions

        // 1/11/2020
        currentDate = startDate.minusDays(20)
        currentTime = LocalTime.of(8, 1, 1)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day21Event1 = Model.InhaleEvent(1, eventTime, timezoneOffset, 22, 1535, 1002, 769, 16, 7,
                1, "Cartridge3", 0, 0, false, "234565432") // time stamp error
        day21Event1.drugUID = "745751"

        currentDate = startDate.minusDays(20)
        currentTime = LocalTime.of(13, 15, 0)
        eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val day21Event2 = Model.InhaleEvent(1, eventTime, timezoneOffset, 23, 1124, 803, 739, 0, 6,
                2, "Cartridge3", 0, 0, true, "234565432")
        day21Event2.drugUID = "745751"

        relieverInhaleEventHistory.add(day21Event1)
        relieverInhaleEventHistory.add(day21Event2)

        val day21 = HistoryDay(currentDate)
        val day21Dose2 = HistoryDose("745751", listOf(day21Event2))
        day21Dose2.isReliever = true
        day21.relieverDoses.add(day21Dose2)
        day21.dailyUserFeeling = dailyUserFeelingHistory[currentDate]
        day21.connectedInhalerCount = connectionMetaHistory[currentDate]!!
        day21.prescriptions = oldPrescriptions

        historyDays.add(day1)
        historyDays.add(day2)
        historyDays.add(day3)
        historyDays.add(day4)
        historyDays.add(day5)
        historyDays.add(day6)
        historyDays.add(day7)
        historyDays.add(day8)
        historyDays.add(day9)
        historyDays.add(day10)
        historyDays.add(day11)
        historyDays.add(day12)
        historyDays.add(day13)
        historyDays.add(day14)
        historyDays.add(day15)
        historyDays.add(day16)
        historyDays.add(day17)
        historyDays.add(day18)
        historyDays.add(day19)
        historyDays.add(day20)
        historyDays.add(day21)

        for (historyDay in historyDays) {
            calculatePifForHistoryDayWithRelieverDoses(historyDay)
        }

        return historyDays
    }

    /**
     * This method computes the PIF value for a history day.

     * @param historyDay - the history day for which PIF value needs to be computed.
     */
    private fun calculatePifForHistoryDayWithRelieverDoses(historyDay: HistoryDay) {
        var totalPif = 0
        var eventCount = 0
        for (dose in historyDay.relieverDoses) {
            for (event in dose.events) {
                totalPif += event.peakInspiratoryFlow
                eventCount++
            }
        }

        if (eventCount > 0) {
            historyDay.pif = totalPif / eventCount
        }
    }
}
