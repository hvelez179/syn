//
// EnvironmentProviderTests.kt
// teva_environment
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.environment.services

import android.util.Log
import com.nhaarman.mockito_kotlin.*
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.rest.*
import com.teva.environment.entities.AirQualityInfo
import com.teva.environment.entities.PollenInfo
import com.teva.environment.entities.WeatherInfo
import com.teva.environment.enumerations.EnvironmentProviderCapabilities
import com.teva.environment.services.providers.EnvironmentProviderDelegate
import org.json.JSONException
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito.mockStatic
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.net.HttpRetryException
import java.util.*

/**
 * This class defines unit tests for the WeatherDotComProvider class.
 */
@RunWith(PowerMockRunner::class)
@PrepareForTest(WeatherDotComProvider::class, Log::class, WeatherInformationDeserializer::class)
class WeatherDotComProviderTests {
    private val latitude = 40.069308
    private val longitude = -75.556552

    private val dependencyProvider: DependencyProvider = DependencyProvider.default
    private val restClient: RestClient = mock()

    private val restRequest: RestRequest = mock()
    private val airQualityRequest: RestRequest = mock()
    private val pollenRequest: RestRequest = mock()
    private val weatherRequest: RestRequest = mock()
    private val weatherForecastRequest: RestRequest = mock()
    private val requestBuilder: RequestBuilder = mock()
    private val airQualityRequestBuilder: RequestBuilder = mock()
    private val pollenRequestBuilder: RequestBuilder = mock()
    private val weatherRequestBuilder: RequestBuilder = mock()
    private val weatherForecastRequestBuilder: RequestBuilder = mock()
    private val apiKey = WeatherDotComProvider.apiKey

    @Before
    fun setup() {
        dependencyProvider.unregisterAll()


        whenever(restClient.request(any<String>())).thenAnswer { invocation ->
            val args = invocation.arguments
            val url = args[0] as String
            val builder: RequestBuilder
            if (url.contains("airquality")) {
                builder = airQualityRequestBuilder
            } else if (url.contains("pollen")) {
                builder = pollenRequestBuilder
            } else if (url.contains("observations")) {
                builder = weatherRequestBuilder
            } else if (url.contains("forecast")) {
                builder = weatherForecastRequestBuilder
            } else {
                builder = requestBuilder
            }

            builder
        }
        whenever(requestBuilder.build()).thenReturn(restRequest)
        whenever(airQualityRequestBuilder.build()).thenReturn(airQualityRequest)
        whenever(pollenRequestBuilder.build()).thenReturn(pollenRequest)
        whenever(weatherRequestBuilder.build()).thenReturn(weatherRequest)
        whenever(weatherForecastRequestBuilder.build()).thenReturn(weatherForecastRequest)

        whenever(requestBuilder.method(any())).thenReturn(requestBuilder)
        whenever(airQualityRequestBuilder.method(any())).thenReturn(airQualityRequestBuilder)
        whenever(pollenRequestBuilder.method(any())).thenReturn(pollenRequestBuilder)
        whenever(weatherRequestBuilder.method(any())).thenReturn(weatherRequestBuilder)
        whenever(weatherForecastRequestBuilder.method(any())).thenReturn(weatherForecastRequestBuilder)

        dependencyProvider.register(RestClient::class, restClient)
    }

    @Test
    fun testThatProviderReturnsItsCapabilitiesCorrectly() {
        // set the WeatherDotCom provider with all three capabilities.
        val weatherDotComProvider = WeatherDotComProvider(dependencyProvider)
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.allOf(EnvironmentProviderCapabilities::class.java)
        val providerCapabilities = weatherDotComProvider.environmentProviderCapabilities

        // verify that the provider has each of the set capabilities
        assertTrue(providerCapabilities.contains(EnvironmentProviderCapabilities.AIR_QUALITY))
        assertTrue(providerCapabilities.contains(EnvironmentProviderCapabilities.WEATHER))
        assertTrue(providerCapabilities.contains(EnvironmentProviderCapabilities.POLLEN))
    }

