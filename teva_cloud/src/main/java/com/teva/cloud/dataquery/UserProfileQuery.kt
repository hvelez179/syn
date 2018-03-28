//
// UserProfileQuery.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.dataquery

import com.teva.cloud.dataentities.UserProfile
import com.teva.common.dataquery.DataQueryForTrackedModels


/**
 * Classes that implement this interface support persistence of user profile information.
 */
interface UserProfileQuery : DataQueryForTrackedModels<UserProfile> {
    /**
     * Returns user profile information given a profileId.
     */
    fun getUserProfile(profileId: String): UserProfile?

    /**
     * Returns the account owner user profile, null if no account owner has been created.
     */
    fun getAccountOwner(): UserProfile?

    /**
     * Returns the active user profile, null if no user profile has been created.
     */
    fun getActive(): UserProfile?
}