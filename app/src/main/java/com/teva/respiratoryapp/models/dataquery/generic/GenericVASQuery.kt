//
// GenericVASQuery.java
// app
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.models.dataquery.generic

import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.entities.DailyUserFeelingDataEncrypted
import com.teva.userfeedback.dataquery.DailyUserFeelingDataQuery
import com.teva.userfeedback.entities.DailyUserFeeling
import org.threeten.bp.LocalDate
import java.util.*

/**
 * Instances of this class provide access to the daily user feeling data.
 */
abstract class GenericVASQuery(
        dependencyProvider: DependencyProvider,
        mapper: DataMapper<DailyUserFeeling, DailyUserFeelingDataEncrypted>)
    : GenericQueryBaseForTrackedModels<DailyUserFeeling, DailyUserFeelingDataEncrypted>(
        dependencyProvider, DailyUserFeelingDataEncrypted::class.java, mapper),
        DailyUserFeelingDataQuery {

    /**
     * Returns the search criteria for returning a unique daily user feeling managed object.

     * @param model - the daily user feeling object from which to create the search criteria.
     * *
     * @return - the search criteria for returning a unique daily user feeling.
     */
    override fun uniqueSearchCriteria(model: DailyUserFeeling): SearchCriteria {
        return SearchCriteria("date = %@", model.date!!)
    }

    /**
     * This method reset the cache for queries which use cache for storing objects.
     * Since this query does not use cache, we do nothing here.
     */
    override fun resetCache() {}

    /**
     * Gets the daily user feeling for the given date.

     * @param date - the date to get the Daily User Feeling.
     * *
     * @return - Returns the Daily User Feeling for the specified date, if found. Otherwise returns null.
     */
    override operator fun get(date: LocalDate): DailyUserFeeling? {
        val queryInfo = QueryInfo(SearchCriteria("date = %@", date))
        val dailyUserFeelings = readBasedOnQuery(queryInfo)
        if (dailyUserFeelings.isNotEmpty())
            return dailyUserFeelings[0]

        return null
    }

    /**
     * Returns a map of daily user feeling for each date within the given date range.

     * @param startDate - the start date of the date range.
     * *
     * @param endDate   - the end date of the date range.
     * *
     * @return - a map containing the daily user feeling for each date in the specified range.
     */
    override operator fun get(startDate: LocalDate, endDate: LocalDate): Map<LocalDate, DailyUserFeeling> {
        val queryInfo = QueryInfo(SearchCriteria("date >= %@ AND date <= %@", startDate, endDate))
        val dailyUserFeelings = readBasedOnQuery(queryInfo)
        val userFeelingsByDate = HashMap<LocalDate, DailyUserFeeling>()

        for (item in dailyUserFeelings) {
            userFeelingsByDate.put(item.date!!, item)
        }

        return userFeelingsByDate
    }
}
