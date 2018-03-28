///
// EnvironmentServiceImpl.kt
// teva_environment
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.environment.services

import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.*
import com.teva.environment.entities.AirQualityInfo
import com.teva.environment.entities.EnvironmentInfo
import com.teva.environment.entities.PollenInfo
import com.teva.environment.entities.WeatherInfo
import com.teva.environment.enumerations.EnvironmentProviderCapabilities
import com.teva.environment.services.providers.EnvironmentProviderDelegate
import com.teva.environment.services.providers.EnvironmentProviderFactory
import com.teva.location.services.LocationInfo
import org.threeten.bp.Instant
import java.util.*

/**
 * This class is the EnvironmentService implementation. It implements the EnvironmentProviderDelegate.
 *
 * @param dependencyProvider - the dependency injection container.
 * @param environmentProviderFactory - the environment provider factory.
 */
class EnvironmentServiceImpl(
        private val dependencyProvider: DependencyProvider,
        private val environmentProviderFactory: EnvironmentProviderFactory)
    : EnvironmentService, EnvironmentProviderDelegate {

    /**
     * This property is the delegate to receive requested environment data.
     */
    private var environmentServiceDelegate: EnvironmentServiceDelegate? = null

    /**
     * This property is used to determine which provider responses to wait for.
     */
    private var waitingForResponse = EnumSet.noneOf(EnvironmentProviderCapabilities::class.java)

    /**
     * This property contains responses from the environment providers. It is forwarded to the EnvironmentServiceDelegate.. Its value is null if all of the providers fail to receive data from their corresponding services.
     */
    private var environmentInfo: EnvironmentInfo? = null

    /**
     * This property contains the location of the last pending request for environment data.
     * If there is no pending request, its value is null.
     */
    private var pendingGetEnvironmentRequestLocation: LocationInfo? = null

    /**
     * This method is the setter for the environment service delegate.
     * @param environmentServiceDelegate - the environment service delegate.
     */
    override fun setEnvironmentServiceDelegate(environmentServiceDelegate: EnvironmentServiceDelegate) {
        this.environmentServiceDelegate = environmentServiceDelegate
    }

    /**
     * This method starts a request to get environment data.
     *
     * @param locationInfo - This parameter contains the the requested environment location.
     */
    override fun getEnvironmentAsync(locationInfo: LocationInfo) {

        // Check if there was a previous Environment request that has not yet completed.
        if (waitingForEnvironmentProviderRequestsToComplete()) {
            // Save Environment request's location.
            pendingGetEnvironmentRequestLocation = locationInfo
            logger.log(INFO, String.format("Postponing Environment for location: %s due to outstanding request.", locationInfo))
            return
        } else {
            logger.log(INFO, String.format("Getting Environment for location: %s.", locationInfo))
        }

        // Reset old state.
        environmentInfo = null
        pendingGetEnvironmentRequestLocation = null
        waitingForResponse = EnumSet.noneOf(EnvironmentProviderCapabilities::class.java)

        val providers = environmentProviderFactory.getProviders(locationInfo)

        // Build waitingForResponse before getting individual provider data.
        // This ensures that the environment completion is sent only once, regardless of
        // whether provider.getDataAsync() is called synchronously (in unit test), or asynchronously.
        // NOTE: Do not combine loops.
        for (provider in providers) {
            waitingForResponse.addAll(provider.environmentProviderCapabilities)
        }

        for (provider in providers) {

            // Only request data if capability is in waitingForResponse. This looks like a redundant check
            // but provides the ability for a unit test to check if waitingForResponse is built properly.
            if (waitingForResponse.containsAll(provider.environmentProviderCapabilities)) {
                provider.setEnvironmentProviderDelegate(this)
                provider.getDataAsync(locationInfo.latitude, locationInfo.longitude)
            }
        }
    }

    /**
     * This is the method called after air quality info data is retrieved.
     *
     * @param airQualityInfo - the air quality info retrieved.
     */
    override fun getDataComplete(airQualityInfo: AirQualityInfo?) {

        waitingForResponse.remove(EnvironmentProviderCapabilities.AIR_QUALITY)

        // Keep environmentInfo null if fail to get AirQualityInfo.
        if (airQualityInfo != null && environmentInfo == null) {
            environmentInfo = EnvironmentInfo()
        }

        environmentInfo?.let { environmentInfo ->
            environmentInfo.airQualityInfo = airQualityInfo
        }

        sendCompletionIfDoneWaitingForProviders()
    }

    /**
     * This is the method called after pollen info data is retrieved.
     *
     * @param pollenInfo - the pollen info retrieved.
     */
    override fun getDataComplete(pollenInfo: PollenInfo?) {
        waitingForResponse.remove(EnvironmentProviderCapabilities.POLLEN)

        // Keep environmentInfo null if fail to get AirQualityInfo.
        if (pollenInfo != null && environmentInfo == null) {
            environmentInfo = EnvironmentInfo()
        }

        if (environmentInfo != null) {
            environmentInfo!!.pollenInfo = pollenInfo
        }

        sendCompletionIfDoneWaitingForProviders()
    }

    /**
     * This is the method called after weather info data is retrieved.
     *
     * @param weatherInfo - the weather info retrieved.
     */
    override fun getDataComplete(weatherInfo: WeatherInfo?) {
        waitingForResponse.remove(EnvironmentProviderCapabilities.WEATHER)

        // Keep environmentInfo null if fail to get AirQualityInfo.
        if (weatherInfo != null && environmentInfo == null) {
            environmentInfo = EnvironmentInfo()
        }

        if (environmentInfo != null) {
            environmentInfo!!.weatherInfo = weatherInfo
        }

        sendCompletionIfDoneWaitingForProviders()
    }

    /**
     * This method checks if there are any pending EnvironmentProvider responses.
     * Returns: Returns true if there are pending EnvironmentProvider responses, otherwise false.
     */
    private fun waitingForEnvironmentProviderRequestsToComplete(): Boolean {
        return waitingForResponse.size > 0
    }

    /**
     * This method checks if there are outstanding EnvironmentProvider requests.
     * If so, it returns.  Otherwise, it checks if there are pending requests for environment data, and starts the request if so.
     * Otherwise, it returns environment data to the EnvironmentServiceDelegate.
     */
    private fun sendCompletionIfDoneWaitingForProviders() {

        if (waitingForEnvironmentProviderRequestsToComplete()) {
            return
        }

        // If there is a pending request to get Environment, honor request now that
        // all previous responses have been received.
        // Don't raise completion if there is a pending request.
        if (pendingGetEnvironmentRequestLocation != null) {

            getEnvironmentAsync(pendingGetEnvironmentRequestLocation!!)
            return
        }

        // if all requests have failed, the environment info would remain uninitialized,
        // initialize it.
        if (environmentInfo == null) {
            environmentInfo = EnvironmentInfo()
        }

        // No pending request.  Raise completion.
        val now = dependencyProvider.resolve<TimeService>().now()
        environmentInfo!!.lastEnvironmentUpdateTime = now

        // Update the expiration date with the earliest expiration date.
        var expirationDate: Instant? = null

        if (environmentInfo!!.airQualityInfo != null) {
            expirationDate = environmentInfo!!.airQualityInfo!!.expirationDate
        }

        if (environmentInfo!!.pollenInfo != null && (expirationDate == null || expirationDate.isAfter(environmentInfo!!.pollenInfo!!.expirationDate!!))) {
            expirationDate = environmentInfo!!.pollenInfo!!.expirationDate
        }

        if (environmentInfo!!.weatherInfo != null && (expirationDate == null || expirationDate.isAfter(environmentInfo!!.weatherInfo!!.expirationDate!!))) {
            expirationDate = environmentInfo!!.weatherInfo!!.expirationDate
        }

        // if all requests have failed, the expiration date would be null
        // set it to the current time.
        if (expirationDate == null) {
            expirationDate = now
        }

        environmentInfo!!.expirationDate = expirationDate

        if (environmentServiceDelegate != null) {
            environmentServiceDelegate!!.getEnvironmentComplete(environmentInfo!!)
        }
    }

    companion object {

        private val logger = Logger(EnvironmentServiceImpl::class)
    }
}
