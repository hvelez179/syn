//
// UserFeelingManager.kt
// teva_userfeedback
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.userfeedback.model

import android.support.annotation.WorkerThread

import com.teva.userfeedback.entities.DailyUserFeeling
import com.teva.userfeedback.enumerations.UserFeeling

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

/**
 * This interface defines methods to set a UserFeeling and retrieve past UserFeeling data.
 */
@WorkerThread
interface UserFeelingManager {
    /**
     * This method saves a UserFeeling for the date passed in. It updates the local database. It overwrites a previously stored value.
     *
     * @param userFeeling - The user feeling corresponding to the timestamp passed in.
     * @param timeStamp   - The date/time that the user sets user feeling.
     */
    fun saveUserFeeling(userFeeling: UserFeeling, timeStamp: Instant)

    /**
     * This method returns a UserFeeling for the date passed in, if it exists, otherwise Unknown.
     *
     * @param date - The date to get the UserFeeling.
     * @return - Returns a UserFeeling for the date passed in, if it exists, otherwise Unknown.
     */
    fun getUserFeelingAtDate(date: LocalDate): DailyUserFeeling

    /**
     * This method returns an array of DailyUserFeeling objects starting from the date passed in, to the current date. DailyUserFeeling objects contains a timestamp and UserFeeling.
     *
     * @param fromDate - The starting date.
     * @param toDate   - The ending date.
     * @return - Returns the history of DailyUserFeeling for the dates passed in.
     */
    fun getUserFeelingHistoryFromDate(fromDate: LocalDate, toDate: LocalDate): Map<LocalDate, DailyUserFeeling?>
}
