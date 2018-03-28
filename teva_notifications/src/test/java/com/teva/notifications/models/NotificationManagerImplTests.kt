//
// NotificationManagerImplTests.kt
// teva_notifications
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.notifications.models

import com.nhaarman.mockito_kotlin.*
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
import com.teva.notifications.utils.NotificationDataMatcher.matchesNotificationData
import com.teva.notifications.utils.NotificationDataMatcher.matchesRecurringScheduledNotification
import com.teva.notifications.utils.NotificationDataMatcher.matchesScheduledNotification
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.threeten.bp.*
import java.util.*

/**
 * This class contains unit tests for the NotificationManagerImpl class.
 */

class NotificationManagerImplTests {

    private lateinit var notificationService: NotificationService
    private lateinit var reminderDataQuery: ReminderDataQuery
    private lateinit var timeService: TimeService
    private lateinit var dependencyProvider: DependencyProvider

    @Before
    fun setup() {
        DependencyProvider.default.unregisterAll()

        notificationService = mock()
        reminderDataQuery = mock()
        timeService = mock()

        dependencyProvider = DependencyProvider.default
        dependencyProvider.register(NotificationService::class, notificationService)
        dependencyProvider.register(TimeService::class, timeService)
        dependencyProvider.register(ReminderDataQuery::class, reminderDataQuery)
    }

    @Test
    fun testGetReminderSettingByNameDelegatesCallToReminderDataQuery() {
        // create expectations
        val categoryId = "VASNotificationId.VASReminder"

        // perform operation
        val notificationManager = NotificationManagerImpl(dependencyProvider)
        notificationManager.getReminderSettingByName(categoryId)

        // check expectations
        // getReminderSettingByName simply delegates the call to ReminderDataQuery, so we only
        // check that the call to ReminderDataQuery was called.
        verify(reminderDataQuery).get(eq(categoryId))
    }

    @Test
    fun testThatSetNotificationSendsTheImmediateNotificationToService() {
        // create expectations
        val categoryId = "VASNotificationId.VASReminder"
        val notificationDataMap = HashMap<String, Any>()
        val notificationInfo = NotificationInfo(categoryId, notificationDataMap)
        val notificationDataArgumentCaptor = argumentCaptor<NotificationInfo>()
                
        // perform operation
        val notificationManager = NotificationManagerImpl(dependencyProvider)
        notificationManager.setNotification(categoryId, notificationDataMap)

        // check expectations

        // verify that NotificationService.scheduleNotification was called.
        verify(notificationService).scheduleNotification(notificationDataArgumentCaptor.capture())

        // verify that NotificationData passed to the service matches the expected value.
        assertThat(notificationInfo, matchesNotificationData(notificationDataArgumentCaptor.lastValue))

    }

    @Test
    fun testThatSetNotificationSendsTheScheduledNotificationToService() {
        // create expectations
        val categoryId = "VASNotificationId.VASReminder"
        val notificationDataMap = HashMap<String, Any>()
        val timeForNotificationFromNowInSeconds = 1200
        val currentTime = Instant.ofEpochMilli(1485960617000L)
        val firingTime = currentTime.plusSeconds(timeForNotificationFromNowInSeconds.toLong())
        val scheduledNotificationInfo = ScheduledNotificationInfo(categoryId, notificationDataMap, firingTime)
        val scheduledNotificationArgumentCaptor = argumentCaptor<ScheduledNotificationInfo>()
        whenever(timeService.now()).thenReturn(currentTime)

        // perform operation
        val notificationManager = NotificationManagerImpl(dependencyProvider)
        notificationManager.setNotification(categoryId, notificationDataMap, timeForNotificationFromNowInSeconds)

        // check expectations
        // verify that NotificationService.scheduleNotification was called
        verify(notificationService).scheduleNotification(scheduledNotificationArgumentCaptor.capture())

        // verify that the scheduled notification passed to the service matches the expected value.
        assertThat(scheduledNotificationInfo, matchesScheduledNotification(scheduledNotificationArgumentCaptor.lastValue))
    }

