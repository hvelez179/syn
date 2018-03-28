///
// EnvironmentMonitorTests.kt
// teva_environment
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.environment.models

import android.content.Context
import android.location.Location
import com.nhaarman.mockito_kotlin.*
import com.teva.common.services.AlarmService
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.environment.entities.EnvironmentInfo
import com.teva.environment.entities.WeatherInfo
import com.teva.environment.messages.UpdateEnvironmentMessage
import com.teva.environment.services.EnvironmentService
import com.teva.location.models.LocationManager
import com.teva.location.services.LocationCallback
import com.teva.location.services.LocationInfo
import com.teva.notifications.models.NotificationManager
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant

/**
 * This class defines unit tests for the EnvironmentMonitorImpl class.
 */
class EnvironmentMonitorTests {

    private val latitude = 40.069308
    private val longitude = -75.556552

    private val dependencyProvider: DependencyProvider = DependencyProvider.default
    private val timeService: TimeService = mock()
    private val alarmService: AlarmService = mock()
    private val messenger: Messenger = mock()
    private val environmentService: EnvironmentService = mock()
    private val notificationManager: NotificationManager = mock()
    private val locationManager: LocationManager = mock()
    private val locationInfo: LocationInfo = LocationInfo(latitude, longitude, "", "", "", "")
    private val location: Location = mock()
    private val context: Context = mock()

    private val currentTime = Instant.ofEpochMilli(1491498741473L)

    private var alarmId: String? = null
    private var defaultAlarmInterval: Int = 0

    @Before
    fun setup() {
        dependencyProvider.unregisterAll()

        whenever(timeService.now()).thenReturn(currentTime)

        dependencyProvider.register(Context::class, context)
        dependencyProvider.register(TimeService::class, timeService)
        dependencyProvider.register(AlarmService::class, alarmService)
        dependencyProvider.register(Messenger::class, messenger)
        dependencyProvider.register(NotificationManager::class, notificationManager)
        dependencyProvider.register(LocationManager::class, locationManager)
        dependencyProvider.register(Context::class, context)
        whenever(location.latitude).thenReturn(latitude)
        whenever(location.longitude).thenReturn(longitude)
    }

    @Test
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun testGetCurrentEnvironmentInfoReturnsCachedEnvironmentInfo() {
        // create the environment monitor
        val environmentMonitor = EnvironmentMonitorImpl(dependencyProvider, environmentService)
        val mockEnvironmentInfo = EnvironmentInfo()
        mockEnvironmentInfo.expirationDate = Instant.ofEpochMilli(1491499741473L)

        // Update the cached environment info
        environmentMonitor.getEnvironmentComplete(mockEnvironmentInfo)

        // retrieve the environment information from the environment monitor
        val environmentInfo = environmentMonitor.currentEnvironmentInfo

        // verify that the cached environment information is returned
        assertEquals(environmentInfo, mockEnvironmentInfo)
    }

    @Test
    fun testUpdateEnvironmentMessageSendsARequestToEnvironmentService() {
        // simulate the location manager to return a valid location
        whenever(locationManager.currentLocation).thenReturn(location)
        doAnswer { invocation ->
            (invocation.arguments[2] as LocationCallback).locationLookupCompleted(locationInfo)
            null
        }.whenever(locationManager)!!.reverseLocationLookup(any(), any(), any())
        dependencyProvider.register(LocationManager::class, locationManager)

        // create the environment monitor
        val environmentMonitor = EnvironmentMonitorImpl(dependencyProvider, environmentService)

        // simulate an UpdateEnvironment message
        environmentMonitor.onUpdateEnvironmentMessage(UpdateEnvironmentMessage())

        // verify that a request is sent to the environment service to retrieve the environment data
        verify(environmentService).getEnvironmentAsync(eq(locationInfo))
    }

    @Test
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun testUpdateEnvironmentMessageDoesNotSendARequestWhenLocationInformationCannotBeRetrieved() {
        // simulate the location manager to not return a valid location
        whenever(locationManager.currentLocation).thenReturn(location)
        doAnswer { invocation ->
            (invocation.arguments[2] as LocationCallback).locationLookupCompleted(null)
            null
        }.whenever(locationManager)!!.reverseLocationLookup(any(), any(), any())
        dependencyProvider.register(LocationManager::class, locationManager)

        // create the environment monitor
        val environmentMonitor = EnvironmentMonitorImpl(dependencyProvider, environmentService)

        retrieveAlarmIdAndInterval()
        val expectedAlarmTime = currentTime.plusSeconds(defaultAlarmInterval.toLong())

        // simulate an UpdateEnvironment message
        environmentMonitor.onUpdateEnvironmentMessage(UpdateEnvironmentMessage())

        // verify that a request is not sent to the environment service to retrieve the environment data
        verify(environmentService, never()).getEnvironmentAsync(eq(locationInfo))

        // verify that an alarm is scheduled to retry retrieving environment data
        verify(alarmService).setAlarm(eq<String>(alarmId!!), eq(expectedAlarmTime), isNull())
    }

