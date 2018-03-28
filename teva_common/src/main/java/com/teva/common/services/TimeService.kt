//
// TimeService.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.common.services

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

/**
 * Service that provides the current time and allows time
 * to be accelerated.
 */
interface TimeService {

    /**
     * Sets properties based on the run mode passed in. If null, it reads the saved value.
     * If the passed-in run mode is not null, this method reschedules notifications, with a new time adjusted for the run mode.
     * @param newMode This parameter is the run mode.  If null, use value saved.
     */
    fun initializeTimeService(newMode: RunModes)

    /**
     * Gets the current application time as an Instant.
     */
    fun now(): Instant

    /**
     * Gets the current application time as a LocalDate.
     */
    fun today(): LocalDate

    /**
     * Gets the current application time as a LocalTime
     */
    fun localTime(): LocalTime

    /**
     * Gets the OS time from an instant based on the current Time Mode.
     *
     * @param targetDate The Instant to convert into OS time.
     * @return The OS time corresponding to the target application time.
     */
    fun getRealTimeFromDate(targetDate: Instant): Instant

    /**
     * Gets an application time Instant by converting an OS time interval into a application time interval and
     * adding it to the current application time.
     *
     * @param intervalInSeconds The OS time interval in seconds.
     * @return The hypertime co
     */
    fun getTimeFromRealTimeInterval(intervalInSeconds: Int): Instant

    /**
     * Gets an application time from an OS time.
     *
     * @param date The OS time.
     * @return The application time corresponding to the OS time.
     */
    fun getApplicationTime(date: Instant): Instant

    /**
     * Gets the time mode for the TimeService.
     */
    val timeMode: RunModes?

    /**
     * Gets the reference OS time used to calculate the current application time.
     */
    val referenceTime: Instant?

    /**
     * Gets the reference application time used to calculate the current application time.
     */
    val referenceHypertime: Instant?


    /**
     * Gets the timezone offset for the current application time.
     */
    val timezoneOffsetMinutes: Int?
}
