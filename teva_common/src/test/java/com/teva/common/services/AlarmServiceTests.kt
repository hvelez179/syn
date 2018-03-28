///
// AlarmServiceTests.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.common.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.nhaarman.mockito_kotlin.*
import com.teva.utilities.services.DependencyProvider
import junit.framework.TestCase.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

/**
 * This class defines unit tests for the AlarmService class.
 */
class AlarmServiceTests {

    private lateinit var context: Context
    private lateinit var alarmManager: AlarmManager
    private lateinit var dependencyProvider: DependencyProvider
    private var parcelable: Parcelable? = null

    @Before
    fun setup() {
        dependencyProvider = DependencyProvider.default
        dependencyProvider.unregisterAll()

        context = mock()
        dependencyProvider.register(Context::class, context)

        alarmManager = mock()
        dependencyProvider.register(AlarmManager::class, alarmManager)

        //parcelable = mock()
    }

    @Test
    @Throws(Exception::class)
    fun testSetAlarmSetsAlarmOnAndroidAlarmManager() {
        // create expectations
        val alarmId = "com.teva.notifications.NotificationServiceImpl.Alarm.VASNotification"
        val alarmTime = Instant.from(ZonedDateTime.of(2017, 1, 20, 11, 7, 9, 0, GMT_ZONE_ID))

        // perform operation
        val alarmService = AlarmServiceImpl(dependencyProvider)
        alarmService.setAlarm(alarmId, alarmTime, parcelable)
        alarmService.setAlarm(alarmId, alarmTime.toEpochMilli(), parcelable)

        // test expectations
        verify(alarmManager, times(1)).setExact(eq(AlarmManager.RTC_WAKEUP), eq(alarmTime.toEpochMilli()), any())
        verify(alarmManager, times(1)).setExact(eq(AlarmManager.ELAPSED_REALTIME_WAKEUP), eq(alarmTime.toEpochMilli()), any())
    }

    @Test
    @Throws(Exception::class)
    fun testSetRepeatingAlarmSetsRepeatingAlarmOnAndroidAlarmManager() {
        // create expectations
        val alarmId = "com.teva.notifications.NotificationServiceImpl.Alarm.VASNotification"
        val startMilliseconds: Long = 1000
        val repeatMilliseconds: Long = 2000

        // perform operation
        val alarmService = AlarmServiceImpl(dependencyProvider)
        alarmService.setRepeatingAlarm(alarmId, startMilliseconds, repeatMilliseconds, parcelable)

        // test expectations
        verify(alarmManager).setRepeating(eq(AlarmManager.ELAPSED_REALTIME_WAKEUP), eq(startMilliseconds), eq(repeatMilliseconds), any())
    }

    @Test
    @Throws(Exception::class)
    fun testCancelAlarmCancelsAlarmOnAndroidAlarmManager() {
        // create expectations
        val alarmId = "com.teva.notifications.NotificationServiceImpl.Alarm.VASNotification"
        val alarmTime = Instant.from(ZonedDateTime.of(2017, 1, 20, 11, 7, 9, 0, GMT_ZONE_ID))

        // perform operation
        val alarmService = AlarmServiceImpl(dependencyProvider)
        alarmService.setAlarm(alarmId, alarmTime, parcelable)
        alarmService.cancelAlarm(alarmId)

        // test expectations
        verify(alarmManager).cancel(any<PendingIntent>())
    }

    @Test
    @Throws(Exception::class)
    fun testAlarmNotificationTriggersACallback() {
        // create expectations
        val alarmServiceCallback: AlarmServiceCallback = mock()
        val clientId = "com.teva.notifications.NotificationServiceImpl.Alarm"
        val alarmId = clientId + ".VASNotification"
        val alarmTime = Instant.from(ZonedDateTime.of(2017, 1, 20, 11, 7, 9, 0, GMT_ZONE_ID))

        // create a "mock" intent
        val intent = Intent()
        intent.setAction(alarmId)
        intent.putExtra("repeatingAlarm", false)

        // perform operation
        val alarmService = AlarmServiceImpl(dependencyProvider)
        alarmService.register(clientId, alarmServiceCallback)
        alarmService.setAlarm(alarmId, alarmTime, parcelable)

        val onReceiveMethod = alarmService.javaClass.getDeclaredMethod("onReceive", Intent::class.java)
        onReceiveMethod.isAccessible = true
        onReceiveMethod.invoke(alarmService, intent)

        // test expectations
        verify(alarmServiceCallback).onAlarm(alarmId, parcelable)
    }

