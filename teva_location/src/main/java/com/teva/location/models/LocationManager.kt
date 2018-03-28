//
// LocationManager.kt
// teva_location
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.location.models

import android.app.Activity
import android.location.Location
import com.teva.location.services.LocationCallback

/**
 * This interface provides location information.
 */

interface LocationManager {
    /**
     * Gets the current location information, or null if location information is not available.
     */
    val currentLocation: Location?

    /**
     * This method checks if the Location Services are enabled.
     */
    val isLocationServicesEnabled: Boolean

    /**
     * Enables location services for the phone
     */
    fun enableLocationServices(activity: Activity)

    /**
     * This method looks up the location details of the provided address.
     * The callback method specified is executed after the lookup is completed.
     *
     * @param address - the address for which the location is to be retrieved.
     * @param locationLookupCallback - the callback to be executed after the lookup is completed.
     */
    fun locationLookup(address: String, locationLookupCallback: LocationCallback)

    /**
     * This method looks up the address for the specified latitude and longitude.
     * The callback method specified is executed after the lookup is completed.
     *
     * @param latitude - the latitude of the location that needs to be looked up.
     * @param longitude - the longitude of the location that needs to be looked up.
     * @param reverseLocationLookupCallback - the callback to be executed after the lookup is completed.
     */
    fun reverseLocationLookup(latitude: Double, longitude: Double, reverseLocationLookupCallback: LocationCallback)
}
