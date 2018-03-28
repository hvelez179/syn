///
// EnvironmentProviderFactory.kt
// teva_environment
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.environment.services.providers

import com.teva.location.services.LocationInfo

/**
 * This interface defines the factory for returning environment providers.
 */

interface EnvironmentProviderFactory {

    /**
     * Returns the list of environment providers.
     *
     * @param locationInfo - the location for which environment providers are required.
     * @return - the list of environment providers.
     */
    fun getProviders(locationInfo: LocationInfo): List<EnvironmentProvider>

    /**
     * Releases the environment providers.
     */
    fun releaseProviders()
}
