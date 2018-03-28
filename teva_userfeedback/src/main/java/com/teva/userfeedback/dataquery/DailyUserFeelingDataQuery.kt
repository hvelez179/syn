//
// DailyUserFeelingDataQuery.kt
// teva_userfeedback
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.userfeedback.dataquery

import com.teva.common.dataquery.DataQueryForTrackedModels
import com.teva.userfeedback.entities.DailyUserFeeling
import org.threeten.bp.LocalDate

/**
 * Classes conforming to this interface allow access to the daily user feeling data.
 */

interface DailyUserFeelingDataQuery : DataQueryForTrackedModels<DailyUserFeeling> {

    /**
     * Returns the daily user feeling for the given date.
     *
     * @property date The date of the DailyUserFeeling.
     */
    fun get(date: LocalDate): DailyUserFeeling?

    /**
     * Returns a map containing date and daily user feelings for the given date range.
     *
     * @property startDate The start date of the range.
     * @property endData The end date of the range.
     */
    fun get(startDate: LocalDate, endDate: LocalDate): Map<LocalDate, DailyUserFeeling?>
}
