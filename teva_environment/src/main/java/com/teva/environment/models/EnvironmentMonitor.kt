///
// EnvironmentMonitor.kt
// teva_environment
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.environment.models

import com.teva.environment.entities.EnvironmentInfo

/**
 * This interface provides current environment data.
 */

interface EnvironmentMonitor {
    /**
     * This property returns the cached environment data.
     * This property returns null if the data is not available, due to either no internet connection, or failure to get current location (i.e., location services not available).
     * The current EnvironmentInfo could become null, if internet connection or location services are subsequently lost, and attempt to update data fails due to data expiration.
     * The individual environment properties, e.g., AirQualityInfo, PollenInfo, WeatherInfo could be null if the corresponding request fails (e.g., HTTP error)
     * or could be marked as invalid if there was a problem parsing the corresponding web service response.
     */
    val currentEnvironmentInfo: EnvironmentInfo?
}