    @Test
    fun testThatProviderReturnsOnlyTheCapabilitiesThatWereSetAndNotAllTheCapabilities() {
        val weatherDotComProvider = WeatherDotComProvider(dependencyProvider)

        // set the WeatherDotCom provider with weather capability.
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.of(EnvironmentProviderCapabilities.WEATHER)
        var providerCapabilities = weatherDotComProvider.environmentProviderCapabilities

        // verify that the provider does not support air quality and pollen
        assertFalse(providerCapabilities.contains(EnvironmentProviderCapabilities.AIR_QUALITY))
        assertTrue(providerCapabilities.contains(EnvironmentProviderCapabilities.WEATHER))
        assertFalse(providerCapabilities.contains(EnvironmentProviderCapabilities.POLLEN))

        // set the WeatherDotCom provider with air quality capability.
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.of(EnvironmentProviderCapabilities.AIR_QUALITY)
        providerCapabilities = weatherDotComProvider.environmentProviderCapabilities

        // verify that the provider does not support pollen and weather.
        assertTrue(providerCapabilities.contains(EnvironmentProviderCapabilities.AIR_QUALITY))
        assertFalse(providerCapabilities.contains(EnvironmentProviderCapabilities.WEATHER))
        assertFalse(providerCapabilities.contains(EnvironmentProviderCapabilities.POLLEN))

        // set the WeatherDotCom provider with air quality capability.
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.of(EnvironmentProviderCapabilities.POLLEN)
        providerCapabilities = weatherDotComProvider.environmentProviderCapabilities

        // verify that the provider does not support pollen and weather.
        assertFalse(providerCapabilities.contains(EnvironmentProviderCapabilities.AIR_QUALITY))
        assertFalse(providerCapabilities.contains(EnvironmentProviderCapabilities.WEATHER))
        assertTrue(providerCapabilities.contains(EnvironmentProviderCapabilities.POLLEN))
    }

    @Test
    fun testGetDataAsyncPlacesASeparateRequestForEachCapability() {
        // set the WeatherDotCom provider with all  three capabilities.
        val weatherDotComProvider = WeatherDotComProvider(dependencyProvider)
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.allOf(EnvironmentProviderCapabilities::class.java)

        // Request for environment data
        weatherDotComProvider.getDataAsync(latitude, longitude)

        val restRequestArgumentCaptor = argumentCaptor<RestRequest>()

        //verify that three separate requests are sent to the rest client
        verify(restClient, times(3)).execute(restRequestArgumentCaptor.capture(), any())

        assertEquals(airQualityRequest, restRequestArgumentCaptor.allValues[0])
        assertEquals(pollenRequest, restRequestArgumentCaptor.allValues[1])
        assertEquals(weatherRequest, restRequestArgumentCaptor.allValues[2])
    }

    @Test
    fun testWeatherDotComProviderRequestsForTheCorrectUrlForAirQualityRequest() {
        val expectedAirQualityUrl = "https://api.weather.com/v1/geocode/" + java.lang.Double.toString(latitude) + "/" + java.lang.Double.toString(longitude) + "/airquality.json?language=en-US&apiKey=" + apiKey

        // set the WeatherDotCom provider with air quality capability.
        val weatherDotComProvider = WeatherDotComProvider(dependencyProvider)
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.of(EnvironmentProviderCapabilities.AIR_QUALITY)

        // Request for environment data
        weatherDotComProvider.getDataAsync(latitude, longitude)

        //verify that the correct url is sent as part of the request to the rest client
        val urlArgumentCaptor = argumentCaptor<String>()
        verify(restClient).request(urlArgumentCaptor.capture())
        assertEquals(expectedAirQualityUrl, urlArgumentCaptor.lastValue)
    }

    @Test
    fun testWeatherDotComProviderRequestsForTheCorrectUrlForPollenRequest() {
        val expectedPollenUrl = "https://api.weather.com/v2/indices/pollen/daypart/3day?geocode=" + java.lang.Double.toString(latitude) + "," + java.lang.Double.toString(longitude) + "&language=en-US&format=json&apiKey=" + apiKey

        // set the WeatherDotCom provider with pollen capability.
        val weatherDotComProvider = WeatherDotComProvider(dependencyProvider)
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.of(EnvironmentProviderCapabilities.POLLEN)

        // Request for environment data
        weatherDotComProvider.getDataAsync(latitude, longitude)

        //verify that the correct url is sent as part of the request to the rest client
        val urlArgumentCaptor = argumentCaptor<String>()
        verify(restClient).request(urlArgumentCaptor.capture())
        assertEquals(expectedPollenUrl, urlArgumentCaptor.lastValue)
    }

