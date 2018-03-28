//
// HistoryCollatorImpl.kt
// teva_analysis
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.analysis.model

import android.support.annotation.WorkerThread
import com.teva.analysis.entities.HistoryDose
import com.teva.analysis.extensions.enumerations.InhalationEffort
import com.teva.analysis.extensions.inhalationEffort
import com.teva.common.utilities.*
import com.teva.utilities.utilities.Logger.Level.DEBUG
import com.teva.utilities.utilities.Logger.Level.VERBOSE
import com.teva.devices.dataquery.ConnectionMetaDataQuery
import com.teva.devices.dataquery.InhaleEventDataQuery
import com.teva.devices.entities.InhaleEvent
import com.teva.medication.dataquery.MedicationDataQuery
import com.teva.medication.dataquery.PrescriptionDataQuery
import com.teva.medication.entities.Medication
import com.teva.userfeedback.model.UserFeelingManager
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import org.threeten.bp.*
import org.threeten.bp.temporal.ChronoUnit.DAYS
import java.util.*

/**
 * This class collates historical data of the application.
 * @param dependencyProvider - the dependency provider.
 */
@WorkerThread
class HistoryCollatorImpl(private val dependencyProvider: DependencyProvider) : HistoryCollator {
    private val logger = Logger(HistoryCollatorImpl::class)

    private val medicationDataQuery: MedicationDataQuery = dependencyProvider.resolve<MedicationDataQuery>()
    private val inhaleEventDataQuery: InhaleEventDataQuery = dependencyProvider.resolve<InhaleEventDataQuery>()


    /**
     * This method returns a collection of HistoryDay objects from the start to end dates, inclusive.
     *
     * @param startDate - this parameter contains the start date for the history range.
     * @param endDate   - this parameter contains the end date for the history range.
     * @return - returns a list of HistoryDay objects for the date range passed in.
     */
    override fun getHistory(startDate: LocalDate, endDate: LocalDate): List<HistoryDay> {
        logger.log(VERBOSE, "getHistory: $startDate - $endDate")

        val inhaleEvents = inhaleEventDataQuery.get(startDate, endDate)
        val medications = medicationDataQuery.getAll()
        val collatedEvents = collateEvents(inhaleEvents, medications, startDate, endDate)

        return collatedEvents
    }

    /**
     * This method collates the list of inhale events into HistoryDay objects for the specified date range.
     *
     * @param eventsToCollate - the inhale events to be collated.
     * @param medications     - the medications for matching the inhale events.
     * @param startDate       - the start date of the history range.
     * @param endDate         - the end date of the history range.
     * @return - a list of HistoryDay objects for the specified range.
     */
    private fun collateEvents(eventsToCollate: List<InhaleEvent>,
                              medications: List<Medication>,
                              startDate: LocalDate, endDate: LocalDate): List<HistoryDay> {
        logger.log(DEBUG, "collateEvents")

        // make a copy so that the list can be manipulated without affecting the
        // the list that was passed in.
        val inhaleEvents = ArrayList(eventsToCollate)

        // create a date wise mapping of the HistoryDay information for the specified range.
        val historyByDate = createDateWiseHistory(startDate, endDate)

        // sort the inhale events by open time in ascending order
        Collections.sort(inhaleEvents) { inhaleEvent2, inhaleEvent1 -> Duration.between(inhaleEvent1.eventTime, inhaleEvent2.eventTime).seconds.toInt() }

        for (medication in medications) {

            // retrieve all InhaleEvents for the current medication.
            val medicationInhaleEvents: List<InhaleEvent> = inhaleEvents.filter { it.drugUID == medication.drugUID }

            inhaleEvents.removeAll(medicationInhaleEvents)

            // collate event information.
            // Todo - Controller dose collation to be handled later.
            if (medication.isReliever) {
                collateRelieverDoses(historyByDate, medication, medicationInhaleEvents)
            }

        }

        // Build an array of the history days, sorted by day
        // and generate the PIF for each day
        val historyDays = SortedArrayList<HistoryDay>()
        for (day in historyByDate.values) {

            // calculate the average PIF
            if (day.relieverDoses.size > 0) {
                val pifTotal = day.relieverDoses
                        .flatMap { it.events }
                        .sumBy { it.peakInspiratoryFlow }

                day.pif = pifTotal / day.relieverDoses.size
            }

            historyDays.insertSorted(day, false)
        }

        // return the list of HistoryDay objects.
        return historyDays.toList()
    }