    @Test
    @Throws(Exception::class)
    fun testOnAlarmReceiveTriggersMostRecentCallback() {
        // create expectations
        val alarmServiceCallback: AlarmServiceCallback = mock()
        val alarmServiceCallback2: AlarmServiceCallback = mock()
        val clientId = "com.teva.notifications.NotificationServiceImpl.Alarm"
        val alarmId = clientId + ".VASNotification"
        val alarmTime = Instant.from(ZonedDateTime.of(2017, 1, 20, 11, 7, 9, 0, GMT_ZONE_ID))

        // create a "mock" intent
        val intent = Intent()
        intent.setAction(alarmId)
        intent.putExtra("repeatingAlarm", false)

        // perform operation
        val alarmService = AlarmServiceImpl(dependencyProvider)
        alarmService.register(clientId, alarmServiceCallback)
        alarmService.setAlarm(alarmId, alarmTime, parcelable)
        alarmService.register(clientId, alarmServiceCallback2)

        val onReceiveMethod = alarmService.javaClass.getDeclaredMethod("onReceive", Intent::class.java)
        onReceiveMethod.isAccessible = true
        onReceiveMethod.invoke(alarmService, intent)

        // test expectations
        verify(alarmServiceCallback2).onAlarm(alarmId, parcelable)
        verify(alarmServiceCallback, never()).onAlarm(any(), any())
    }

    @Test
    @Throws(Exception::class)
    fun testOnAlarmReceiveTriggersNoCallbackIfClientUnregistered() {
        // create expectations
        val alarmServiceCallback: AlarmServiceCallback = mock()
        val clientId = "com.teva.notifications.NotificationServiceImpl.Alarm"
        val alarmId = clientId + ".VASNotification"
        val alarmTime = Instant.from(ZonedDateTime.of(2017, 1, 20, 11, 7, 9, 0, GMT_ZONE_ID))

        // create a "mock" intent
        val intent = Intent()
        intent.setAction(alarmId)
        intent.putExtra("repeatingAlarm", false)

        // perform operation
        val alarmService = AlarmServiceImpl(dependencyProvider)
        alarmService.register(clientId, alarmServiceCallback)
        alarmService.setAlarm(alarmId, alarmTime, parcelable)
        alarmService.unregister(clientId)

        val onReceiveMethod = alarmService.javaClass.getDeclaredMethod("onReceive", Intent::class.java)
        onReceiveMethod.isAccessible = true
        onReceiveMethod.invoke(alarmService, intent)

        // test expectations
        verify(alarmServiceCallback, never()).onAlarm(alarmId, parcelable)
    }

    @Test
    @Throws(Exception::class)
    fun testOnAlarmReceiveTriggersNoCallbackToOtherRegisteredClients() {
        // create expectations
        val alarmServiceCallback: AlarmServiceCallback = mock()
        val clientId = "com.teva.notifications.NotificationServiceImpl.Alarm"
        val alarmId = clientId + ".VASNotification"
        val alarmId2 = "com.teva.notifications.MockNotificationServiceImpl.Alarm.VASNotification"

        val alarmTime = Instant.from(ZonedDateTime.of(2017, 1, 20, 11, 7, 9, 0, GMT_ZONE_ID))

        // create a "mock" intent
        val intent = Intent()
        intent.setAction(alarmId)
        intent.putExtra("repeatingAlarm", false)

        val intent2 = Intent()
        intent2.setAction(alarmId2)

        // perform operation
        val alarmService = AlarmServiceImpl(dependencyProvider)
        alarmService.register(clientId, alarmServiceCallback)
        alarmService.setAlarm(alarmId, alarmTime, parcelable)
        alarmService.setAlarm(alarmId2, alarmTime, parcelable)

        val onReceiveMethod = alarmService.javaClass.getDeclaredMethod("onReceive", Intent::class.java)
        onReceiveMethod.isAccessible = true
        onReceiveMethod.invoke(alarmService, intent2)

        // test expectations
        verify(alarmServiceCallback, never()).onAlarm(any(), any())
    }

    @Test
    @Throws(Exception::class)
    fun testCheckingIfAlarmIsScheduledReturnsCorrectStatus() {
        // create expectations
        val clientId = "com.teva.notifications.NotificationServiceImpl.Alarm"
        val alarmId = clientId + ".VASNotification"
        val alarmId2 = "AnInvalidAlarmID"
        val alarmTime = Instant.from(ZonedDateTime.of(2017, 1, 20, 11, 7, 9, 0, GMT_ZONE_ID))

        // perform operation
        val alarmService = AlarmServiceImpl(dependencyProvider)
        alarmService.setAlarm(alarmId, alarmTime, parcelable)
        val alarm1Scheduled = alarmService.isAlarmScheduled(alarmId)
        val alarm2Scheduled = alarmService.isAlarmScheduled(alarmId2)

        // test expectations
        assertTrue(alarm1Scheduled)
        assertFalse(alarm2Scheduled)
    }

    companion object {
        private val GMT_ZONE_ID = ZoneId.ofOffset("GMT", ZoneOffset.UTC)
    }
}
