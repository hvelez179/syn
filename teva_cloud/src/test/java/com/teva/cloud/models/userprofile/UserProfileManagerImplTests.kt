//
// UserProfileManagerImplTests.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.models.userprofile

import com.nhaarman.mockito_kotlin.*
import com.teva.cloud.dataentities.UserProfile
import com.teva.cloud.dataquery.UserProfileQuery
import com.teva.cloud.enumerations.UserProfileStatusCode
import com.teva.cloud.messages.UserProfileMessage
import com.teva.cloud.services.userprofile.UserProfileCloudService
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.dhp.models.DHPManager
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test


/**
 * This class defines unit tests for the UerProfileManagerImpl class.
 */
class UserProfileManagerImplTests {
    private var dependencyProvider: DependencyProvider = DependencyProvider.default
    private val userProfileQuery: UserProfileQuery = mock()
    private val messenger: Messenger = mock()
    private val dhpManager: DHPManager = mock()
    private val timeService: TimeService = mock()

    @Before
    @Throws(Exception::class)
    fun setup() {
        dependencyProvider.register(Messenger::class, messenger)
        dependencyProvider.register(DHPManager::class, dhpManager)
        dependencyProvider.register(TimeService::class, timeService)
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        DependencyProvider.default.unregisterAll()
    }

    @Test
    fun testThatGetAccountOwnerRetrievesAccountOwnerUsingUserProfileQuery() {
        // arrange
        val userProfile: UserProfile = mock()
        whenever(userProfileQuery.getAccountOwner()).thenReturn(userProfile)
        val userProfileManager = UserProfileManagerImpl()
        userProfileManager.setQuery(userProfileQuery)

        // act
        val accountOwner = userProfileManager.getAccountOwner()

        // assert
        verify(userProfileQuery).getAccountOwner()
        assertEquals(accountOwner, userProfile)
    }

    @Test
    fun testThatGetActiveRetrievesActiveUserProfileUsingUserProfileQuery() {
        // arrange
        val userProfile: UserProfile = mock()
        whenever(userProfileQuery.getActive()).thenReturn(userProfile)
        val userProfileManager = UserProfileManagerImpl()
        userProfileManager.setQuery(userProfileQuery)

        // act
        val activeUser = userProfileManager.getActive()

        // assert
        verify(userProfileQuery).getActive()
        assertEquals(activeUser, userProfile)
    }

    @Test
    fun testThatUserProfileMessageWithCorrectStatusCodeIsPostedWhenGetAllProfileAsyncFails() {
        // arrange
        val userProfileManager = UserProfileManagerImpl()
        val userProfileMessageArgumentCaptor = argumentCaptor<UserProfileMessage>()

        // act
        userProfileManager.getAllProfilesCompleted(false, ArrayList())

        // assert
        verify(messenger).post(userProfileMessageArgumentCaptor.capture())
        assertEquals(UserProfileStatusCode.ERROR_DURING_GET_ALL_PROFILES, userProfileMessageArgumentCaptor.firstValue.messageCode)
    }

    @Test
    fun testThatUserProfileMessageWithCorrectStatusCodeAndUserProfilesIsPostedWhenGetAllProfileAsyncSucceeds() {
        // arrange
        val userProfileManager = UserProfileManagerImpl()
        whenever(userProfileQuery.getAll()).thenReturn(ArrayList())
        userProfileManager.setQuery(userProfileQuery)
        val userProfileMessageArgumentCaptor = argumentCaptor<UserProfileMessage>()
        val userProfile: UserProfile = mock()
        val profileList = listOf(userProfile)

        // act
        userProfileManager.getAllProfilesCompleted(true, profileList)

        // assert
        verify(messenger).post(userProfileMessageArgumentCaptor.capture())
        assertEquals(UserProfileStatusCode.DID_GET_ALL_PROFILES, userProfileMessageArgumentCaptor.firstValue.messageCode)
        assertEquals(1, userProfileMessageArgumentCaptor.firstValue.profileData.size)
        assertEquals(userProfile, userProfileMessageArgumentCaptor.firstValue.profileData.first())
    }

    @Test
    fun testThatNewProfilesAreInsertedIntoTheDatabaseWhenGetAllProfileAsyncSucceeds() {
        // arrange
        val userProfileManager = UserProfileManagerImpl()
        val existingProfile: UserProfile = mock()
        whenever(existingProfile.profileId).thenReturn("P001")
        whenever(userProfileQuery.getAll()).thenReturn(listOf(existingProfile))
        whenever(userProfileQuery.getActive()).thenReturn(existingProfile)
        userProfileManager.setQuery(userProfileQuery)
        val userProfileArgumentCaptor = argumentCaptor<UserProfile>()
        val newUserProfile: UserProfile = mock()
        whenever(existingProfile.profileId).thenReturn("P002")
        val profileList = listOf(existingProfile, newUserProfile)

        // act
        userProfileManager.getAllProfilesCompleted(true, profileList)

        // assert
        verify(userProfileQuery).insert(userProfileArgumentCaptor.capture(), eq(false))
        assertEquals(newUserProfile, userProfileArgumentCaptor.firstValue)
    }

