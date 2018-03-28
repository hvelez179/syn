///
// NotificationPresenter.kt
// teva_notifications
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.notifications.services

import com.teva.notifications.services.notification.NotificationInfo

/**
 * This interface is implemented by an application object that builds notifications
 * for the notification service.
 */
interface NotificationPresenter {
    /**
     * This method displays an Android notification for the specified NotificationData.
     *
     * @param notificationInfo The NotificationData containing the description of the notification.
     */
    fun displayNotification(notificationInfo: NotificationInfo)

    /**
     * Sets a value indicating whether the app is in the foreground.
     *
     * @param inForeground True if the app is in the foreground, false otherwise.
     */
    fun setInForeground(inForeground: Boolean)
}
