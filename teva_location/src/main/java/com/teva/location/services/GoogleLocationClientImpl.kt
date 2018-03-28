///
// GoogleLocationClientImpl.kt
// teva_location
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.location.services

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.location.Location
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger

/**
 * Implementation of an abstraction around the google play location services
 */
class GoogleLocationClientImpl(dependencyProvider: DependencyProvider) : GoogleLocationClient {
    private val logger = Logger(GoogleLocationClient::class)
    private val REQUEST_CHECK_SETTINGS = 1001

    private val context = dependencyProvider.resolve<Context>()

    private var googleApiClient: GoogleApiClient? = null

    /**
     * The current location
     */
    override val currentLocation: Location?
        get() {
            try {
                return LocationServices.FusedLocationApi.getLastLocation(
                        googleApiClient)
            } catch (ex: SecurityException) {
                logger.logException(Logger.Level.ERROR, "Exception getting last location", ex)
            }
            return null
        }

    /**
     * Connect to the google play services for using location services.
     */
    override fun initialize() {
        // Connect to the google play services for using location services.
        if (googleApiClient == null) {
            googleApiClient = GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .build()
        }

        googleApiClient?.connect()
    }

    /**
     * Disconnect from the google play services
     */
    override fun uninitialize() {
        googleApiClient?.disconnect()
    }

    /**
     * Displays a dialog to enable location services through google play services
     */
    override fun enableLocationServices(activity: Activity) {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_LOW_POWER

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback { result ->
            val status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> logger.log(Logger.Level.VERBOSE, "All location settings are satisfied.")
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    logger.log(Logger.Level.DEBUG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ")

                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        status.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS)
                    } catch (e: IntentSender.SendIntentException) {
                        logger.logException(Logger.Level.ERROR, "PendingIntent unable to execute request.", e)
                    }

                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> logger.log(Logger.Level.WARN, "Location settings are inadequate, and cannot be fixed here. Dialog not created.")
            }
        }
    }

}