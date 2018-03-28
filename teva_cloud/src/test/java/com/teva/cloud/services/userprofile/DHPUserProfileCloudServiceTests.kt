/*
 *
 *  DHPUserProfileCloudServiceTests.kt
 *  teva_cloud
 *
 *  Copyright © 2018 Teva. All rights reserved.
 *
 */

package com.teva.cloud.services.userprofile

import android.content.SharedPreferences
import com.nhaarman.mockito_kotlin.*
import com.teva.cloud.dataentities.ConsentData
import com.teva.cloud.dataentities.UserProfile
import com.teva.cloud.dataquery.ConsentDataQuery
import com.teva.cloud.models.CloudSessionState
import com.teva.cloud.models.userprofile.UserProfileManager
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.Endpoints.*
import com.teva.dhp.DataEntities.DHPDataTypes.GenericDHPRequest
import com.teva.dhp.DataEntities.DHPDataTypes.GenericDHPResponse
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPAddDependentProfileResponseBody
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPGetRelatedProfilesResponseBody
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPResponseBody
import com.teva.dhp.models.DHPAPIs
import com.teva.dhp.models.DHPManager
import com.teva.dhp.models.DHPSession
import junit.framework.Assert.assertEquals
import org.json.JSONArray
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

/**
 * This class defines unit tests for the DHPUserProfileCloudService class.
 */
class DHPUserProfileCloudServiceTests {
    private var dependencyProvider: DependencyProvider = DependencyProvider.default
    private val dhpManager: DHPManager = mock()
    private val timeService: TimeService = mock()
    private val userProfileManager: UserProfileManager = mock()
    private val sharedPreferences: SharedPreferences = mock()
    private val sharedPreferencesEditor: SharedPreferences.Editor = mock()
    private val consentDataQuery: ConsentDataQuery = mock()

    @Before
    @Throws(Exception::class)
    fun setup() {
        dependencyProvider.register(DHPManager::class, dhpManager)
        whenever(timeService.now()).thenReturn(Instant.ofEpochMilli(1516821870215))
        whenever(timeService.today()).thenReturn(LocalDate.of(2018, 1, 24))
        dependencyProvider.register(TimeService::class, timeService)
        dependencyProvider.register(UserProfileManager::class, userProfileManager)
        whenever(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor)
        dependencyProvider.register(SharedPreferences::class, sharedPreferences)

        val consentData: ConsentData = mock()
        whenever(consentData.patientDOB).thenReturn(LocalDate.of(1970, 1, 1))
        whenever(consentDataQuery.getConsentData()).thenReturn(consentData)
        dependencyProvider.register(ConsentDataQuery::class, consentDataQuery)
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        DependencyProvider.default.unregisterAll()
    }

    @Test
    fun testThatGetAllProfilesAsyncSendsAGetRolesForProfileRequestToTheDHPManager() {
        // arrange
        val dhpUserProfileService = DHPUserProfileCloudService()

        // act
        dhpUserProfileService.getAllProfilesAsync()
        val dhpRequestArgumentCaptor = argumentCaptor<GenericDHPRequest<*>>()
        val callbackArgumentCaptor = argumentCaptor<(Boolean, String, String?)->Unit>()

        // assert
        verify(dhpManager).executeAsync(dhpRequestArgumentCaptor.capture(), callbackArgumentCaptor.capture())
        assertEquals(DHPAPIs.getRolesForProfile, dhpRequestArgumentCaptor.firstValue.uri)
        assertEquals(DHPGetRolesForProfile::class.java, dhpRequestArgumentCaptor.firstValue.payload.javaClass)
    }

    @Test
    fun testThatGetAllProfilesAsyncDoesNotSendRequestToTheDHPManagerAndReturnsEmptyProfileListIfAccountOwnerDOBIsUnavailable() {
        // arrange
        val dhpUserProfileService = DHPUserProfileCloudService()
        val callback: (Boolean, List<UserProfile>)->Unit = mock()
        dhpUserProfileService.didGetAllProfiles = callback
        val consentData: ConsentData = mock()
        whenever(consentData.patientDOB).thenReturn(null)
        whenever(consentDataQuery.getConsentData()).thenReturn(consentData)

        // act
        dhpUserProfileService.getAllProfilesAsync()
        val profileListArgumentCaptor = argumentCaptor<List<UserProfile>>()

        // assert
        verify(dhpManager, never()).executeAsync(any(), any())
        verify(callback).invoke(eq(false), profileListArgumentCaptor.capture())
        assertEquals(0, profileListArgumentCaptor.firstValue.size)
    }

