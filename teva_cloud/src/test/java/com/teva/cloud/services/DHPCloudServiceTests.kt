//
// DHPCloudServiceTests.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.services

import android.content.SharedPreferences
import com.nhaarman.mockito_kotlin.*
import com.teva.cloud.dataentities.UserAccount
import com.teva.cloud.dataquery.UserAccountQuery
import com.teva.cloud.models.CloudSessionState
import com.teva.cloud.models.userprofile.UserProfileManager
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.Endpoints.DHPGetServerTime
import com.teva.dhp.DataEntities.DHPDataTypes.GenericDHPRequest
import com.teva.dhp.DataEntities.DHPDataTypes.GenericDHPResponse
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPGetServerTimeResponseBody
import com.teva.dhp.models.DHPAPIs
import com.teva.dhp.models.DHPManager
import com.teva.dhp.models.DHPSession
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant

/**
 * This class defines unit tests for the DHPCloudService class.
 */
class DHPCloudServiceTests {
    private var dependencyProvider: DependencyProvider = DependencyProvider.default
    private val dhpManager: DHPManager = mock()
    private val userAccountQuery: UserAccountQuery = mock()
    private val timeService: TimeService = mock()
    private val userProfileManager: UserProfileManager = mock()
    private val sharedPreferences: SharedPreferences = mock()
    private val sharedPreferencesEditor: SharedPreferences.Editor = mock()
    private val messenger: Messenger = mock()

    @Before
    @Throws(Exception::class)
    fun setup() {
        dependencyProvider.register(DHPManager::class, dhpManager)
        dependencyProvider.register(UserAccountQuery::class, userAccountQuery)
        whenever(timeService.now()).thenReturn(Instant.ofEpochMilli(1516025834194))
        dependencyProvider.register(TimeService::class, timeService)
        dependencyProvider.register(UserProfileManager::class, userProfileManager)
        whenever(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor)
        dependencyProvider.register(SharedPreferences::class, sharedPreferences)
        dependencyProvider.register(Messenger::class, messenger)
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        DependencyProvider.default.unregisterAll()
    }

    @Test
    fun testLogOutInvokesMethodOnTheDHPManagerAndClearsTokensFromTheUserAccount() {
        // arrange
        val userAccount: UserAccount = mock()

        val cloudService = DHPCloudService(dhpManager)
        whenever(userAccountQuery.getUserAccount()).thenReturn(userAccount)

        // act
        cloudService.logOut()

        // assert
        verify(dhpManager).logOut()
        verify(userAccountQuery).update(eq(userAccount), eq(true))
        verify(userAccount).DHPAccessToken = ""
        verify(userAccount).DHPRefreshToken = ""
        verify(userAccount).identityHubProfileUrl = ""
        verify(userAccount).identityHubAccessToken = ""
        verify(userAccount).identityHubRefreshToken = ""
    }

    @Test
    fun testGetServerTimeSendsRequestToTheDHPManagerWithCorrectPayload() {
        // arrange
        val cloudService = DHPCloudService(dhpManager)
        DHPSession.shared.federationId = "FED1234"
        CloudSessionState.shared.activeInvokingRole = DHPCodes.Role.patient
        CloudSessionState.shared.activeProfileID = "P001"
        CloudSessionState.shared.appVersionNumber = "1.0"
        CloudSessionState.shared.appName = "eProAir"
        CloudSessionState.shared.mobileUUID = "123e4567-e89b-12d3-a456-426655440000"

        // act
        cloudService.getServerTimeAsync()
        val dhpRequestArgumentCaptor = argumentCaptor<GenericDHPRequest<*>>()

        // assert
        verify(dhpManager).executeAsync(dhpRequestArgumentCaptor.capture(), isNull())
        assertEquals(DHPAPIs.getServerTime, dhpRequestArgumentCaptor.firstValue.uri)
        assertEquals(DHPGetServerTime::class.java, dhpRequestArgumentCaptor.firstValue.payload.javaClass)
    }

    @Test
    fun testGetServerTimeCompletedWhenFailedInvokesMethodOnTheDelegateWithCorrectValues() {
        // arrange
        val cloudServiceDelegate: CloudServiceDelegate = mock()
        val cloudService = DHPCloudService(dhpManager)
        cloudService.delegate = cloudServiceDelegate
        val response: GenericDHPResponse<DHPGetServerTimeResponseBody> = mock()
        whenever(response.success).thenReturn(false)

        // act
        val getServerTimeCompletedMethod = cloudService.javaClass.getDeclaredMethod("getServerTimeCompleted", GenericDHPResponse::class.java)
        getServerTimeCompletedMethod.isAccessible = true
        getServerTimeCompletedMethod.invoke(cloudService, response)

        // assert
        verify(cloudServiceDelegate).getServerTimeCompleted(eq(false), isNull())
    }

