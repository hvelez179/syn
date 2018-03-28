///
// NotificationService.kt
// teva_notifications
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.notifications.services

import com.teva.notifications.services.notification.NotificationInfo

/**
 * This interface defines the ability to schedule notifications, check if a notification is
 * scheduled, and cancel a scheduled notification.
 */
interface NotificationService {
    /**
     * This method returns an indication whether the notification with the ID passed in,
     * is scheduled.
     *
     * @param notificationId This parameter is used to find the scheduled notification.
     */
    fun isNotificationScheduled(notificationId: String): Boolean

    /**
     * This method schedules an immediate, scheduled, or recurring notificationData.
     *
     * @param notificationInfo This parameter contains information used to present the notificationData,
     *                         including the ID, and notificationData data.  It is expected to be
     *                         a NotificationData, ScheduledNotification,
     *                         or RecurringScheduledNotification object.
     */
    fun scheduleNotification(notificationInfo: NotificationInfo)

    /**
     * This method searches the scheduledLocalNotifications for a specific ID,
     * and cancels it if it exists.
     *
     * @param notificationId This parameter is used to find the scheduled notification to cancel.
     *                       It is the same as the ID used to the scheduled notification.
     */
    fun cancelScheduledNotification(notificationId: String)

}
