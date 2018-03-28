//
// TimeServiceImpl.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.common.services

import android.content.SharedPreferences
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.DEBUG
import com.teva.common.utilities.Messenger
import org.threeten.bp.*

/**
 * Service that provides the current time and allows time
 * to be accelerated.
 *
 * @param dependencyProvider The dependency injection mechanism
 */
class TimeServiceImpl(private val dependencyProvider: DependencyProvider) : TimeService {

    private var hyperFactor: Double = MINUTES_IN_DAY / REALTIME_MINUTE_TO_DAY_FACTOR

    /**
     * Gets the reference OS time used to calculate the current application time.
     */
    override var referenceTime: Instant = systemNow

    /**
     * Gets the reference application time used to calculate the current application time.
     */
    override var referenceHypertime: Instant = referenceTime

    /**
     * Gets the time mode for the TimeService.
     */
    override var timeMode: RunModes = RunModes.REALTIME
        set(value) {
            field = value

            when (value) {
                RunModes.REALTIME -> hyperFactor = MINUTES_IN_DAY / REALTIME_MINUTE_TO_DAY_FACTOR

                RunModes.MEDIUM -> hyperFactor = MINUTES_IN_DAY / MEDIUM_MINUTE_TO_DAY_FACTOR

                RunModes.FAST -> hyperFactor = MINUTES_IN_DAY / FAST_MINUTE_TO_DAY_FACTOR

                RunModes.HYPER -> hyperFactor = MINUTES_IN_DAY / HYPERTIME_MINUTE_TO_DAY_FACTOR
            }
        }

    // retrieve the current System time using a dependency provider factory to facilitate testing.
    private val systemNow: Instant
        get() = dependencyProvider.resolve()

    init {
        val settings = dependencyProvider.resolve<SharedPreferences>(SETTINGS_NAME)

        val hypertimeMilliseconds = settings.getLong(REFERENCE_HYPERTIME_KEY, 0)
        val timeMilliseconds = settings.getLong(REFERENCE_TIME_KEY, 0)

        if (timeMilliseconds != 0L && hypertimeMilliseconds != 0L) {
            // initialize from saved settings
            timeMode = RunModes.fromRawValue(settings.getInt(TIME_MODE_KEY, 1))
            referenceTime = Instant.ofEpochMilli(timeMilliseconds)
            referenceHypertime = Instant.ofEpochMilli(hypertimeMilliseconds)
        } else {
            // first time, so save the default real-time settings
            saveTimeSettings()
        }
    }

    /**
     * Sets properties based on the run mode passed in. If null, it reads the saved value.
     * If the passed-in run mode is not null, this method reschedules notifications, with a new time adjusted for the
     * run mode.
     *
     * @param newMode This parameter is the run mode.  If null, use value saved.
     */
    override fun initializeTimeService(newMode: RunModes) {
        var updated = false

        if (timeMode != newMode) {
            referenceHypertime = currentTime
            referenceTime = systemNow
            timeMode = newMode
            updated = true
        }

        if (updated) {
            saveTimeSettings()

            val messenger = dependencyProvider.resolve(Messenger::class.java)
            messenger.publish(UpdateTimeMessage())
        }

        logger.log(DEBUG, "mode: $timeMode referenceTime: $referenceTime hyperTime: $referenceHypertime")
    }

    /**
     * Saves the time settings to the SharedPreferences
     */
    private fun saveTimeSettings() {
        val settings = dependencyProvider.resolve<SharedPreferences>(SETTINGS_NAME)

        val editor = settings.edit()
        editor.putInt(TIME_MODE_KEY, timeMode.rawValue)
        editor.putLong(REFERENCE_TIME_KEY, referenceTime.toEpochMilli())
        editor.putLong(REFERENCE_HYPERTIME_KEY, referenceHypertime.toEpochMilli())
        editor.apply()
    }

    /**
     * Gets the current application time as an Instant.
     */
    override fun now(): Instant {
        return currentTime
    }

    /**
     * Gets the current application time as a LocalDate.
     */
    override fun today(): LocalDate {
        return LocalDateTime.ofInstant(currentTime, dependencyProvider.resolve<ZoneId>()).toLocalDate()
    }

    /**
     * Gets the current application time as a LocalTime
     */
    override fun localTime(): LocalTime {
        return LocalDateTime.ofInstant(currentTime, dependencyProvider.resolve<ZoneId>()).toLocalTime()
    }

    /**
     * Gets the OS time from an instant based on the current Time Mode.
     *
     * @param targetDate The Instant to convert into OS time.
     * @return The OS time corresponding to the target application time.
     */
    override fun getRealTimeFromDate(targetDate: Instant): Instant {
        val elapsedHyperMilliseconds = targetDate.toEpochMilli() - referenceHypertime.toEpochMilli()
        val elapsedTime = (elapsedHyperMilliseconds / hyperFactor).toLong()

        return referenceTime.plusMillis(elapsedTime)
    }

    /**
     * Gets an application time from an OS time.
     *
     * @param date The OS time.
     * @return The application time corresponding to the OS time.
     */
    override fun getApplicationTime(date: Instant): Instant {
        val elapsedTime = date.toEpochMilli() - referenceTime.toEpochMilli()
        val elapsedHyperTime = (elapsedTime * hyperFactor).toLong()

        return referenceHypertime.plusMillis(elapsedHyperTime)
    }

    /**
     * Gets an application time Instant by converting an OS time interval into a application time interval and
     * adding it to the current application time.
     *
     * @param intervalInSeconds The OS time interval in seconds.
     * @return The hypertime co
     */
    override fun getTimeFromRealTimeInterval(intervalInSeconds: Int): Instant {
        val hyperMilliseconds = intervalInSeconds.toLong() * 1000 * hyperFactor.toLong()
        return Instant.ofEpochMilli(currentTime.toEpochMilli() + hyperMilliseconds)
    }

    /**
     * Gets the timezone offset for the current application time.
     */
    override val timezoneOffsetMinutes: Int
        get() {
            val zonedDateTime = ZonedDateTime.ofInstant(now(), dependencyProvider.resolve<ZoneId>())
            val zoneOffset = zonedDateTime.offset
            val seconds = zoneOffset.totalSeconds.toLong()
            return (seconds / SECONDS_IN_MINUTE).toInt()
        }

    /**
     * Calculates the current application time from the reference times.
     * @return The current application time.
     */
    private val currentTime: Instant
        get() {
            val elapsedMilliseconds = systemNow.toEpochMilli() - referenceTime.toEpochMilli()
            val elapsedHyperMilliseconds = (elapsedMilliseconds * hyperFactor).toLong()

            return Instant.ofEpochMilli(referenceHypertime.toEpochMilli() + elapsedHyperMilliseconds)
        }

    companion object {
        private val logger = Logger("TimeService")

        private val SETTINGS_NAME = "TimeService"
        private val REFERENCE_TIME_KEY = "ReferenceTime"
        private val REFERENCE_HYPERTIME_KEY = "ReferenceHypertime"
        private val TIME_MODE_KEY = "TimeMode"

        private val REALTIME_MINUTE_TO_DAY_FACTOR = 1440.0
        private val MEDIUM_MINUTE_TO_DAY_FACTOR = 10.0
        private val FAST_MINUTE_TO_DAY_FACTOR = 4.0
        private val HYPERTIME_MINUTE_TO_DAY_FACTOR = 0.5

        private val SECONDS_IN_MINUTE = 60
        private val MINUTES_IN_DAY = 1440
    }


}
