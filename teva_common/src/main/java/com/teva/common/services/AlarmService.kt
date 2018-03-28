//
// AlarmService.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.common.services

import android.os.Parcelable
import org.threeten.bp.Instant

/**
 * A service that allows alarms to be scheduled to occurs at specific times.
 * The OS will wake up or start the process when the alarm time occurs.
 */
interface AlarmService {
    /**
     * Register a callback for an alarm
     *
     * @param id       The client id
     * @param callback The interface to call when the alarm occurs.
     */
    fun register(id: String, callback: AlarmServiceCallback)

    /**
     * Unregister a callback for an alarm.
     *
     * @param id The client id
     */
    fun unregister(id: String)

    /**
     * Checks to see if an alarm is scheduled for the specified alarm id.
     *
     * @param alarmId The id of the alarm to check.
     * @return True if the alarm is scheduled, false otherwise.
     */
    fun isAlarmScheduled(alarmId: String): Boolean

    /**
     * Sets an alarm relative to the current time. Only one active alarm is allowed per alarm id.
     *
     * @param alarmId      The id of the alarm.  The alarm id must be prefixed with the client id.
     *                     ex: "com.package.service.myservice.MY_ALARM", where
     *                     "com.package.service.myservice" is the client id.
     *
     * @param milliseconds The time offset.
     * @param data         Client data.
     */
    fun setAlarm(alarmId: String, milliseconds: Long, data: Parcelable?)

    /**
     * Sets a repeating alarm. Only one active alarm is allowed per alarm id.
     *
     * @param alarmId      The id of the alarm.  The alarm id must be prefixed with the client id.
     *                     ex: "com.package.service.myservice.MY_ALARM", where
     *                     "com.package.service.myservice" is the client id.
     * @param startMilliseconds The initial time interval.
     * @param repeatMilliseconds The repeat time interval.
     * @param data         Client data.
     */
    fun setRepeatingAlarm(alarmId: String, startMilliseconds: Long, repeatMilliseconds: Long, data: Parcelable?)

    /**
     * Sets an alarm at a specific time. Only one active alarm is allowed per alarm id.
     *
     * @param alarmId The id of the alarm.  The alarm id must be prefixed with the client id.
     *                ex: "com.package.service.myservice.MY_ALARM", where
     *                "com.package.service.myservice" is the client id.
     * @param time    The time to wake up the alarm.
     * @param data    Client data.
     */
    fun setAlarm(alarmId: String, time: Instant, data: Parcelable?)

    /**
     * Cancels an alarm
     *
     * @param alarmId The id of the client whose alarm should be canceled.
     */
    fun cancelAlarm(alarmId: String)
}
