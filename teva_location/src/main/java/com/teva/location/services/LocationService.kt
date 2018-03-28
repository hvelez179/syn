//
// LocationService.kt
// teva_location
//
// Copyright (c) 2017 Teva. All rights reserved.
//
package com.teva.location.services


import android.app.Activity
import android.location.Location
import android.support.annotation.MainThread

import com.teva.common.services.ServiceControl

@MainThread
interface LocationService : ServiceControl {

    /**
     * Gets the current location coordinates, or null if the information cannot be obtained.
     */
    val currentLocation: Location?

    /**
     * Gets whether the location service is available.
     */
    val isAvailable: Boolean

    /**
     * Enables location services for the phone
     */
    fun enableLocationServices(activity: Activity)

    /**
     * The location service delegate.
     */
    var locationServiceDelegate: LocationServiceDelegate?

    /**
     * This method performs a location lookup on address, and returns the results in locationLookup callback.
     */
    fun locationLookup(address: String, locationLookupCallback: LocationCallback)

    /**
     * This method performs a reverse location lookup on latitude and longitude, and returns the results in reverseLocationLookup callback.
     */
    fun reverseLocationLookup(latitude: Double, longitude: Double, reverseLocationLookupCallback: LocationCallback)
}