    /**
     * This method creates a map of each day in the specified range and the corresponding HistoryDay object.
     *
     * @param startDate - the start date of the range.
     * @param endDate   -  the end date of the range.
     * @return - returns a map of each day in the specified range and the corresponding HistoryDay object.
     */
    private fun createDateWiseHistory(startDate: LocalDate, endDate: LocalDate): Map<LocalDate, HistoryDay> {
        val historyDays = HashMap<LocalDate, HistoryDay>()

        // build a map relating the drugID to the medication.
        val medicationMap = medicationDataQuery.getAll().associateBy { it.drugUID }

        val prescriptionDataQuery = dependencyProvider.resolve<PrescriptionDataQuery>()
        val prescriptions = prescriptionDataQuery.getAll()

        // update the medication property in the prescriptions to have the full medication object
        // instead of the limited version.
        for (prescription in prescriptions) {
            val drugID = prescription.medication!!.drugUID
            prescription.medication = medicationMap[drugID]
        }

        // sort the prescriptions by date in descending order
        Collections.sort(prescriptions) { prescription2, prescription1 ->
            Duration.between(prescription2.prescriptionDate, prescription1.prescriptionDate)
                    .seconds.toInt()
        }

        var historyDate = endDate
        val numberOfDays = DAYS.between(startDate, endDate)

        // iterate from the end date to the start date in the range.
        for (dayIndex in 0..numberOfDays) {
            val historyDay = HistoryDay(historyDate)

            // add matching prescriptions to history day.
            for (prescription in prescriptions) {
                val zonedPrescriptionDate = ZonedDateTime.ofInstant(prescription.prescriptionDate!!,
                        ZoneId.systemDefault())
                val prescriptionDate = LocalDate.from(zonedPrescriptionDate)

                if (prescriptionDate.isBefore(historyDate)) {
                    historyDay.prescriptions.add(prescription)
                }
            }

            // if no prescriptions got added, add the entire collection.
            if (historyDay.prescriptions.size == 0) {
                // This would need to be tweaked if we have multiple controller drugs and switching
                // from a prescription from one drug to another.
                historyDay.prescriptions = prescriptions.toMutableList()
            }

            // add the history day to the map and move to the previous day.
            historyDays.put(historyDate, historyDay)
            historyDate = historyDate.minusDays(1)
        }

        // retrieve the user feeling and connection information.
        val userFeelingManager = dependencyProvider.resolve<UserFeelingManager>()
        val dailyUserFeelingHistory = userFeelingManager.getUserFeelingHistoryFromDate(
                startDate, endDate)

        val connectionMetaDataQuery = dependencyProvider.resolve<ConnectionMetaDataQuery>()
        val connectionInhalerCountHistory = connectionMetaDataQuery.get(startDate, endDate)

        // add the air quality, user feeling and connection information to the corresponding history day.
        for ((key, historyDay) in historyDays) {
            historyDay.dailyUserFeeling = dailyUserFeelingHistory[key]
            historyDay.connectedInhalerCount = connectionInhalerCountHistory[key] ?: 0
        }

        // return the map.
        return historyDays
    }

    /**
     * This method collates reliever events into HistoryDay objects.
     *
     * @param historyByDate          - map between days and HistoryDay objects.
     * @param medication             - the reliever medication for the inhale events are being collated.
     * @param medicationInhaleEvents - the inhale events being collated.
     */
    private fun collateRelieverDoses(historyByDate: Map<LocalDate, HistoryDay>,
                                     medication: Medication,
                                     medicationInhaleEvents: List<InhaleEvent>) {
        val secondsPerMinute = 60
        val minutesPerHour = 60

        var lastEvent: InhaleEvent? = null

        // iterate through the inhale events.
        for (currentEvent in medicationInhaleEvents) {
            val inhaleEventTimezoneOffset = currentEvent.timezoneOffsetMinutes
            val inhaleEventZoneId = ZoneOffset.ofHoursMinutes(inhaleEventTimezoneOffset / minutesPerHour,
                    inhaleEventTimezoneOffset % minutesPerHour)
            val inhaleEventDate = LocalDateTime.ofInstant(currentEvent.eventTime, inhaleEventZoneId)
                    .toLocalDate()
            val currentDay = historyByDate[inhaleEventDate] ?: continue

            // create a dose.
            val dose = HistoryDose(medication.drugUID, listOf(currentEvent))
            dose.isReliever = true
            dose.hasIssues = currentEvent.hasIssues

            val inhalationEffort = currentEvent.inhalationEffort
            if (inhalationEffort.isAcceptable) {

                // Check to see if this dose is too close to the previous one.
                if (lastEvent != null) {
                    val intervalBetweenDoses = Duration.between(lastEvent.eventTime,
                            currentEvent.eventTime).seconds.toInt() / secondsPerMinute
                    if (intervalBetweenDoses < medication.minimumDoseInterval) {
                        dose.isTooSoon = true
                    }
                }

                // add to reliever doses.
                currentDay.relieverDoses.add(dose)
                lastEvent = currentEvent
            } else if (inhalationEffort === InhalationEffort.SYSTEM_ERROR) {
                currentDay.systemErrorDoses.add(dose)
            } else {
                // inhalation effort is not acceptable. add as an invalid dose.
                currentDay.invalidDoses.add(dose)

            }
        }
    }

}
