//
// CareProgramManagerImplTests.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.models.programmanagement

import android.content.SharedPreferences
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.*
import com.teva.cloud.dataentities.CloudAppData
import com.teva.cloud.dataentities.ProgramData
import com.teva.cloud.dataentities.UserProfile
import com.teva.cloud.dataquery.ProgramDataQuery
import com.teva.cloud.messages.*
import com.teva.cloud.models.CloudManager
import com.teva.cloud.models.CloudSessionState
import com.teva.cloud.models.userprofile.UserProfileManager
import com.teva.cloud.services.programmanagement.*
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.dhp.models.DHPManager
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test


/**
 * This class defines unit tests for the CareProgramManagerImpl class.
 */
class CareProgramManagerImplTests {
    private var dependencyProvider: DependencyProvider = DependencyProvider.default
    private val messenger: Messenger = mock()
    private val cloudManager: CloudManager = mock()
    private val programDataQuery: ProgramDataQuery = mock()
    private val dhpManager: DHPManager = mock()
    private val userProfileManager: UserProfileManager = mock()
    private val timeService: TimeService = mock()
    private val sharedPreferences: SharedPreferences = mock()
    private val sharedPreferencesEditor: SharedPreferences.Editor = mock()

    @Before
    @Throws(Exception::class)
    fun setup() {
        dependencyProvider.register(Messenger::class, messenger)
        dependencyProvider.register(CloudManager::class, cloudManager)
        dependencyProvider.register(ProgramDataQuery::class, programDataQuery)
        dependencyProvider.register(DHPManager::class, dhpManager)
        dependencyProvider.register(UserProfileManager::class, userProfileManager)
        dependencyProvider.register(TimeService::class, timeService)
        whenever(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor)
        dependencyProvider.register(SharedPreferences::class, sharedPreferences)
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        DependencyProvider.default.unregisterAll()
    }

    @Test
    fun testThatCanManagerProgramsReturnsFalseIfUserHasNotLoggedIn() {
        // arrange
        val careProgramManager = CareProgramManagerImpl()
        careProgramManager.hasManagedInitialUserProgramList = true
        whenever(cloudManager.isLoggedIn).thenReturn(false)
        whenever(userProfileManager.activeUserAppList).thenReturn(ArrayList())

        // act
        val canManagerPrograms = careProgramManager.canManagePrograms

        // assert
        assertEquals(false, canManagerPrograms)
    }

    @Test
    fun testThatCanManagerProgramsReturnsFalseIfUserAppListIsNull() {
        // arrange
        val careProgramManager = CareProgramManagerImpl()
        careProgramManager.hasManagedInitialUserProgramList = true
        whenever(cloudManager.isLoggedIn).thenReturn(true)
        whenever(userProfileManager.activeUserAppList).thenReturn(null)

        // act
        val canManagerPrograms = careProgramManager.canManagePrograms

        // assert
        assertEquals(false, canManagerPrograms)
    }

    @Test
    fun testThatCareProgramManagerReturnsAppNameAndVersionFromCloudSessionState() {
        // arrange
        val careProgramManager = CareProgramManagerImpl()
        CloudSessionState.shared.appName = "eProAir"
        CloudSessionState.shared.appVersionNumber = "1.0"

        // act
        val appName = careProgramManager.currentAppName
        val appVersion = careProgramManager.currentAppVersionNumber

        // assert
        assertEquals(CloudSessionState.shared.appName, appName)
        assertEquals(CloudSessionState.shared.appVersionNumber, appVersion)
    }

    @Test
    fun testThatGetAndManageUserProgramListAsyncDoesNotCallTheCorrespondingMethodOnTheCareProgramServiceIfUserProgramListWasRetrieved() {
        // arrange
        val careProgramManager = CareProgramManagerImpl()
        val dhpCareProgramService: DHPCareProgramCloudService = mock()
        careProgramManager.careProgramService = dhpCareProgramService
        careProgramManager.hasRetrievedInitialUserProgramList = true

        // act
        careProgramManager.getAndManageInitialUserProgramListAsync()

        // assert
        verify(dhpCareProgramService, never()).getUserProgramListAsync()
    }

