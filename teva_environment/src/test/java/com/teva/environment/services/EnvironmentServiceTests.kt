///
// EnvironmentServiceTests.kt
// teva_environment
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.environment.services

import com.nhaarman.mockito_kotlin.*
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.environment.entities.AirQualityInfo
import com.teva.environment.entities.EnvironmentInfo
import com.teva.environment.entities.PollenInfo
import com.teva.environment.entities.WeatherInfo
import com.teva.environment.enumerations.EnvironmentProviderCapabilities
import com.teva.environment.services.providers.EnvironmentProvider
import com.teva.environment.services.providers.EnvironmentProviderFactory
import com.teva.location.services.LocationInfo
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.threeten.bp.Instant
import java.util.*

/**
 * This class defines unit tests for the EnvironmentServiceImpl class.
 */
class EnvironmentServiceTests {

    private val latitude = 40.069308
    private val longitude = -75.556552

    private val dependencyProvider: DependencyProvider = DependencyProvider.default
    private val environmentProviderFactory: EnvironmentProviderFactory = mock()
    private val environmentProvider: EnvironmentProvider = mock()
    private val locationInfo: LocationInfo = LocationInfo(latitude, longitude, "", "", "", "")
    private val timeService: TimeService = mock()

    private val currentTime = Instant.ofEpochMilli(1491498741473L)

    @Before
    fun setup() {
        dependencyProvider.unregisterAll()

        whenever(environmentProvider.environmentProviderCapabilities).thenReturn(EnumSet.of(EnvironmentProviderCapabilities.AIR_QUALITY, EnvironmentProviderCapabilities.POLLEN, EnvironmentProviderCapabilities.WEATHER))
        val environmentProviderList = ArrayList<EnvironmentProvider>()
        environmentProviderList.add(environmentProvider)
        whenever(environmentProviderFactory.getProviders(any())).thenReturn(environmentProviderList)
        whenever(timeService.now()).thenReturn(currentTime)
        dependencyProvider.register(TimeService::class, timeService)
    }

    @Test
    fun testGetEnvironmentAsyncRequestsProviderForInformation() {
        val environmentService = EnvironmentServiceImpl(dependencyProvider, environmentProviderFactory)

        // request for the environment information.
        environmentService.getEnvironmentAsync(locationInfo)

        // verify that the request is delegated to the environment provider.
        verify(environmentProvider).getDataAsync(eq(latitude), eq(longitude))
    }

    @Test
    fun testGetEnvironmentAsyncWhileOneRequestIsInProgressDoesNotRequestProviderForInformation() {
        val environmentService = EnvironmentServiceImpl(dependencyProvider, environmentProviderFactory)
        // request for the environment information
        environmentService.getEnvironmentAsync(locationInfo)

        val latitude2 = 39.996811
        val longitude2 = -75.584094
        val locationInfo2 = LocationInfo(latitude2, longitude2, "", "", "", "")
        // request for the environment information of a second location
        environmentService.getEnvironmentAsync(locationInfo2)

        // verify that the first request is delegated to the environment provider
        // and the second request is not
        verify(environmentProvider, times(1)).getDataAsync(eq(latitude), eq(longitude))
        verify(environmentProvider, never()).getDataAsync(eq(latitude2), eq(longitude2))
    }

    @Test
    fun testGetEnvironmentAsyncRequestDoesNotCallbackTheDelegateIfAllResultsAreNotAvailable() {
        val environmentService = EnvironmentServiceImpl(dependencyProvider, environmentProviderFactory)

        val environmentServiceDelegate: EnvironmentServiceDelegate = mock()
        environmentService.setEnvironmentServiceDelegate(environmentServiceDelegate)

        // request for the environment information
        environmentService.getEnvironmentAsync(locationInfo)

        // simulate receipt of part of the results
        val airQualityInfo = AirQualityInfo("providerName")
        val pollenInfo = PollenInfo("providerName")

        environmentService.getDataComplete(airQualityInfo)
        environmentService.getDataComplete(pollenInfo)

        // verify that the response is not sent
        verify(environmentServiceDelegate, never()).getEnvironmentComplete(any())
    }

