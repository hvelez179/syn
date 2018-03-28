//
// LocationCallback.kt
// teva_location
//
// Copyright (c) 2017 Teva. All rights reserved.
//
package com.teva.location.services

/**
 * This interface defines callback method to be invoked when location lookup is completed.
 */
interface LocationCallback {
    /**
     * The callback method invoked when location lookup is completed.
     * @param locationInfo - the location information resulting from the lookup.
     */
    fun locationLookupCompleted(locationInfo: LocationInfo?)
}
