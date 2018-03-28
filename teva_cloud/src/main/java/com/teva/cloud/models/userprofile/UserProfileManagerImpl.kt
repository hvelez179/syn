//
// UserProfileManagerImpl.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.models.userprofile

import com.teva.cloud.dataentities.CloudAppData
import com.teva.cloud.dataentities.UserProfile
import com.teva.cloud.dataquery.UserProfileQuery
import com.teva.cloud.enumerations.UserProfileStatusCode
import com.teva.cloud.messages.UserAppListMessage
import com.teva.cloud.messages.UserProfileMessage
import com.teva.cloud.models.CloudSessionState
import com.teva.cloud.services.userprofile.DHPUserProfileCloudService
import com.teva.cloud.services.userprofile.UserProfileCloudService
import com.teva.cloud.services.userprofile.UserProfileCloudServiceDelegate
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.DEBUG
import com.teva.utilities.utilities.Logger.Level.INFO
import com.teva.common.utilities.Messenger
import com.teva.common.utilities.toLocalDate
import org.threeten.bp.LocalDate


/**
 * This class implements the UserProfileManager
 */
class UserProfileManagerImpl : UserProfileManager , UserProfileCloudServiceDelegate {

    private var _activeUserAppList: List<CloudAppData>? = null
    /**
     * This property indicates whether the ActiveUserAppList needs to be refreshed.
     */
    override var shouldRefreshActiveUserAppList: Boolean = true

    /**
     * This property indicates includes the list of Apps used by the current profile. If nil, getUserAppListAsync needs to be called.
     */
    override val activeUserAppList: List<CloudAppData>?
        get() = _activeUserAppList

    /**
     * This property indicates whether the active user's applist is currently being retrieved.
     */
    private var isGettingActiveUserAppList: Boolean = false

    private val logger = Logger("UserProfileManager")

    private val timeService: TimeService = DependencyProvider.default.resolve()

    /**
     * This property stores the UserProfileQuery dependency.
     */
    private lateinit var userProfileQuery: UserProfileQuery

    internal var userProfileService: UserProfileCloudService = DHPUserProfileCloudService()

    //private var cloudManager = DependencyProvider.default.resolve<CloudManager>()
    private var messenger = DependencyProvider.default.resolve<Messenger>()

    private var lastCheckedEmancipationDate: LocalDate? = null

    init {
        userProfileService.didGetAllProfiles = this::getAllProfilesCompleted
        userProfileService.didSetProfile = this::setupProfileCompleted
        userProfileService.didGetUserAppList = this::getUserAppListCompleted
    }

    /**
     * This method sets the UserProfileQuery dependency. It is set directly rather than registered with the DependencyProvider to prevent access by other objects.
     */
    internal fun setQuery(query: UserProfileQuery) {
        userProfileQuery = query
    }

    // Methods that don't go to cloud

    /**
     * Returns the account owner user profile.
     */
    override fun getAccountOwner(): UserProfile? {
        return userProfileQuery.getAccountOwner()
    }

    /**
     * Returns the active user profile.
     */
    override fun getActive(): UserProfile? {
        return userProfileQuery.getActive()
    }

    // Async Methods

    /**
     * This method gets all the UserProfiles stored in the DHP. This call is async.
     */
    override fun getAllProfilesAsync() {
        userProfileService.getAllProfilesAsync()
    }

    /**
     * Updates a user profile object in the database.
     * @param profile: the user profile to update.
     */
    override fun update(profile: UserProfile, changed: Boolean) {
        userProfileQuery.insertOrUpdate(profile, changed)
        if (!changed) {
            userProfileQuery.resetChangedFlag(profile, changed)
        }
    }

    /**
     * Returns a list of the profiles that have changed and need to be updated in the DHP.
     * @return: an array of profiles.
     */
    override fun getAllChangedProfiles() : List<UserProfile> {
        return userProfileQuery.getAllChanged()
    }



    /**
     * Sets up or creates a user profile and makes it active. This call is async. If the profile is already set up in the cloud, it will callback synchronously.
     * @param profile: the user profile to setup and set as active.
     */
    override fun setupActiveProfileAsync(profile: UserProfile) {
        userProfileService.setupProfileAsync(profile)
    }

    // Completion Callbacks

