//
// EnvironmentViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.environment

import android.os.Handler
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.environment.entities.EnvironmentInfo
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
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel
import org.greenrobot.eventbus.Subscribe
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import java.util.*
import com.teva.utilities.utilities.Logger.Level.*

/**
 * Viewmodel for the Environment screen.
 *
 * @param dependencyProvider The dependency injection mechanism.
 */
class EnvironmentViewModel(dependencyProvider: DependencyProvider) : FragmentViewModel(dependencyProvider) {

    private val environmentMonitor: EnvironmentMonitor = dependencyProvider.resolve()
    private val messenger: Messenger = dependencyProvider.resolve()
    private val dateTimeLocalization: DateTimeLocalization = dependencyProvider.resolve()

    private var date: String? = null
    private var chanceOfPrecipitationValue: Int? = null
    private var windDirectionCardinal: WindDirectionCardinal? = null
    private var windSpeed: Int? = null
    private var humidityValue: Int? = null
    private var temperatureValue: Int? = null
    private var timer:Timer? = null
    private val LOCATION_CHECK_TIMER_INTERVAL = 120000L // MILLISECONDS

    private val locationCheckHandler = Handler()


    /**
     * The weather condition.
     */
    var weatherCondition: WeatherCondition? = null
        private set

    /**
     * The air quality.
     */
    var airQuality: AirQuality? = null
        private set

    /**
     * The tree pollen level.
     */
    var treePollenLevel: PollenLevel? = null
        private set

    /**
     * The grass pollen level.
     */
    var grassPollenLevel: PollenLevel? = null
        private set

    /**
     * The getter for the weed pollen level.
     */
    var weedPollenLevel: PollenLevel? = null
        private set

    /**
     * The weather condition extended code.
     */
    var weatherConditionExtendedCode: Int? = null
        private set

    /**
     * The air quality source.
     */
    var airQualitySource: String? = null
        private set

    /**
     * The location
     */
    var location: String? = null
        private set


    /**
     * The day.
     */
    private var day: String? = null

    val locationAndDate: String
        get() {
            var locationAndDate = ""

            if (location != null && day != null && date != null) {
                locationAndDate = "$location, $day $date"
            }

            return locationAndDate
        }

    /**
     * The temperature string.
     */
    val temperature: String
        get() {
            var temperatureString = ""

            if (temperatureValue != null) {
                temperatureString = Integer.toString(temperatureValue!!) + getString(R.string.environment_screen_degree_symbol_text)
            }

            return temperatureString
        }

    /**
     * This method is the getter for the chance of precipitation.
     *
     * @return - the chance of precipitation.
     */
    val chanceOfPrecipitation: String
        get() {
            var precipitationString = getString(R.string.blankFieldDash_text)

            if (chanceOfPrecipitationValue != null) {
                precipitationString = Integer.toString(chanceOfPrecipitationValue!!) + getString(R.string.percentSign_text)
            }

            return precipitationString
        }

    /**
     * This method combines the wind direction and cardinal information and returns it.

     * @return - the wind details.
     */
    val windLabel: String
        get() {
            if (windDirectionCardinal != null) {
                return windDirectionCardinal!!.toString() + " " + getString(R.string.environmentWeatherWind_text)
            } else {
                return getString(R.string.environmentWeatherWind_text)
            }
        }

    /**
     * This method combines the wind direction and cardinal information and returns it.

     * @return - the wind details.
     */
    val windDetails: String
        get() {
            var windDetails = getString(R.string.blankFieldDash_text)

            if (windSpeed != null) {
                windDetails = Integer.toString(windSpeed!!) + " " + getString(R.string.distancePerHourUnit_text)
            }

            return windDetails
        }


    /**
     * This method is the getter for the humidity.
     */
    val humidity: String
        get() {
            var humidityString = getString(R.string.blankFieldDash_text)

            if (humidityValue != null) {
                humidityString = Integer.toString(humidityValue!!) + getString(R.string.percentSign_text)
            }

            return humidityString
        }

    /**
     * This function starts the timer which periodically sends request for
     * checking the location and updating the environment information.
     */
    private fun startLocationCheckRequests() {

        logger.log(INFO, "Checking for location update...")
        dependencyProvider.resolve<Messenger>().publish(CheckLocationAndUpdateEnvironmentMessage())
        locationCheckHandler.postDelayed({startLocationCheckRequests()}, LOCATION_CHECK_TIMER_INTERVAL)
    }