    @Test
    fun testWeatherDotComProviderRequestsForTheCorrectUrlForWeatherInfoRequest() {
        val expectedWeatherInfoUrl = "https://api.weather.com/v1/geocode/" + java.lang.Double.toString(latitude) + "/" + java.lang.Double.toString(longitude) + "/observations/current.json?language=en-US&units=e&apiKey=" + apiKey

        // set the WeatherDotCom provider with weather capability.
        val weatherDotComProvider = WeatherDotComProvider(dependencyProvider)
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.of(EnvironmentProviderCapabilities.WEATHER)

        // Request for environment data
        weatherDotComProvider.getDataAsync(latitude, longitude)

        //verify that the correct url is sent as part of the request to the rest client
        val urlArgumentCaptor = argumentCaptor<String>()
        verify(restClient).request(urlArgumentCaptor.capture())
        assertEquals(expectedWeatherInfoUrl, urlArgumentCaptor.lastValue)
    }

    @Test
    @Throws(JSONException::class)
    fun testWeatherDotComProviderRequestsForTheCorrectUrlForWeatherForecastRequest() {
        val expectedWeatherForecastUrl = "https://api.weather.com/v1/geocode/" + java.lang.Double.toString(latitude) + "/" + java.lang.Double.toString(longitude) + "/forecast/hourly/48hour.json?language=en-US&units=e&apiKey=" + apiKey

        val weatherInfo = WeatherInfo("providerName")
        mockStatic(WeatherInformationDeserializer::class.java)
        whenever(WeatherInformationDeserializer.deserializeCurrentWeatherInfo(any(), any())).thenReturn(weatherInfo)
        val responseJson = ""

        // set the WeatherDotCom provider with weather capability.
        val weatherDotComProvider = WeatherDotComProvider(dependencyProvider)
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.of(EnvironmentProviderCapabilities.WEATHER)

        // Request for environment data
        weatherDotComProvider.getDataAsync(latitude, longitude)

        // simulate receipt of weather information
        weatherDotComProvider.pullCurrentWeatherConditionCompleted(weatherRequest, HttpStatusCode.OK.value, responseJson, null)

        // verify that the weather forecast request is sent
        val urlArgumentCaptor = argumentCaptor<String>()
        verify(restClient, times(2)).request(urlArgumentCaptor.capture())

        // verify that the correct url is sent as part of the weather forecast request to the rest client
        assertEquals(expectedWeatherForecastUrl, urlArgumentCaptor.allValues[1])
    }

    @Test
    @Throws(JSONException::class)
    fun testWeatherDotComProviderProcessesSuccessfulAirQualityResponseCorrectlyAndReturnsTheResult() {
        val environmentProviderDelegate: EnvironmentProviderDelegate = mock()
        val airQualityInfo = AirQualityInfo("providerName")
        mockStatic(WeatherInformationDeserializer::class.java)
        whenever(WeatherInformationDeserializer.deserializeAirQualityInfo(any(), any())).thenReturn(airQualityInfo)
        val responseJson = ""

        // set the WeatherDotCom provider with air quality capability.
        val weatherDotComProvider = WeatherDotComProvider(dependencyProvider)
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.of(EnvironmentProviderCapabilities.AIR_QUALITY)
        weatherDotComProvider.setEnvironmentProviderDelegate(environmentProviderDelegate)

        // Request for environment data
        weatherDotComProvider.getDataAsync(latitude, longitude)

        // Simulate a response for the request
        weatherDotComProvider.pullAirQualityCompleted(airQualityRequest, HttpStatusCode.OK.value, responseJson, null)

        // verify that the air quality information is sent to the EnvironmentProviderDelegate
        val airQualityInfoArgumentCaptor = argumentCaptor<AirQualityInfo>()
        verify(environmentProviderDelegate).getDataComplete(airQualityInfoArgumentCaptor.capture())

        assertEquals(airQualityInfo, airQualityInfoArgumentCaptor.lastValue)
    }