    @Test
    fun testThatGetRolesForProfileCompletedReturnsTheProfilesListIfAccountOwnerIsPatient() {
        // arrange
        CloudSessionState.shared.idHubFirstName = "FirstName"
        CloudSessionState.shared.idHubLastName = "LastName"
        DHPSession.shared.federationId = "FED12345"

        val dhpUserProfileService = DHPUserProfileCloudService()
        val callback: (Boolean, List<UserProfile>)->Unit = mock()
        dhpUserProfileService.didGetAllProfiles = callback

        // act
        val getRolesCompletedMethod = dhpUserProfileService::class.java.getDeclaredMethod("getRolesForProfileCompleted", GenericDHPResponse::class.java)
        getRolesCompletedMethod.isAccessible = true
        val dhpResponse: GenericDHPResponse<DHPResponseBody> = mock()
        whenever(dhpResponse.success).thenReturn(true)
        val returnObjects : JSONArray = mock()
        whenever(returnObjects.toString()).thenReturn("Role=patient")
        val responseBody: DHPResponseBody = mock()
        whenever(responseBody.returnObjects).thenReturn(returnObjects)
        whenever(dhpResponse.body).thenReturn(responseBody)

        val profileListArgumentCaptor = argumentCaptor<List<UserProfile>>()

        dhpUserProfileService.getAllProfilesAsync()
        getRolesCompletedMethod.invoke(dhpUserProfileService, dhpResponse)


        // assert
        verify(callback).invoke(eq(true), profileListArgumentCaptor.capture())
        assertEquals(1, profileListArgumentCaptor.firstValue.size)
        assertEquals("FirstName", profileListArgumentCaptor.firstValue[0].firstName)
        assertEquals("LastName", profileListArgumentCaptor.firstValue[0].lastName)
        assertEquals("FED12345", profileListArgumentCaptor.firstValue[0].profileId)
        assertEquals("1970-01-01", profileListArgumentCaptor.firstValue[0].dateOfBirth.toString())
    }

    @Test
    fun testThatGetRolesForProfileCompletedSendsGetAllProfilesRequestToTheDHPManagerIfAccountOwnerIsGuardian() {
        // arrange
        val dhpUserProfileService = DHPUserProfileCloudService()
        val callback: (Boolean, List<UserProfile>)->Unit = mock()
        dhpUserProfileService.didGetAllProfiles = callback

        // act
        val getRolesCompletedMethod = dhpUserProfileService::class.java.getDeclaredMethod("getRolesForProfileCompleted", GenericDHPResponse::class.java)
        getRolesCompletedMethod.isAccessible = true
        val dhpResponse: GenericDHPResponse<DHPResponseBody> = mock()
        whenever(dhpResponse.success).thenReturn(true)
        val returnObjects : JSONArray = mock()
        whenever(returnObjects.toString()).thenReturn("Role=guardian")
        val responseBody: DHPResponseBody = mock()
        whenever(responseBody.returnObjects).thenReturn(returnObjects)
        whenever(dhpResponse.body).thenReturn(responseBody)

        val dhpRequestArgumentCaptor = argumentCaptor<GenericDHPRequest<*>>()
        val callbackArgumentCaptor = argumentCaptor<(Boolean, String, String?)->Unit>()

        getRolesCompletedMethod.invoke(dhpUserProfileService, dhpResponse)

        // assert
        verify(dhpManager).executeAsync(dhpRequestArgumentCaptor.capture(), callbackArgumentCaptor.capture())
        assertEquals(DHPAPIs.getRelatedProfilesList, dhpRequestArgumentCaptor.firstValue.uri)
        assertEquals(DHPGetRelatedProfiles::class.java, dhpRequestArgumentCaptor.firstValue.payload.javaClass)
    }

