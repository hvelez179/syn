//
// CloudManagerImplTests.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.models

import android.content.SharedPreferences
import com.teva.cloud.services.CloudService
import com.teva.utilities.services.DependencyProvider
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.nhaarman.mockito_kotlin.*
import com.teva.cloud.dataquery.UserAccountQuery
import com.teva.cloud.enumerations.CloudManagerState
import com.teva.cloud.messages.CloudLoginStateChangedMessage
import com.teva.cloud.messages.SyncCloudMessage
import com.teva.cloud.models.programmanagement.CareProgramManager
import com.teva.cloud.models.sync.SyncManager
import com.teva.cloud.models.userprofile.UserProfileManager
import com.teva.common.services.TimeService
import com.teva.common.utilities.Messenger
import com.teva.dhp.models.DHPManager
import com.teva.notifications.models.NotificationManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.threeten.bp.Duration
import org.threeten.bp.Instant

/**
 * This class contains unit tests for the CloudManagerImpl class.
 */
class CloudManagerImplTests {
    private var dependencyProvider: DependencyProvider = DependencyProvider.default
    private val timeService: TimeService = mock()
    private val messenger: Messenger = mock()
    private val notificationManager: NotificationManager = mock()
    private val userProfileManager: UserProfileManager = mock()
    private val sharedPreferences: SharedPreferences = mock()
    private val sharedPreferencesEditor: SharedPreferences.Editor = mock()
    private val dhpManager: DHPManager = mock()
    private val userAccountQuery: UserAccountQuery = mock()
    private val syncManager: SyncManager = mock()
    private val cloudService: CloudService = mock()
    private val careProgramManager: CareProgramManager = mock()


    @Before
    @Throws(Exception::class)
    fun setup() {
        dependencyProvider.register(DHPManager::class, dhpManager)
        dependencyProvider.register(UserAccountQuery::class, userAccountQuery)
        dependencyProvider.register(TimeService::class, timeService)
        dependencyProvider.register(Messenger::class, messenger)
        dependencyProvider.register(NotificationManager::class, notificationManager)
        dependencyProvider.register(UserProfileManager::class, userProfileManager)
        whenever(sharedPreferences.getString(any(), any())).thenReturn("string")
        whenever(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor)
        dependencyProvider.register(SharedPreferences::class, sharedPreferences)
        dependencyProvider.register(CareProgramManager::class, careProgramManager)
        dependencyProvider.register(SyncManager::class, syncManager)
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        dependencyProvider.unregisterAll()
    }

    @Test
    fun testThatAMessageIsTriggeredWhenLoginStateChangesFromLoggedInToLoggedOut() {
        // arrange
        val cloudManager = CloudManagerImpl(cloudService)
        cloudManager.isLoggedIn = true
        val cloudLoginStateChangedMessageCaptor = argumentCaptor<CloudLoginStateChangedMessage>()

        // act
        cloudManager.isLoggedIn = false

        // assert
        verify(messenger).publish(cloudLoginStateChangedMessageCaptor.capture())
        assertEquals(CloudManagerState.NOT_LOGGED_IN, cloudLoginStateChangedMessageCaptor.firstValue.loginState)
    }

    @Test
    fun testThatCloudManagerStateIsSetToIdleIfIsLoggedInIsSetToTrue() {
        // arrange
        val cloudManager = CloudManagerImpl(cloudService)

        // act
        cloudManager.isLoggedIn = true

        // assert
        assertEquals(CloudManagerState.IDLE, cloudManager.cloudManagerState)
    }

    @Test
    fun testThatLogOutInvokesTheCorrespondingMethodOnCloudService() {
        // arrange
        val cloudManager = CloudManagerImpl(cloudService)

        // act
        cloudManager.logOut()

        // assert
        verify(cloudService).logOut()
    }

    @Test
    fun testThatGetServerTimeIfFailedDoesNotUpdateTheServerTime() {
        // arrange
        val now = Instant.parse("2017-11-14T10:15:30.00Z")
        val serverTime = Instant.parse("2017-11-14T10:15:32.00Z")
        whenever(timeService.now()).thenReturn(now)
        whenever(timeService.getApplicationTime(eq(serverTime))).thenReturn(serverTime)
        val cloudManager = CloudManagerImpl(cloudService)
        CloudSessionState.shared.serverTime = null
        CloudSessionState.shared.serverTimeOffset = null

        // act
        cloudManager.getServerTimeCompleted(false, serverTime)

        // assert
        assertNull(CloudSessionState.shared.serverTimeOffset)
        assertNull(CloudSessionState.shared.serverTime)
    }

    @Test
    fun testThatGetServerTimeIfSuccessfulCalculatesTheServerTimeOffsetCorrectly() {
        // arrange
        val now = Instant.parse("2017-11-14T10:15:30.00Z")
        val serverTime = Instant.parse("2017-11-14T10:15:32.00Z")
        whenever(timeService.now()).thenReturn(now)
        whenever(timeService.getApplicationTime(eq(serverTime))).thenReturn(serverTime)
        val cloudManager = CloudManagerImpl(cloudService)
        CloudSessionState.shared.serverTime = null
        CloudSessionState.shared.serverTimeOffset = null
        val calculatedTimeOffset = Duration.between(now, serverTime).seconds.toInt()

        // act
        cloudManager.getServerTimeCompleted(true, serverTime)

        // assert
        assertEquals(calculatedTimeOffset, CloudSessionState.shared.serverTimeOffset)
        assertEquals(serverTime, CloudSessionState.shared.serverTime)
    }

    @Test
    fun testThatClearServerTimeOffsetResetsServerTimeOffset() {
        // arrange
        val cloudManager = CloudManagerImpl(cloudService)
        CloudSessionState.shared.serverTimeOffset = 2

        // act
        cloudManager.clearServerTimeOffset()

        // assert
        assertNull(CloudSessionState.shared.serverTimeOffset)
    }

    @Test
    fun testSyncCloudMessageRetrievesServerTimeIfLoggedInAndServerTimeWasNotPreviouslyRetrieved() {
        // arrange
        val cloudManager = CloudManagerImpl(cloudService)
        cloudManager.isLoggedIn = true
        whenever(userProfileManager.getActive()).thenReturn(mock())

        // act
        cloudManager.onSyncCloudMessage(SyncCloudMessage())

        // assert
        verify(cloudService).getServerTimeAsync()
    }

    @Test
    fun testSyncCloudMessageStartsSyncingIfLoggedInAndServerTimeWasPreviouslyRetrieved() {
        // arrange
        val now = Instant.parse("2017-11-14T10:15:30.00Z")
        val serverTime = Instant.parse("2017-11-14T10:15:32.00Z")
        whenever(timeService.now()).thenReturn(now)
        whenever(timeService.getApplicationTime(eq(serverTime))).thenReturn(serverTime)
        whenever(userProfileManager.getActive()).thenReturn(mock())
        whenever(careProgramManager.hasManagedInitialUserProgramList).thenReturn(true)

        // act
        val cloudManager = CloudManagerImpl(cloudService)
        cloudManager.isLoggedIn = true
        cloudManager.getServerTimeCompleted(true, serverTime)
        cloudManager.onSyncCloudMessage(SyncCloudMessage())

        // assert
        verify(syncManager).sync()
    }
}