    @Test
    @Throws(JSONException::class)
    fun testWeatherDotComProviderProcessesFailedAirQualityResponseCorrectlyAndReturnsNull() {
        val environmentProviderDelegate: EnvironmentProviderDelegate = mock()
        val responseJson = ""

        // set the WeatherDotCom provider with air quality capability.
        val weatherDotComProvider = WeatherDotComProvider(dependencyProvider)
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.of(EnvironmentProviderCapabilities.AIR_QUALITY)
        weatherDotComProvider.setEnvironmentProviderDelegate(environmentProviderDelegate)

        // Request for environment data
        weatherDotComProvider.getDataAsync(latitude, longitude)

        // Simulate an error response for the request
        weatherDotComProvider.pullAirQualityCompleted(airQualityRequest, HttpStatusCode.NOT_FOUND.value, responseJson, null)

        // verify that null is sent to the EnvironmentProviderDelegate
        val airQualityInfoArgumentCaptor = argumentCaptor<AirQualityInfo>()
        verify(environmentProviderDelegate).getDataComplete(airQualityInfoArgumentCaptor.capture())

        assertNull(airQualityInfoArgumentCaptor.lastValue)
    }

    @Test
    @Throws(JSONException::class)
    fun testWeatherDotComProviderProcessesExceptionInAirQualityResponseAndReturnsNull() {
        val environmentProviderDelegate: EnvironmentProviderDelegate = mock()
        val responseJson = ""

        // set the WeatherDotCom provider with air quality capability.
        val weatherDotComProvider = WeatherDotComProvider(dependencyProvider)
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.of(EnvironmentProviderCapabilities.AIR_QUALITY)
        weatherDotComProvider.setEnvironmentProviderDelegate(environmentProviderDelegate)

        // Request for environment data
        weatherDotComProvider.getDataAsync(latitude, longitude)

        // Simulate an exception in response for the request
        weatherDotComProvider.pullAirQualityCompleted(airQualityRequest, HttpStatusCode.OK.value, responseJson, HttpRetryException("", HttpStatusCode.NOT_FOUND.value))

        // verify that null is sent to the EnvironmentProviderDelegate
        val airQualityInfoArgumentCaptor = argumentCaptor<AirQualityInfo>()
        verify(environmentProviderDelegate).getDataComplete(airQualityInfoArgumentCaptor.capture())

        assertNull(airQualityInfoArgumentCaptor.lastValue)
    }

    @Test
    @Throws(JSONException::class)
    fun testWeatherDotComProviderReturnsNullIfAirQualityResponseCouldNotBeProcessed() {
        val environmentProviderDelegate: EnvironmentProviderDelegate = mock()
        mockStatic(WeatherInformationDeserializer::class.java)
        // simulate a JSON exception from the deserializer
        whenever(WeatherInformationDeserializer.deserializeAirQualityInfo(any(), any())).thenThrow(JSONException("Invalid JSON string"))
        val responseJson = ""

        // set the WeatherDotCom provider with air quality capability.
        val weatherDotComProvider = WeatherDotComProvider(dependencyProvider)
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.of(EnvironmentProviderCapabilities.AIR_QUALITY)
        weatherDotComProvider.setEnvironmentProviderDelegate(environmentProviderDelegate)

        // Request for environment data
        weatherDotComProvider.getDataAsync(latitude, longitude)

        // Simulate an response for the request
        weatherDotComProvider.pullAirQualityCompleted(airQualityRequest, HttpStatusCode.OK.value, responseJson, null)

        // verify that null is sent to the EnvironmentProviderDelegate
        val airQualityInfoArgumentCaptor = argumentCaptor<AirQualityInfo>()
        verify(environmentProviderDelegate).getDataComplete(airQualityInfoArgumentCaptor.capture())

        assertNull(airQualityInfoArgumentCaptor.lastValue)
    }