    @Test
    fun testGetServerTimeCompletedWhenSucceededInvokesMethodOnTheDelegateWithCorrectValues() {
        // arrange
        val cloudServiceDelegate: CloudServiceDelegate = mock()
        val cloudService = DHPCloudService(dhpManager)
        cloudService.delegate = cloudServiceDelegate
        val response: GenericDHPResponse<DHPGetServerTimeResponseBody> = mock()
        val responseBody: DHPGetServerTimeResponseBody = mock()
        val serverTimeGMT = "2018-01-16T11:00:00Z"
        whenever(responseBody.serverTimeGMT).thenReturn(serverTimeGMT)
        whenever(response.success).thenReturn(true)
        whenever(response.body).thenReturn(responseBody)

        // act
        val getServerTimeCompletedMethod = cloudService.javaClass.getDeclaredMethod("getServerTimeCompleted", GenericDHPResponse::class.java)
        getServerTimeCompletedMethod.isAccessible = true
        getServerTimeCompletedMethod.invoke(cloudService, response)
        val instantCaptor = argumentCaptor<Instant>()

        // assert
        verify(cloudServiceDelegate).getServerTimeCompleted(eq(true), instantCaptor.capture())
        assertEquals(serverTimeGMT, instantCaptor.firstValue.toString())
    }

    @Test
    fun testThatLoginStatusIsPassedOntoTheDelegateWhenDHPLoginSucceeds() {
        // arrange
        val cloudServiceDelegate: CloudServiceDelegate = mock()
        val cloudService = DHPCloudService(dhpManager)
        cloudService.delegate = cloudServiceDelegate

        // act
        cloudService.didLoginToDHP()

        // assert
        verify(cloudServiceDelegate).isLoggedIn = true
    }

    @Test
    fun testThatLoginStatusIsPassedOntoTheDelegateWhenDHPLoginFails() {
        // arrange
        val userAccount: UserAccount = mock()
        whenever(userAccount.hasTokens()).thenReturn(true)
        whenever(userAccountQuery.getUserAccount()).thenReturn(userAccount)
        val cloudServiceDelegate: CloudServiceDelegate = mock()
        val cloudService = DHPCloudService(dhpManager)
        cloudService.delegate = cloudServiceDelegate

        // act
        cloudService.failLogin()

        // assert
        verify(cloudServiceDelegate).isLoggedIn = false

    }

    @Test
    fun testThatUserAccountAndCloudSessionStateAreUpdatedWhenIdentityHubProfileIsRetrieved() {
        // arrange
        val identityHubProfileJson: JSONObject = mock()
        whenever(identityHubProfileJson.getString("first_name")).thenReturn("user")
        whenever(identityHubProfileJson.getString("last_name")).thenReturn("user")
        val userAccount: UserAccount = mock()
        whenever(userAccountQuery.getUserAccount()).thenReturn(userAccount)
        val cloudService = DHPCloudService(dhpManager)

        // act
        cloudService.didGetIdentityHubProfile("user001@mail.com", "FED1234", identityHubProfileJson , "pseudo")

        // assert
        assertEquals("user",CloudSessionState.shared.idHubFirstName)
        assertEquals("user",CloudSessionState.shared.idHubLastName)
        verify(userAccount).username = "user001@mail.com"
        verify(userAccount).federationId = "FED1234"
        verify(userAccount).pseudoName = "pseudo"

        verify(userAccountQuery).update(eq(userAccount), eq(true))
    }

    @Test
    fun testThatUserAccountIsUpdatedWhenDHPTokensAreRetrieved() {
        // arrange
        val dhpAccessToken = "DHPAccessToken"
        val dhpRefreshToken = "DHPRefreshToken"
        val userAccount: UserAccount = mock()
        whenever(userAccountQuery.getUserAccount()).thenReturn(userAccount)
        val cloudService = DHPCloudService(dhpManager)

        // act
        cloudService.didGetDHPTokens(dhpAccessToken, dhpRefreshToken)

        // assert
        verify(userAccount).DHPAccessToken = dhpAccessToken
        verify(userAccount).DHPRefreshToken = dhpRefreshToken

        verify(userAccountQuery).update(eq(userAccount), eq(true))
    }

    @Test
    fun testThatUserAccountIsUpdatedWhenIdentityHubTokensAreRetrieved() {
        // arrange
        val accessToken = "AccessToken"
        val profileUrl = "http://dummyprofileurl"
        val idToken = "IDToken"
        val refreshToken = "RefreshToken"

        val userAccount: UserAccount = mock()
        whenever(userAccountQuery.getUserAccount()).thenReturn(userAccount)
        val cloudService = DHPCloudService(dhpManager)

        // act
        cloudService.didGetIdentityHubTokens(accessToken, profileUrl, idToken, refreshToken)

        // assert
        verify(userAccount).identityHubAccessToken = accessToken
        verify(userAccount).identityHubProfileUrl = profileUrl
        verify(userAccount).identityHubIdToken = idToken
        verify(userAccount).identityHubRefreshToken = refreshToken

        verify(userAccountQuery).update(eq(userAccount), eq(true))
    }
}