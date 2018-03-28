//
// AnalyzedDataProvider.kt
// teva_analysis
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.analysis.model

import android.support.annotation.MainThread
import android.support.annotation.WorkerThread

import com.teva.analysis.entities.SummaryInfo

import org.threeten.bp.LocalDate

/**
 * The analyzed data provider is intended to serve as a cache for the history data.
 * The complete history data is not cached.
 * The cache is updated as and when additional data is retrieved
 */
interface AnalyzedDataProvider {
    /**
     * Returns the date from which data is cached.
     *
     * @return -  the date from which data is cached.
     */
    @get:WorkerThread
    val trackingStartDate: LocalDate?

    /**
     * Returns the history for the specified date range.
     *
     * @param startDate - the start date of the date range.
     * @param endDate   - the end date of the date range.
     * @return -  the history of the specified date range.
     */
    @WorkerThread
    fun getHistory(startDate: LocalDate, endDate: LocalDate): List<HistoryDay>

    /**
     * Returns the summary message to be displayed on the dashboard.
     *
     * @return - The highest priority summary message to be displayed and null if no message exists.
     */
    @get:MainThread
    val summaryInfo: SummaryInfo?
}