    @Test
    @Throws(JSONException::class)
    fun testWeatherDotComProviderProcessesSuccessfulPollenResponseCorrectlyAndReturnsTheResult() {
        val environmentProviderDelegate: EnvironmentProviderDelegate = mock()
        val pollenInfo = PollenInfo("providerName")
        mockStatic(WeatherInformationDeserializer::class.java)
        whenever(WeatherInformationDeserializer.deserializePollenInfo(any(), any())).thenReturn(pollenInfo)
        val responseJson = ""

        // set the WeatherDotCom provider with pollen capability.
        val weatherDotComProvider = WeatherDotComProvider(dependencyProvider)
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.of(EnvironmentProviderCapabilities.POLLEN)
        weatherDotComProvider.setEnvironmentProviderDelegate(environmentProviderDelegate)

        // Request for environment data
        weatherDotComProvider.getDataAsync(latitude, longitude)

        // Simulate a response for the request
        weatherDotComProvider.pullPollenCompleted(pollenRequest, HttpStatusCode.OK.value, responseJson, null)

        // verify that the pollen information is sent to the EnvironmentProviderDelegate
        val pollenInfoArgumentCaptor = argumentCaptor<PollenInfo>()
        verify(environmentProviderDelegate).getDataComplete(pollenInfoArgumentCaptor.capture())

        assertEquals(pollenInfo, pollenInfoArgumentCaptor.lastValue)
    }

    @Test
    @Throws(JSONException::class)
    fun testWeatherDotComProviderProcessesFailedPollenResponseCorrectlyAndReturnsNull() {
        val environmentProviderDelegate: EnvironmentProviderDelegate = mock()
        val responseJson = ""

        // set the WeatherDotCom provider with pollen capability.
        val weatherDotComProvider = WeatherDotComProvider(dependencyProvider)
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.of(EnvironmentProviderCapabilities.POLLEN)
        weatherDotComProvider.setEnvironmentProviderDelegate(environmentProviderDelegate)

        // Request for environment data
        weatherDotComProvider.getDataAsync(latitude, longitude)

        // Simulate an error response for the request
        weatherDotComProvider.pullPollenCompleted(pollenRequest, HttpStatusCode.NOT_FOUND.value, responseJson, null)

        // verify that null is sent to the EnvironmentProviderDelegate
        val pollenInfoArgumentCaptor = argumentCaptor<PollenInfo>()
        verify(environmentProviderDelegate).getDataComplete(pollenInfoArgumentCaptor.capture())

        assertNull(pollenInfoArgumentCaptor.lastValue)
    }

    @Test
    @Throws(JSONException::class)
    fun testWeatherDotComProviderProcessesExceptionInPollenResponseAndReturnsNull() {
        val environmentProviderDelegate: EnvironmentProviderDelegate = mock()
        val responseJson = ""

        // set the WeatherDotCom provider with pollen capability.
        val weatherDotComProvider = WeatherDotComProvider(dependencyProvider)
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.of(EnvironmentProviderCapabilities.POLLEN)
        weatherDotComProvider.setEnvironmentProviderDelegate(environmentProviderDelegate)

        // Request for environment data
        weatherDotComProvider.getDataAsync(latitude, longitude)

        // Simulate an exception in response to the request
        weatherDotComProvider.pullPollenCompleted(pollenRequest, HttpStatusCode.OK.value, responseJson, HttpRetryException("", HttpStatusCode.NOT_FOUND.value))

        // verify that null is sent to the EnvironmentProviderDelegate
        val pollenInfoArgumentCaptor = argumentCaptor<PollenInfo>()
        verify(environmentProviderDelegate).getDataComplete(pollenInfoArgumentCaptor.capture())

        assertNull(pollenInfoArgumentCaptor.lastValue)
    }

    @Test
    @Throws(JSONException::class)
    fun testWeatherDotComProviderReturnsNullIfPollenResponseCouldNotBeProcessed() {
        val environmentProviderDelegate: EnvironmentProviderDelegate = mock()
        mockStatic(WeatherInformationDeserializer::class.java)
        // simulate a JSON exception from the deserializer
        whenever(WeatherInformationDeserializer.deserializePollenInfo(any(), any())).thenThrow(JSONException("Invalid JSON string"))
        val responseJson = ""

        // set the WeatherDotCom provider with pollen capability.
        val weatherDotComProvider = WeatherDotComProvider(dependencyProvider)
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.of(EnvironmentProviderCapabilities.POLLEN)
        weatherDotComProvider.setEnvironmentProviderDelegate(environmentProviderDelegate)

        // Request for environment data
        weatherDotComProvider.getDataAsync(latitude, longitude)

        // Simulate a response for the request
        weatherDotComProvider.pullPollenCompleted(pollenRequest, HttpStatusCode.OK.value, responseJson, null)

        // verify that null is sent to the EnvironmentProviderDelegate
        val pollenInfoArgumentCaptor = argumentCaptor<PollenInfo>()
        verify(environmentProviderDelegate).getDataComplete(pollenInfoArgumentCaptor.capture())

        assertNull(pollenInfoArgumentCaptor.lastValue)
    }