    @Test
    fun testThatGetRelatedProfilesCompletedReturnsTheProfilesListThroughTheCallback() {
        // arrange
        CloudSessionState.shared.idHubFirstName = "FirstName"
        CloudSessionState.shared.idHubLastName = "LastName"
        DHPSession.shared.federationId = "FED12345"

        val dhpUserProfileService = DHPUserProfileCloudService()
        val callback: (Boolean, List<UserProfile>)->Unit = mock()
        dhpUserProfileService.didGetAllProfiles = callback

        // act
        val getRelatedProfilesCompletedMethod = dhpUserProfileService::class.java.getDeclaredMethod("getRelatedProfilesCompleted", GenericDHPResponse::class.java)
        getRelatedProfilesCompletedMethod.isAccessible = true
        val dhpResponse: GenericDHPResponse<DHPGetRelatedProfilesResponseBody> = mock()
        val profiles :List<DHPGetRelatedProfilesResponseBody.ProfileInfo> = listOf(DHPGetRelatedProfilesResponseBody.ProfileInfo("DepFirstName", "DepLastName", "2002-01-01", "FED12346"))
        val responseBody: DHPGetRelatedProfilesResponseBody = mock()
        whenever(responseBody.profiles).thenReturn(profiles)
        whenever(dhpResponse.body).thenReturn(responseBody)
        whenever(dhpResponse.success).thenReturn(true)

        val profileListArgumentCaptor = argumentCaptor<List<UserProfile>>()

        dhpUserProfileService.getAllProfilesAsync()
        getRelatedProfilesCompletedMethod.invoke(dhpUserProfileService, dhpResponse)


        // assert
        verify(callback).invoke(eq(true), profileListArgumentCaptor.capture())
        assertEquals(2, profileListArgumentCaptor.firstValue.size)
        assertEquals("FirstName", profileListArgumentCaptor.firstValue[0].firstName)
        assertEquals("LastName", profileListArgumentCaptor.firstValue[0].lastName)
        assertEquals("FED12345", profileListArgumentCaptor.firstValue[0].profileId)
        assertEquals("1970-01-01", profileListArgumentCaptor.firstValue[0].dateOfBirth.toString())
        assertEquals("DepFirstName", profileListArgumentCaptor.firstValue[1].firstName)
        assertEquals("DepLastName", profileListArgumentCaptor.firstValue[1].lastName)
        assertEquals("FED12346", profileListArgumentCaptor.firstValue[1].profileId)
        assertEquals("2002-01-01", profileListArgumentCaptor.firstValue[1].dateOfBirth.toString())
    }

    @Test
    fun testThatSetupProfileAsyncSendsRequestToAddAccountOwnerProfileAsPatientIfAccountOwnerProfileIsActiveAndNotPreviouslyRegistered() {
        // arrange
        val dhpUserProfileService = DHPUserProfileCloudService()
        val userProfile = UserProfile("FED12345", "FirstName", "LastName", true, true, false, LocalDate.of(1970, 1, 1), Instant.ofEpochMilli(1516887385499))

        // act
        dhpUserProfileService.setupProfileAsync(userProfile)
        val dhpRequestArgumentCaptor = argumentCaptor<GenericDHPRequest<*>>()
        val callbackArgumentCaptor = argumentCaptor<(Boolean, String, String?)->Unit>()

        // assert
        verify(dhpManager).executeAsync(dhpRequestArgumentCaptor.capture(), callbackArgumentCaptor.capture())
        assertEquals(DHPAPIs.addProfile, dhpRequestArgumentCaptor.firstValue.uri)
        assertEquals(DHPRegisterProfile::class.java, dhpRequestArgumentCaptor.firstValue.payload.javaClass)
        assertEquals(userProfile.firstName, ((dhpRequestArgumentCaptor.firstValue.payload) as DHPRegisterProfile).profile_info!!.firstName)
        assertEquals(userProfile.lastName, ((dhpRequestArgumentCaptor.firstValue.payload) as DHPRegisterProfile).profile_info!!.lastName)
        assertEquals(userProfile.profileId, ((dhpRequestArgumentCaptor.firstValue.payload) as DHPRegisterProfile).profile_info!!.externalEntityID)
        assertEquals(DHPCodes.Role.patient, ((dhpRequestArgumentCaptor.firstValue.payload) as DHPRegisterProfile).invokingRole)
    }

