package com.teva.environment.entities

import com.teva.environment.enumerations.AirQuality
import com.teva.environment.enumerations.Pollutant

import org.threeten.bp.Instant

/**
 * This class provides air quality information from the air quality provider.
 *
 * @property providerName The provider's name.
 * @property airQualityIndex A value indicating current air quality.
 *                           Range is [0..500] Low value is good, high value is poor.
 * @property airQuality The AirQuality enum, mapped from the airQualityIndex
 * @property expirationDate The date/time that this data is declared as stale.
 *                          If not available, its value is null.
 * @property isValid A value indicating whether all of the property values are valid.
 * @property primaryPollutant The primary pollutant.
 */

class AirQualityInfo(
        var providerName: String?,
        var airQualityIndex: Int = 0,
        var airQuality: AirQuality? = null,
        var expirationDate: Instant? = null,
        var primaryPollutant: Pollutant? = null,
        var isValid: Boolean = false) {

    /**
     * Secondary constructor required for Java unit tests
     */
    constructor(providerName: String?) : this(providerName, 0, null, null, null, false)

    companion object {
        /**
         * This value is the minimum air quality index returned by the web service.
         */
        val minAirQualityIndex = 0

        /**
         * This value is the maximum air quality index returned by the web service.
         */
        val maxAirQualityIndex = 500
    }
}
