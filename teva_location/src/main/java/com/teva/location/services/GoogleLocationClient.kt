///
// GoogleLocationClient.kt
// teva_location
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.location.services

import android.app.Activity
import android.location.Location

/**
 * Abstraction around the Google Services Location API
 */
interface  GoogleLocationClient {
    /**
     * The current location
     */
    val currentLocation: Location?

    /**
     * Connect to the google play services for using location services.
     */
    fun initialize()

    /**
     * Disconnect from the google play services
     */
    fun uninitialize()

    /**
     * Displays a dialog to enable location services through google play services
     */
    fun enableLocationServices(activity: Activity)
}