    @Test
    fun testThatSetupProfileAsyncSendsRequestToAddDependentProfileIfDependentProfileIsActiveAndNotPreviouslyRegistered() {
        // arrange
        val dhpUserProfileService = DHPUserProfileCloudService()
        val ownerUserProfile = UserProfile("FED12345", "FirstName", "LastName", true, true, false, LocalDate.of(1970, 1, 1), Instant.ofEpochMilli(1516887385499))
        val dependentUserProfile = UserProfile("FED12346", "DepFirstName", "DepLastName", false, true, false, LocalDate.of(2002, 1, 1), Instant.ofEpochMilli(1516887385499))

        // act
        val profiles = dhpUserProfileService::class.java.getDeclaredField("profiles")
        profiles.isAccessible = true
        profiles.set(dhpUserProfileService, listOf(ownerUserProfile, dependentUserProfile))
        dhpUserProfileService.setupProfileAsync(dependentUserProfile)
        val dhpRequestArgumentCaptor = argumentCaptor<GenericDHPRequest<*>>()
        val callbackArgumentCaptor = argumentCaptor<(Boolean, String, String?)->Unit>()

        // assert
        verify(dhpManager).executeAsync(dhpRequestArgumentCaptor.capture(), callbackArgumentCaptor.capture())
        assertEquals(DHPAPIs.addProfile, dhpRequestArgumentCaptor.firstValue.uri)
        assertEquals(DHPRegisterProfile::class.java, dhpRequestArgumentCaptor.firstValue.payload.javaClass)
        assertEquals(dependentUserProfile.firstName, ((dhpRequestArgumentCaptor.firstValue.payload) as DHPRegisterProfile).profile_info!!.firstName)
        assertEquals(dependentUserProfile.lastName, ((dhpRequestArgumentCaptor.firstValue.payload) as DHPRegisterProfile).profile_info!!.lastName)
        assertEquals(dependentUserProfile.profileId, ((dhpRequestArgumentCaptor.firstValue.payload) as DHPRegisterProfile).profile_info!!.externalEntityID)
        assertEquals(DHPCodes.Role.guardian, ((dhpRequestArgumentCaptor.firstValue.payload) as DHPRegisterProfile).invokingRole)
        assertEquals(ownerUserProfile.firstName, ((dhpRequestArgumentCaptor.firstValue.payload) as DHPRegisterProfile).guardian_info!!.firstName)
        assertEquals(ownerUserProfile.lastName, ((dhpRequestArgumentCaptor.firstValue.payload) as DHPRegisterProfile).guardian_info!!.lastName)
        assertEquals(ownerUserProfile.profileId, ((dhpRequestArgumentCaptor.firstValue.payload) as DHPRegisterProfile).guardian_info!!.externalEntityID)
    }

    @Test
    fun testThatSetupProfileAsyncSendsRequestToAddAccountOwnerProfileAsPatientIfAccountOwnerProfileIsActiveAndPreviouslyRegisteredAsGuardian() {
        // arrange
        val dhpUserProfileService = DHPUserProfileCloudService()
        val userProfile = UserProfile("FED12345", "FirstName", "LastName", true, true, false, LocalDate.of(1970, 1, 1), Instant.ofEpochMilli(1516887385499))

        // act
        val isAccountRegisteredField = dhpUserProfileService::class.java.getDeclaredField("isAccountRegistered")
        val isAccountOwnerAPatientField = dhpUserProfileService::class.java.getDeclaredField("isAccountOwnerAPatient")
        isAccountRegisteredField.isAccessible = true
        isAccountOwnerAPatientField.isAccessible = true
        isAccountRegisteredField.set(dhpUserProfileService, true)
        isAccountOwnerAPatientField.set(dhpUserProfileService, false)
        val dhpRequestArgumentCaptor = argumentCaptor<GenericDHPRequest<*>>()
        val callbackArgumentCaptor = argumentCaptor<(Boolean, String, String?)->Unit>()
        dhpUserProfileService.setupProfileAsync(userProfile)

        // assert
        verify(dhpManager).executeAsync(dhpRequestArgumentCaptor.capture(), callbackArgumentCaptor.capture())
        assertEquals(DHPAPIs.addProfile, dhpRequestArgumentCaptor.firstValue.uri)
        assertEquals(DHPRegisterProfile::class.java, dhpRequestArgumentCaptor.firstValue.payload.javaClass)
        assertEquals(userProfile.firstName, ((dhpRequestArgumentCaptor.firstValue.payload) as DHPRegisterProfile).profile_info!!.firstName)
        assertEquals(userProfile.lastName, ((dhpRequestArgumentCaptor.firstValue.payload) as DHPRegisterProfile).profile_info!!.lastName)
        assertEquals(userProfile.profileId, ((dhpRequestArgumentCaptor.firstValue.payload) as DHPRegisterProfile).profile_info!!.externalEntityID)
        assertEquals(DHPCodes.Role.patient, ((dhpRequestArgumentCaptor.firstValue.payload) as DHPRegisterProfile).invokingRole)
    }