    @Test
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun testUpdateEnvironmentAlarmSendsARequestToEnvironmentService() {
        // simulate the location manager to return a valid location
        whenever(locationManager.currentLocation).thenReturn(location)
        doAnswer { invocation ->
            (invocation.arguments[2] as LocationCallback).locationLookupCompleted(locationInfo)
            null
        }.whenever(locationManager).reverseLocationLookup(any(), any(), any())
        dependencyProvider.register(LocationManager::class, locationManager)

        // create the environment monitor
        val environmentMonitor = EnvironmentMonitorImpl(dependencyProvider, environmentService)
        retrieveAlarmIdAndInterval()

        // simulate an UpdateEnvironment alarm
        environmentMonitor.onAlarm(alarmId!!, null)

        // verify that a request is sent to the environment service to retrieve the environment data
        verify(environmentService).getEnvironmentAsync(eq(locationInfo))
    }

    @Test
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun testUpdateEnvironmentAlarmDoesNotSendARequestToEnvironmentServiceIfLocationInformationIsNotAvailableAndSetsAnAlarmToRetry() {
        // simulate the location manager to not return a valid location
        whenever(locationManager.currentLocation).thenReturn(null)
        dependencyProvider.register(LocationManager::class, locationManager)

        // create the environment monitor
        val environmentMonitor = EnvironmentMonitorImpl(dependencyProvider, environmentService)

        retrieveAlarmIdAndInterval()
        val expectedAlarmTime = currentTime.plusSeconds(defaultAlarmInterval.toLong())

        // simulate an UpdateEnvironment alarm
        environmentMonitor.onAlarm(alarmId!!, null)

        // verify that a request is not sent to the environment service to retrieve the environment data
        verify(environmentService, never()).getEnvironmentAsync(eq(locationInfo))

        // verify that an alarm is scheduled to retry retrieving environment data
        verify(alarmService).setAlarm(eq(alarmId!!), eq(expectedAlarmTime), isNull())
    }

    @Test
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun testGetEnvironmentCompleteWithValidEnvironmentInfoTriggersEnvironmentUpdatedMessageAndSetsAlarmAtExpirationTime() {
        // create a mock EnvironmentInfo with valid expiration date
        val expirationDate = Instant.ofEpochMilli(1491499441473L)
        val environmentInfo = EnvironmentInfo()
        val weatherInfo = WeatherInfo("providerName")
        environmentInfo.weatherInfo = weatherInfo
        environmentInfo.expirationDate = expirationDate

        // create the environment monitor
        val environmentMonitor = EnvironmentMonitorImpl(dependencyProvider, environmentService)

        retrieveAlarmIdAndInterval()

        // simulate an UpdateEnvironment alarm
        environmentMonitor.getEnvironmentComplete(environmentInfo)

        // verify that a EnvironmentUpdated message is triggered
        verify(messenger, times(1)).publish(any())

        // verify that an alarm is scheduled at the expiration date
        verify(alarmService).setAlarm(eq(alarmId!!), eq(expirationDate), isNull())
    }

    @Test
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun testGetEnvironmentCompleteWithExpiredEnvironmentInfoTriggersEnvironmentUpdatedMessageAndSetsAlarmForRetry() {
        // create a mock EnvironmentInfo with expired EnvironmentInfo
        val expirationDate = Instant.ofEpochMilli(1491498541473L)
        val environmentInfo = EnvironmentInfo()
        val weatherInfo = WeatherInfo("providerName")
        environmentInfo.weatherInfo = weatherInfo
        environmentInfo.expirationDate = expirationDate

        // create the environment monitor
        val environmentMonitor = EnvironmentMonitorImpl(dependencyProvider, environmentService)

        retrieveAlarmIdAndInterval()
        val expectedAlarmTime = currentTime.plusSeconds(defaultAlarmInterval.toLong())

        // simulate an UpdateEnvironment alarm
        environmentMonitor.getEnvironmentComplete(environmentInfo)

        // verify that a EnvironmentUpdated message is triggered
        verify(messenger, times(1)).publish(any())

        // verify that an alarm is scheduled for a retry
        verify(alarmService).setAlarm(eq(alarmId!!), eq(expectedAlarmTime), isNull())
    }

    @Test
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun testGetEnvironmentCompleteWithValidEnvironmentInfoStartsProcessingPendingRequests() {
        // simulate the location manager to return a valid location
        whenever(locationManager.currentLocation).thenReturn(location)
        doAnswer { invocation ->
            (invocation.arguments[2] as LocationCallback).locationLookupCompleted(locationInfo)
            null
        }.whenever(locationManager).reverseLocationLookup(any(), any(), any())
        dependencyProvider.register(LocationManager::class, locationManager)

        // create a mock EnvironmentInfo with valid expiration date
        val expirationDate = Instant.ofEpochMilli(1491499441473L)
        val environmentInfo = EnvironmentInfo()
        val weatherInfo = WeatherInfo("providerName")
        environmentInfo.weatherInfo = weatherInfo
        environmentInfo.expirationDate = expirationDate

        // create the environment monitor
        val environmentMonitor = EnvironmentMonitorImpl(dependencyProvider, environmentService)

        retrieveAlarmIdAndInterval()

        // simulate two UpdateEnvironment messages
        environmentMonitor.onUpdateEnvironmentMessage(UpdateEnvironmentMessage())
        environmentMonitor.onUpdateEnvironmentMessage(UpdateEnvironmentMessage())

        // verify that only one request is submitted to the environment service
        verify(environmentService, times(1)).getEnvironmentAsync(any())

        // trigger an environment complete message
        environmentMonitor.getEnvironmentComplete(environmentInfo)

        // verify that the second request is submitted to the environment service
        verify(environmentService, times(2)).getEnvironmentAsync(any())
    }