    @Test
    @Throws(JSONException::class)
    fun testWeatherDotComProviderProcessesSuccessfulWeatherConditionResponseCorrectlyAndRequestsForWeatherForecast() {
        val environmentProviderDelegate: EnvironmentProviderDelegate = mock()
        val weatherInfo = WeatherInfo("providerName")
        mockStatic(WeatherInformationDeserializer::class.java)
        whenever(WeatherInformationDeserializer.deserializeCurrentWeatherInfo(any(), any())).thenReturn(weatherInfo)
        val responseJson = ""

        // set the WeatherDotCom provider with weather capability.
        val weatherDotComProvider = WeatherDotComProvider(dependencyProvider)
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.of(EnvironmentProviderCapabilities.WEATHER)
        weatherDotComProvider.setEnvironmentProviderDelegate(environmentProviderDelegate)

        // Request for environment data
        weatherDotComProvider.getDataAsync(latitude, longitude)
        verify(restClient, times(1)).execute(eq(weatherRequest), any())

        // Simulate a response for the request
        weatherDotComProvider.pullCurrentWeatherConditionCompleted(weatherRequest, HttpStatusCode.OK.value, responseJson, null)

        // verify that a new request has been sent for the weather forecast
        verify(restClient, times(1)).execute(eq(weatherForecastRequest), any())
    }

    @Test
    @Throws(JSONException::class)
    fun testWeatherDotComProviderProcessesFailedWeatherConditionResponseCorrectlyAndReturnsNull() {
        val environmentProviderDelegate: EnvironmentProviderDelegate = mock()
        val responseJson = ""

        // set the WeatherDotCom provider with weather capability.
        val weatherDotComProvider = WeatherDotComProvider(dependencyProvider)
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.of(EnvironmentProviderCapabilities.WEATHER)
        weatherDotComProvider.setEnvironmentProviderDelegate(environmentProviderDelegate)

        // Request for environment data
        weatherDotComProvider.getDataAsync(latitude, longitude)

        // Simulate an error in response to the request
        weatherDotComProvider.pullCurrentWeatherConditionCompleted(weatherRequest, HttpStatusCode.NOT_FOUND.value, responseJson, null)

        // verify that null is sent to the EnvironmentProviderDelegate
        val weatherInfoArgumentCaptor = argumentCaptor<WeatherInfo>()
        verify(environmentProviderDelegate).getDataComplete(weatherInfoArgumentCaptor.capture())

        assertNull(weatherInfoArgumentCaptor.lastValue)
    }

    @Test
    @Throws(JSONException::class)
    fun testWeatherDotComProviderProcessesExceptionInWeatherConditionResponseAndReturnsNull() {
        val environmentProviderDelegate: EnvironmentProviderDelegate = mock()
        val responseJson = ""

        // set the WeatherDotCom provider with weather capability.
        val weatherDotComProvider = WeatherDotComProvider(dependencyProvider)
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.of(EnvironmentProviderCapabilities.WEATHER)
        weatherDotComProvider.setEnvironmentProviderDelegate(environmentProviderDelegate)

        // Request for environment data
        weatherDotComProvider.getDataAsync(latitude, longitude)

        // Simulate an exception in response to the request
        weatherDotComProvider.pullCurrentWeatherConditionCompleted(weatherRequest, HttpStatusCode.OK.value, responseJson, HttpRetryException("", HttpStatusCode.NOT_FOUND.value))

        // verify that null is sent to the EnvironmentProviderDelegate
        val weatherInfoArgumentCaptor = argumentCaptor<WeatherInfo>()
        verify(environmentProviderDelegate).getDataComplete(weatherInfoArgumentCaptor.capture())

        assertNull(weatherInfoArgumentCaptor.lastValue)
    }

