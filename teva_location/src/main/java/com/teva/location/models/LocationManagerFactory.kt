//
// LocationManagerFactory.kt
// teva_location
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.location.models

import com.teva.utilities.services.DependencyProvider
import com.teva.location.services.GoogleLocationClient
import com.teva.location.services.GoogleLocationClientImpl
import com.teva.location.services.LocationService
import com.teva.location.services.LocationServiceImpl

/**
 * This class creates a LocationManager
 */

object LocationManagerFactory {
    /**
     * This method creates an instance of the location manager.
     *
     * @return - the location manager.
     */
    private fun createLocationManagerImpl(): LocationManagerImpl {
        val dependencyProvider = DependencyProvider.default
        dependencyProvider.register(GoogleLocationClient::class, GoogleLocationClientImpl(dependencyProvider))
        dependencyProvider.register(LocationService::class, LocationServiceImpl(dependencyProvider))

        return LocationManagerImpl(dependencyProvider)
    }

    /**
     * This method returns the Location Manager.
     *
     * @return - the LocationManager.
     */
    val locationManager: LocationManager by lazy { createLocationManagerImpl() }
}
