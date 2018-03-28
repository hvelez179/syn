package com.teva.environment.entities

import org.threeten.bp.Instant

/**
 * This class contains current weather conditions, and weather forecast information.
 *
 * @property airQualityInfo The air quality information that was provided by a web service.
 *                          If the web service is not available, this value is null.
 * @property pollenInfo pollen information that was provided by a web service.
 *                      If the web service is not available, this value is null.
 * @property weatherInfo The weather information that was provided by a web service.
 *                       If the web service is not available, this value is null.
 * @property lastEnvironmentUpdateTime The date and time that the environment conditions were last
 *                                     obtained from the environment provider(s).
 * @property expirationDate The date/time when the data is stale. It is the soonest date/time of any
 *                          of the individual environment data.
 */

class EnvironmentInfo(var airQualityInfo: AirQualityInfo? = null,
                      var pollenInfo: PollenInfo? = null,
                      var weatherInfo: WeatherInfo? = null,
                      var lastEnvironmentUpdateTime: Instant? = null,
                      var expirationDate: Instant? = null)