    @Test
    @Throws(JSONException::class)
    fun testWeatherDotComProviderReturnsNullIfWeatherConditionResponseCouldNotBeProcessed() {
        val environmentProviderDelegate: EnvironmentProviderDelegate = mock()
        mockStatic(WeatherInformationDeserializer::class.java)
        // simulate a JSON exception from the deserializer
        whenever(WeatherInformationDeserializer.deserializeCurrentWeatherInfo(any(), any())).thenThrow(JSONException("Invalid JSON string"))
        val responseJson = ""

        // set the WeatherDotCom provider with weather capability.
        val weatherDotComProvider = WeatherDotComProvider(dependencyProvider)
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.of(EnvironmentProviderCapabilities.WEATHER)
        weatherDotComProvider.setEnvironmentProviderDelegate(environmentProviderDelegate)

        // Request for environment data
        weatherDotComProvider.getDataAsync(latitude, longitude)

        // Simulate a response for the request
        weatherDotComProvider.pullCurrentWeatherConditionCompleted(weatherRequest, HttpStatusCode.OK.value, responseJson, null)

        // verify that null is sent to the EnvironmentProviderDelegate
        val weatherInfoArgumentCaptor = argumentCaptor<WeatherInfo>()
        verify(environmentProviderDelegate).getDataComplete(weatherInfoArgumentCaptor.capture())

        assertNull(weatherInfoArgumentCaptor.lastValue)
    }

    @Test
    @Throws(JSONException::class)
    fun testWeatherDotComProviderProcessesSuccessfulWeatherForecastResponseCorrectlyAndRequestsForWeatherForecast() {
        val environmentProviderDelegate: EnvironmentProviderDelegate = mock()
        val weatherInfo = WeatherInfo("providerName")
        val chanceOfPrecipitation = 30
        mockStatic(WeatherInformationDeserializer::class.java)
        whenever(WeatherInformationDeserializer.deserializeCurrentWeatherInfo(any(), any())).thenReturn(weatherInfo)
        whenever(WeatherInformationDeserializer.deserializeWeatherForecastInfoForChanceOfPrecipitation(any())).thenReturn(chanceOfPrecipitation)
        val responseJson = ""

        // set the WeatherDotCom provider with weather capability.
        val weatherDotComProvider = WeatherDotComProvider(dependencyProvider)
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.of(EnvironmentProviderCapabilities.WEATHER)
        weatherDotComProvider.setEnvironmentProviderDelegate(environmentProviderDelegate)

        // Request for environment data
        weatherDotComProvider.getDataAsync(latitude, longitude)

        // Simulate a response for weather condition
        weatherDotComProvider.pullCurrentWeatherConditionCompleted(weatherRequest, HttpStatusCode.OK.value, responseJson, null)
        // simulate a response for weather forecast
        weatherDotComProvider.pullWeatherForecastCompleted(weatherForecastRequest, HttpStatusCode.OK.value, responseJson, null)

        // verify that the weather information is sent to the EnvironmentProviderDelegate
        val weatherInfoArgumentCaptor = argumentCaptor<WeatherInfo>()
        verify(environmentProviderDelegate).getDataComplete(weatherInfoArgumentCaptor.capture())
        assertEquals(chanceOfPrecipitation, weatherInfo.chanceOfPrecipitation)

        assertEquals(weatherInfo, weatherInfoArgumentCaptor.lastValue)
    }

    @Test
    @Throws(JSONException::class)
    fun testWeatherDotComProviderProcessesFailedWeatherForecastResponseCorrectlyAndReturnsWeatherInfoWithoutChanceOfPrecipitation() {
        val environmentProviderDelegate: EnvironmentProviderDelegate = mock()
        val weatherInfo = WeatherInfo("providerName")
        mockStatic(WeatherInformationDeserializer::class.java)
        whenever(WeatherInformationDeserializer.deserializeCurrentWeatherInfo(any(), any())).thenReturn(weatherInfo)
        val responseJson = ""

        // set the WeatherDotCom provider with weather capability.
        val weatherDotComProvider = WeatherDotComProvider(dependencyProvider)
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.of(EnvironmentProviderCapabilities.WEATHER)
        weatherDotComProvider.setEnvironmentProviderDelegate(environmentProviderDelegate)

        // Request for environment data
        weatherDotComProvider.getDataAsync(latitude, longitude)

        // Simulate a response for the weather condition request
        weatherDotComProvider.pullCurrentWeatherConditionCompleted(weatherRequest, HttpStatusCode.OK.value, responseJson, null)
        // simulate an error for the weather forecast request
        weatherDotComProvider.pullWeatherForecastCompleted(weatherForecastRequest, HttpStatusCode.NOT_FOUND.value, responseJson, null)

        // verify that the weather info is sent without the chance of precipitation
        val weatherInfoArgumentCaptor = argumentCaptor<WeatherInfo>()
        verify(environmentProviderDelegate).getDataComplete(weatherInfoArgumentCaptor.capture())

        assertNull( weatherInfo.chanceOfPrecipitation)

        assertEquals(weatherInfo, weatherInfoArgumentCaptor.lastValue)
    }

