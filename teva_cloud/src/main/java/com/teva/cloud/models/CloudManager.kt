//
// CloudManager.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.models

/**
 * This interface is the public interface for Teva_Cloud.
 */
interface CloudManager {
    /**
     * This property indicates whether the Cloud onboarding process has been completed.
     */
    val isInitialSetupCompleted: Boolean

    /**
     * This property indicates whether the Cloud Service is currently logged in.
     */
    val isLoggedIn: Boolean

    /**
     * This property indicates whether the Cloud user is an emancipated minor.
     */
    val isEmancipated: Boolean

    /**
     * This method logs out of the Cloud.
     */
    fun logOut()
}