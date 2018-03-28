package com.teva.environment.entities

import com.teva.environment.enumerations.UnitOfMeasurement
import com.teva.environment.enumerations.WeatherCondition
import com.teva.environment.enumerations.WindDirectionCardinal

import org.threeten.bp.Instant

/**
 * This class provides weather information from the weather provider.
 * @property providerName - the provider name.
 * @property chanceOfPrecipitation contains the chance of precipitation, in percentage.
 * @property date The date/time that this data was retrieved. If not available, its value is null.
 * @property expirationDate The date/time that this data is declared as stale. If not available, its value is null.
 * @property feelsLikeTemperature The real-feel temperature in degrees specified by the units property.
 * @property isDaytime Indicates whether or not it is daytime (true if daytime, otherwise false).
 * @property isValid This property indicates whether all of the property values are valid.
 * @property locationFull The requested weather location. It is not necessarily the weather observation location.
 * @property relativeHumidity The relative humidity, as a percentage e.g., 65.
 * @property temperature The temperature in degrees specified by the units property.
 * @property units the units of measurement that is used in the environment provider call.
 * @property weatherCondition An enumeration representing weather description.
 * @property weatherConditionExtendedCode The weather condition extended code.
 *                                        It is used to specify a weather condition phrase.
 * @property windSpeed The wind speed in distance (specified by the units property) per hour.
 * @property windDirectionCardinal The wind direction cardinal string as returned by the service.
 */

class WeatherInfo(
        var providerName: String,
        var chanceOfPrecipitation: Int? = null,
        var date: Instant? = null,
        var expirationDate: Instant? = null,
        var feelsLikeTemperature: Int? = null,
        var isDaytime: Boolean? = null,
        var locationFull: String? = null,
        var relativeHumidity: Int? = null,
        var temperature: Int? = null,
        var units: UnitOfMeasurement? = null,
        var weatherCondition: WeatherCondition? = null,
        var weatherConditionExtendedCode: Int? = null,
        var windSpeed: Int? = null,
        var windDirectionCardinal: WindDirectionCardinal? = null,
        var isValid: Boolean? = false) {

    /**
     * Secondary constructor required for Java unit tests
     */
    constructor(providerName: String) : this(providerName, chanceOfPrecipitation = null)
}

