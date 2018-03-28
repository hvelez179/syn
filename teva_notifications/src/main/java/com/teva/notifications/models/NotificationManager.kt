///
// NotificationManager.kt
// teva_notifications
//
// Copyright © 2017 Teva. All rights reserved
///

@file:Suppress("KDocUnresolvedReference")

package com.teva.notifications.models

import com.teva.notifications.entities.ReminderSetting
import com.teva.notifications.enumerations.RepeatType

import org.threeten.bp.LocalTime

/**
 * This interface defines methods for scheduling notifications.
 */
interface NotificationManager {

    /**
     * This method returns a reminder setting with a specified name.
     *
     * @param name This parameter uniquely identifies the setting.
     * @return Returns the reminder setting with the specified name.
     */
    fun getReminderSettingByName(name: String): ReminderSetting?

    /**
     * This method is used to present a notification immediately.
     * To create multiple notifications with the same categoryId, add a unique ID to
     * notificationData[NotificationDataKey.NotificationId].
     *
     * @param categoryId       This parameter is used to look up notification text and category.
     * @param notificationData This parameter contains optional data that is returned to the notification handler.
     */
    fun setNotification(categoryId: String, notificationData: Map<String, Any>)

    /**
     * This method schedules a one-time notification corresponding to the ID passed in, at the passed-in time from now.
     * To create multiple notifications with the same categoryId, add a unique ID to
     * notificationData[NotificationDataKey.NotificationId].
     *
     * @param categoryId       This parameter is used to look up notification text and category.
     * @param secondsFromNow   This parameter is the number of seconds from now to present the reminder notification.
     * @param notificationData This parameter contains optional data that is returned to the notification handler.
     */
    fun setNotification(categoryId: String, notificationData: Map<String, Any>, secondsFromNow: Int)

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
     * @param scheduleForTomorrow This parameter indicates whether the notification should be scheduled for tomorrow.
     */
    fun setNotification(categoryId: String,
                        notificationData: Map<String, Any>,
                        reminderSetting: ReminderSetting,
                        scheduleForTomorrow: Boolean)

    /**
     * This method creates or disables a recurring notification with arbitrary repeat type (e.g., Monthly).
     * This method is not associated with a saved setting; i.e, the notification is always enabled.
     * If the notification was already scheduled, this method cancels the notification, and then schedules it with
     * the time provided.
     * To create multiple notifications with the same categoryId, add a unique ID to
     * notificationData[NotificationDataKey.NotificationId].
     *
     * @param categoryId       This parameter is used to look up notification text and category.
     * @param notificationData This parameter contains optional data that is returned to the notification handler.
     * @param daysFromNow      This parameter is the number of days from today to schedule the notification.
     * @param timeOfDay        This parameter is the time (wall-clock) that the notification is scheduled.
     * @param repeatType       This parameter specifies how the notification repeats, e.g., once per day, monthly.
     */
    fun setNotification(categoryId: String,
                        notificationData: Map<String, Any>,
                        daysFromNow: Int,
                        timeOfDay: LocalTime,
                        repeatType: RepeatType)

    /**
     * This method disables a scheduled notification.
     * If the scheduled notification has a corresponding setting, this method updates the enabled setting to false.
     * This method cancels the reminder notification if it is scheduled.
     *
     * @param categoryId - This parameter is used to find the corresponding notification setting.
     */
    fun disableNotification(categoryId: String)

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
    fun disableNotification(categoryId: String, notificationData: Map<String, Any>)

    /**
     * This method checks if there is an existing notification for the given notification Id.
     *
     * @param notificationId This parameter is used to uniquely identify a notification to be checked.
     * @return Returns true if there is an existing notification for the given notification id else false.
     */
    fun hasReminderSetting(notificationId: String): Boolean

    /**
     * This method saves a reminder setting without triggering a notification.
     *
     * @param reminderSetting The reminder setting to be saved.
     */
    fun saveReminderSettingByName(reminderSetting: ReminderSetting)
}
