///
// NotificationServiceImpl.kt
// teva_notifications
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.notifications.services

import android.os.Parcelable

import com.teva.common.services.AlarmService
import com.teva.common.services.AlarmServiceCallback
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.*
import com.teva.notifications.enumerations.RepeatType
import com.teva.notifications.services.notification.NotificationInfo
import com.teva.notifications.services.notification.NotificationDataKey
import com.teva.notifications.services.notification.RecurringScheduledNotificationInfo
import com.teva.notifications.services.notification.ScheduledNotificationInfo
import org.threeten.bp.*

import org.threeten.bp.temporal.ChronoUnit

/**
 * This class provides the ability to schedule and cancel Android notifications.
 *
 *
 * Android does not provide a facility to schedule notifications for future display so
 * The NotificationService uses the AlarmService for scheduling.  To schedule a notification,
 * an alarm containing the information about the notification is set for the scheduled time.
 * When the NotificationService is notified that the alarm expired, it retrieves the
 * notification information from the alarm an displays the notification.
 *
 * @param dependencyProvider The dependency injection container.
 */
class NotificationServiceImpl(dependencyProvider: DependencyProvider)
    : NotificationService, AlarmServiceCallback {

    private val logger = Logger(NotificationServiceImpl::class)
    private val ALARM_SERVICE_ID = NotificationServiceImpl::class.java.canonicalName + ".Alarm"

    private val alarmService: AlarmService = dependencyProvider.resolve()
    private val timeService: TimeService = dependencyProvider.resolve()
    private val notificationPresenter: NotificationPresenter = dependencyProvider.resolve()

    init {
        alarmService.register(ALARM_SERVICE_ID, this)
    }

    /**
     * Creates an alarm id by appending the notification id to the AlarmService client id.
     *
     * @param notificationId The notification id to create an alarm id for.
     * @return The alarm id created for the specified notification id.
     */
    private fun createAlarmId(notificationId: String): String {
        return ALARM_SERVICE_ID + "." + notificationId
    }

    /**
     * This method returns an indication whether the notification with the ID passed in,
     * is scheduled.
     *
     * @param notificationId This parameter is used to find the scheduled notification.
     */
    override fun isNotificationScheduled(notificationId: String): Boolean {
        val alarmId = createAlarmId(notificationId)
        return alarmService.isAlarmScheduled(alarmId)
    }

    /**
     * This method schedules an immediate, scheduled, or recurring notificationData.
     *
     * @param notificationInfo This parameter contains information used to present the notificationData,
     *                         including the ID, and notificationData data.  It is expected to be
     *                         a NotificationData, ScheduledNotification,
     *                         or RecurringScheduledNotification object.
     */
    override fun scheduleNotification(notificationInfo: NotificationInfo) {
        val data = notificationInfo.notificationData
        var notificationId = data[NotificationDataKey.NOTIFICATION_ID] as String?
        if (notificationId == null) {
            notificationId = notificationInfo.categoryId
            data.put(NotificationDataKey.NOTIFICATION_ID, notificationId as Any)
        }

        if (notificationInfo is ScheduledNotificationInfo) {

            val fireDateApplicationTime = notificationInfo.fireDateApplicationTime
            val fireDateRealTime = timeService.getRealTimeFromDate(fireDateApplicationTime)

            logger.log(ERROR, "Scheduling notification "
                    + notificationId
                    + " for "
                    + fireDateRealTime.toString())

            alarmService.setAlarm(createAlarmId(notificationId), fireDateRealTime, notificationInfo)

        } else {
            logger.log(DEBUG, "Presenting immediate notification " + notificationId)
            notificationPresenter.displayNotification(notificationInfo)
        }
    }

    /**
     * This method searches the scheduledLocalNotifications for a specific ID,
     * and cancels it if it exists.
     *
     * @param notificationId This parameter is used to find the scheduled notification to cancel.
     *                       It is the same as the ID used to the scheduled notification.
     */
    override fun cancelScheduledNotification(notificationId: String) {
        alarmService.cancelAlarm(createAlarmId(notificationId))
    }

    /**
     * Callback method from the AlarmService that indicates when an scheduled notification's
     * alarm has expired.
     *
     * @param id   The alarm id.
     * @param data The alarm data.
     */
    override fun onAlarm(id: String, data: Parcelable?) {
        logger.log(VERBOSE, "onAlarm: " + id)

        if (data is NotificationInfo) {
            logger.log(DEBUG, "Received notification alarm " + id)
            notificationPresenter.displayNotification(data)
        }

        if (data is RecurringScheduledNotificationInfo) {
            val recurringNotification = data
            val fireDateApplicationTime = recurringNotification.fireDateApplicationTime
            val newFireDateApplicationTime: Instant?

            // If there is a change in system time(user changes to a future date), schedule the next notification such that
            // it is configured according to the current system date.
            val currentTime = timeService.now()
            val elapsedDays = Duration.between(fireDateApplicationTime, currentTime).toDays()
            val DAYS_PER_WEEK = 7
            val elapsedWeeks = elapsedDays / DAYS_PER_WEEK
            val elapsedMonths = ChronoUnit.MONTHS.between(LocalDateTime.ofInstant(fireDateApplicationTime, ZoneId.systemDefault()), LocalDateTime.ofInstant(currentTime, ZoneId.systemDefault()))
            val incrementByDays = if(elapsedDays > 0) elapsedDays + 1 else 1
            val incrementByWeeks = if(elapsedWeeks > 0) elapsedWeeks + 1 else 1
            val incrementByMonths = if(elapsedMonths > 0) elapsedMonths + 1 else 1

            when (recurringNotification.repeatType) {
                RepeatType.ONCE_PER_DAY -> newFireDateApplicationTime = fireDateApplicationTime.plus(incrementByDays, ChronoUnit.DAYS)
                RepeatType.ONCE_PER_WEEK -> {
                    val zonedDateTime = ZonedDateTime.ofInstant(fireDateApplicationTime, ZoneId.systemDefault())
                    val updatedZonedDateTime = zonedDateTime.plusWeeks(incrementByWeeks)
                    newFireDateApplicationTime = Instant.from(updatedZonedDateTime)
                }
                RepeatType.MONTHLY -> {
                    // This implementation automatically calculates a valid date from the next month.
                    // For example, if the current date was 31st January, the new date would be
                    // the last valid date of February, similarly if the current date was
                    // 31st March, the new date would be 30th April etc.
                    val zonedDateTime2 = ZonedDateTime.ofInstant(fireDateApplicationTime, ZoneId.systemDefault())
                    val updatedZonedDateTime2 = zonedDateTime2.plusMonths(incrementByMonths)
                    newFireDateApplicationTime = Instant.from(updatedZonedDateTime2)
                }
                RepeatType.NONE -> newFireDateApplicationTime = null
            }

            if (newFireDateApplicationTime != null) {
                recurringNotification.fireDateApplicationTime = newFireDateApplicationTime
                scheduleNotification(recurringNotification)
            }
        }
    }
}