    @Test
    fun testThatSetupProfileAsyncDoesNotSendRequestToDHPManagerIfAccountOwnerProfileIsActiveAndPreviouslyRegisteredAsPatientAndProfileIDIsNotAvailable() {
        // arrange
        val dhpUserProfileService = DHPUserProfileCloudService()
        val userProfile = UserProfile(null, "FirstName", "LastName", true, true, false, LocalDate.of(1970, 1, 1), Instant.ofEpochMilli(1516887385499))

        // act
        val isAccountRegisteredField = dhpUserProfileService::class.java.getDeclaredField("isAccountRegistered")
        val isAccountOwnerAPatientField = dhpUserProfileService::class.java.getDeclaredField("isAccountOwnerAPatient")
        isAccountRegisteredField.isAccessible = true
        isAccountOwnerAPatientField.isAccessible = true
        isAccountRegisteredField.set(dhpUserProfileService, true)
        isAccountOwnerAPatientField.set(dhpUserProfileService, true)
        dhpUserProfileService.setupProfileAsync(userProfile)

        // assert
        verify(dhpManager, never()).executeAsync(any(), any())
    }

    @Test
    fun testThatSetupProfileAsyncSendsRequestToRegisterMobileDeviceIfAccountOwnerProfileIsActiveAndPreviouslyRegisteredAsPatient() {
        // arrange
        val dhpUserProfileService = DHPUserProfileCloudService()
        val userProfile = UserProfile("FED12345", "FirstName", "LastName", true, true, false, LocalDate.of(1970, 1, 1), Instant.ofEpochMilli(1516887385499))

        // act
        val isAccountRegisteredField = dhpUserProfileService::class.java.getDeclaredField("isAccountRegistered")
        val isAccountOwnerAPatientField = dhpUserProfileService::class.java.getDeclaredField("isAccountOwnerAPatient")
        isAccountRegisteredField.isAccessible = true
        isAccountOwnerAPatientField.isAccessible = true
        isAccountRegisteredField.set(dhpUserProfileService, true)
        isAccountOwnerAPatientField.set(dhpUserProfileService, true)
        val dhpRequestArgumentCaptor = argumentCaptor<GenericDHPRequest<*>>()
        val callbackArgumentCaptor = argumentCaptor<(Boolean, String, String?)->Unit>()
        dhpUserProfileService.setupProfileAsync(userProfile)

        // assert
        verify(dhpManager).executeAsync(dhpRequestArgumentCaptor.capture(), callbackArgumentCaptor.capture())
        assertEquals(DHPAPIs.addMobileDevice, dhpRequestArgumentCaptor.firstValue.uri)
        assertEquals(DHPAddMobileDevice::class.java, dhpRequestArgumentCaptor.firstValue.payload.javaClass)
    }

    @Test
    fun testThatSetupProfileAsyncSendsRequestToRegisterMobileDeviceIfDependentProfileIsActiveAndPreviouslyRegisteredAsPatient() {
        // arrange
        val dhpUserProfileService = DHPUserProfileCloudService()
        val userProfile = UserProfile("FED12346", "DepFirstName", "DepLastName", false, true, false, LocalDate.of(2002, 1, 1), Instant.ofEpochMilli(1516887385499))

        // act
        val dhpRequestArgumentCaptor = argumentCaptor<GenericDHPRequest<*>>()
        val callbackArgumentCaptor = argumentCaptor<(Boolean, String, String?)->Unit>()
        val isAccountRegisteredField = dhpUserProfileService::class.java.getDeclaredField("isAccountRegistered")
        isAccountRegisteredField.isAccessible = true
        isAccountRegisteredField.set(dhpUserProfileService, true)
        dhpUserProfileService.setupProfileAsync(userProfile)

        // assert
        verify(dhpManager).executeAsync(dhpRequestArgumentCaptor.capture(), callbackArgumentCaptor.capture())
        assertEquals(DHPAPIs.addMobileDevice, dhpRequestArgumentCaptor.firstValue.uri)
        assertEquals(DHPAddMobileDevice::class.java, dhpRequestArgumentCaptor.firstValue.payload.javaClass)
    }