    @Test
    fun testThatGetAndManageUserProgramListAsyncPostsARemindConsentMessageIfProgramWasNotConsented() {
        // arrange
        val careProgramManager = CareProgramManagerImpl()
        val dhpCareProgramService: DHPCareProgramCloudService = mock()
        careProgramManager.careProgramService = dhpCareProgramService
        careProgramManager.hasRetrievedInitialUserProgramList = true
        val userProfile: UserProfile = mock()
        whenever(userProfile.profileId).thenReturn("12345")
        whenever(userProfileManager.getActive()).thenReturn(userProfile)
        val existingProgram = ProgramData("Asthma Program", "P0001", "12345", mutableListOf(CloudAppData("CareTrx", "2.0")))
        whenever(programDataQuery.getCarePrograms("12345")).thenReturn(listOf(existingProgram))

        // act
        careProgramManager.getAndManageInitialUserProgramListAsync()

        // assert
        verify(messenger).post(any<RemindProgramConsentMessage>())
    }

    @Test
    fun testThatGetAndManageUserProgramListAsyncCallsTheCorrespondingMethodOnTheCareProgramServiceIfUserProgramListWasRetrieved() {
        // arrange
        val careProgramManager = CareProgramManagerImpl()
        val dhpCareProgramService: DHPCareProgramCloudService = mock()
        careProgramManager.careProgramService = dhpCareProgramService

        // act
        careProgramManager.getAndManageInitialUserProgramListAsync()

        // assert
        verify(dhpCareProgramService).getUserProgramListAsync()
    }

    @Test
    fun testThatGetInvitationDetailsAsyncCallsTheCorrespondingMethodOnTheCareProgramService() {
        // arrange
        val careProgramManager = CareProgramManagerImpl()
        val dhpCareProgramService: DHPCareProgramCloudService = mock()
        careProgramManager.careProgramService = dhpCareProgramService
        val invitationCode = "900300123"

        // act
        careProgramManager.getInvitationDetailsAsync(invitationCode)

        // assert
        verify(dhpCareProgramService).getInvitationDetailsAsync(eq(invitationCode))
    }

    @Test
    fun testThatAcceptInvitationDetailsAsyncCallsTheCorrespondingMethodOnTheCareProgramService() {
        // arrange
        val careProgramManager = CareProgramManagerImpl()
        val dhpCareProgramService: DHPCareProgramCloudService = mock()
        careProgramManager.careProgramService = dhpCareProgramService
        val invitationDetails: InvitationDetails = mock()

        // act
        careProgramManager.acceptInvitationAsync(invitationDetails)

        // assert
        verify(dhpCareProgramService).acceptInvitationAsync(eq(invitationDetails))
    }

    @Test
    fun testThatGetCareProgramsReturnsAnEmptyListIfUserProfileIdIsNotProvided() {
        // arrange
        val careProgramManager = CareProgramManagerImpl()
        val dhpCareProgramService: DHPCareProgramCloudService = mock()
        careProgramManager.careProgramService = dhpCareProgramService
        val userProfile: UserProfile = mock()
        whenever(userProfile.profileId).thenReturn(null)

        // act
        val programs = careProgramManager.getCarePrograms(userProfile)

        // assert
        assertEquals(0, programs.size)
    }

    @Test
    fun testThatGetCareProgramsCallsTheCorrespondingMethodOnTheProgramDataQuery() {
        // arrange
        val careProgramManager = CareProgramManagerImpl()
        val dhpCareProgramService: DHPCareProgramCloudService = mock()
        careProgramManager.careProgramService = dhpCareProgramService
        val profileId = "12345"
        val userProfile: UserProfile = mock()
        whenever(userProfile.profileId).thenReturn(profileId)

        // act
        careProgramManager.getCarePrograms(userProfile)

        // assert
        verify(programDataQuery).getCarePrograms(eq(profileId))
    }

    @Test
    fun testThatCareProgramManagerDoesNotUpdateProgramListIfUserProfileIdIsNotProvided() {
        // arrange
        val careProgramManager = CareProgramManagerImpl()
        val userProfile: UserProfile = mock()
        whenever(userProfile.profileId).thenReturn(null)

        // act
        careProgramManager.update(mock(), userProfile)

        // assert
        verify(messenger, never()).post(any<ProgramListUpdatedMessage>())
    }

