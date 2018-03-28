///
// EnvironmentServiceDelegate.kt
// teva_environment
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.environment.services

import com.teva.environment.entities.EnvironmentInfo

/**
 * This interface provides the callback for returning requested environment data.
 */

interface EnvironmentServiceDelegate {
    /**
     * This method is called by the EnvironmentService to provide the delegate with the requested environment data.
     *
     * @param environmentInfo - This parameter contains the requested environment information. If the request fails, the value is null.
     */
    fun getEnvironmentComplete(environmentInfo: EnvironmentInfo?)
}