    @Test
    fun testThatSetupProfileAsyncRegistersDependentIfDependentProfileWasNotPreviouslySetupAndAccountOwnerWasPreviouslyRegisteredAsGuardian() {
        // arrange
        val dhpUserProfileService = DHPUserProfileCloudService()
        val userProfile = UserProfile(null, "DepFirstName", "DepLastName", false, true, false, LocalDate.of(2002, 1, 1), Instant.ofEpochMilli(1516887385499))

        // act
        val dhpRequestArgumentCaptor = argumentCaptor<GenericDHPRequest<*>>()
        val callbackArgumentCaptor = argumentCaptor<(Boolean, String, String?)->Unit>()
        val isAccountRegisteredField = dhpUserProfileService::class.java.getDeclaredField("isAccountRegistered")
        isAccountRegisteredField.isAccessible = true
        isAccountRegisteredField.set(dhpUserProfileService, true)
        val isAccountOwnerAGuardianField = dhpUserProfileService::class.java.getDeclaredField("isAccountOwnerAGuardian")
        isAccountOwnerAGuardianField.isAccessible = true
        isAccountOwnerAGuardianField.set(dhpUserProfileService, true)
        dhpUserProfileService.setupProfileAsync(userProfile)

        // assert
        verify(dhpManager).executeAsync(dhpRequestArgumentCaptor.capture(), callbackArgumentCaptor.capture())
        assertEquals(DHPAPIs.addDependentPatient, dhpRequestArgumentCaptor.firstValue.uri)
        assertEquals(DHPAddDependentPatient::class.java, dhpRequestArgumentCaptor.firstValue.payload.javaClass)
    }

    @Test
    fun testThatSetupProfileAsyncRegistersDependentAndGuardianIfDependentProfileWasNotPreviouslySetupAndAccountOwnerWasNotPreviouslyRegisteredAsGuardian() {
        // arrange
        val dhpUserProfileService = DHPUserProfileCloudService()
        val ownerUserProfile = UserProfile("FED12345", "FirstName", "LastName", true, true, false, LocalDate.of(1970, 1, 1), Instant.ofEpochMilli(1516887385499))
        val dependentUserProfile = UserProfile(null, "DepFirstName", "DepLastName", false, true, false, LocalDate.of(2002, 1, 1), Instant.ofEpochMilli(1516887385499))

        // act
        val dhpRequestArgumentCaptor = argumentCaptor<GenericDHPRequest<*>>()
        val callbackArgumentCaptor = argumentCaptor<(Boolean, String, String?)->Unit>()
        val profiles = dhpUserProfileService::class.java.getDeclaredField("profiles")
        profiles.isAccessible = true
        profiles.set(dhpUserProfileService, listOf(ownerUserProfile, dependentUserProfile))
        val isAccountRegisteredField = dhpUserProfileService::class.java.getDeclaredField("isAccountRegistered")
        isAccountRegisteredField.isAccessible = true
        isAccountRegisteredField.set(dhpUserProfileService, true)
        val isAccountOwnerAGuardianField = dhpUserProfileService::class.java.getDeclaredField("isAccountOwnerAGuardian")
        isAccountOwnerAGuardianField.isAccessible = true
        isAccountOwnerAGuardianField.set(dhpUserProfileService, false)
        dhpUserProfileService.setupProfileAsync(dependentUserProfile)

        // assert
        verify(dhpManager).executeAsync(dhpRequestArgumentCaptor.capture(), callbackArgumentCaptor.capture())
        assertEquals(DHPAPIs.addProfile, dhpRequestArgumentCaptor.firstValue.uri)
        assertEquals(DHPRegisterProfile::class.java, dhpRequestArgumentCaptor.firstValue.payload.javaClass)
        assertEquals(dependentUserProfile.firstName, ((dhpRequestArgumentCaptor.firstValue.payload) as DHPRegisterProfile).profile_info!!.firstName)
        assertEquals(dependentUserProfile.lastName, ((dhpRequestArgumentCaptor.firstValue.payload) as DHPRegisterProfile).profile_info!!.lastName)
        assertEquals(dependentUserProfile.profileId, ((dhpRequestArgumentCaptor.firstValue.payload) as DHPRegisterProfile).profile_info!!.externalEntityID)
        assertEquals(DHPCodes.Role.guardian, ((dhpRequestArgumentCaptor.firstValue.payload) as DHPRegisterProfile).invokingRole)
        assertEquals(ownerUserProfile.firstName, ((dhpRequestArgumentCaptor.firstValue.payload) as DHPRegisterProfile).guardian_info!!.firstName)
        assertEquals(ownerUserProfile.lastName, ((dhpRequestArgumentCaptor.firstValue.payload) as DHPRegisterProfile).guardian_info!!.lastName)
        assertEquals(ownerUserProfile.profileId, ((dhpRequestArgumentCaptor.firstValue.payload) as DHPRegisterProfile).guardian_info!!.externalEntityID)
    }

