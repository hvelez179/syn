/*
 *
 *  DHPCareProgramCloudServiceTests.kt
 *  teva_cloud
 *
 *  Copyright © 2018 Teva. All rights reserved.
 *
 */

package com.teva.cloud.services.programmanagement

import android.content.SharedPreferences
import com.nhaarman.mockito_kotlin.*
import com.teva.cloud.dataentities.ProgramData
import com.teva.cloud.models.programmanagement.InvitationDetails
import com.teva.cloud.models.userprofile.UserProfileManager
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.dhp.DataEntities.DHPDataTypes.Endpoints.DHPAcceptInvitation
import com.teva.dhp.DataEntities.DHPDataTypes.Endpoints.DHPGetInvitationDetails
import com.teva.dhp.DataEntities.DHPDataTypes.Endpoints.DHPGetUserProgramAppList
import com.teva.dhp.DataEntities.DHPDataTypes.Endpoints.DHPOptOutDataSharing
import com.teva.dhp.DataEntities.DHPDataTypes.GenericDHPRequest
import com.teva.dhp.DataEntities.DHPDataTypes.GenericDHPResponse
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.*
import com.teva.dhp.models.DHPAPIs
import com.teva.dhp.models.DHPManager
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

/**
 * This class defines unit tests for the DHPCareProgramCloudService class.
 */