    @Test
    fun testThatUserProfileMessageWithCorrectStatusCodeIsPostedWhenSetupActiveProfileAsyncFails() {
        // arrange
        val userProfileManager = UserProfileManagerImpl()
        val userProfileMessageArgumentCaptor = argumentCaptor<UserProfileMessage>()
        val userProfile: UserProfile = mock()

        // act
        userProfileManager.setupProfileCompleted(false, userProfile)

        // assert
        verify(messenger).post(userProfileMessageArgumentCaptor.capture())
        assertEquals(UserProfileStatusCode.ERROR_DURING_SETUP_ACTIVE_PROFILE, userProfileMessageArgumentCaptor.firstValue.messageCode)
    }

    @Test
    fun testThatCurrentlyActiveProfileIsMarkedInactiveAndNewProfileMarkedActiveWhenSetupActiveProfileAsyncSucceeds() {
        // arrange
        val userProfileManager = UserProfileManagerImpl()
        val userProfileArgumentCaptor = argumentCaptor<UserProfile>()
        val newProfile: UserProfile = mock()
        val activeProfile: UserProfile = mock()
        whenever(userProfileQuery.getActive()).thenReturn(activeProfile)
        userProfileManager.setQuery(userProfileQuery)

        // act
        userProfileManager.setupProfileCompleted(true, newProfile)

        // assert
        verify(userProfileQuery, times(2)).insertOrUpdate(userProfileArgumentCaptor.capture(), eq(false))
        verify(activeProfile).isActive = false
        assertEquals(activeProfile, userProfileArgumentCaptor.firstValue)
        verify(newProfile).isActive = true
        assertEquals(newProfile, userProfileArgumentCaptor.secondValue)
    }

    @Test
    fun testThatGetAllProfilesAsyncInvokesTheCorrespondingMethodOnTheUserProfileCloudService() {
        // arrange
        val userProfileManager = UserProfileManagerImpl()
        val userProfileService: UserProfileCloudService = mock()
        userProfileManager.userProfileService = userProfileService

        // act
        userProfileManager.getAllProfilesAsync()

        // assert
        verify(userProfileService).getAllProfilesAsync()
    }

    @Test
    fun testThatSetupActiveProfilesAsyncInvokesTheCorrespondingMethodOnTheUserProfileCloudServiceWithCorrectParameters() {
        // arrange
        val userProfileManager = UserProfileManagerImpl()
        val userProfileService: UserProfileCloudService = mock()
        userProfileManager.userProfileService = userProfileService
        val userProfile: UserProfile = mock()
        val userProfileArgumentCaptor = argumentCaptor<UserProfile>()

        // act
        userProfileManager.setupActiveProfileAsync(userProfile)

        // assert
        verify(userProfileService).setupProfileAsync(userProfileArgumentCaptor.capture())
        assertEquals(userProfile, userProfileArgumentCaptor.firstValue)
    }

    @Test
    fun testThatGetAllChangedProfilesReturnsTheChangedProfilesFromTheUserProfileQuery() {
        // arrange
        val userProfileManager = UserProfileManagerImpl()
        val userProfileService: UserProfileCloudService = mock()
        userProfileManager.userProfileService = userProfileService
        userProfileManager.setQuery(userProfileQuery)
        val userProfile: UserProfile = mock()
        val mockChangedProfiles = listOf(userProfile)
        whenever(userProfileQuery.getAllChanged()).thenReturn(mockChangedProfiles)

        // act
        val changedProfiles = userProfileManager.getAllChangedProfiles()

        // assert
        verify(userProfileQuery).getAllChanged()
        assertEquals(mockChangedProfiles.size, changedProfiles.size)

        for(i in 0 until mockChangedProfiles.size) {
            assertEquals(mockChangedProfiles[i], changedProfiles[i])
        }
    }

    @Test
    fun testThatUpdateUpdatesTheProfileInTheDatabase() {
        // arrange
        val userProfileManager = UserProfileManagerImpl()
        val userProfileService: UserProfileCloudService = mock()
        userProfileManager.userProfileService = userProfileService
        userProfileManager.setQuery(userProfileQuery)
        val userProfile: UserProfile = mock()

        // act
        userProfileManager.update(userProfile, false)

        // assert
        verify(userProfileQuery).insertOrUpdate(userProfile, false)
        verify(userProfileQuery).resetChangedFlag(userProfile, false)
    }
}