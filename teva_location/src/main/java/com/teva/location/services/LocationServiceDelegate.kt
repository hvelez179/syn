//
// LocationServiceListener.kt
// teva_location
//
// Copyright (c) 2017 Teva. All rights reserved.
//
package com.teva.location.services

/**
 * This interface defines the callbacks for entering region, exiting region and when current location is updated.
 */
interface LocationServiceDelegate {

    /**
     * This function is invoked when the region described by identifier has been exited.
     */
    fun exitedRegion(identifier: String)

    /**
     * This function is invoked when the region described by identifier has been entered.
     */
    fun enteredRegion(identifier: String)

    /**
     * This function is invoked when the current location is updated.
     * The new latitude and longitude of the current location are passed in.
     */
    fun currentLocationUpdated(latitude: Double, longitude: Double)
}
