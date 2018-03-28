///
// EnvironmentService.kt
// teva_environment
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.environment.services

import com.teva.location.services.LocationInfo

/**
 * This interface provides the ability to asynchronously get environment data.
 */
interface EnvironmentService {
    /**
     * Sets the environment service delegate used for the callback.
     */
    fun setEnvironmentServiceDelegate(environmentServiceDelegate: EnvironmentServiceDelegate)

    /**
     * This method starts a request to get environment data.
     *
     * @param locationInfo - This parameter contains the the requested environment location.
     */
    fun getEnvironmentAsync(locationInfo: LocationInfo)
}