    @Test
    fun testGetEnvironmentAsyncRequestCallsBackTheDelegateIfAllResultsAreAvailable() {
        val environmentService = EnvironmentServiceImpl(dependencyProvider, environmentProviderFactory)

        val environmentServiceDelegate: EnvironmentServiceDelegate = mock()
        environmentService.setEnvironmentServiceDelegate(environmentServiceDelegate)

        // request for the environment information
        environmentService.getEnvironmentAsync(locationInfo)

        // simulate the receipt of all the results
        val airQualityInfo = AirQualityInfo("providerName")
        val pollenInfo = PollenInfo("providerName")
        val weatherInfo = WeatherInfo("providerName")
        environmentService.getDataComplete(airQualityInfo)
        environmentService.getDataComplete(pollenInfo)
        environmentService.getDataComplete(weatherInfo)

        val environmentInfoArgumentCaptor = ArgumentCaptor.forClass(EnvironmentInfo::class.java)

        // verify that the response is sent
        verify(environmentServiceDelegate, times(1)).getEnvironmentComplete(environmentInfoArgumentCaptor.capture())

        // verify that the airqualityinfo, polleninfo and weatherinfo in the response
        // match the received values
        assertEquals(environmentInfoArgumentCaptor.value.airQualityInfo, airQualityInfo)
        assertEquals(environmentInfoArgumentCaptor.value.pollenInfo, pollenInfo)
        assertEquals(environmentInfoArgumentCaptor.value.weatherInfo, weatherInfo)
    }

    @Test
    fun testEnvironmentServiceSetsExpirationDateOfEnvironmentInfoToTheEarliestExpirationDate() {
        val earliestExpirationDate = Instant.ofEpochMilli(1491499341473L)
        val latestExpirationDate = Instant.ofEpochMilli(1491499941473L)
        val intermediateExpirationDate = Instant.ofEpochMilli(1491499741473L)

        val environmentService = EnvironmentServiceImpl(dependencyProvider, environmentProviderFactory)

        val environmentServiceDelegate:EnvironmentServiceDelegate = mock()
        environmentService.setEnvironmentServiceDelegate(environmentServiceDelegate)

        // request for the environment information
        environmentService.getEnvironmentAsync(locationInfo)

        // simulate receipt of the results with different expiration dates
        val airQualityInfo = AirQualityInfo("providerName")
        airQualityInfo.expirationDate = latestExpirationDate
        val pollenInfo = PollenInfo("providerName")
        pollenInfo.expirationDate = earliestExpirationDate
        val weatherInfo = WeatherInfo("providerName")
        weatherInfo.expirationDate = intermediateExpirationDate
        environmentService.getDataComplete(airQualityInfo)
        environmentService.getDataComplete(pollenInfo)
        environmentService.getDataComplete(weatherInfo)

        val environmentInfoArgumentCaptor = ArgumentCaptor.forClass(EnvironmentInfo::class.java)

        // verify that the response is sent with the earliest expiration date
        verify(environmentServiceDelegate, times(1)).getEnvironmentComplete(environmentInfoArgumentCaptor.capture())
        assertEquals(environmentInfoArgumentCaptor.value.expirationDate, earliestExpirationDate)
    }

    @Test
    fun testEnvironmentServiceProcessesPendingRequestAfterCurrentRequestIsCompleted() {
        val environmentService = EnvironmentServiceImpl(dependencyProvider, environmentProviderFactory)

        val environmentServiceDelegate: EnvironmentServiceDelegate = mock()
        environmentService.setEnvironmentServiceDelegate(environmentServiceDelegate)

        // request for environment information
        environmentService.getEnvironmentAsync(locationInfo)

        val latitude2 = 39.996811
        val longitude2 = -75.584094
        val locationInfo2 = LocationInfo(latitude2, longitude2, "", "", "", "")

        // request for environment information of another location
        environmentService.getEnvironmentAsync(locationInfo2)
        verify(environmentProvider, never()).getDataAsync(latitude2, longitude2)

        // simulate the receipt of results for the first request
        val airQualityInfo = AirQualityInfo("providerName")
        val pollenInfo = PollenInfo("providerName")
        val weatherInfo = WeatherInfo("providerName")
        environmentService.getDataComplete(airQualityInfo)
        environmentService.getDataComplete(pollenInfo)
        environmentService.getDataComplete(weatherInfo)

        // verify that the second request is delegated to the environment provider
        verify(environmentProvider, times(1)).getDataAsync(latitude2, longitude2)
    }