    @Test
    fun testThatCareProgramManagerUpdateUpdatesProgramDetailsInDatabaseIfItAlreadyExistsAndPostsAProgramListUpdatedMessage() {
        // arrange
        val careProgramManager = CareProgramManagerImpl()
        val userProfile: UserProfile = mock()
        whenever(userProfile.profileId).thenReturn("12345")
        val existingProgram = ProgramData("Asthma Program", "P0001", "12345")
        val updatedProgram = ProgramData("Asthma Program Updated", "P0001", "12345", mutableListOf(CloudAppData("eProAir", "1.0")), "900300123", true)
        whenever(programDataQuery.getCarePrograms("12345")).thenReturn(listOf(existingProgram))

        // act
        careProgramManager.update(listOf(updatedProgram), userProfile)
        val programDataArgumentCaptor = argumentCaptor<ProgramData>()

        // assert
        verify(programDataQuery).insertOrUpdate(programDataArgumentCaptor.capture(), eq(true))
        assertEquals(updatedProgram.programId, programDataArgumentCaptor.firstValue.programId)
        assertEquals(updatedProgram.profileId, programDataArgumentCaptor.firstValue.profileId)
        assertEquals(existingProgram.programName, programDataArgumentCaptor.firstValue.programName)
        assertEquals(updatedProgram.invitationCode, programDataArgumentCaptor.firstValue.invitationCode)

        verify(messenger).post(any<ProgramListUpdatedMessage>())
    }

    @Test
    fun testThatCareProgramManagerUpdateDeletesProgramDetailsFromDatabaseIfItIsNoLongerActive() {
        // arrange
        val careProgramManager = CareProgramManagerImpl()
        val userProfile: UserProfile = mock()
        whenever(userProfile.profileId).thenReturn("12345")
        val existingProgram = ProgramData("Asthma Program", "P0001", "12345")
        val updatedProgram = ProgramData("New Asthma Program", "P0002", "12345", mutableListOf(CloudAppData("eProAir", "1.0")), "900300123", true)
        whenever(programDataQuery.getCarePrograms("12345")).thenReturn(listOf(existingProgram))

        // act
        careProgramManager.update(listOf(updatedProgram), userProfile)
        val programDataArgumentCaptor = argumentCaptor<ProgramData>()

        // assert
        verify(programDataQuery).delete(programDataArgumentCaptor.capture())
        assertEquals(existingProgram.programId, programDataArgumentCaptor.firstValue.programId)
    }

    @Test
    fun testThatLeaveProgramAsyncCallsTheCorrespondingMethodOnTheCareProgramService() {
        // arrange
        val careProgramManager = CareProgramManagerImpl()
        val dhpCareProgramService: DHPCareProgramCloudService = mock()
        careProgramManager.careProgramService = dhpCareProgramService
        val programId = "P0001"

        // act
        careProgramManager.leaveCareProgramAsync(programId)

        // assert
        dhpCareProgramService.leaveProgramAsync(eq(programId))
    }

    @Test
    fun testThatContinueConsentAddsTheAppToTheProgramConsentedAppListAndMarksTheUserProfileAsChanged() {
        // arrange
        val careProgramManager = CareProgramManagerImpl()
        val dhpCareProgramService: DHPCareProgramCloudService = mock()
        careProgramManager.careProgramService = dhpCareProgramService
        val programId = "P0001"
        val userProfile: UserProfile = mock()
        whenever(userProfile.profileId).thenReturn("12345")
        whenever(userProfileManager.getActive()).thenReturn(userProfile)

        CloudSessionState.shared.appName = "eProAir"
        CloudSessionState.shared.appVersionNumber = "1.0"
        val programData = ProgramData("Asthma Program", "P0001", "12345", mutableListOf())
        whenever(programDataQuery.getCarePrograms("12345")).thenReturn(listOf(programData))


        // act
        careProgramManager.continueConsent(programId)
        val programDataArgumentCaptor = argumentCaptor<ProgramData>()

        // assert
        verify(programDataQuery).update(programDataArgumentCaptor.capture(), eq(true))
        assertEquals(CloudSessionState.shared.appVersionNumber, programDataArgumentCaptor.firstValue.consentedApps!![0].appVersionNumber)
        assertEquals(CloudSessionState.shared.appName, programDataArgumentCaptor.firstValue.consentedApps!![0].appName)

        verify(userProfileManager).update(eq(userProfile), eq(true))
    }

