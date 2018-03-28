//
// UserProfileManager.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.models.userprofile

import com.teva.cloud.dataentities.CloudAppData
import com.teva.cloud.dataentities.UserProfile


/**
 * This interface defines methods used to store and retrieve user profile information from the DHP.
 */
interface UserProfileManager {
    /**
     * This property indicates whether the ActiveUserAppList needs to be refreshed.
     */
    var shouldRefreshActiveUserAppList: Boolean

    /**
     * This property indicates includes the list of Apps used by the current profile. If nil, getUserAppListAsync needs to be called.
     */
    val activeUserAppList: List<CloudAppData>?

    /**
     * Gets all the UserProfiles stored in the DHP. This call is async.
     */
    fun getAllProfilesAsync()

    /**
     * Sets up or creates a user profile and makes it active. This call is async. If the profile is already set up in the cloud, it will callback synchronously.
     * @param profile: the user profile to setup and set as active.
     */
    fun setupActiveProfileAsync(profile: UserProfile)

    /**
     * Updates a user profile object in the database.
     * @param profile: the user profile to update.
     */
    fun update(profile: UserProfile, changed: Boolean)

    /**
     * Returns a list of the profiles that have changed and need to be updated in the DHP.
     * @return: an array of profiles.
     */
    fun getAllChangedProfiles() : List<UserProfile>

    /**
     * Returns the account owner profile, if any.
     */
    fun getAccountOwner(): UserProfile?

    /**
     * Returns the active profile, if any.
     */
    fun getActive(): UserProfile?

    /**
     * This method starts to get a list of Apps used by the current profile.
     */
    fun refreshActiveUserAppListAsync()

    /**
     * This method checks whether the active profile is a dependent who has turned 18.
     * @return Boolean
     */
    fun isActiveProfileEmancipated(): Boolean
}