    @Test
    fun testThatSetupProfileCompletedInvokesTheCallbackToSendTheResponse() {
        // arrange
        val dhpUserProfileService = DHPUserProfileCloudService()
        val callback: (Boolean, UserProfile)->Unit = mock()
        dhpUserProfileService.didSetProfile = callback
        val userProfile: UserProfile = mock()

        // act
        val setupProfileCompletedMethod = dhpUserProfileService::class.java.getDeclaredMethod("setupProfileCompleted", GenericDHPResponse::class.java)
        setupProfileCompletedMethod.isAccessible = true
        val userProfileRequestQueueManagerField = dhpUserProfileService::class.java.getDeclaredField("userProfileRequestQueue")
        userProfileRequestQueueManagerField.isAccessible = true
        val partialProfileInfoField = userProfileRequestQueueManagerField.type.getDeclaredField("partialProfileInfo")
        partialProfileInfoField.isAccessible = true
        partialProfileInfoField.set(userProfileRequestQueueManagerField.get(dhpUserProfileService), mutableListOf(userProfile))
        val dhpResponse: GenericDHPResponse<DHPResponseBody> = mock()
        whenever(dhpResponse.success).thenReturn(true)

        setupProfileCompletedMethod.invoke(dhpUserProfileService, dhpResponse)

        // assert
        verify(callback).invoke(eq(true), eq(userProfile))
    }

    @Test
    fun testThatAddDependentProfileCompletedInvokesTheCallbackToSendTheResponse() {
        // arrange
        val dhpUserProfileService = DHPUserProfileCloudService()
        val callback: (Boolean, UserProfile)->Unit = mock()
        dhpUserProfileService.didSetProfile = callback
        val userProfile: UserProfile = mock()

        // act
        val addDependentProfileCompletedMethod = dhpUserProfileService::class.java.getDeclaredMethod("addDependentProfileCompleted", GenericDHPResponse::class.java)
        addDependentProfileCompletedMethod.isAccessible = true
        val userProfileRequestQueueManagerField = dhpUserProfileService::class.java.getDeclaredField("userProfileRequestQueue")
        userProfileRequestQueueManagerField.isAccessible = true
        val partialProfileInfoField = userProfileRequestQueueManagerField.type.getDeclaredField("partialProfileInfo")
        partialProfileInfoField.isAccessible = true
        partialProfileInfoField.set(userProfileRequestQueueManagerField.get(dhpUserProfileService), mutableListOf(userProfile))
        val dhpResponse: GenericDHPResponse<DHPAddDependentProfileResponseBody> = mock()
        whenever(dhpResponse.success).thenReturn(true)
        val responseBody: DHPAddDependentProfileResponseBody = mock()
        whenever(responseBody.externalEntityID).thenReturn("FED12346")
        whenever(dhpResponse.body).thenReturn(responseBody)

        addDependentProfileCompletedMethod.invoke(dhpUserProfileService, dhpResponse)

        // assert
        verify(callback).invoke(eq(true), eq(userProfile))
    }

    @Test
    fun testThatGetUserAppListAsyncSendsRequestToTheDHPManager() {
        // arrange
        val dhpUserProfileService = DHPUserProfileCloudService()
        val userProfile = UserProfile("FED12346", "DepFirstName", "DepLastName", false, true, false, LocalDate.of(2002, 1, 1), Instant.ofEpochMilli(1516887385499))

        // act
        val dhpRequestArgumentCaptor = argumentCaptor<GenericDHPRequest<*>>()
        val callbackArgumentCaptor = argumentCaptor<(Boolean, String, String?)->Unit>()
        dhpUserProfileService.getUserAppListAsync(userProfile)

        // assert
        verify(dhpManager).executeAsync(dhpRequestArgumentCaptor.capture(), callbackArgumentCaptor.capture())
        assertEquals(DHPAPIs.getPatientAppList, dhpRequestArgumentCaptor.firstValue.uri)
        assertEquals(DHPGetPatientAppList::class.java, dhpRequestArgumentCaptor.firstValue.payload.javaClass)
    }
}