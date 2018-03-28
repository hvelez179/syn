//
// GenericDailyAirQualityQuery.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.generic

import com.teva.utilities.services.DependencyProvider
import com.teva.environment.dataquery.DailyAirQualityDataQuery
import com.teva.environment.entities.DailyAirQuality
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.entities.DailyAirQualityDataEncrypted
import org.threeten.bp.LocalDate
import java.util.*

/**
 * Instances of this class provide access to the daily air quality data
 */
abstract class GenericDailyAirQualityQuery(
        dependencyProvider: DependencyProvider,
        mapper: DataMapper<DailyAirQuality, DailyAirQualityDataEncrypted>)
    : GenericQueryBaseForTrackedModels<DailyAirQuality, DailyAirQualityDataEncrypted>(
        dependencyProvider, DailyAirQualityDataEncrypted::class.java, mapper),
        DailyAirQualityDataQuery {

    /**
     * Returns a unique search criteria for the DailyAirQuality object.

     * @param model - the DailyAirQuality object for which the search criteria needs to be returned.
     * *
     * @return - the search criteria.
     */
    override fun uniqueSearchCriteria(model: DailyAirQuality): SearchCriteria {
        return SearchCriteria("date = %@", model.date!!)
    }

    /**
     * Implemented for query classes using caching. Since caching is not used for daily air quality,
     * we do not do anything in this method.
     */
    override fun resetCache() {}

    /**
     * This method returns the daily air quality for the given date.
     * - Parameters:
     * - date: This parameter specifies the date for the air quality query.
     * - Returns: Returns the DailyAirQuality for the specified date.
     */
    override fun get(date: LocalDate): DailyAirQuality? {
        val queryInfo = QueryInfo(SearchCriteria("date = %@", date))
        val dailyAirQualityEntries = readBasedOnQuery(queryInfo)

        if (dailyAirQualityEntries.isNotEmpty()) {
            return dailyAirQualityEntries[0]
        }

        return null
    }

    /**
     * This method returns the daily air quality within the given date range.
     * - Parameters:
     * - startDate: This parameter specifies the start date for the air quality query.
     * - endDate: This parameter specifies the end date for the air quality query.
     * - Returns: Returns a map of date and daily air quality within the given date range.
     */
    override fun get(startDate: LocalDate, endDate: LocalDate): Map<LocalDate, DailyAirQuality> {
        val queryInfo = QueryInfo(SearchCriteria("date >= %@ AND date <= %@", startDate, endDate))
        val dailyAirQualityEntries = readBasedOnQuery(queryInfo)
        val dateAndDailyAirQualityMap = HashMap<LocalDate, DailyAirQuality>()

        for (dailyAirQuality in dailyAirQualityEntries) {
            dateAndDailyAirQualityMap.put(dailyAirQuality.date!!, dailyAirQuality)
        }

        return dateAndDailyAirQualityMap
    }
}
