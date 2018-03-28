//
// UserProfileCloudServiceDelegate.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.services.userprofile

import com.teva.cloud.dataentities.UserProfile


/**
 * This interface defines callbacks to async calls intended to pass user profile information
 * from the service layer to the model layer.
 */
interface UserProfileCloudServiceDelegate {

    /**
     * Called upon completing the async call to get all profiles.
     * @param success: whether the call succeeded or not.
     * @param profiles: list of all the profiles stored in the DHP.
     */
    fun getAllProfilesCompleted(success: Boolean, profiles: List<UserProfile>)

    /**
     * Called upon completing the async call to create or setup a profile.
     * @param success: true if call succeeded, false otherwise.
     * @param profile: the profile.
     */
    fun setupProfileCompleted(success: Boolean, profile: UserProfile)

}