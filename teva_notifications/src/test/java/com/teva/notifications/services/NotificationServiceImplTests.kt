//
// NotificationServiceImplTests.kt
// teva_notifications
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.notifications.services

import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.teva.common.services.AlarmService
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.notifications.enumerations.RepeatType
import com.teva.notifications.services.notification.NotificationDataKey
import com.teva.notifications.services.notification.NotificationInfo
import com.teva.notifications.services.notification.RecurringScheduledNotificationInfo
import com.teva.notifications.services.notification.ScheduledNotificationInfo
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import java.util.*

/**
 * This class contains unit tests for the NotificationServiceImpl class.
 */

class NotificationServiceImplTests {

    private lateinit var alarmService: AlarmService
    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var notificationPresenter: NotificationPresenter
    private lateinit var timeService: TimeService

    @Before
    fun setup() {
        DependencyProvider.default.unregisterAll()

        alarmService = mock()
        dependencyProvider = DependencyProvider.default
        dependencyProvider.register(AlarmService::class, alarmService)
        notificationPresenter = mock()
        dependencyProvider.register(NotificationPresenter::class, notificationPresenter)
        timeService = mock()
        dependencyProvider.register(TimeService::class, timeService)
    }

    @Test
    fun testThatIsNotificationIsScheduledCallsAlarmServiceWithExpectedAlarmId() {
        // create expectations
        val notificationId = "VASNotification"
        val alarmId = "com.teva.notifications.services.NotificationServiceImpl.Alarm." + notificationId

        // perform operation
        val notificationService = NotificationServiceImpl(dependencyProvider)
        notificationService.isNotificationScheduled(notificationId)

        // verify expectations
        verify(alarmService).isAlarmScheduled(eq(alarmId))
    }

    @Test
    fun testThatScheduleNotificationSendsImmediateNotificationDirectlyToNotificationPresenter() {
        // create expectations
        val categoryId = "ImmediateNotification"
        val notificationId = "VASNotification"

        val notificationDataMap = HashMap<String, Any>()
        notificationDataMap.put(NotificationDataKey.NOTIFICATION_ID, notificationId)

        val notificationInfo = NotificationInfo(categoryId, notificationDataMap)

        // perform operation
        val notificationService = NotificationServiceImpl(dependencyProvider)
        notificationService.scheduleNotification(notificationInfo)

        // verify expectations
        verify(notificationPresenter).displayNotification(eq(notificationInfo))
    }

    @Test
    fun testThatScheduleNotificationWithNotificationIdCreatesAnAlarmForAScheduledNotificationWithCorrectParameters() {
        // create expectations
        val categoryId = "ImmediateNotification"
        val notificationId = "VASNotification"
        val alarmId = "com.teva.notifications.services.NotificationServiceImpl.Alarm." + notificationId

        val notificationTime = Instant.from(ZonedDateTime.of(2017, 1, 20, 11, 7, 9, 0, GMT_ZONE_ID))
        whenever(timeService!!.getRealTimeFromDate(eq(notificationTime))).thenReturn(notificationTime)

        val notificationDataMap = HashMap<String, Any>()
        notificationDataMap.put(NotificationDataKey.NOTIFICATION_ID, notificationId)
        notificationDataMap.put(NotificationDataKey.FIRE_DATE_APPLICATION_TIME, notificationTime)

        val scheduledNotificationInfo = ScheduledNotificationInfo(categoryId, notificationDataMap, notificationTime)

        // perform operation
        val notificationService = NotificationServiceImpl(dependencyProvider!!)
        notificationService.scheduleNotification(scheduledNotificationInfo)

        // verify expectations
        verify(alarmService).setAlarm(eq(alarmId), eq(notificationTime), eq(scheduledNotificationInfo))
    }

    @Test
    fun testThatScheduleNotificationWithoutNotificationIdCreatesAnAlarmForAScheduledNotificationWithCorrectParameters() {
        // create expectations
        val categoryId = "ImmediateNotification"
        val alarmId = "com.teva.notifications.services.NotificationServiceImpl.Alarm." + categoryId

        val notificationTime = Instant.from(ZonedDateTime.of(2017, 1, 20, 11, 7, 9, 0, GMT_ZONE_ID))
        whenever(timeService.getRealTimeFromDate(eq(notificationTime))).thenReturn(notificationTime)

        val notificationDataMap = HashMap<String, Any>()

        val scheduledNotificationInfo = ScheduledNotificationInfo(categoryId, notificationDataMap, notificationTime)


        // perform operation
        val notificationService = NotificationServiceImpl(dependencyProvider)
        notificationService.scheduleNotification(scheduledNotificationInfo)

        // verify expectations
        verify<AlarmService>(alarmService).setAlarm(eq(alarmId), eq(notificationTime), eq(scheduledNotificationInfo))
    }

    @Test
    fun testThatScheduleNotificationWithRecurringScheduledNotificationCreatesAnAlarmWithCorrectParameters() {
        // create expectations
        val categoryId = "ImmediateNotification"
        val notificationId = "VASNotification"
        val alarmId = "com.teva.notifications.services.NotificationServiceImpl.Alarm." + notificationId

        val notificationTime = Instant.from(ZonedDateTime.of(2017, 1, 20, 11, 7, 9, 0, GMT_ZONE_ID))
        whenever(timeService.getRealTimeFromDate(eq(notificationTime))).thenReturn(notificationTime)

        val notificationDataMap = HashMap<String, Any>()
        notificationDataMap.put(NotificationDataKey.NOTIFICATION_ID, notificationId)
        notificationDataMap.put(NotificationDataKey.FIRE_DATE_APPLICATION_TIME, notificationTime)

        val recurringScheduledNotificationInfo = RecurringScheduledNotificationInfo(categoryId, notificationDataMap, notificationTime, RepeatType.ONCE_PER_DAY)


        // perform operation
        val notificationService = NotificationServiceImpl(dependencyProvider)
        notificationService.scheduleNotification(recurringScheduledNotificationInfo)

        // verify expectations
        verify(alarmService).setAlarm(eq(alarmId), eq(notificationTime), eq(recurringScheduledNotificationInfo))
    }