    @Test
    fun testThatGetUserProgramListCompletedUpdatesTheProfileWithTheActiveProgramListAndSetsFlagToIndicateThatProgramListWasRetrieved() {
        // arrange
        val careProgramManager = CareProgramManagerImpl()
        val dhpCareProgramService: DHPCareProgramCloudService = mock()
        careProgramManager.careProgramService = dhpCareProgramService
        val userProfile: UserProfile = mock()
        whenever(userProfile.profileId).thenReturn("12345")
        whenever(userProfileManager.getActive()).thenReturn(userProfile)

        CloudSessionState.shared.appName = "eProAir"
        CloudSessionState.shared.appVersionNumber = "1.0"
        val programData1 = ProgramData("Asthma Program", "P0001", "12345", mutableListOf(), "I0001", true)
        val programData2 = ProgramData("Asthma Program 2", "P0002", "12345", mutableListOf(), "I0002", false)
        whenever(programDataQuery.getCarePrograms("12345")).thenReturn(listOf())


        // act
        careProgramManager.getUserProgramListCompleted(CareProgramErrorCode.NO_ERROR, listOf(), listOf(programData1, programData2))
        val programDataArgumentCaptor = argumentCaptor<ProgramData>()

        // assert
        verify(programDataQuery).insertOrUpdate(programDataArgumentCaptor.capture(), eq(true))
        assertEquals(programData1.programId, programDataArgumentCaptor.firstValue.programId)
        assertEquals(programData1.profileId, programDataArgumentCaptor.firstValue.profileId)
        assertEquals(programData1.invitationCode, programDataArgumentCaptor.firstValue.invitationCode)
        assertEquals(programData1.programName, programDataArgumentCaptor.firstValue.programName)
        assertEquals(true, careProgramManager.hasRetrievedInitialUserProgramList)
    }

    @Test
    fun testThatGetUserProgramListCompletedDoesNotSetFlagToIndicateThatProgramListWasRetrievedIfThereWereErrors() {
        // arrange
        val careProgramManager = CareProgramManagerImpl()
        val dhpCareProgramService: DHPCareProgramCloudService = mock()
        careProgramManager.careProgramService = dhpCareProgramService

        // act
        careProgramManager.getUserProgramListCompleted(CareProgramErrorCode.INVALID_PROFILE, listOf(), listOf())

        // assert
        assertEquals(false, careProgramManager.hasRetrievedInitialUserProgramList)
    }

    @Test
    fun testThatGetInvitationDetailsCompletedPostsAnInvitationDetailsMessageWithErrorInformationIfThereWereErrors() {
        // arrange
        val careProgramManager = CareProgramManagerImpl()
        val dhpCareProgramService: DHPCareProgramCloudService = mock()
        careProgramManager.careProgramService = dhpCareProgramService
        val invitationDetails: InvitationDetails = mock()

        // act
        careProgramManager.getInvitationDetailsCompleted(CareProgramErrorCode.GET_INVITATION_DETAILS_REQUEST_FAILED, listOf(CareProgramErrorDetail.UNKNOWN), invitationDetails)
        val invitationDetailsMessageArgumentCaptor = argumentCaptor<InvitationDetailsMessage>()

        // assert
        verify(messenger).post(invitationDetailsMessageArgumentCaptor.capture())
        assertEquals(CareProgramErrorCode.GET_INVITATION_DETAILS_REQUEST_FAILED, invitationDetailsMessageArgumentCaptor.firstValue.errorCode)
        assertEquals(1, invitationDetailsMessageArgumentCaptor.firstValue.errorDetails!!.size)
        assertEquals(CareProgramErrorDetail.UNKNOWN, invitationDetailsMessageArgumentCaptor.firstValue.errorDetails!![0])
        assertEquals(invitationDetails, invitationDetailsMessageArgumentCaptor.firstValue.invitationDetails)
    }

