//
// DSAManagerTests.kt
// teva_userfeedback
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.userfeedback.model

import com.nhaarman.mockito_kotlin.*
import com.teva.common.services.TimeService
import com.teva.common.services.UpdateTimeMessage
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.notifications.entities.ReminderSetting
import com.teva.notifications.models.NotificationManager
import com.teva.userfeedback.dataquery.DailyUserFeelingDataQuery
import com.teva.userfeedback.entities.DailyUserFeeling
import com.teva.userfeedback.enumerations.UserFeeling
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.*

/**
 * This class defines unit tests for the DSAManagerImpl class.
 */
class DSAManagerTests {

    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var notificationManager: NotificationManager
    private lateinit var messenger: Messenger
    private lateinit var dailyUserFeelingDataQuery: DailyUserFeelingDataQuery
    private lateinit var timeService: TimeService

    @Before
    fun setup() {
        DependencyProvider.default.unregisterAll()

        dependencyProvider = DependencyProvider.default
        messenger = mock()
        dependencyProvider.register(Messenger::class, messenger)
        dailyUserFeelingDataQuery = mock()
        dependencyProvider.register(DailyUserFeelingDataQuery::class, dailyUserFeelingDataQuery)
        timeService = mock()
        dependencyProvider.register(TimeService::class, timeService)
        notificationManager = mock()
        dependencyProvider.register(NotificationManager::class, notificationManager)
    }

    @Test
    fun testDSAManagerOnInstantiationConfiguresReminderIfItDoesNotExist() {
        // simulate no existing reminder.
        whenever(notificationManager.hasReminderSetting(eq(DSANotificationId.DSA_REMINDER))).thenReturn(false)

        // create DSA manager.
        DSAManagerImpl(dependencyProvider)

        // verify that reminder is set.
        verify(notificationManager).setNotification(eq(DSANotificationId.DSA_REMINDER), any(), any(), any())
    }

    @Test
    fun testDSAManagerOnInstantiationDoesNotConfigureReminderIfItAlreadyExists() {
        // simulate an existing reminder.
        whenever(notificationManager.hasReminderSetting(eq(DSANotificationId.DSA_REMINDER))).thenReturn(true)

        // create DSA manager.
        DSAManagerImpl(dependencyProvider)

        // verify that reminder is not set.
        verify(notificationManager, never()).setNotification(eq(DSANotificationId.DSA_REMINDER), any(), any(), any())
    }

    @Test
    fun testDSAManagerRetrievesReminderSettingFromNotificationManager() {
        val expectedReminderSetting = ReminderSetting()
        whenever(notificationManager.getReminderSettingByName(eq(DSANotificationId.DSA_REMINDER))).thenReturn(expectedReminderSetting)

        // query the dsa manager for reminder.
        val dsaManager = DSAManagerImpl(dependencyProvider)
        val reminderSetting = dsaManager.reminderSetting

        // verify that the reminder is retrieved from the notification manager.
        verify(notificationManager).getReminderSettingByName(eq(DSANotificationId.DSA_REMINDER))
        assertEquals(reminderSetting, expectedReminderSetting)
    }

    @Test
    fun testSaveUserFeelingWritesUserFeelingDataToDatabaseAndSchedulesNotificationForNextDay() {
        val userFeelingTime = Instant.ofEpochMilli(1493657181421L)
        val savedUserFeeling = UserFeeling.GOOD

        // save user feeling.
        val dsaManager = DSAManagerImpl(dependencyProvider)
        dsaManager.saveUserFeeling(savedUserFeeling, userFeelingTime)

        // verify that user feeling information is stored in the database.
        val dailyUserFeelingArgumentCaptor = argumentCaptor<DailyUserFeeling>()
        verify(dailyUserFeelingDataQuery).insertOrUpdate(dailyUserFeelingArgumentCaptor.capture(), eq(true))
        assertEquals(dailyUserFeelingArgumentCaptor.lastValue.userFeeling, savedUserFeeling)
        assertEquals(dailyUserFeelingArgumentCaptor.lastValue.time, userFeelingTime)

        // verify that new reminder is scheduled for the next day.
        verify(notificationManager).setNotification(eq(DSANotificationId.DSA_REMINDER), any(), any(), eq(true))
    }

