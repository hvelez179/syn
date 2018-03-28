///
// AlarmServiceImpl.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.common.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.os.SystemClock
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.DEBUG
import com.teva.utilities.utilities.Logger.Level.VERBOSE
import com.teva.common.utilities.marshall
import com.teva.common.utilities.unmarshall
import org.threeten.bp.Instant
import java.util.*

/**
 * A service that allows alarms to be scheduled to occurs at specific times.
 * The OS will wake up or start the process when the alarm time occurs.
 */
class AlarmServiceImpl(private val dependencyProvider: DependencyProvider) : AlarmService {
    private val ALARM_DATA_KEY = "alarmData"
    private val ALARM_REPEATING_KEY = "repeatingAlarm"

    private val logger = Logger(AlarmService::class)
    private val clientList: MutableMap<String, AlarmServiceCallback>
    private val context: Context = dependencyProvider.resolve()

    init {
        Receiver.alarmService = this
        clientList = HashMap<String, AlarmServiceCallback>()
    }

    /**
     * Register a callback for an alarm
     *
     * @param id       The client id
     * @param callback The interface to call when the alarm occurs.
     */
    override fun register(id: String, callback: AlarmServiceCallback) {
        clientList[id] = callback
    }

    /**
     * Unregister a callback for an alarm.
     *
     * @param id The client id
     */
    override fun unregister(id: String) {
        clientList.remove(id)
    }


    /**
     * Creates a PendingIntent to be used in setting an alarm in the Android AlarmManager.
     *
     * @param alarmId The intent action
     * @param data    Private data associated with the alarm.
     * @return A PendingIntent created from the action and data.
     */
    private fun createPendingIntent(alarmId: String, isRepeating: Boolean, data: Parcelable?): PendingIntent {
        val intent = Intent()
        intent.setClass(context, RemoteReceiver::class.java)
        intent.action = alarmId
        intent.putExtra(ALARM_REPEATING_KEY, isRepeating)

        if (data != null) {
            intent.putExtra(ALARM_DATA_KEY, data.marshall())
        }

        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        return pendingIntent
    }

