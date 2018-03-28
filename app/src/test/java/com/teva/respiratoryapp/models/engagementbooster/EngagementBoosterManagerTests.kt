//
// EngagementBoosterManagerTests.java
// app
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.models.engagementbooster

import com.nhaarman.mockito_kotlin.*
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.notifications.enumerations.RepeatType
import com.teva.notifications.models.NotificationManager
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId

/**
 * This class defines unit tests for the EngagementBoosterManagerImpl class.
 */
class EngagementBoosterManagerTests {

    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var timeService: TimeService
    private lateinit var messenger: Messenger
    private lateinit var notificationManager: NotificationManager
    
    private val now = Instant.ofEpochSecond(1492714854L)
    private val zoneId = ZoneId.of("UTC-04:00")

    @Before
    fun setup() {
        dependencyProvider = DependencyProvider.default
        timeService = mock()
        whenever(timeService.now()).thenReturn(now)
        dependencyProvider.register(TimeService::class, timeService)
        messenger = mock()
        dependencyProvider.register(Messenger::class, messenger)
        notificationManager = mock()
        dependencyProvider.register(NotificationManager::class, notificationManager)
        dependencyProvider.register(ZoneId::class, zoneId)
    }

    @Test
    fun testUpdateEngagementBoosterMessageSetsFourEngagementBoosterNotifications() {

        val NUMBER_OF_ENGAGEMENT_NOTIFICATIONS = 4
        val FIRST_NOTIFICATION_INTERVAL = 7
        val SECOND_NOTIFICATION_INTERVAL = 14
        val THIRD_NOTIFICATION_INTERVAL = 21
        val FOURTH_NOTIFICATION_INTERVAL = 28
        val engagementBoosterManager = EngagementBoosterManagerImpl(dependencyProvider)
        engagementBoosterManager.onUpdateEngagementBooster(UpdateEngagementBoosterMessage())

        val notificationIdCaptor = argumentCaptor<String>()
        val daysCaptor = ArgumentCaptor.forClass(Int::class.java)

        // verify that four notifications are scheduled.
        verify(notificationManager, times(NUMBER_OF_ENGAGEMENT_NOTIFICATIONS)).setNotification(notificationIdCaptor.capture(), any(), daysCaptor.capture(), any(), eq(RepeatType.MONTHLY))

        // verify that the notifications are scheduled in the correct order.
        assertEquals(EngagementBoosterNotificationId.TRACKING, notificationIdCaptor.allValues[0])
        assertEquals(EngagementBoosterNotificationId.DAILY_SELF_ASSESSMENT_TOOL, notificationIdCaptor.allValues[1])
        assertEquals(EngagementBoosterNotificationId.CREATE_USER_REPORT, notificationIdCaptor.allValues[2])
        assertEquals(EngagementBoosterNotificationId.ENVIRONMENT, notificationIdCaptor.allValues[3])

        // verify that the notifications are separated by 7 days.
        assertTrue(daysCaptor.allValues[0] == FIRST_NOTIFICATION_INTERVAL)
        assertTrue(daysCaptor.allValues[1] == SECOND_NOTIFICATION_INTERVAL)
        assertTrue(daysCaptor.allValues[2] == THIRD_NOTIFICATION_INTERVAL)
        assertTrue(daysCaptor.allValues[3] == FOURTH_NOTIFICATION_INTERVAL)
    }
}
