///
// EnvironmentProviderFactoryImpl.kt
// teva_environment
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.environment.services.providers

import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.*
import com.teva.environment.enumerations.EnvironmentProviderCapabilities
import com.teva.environment.services.WeatherDotComProvider
import com.teva.location.services.LocationInfo
import java.util.*

/**
 * This class implements the EnvironmentProviderFactory interface for returning EnvironmentProviders
 *
 * @param dependencyProvider - the dependency injection container.
 */

class EnvironmentProviderFactoryImpl(private val dependencyProvider: DependencyProvider)
    : EnvironmentProviderFactory {

    private val environmentProviders = ArrayList<EnvironmentProvider>()

    /**
     * Returns the list of environment providers.
     *
     * @param locationInfo - the location for which environment providers are required.
     * @return - the list of environment providers.
     */
    override fun getProviders(locationInfo: LocationInfo): List<EnvironmentProvider> {

        logger.log(ERROR, locationInfo.country)

        if (locationInfo.country != "United States") {
            logger.log(WARN, String.format("%s may not be supported.  Trying anyway...", locationInfo.country))
        }

        // Release previously created providers.
        releaseProviders()

        // US Providers
        // Let provider know what capabilities it is expected to provide.
        val capabilities = EnumSet.noneOf(EnvironmentProviderCapabilities::class.java)
        capabilities.add(EnvironmentProviderCapabilities.AIR_QUALITY)
        capabilities.add(EnvironmentProviderCapabilities.POLLEN)
        capabilities.add(EnvironmentProviderCapabilities.WEATHER)

        val weatherAndAirQualityProvider = WeatherDotComProvider(dependencyProvider)
        weatherAndAirQualityProvider.environmentProviderCapabilities = capabilities

        environmentProviders.add(weatherAndAirQualityProvider)

        return environmentProviders
    }

    /**
     * Releases the environment providers.
     */
    override fun releaseProviders() {
        environmentProviders.clear()
    }

    companion object {
        private val logger = Logger(EnvironmentProviderFactoryImpl::class)
    }
}
