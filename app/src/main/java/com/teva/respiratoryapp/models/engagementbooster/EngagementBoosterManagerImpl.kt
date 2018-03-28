//
// EngagementBoosterManagerImpl.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.engagementbooster

import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.notifications.enumerations.RepeatType
import com.teva.notifications.models.NotificationManager
import com.teva.notifications.services.notification.NotificationDataKey
import org.greenrobot.eventbus.Subscribe
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import java.util.*

/**
 * This class implements the EngagementBoosterManager interface.
 *
 * @param dependencyProvider - the dependency provider.
 */
class EngagementBoosterManagerImpl(private val dependencyProvider: DependencyProvider)
    : EngagementBoosterManager {

    private val engagementBoosterNotificationIds = arrayListOf(
            EngagementBoosterNotificationId.TRACKING,
            EngagementBoosterNotificationId.DAILY_SELF_ASSESSMENT_TOOL,
            EngagementBoosterNotificationId.CREATE_USER_REPORT,
            EngagementBoosterNotificationId.ENVIRONMENT)

    init {
        dependencyProvider.resolve<Messenger>().subscribe(this)
    }

    /**
     * This method handles the UpdateEngagementBooster message and resets the
     * engagement booster notifications.
     *
     * @param updateEngagementBoosterMessage - the UpdateEngagementBooster message.
     */
    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onUpdateEngagementBooster(updateEngagementBoosterMessage: UpdateEngagementBoosterMessage) {
        val MAX_ENGAGEMENT_BOOSTER_NOTIFICATIONS = 4

        val now = dependencyProvider.resolve<TimeService>().now()
        val currentTime = LocalTime.from(now.atZone(dependencyProvider.resolve<ZoneId>()))

        for (index in 0 until MAX_ENGAGEMENT_BOOSTER_NOTIFICATIONS) {
            scheduleEngagementBoosterNotification(engagementBoosterNotificationIds[index], DAYS_PER_WEEK * (index + 1), currentTime)
        }
    }

    /**
     * This method schedules an engagement booster notification.
     *
     * @param notificationId - the id of the notification.
     * @param daysFromNow - the number of days after which the notification is to be scheduled
     * @param timeOfDay - the time of the day at which the notification must be scheduled.
     */
    private fun scheduleEngagementBoosterNotification(notificationId: String, daysFromNow: Int, timeOfDay: LocalTime) {
        val notificationData = HashMap<String, Any>()
        notificationData.put(NotificationDataKey.NOTIFICATION_ID, notificationId)
        dependencyProvider.resolve<NotificationManager>().setNotification(notificationId, notificationData, daysFromNow, timeOfDay, RepeatType.MONTHLY)
    }

    companion object {
        private val DAYS_PER_WEEK = 7
    }
}