    @Test
    @Throws(JSONException::class)
    fun testWeatherDotComProviderProcessesExceptionInWeatherForecastResponseAndReturnsWeatherInfoWithoutChanceOfPrecipitation() {
        val environmentProviderDelegate: EnvironmentProviderDelegate = mock()
        val weatherInfo = WeatherInfo("providerName")
        mockStatic(WeatherInformationDeserializer::class.java)
        whenever(WeatherInformationDeserializer.deserializeCurrentWeatherInfo(any(), any())).thenReturn(weatherInfo)
        val responseJson = ""

        // set the WeatherDotCom provider with weather capability.
        val weatherDotComProvider = WeatherDotComProvider(dependencyProvider)
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.of(EnvironmentProviderCapabilities.WEATHER)
        weatherDotComProvider.setEnvironmentProviderDelegate(environmentProviderDelegate)

        // Request for environment data
        weatherDotComProvider.getDataAsync(latitude, longitude)

        // Simulate a response for the weather condition request
        weatherDotComProvider.pullCurrentWeatherConditionCompleted(weatherRequest, HttpStatusCode.OK.value, responseJson, null)
        // simulate an exception for the weather forecast request
        weatherDotComProvider.pullWeatherForecastCompleted(weatherForecastRequest, HttpStatusCode.OK.value, responseJson, HttpRetryException("", HttpStatusCode.NOT_FOUND.value))

        // verify that weather info is sent to the EnvironmentProviderDelegate without chance of precipitation
        val weatherInfoArgumentCaptor = argumentCaptor<WeatherInfo>()
        verify(environmentProviderDelegate).getDataComplete(weatherInfoArgumentCaptor.capture())

        assertNull(weatherInfo.chanceOfPrecipitation)

        assertEquals(weatherInfo, weatherInfoArgumentCaptor.lastValue)
    }

    @Test
    @Throws(JSONException::class)
    fun testWeatherDotComProviderReturnsWeatherInfoWithoutChanceOfPrecipitationIfWeatherForecastResponseCouldNotBeProcessed() {
        val environmentProviderDelegate: EnvironmentProviderDelegate = mock()
        val weatherInfo = WeatherInfo("providerName")
        mockStatic(WeatherInformationDeserializer::class.java)
        whenever(WeatherInformationDeserializer.deserializeCurrentWeatherInfo(any(), any())).thenReturn(weatherInfo)
        // simulate a JSONException from the deserializer for weather forecast information
        whenever(WeatherInformationDeserializer.deserializeWeatherForecastInfoForChanceOfPrecipitation(any())).thenThrow(JSONException("Invalid JSON string"))
        val responseJson = ""

        // set the WeatherDotCom provider with weather capability.
        val weatherDotComProvider = WeatherDotComProvider(dependencyProvider)
        weatherDotComProvider.environmentProviderCapabilities = EnumSet.of(EnvironmentProviderCapabilities.WEATHER)
        weatherDotComProvider.setEnvironmentProviderDelegate(environmentProviderDelegate)

        // Request for environment data
        weatherDotComProvider.getDataAsync(latitude, longitude)

        // Simulate a response for the weather condition request
        weatherDotComProvider.pullCurrentWeatherConditionCompleted(weatherRequest, HttpStatusCode.OK.value, responseJson, null)
        // Simulate a response for the weather forecast request
        weatherDotComProvider.pullWeatherForecastCompleted(weatherForecastRequest, HttpStatusCode.OK.value, responseJson, null)

        // verify that weather info is sent without the chance of precipitation
        val weatherInfoArgumentCaptor = argumentCaptor<WeatherInfo>()
        verify(environmentProviderDelegate).getDataComplete(weatherInfoArgumentCaptor.capture())

        assertNull(weatherInfo.chanceOfPrecipitation)

        assertEquals(weatherInfo, weatherInfoArgumentCaptor.lastValue)
    }
}
