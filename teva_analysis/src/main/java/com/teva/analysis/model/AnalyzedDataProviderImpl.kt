//
// AnalyzedDataProviderImpl.kt
// teva_analysis
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.analysis.model


import android.support.annotation.MainThread

import com.teva.analysis.entities.SummaryInfo
import com.teva.analysis.messages.HistoryUpdatedMessage
import com.teva.analysis.messages.UpdateAnalysisDataMessage
import com.teva.analysis.model.datamonitors.DeviceDataMonitor
import com.teva.analysis.model.datamonitors.EnvironmentDataMonitor
import com.teva.analysis.model.datamonitors.InhalationDataMonitor
import com.teva.analysis.model.datamonitors.SummaryMessageQueue
import com.teva.common.services.TimeService
import com.teva.common.utilities.DataTask
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.common.utilities.Messenger
import com.teva.common.utilities.Stopwatch
import com.teva.devices.dataquery.InhaleEventDataQuery
import com.teva.medication.dataquery.MedicationDataQuery

import org.greenrobot.eventbus.Subscribe
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

import java.util.ArrayList

import com.teva.utilities.utilities.Logger.Level.VERBOSE

/**
 * This class is an implementation of the AnalyzedDataProvider interface.
 *
 * @param dependencyProvider The dependency injection mechanism
 * @param daysInCache Number of days for which hte history will be cached
 */

class AnalyzedDataProviderImpl(
        private val dependencyProvider: DependencyProvider,
        private val daysInCache: Int)
    : AnalyzedDataProvider {

    private val logger = Logger(AnalyzedDataProviderImpl::class)

    private val historyCollator: HistoryCollator = dependencyProvider.resolve<HistoryCollator>()
    private var cachedHistory: List<HistoryDay> = ArrayList()
    private var cacheValid: Boolean = false
    private val timeService: TimeService = dependencyProvider.resolve()
    private val inhaleEventDataQuery: InhaleEventDataQuery = dependencyProvider.resolve()
    private val medicationDataQuery: MedicationDataQuery = dependencyProvider.resolve()

    @Suppress("unused")
    private val deviceDataMonitor: DeviceDataMonitor = DeviceDataMonitor(dependencyProvider)
    @Suppress("unused")
    private val inhalationDataMonitor: InhalationDataMonitor = InhalationDataMonitor(dependencyProvider)
    @Suppress("unused")
    private val environmentDataMonitor: EnvironmentDataMonitor = EnvironmentDataMonitor(dependencyProvider)

    init {

        dependencyProvider.resolve<Messenger>().subscribe(this)

        updateCache(ArrayList())
    }

    /**
     * Returns the date from which data is cached.
     *
     * @return -  the date from which data is cached.
     */
    override val trackingStartDate: LocalDate?
        get() {
            val earliestEventDate = inhaleEventDataQuery.getEarliestInhaleEventDate()
            if (earliestEventDate != null) {
                return LocalDateTime.ofInstant(earliestEventDate, ZoneId.systemDefault()).toLocalDate()
            }

            val earliestPrescriptionDate = medicationDataQuery.earliestPrescriptionDate
            if (earliestPrescriptionDate != null) {
                return LocalDateTime.ofInstant(earliestPrescriptionDate, ZoneId.systemDefault()).toLocalDate()
            }

            return null
        }

    /**
     * Returns the history for the specified date range.
     *
     * @param startDate - the start date of the date range.
     * @param endDate   - the end date of the date range.
     * @return -  the history of the specified date range.
     */
    override fun getHistory(startDate: LocalDate, endDate: LocalDate): List<HistoryDay> {
        val stopwatch = Stopwatch.Start(logger)

        val history: List<HistoryDay>

        // copy values to local variables for swapping if required.
        var historyStartDate = startDate
        var historyEndDate = endDate

        if (historyStartDate.isAfter(historyEndDate)) {
            // if dates are in the wrong order swap them
            val temp = historyStartDate
            historyStartDate = historyEndDate
            historyEndDate = temp
        }

        val today = timeService.today()

        if (historyEndDate.isAfter(today)) {
            historyEndDate = today
        }

        val cachingEndDate = timeService.today()
        val cachingStartDate = cachingEndDate.minusDays(daysInCache.toLong())

        // if requested history range fits within the cached date range, return data from the cache
        if (cacheValid &&
                (historyStartDate.isAfter(cachingStartDate) || historyStartDate.isEqual(cachingStartDate)) &&
                (historyEndDate.isBefore(cachingEndDate) || historyEndDate.isEqual(cachingEndDate))) {

            // extract data for the required date range from cache.
            history = filterHistoryData(historyStartDate, historyEndDate)
        } else {
            // obtain history directly from the history collator.
            history = historyCollator.getHistory(historyStartDate, historyEndDate)
        }

        stopwatch.mark(VERBOSE, "getHistory()")
        return history
    }

    /**
     * Returns the summary message to be displayed on the dashboard.
     *
     * @return - The highest priority summary message to be displayed and null if no message exists.
     */
    override val summaryInfo: SummaryInfo?
        get() = dependencyProvider.resolve<SummaryMessageQueue>().topMessage

    /**
     * Returns the history data from the cache corresponding to the date range specified.
     *
     * @param startDate - the start date of the date range.
     * @param endDate   - the end date of the date range.
     * @return - the history data from cache corresponding to the specified date range.
     */
    private fun filterHistoryData(startDate: LocalDate, endDate: LocalDate): List<HistoryDay> {

//        val hd = cachedHistory.filter {  }

        val historyDays = ArrayList<HistoryDay>()
        for (historyDay in cachedHistory) {
            val historyDate = historyDay.day
            if ((historyDate.isAfter(startDate) || historyDate.isEqual(startDate)) && (historyDate.isBefore(endDate) || historyDate.isEqual(endDate))) {
                historyDays.add(historyDay)
            }
        }

        return historyDays
    }

    /**
     * Updates the Cache as the result of the changes to the passed in objects.
     */
    @MainThread
    private fun updateCache(objectsChanged: List<Any>) {
        val cachingEndDate = timeService.today()
        val cachingStartDate = cachingEndDate.minusDays(daysInCache.toLong())

        DataTask<Unit, List<HistoryDay>>("AnalyzedDataProvider_UpdateCache")
                .inBackground {
                    historyCollator.getHistory(cachingStartDate, cachingEndDate)
                }
                .onResult { result ->
                    cachedHistory = result ?: ArrayList()
                    cacheValid = true

                    // Broadcast that the history has been updated.
                    dependencyProvider.resolve<Messenger>()
                            .publish(HistoryUpdatedMessage(objectsChanged))
                }
                .execute()
    }

    /**
     * This method is the handler for the UpdateAnalysisDataMessage triggered when analysis data
     * needs to be updated due to events such as day change, inhalation, device connection etc.
     *
     * @param updateAnalysisDataMessage - The update analysis data message.
     */
    @Subscribe
    @MainThread
    fun onUpdateAnalysisData(updateAnalysisDataMessage: UpdateAnalysisDataMessage) {
        // Check for summary messages only if any Devices have changed.
        updateCache(updateAnalysisDataMessage.objectsChanged)
    }
}