    @Test
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun testEnvironmentMonitorProcessesOnlyTheLatestRequestIfMultipleRequestsWereReceivedDuringTheProcessingOfOne() {
        // simulate the location manager to return a valid location
        whenever(locationManager.currentLocation).thenReturn(location)
        doAnswer { invocation ->
            (invocation.arguments[2] as LocationCallback).locationLookupCompleted(locationInfo)
            null
        }.whenever(locationManager).reverseLocationLookup(any(), any(), any())
        dependencyProvider.register(LocationManager::class, locationManager)

        // create a mock EnvironmentInfo with valid expiration date
        val expirationDate = Instant.ofEpochMilli(1491499441473L)
        val environmentInfo = EnvironmentInfo()
        val weatherInfo = WeatherInfo("providerName")
        environmentInfo.weatherInfo = weatherInfo
        environmentInfo.expirationDate = expirationDate

        // create the environment monitor
        val environmentMonitor = EnvironmentMonitorImpl(dependencyProvider, environmentService)

        retrieveAlarmIdAndInterval()

        // simulate three UpdateEnvironment messages
        environmentMonitor.onUpdateEnvironmentMessage(UpdateEnvironmentMessage())

        // simulate the location manager to return a valid location
        val latitude2 = 40.11
        val longitude2 = -70.11
        val location2: Location = mock()
        whenever(location2.latitude).thenReturn(latitude2)
        whenever(location2.longitude).thenReturn(longitude2)
        whenever(locationManager.currentLocation).thenReturn(location2)
        val locationInfo2 = LocationInfo(latitude2, longitude2, "", "", "", "")
        doAnswer { invocation ->
            (invocation.arguments[2] as LocationCallback).locationLookupCompleted(locationInfo2)
            null
        }.whenever(locationManager).reverseLocationLookup(any(), any(), any())
        dependencyProvider.register(LocationManager::class, locationManager)
        environmentMonitor.onUpdateEnvironmentMessage(UpdateEnvironmentMessage())

        // simulate the location manager to return a valid location
        val latitude3 = 40.33
        val longitude3 = -70.33
        val location3: Location = mock()
        whenever(location3.latitude).thenReturn(latitude3)
        whenever(location3.longitude).thenReturn(longitude3)
        whenever(locationManager.currentLocation).thenReturn(location3)
        val locationInfo3 = LocationInfo(latitude3, longitude3, "", "", "", "")
        doAnswer { invocation ->
            (invocation.arguments[2] as LocationCallback).locationLookupCompleted(locationInfo3)
            null
        }.whenever(locationManager).reverseLocationLookup(any(), any(), any())
        dependencyProvider.register(LocationManager::class, locationManager)
        environmentMonitor.onUpdateEnvironmentMessage(UpdateEnvironmentMessage())

        // verify that only one request is submitted to the environment service
        verify(environmentService, times(1)).getEnvironmentAsync(eq(locationInfo))

        // trigger an environment complete message
        environmentMonitor.getEnvironmentComplete(environmentInfo)

        // verify that the third request is submitted to the environment service
        verify(environmentService, times(1)).getEnvironmentAsync(eq(locationInfo3))
    }

    @Test
    fun testGetReminderSettingRetrievesTheDailyEnvironmenTalReminderSettingFromNotificationManager() {
        // create the environment monitor
        val environmentMonitor = EnvironmentMonitorImpl(dependencyProvider, environmentService)

        // simulate two UpdateEnvironment messages
        environmentMonitor.reminderSetting

        // verify that the reminder setting is retrieved from the notification manager.
        verify(notificationManager, times(2)).getReminderSettingByName(eq(EnvironmentNotificationId.DailyEnvironmentalReminder))
    }

    /**
     * This method retrieves the alarm ID for the update environment alarm and the
     * environment update retry interval from the EnvironmentMonitor.
     */
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    private fun retrieveAlarmIdAndInterval() {
        val alarmIdField = EnvironmentMonitorImpl::class.java.getDeclaredField("ENVIRONMENT_UPDATE_ALARM_ID")
        alarmIdField.isAccessible = true
        alarmId = alarmIdField.get(null) as String

        val alarmIntervalField = EnvironmentMonitorImpl::class.java.getDeclaredField("ENVIRONMENT_RETRY_INTERVAL_IN_SECONDS")
        alarmIntervalField.isAccessible = true
        defaultAlarmInterval = alarmIntervalField.getInt(null)
    }
}