    @Test
    fun testThatGetInvitationDetailsCompletedPostsAnInvitationDetailsMessageWithAlreadyEnrolledErrorCodeIfUserHasAlreadyEnrolledInTheProgram() {
        // arrange
        val careProgramManager = CareProgramManagerImpl()
        val dhpCareProgramService: DHPCareProgramCloudService = mock()
        careProgramManager.careProgramService = dhpCareProgramService
        val invitationDetails: InvitationDetails = mock()
        whenever(invitationDetails.programId).thenReturn("P0001")
        val userProfile: UserProfile = mock()
        whenever(userProfile.profileId).thenReturn("12345")
        whenever(userProfileManager.getActive()).thenReturn(userProfile)
        whenever(programDataQuery.getCarePrograms("12345")).thenReturn(listOf(ProgramData("Asthma Program", "P0001", "12345")))

        // act
        careProgramManager.getInvitationDetailsCompleted(CareProgramErrorCode.NO_ERROR, listOf(), invitationDetails)
        val invitationDetailsMessageArgumentCaptor = argumentCaptor<InvitationDetailsMessage>()

        // assert
        verify(messenger).post(invitationDetailsMessageArgumentCaptor.capture())
        assertEquals(CareProgramErrorCode.ALREADY_ENROLLED_IN_PROGRAM, invitationDetailsMessageArgumentCaptor.firstValue.errorCode)
    }

    @Test
    fun testThatGetInvitationDetailsCompletedPostsAnInvitationDetailsMessageWithFilteredApps() {
        // arrange
        val careProgramManager = CareProgramManagerImpl()
        val dhpCareProgramService: DHPCareProgramCloudService = mock()
        careProgramManager.careProgramService = dhpCareProgramService
        val invitationDetails = InvitationDetails("P0001", "Asthma Program", "I0001", "",
                listOf(CloudAppData("eProAir", "1.0"), CloudAppData("CareTrx", "2.0")),
                listOf(CloudAppData("eProAir", "1.0"), CloudAppData("CareTrx", "2.0")))
        val userProfile: UserProfile = mock()
        whenever(userProfile.profileId).thenReturn("12345")
        whenever(userProfileManager.getActive()).thenReturn(userProfile)
        whenever(programDataQuery.getCarePrograms("12345")).thenReturn(listOf())
        whenever(userProfileManager.activeUserAppList).thenReturn(listOf(CloudAppData("eProAir", "1.0")))

        // act
        careProgramManager.getInvitationDetailsCompleted(CareProgramErrorCode.NO_ERROR, listOf(), invitationDetails)
        val invitationDetailsMessageArgumentCaptor = argumentCaptor<InvitationDetailsMessage>()

        // assert
        verify(messenger).post(invitationDetailsMessageArgumentCaptor.capture())
        assertEquals(1, invitationDetailsMessageArgumentCaptor.firstValue.invitationDetails.programSupportedUserApps.size)
        assertEquals("eProAir", invitationDetailsMessageArgumentCaptor.firstValue.invitationDetails.programSupportedUserApps[0].appName)
        assertEquals("1.0", invitationDetailsMessageArgumentCaptor.firstValue.invitationDetails.programSupportedUserApps[0].appVersionNumber)
    }