    /**
     * Gets an existing PendingIntent for the action, if one exists.
     *
     * @param action The intent action.
     * @return The existing PendingIntent for the action, or null if one does not exist.
     */
    private fun getExistingPendingIntent(action: String): PendingIntent? {
        val intent = Intent()
        intent.setClass(context, RemoteReceiver::class.java)
        intent.action = action

        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE)
    }

    /**
     * Checks to see if an alarm is scheduled for the specified alarm id.
     *
     * @param alarmId The id of the alarm to check.
     * @return True if the alarm is scheduled, false otherwise.
     */
    override fun isAlarmScheduled(alarmId: String): Boolean {
        // check to see if the pending intent exists
        val pendingIntent = getExistingPendingIntent(alarmId)
        return pendingIntent != null
    }

    /**
     * Sets an alarm relative to the current time. Only one active alarm is allowed per alarm id.
     *
     * @param alarmId      The id of the alarm.  The alarm id must be prefixed with the client id.
     *                     ex: "com.package.service.myservice.MY_ALARM", where
     *                     "com.package.service.myservice" is the client id.
     * @param milliseconds The time offset.
     * @param data         Client data.
     */
    override fun setAlarm(alarmId: String, milliseconds: Long, data: Parcelable?) {
        logger.log(DEBUG, "setAlarm() $alarmId $milliseconds")

        val alarmManager: AlarmManager = dependencyProvider.resolve()

        val pendingIntent = createPendingIntent(alarmId, false, data)

        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + milliseconds,
                pendingIntent)
    }

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
    override fun setRepeatingAlarm(alarmId: String, startMilliseconds: Long, repeatMilliseconds: Long, data: Parcelable?) {
        logger.log(DEBUG, "setRepeatingAlarm() $alarmId $startMilliseconds $repeatMilliseconds")

        val alarmManager: AlarmManager = dependencyProvider.resolve()

        val pendingIntent = createPendingIntent(alarmId, true, data)

        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + startMilliseconds,
                repeatMilliseconds,
                pendingIntent)
    }


    /**
     * Sets an alarm at a specific time. Only one active alarm is allowed per alarm id.
     *
     * @param alarmId The id of the alarm.  The alarm id must be prefixed with the client id.
     *                ex: "com.package.service.myservice.MY_ALARM", where
     *                "com.package.service.myservice" is the client id.
     * @param time    The time to wake up the alarm.
     * @param data    Client data.
     */
    override fun setAlarm(alarmId: String, time: Instant, data: Parcelable?) {
        logger.log(DEBUG, "setAlarm() $alarmId $time")

        val alarmManager: AlarmManager = dependencyProvider.resolve()

        val pendingIntent = createPendingIntent(alarmId, false, data)

        alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                time.toEpochMilli(),
                pendingIntent)
    }

    /**
     * Cancels an alarm
     *
     * @param alarmId The id of the client whose alarm should be canceled.
     */
    override fun cancelAlarm(alarmId: String) {
        logger.log(DEBUG, "cancelAlarm() $alarmId")

        val alarmManager: AlarmManager = dependencyProvider.resolve()

        val pendingIntent = getExistingPendingIntent(alarmId)
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    /**
     * This method searches for a registered client whose client id is prefix of the alarm id.
     *
     * @param alarmId The alarm id to find a client for.
     * @return The registered Client for the alarm id.
     */
    private fun findClient(alarmId: String): AlarmServiceCallback? {
        // Search for a client whose client id is prefix of the alarm id.
        var client: AlarmServiceCallback? = null
        for ((key, value) in clientList) {
            if (alarmId.startsWith(key)) {
                client = value
                break
            }
        }

        return client
    }

    /**
     * Receives an alarm intent from the broadcast receiver.
     *
     * @param intent The alarm intent
     */
    private fun onReceive(intent: Intent) {
        val alarmId = intent.action

        val isRepeating = intent.getBooleanExtra(ALARM_REPEATING_KEY, false)

        if (!isRepeating) {
            val pendingIntent = getExistingPendingIntent(alarmId)

            pendingIntent?.cancel()
        }

        logger.log(DEBUG, "onReceive() $alarmId")

        val client = findClient(alarmId)

        if (client != null) {
            val data = intent.getByteArrayExtra(ALARM_DATA_KEY)?.unmarshall(client.javaClass.classLoader)
            client.onAlarm(alarmId, data)
        }
    }

    /**
     * This broadcast receiver is configured to run in a separate process so that if
     * the user kills the app, the alarms will still be received.
     */
    class RemoteReceiver : BroadcastReceiver() {

        /**
         * This method is called when the BroadcastReceiver is receiving an Intent
         * broadcast.
         *
         * @param context The Context in which the receiver is running.
         * @param intent  The Intent being received.
         */
        override fun onReceive(context: Context, intent: Intent) {
            logger.log(VERBOSE, "onReceive")
            // resend to the main process receiver.
            intent.setClass(context, Receiver::class.java)
            context.sendBroadcast(intent)
        }

        companion object {
            private val logger = Logger(RemoteReceiver::class)
        }
    }

    /**
     * The broadcast receiver that will receive the alarm intents from the OS.
     */
    class Receiver : BroadcastReceiver() {

        /**
         * This method is called when the BroadcastReceiver is receiving an Intent
         * broadcast.
         *
         * @param context The Context in which the receiver is running.
         * @param intent  The Intent being received.
         */
        override fun onReceive(context: Context, intent: Intent) {
            logger.log(VERBOSE, "onReceive")
            alarmService?.onReceive(intent)
        }

        companion object {
            private val logger = Logger(Receiver::class)
            internal var alarmService: AlarmServiceImpl? = null
        }
    }
}
