///
// LocationSettings.kt
// teva_location
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.location.services

/**
 * Abstraction around the system location settings.
 * This is a service used by the LocationServiceImpl and is in a separate class so that it
 * can be mocked.
 */
interface LocationSettings {

    /**
     * Checks if the location setting is turned on.
     */
    val isLocationModeOn: Boolean
}
