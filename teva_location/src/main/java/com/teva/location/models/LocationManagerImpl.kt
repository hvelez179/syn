//
// LocationManagerImpl.kt
// teva_location
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.location.models

import android.app.Activity
import android.location.Location
import com.teva.utilities.services.DependencyProvider

import com.teva.location.services.LocationCallback
import com.teva.location.services.LocationService

/**
 * This class implements the LocationManager interface.
 */
class LocationManagerImpl(dependencyProvider: DependencyProvider) : LocationManager {

    private val locationService = dependencyProvider.resolve<LocationService>();

    /**
     * Gets the current location coordinates, or null if the information cannot be obtained.
     */
    override val currentLocation: Location?
        get() = locationService.currentLocation

    /**
     * Gets whether the location service is enabled.
     */
    override val isLocationServicesEnabled: Boolean
        get() = locationService.isAvailable

    /**
     * Enables location services for the phone
     */
    override fun enableLocationServices(activity: Activity) {
        locationService.enableLocationServices(activity)
    }

    /**
     * This method performs a location lookup on address, and returns the results in locationLookup callback.
     */
    override fun locationLookup(address: String, locationLookupCallback: LocationCallback) {
        locationService.locationLookup(address, locationLookupCallback)
    }

    /**
     * This method performs a reverse location lookup on latitude and longitude, and returns the results in reverseLocationLookup callback.
     */
    override fun reverseLocationLookup(latitude: Double, longitude: Double, reverseLocationLookupCallback: LocationCallback) {
        locationService.reverseLocationLookup(latitude, longitude, reverseLocationLookupCallback)
    }
}