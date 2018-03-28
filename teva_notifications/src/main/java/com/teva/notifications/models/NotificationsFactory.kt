//
// NotificationsFactory.kt
// teva_notifications
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.notifications.models

import com.teva.utilities.services.DependencyProvider

/**
 * This class provides the implementation of the notification manager.
 */
object NotificationsFactory {
    private var notificationManager: NotificationManager? = null

    /**
     * Returns an implementation of the notification manager.
     *
     * @param dependencyProvider The dependency provider.
     * @return An implementation of the notification manager.
     */
    fun getNotificationManager(dependencyProvider: DependencyProvider): NotificationManager {
        if (notificationManager == null) {
            notificationManager = NotificationManagerImpl(dependencyProvider)
        }

        return notificationManager!!
    }
}