    @Test
    fun testThatAcceptInvitationCompletedSavesTheProgramAndUserProfileAndSendsAMessageForSyncing() {
        // arrange
        val careProgramManager = CareProgramManagerImpl()
        val dhpCareProgramService: DHPCareProgramCloudService = mock()
        careProgramManager.careProgramService = dhpCareProgramService
        val invitationDetails = InvitationDetails("P0001", "Asthma Program", "I0001", "",
                listOf(CloudAppData("eProAir", "1.0"), CloudAppData("CareTrx", "2.0")),
                listOf(CloudAppData("eProAir", "1.0"), CloudAppData("CareTrx", "2.0")))
        val userProfile: UserProfile = mock()
        whenever(userProfileManager.getActive()).thenReturn(userProfile)

        // act
        careProgramManager.acceptInvitationCompleted(CareProgramErrorCode.NO_ERROR, listOf(), invitationDetails)
        val programDataArgumentCaptor = argumentCaptor<ProgramData>()
        val messageArgumentCaptor = argumentCaptor<Any>()

        // assert
        verify(messenger, times(2)).post(messageArgumentCaptor.capture())
        verify(programDataQuery).insertOrUpdate(programDataArgumentCaptor.capture(), eq(true))
        verify(userProfileManager).update(eq(userProfile), eq(true))
        assertEquals(InvitationAcceptedMessage::class.java, messageArgumentCaptor.firstValue.javaClass)
        assertEquals(SyncCloudMessage::class.java, messageArgumentCaptor.secondValue.javaClass)

        val invitationAcceptedMessage = messageArgumentCaptor.firstValue as InvitationAcceptedMessage

        assertEquals(CareProgramErrorCode.NO_ERROR, invitationAcceptedMessage.errorCode)
        assertEquals(invitationDetails, invitationAcceptedMessage.invitationDetails)
        assertEquals(invitationDetails.programId, programDataArgumentCaptor.firstValue.programId)
        assertEquals(invitationDetails.programName, programDataArgumentCaptor.firstValue.programName)
        assertEquals(invitationDetails.invitationCode, programDataArgumentCaptor.firstValue.invitationCode)
    }

    @Test
    fun testThatAcceptInvitationCompletedDoesNotSaveTheProgramIfThereWereErrors() {
        // arrange
        val careProgramManager = CareProgramManagerImpl()
        val dhpCareProgramService: DHPCareProgramCloudService = mock()
        careProgramManager.careProgramService = dhpCareProgramService
        val invitationDetails: InvitationDetails = mock()
        val userProfile: UserProfile = mock()
        whenever(userProfileManager.getActive()).thenReturn(userProfile)

        // act
        careProgramManager.acceptInvitationCompleted(CareProgramErrorCode.INVALID_INVITATION_CODE, listOf(), invitationDetails)

        // assert
        verify(programDataQuery, never()).insertOrUpdate(any(), eq(true))
    }

    @Test
    fun testThatLeaveProgramCompletedDeletesTheProgramAndSavesTheUserProfileAndSendsAMessageForSyncing() {
        // arrange
        val careProgramManager = CareProgramManagerImpl()
        val dhpCareProgramService: DHPCareProgramCloudService = mock()
        careProgramManager.careProgramService = dhpCareProgramService
        val programId = "P0001"
        val userProfile: UserProfile = mock()
        whenever(userProfileManager.getActive()).thenReturn(userProfile)

        // act
        careProgramManager.leaveProgramCompleted(CareProgramErrorCode.NO_ERROR, listOf(), programId)
        val programDataArgumentCaptor = argumentCaptor<ProgramData>()
        val messageArgumentCaptor = argumentCaptor<Any>()

        // assert
        verify(messenger, times(2)).post(messageArgumentCaptor.capture())
        verify(programDataQuery).delete(programDataArgumentCaptor.capture())
        verify(userProfileManager).update(eq(userProfile), eq(true))
        assertEquals(LeaveProgramMessage::class.java, messageArgumentCaptor.firstValue.javaClass)
        assertEquals(SyncCloudMessage::class.java, messageArgumentCaptor.secondValue.javaClass)

        val leaveProgramMessage = messageArgumentCaptor.firstValue as LeaveProgramMessage

        assertEquals(CareProgramErrorCode.NO_ERROR, leaveProgramMessage.errorCode)
        assertEquals(programId, programDataArgumentCaptor.firstValue.programId)
    }

    @Test
    fun testThatLeaveProgramCompletedDoesNotDeleteTheProgramIfThereWereErrors() {
        // arrange
        val careProgramManager = CareProgramManagerImpl()
        val dhpCareProgramService: DHPCareProgramCloudService = mock()
        careProgramManager.careProgramService = dhpCareProgramService
        val programId = "P0001"
        val userProfile: UserProfile = mock()
        whenever(userProfileManager.getActive()).thenReturn(userProfile)

        // act
        careProgramManager.leaveProgramCompleted(CareProgramErrorCode.INVALID_INVITATION_CODE, listOf(), programId)

        // assert
        verify(programDataQuery, never()).delete(any())
    }
}