    /**
     * This function stops the timer which periodically sends request for
     * checking the location and updating the environment information.
     */
    private fun stopLocationCheckRequests() {
        locationCheckHandler.removeCallbacks(null)
    }

    /**
     * Method called by the BaseFragment when the fragment's onStart() lifecycle method is called.
     */
    override fun onStart() {
        super.onStart()
        messenger.subscribe(this)

        val environmentInfo = environmentMonitor.currentEnvironmentInfo

        if (environmentInfo == null) {
            messenger.publish(UpdateEnvironmentMessage())
        } else {
            updateFieldsFromEnvironmentInfo(environmentInfo)
        }
        logger.log(INFO, "Starting location check from EnvironmentViewModel")
        startLocationCheckRequests()
    }

    /**
     * Method called by the BaseFragment when the fragment's onStop() lifecycle method is called.
     */
    override fun onStop() {
        super.onStop()
        messenger.unsubscribeToAll(this)
        logger.log(INFO, "Stopping location check from EnvironmentViewModel")
        stopLocationCheckRequests()
    }

    /**
     * This method is invoked when an EnvironmentUpdatedMessage is published by the environment monitor.

     * @param environmentUpdatedMessage - the environment updated message.
     */
    @Subscribe
    fun onEnvironmentUpdated(environmentUpdatedMessage: EnvironmentUpdatedMessage) {
        val environmentInfo = environmentMonitor.currentEnvironmentInfo
        if (environmentInfo != null) {
            updateFieldsFromEnvironmentInfo(environmentInfo)
        } else {
            resetAirQualityValues()
            resetPollenValues()
            resetWeatherConditionValues()
            notifyChange()
        }
    }

    /**
     * This method updates the member fields from the environment info.

     * @param environmentInfo - the environment info.
     */
    private fun updateFieldsFromEnvironmentInfo(environmentInfo: EnvironmentInfo) {
        val weatherInfo = environmentInfo.weatherInfo
        val airQualityInfo = environmentInfo.airQualityInfo
        val pollenInfo = environmentInfo.pollenInfo

        if (weatherInfo != null) {
            chanceOfPrecipitationValue = weatherInfo.chanceOfPrecipitation!!
            humidityValue = weatherInfo.relativeHumidity!!
            temperatureValue = weatherInfo.temperature!!
            windDirectionCardinal = weatherInfo.windDirectionCardinal!!
            windSpeed = weatherInfo.windSpeed!!
            val weatherDate = LocalDateTime.ofInstant(weatherInfo.date!!,
                    dependencyProvider.resolve<ZoneId>()).toLocalDate()

            date = dateTimeLocalization.toNumericMonthDay(weatherDate)
            day = dateTimeLocalization.toFullWeekDay(weatherDate)
            location = weatherInfo.locationFull!!
            weatherCondition = weatherInfo.weatherCondition
            weatherConditionExtendedCode = weatherInfo.weatherConditionExtendedCode
        } else {
            resetWeatherConditionValues()
        }

        if (airQualityInfo != null) {
            airQuality = airQualityInfo.airQuality
            airQualitySource = airQualityInfo.providerName
        } else {
            resetAirQualityValues()
        }

        if (pollenInfo != null) {
            treePollenLevel = pollenInfo.treePollenLevel
            grassPollenLevel = pollenInfo.grassPollenLevel
            weedPollenLevel = pollenInfo.weedPollenLevel
        } else {
            resetPollenValues()
        }

        notifyChange()
    }

    /**
     * This method resets the weather condition values.
     */
    private fun resetWeatherConditionValues() {
        chanceOfPrecipitationValue = null
        humidityValue = null
        temperatureValue = null
        windDirectionCardinal = null
        windSpeed = null
        date = null
        day = null
        location = null
        weatherCondition = null
        weatherConditionExtendedCode = null
    }

    /**
     * This method resets the pollen values.
     */
    private fun resetPollenValues() {
        treePollenLevel = null
        grassPollenLevel = null
        weedPollenLevel = null
    }

    /**
     * This method resets the air quality values.
     */
    private fun resetAirQualityValues() {
        airQuality = null
        airQualitySource = ""
    }
}
