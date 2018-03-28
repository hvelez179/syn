///
// NotificationManagerImpl.kt
// teva_notifications
//
// Copyright © 2017 Teva. All rights reserved
///

@file:Suppress("KDocUnresolvedReference")

package com.teva.notifications.models


import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.notifications.dataquery.ReminderDataQuery
import com.teva.notifications.entities.ReminderSetting
import com.teva.notifications.enumerations.RepeatType
import com.teva.notifications.services.NotificationService
import com.teva.notifications.services.notification.NotificationDataKey
import com.teva.notifications.services.notification.NotificationInfo
import com.teva.notifications.services.notification.RecurringScheduledNotificationInfo
import com.teva.notifications.services.notification.ScheduledNotificationInfo
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneOffset

/**
 * This class implements the NotificationManager interface used for scheduling notifications.
 * @param dependencyProvider The dependency provider.
 */
class NotificationManagerImpl(dependencyProvider: DependencyProvider) : NotificationManager {
    private val notificationService: NotificationService = dependencyProvider.resolve()
    private val reminderDataQuery: ReminderDataQuery = dependencyProvider.resolve()
    private val timeService: TimeService = dependencyProvider.resolve()

    /**
     * This method returns a reminder setting with a specified name.
     *
     * @param name This parameter uniquely identifies the setting.
     * @return Returns the reminder setting with the specified name.
     */
    override fun getReminderSettingByName(name: String): ReminderSetting? {
        return reminderDataQuery.get(name)
    }

    /**
     * This method is used to present a notification immediately.
     * To create multiple notifications with the same categoryId, add a unique ID to
     * notificationData[NotificationDataKey.NotificationId].
     *
     * @param categoryId       This parameter is used to look up notification text and category.
     * @param notificationData This parameter contains optional data that is returned to the notification handler.
     */
    override fun setNotification(categoryId: String, notificationData: Map<String, Any>) {
        val notification = NotificationInfo(categoryId, notificationData)
        notificationService.scheduleNotification(notification)
    }

    /**
     * This method schedules a one-time notification corresponding to the ID passed in, at the passed-in time from now.
     * To create multiple notifications with the same categoryId, add a unique ID to
     * notificationData[NotificationDataKey.NotificationId].
     *
     * @param categoryId       This parameter is used to look up notification text and category.
     * @param secondsFromNow   This parameter is the number of seconds from now to present the reminder notification.
     * @param notificationData This parameter contains optional data that is returned to the notification handler.
     */
    override fun setNotification(categoryId: String, notificationData: Map<String, Any>, secondsFromNow: Int) {
        val now = timeService.now()
        val fireDateApplicationTime = now.plusSeconds(secondsFromNow.toLong())
        val scheduledNotificationInfo = ScheduledNotificationInfo(categoryId, notificationData, fireDateApplicationTime)
        notificationService.scheduleNotification(scheduledNotificationInfo)
    }

    /**
     * This method creates or disables a daily recurring notification.
     * Examples of recurring reminders are Take Dose on Time, and Daily User Feeling (VAS).
     * If enabled, this method schedules the notification.  If it was already scheduled, it cancels the notification,
     * and then schedules it with the time provided.
     * If disabled, this method cancels the notification.
     * This method updates the corresponding RecurringReminderSetting in the database.
     * To create multiple notifications with the same categoryId, add a unique ID to
     * notificationData[NotificationDataKey.NotificationId].
     *
     * @param categoryId          This parameter is used to look up notification text and category.
     * @param notificationData    This parameter contains optional data that is returned to the notification handler.
     * @param reminderSetting     This parameter contains settings to configure the reminder, including name,
     *                            enable flag, and time.
     * @param scheduleForTomorrow This parameter indicates whether the notification should be scheduled for tomorrow.
     */
    override fun setNotification(categoryId: String,
                                 notificationData: Map<String, Any>,
                                 reminderSetting: ReminderSetting, scheduleForTomorrow: Boolean) {
        val daysFromNow = if (scheduleForTomorrow) 1 else 0
        reminderDataQuery.insertOrUpdate(reminderSetting, true)
        scheduleNotification(categoryId, notificationData, reminderSetting.isEnabled, daysFromNow,
                reminderSetting.timeOfDay!!, reminderSetting.repeatType)
    }

    /**
     * This method creates or disables a recurring notification with arbitrary repeat type (e.g., Monthly).
     * This method is not associated with a saved setting; i.e, the notification is always enabled.
     * If the notification was already scheduled, this method cancels the notification, and then schedules
     * it with the time provided. To create multiple notifications with the same categoryId, add a unique ID
     * to notificationData[NotificationDataKey.NotificationId].
     *
     * @param categoryId       This parameter is used to look up notification text and category.
     * @param notificationData This parameter contains optional data that is returned to the notification handler.
     * @param daysFromNow      This parameter is the number of days from today to schedule the notification.
     *                         For example, to schedule for tomorrow, pass the value 1.
     * @param timeOfDay        This parameter is the time (wall-clock) that the notification is scheduled.
     * @param repeatType       This parameter specifies how the notification repeats, e.g., once per day, monthly.
     */
    override fun setNotification(categoryId: String,
                                 notificationData: Map<String, Any>,
                                 daysFromNow: Int,
                                 timeOfDay: LocalTime,
                                 repeatType: RepeatType) {
        scheduleNotification(categoryId, notificationData, true, daysFromNow, timeOfDay, repeatType)
    }