    @Test
    fun testThatCancelScheduledNotificationCancelsTheCorrectAlarm() {
        // create expectations
        val notificationId = "VASNotification"
        val alarmId = "com.teva.notifications.services.NotificationServiceImpl.Alarm." + notificationId

        // perform operation
        val notificationService = NotificationServiceImpl(dependencyProvider!!)
        notificationService.cancelScheduledNotification(notificationId)

        // verify expectations
        verify(alarmService).cancelAlarm(eq(alarmId))
    }

    @Test
    fun testThatAlarmCallbackSendsScheduledNotificationToNotificationPresenter() {
        // create expectations
        val categoryId = "ImmediateNotification"
        val notificationId = "VASNotification"
        val alarmId = "com.teva.notifications.services.NotificationServiceImpl.Alarm." + notificationId

        val notificationTime = Instant.from(ZonedDateTime.of(2017, 1, 20, 11, 7, 9, 0, GMT_ZONE_ID))

        val notificationDataMap = HashMap<String, Any>()
        notificationDataMap.put(NotificationDataKey.NOTIFICATION_ID, notificationId)
        notificationDataMap.put(NotificationDataKey.FIRE_DATE_APPLICATION_TIME, notificationTime)

        val scheduledNotificationInfo = ScheduledNotificationInfo(categoryId, notificationDataMap, notificationTime)

        // perform operation
        val notificationService = NotificationServiceImpl(dependencyProvider!!)
        notificationService.onAlarm(alarmId, scheduledNotificationInfo)

        // verify expectations
        verify(notificationPresenter).displayNotification(eq(scheduledNotificationInfo))
    }

    @Test
    fun testThatAlarmCallbackReschedulesDailyRecurringNotificationAndSendsNotificationToNotificationPresenter() {
        // create expectations
        val categoryId = "ImmediateNotification"
        val notificationId = "VASNotification"
        val alarmId = "com.teva.notifications.services.NotificationServiceImpl.Alarm." + notificationId

        val notificationTime = Instant.from(ZonedDateTime.of(2017, 1, 20, 11, 7, 9, 0, GMT_ZONE_ID))
        val nextNotificationTime = Instant.from(ZonedDateTime.of(2017, 1, 21, 11, 7, 9, 0, GMT_ZONE_ID))
        whenever(timeService.getRealTimeFromDate(eq(nextNotificationTime))).thenReturn(nextNotificationTime)
        whenever(timeService.now()).thenReturn(notificationTime)

        val notificationDataMap = HashMap<String, Any>()
        notificationDataMap.put(NotificationDataKey.NOTIFICATION_ID, notificationId)
        notificationDataMap.put(NotificationDataKey.FIRE_DATE_APPLICATION_TIME, notificationTime)

        val recurringScheduledNotificationInfo = RecurringScheduledNotificationInfo(categoryId, notificationDataMap, notificationTime, RepeatType.ONCE_PER_DAY)

        // perform operation
        val notificationService = NotificationServiceImpl(dependencyProvider!!)
        notificationService.onAlarm(alarmId, recurringScheduledNotificationInfo)

        // verify expectations
        verify<NotificationPresenter>(notificationPresenter).displayNotification(eq(recurringScheduledNotificationInfo))
        verify<AlarmService>(alarmService).setAlarm(eq(alarmId), eq(nextNotificationTime), eq(recurringScheduledNotificationInfo))
    }

    @Test
    fun testThatAlarmCallbackReschedulesMonthlyRecurringNotificationAndSendsNotificationToNotificationPresenter() {
        // create expectations
        val categoryId = "ImmediateNotification"
        val notificationId = "VASNotification"
        val alarmId = "com.teva.notifications.services.NotificationServiceImpl.Alarm." + notificationId

        val notificationTime = Instant.from(ZonedDateTime.of(2016, 1, 31, 11, 7, 9, 0, GMT_ZONE_ID))
        val nextNotificationTime = Instant.from(ZonedDateTime.of(2016, 2, 29, 11, 7, 9, 0, GMT_ZONE_ID))
        whenever(timeService.getRealTimeFromDate(eq(nextNotificationTime))).thenReturn(nextNotificationTime)
        whenever(timeService.now()).thenReturn(notificationTime)

        val notificationDataMap = HashMap<String, Any>()
        notificationDataMap.put(NotificationDataKey.NOTIFICATION_ID, notificationId)
        notificationDataMap.put(NotificationDataKey.FIRE_DATE_APPLICATION_TIME, notificationTime)

        val recurringScheduledNotificationInfo = RecurringScheduledNotificationInfo(categoryId, notificationDataMap, notificationTime, RepeatType.MONTHLY)

        // perform operation
        val notificationService = NotificationServiceImpl(dependencyProvider)
        notificationService.onAlarm(alarmId, recurringScheduledNotificationInfo)

        // verify expectations
        verify(notificationPresenter).displayNotification(eq(recurringScheduledNotificationInfo))
        verify(alarmService).setAlarm(eq(alarmId), eq(nextNotificationTime), eq(recurringScheduledNotificationInfo))
    }

    companion object {
        private val GMT_ZONE_ID = ZoneId.ofOffset("GMT", ZoneOffset.UTC)
    }
}