    @Test
    fun testThatSetNotificationWithReminderSettingUpdatesDatabaseAndSendsTheRecurringNotificationToService() {
        // create expectations
        val categoryId = "VASNotificationId.VASReminder"
        val timeOfDay = LocalTime.ofSecondOfDay(60)
        val reminderSetting = ReminderSetting()
        reminderSetting.timeOfDay = timeOfDay
        reminderSetting.repeatType = RepeatType.ONCE_PER_DAY
        reminderSetting.name = categoryId
        reminderSetting.isEnabled = true
        val notificationDataMap = HashMap<String, Any>()
        val currentDay = LocalDate.of(2017, 2, 1)
        val SECONDS_PER_DAY = 86400


        val currentTime = Instant.ofEpochMilli(1485960617000L)
        val daysFromNow = 1

        whenever(timeService.now()).thenReturn(currentTime)
        whenever(timeService.today()).thenReturn(currentDay)
        whenever(timeService.timezoneOffsetMinutes).thenReturn(-300)

        // Calculate the next notification time.
        val localNotificationTime = LocalDateTime.of(currentDay, timeOfDay)

        val timeOffsetInMinutes = timeService.timezoneOffsetMinutes!!
        val zoneOffset = ZoneOffset.ofHoursMinutes(timeOffsetInMinutes / 60, timeOffsetInMinutes % 60)

        var notificationTime = localNotificationTime.toInstant(zoneOffset)
        notificationTime = notificationTime.plusSeconds((daysFromNow * SECONDS_PER_DAY).toLong())

        val recurringScheduledNotificationInfo = RecurringScheduledNotificationInfo(
                categoryId,
                notificationDataMap,
                notificationTime,
                reminderSetting.repeatType)

        val recurringScheduledNotificationArgumentCaptor = argumentCaptor<RecurringScheduledNotificationInfo>()

        // set up mock to return that the notification had been previously scheduled.
        whenever(notificationService.isNotificationScheduled(categoryId)).thenReturn(true)

        // perform operation to schedule the recurring notification
        val notificationManager = NotificationManagerImpl(dependencyProvider)
        notificationManager.setNotification(categoryId, notificationDataMap, reminderSetting, false)

        // check expectations

        // verify that the previously scheduled notification was canceled.
        verify(notificationService).cancelScheduledNotification(eq(categoryId))

        // verify that the reminder setting was updated in the database.
        verify(reminderDataQuery).insertOrUpdate(eq(reminderSetting), eq(true))

        // verify that the scheduleNotification method on the service was invoked.
        verify(notificationService).scheduleNotification(recurringScheduledNotificationArgumentCaptor.capture())

        // verify that the  data sent to the service matches the expected data.
        assertThat(recurringScheduledNotificationInfo, matchesRecurringScheduledNotification(recurringScheduledNotificationArgumentCaptor.lastValue))

    }

    @Test
    fun testThatSetNotificationWithOutReminderSettingSendsTheRecurringNotificationToService() {
        // create expectations
        val categoryId = "VASNotificationId.VASReminder"
        val timeOfDay = LocalTime.ofSecondOfDay(60)
        val currentDay = LocalDate.of(2017, 2, 1)
        val notificationDataMap = HashMap<String, Any>()
        val SECONDS_PER_DAY = 86400


        val currentTime = Instant.ofEpochMilli(1485960617000L)
        val daysFromNow = 2

        whenever(timeService.now()).thenReturn(currentTime)
        whenever(timeService.today()).thenReturn(currentDay)
        whenever(timeService.timezoneOffsetMinutes).thenReturn(-300)

        // Calculate the next notification time.
        val localNotificationTime = LocalDateTime.of(currentDay, timeOfDay)

        val timeOffsetInMinutes = timeService.timezoneOffsetMinutes!!
        val zoneOffset = ZoneOffset.ofHoursMinutes(timeOffsetInMinutes / 60, timeOffsetInMinutes % 60)

        var notificationTime = localNotificationTime.toInstant(zoneOffset)
        notificationTime = notificationTime.plusSeconds((daysFromNow * SECONDS_PER_DAY).toLong())

        val recurringScheduledNotificationInfo = RecurringScheduledNotificationInfo(
                categoryId,
                notificationDataMap,
                notificationTime,
                RepeatType.ONCE_PER_DAY)

        //final ScheduledNotification scheduledNotification = new ScheduledNotification(categoryId, notificationDataMap, firingTime);
        val recurringScheduledNotificationArgumentCaptor = argumentCaptor<RecurringScheduledNotificationInfo>()

        // set up mock to return that the notification had been previously scheduled.
        whenever(notificationService.isNotificationScheduled(categoryId)).thenReturn(true)

        // perform operation to schedule the recurring notification
        val notificationManager = NotificationManagerImpl(dependencyProvider)
        notificationManager.setNotification(categoryId, notificationDataMap, daysFromNow, timeOfDay, RepeatType.ONCE_PER_DAY)

        // check expectations

        // verify that the previously scheduled notification was canceled.
        verify(notificationService).cancelScheduledNotification(eq(categoryId))


        // verify that the scheduleNotification method on the service was invoked.
        verify(notificationService).scheduleNotification(recurringScheduledNotificationArgumentCaptor.capture())

        // verify that the data sent to the service matches the expected data.
        assertThat(recurringScheduledNotificationInfo, matchesRecurringScheduledNotification(recurringScheduledNotificationArgumentCaptor.lastValue))

    }