    /**
     * This method disables a scheduled notification.
     * If the scheduled notification has a corresponding setting, this method updates the enabled setting to false.
     * This method cancels the reminder notification if it is scheduled.
     *
     * @param categoryId This parameter is used to find the corresponding notification setting.
     */
    override fun disableNotification(categoryId: String) {
        // Get current settings, then set the enabled setting to false.
        val reminderSetting = reminderDataQuery.get(categoryId)

        if (reminderSetting != null) {
            reminderSetting.isEnabled = false
            reminderDataQuery.update(reminderSetting, true)
        }

        // If scheduled, cancel the notification.
        val isScheduled = notificationService.isNotificationScheduled(categoryId)
        if (isScheduled) {
            notificationService.cancelScheduledNotification(categoryId)
        }
    }

    /**
     * This method disables a scheduled notification.
     * If the scheduled notification has a corresponding setting, this method updates the enabled setting to false.
     * This method cancels the reminder notification if it is scheduled.
     * This overload provides the ability to specify a unique notification ID that is different from the category ID.
     * Add the unique ID to notificationData[NotificationDataKey.NotificationId].
     *
     * @param categoryId       This parameter is used to find the corresponding notification setting.
     * @param notificationData This parameter is a dictionary that contains a unique notification ID.
     */
    override fun disableNotification(categoryId: String, notificationData: Map<String, Any>) {
        // Get current settings, then set the enabled setting to false.
        val reminderSetting = reminderDataQuery.get(categoryId)
        if (reminderSetting != null) {
            reminderSetting.isEnabled = false
            reminderDataQuery.update(reminderSetting, true)

            val notificationId = notificationData[NotificationDataKey.NOTIFICATION_ID] as String
            // If scheduled, cancel the notification.
            if (notificationService.isNotificationScheduled(notificationId)) {
                notificationService.cancelScheduledNotification(notificationId)
            }
        }
    }

    /**
     * This method checks if there is an existing notification for the given notification Id.
     *
     * @param notificationId This parameter is used to uniquely identify a notification to be checked.
     * @return Returns true if there is an existing notification for the given notification id else false.
     */
    override fun hasReminderSetting(notificationId: String): Boolean {
        return reminderDataQuery.hasData(notificationId)
    }

    /**
     * This method saves a reminder setting without triggering a notification.
     *
     * @param reminderSetting The reminder setting to be saved.
     */
    override fun saveReminderSettingByName(reminderSetting: ReminderSetting) {
        reminderDataQuery.insertOrUpdate(reminderSetting, true)
    }

    /**
     * This method schedules a recurring notification with the ID passed in, if the enabled parameter is true.
     * Regardless of the enabled parameter, this method cancels a previously scheduled notification, with the same ID,
     * before scheduling the new one.
     *
     * @param categoryId       This parameter is used to look up notification text and category.
     * @param notificationData This parameter contains optional data that is returned to the notification handler.
     * @param isEnabled        This parameter indicates whether to schedule the notification.  If false, this method
     *                         only cancels the notification.  If true, it first cancels the notification,
     *                         then schedules the notification.
     * @param daysFromNow      This parameter is the number of days from today to schedule the notification.
     *                         For example, to schedule for tomorrow, pass the value 1.
     * @param timeOfDay        This parameter is the time (wall-clock) that the notification is scheduled.
     * @param repeatType       This parameter specifies how the notification repeats, e.g., once per day, monthly.
     */
    private fun scheduleNotification(categoryId: String,
                                     notificationData: Map<String, Any>,
                                     isEnabled: Boolean,
                                     daysFromNow: Int,
                                     timeOfDay: LocalTime,
                                     repeatType: RepeatType) {

        val SECONDS_PER_DAY = 86400
        val MINUTES_PER_HOUR = 60

        // Cancel notification, regardless of enabled flag.
        if (notificationService.isNotificationScheduled(categoryId)) {
            notificationService.cancelScheduledNotification(categoryId)
        }

        if (isEnabled) {
            // Get today's notification time
            val today = timeService.today()
            val localNotificationTime = LocalDateTime.of(today, timeOfDay)

            // Get current time zone offset
            val timeOffsetInMinutes = timeService.timezoneOffsetMinutes!!
            val zoneOffset = ZoneOffset.ofHoursMinutes(
                    timeOffsetInMinutes / MINUTES_PER_HOUR, timeOffsetInMinutes % MINUTES_PER_HOUR)

            // Convert local notification time to an instant
            var notificationTime = localNotificationTime.toInstant(zoneOffset)

            // If notification is to be configured after a specified number of days, add those number of days
            notificationTime = notificationTime.plusSeconds((daysFromNow * SECONDS_PER_DAY).toLong())

            // Get the current time.
            val now = timeService.now()

            // If the notification time has passed, schedule for the next day.
            if (now.isAfter(notificationTime)) {
                notificationTime = notificationTime.plusSeconds(SECONDS_PER_DAY.toLong())
            }

            // Look up the notification messages and category from the notificationTranslator.
            val recurringScheduledNotificationInfo = RecurringScheduledNotificationInfo(
                    categoryId,
                    notificationData,
                    notificationTime,
                    repeatType)

            notificationService.scheduleNotification(recurringScheduledNotificationInfo)
        }
    }
}