    /**
     * Called upon completing the async call to get all profiles. This method merges profiles created on other devices.
     * @param success: whether the call succeeded or not.
     * @param profiles: list of all the profiles stored in the DHP.
     */
    override fun getAllProfilesCompleted(success: Boolean, profiles: List<UserProfile>) {

        if (success) {

            val localProfiles = userProfileQuery.getAll().toMutableList()

            // If initial setup is not complete yet, clear the existing profiles, in case we killed the app and logged in with another account.
            if (getActive() == null) {
                localProfiles.clear()
            }

            // Merge any new profiles created on other devices into the local database.
            for (profile in profiles) {
                if (localProfiles.firstOrNull { it.profileId == profile.profileId } == null) {
                    userProfileQuery.insert(profile, false)
                    localProfiles.add(profile)
                }
            }

            messenger.post(UserProfileMessage(UserProfileStatusCode.DID_GET_ALL_PROFILES, localProfiles))

        } else {
            messenger.post(UserProfileMessage(UserProfileStatusCode.ERROR_DURING_GET_ALL_PROFILES))
        }
    }

    /**
     * Called upon completing the async call to create or setup a profile.
     * @param success: true if call succeeded, false otherwise.
     * @param profile: the profile.
     */
    override fun setupProfileCompleted(success: Boolean, profile: UserProfile) {
        if (success) {

            userProfileQuery.getActive()?.let { activeProfile ->
                activeProfile.isActive = false
                userProfileQuery.insertOrUpdate(activeProfile, false)
            }

            profile.isActive = true
            userProfileQuery.insertOrUpdate(profile, false)

            messenger.post(UserProfileMessage(UserProfileStatusCode.DID_SETUP_ACTIVE_PROFILE, listOf(profile)))

        } else {

            // TODO SAT: we may not want to pass the profile up on failed calls when all the APIs become available
            messenger.post(UserProfileMessage(UserProfileStatusCode.ERROR_DURING_SETUP_ACTIVE_PROFILE, listOf(profile)))
        }
    }

    /**
     * This method starts to get a list of Apps used by the current Patient.
     */
    override fun refreshActiveUserAppListAsync() {

        logger.log(DEBUG, "refreshActiveUserAppListAsync()")

        val activeProfile = getActive()

        if (!isGettingActiveUserAppList && activeProfile != null) {
            isGettingActiveUserAppList = true
            _activeUserAppList = null
            userProfileService.getUserAppListAsync(activeProfile)
        }
    }

    /**
     * This method is called upon completion of the asynchronous request for Get Patient App List.
     * @param success: true if call succeeded, false otherwise.
     * @param userAppList: This parameter contains a list of Apps that the patient is using.
     */
    internal fun getUserAppListCompleted(success: Boolean, userAppList: List<CloudAppData>) {

        logger.log(INFO, "getUserAppListCompleted: UserAppList: $userAppList")

        _activeUserAppList = if(success) userAppList else null

        isGettingActiveUserAppList = false
        shouldRefreshActiveUserAppList = !success
        messenger.post(UserAppListMessage(success, userAppList))
    }

    /**
     * This method checks whether the active profile is a dependent who has turned 18.
     * It confirms using both the server time and the application time.
     * If server time confirms dependent is emancipated, permanently set a flag, so the emancipation screen can not be circumvented.
     * If application time confirms dependent is emancipated, change can be reversed by restarting the app. In this case it is assumed
     * the determination was made either through hypertime or by changing the mobile device clock.
     * @return Boolean
     */
    override fun isActiveProfileEmancipated(): Boolean {
        val activeProfile = getActive()
        if (activeProfile == null || (activeProfile.isAccountOwner ?: false)) {
            return false
        }
        if (activeProfile.isEmancipated ?: false) {
            return true
        }
        val serverTime = CloudSessionState.shared.serverTime?.toLocalDate()
        if (serverTime != null && isProfileEmancipated(serverTime, activeProfile)) {
            activeProfile.isEmancipated = true
            update(activeProfile, changed = false)
            return true
        }
        if (isProfileEmancipated(timeService.now().toLocalDate(), activeProfile)) {
            return true
        }
        return false
    }

    /**
     * This method checks whether a profile is emancipated, using a provided current date for reference.
     * @param currentDate: the date to use for reference
     * @param profile: the profile to check
     * @return Boolean
     */
    private fun isProfileEmancipated(currentDate: LocalDate, profile: UserProfile) : Boolean {
        val emancipationAge = 18L
        if (lastCheckedEmancipationDate != null){
            if (currentDate == lastCheckedEmancipationDate) {
                return false
            }
        }

        lastCheckedEmancipationDate = currentDate

        val profileDOB = profile.dateOfBirth
        if (profileDOB != null) {
            val emancipationDate = currentDate.plusYears(-emancipationAge)
            if (profileDOB <= emancipationDate) {
                return true
            }
        }
        return false
    }


}