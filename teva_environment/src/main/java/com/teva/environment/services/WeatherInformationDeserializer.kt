///
// WeatherInformationDeserializer.kt
// teva_environment
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.environment.services

import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.environment.entities.AirQualityInfo
import com.teva.environment.entities.PollenInfo
import com.teva.environment.entities.WeatherInfo
import com.teva.environment.enumerations.*
import org.json.JSONException
import org.json.JSONObject
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalTime

/**
 * This class defines utility methods for parsing environment information
 * retrieved from providers.
 */

internal object WeatherInformationDeserializer {

    private val logger = Logger(WeatherInformationDeserializer::class)

    /**
     * This method extracts the air quality information from the json returned by
     * the weather information provider.
     *
     * @param providerName - the provider name for the air quality information.
     * @param json         - the json string returned by the provider
     * @return - the AirQuality information extracted from the json.
     */
    @Throws(JSONException::class)
    @JvmStatic
    fun deserializeAirQualityInfo(providerName: String, json: String): AirQualityInfo {
        val airQualityInfo = AirQualityInfo(providerName)

        val jsonObject = JSONObject(json)
        val airQualityTypesArray = jsonObject.getJSONArray("airquality") ?: return airQualityInfo

        for (typeIndex in 0..airQualityTypesArray.length() - 1) {
            val airQualityType = airQualityTypesArray.getJSONObject(typeIndex)
            val dataType = airQualityType.getString("data_type")

            if (dataType == "O") {
                val validTimeString = airQualityType.getString("valid_time_lap")
                var expirationDate = computeExpirationTime(validTimeString)
                expirationDate = checkExpirationDate(expirationDate)
                airQualityInfo.expirationDate = expirationDate
                val pollutants = airQualityType.getJSONArray("pollutants") ?: return airQualityInfo

                val airQualityIndex: Int?
                var primaryPollutantString: String? = null

                for (loop1 in 0..pollutants.length() - 1) {
                    val pollutant = pollutants.getJSONObject(loop1)
                    val primaryPollutantIndicator = pollutant.getString("primary_pollutant_ind")

                    if (primaryPollutantIndicator == "Y") {
                        airQualityIndex = pollutant.getInt("air_quality_idx")
                        airQualityInfo.airQualityIndex = airQualityIndex
                        primaryPollutantString = pollutant.getString("pollutant")
                        break
                    }
                }

                airQualityInfo.airQuality = AirQuality.fromIndex(airQualityInfo.airQualityIndex)
                airQualityInfo.primaryPollutant = Pollutant.fromString(primaryPollutantString)
                airQualityInfo.isValid = true
                break
            }
        }

        return airQualityInfo
    }

    /**
     * This method extracts the pollen information from the json returned by
     * the weather information provider.
     * @param providerName - the provider name for the pollen information.
     * @param json         - the json string returned by the provider
     * @return - the pollen information extracted from the json.
     */
    @Throws(JSONException::class)
    @JvmStatic
    fun deserializePollenInfo(providerName: String, json: String): PollenInfo {
        val pollenInfo = PollenInfo(providerName)

        val jsonObject = JSONObject(json)
        val metaData = jsonObject.getJSONObject("metadata") ?: return pollenInfo

        val expirationTime = metaData.getLong("expireTimeGmt")
        var expirationDate = Instant.ofEpochSecond(expirationTime)
        expirationDate = checkExpirationDate(expirationDate)
        pollenInfo.expirationDate = expirationDate

        val pollenForecast = jsonObject.getJSONObject("pollenForecast12hour") ?: return pollenInfo

        val currentDayIndex = 0

        val grassPollenIndices = pollenForecast.getJSONArray("grassPollenIndex")
        val grassPollenLevelValue = grassPollenIndices.getInt(currentDayIndex)
        pollenInfo.grassPollenLevel = PollenLevel.fromInteger(grassPollenLevelValue)

        val treePollenIndices = pollenForecast.getJSONArray("treePollenIndex")
        val treePollenLevelValue = treePollenIndices.getInt(currentDayIndex)
        pollenInfo.treePollenLevel = PollenLevel.fromInteger(treePollenLevelValue)

        val weedPollenIndices = pollenForecast.getJSONArray("ragweedPollenIndex")
        val weedPollenLevelValue = weedPollenIndices.getInt(currentDayIndex)
        pollenInfo.weedPollenLevel = PollenLevel.fromInteger(weedPollenLevelValue)

        pollenInfo.isValid = true

        return pollenInfo
    }