class DHPCareProgramCloudServiceTests {
    private var dependencyProvider: DependencyProvider = DependencyProvider.default
    private val dhpManager: DHPManager = mock()
    private val timeService: TimeService = mock()
    private val userProfileManager: UserProfileManager = mock()
    private val sharedPreferences: SharedPreferences = mock()
    private val sharedPreferencesEditor: SharedPreferences.Editor = mock()

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
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        DependencyProvider.default.unregisterAll()
    }

    @Test
    fun testThatGetUserProgramListAsyncSendsTheCorrectRequestToTheDHPManager() {
        // arrange
        val dhpCareProgramService = DHPCareProgramCloudService()

        // act
        dhpCareProgramService.getUserProgramListAsync()
        val dhpRequestArgumentCaptor = argumentCaptor<GenericDHPRequest<*>>()
        val callbackArgumentCaptor = argumentCaptor<(Boolean, String, String?)->Unit>()

        // assert
        verify(dhpManager).executeAsync(dhpRequestArgumentCaptor.capture(), callbackArgumentCaptor.capture())
        assertEquals(DHPAPIs.getUserProgramAppList, dhpRequestArgumentCaptor.firstValue.uri)
        assertEquals(DHPGetUserProgramAppList::class.java, dhpRequestArgumentCaptor.firstValue.payload.javaClass)
    }

    @Test
    fun testThatGetInvitationDetailsAsyncSendsTheCorrectRequestToTheDHPManager() {
        // arrange
        val dhpCareProgramService = DHPCareProgramCloudService()
        val invitationCode = "I0001"

        // act
        dhpCareProgramService.getInvitationDetailsAsync(invitationCode)
        val dhpRequestArgumentCaptor = argumentCaptor<GenericDHPRequest<*>>()
        val callbackArgumentCaptor = argumentCaptor<(Boolean, String, String?)->Unit>()

        // assert
        verify(dhpManager).executeAsync(dhpRequestArgumentCaptor.capture(), callbackArgumentCaptor.capture())
        assertEquals(DHPAPIs.getInvitationDetails, dhpRequestArgumentCaptor.firstValue.uri)
        assertEquals(DHPGetInvitationDetails::class.java, dhpRequestArgumentCaptor.firstValue.payload.javaClass)
    }

    @Test
    fun testThatAcceptInvitationAsyncSendsTheCorrectRequestToTheDHPManager() {
        // arrange
        val dhpCareProgramService = DHPCareProgramCloudService()
        val invitationDetails: InvitationDetails = mock()

        // act
        dhpCareProgramService.acceptInvitationAsync(invitationDetails)
        val dhpRequestArgumentCaptor = argumentCaptor<GenericDHPRequest<*>>()
        val callbackArgumentCaptor = argumentCaptor<(Boolean, String, String?)->Unit>()

        // assert
        verify(dhpManager).executeAsync(dhpRequestArgumentCaptor.capture(), callbackArgumentCaptor.capture())
        assertEquals(DHPAPIs.acceptInvitation, dhpRequestArgumentCaptor.firstValue.uri)
        assertEquals(DHPAcceptInvitation::class.java, dhpRequestArgumentCaptor.firstValue.payload.javaClass)
    }

    @Test
    fun testThatLeaveProgramAsyncSendsTheCorrectRequestToTheDHPManager() {
        // arrange
        val dhpCareProgramService = DHPCareProgramCloudService()
        val programId = "P0001"

        // act
        dhpCareProgramService.leaveProgramAsync(programId)
        val dhpRequestArgumentCaptor = argumentCaptor<GenericDHPRequest<*>>()
        val callbackArgumentCaptor = argumentCaptor<(Boolean, String, String?)->Unit>()

        // assert
        verify(dhpManager).executeAsync(dhpRequestArgumentCaptor.capture(), callbackArgumentCaptor.capture())
        assertEquals(DHPAPIs.consentOptOut, dhpRequestArgumentCaptor.firstValue.uri)
        assertEquals(DHPOptOutDataSharing::class.java, dhpRequestArgumentCaptor.firstValue.payload.javaClass)
    }

    @Test
    fun testThatErrorDetailsAreSentToTheCareProgramDelegateIfGetInvitationDetailsRequestFails() {
        // arrange
        val dhpCareProgramService = DHPCareProgramCloudService()

        val getInvitationDetailsCompleted: ((errorCode: CareProgramErrorCode, errorDetails: List<CareProgramErrorDetail>?, invitationDetails: InvitationDetails) -> Unit)= mock()
        dhpCareProgramService.didGetInvitationDetails = getInvitationDetailsCompleted

        val responseBody: DHPGetInvitationDetailResponseBody = mock()
        whenever(responseBody.responseMessageCode).thenReturn("112")
        val response: GenericDHPResponse<DHPGetInvitationDetailResponseBody> = mock()
        whenever(response.body).thenReturn(responseBody)
        whenever(response.success).thenReturn(false)

        // act
        val getInvitationDetailsCompletedMethod = dhpCareProgramService.javaClass.getDeclaredMethod("getInvitationDetailsCompleted", GenericDHPResponse::class.java)
        getInvitationDetailsCompletedMethod.isAccessible = true
        getInvitationDetailsCompletedMethod.invoke(dhpCareProgramService, response)

        // assert
        verify(getInvitationDetailsCompleted)(eq(CareProgramErrorCode.API_REQUEST_FAILED), isNull(), any())
    }

    @Test
    fun testThatErrorDetailsAreSentToTheCareProgramDelegateIfGetUserProgramAppListRequestFails() {
        // arrange
        val dhpCareProgramService = DHPCareProgramCloudService()

        val getUserProgramListCompleted: ((errorCode: CareProgramErrorCode, errorDetails: List<CareProgramErrorDetail>?, programs: List<ProgramData>) -> Unit) = mock()
        dhpCareProgramService.didGetUserProgramList = getUserProgramListCompleted

        val responseBody: DHPGetUserProgramAppListResponseBody = mock()
        val response: GenericDHPResponse<DHPGetUserProgramAppListResponseBody> = mock()
        whenever(response.body).thenReturn(responseBody)
        whenever(response.success).thenReturn(false)

        // act
        val getUserProgramAppListCompletedMethod = dhpCareProgramService.javaClass.getDeclaredMethod("getUserProgramAppListCompleted", GenericDHPResponse::class.java)
        getUserProgramAppListCompletedMethod.isAccessible = true
        getUserProgramAppListCompletedMethod.invoke(dhpCareProgramService, response)

        // assert
        verify(getUserProgramListCompleted)(eq(CareProgramErrorCode.UNKNOWN), isNull(), any())
    }

    @Test
    fun testThatProgramListIsSentToTheCareProgramDelegateIfGetUserProgramAppListRequestSucceeds() {
        // arrange
        val dhpCareProgramService = DHPCareProgramCloudService()

        val getUserProgramListCompleted: ((errorCode: CareProgramErrorCode, errorDetails: List<CareProgramErrorDetail>?, programs: List<ProgramData>) -> Unit) = mock()
        dhpCareProgramService.didGetUserProgramList = getUserProgramListCompleted

        val programGroups:MutableList<Group> = ArrayList()
        programGroups.add(Group("P0001", "Asthma Program", "Group1", "true", "G0001", "I0001", listOf(AppInfo("eProAir", "1.0", "2018-01-24"))))
        val returnObject = GetUserProgramAppListReturnObject(programGroups)
        val responseBody: DHPGetUserProgramAppListResponseBody = mock()
        whenever(responseBody.responseMessageCode).thenReturn("110")
        whenever(responseBody.getUserProgramAppListReturnObject).thenReturn(returnObject)
        val response: GenericDHPResponse<DHPGetUserProgramAppListResponseBody> = mock()
        whenever(response.body).thenReturn(responseBody)
        whenever(response.success).thenReturn(true)

        // act
        val getUserProgramAppListCompletedMethod = dhpCareProgramService.javaClass.getDeclaredMethod("getUserProgramAppListCompleted", GenericDHPResponse::class.java)
        getUserProgramAppListCompletedMethod.isAccessible = true
        getUserProgramAppListCompletedMethod.invoke(dhpCareProgramService, response)
        val programListArgumentCaptor = argumentCaptor<List<ProgramData>>()

        // assert
        verify(getUserProgramListCompleted)(eq(CareProgramErrorCode.NO_ERROR), any(), programListArgumentCaptor.capture())
        assertEquals(1, programListArgumentCaptor.firstValue.size)
        assertEquals("P0001", programListArgumentCaptor.firstValue[0].programId)
        assertEquals("I0001", programListArgumentCaptor.firstValue[0].invitationCode)
        assertEquals(true, programListArgumentCaptor.firstValue[0].active)
        assertEquals("Asthma Program", programListArgumentCaptor.firstValue[0].programName)
        assertEquals(1, programListArgumentCaptor.firstValue[0].consentedApps!!.size)
        assertEquals("eProAir", programListArgumentCaptor.firstValue[0].consentedApps!![0].appName)
        assertEquals("1.0", programListArgumentCaptor.firstValue[0].consentedApps!![0].appVersionNumber)
    }

    @Test
    fun testThatErrorDetailsAreSentToTheCareProgramDelegateIfAcceptInvitationRequestFails() {
        // arrange
        val dhpCareProgramService = DHPCareProgramCloudService()

        val acceptInvitationCompleted: ((errorCode: CareProgramErrorCode, errorDetails: List<CareProgramErrorDetail>?, invitationDetails: InvitationDetails?) -> Unit) = mock()
        dhpCareProgramService.didAcceptInvitation = acceptInvitationCompleted

        val responseBody: DHPResponseBody = mock()
        whenever(responseBody.responseMessageCode).thenReturn("112")
        val response: GenericDHPResponse<DHPResponseBody> = mock()
        whenever(response.body).thenReturn(responseBody)
        whenever(response.success).thenReturn(false)

        // act
        val acceptInvitationCompletedMethod = dhpCareProgramService.javaClass.getDeclaredMethod("acceptInvitationCompleted", GenericDHPResponse::class.java)
        acceptInvitationCompletedMethod.isAccessible = true
        acceptInvitationCompletedMethod.invoke(dhpCareProgramService, response)

        // assert
        verify(acceptInvitationCompleted)(eq(CareProgramErrorCode.API_REQUEST_FAILED), any(), isNull())
    }

    @Test
    fun testThatErrorDetailsAreSentToTheCareProgramDelegateIfConsentOptOutRequestFails() {
        // arrange
        val dhpCareProgramService = DHPCareProgramCloudService()

        val leaveProgramCompleted: ((errorCode: CareProgramErrorCode, errorDetails: List<CareProgramErrorDetail>?, programId: String) -> Unit) = mock()
        dhpCareProgramService.didLeaveProgram = leaveProgramCompleted

        val responseBody: DHPResponseBody = mock()
        whenever(responseBody.responseMessageCode).thenReturn("112")
        val response: GenericDHPResponse<DHPResponseBody> = mock()
        whenever(response.body).thenReturn(responseBody)
        whenever(response.success).thenReturn(false)

        // act
        val consentOptOutCompletedMethod = dhpCareProgramService.javaClass.getDeclaredMethod("consentOptOutCompleted", GenericDHPResponse::class.java)
        consentOptOutCompletedMethod.isAccessible = true
        consentOptOutCompletedMethod.invoke(dhpCareProgramService, response)

        // assert
        verify(leaveProgramCompleted)(eq(CareProgramErrorCode.API_REQUEST_FAILED), any(), eq(""))
    }
}