    @Test
    fun testThatDisableReminderUpdatesDatabaseAndCancelsNotification() {
        // create expectations
        val categoryId = "VASNotificationId.VASReminder"
        val timeOfDay = LocalTime.ofSecondOfDay(60)
        val reminderSetting = ReminderSetting()
        reminderSetting.timeOfDay = timeOfDay
        reminderSetting.repeatType = RepeatType.ONCE_PER_DAY
        reminderSetting.name = categoryId
        reminderSetting.isEnabled = true
        whenever(reminderDataQuery.get(eq(categoryId))).thenReturn(reminderSetting)
        whenever(notificationService.isNotificationScheduled(eq(categoryId))).thenReturn(true)

        // perform operation
        val notificationManager = NotificationManagerImpl(dependencyProvider)
        notificationManager.disableNotification(categoryId)

        // check expectations
        verify(reminderDataQuery).get(eq(categoryId))
        verify(reminderDataQuery).update(eq(reminderSetting), eq(true))
        verify(notificationService).cancelScheduledNotification(eq(categoryId))
    }

    @Test
    fun testDisableReminderWithNotificationIdUpdatesDatabaseAndCancelsNotification() {
        // create expectations
        val categoryId = "VASNotificationId.VASReminder"
        val notificationId = "VASNotification"
        val timeOfDay = LocalTime.ofSecondOfDay(60)
        val reminderSetting = ReminderSetting()
        reminderSetting.timeOfDay = timeOfDay
        reminderSetting.repeatType = RepeatType.ONCE_PER_DAY
        reminderSetting.name = categoryId
        reminderSetting.isEnabled = true
        val notificationDataMap = HashMap<String, Any>()
        notificationDataMap.put(NotificationDataKey.NOTIFICATION_ID, notificationId)

        whenever(reminderDataQuery.get(eq(categoryId))).thenReturn(reminderSetting)
        whenever(notificationService.isNotificationScheduled(eq(notificationId))).thenReturn(true)

        // perform operation
        val notificationManager = NotificationManagerImpl(dependencyProvider)
        notificationManager.disableNotification(categoryId, notificationDataMap)

        // check expectations

        // verify that the reminder setting was retrieved from the database.
        verify(reminderDataQuery).get(eq(categoryId))

        // verify that the enabled property was set to false
        assertFalse(reminderSetting.isEnabled)

        // verify that the database was updated with the reminder setting.
        verify(reminderDataQuery).update(eq(reminderSetting), eq(true))

        // verify that the notification was canceled.
        verify(notificationService).cancelScheduledNotification(eq(notificationId))
    }

    @Test
    fun testThatHasReminderSettingChecksDatabaseForSetting() {
        // create expectations
        val notificationId = "VASNotification"
        whenever(reminderDataQuery.hasData(eq(notificationId))).thenReturn(false)

        // perform operation expecting a false result
        val notificationManager = NotificationManagerImpl(dependencyProvider)
        var notificationHasReminder = notificationManager.hasReminderSetting(notificationId)

        // check expectations
        verify<ReminderDataQuery>(reminderDataQuery).hasData(eq(notificationId))
        assertFalse(notificationHasReminder)

        // configure mock to return true
        whenever(reminderDataQuery.hasData(eq(notificationId))).thenReturn(true)

        // perform operation expecting a true result
        notificationHasReminder = notificationManager.hasReminderSetting(notificationId)

        // check expectations
        assertTrue(notificationHasReminder)
    }

    @Test
    fun testThatSaveReminderSettingUpdatesDatabase() {
        // create expectations
        val categoryId = "VASNotificationId.VASReminder"
        val timeOfDay = LocalTime.ofSecondOfDay(60)
        val reminderSetting = ReminderSetting()
        reminderSetting.timeOfDay = timeOfDay
        reminderSetting.repeatType = RepeatType.ONCE_PER_DAY
        reminderSetting.name = categoryId
        reminderSetting.isEnabled = true

        // perform operation
        val notificationManager = NotificationManagerImpl(dependencyProvider)
        notificationManager.saveReminderSettingByName(reminderSetting)

        // check expectations
        verify(reminderDataQuery).insertOrUpdate(eq(reminderSetting), eq(true))
    }
}