    /**
     * This method extracts the pollen information from the json returned by
     * the weather information provider.
     *
     * @param providerName - the provider name for the pollen information.
     * @param json         - the json string returned by the provider
     * @return - the pollen information extracted from the json.
     */
    @Throws(JSONException::class)
    @JvmStatic
    fun deserializeCurrentWeatherInfo(providerName: String, json: String): WeatherInfo {
        val weatherInfo = WeatherInfo(providerName)

        val jsonObject = JSONObject(json)
        val metaData = jsonObject.getJSONObject("metadata") ?: return weatherInfo

        val expirationTime = metaData.getLong("expire_time_gmt")
        var expirationDate = Instant.ofEpochSecond(expirationTime)
        expirationDate = checkExpirationDate(expirationDate)
        weatherInfo.expirationDate = expirationDate

        val unitsString = metaData.getString("units")
        val unitOfMeasurement = UnitOfMeasurement.fromString(unitsString)
        weatherInfo.units = unitOfMeasurement

        val observation = jsonObject.getJSONObject("observation") ?: return weatherInfo

        val observationInterval = observation.getLong("obs_time")
        val observationTime = Instant.ofEpochSecond(observationInterval)
        weatherInfo.date = observationTime

        val weatherConditionValue = observation.getInt("icon_code")
        weatherInfo.weatherCondition = WeatherCondition.fromInteger(weatherConditionValue)

        weatherInfo.weatherConditionExtendedCode = observation.getInt("icon_extd")
        weatherInfo.windDirectionCardinal = WindDirectionCardinal.fromString(observation.getString("wdir_cardinal"))

        // D = Day,
        // N = Night,
        // X = missing (for extreme northern and southern hemisphere)
        weatherInfo.isDaytime = observation.getString("day_ind") == "D"

        var unitOfMeasureKey: String? = null
        when (weatherInfo.units) {
            UnitOfMeasurement.ENGLISH -> unitOfMeasureKey = "imperial"
            UnitOfMeasurement.METRIC -> unitOfMeasureKey = "metric"
        }

        // Unit of Measure Section
        // The following data corresponds to fields with a unit of measure.
        val unitsOfMeasureSection = observation.getJSONObject(unitOfMeasureKey)
        weatherInfo.feelsLikeTemperature = unitsOfMeasureSection.getInt("feels_like")
        weatherInfo.relativeHumidity = unitsOfMeasureSection.getInt("rh")

        // Temperature in degrees, as specified in API parameter, units.
        weatherInfo.temperature = unitsOfMeasureSection.getDouble("temp").toInt()

        // The maximum forecasted hourly wind speed.
        weatherInfo.windSpeed = unitsOfMeasureSection.getDouble("wspd").toInt()
        weatherInfo.isValid = true

        return weatherInfo
    }

    /**
     * This method extracts the pollen information from the json returned by
     * the weather information provider.
     *
     * @param json - the json string returned by the provider
     * @return - the pollen information extracted from the json.
     */
    @Throws(JSONException::class)
    @JvmStatic
    fun deserializeWeatherForecastInfoForChanceOfPrecipitation(json: String): Int? {
        val jsonObject = JSONObject(json)
        val forecasts = jsonObject.getJSONArray("forecasts") ?: return null

        val currentHourForecast = forecasts.getJSONObject(0)
        return currentHourForecast.getInt("pop")
    }

    /**
     * This method computes the expiration time from the valid time string
     * returned by the provider.
     *
     * @param validTimeString - the valid time string returned by the provider.
     * @return - the expiration time computed from the valid time string.
     */
    private fun computeExpirationTime(validTimeString: String): Instant {

        val SECONDS_PER_DAY = 86400

        val extractedTime = validTimeString.substring(0, 19)

        // correction and correctionAmount are for correcting the time zone offset from UTC.
        val correction = validTimeString.substring(19, 20)
        val correctionAmount = validTimeString.substring(20)

        var validTime = Instant.parse(extractedTime + "Z")
        val duration = Duration.between(LocalTime.MIN, LocalTime.parse(correctionAmount))

        /**
         * The valid_time_lap returned from weather.com is in the form
         * 2017-04-18T00:00:00-04:00. Convert the time to UTC time
         * for example 2017-04-18T04:00:00Z in this case
         * and create an Instant from it.
         * If the time is behind UTC time(-) add the time difference
         * and if it is ahead of UTC time(+) subtract the time difference
         * to get UTC time.
         */
        if (correction == "-") {
            validTime = validTime.plus(duration)
        } else {
            validTime = validTime.minus(duration)
        }

        return validTime.plusSeconds(SECONDS_PER_DAY.toLong())
    }

    /**
     * This method checks if the expiration date passed in is in the future.  If not, it is updated to be in the future.
     *
     * @param expirationDate - This parameter is the expiration date from the web service.
     * @return - Returns an updated expiration date if the web service expiration date is in the past.
     */
    private fun checkExpirationDate(expirationDate: Instant?): Instant {

        val DEFAULT_EXPIRATION_IN_SECONDS = 300
        val now = DependencyProvider.default.resolve<TimeService>().now()
        var updatedExpirationDate = expirationDate ?: now

        if (now.isAfter(updatedExpirationDate)) {
            logger.log(Logger.Level.DEBUG, "Expired environment data received.")
            updatedExpirationDate = now.plusSeconds(DEFAULT_EXPIRATION_IN_SECONDS.toLong())
        }

        return updatedExpirationDate
    }
}