    @Test
    fun testDSAManagerRetrievesDSAForGivenDateUsingQueryAndReturnsIt() {
        val userFeelingQueryDate = LocalDate.of(2017, 5, 1)
        // mock user feeling query to return a valid user feeling for required date.
        val expectedDailyUserFeeling = DailyUserFeeling()

        whenever(dailyUserFeelingDataQuery.get(eq(userFeelingQueryDate))).thenReturn(expectedDailyUserFeeling)

        // request user feeling fora date.
        val dsaManager = DSAManagerImpl(dependencyProvider)
        val dailyUserFeeling = dsaManager.getUserFeelingAtDate(userFeelingQueryDate)

        //verify that the value is retrieved using the query and returned.
        verify(dailyUserFeelingDataQuery).get(eq(userFeelingQueryDate))
        assertEquals(dailyUserFeeling, expectedDailyUserFeeling)
    }

    @Test
    fun testDSAManagerReturnsUnknownUserFeelingIfNoDSAExistsForGivenDate() {
        val now = Instant.ofEpochMilli(1493657181421L)
        val userFeelingQueryDate = LocalDate.of(2017, 5, 1)
        whenever(timeService.now()).thenReturn(now)
        dependencyProvider.register(TimeService::class, timeService)
        // mock user feeling query to return null for required date.
        whenever(dailyUserFeelingDataQuery.get(eq(userFeelingQueryDate))).thenReturn(null)
        dependencyProvider.register(DailyUserFeelingDataQuery::class, dailyUserFeelingDataQuery)

        // request user feeling for the date.
        val dsaManager = DSAManagerImpl(dependencyProvider)
        val dailyUserFeeling = dsaManager.getUserFeelingAtDate(userFeelingQueryDate)
        // verify that UNKNOWN user feeling is returned.
        assertEquals(dailyUserFeeling.userFeeling, UserFeeling.UNKNOWN)
        assertNull(dailyUserFeeling.time)
    }

    @Test
    fun testDSAManagerRetrievesDSAForGivenDateRangeUsingQueryAndReturnsIt() {
        val userFeelingQueryStartDate = LocalDate.of(2017, 4, 1)
        val userFeelingQueryEndDate = LocalDate.of(2017, 5, 1)

        // mock user feeling query to return valid data for required date range.
        val expectedDailyUserFeelingValues = HashMap<LocalDate, DailyUserFeeling>()
        whenever(dailyUserFeelingDataQuery.get(eq(userFeelingQueryStartDate), eq(userFeelingQueryEndDate))).thenReturn(expectedDailyUserFeelingValues)
        dependencyProvider.register(DailyUserFeelingDataQuery::class, dailyUserFeelingDataQuery)

        // request user feeling for the date range.
        val dsaManager = DSAManagerImpl(dependencyProvider)
        val dailyUserFeelingValues = dsaManager.getUserFeelingHistoryFromDate(userFeelingQueryStartDate, userFeelingQueryEndDate)
        // verify that the user feeling data is returned.
        verify(dailyUserFeelingDataQuery).get(eq(userFeelingQueryStartDate), eq(userFeelingQueryEndDate))
        assertEquals(dailyUserFeelingValues, expectedDailyUserFeelingValues)
    }

    @Test
    fun testDSAManageConfiguresReminderWhenUpdateTimeMessageIsReceived() {
        whenever(notificationManager.hasReminderSetting(eq(DSANotificationId.DSA_REMINDER))).thenReturn(true)

        // send an UpdateTimeMessage to the DSA manager.
        val dsaManager = DSAManagerImpl(dependencyProvider)
        dsaManager.onUpdateTimeMessage(UpdateTimeMessage())

        // verify that a reminder is set.
        verify(notificationManager).setNotification(eq(DSANotificationId.DSA_REMINDER), any(), any(), any())
    }
}