    @Test
    fun testGetEnvironmentAsyncRequestCallsBackTheDelegateIfRetrievalOfPartOfTheResultsFailed() {
        val environmentService = EnvironmentServiceImpl(dependencyProvider, environmentProviderFactory)

        val environmentServiceDelegate: EnvironmentServiceDelegate = mock()
        environmentService.setEnvironmentServiceDelegate(environmentServiceDelegate)

        // request for environment information
        environmentService.getEnvironmentAsync(locationInfo)

        // simulate receipt of results with failure for part of the request
        val airQualityInfo = AirQualityInfo("providerName")
        val pollenInfo = PollenInfo("providerName")
        val weatherInfo: WeatherInfo? = null
        environmentService.getDataComplete(airQualityInfo)
        environmentService.getDataComplete(pollenInfo)
        environmentService.getDataComplete(weatherInfo)

        val environmentInfoArgumentCaptor = ArgumentCaptor.forClass(EnvironmentInfo::class.java)

        // verify that the response is sent with the received information
        verify(environmentServiceDelegate, times(1)).getEnvironmentComplete(environmentInfoArgumentCaptor.capture())
        assertEquals(environmentInfoArgumentCaptor.value.airQualityInfo, airQualityInfo)
        assertEquals(environmentInfoArgumentCaptor.value.pollenInfo, pollenInfo)
        assertEquals(environmentInfoArgumentCaptor.value.weatherInfo, weatherInfo)
    }

    @Test
    fun testGetEnvironmentAsyncRequestReturnsResponseIrrespectiveOfTheOrderOfTheIndividualResults() {
        val environmentService = EnvironmentServiceImpl(dependencyProvider, environmentProviderFactory)

        val environmentServiceDelegate: EnvironmentServiceDelegate = mock()
        environmentService.setEnvironmentServiceDelegate(environmentServiceDelegate)

        // request for environment information
        environmentService.getEnvironmentAsync(locationInfo)

        // simulate receipt of the results
        val airQualityInfo = AirQualityInfo("providerName")
        val pollenInfo = PollenInfo("providerName")
        val weatherInfo = WeatherInfo("providerName")
        environmentService.getDataComplete(airQualityInfo)
        environmentService.getDataComplete(pollenInfo)
        environmentService.getDataComplete(weatherInfo)

        // request for environment information for another location
        val latitude2 = 39.996811
        val longitude2 = -75.584094
        val locationInfo2 = LocationInfo(latitude2, longitude2, "", "", "", "")
        environmentService.getEnvironmentAsync(locationInfo2)

        // simulate receipt of the results in a different order
        val airQualityInfo2 = AirQualityInfo("providerName")
        val pollenInfo2 = PollenInfo("providerName")
        val weatherInfo2 = WeatherInfo("providerName")
        environmentService.getDataComplete(pollenInfo2)
        environmentService.getDataComplete(weatherInfo2)
        environmentService.getDataComplete(airQualityInfo2)

        // request for environment information for another location
        val latitude3 = 40.229135
        val longitude3 = -75.218633
        val locationInfo3 = LocationInfo(latitude3, longitude3, "", "", "", "")
        environmentService.getEnvironmentAsync(locationInfo3)

        // simulate receipt of the results in a different order from the
        // previous two requests
        val airQualityInfo3 = AirQualityInfo("providerName")
        val pollenInfo3 = PollenInfo("providerName")
        val weatherInfo3 = WeatherInfo("providerName")
        environmentService.getDataComplete(weatherInfo3)
        environmentService.getDataComplete(airQualityInfo3)
        environmentService.getDataComplete(pollenInfo3)


        val environmentInfoArgumentCaptor = ArgumentCaptor.forClass(EnvironmentInfo::class.java)

        // verify that the responses of all the three requests have been sent
        verify(environmentServiceDelegate, times(3)).getEnvironmentComplete(environmentInfoArgumentCaptor.capture())

        // verify that the results of each response sent were correct
        assertEquals(environmentInfoArgumentCaptor.allValues[0].airQualityInfo, airQualityInfo)
        assertEquals(environmentInfoArgumentCaptor.allValues[0].pollenInfo, pollenInfo)
        assertEquals(environmentInfoArgumentCaptor.allValues[0].weatherInfo, weatherInfo)

        assertEquals(environmentInfoArgumentCaptor.allValues[1].airQualityInfo, airQualityInfo2)
        assertEquals(environmentInfoArgumentCaptor.allValues[1].pollenInfo, pollenInfo2)
        assertEquals(environmentInfoArgumentCaptor.allValues[1].weatherInfo, weatherInfo2)

        assertEquals(environmentInfoArgumentCaptor.allValues[2].airQualityInfo, airQualityInfo3)
        assertEquals(environmentInfoArgumentCaptor.allValues[2].pollenInfo, pollenInfo3)
        assertEquals(environmentInfoArgumentCaptor.allValues[2].weatherInfo, weatherInfo3)

    }
}
