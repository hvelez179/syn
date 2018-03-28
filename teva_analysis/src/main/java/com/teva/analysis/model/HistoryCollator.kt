//
// HistoryCollator.kt
// teva_analysis
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.analysis.model

import android.support.annotation.WorkerThread

import org.threeten.bp.LocalDate

/**
 * This interface defines methods for collating historical data of the application..
 */
@WorkerThread
interface HistoryCollator {
    /**
     * This method returns a collection of HistoryDay objects from the start to end dates, inclusive.
     *
     * @param startDate - this parameter contains the start date for the history range.
     * @param endDate   - this parameter contains the end date for the history range.
     * @return - returns a list of HistoryDay objects for the date range passed in.
     */
    fun getHistory(startDate: LocalDate, endDate: LocalDate): List<HistoryDay>
}
