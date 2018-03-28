//
// EnvironmentViewModelTest.java
// app
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.activity.viewmodel.environment

import com.nhaarman.mockito_kotlin.*
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.common.utilities.Messenger
import com.teva.environment.entities.AirQualityInfo
import com.teva.environment.entities.EnvironmentInfo
import com.teva.environment.entities.PollenInfo
import com.teva.environment.entities.WeatherInfo
import com.teva.environment.enumerations.AirQuality
import com.teva.environment.enumerations.PollenLevel
import com.teva.environment.enumerations.WeatherCondition
import com.teva.environment.enumerations.WindDirectionCardinal
import com.teva.environment.messages.CheckLocationAndUpdateEnvironmentMessage
import com.teva.environment.messages.EnvironmentUpdatedMessage
import com.teva.environment.messages.UpdateEnvironmentMessage
import com.teva.environment.models.EnvironmentMonitor
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.common.DateTimeLocalization
import com.teva.respiratoryapp.testutils.BaseTest
import com.teva.respiratoryapp.testutils.mocks.HandlerHelper
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset

/**
 * This class defines unit tests for the environment viewmodel class.
 */
class EnvironmentViewModelTest : BaseTest() {

    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var environmentMonitor: EnvironmentMonitor
    private lateinit var dateTimeLocalization: DateTimeLocalization
    private lateinit var messenger: Messenger
    private lateinit var date: Instant
    private lateinit var localizationService: LocalizationService

    @Before
    fun setup() {
        dependencyProvider = DependencyProvider.default
        dependencyProvider.register(ZoneId::class, ZoneOffset.ofTotalSeconds(0))

        environmentMonitor = mock()
        dateTimeLocalization = mock()
        localizationService = mock()
        whenever(dateTimeLocalization.toNumericMonthDay(any())).thenReturn("4/11")
        whenever(dateTimeLocalization.toFullWeekDay(any())).thenReturn("Tuesday")
        whenever<String>(localizationService.getString(eq(R.string.environment_screen_degree_symbol_text))).thenReturn("\u00B0")
        whenever<String>(localizationService.getString(eq(R.string.percentSign_text))).thenReturn("%")
        whenever<String>(localizationService.getString(eq(R.string.distancePerHourUnit_text))).thenReturn("mph")
        messenger = mock()
        dependencyProvider.register(Messenger::class, messenger)
        dependencyProvider.register(DateTimeLocalization::class, dateTimeLocalization)
        dependencyProvider.register(LocalizationService::class, localizationService)
        date = Instant.ofEpochSecond(1491942050L)
    }

    @Test
    fun testEnvironmentViewModelPublishesAnUpdateEnvironmentMessageIfEnvironmentInfoIsUnavailable() {
        whenever(environmentMonitor.currentEnvironmentInfo).thenReturn(null)
        dependencyProvider.register(EnvironmentMonitor::class, environmentMonitor)

        val environmentViewModel = EnvironmentViewModel(dependencyProvider)
        environmentViewModel.onStart()

        verify(messenger).publish(any<UpdateEnvironmentMessage>())
        verify(messenger).publish(any<CheckLocationAndUpdateEnvironmentMessage>())
    }

    @Test
    fun testEnvironmentViewModelPopulatesDataForDisplayWithoutUpdateEnvironmentMessageIfReadilyAvailable() {
        val expectedWeatherInfo = createWeatherInfo()
        val expectedPollenInfo = createPollenInfo()
        val expectedAirQualityInfo = createAirQualityInfo()
        val expectedLocationAndDate = "New York, Tuesday 4/11"
        val expectedWindDetails = "11 mph"
        val expectedTemperature = "65" + "\u00B0"
        val expectedHumidity = "33%"
        val expectedChanceOfPrecipitation = "10%"

        whenever(environmentMonitor.currentEnvironmentInfo).thenReturn(createEnvironmentInfo())
        dependencyProvider.register(EnvironmentMonitor::class, environmentMonitor)

        val environmentViewModel = EnvironmentViewModel(dependencyProvider)
        environmentViewModel.onStart()

        HandlerHelper.loopHandler();

        verify(messenger, never()).publish(any<UpdateEnvironmentMessage>())

        assertEquals(environmentViewModel.airQuality, expectedAirQualityInfo.airQuality)
        assertEquals(environmentViewModel.airQualitySource, expectedAirQualityInfo.providerName)
        assertEquals(environmentViewModel.chanceOfPrecipitation, expectedChanceOfPrecipitation)
        assertEquals(environmentViewModel.locationAndDate, expectedLocationAndDate)
        assertEquals(environmentViewModel.humidity, expectedHumidity)
        assertEquals(environmentViewModel.temperature, expectedTemperature)
        assertEquals(environmentViewModel.grassPollenLevel, expectedPollenInfo.grassPollenLevel)
        assertEquals(environmentViewModel.weedPollenLevel, expectedPollenInfo.weedPollenLevel)
        assertEquals(environmentViewModel.treePollenLevel, expectedPollenInfo.treePollenLevel)
        assertEquals(environmentViewModel.weatherCondition, expectedWeatherInfo.weatherCondition)
        assertEquals(environmentViewModel.windDetails, expectedWindDetails)
    }

