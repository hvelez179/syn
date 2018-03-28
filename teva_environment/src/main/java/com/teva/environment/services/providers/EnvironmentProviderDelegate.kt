///
// EnvironmentProviderDelegate.kt
// teva_environment
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.environment.services.providers

import com.teva.environment.entities.AirQualityInfo
import com.teva.environment.entities.PollenInfo
import com.teva.environment.entities.WeatherInfo

/**
 * This class is the callback delegate interface for the EnvironmentProvider.
 * It adds the ability to get air quality, pollen, and weather information.
 */

interface EnvironmentProviderDelegate {

    /**
     * This is the method called after air quality info data is retrieved.
     *
     * @param airQualityInfo - the air quality info retrieved.
     */
    fun getDataComplete(airQualityInfo: AirQualityInfo?)

    /**
     * This is the method called after pollen info data is retrieved.

     * @param pollenInfo - the pollen info retrieved.
     */
    fun getDataComplete(pollenInfo: PollenInfo?)

    /**
     * This is the method called after weather info data is retrieved.
     *
     * @param weatherInfo - the weather info retrieved.
     */
    fun getDataComplete(weatherInfo: WeatherInfo?)
}
