///
// WeatherDotComProvider.kt
// teva_environment
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.environment.services

import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.*
import com.teva.utilities.utilities.rest.HttpStatusCode
import com.teva.utilities.utilities.rest.RestClient
import com.teva.utilities.utilities.rest.RestRequest
import com.teva.environment.entities.AirQualityInfo
import com.teva.environment.entities.PollenInfo
import com.teva.environment.entities.WeatherInfo
import com.teva.environment.enumerations.EnvironmentProviderCapabilities
import com.teva.environment.services.providers.EnvironmentProvider
import com.teva.environment.services.providers.EnvironmentProviderDelegate
import org.json.JSONException
import java.util.*

/**
 * This class provides Weather, AirQuality, and Pollen.  A single getData() call will provide separate Weather and AirQuality callbacks.
 */
class WeatherDotComProvider(private val dependencyProvider: DependencyProvider)
    : EnvironmentProvider {

    override var environmentProviderCapabilities = EnumSet.noneOf(EnvironmentProviderCapabilities::class.java)

    private var environmentProviderDelegate: EnvironmentProviderDelegate? = null

   /* private enum class WEATHER_REQUEST_TYPE {
        AIR_QUALITY_REQUEST,
        POLLEN_REQUEST,
        WEATHER_CONDITION_REQUEST,
        WEATHER_FORECAST_REQUEST
    }

    private val requestVsTypeMap = HashMap<RestRequest, WEATHER_REQUEST_TYPE>()*/

    private var weatherInfo: WeatherInfo? = null
    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()


    /**
     * This method returns the provider name.
     *
     * @return - the provider name.
     */
    override val providerName: String = "the Weather Company"

    /**
     * This property is the air quality attribution phrase, as mandated by the Weather Company.
     *
     * @return - the air quality attribution phrase.
     */
    private val airQualitySource: String = "EPA AirNow"

    /**
     * This method is the setter for the environment provider delegate.
     *
     * @param environmentProviderDelegate - the environment provider delegate.
     */
    override fun setEnvironmentProviderDelegate(environmentProviderDelegate: EnvironmentProviderDelegate) {
        this.environmentProviderDelegate = environmentProviderDelegate
    }

    /**
     * This method starts an asynchronous request to get data from the provider.
     * The provider returns the data via the EnvironmentProviderDelegate corresponding callback getDataComplete() method.
     *
     * @param latitude  - The latitude of the location for which data is to be retrieved
     * @param longitude - The longitude of the location for which data is to be retrieved
     */
    override fun getDataAsync(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude

        if (environmentProviderCapabilities!!.contains(EnvironmentProviderCapabilities.AIR_QUALITY)) {
            pullAirQualityAsync(latitude, longitude)
        }

        if (environmentProviderCapabilities!!.contains(EnvironmentProviderCapabilities.POLLEN)) {
            pullPollenAsync(latitude, longitude)
        }

        if (environmentProviderCapabilities!!.contains(EnvironmentProviderCapabilities.WEATHER)) {
            pullCurrentWeatherConditionsAsync(latitude, longitude)
        }
    }

    /**
     * This method requests air quality data from the web service, at the specified location.
     *
     * @param latitude  - This parameter is used to get air quality data for this latitude.
     * @param longitude - This parameter is used to get air quality data for this longitude.
     */
    private fun pullAirQualityAsync(latitude: Double, longitude: Double) {
        val airQualityUrlBuilder = StringBuilder("https://")
        airQualityUrlBuilder.append(apiAddress)
        airQualityUrlBuilder.append("/v1/geocode/")
        airQualityUrlBuilder.append(latitude)
        airQualityUrlBuilder.append("/")
        airQualityUrlBuilder.append(longitude)
        airQualityUrlBuilder.append("/airquality.json?language=")
        airQualityUrlBuilder.append(apiLanguage)
        airQualityUrlBuilder.append("&apiKey=")
        airQualityUrlBuilder.append(apiKey)

        val restClient = dependencyProvider.resolve<RestClient>()
        val request = restClient.request(airQualityUrlBuilder.toString())
                .method("GET")
                .build()
        //requestVsTypeMap.put(request, WEATHER_REQUEST_TYPE.AIR_QUALITY_REQUEST)
        restClient.execute(request, this::pullAirQualityCompleted)
    }

    /**
     * This method requests pollen data from the web service, at the specified location.
     *
     * @param latitude  - This parameter is used to get pollen data for this latitude.
     * @param longitude - This parameter is used to get pollen data for this longitude.
     */
    private fun pullPollenAsync(latitude: Double, longitude: Double) {

        val pollenUrlBuilder = StringBuilder("https://")
        pollenUrlBuilder.append(apiAddress)
        pollenUrlBuilder.append("/v2/indices/pollen/daypart/3day?geocode=")
        pollenUrlBuilder.append(latitude)
        pollenUrlBuilder.append(",")
        pollenUrlBuilder.append(longitude)
        pollenUrlBuilder.append("&language=")
        pollenUrlBuilder.append(apiLanguage)
        pollenUrlBuilder.append("&format=json&apiKey=")
        pollenUrlBuilder.append(apiKey)

        val restClient: RestClient = dependencyProvider.resolve()
        val request = restClient.request(pollenUrlBuilder.toString())
                .method("GET")
                .build()
        //requestVsTypeMap.put(request, WEATHER_REQUEST_TYPE.POLLEN_REQUEST)
        restClient.execute(request, this::pullPollenCompleted)
    }

    /**
     * This method requests weather condition data from the web service, at the specified location.
     *
     * @param latitude  - This parameter is used to get weather condition data for this latitude.
     * @param longitude - This parameter is used to get weather condition data for this longitude.
     */
    private fun pullCurrentWeatherConditionsAsync(latitude: Double, longitude: Double) {
        val weatherConditionUrlBuilder = StringBuilder("https://")
        weatherConditionUrlBuilder.append(apiAddress)
        weatherConditionUrlBuilder.append("/v1/geocode/")
        weatherConditionUrlBuilder.append(latitude)
        weatherConditionUrlBuilder.append("/")
        weatherConditionUrlBuilder.append(longitude)
        weatherConditionUrlBuilder.append("/observations/current.json?language=")
        weatherConditionUrlBuilder.append(apiLanguage)
        weatherConditionUrlBuilder.append("&units=")
        weatherConditionUrlBuilder.append(apiUnits)
        weatherConditionUrlBuilder.append("&apiKey=")
        weatherConditionUrlBuilder.append(apiKey)

        val restClient: RestClient = dependencyProvider.resolve()
        val request = restClient.request(weatherConditionUrlBuilder.toString())
                .method("GET")
                .build()
        //requestVsTypeMap.put(request, WEATHER_REQUEST_TYPE.WEATHER_CONDITION_REQUEST)
        restClient.execute(request, this::pullCurrentWeatherConditionCompleted)
    }

    /**
     * This method requests weather condition data from the web service, at the specified location.
     *
     * @param latitude  - This parameter is used to get weather condition data for this latitude.
     * @param longitude - This parameter is used to get weather condition data for this longitude.
     */
    private fun pullWeatherForecastAsync(latitude: Double, longitude: Double) {
        val weatherConditionUrlBuilder = StringBuilder("https://")
        weatherConditionUrlBuilder.append(apiAddress)
        weatherConditionUrlBuilder.append("/v1/geocode/")
        weatherConditionUrlBuilder.append(latitude)
        weatherConditionUrlBuilder.append("/")
        weatherConditionUrlBuilder.append(longitude)
        weatherConditionUrlBuilder.append("/forecast/hourly/48hour.json?language=")
        weatherConditionUrlBuilder.append(apiLanguage)
        weatherConditionUrlBuilder.append("&units=")
        weatherConditionUrlBuilder.append(apiUnits)
        weatherConditionUrlBuilder.append("&apiKey=")
        weatherConditionUrlBuilder.append(apiKey)

        val restClient: RestClient = dependencyProvider.resolve()
        val request = restClient.request(weatherConditionUrlBuilder.toString())
                .method("GET")
                .build()
        //requestVsTypeMap.put(request, WEATHER_REQUEST_TYPE.WEATHER_FORECAST_REQUEST)
        restClient.execute(request, this::pullWeatherForecastCompleted)
    }

    /**
     * This method parses the air quality information returned by the provider
     * and returns it to the registered delegate.
     *
     * @param statusCode - the status code indicating the status of the air quality request.
     * @param result     - the json returned by the provider.
     * @param exception  - any exception encountered while serving the request.
     */
    internal fun pullAirQualityCompleted(request: RestRequest, statusCode: Int, result: String?, exception: Exception?) {
        var airQualityInfo: AirQualityInfo? = null

        if (exception != null || statusCode != HttpStatusCode.OK.value || result == null) {
            logger.log(ERROR, "Failed to retrieve air quality info. Status code: " + Integer.toString(statusCode))
        } else {
            try {
                airQualityInfo = WeatherInformationDeserializer.deserializeAirQualityInfo(airQualitySource, result)
            } catch (jsonException: JSONException) {
                logger.log(ERROR, "Failed to extract air quality info. Exception: " + jsonException.message)
            }

        }

        // Notify EnvironmentService client.
        environmentProviderDelegate!!.getDataComplete(airQualityInfo)
    }

    /**
     * This method parses the pollen information returned by the provider
     * and returns it to the registered delegate.
     *
     * @param statusCode - the status code indicating the status of the pollen request.
     * @param result     - the json returned by the provider.
     * @param exception  - any exception encountered while serving the request.
     */
    internal fun pullPollenCompleted(request: RestRequest, statusCode: Int, result: String?, exception: Exception?) {
        var pollenInfo: PollenInfo? = null

        if (exception != null || statusCode != HttpStatusCode.OK.value || result == null) {
            logger.log(ERROR, "Failed to retrieve pollen info. Status code: " + Integer.toString(statusCode))
        } else {
            try {
                pollenInfo = WeatherInformationDeserializer.deserializePollenInfo(providerName, result)
            } catch (jsonException: JSONException) {
                logger.log(ERROR, "Failed to extract pollen info. Exception: " + jsonException.message)
            }

        }

        // Notify EnvironmentService client.
        environmentProviderDelegate!!.getDataComplete(pollenInfo)
    }

    /**
     * This method parses the weather information returned by the provider
     * and returns it to the registered delegate.
     *
     * @param statusCode - the status code indicating the status of the weather condition request.
     * @param result     - the json returned by the provider.
     * @param exception  - any exception encountered while serving the request.
     */
    internal fun pullCurrentWeatherConditionCompleted(request: RestRequest, statusCode: Int, result: String?, exception: Exception?) {
        weatherInfo = null

        if (exception != null || statusCode != HttpStatusCode.OK.value || result == null) {
            logger.log(ERROR, "Failed to retrieve weather info. Status code: " + Integer.toString(statusCode))
            // Notify EnvironmentService client.
            environmentProviderDelegate?.getDataComplete(weatherInfo as WeatherInfo?)
        } else {
            try {
                weatherInfo = WeatherInformationDeserializer.deserializeCurrentWeatherInfo(providerName, result)
            } catch (jsonException: JSONException) {
                logger.log(ERROR, "Failed to extract weather info. Exception: " + jsonException.message)
                environmentProviderDelegate?.getDataComplete(weatherInfo as WeatherInfo?)
                return
            }

            pullWeatherForecastAsync(latitude, longitude)
        }
    }

    /**
     * This method parses the weather forecast information returned by the provider
     * and returns it to the registered delegate.
     *
     * @param statusCode - the status code indicating the status of the weather condition request.
     * @param result     - the json returned by the provider.
     * @param exception  - any exception encountered while serving the request.
     */
    internal fun pullWeatherForecastCompleted(request: RestRequest, statusCode: Int, result: String?, exception: Exception?) {
        if (exception != null || statusCode != HttpStatusCode.OK.value || result == null) {
            logger.log(ERROR, "Failed to retrieve weather forecast info. Status code: " + Integer.toString(statusCode))
            // Notify EnvironmentService client.
            environmentProviderDelegate?.getDataComplete(weatherInfo)
        } else {
            try {
                val chanceOfPrecipitation = WeatherInformationDeserializer.deserializeWeatherForecastInfoForChanceOfPrecipitation(result)
                if (chanceOfPrecipitation != null) {
                    weatherInfo?.chanceOfPrecipitation = chanceOfPrecipitation
                }
            } catch (jsonException: JSONException) {
                logger.log(ERROR, "Failed to extract weather forecast info. Exception: " + jsonException.message)
            }

            environmentProviderDelegate?.getDataComplete(weatherInfo)
        }
    }

    companion object {

        private val logger = Logger(WeatherDotComProvider::class)

        var apiKey = "2689a07de77af60072518209dd0d7bfa"
        private val apiAddress = "api.weather.com"
        private val apiLanguage = "en-US"
        private val apiUnits = "e"
    }
}