    @Test
    fun testEnvironmentViewModelPopulatesDataWhenEnvironmentUpdatedMessageIsReceived() {
        val expectedWeatherInfo = createWeatherInfo()
        val expectedPollenInfo = createPollenInfo()
        val expectedAirQualityInfo = createAirQualityInfo()
        val expectedLocationAndDate = "New York, Tuesday 4/11"
        val expectedWindDetails = "11 mph"
        val expectedTemperature = "65" + "\u00B0"
        val expectedHumidity = "33%"
        val expectedChanceOfPrecipitation = "10%"

        whenever(environmentMonitor.currentEnvironmentInfo).thenReturn(createEnvironmentInfo())
        dependencyProvider.register(EnvironmentMonitor::class, environmentMonitor)

        val environmentViewModel = EnvironmentViewModel(dependencyProvider)

        environmentViewModel.onEnvironmentUpdated(EnvironmentUpdatedMessage())

        assertEquals(environmentViewModel.airQuality, expectedAirQualityInfo.airQuality)
        assertEquals(environmentViewModel.airQualitySource, expectedAirQualityInfo.providerName)
        assertEquals(environmentViewModel.chanceOfPrecipitation, expectedChanceOfPrecipitation)
        assertEquals(environmentViewModel.locationAndDate, expectedLocationAndDate)
        assertEquals(environmentViewModel.humidity, expectedHumidity)
        assertEquals(environmentViewModel.temperature, expectedTemperature)
        assertEquals(environmentViewModel.grassPollenLevel, expectedPollenInfo.grassPollenLevel)
        assertEquals(environmentViewModel.weedPollenLevel, expectedPollenInfo.weedPollenLevel)
        assertEquals(environmentViewModel.treePollenLevel, expectedPollenInfo.treePollenLevel)
        assertEquals(environmentViewModel.weatherCondition, expectedWeatherInfo.weatherCondition)
        assertEquals(environmentViewModel.windDetails, expectedWindDetails)
    }

    @Test
    fun testEnvironmentViewModelSubscribesToMessengerWhenStarted() {
        whenever(environmentMonitor.currentEnvironmentInfo).thenReturn(null).thenReturn(null)
        dependencyProvider.register(EnvironmentMonitor::class, environmentMonitor)

        val environmentViewModel = EnvironmentViewModel(dependencyProvider)
        environmentViewModel.onStart()

        verify(messenger).subscribe(eq(environmentViewModel))
    }

    @Test
    fun testEnvironmentViewModelUnSubscribesFromMessengerWhenStopped() {
        whenever(environmentMonitor.currentEnvironmentInfo).thenReturn(null).thenReturn(null)
        dependencyProvider.register(EnvironmentMonitor::class, environmentMonitor)

        val environmentViewModel = EnvironmentViewModel(dependencyProvider)
        environmentViewModel.onStop()

        verify(messenger).unsubscribeToAll(eq(environmentViewModel))
    }

    /**
     * This method creates an EnvironmentInfo object to be used in the tests
     * and fills it with the weather info, pollen info and air quality info.

     * @return - an environment info object to be used in the tests.
     */
    private fun createEnvironmentInfo(): EnvironmentInfo {
        val environmentInfo = EnvironmentInfo()
        environmentInfo.weatherInfo = createWeatherInfo()
        environmentInfo.pollenInfo = createPollenInfo()
        environmentInfo.airQualityInfo = createAirQualityInfo()
        return environmentInfo
    }

    /**
     * This method creates a WeatherInfo object to be used in the tests.

     * @return - a weather info object to be used in the tests.
     */
    private fun createWeatherInfo(): WeatherInfo {
        val weatherInfo = WeatherInfo("Weather.com")
        weatherInfo.locationFull = "New York"
        weatherInfo.chanceOfPrecipitation = 10
        weatherInfo.relativeHumidity = 33
        weatherInfo.temperature = 65
        weatherInfo.date = date
        weatherInfo.weatherCondition = WeatherCondition.FAIR_DAY
        weatherInfo.windDirectionCardinal = WindDirectionCardinal.North
        weatherInfo.windSpeed = 11
        return weatherInfo
    }

    /**
     * This method creates an AirQualityInfo object to be used in the tests.

     * @return - an air quality info object to be used in the tests.
     */
    private fun createAirQualityInfo(): AirQualityInfo {
        val airQualityInfo = AirQualityInfo("Source: EPA: AirNow")
        airQualityInfo.airQuality = AirQuality.MODERATE
        return airQualityInfo
    }

    /**
     * This method creates a PollenInfo object to be used in the tests.

     * @return - a pollen info object to be used in the tests.
     */
    private fun createPollenInfo(): PollenInfo {
        val pollenInfo = PollenInfo("Weather.com")
        pollenInfo.grassPollenLevel = PollenLevel.LOW
        pollenInfo.treePollenLevel = PollenLevel.MODERATE
        pollenInfo.weedPollenLevel = PollenLevel.NONE
        return pollenInfo
    }
}
