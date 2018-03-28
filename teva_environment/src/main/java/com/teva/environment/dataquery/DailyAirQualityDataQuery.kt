///
// DailyAirQualityDataQuery.kt
// teva_environment
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.environment.dataquery

import com.teva.common.dataquery.DataQueryForTrackedModels
import com.teva.environment.entities.DailyAirQuality
import org.threeten.bp.LocalDate

/**
 * Classes conforming to this interface allow access to the daily air quality data.
 */
interface DailyAirQualityDataQuery : DataQueryForTrackedModels<DailyAirQuality> {

    /**
     * This method gets the daily air quality for the given date.
     * - Parameters:
     * - date: This parameter specifies the date for the air quality query.
     * - Returns: Returns the DailyAirQuality for the specified date.
     */
    fun get(date: LocalDate): DailyAirQuality?

    /**
     * This method returns the daily air quality within the given date range.
     * - Parameters:
     * - startDate: This parameter specifies the start date for the air quality query.
     * - endDate: This parameter specifies the end date for the air quality query.
     * - Returns: Returns a map of date and daily air quality within the given date range.
     */
    fun get(startDate: LocalDate, endDate: LocalDate): Map<LocalDate, DailyAirQuality>
}
