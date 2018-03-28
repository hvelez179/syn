//
// UserProfileCloudService.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.services.userprofile

import com.teva.cloud.dataentities.CloudAppData
import com.teva.cloud.dataentities.UserProfile


/**
 * This interface defines methods used to store and retrieve user profile information from the DHP.
 */
interface UserProfileCloudService {

    var didGetAllProfiles: ((success: Boolean, userProfiles: List<UserProfile>) -> Unit)?

    var didSetProfile: ((success: Boolean, userProfile: UserProfile) -> Unit)?

    var didGetUserAppList: ((success: Boolean, userAppList: List<CloudAppData>) -> Unit)?

    /**
     * Makes the call to get all the UserProfiles stored in the DHP. This is an async call.
     */
    fun getAllProfilesAsync()

    /**
     * Creates or sets up a user profile for cloud syncing from this device.. This call is async. If the profile is already set up in the cloud, it will callback synchronously.
     * @param profile: the user profile to create or setup.
     */
    fun setupProfileAsync(profile: UserProfile)

    /**
     * This method creates a Get User App List API request, and executes or queues the request.
     * This API is used to get Mobile App list for a Patient.
     */
    fun getUserAppListAsync(profile: UserProfile)
}