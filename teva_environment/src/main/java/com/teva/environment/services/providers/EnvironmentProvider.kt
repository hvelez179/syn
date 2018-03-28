///
// EnvironmentProvider.kt
// teva_environment
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.environment.services.providers

import com.teva.environment.enumerations.EnvironmentProviderCapabilities

import java.util.EnumSet

/**
 * This interface has to be implemented by the environment providers
 */

interface EnvironmentProvider {

    /**
     * The getter and setter methods for this Environment Provider's capabilities.
     */
    var environmentProviderCapabilities: EnumSet<EnvironmentProviderCapabilities>

    /**
     * This property is the Environment Provider's name.
     */
    val providerName: String

    /**
     * The setter method for the callback delegate to send environment data back to.
     */
    fun setEnvironmentProviderDelegate(environmentProviderDelegate: EnvironmentProviderDelegate)

    /**
     * This method starts an asynchronous request to get data from the provider.  The provider returns the data via the EnvironmentProviderDelegate corresponding callback getDataComplete() method.
     *
     * @param latitude  - The latitude of the location for which data is to be retrieved
     * @param longitude - The longitude of the location for which data is to be retrieved
     */
    fun getDataAsync(latitude: Double, longitude: Double)
}
