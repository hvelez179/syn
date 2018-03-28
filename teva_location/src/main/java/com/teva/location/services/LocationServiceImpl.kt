//
// LocationServiceImpl.kt
// teva_location
//
// Copyright (c) 2017 Teva. All rights reserved.
//
package com.teva.location.services


import android.Manifest
import android.app.Activity
import android.content.*
import android.location.*
import android.location.LocationManager as AndroidLocationManager
import android.os.AsyncTask
import android.os.Bundle
import com.teva.common.messages.PermissionUpdateMessage
import com.teva.common.services.PermissionManager
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.*
import com.teva.common.utilities.Messenger
import org.greenrobot.eventbus.Subscribe
import java.io.IOException

/**
 * This class implements the location service interface.
 * This class requires the location manager and application context objects
 * to be registered with the dependency provider
 */
class LocationServiceImpl(private val dependencyProvider: DependencyProvider) : LocationService, LocationListener {
    private val logger = Logger(LocationServiceImpl::class)

    private val googleLocationClient = dependencyProvider.resolve<GoogleLocationClient>()
    private var androidLocationManager: AndroidLocationManager? = null
    private val locationStateReceiver = LocationStateReceiver()
    private val locationSettings: LocationSettings = dependencyProvider.resolve<LocationSettings>()

    private var serviceStarted = false
    private val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
    private val permissionManager: PermissionManager = dependencyProvider.resolve<PermissionManager>()
    private val serviceUnavailableMessage = " location service has not been started or access permissions have not been granted."

    /**
     * The location service delegate.
     */
    override var locationServiceDelegate: LocationServiceDelegate? = null

    /**
     * Starts a service's background processes.
     */
    override fun startService() {
        logger.log(DEBUG, "startService()")

        serviceStarted = true

        val intentFilter = IntentFilter(AndroidLocationManager.PROVIDERS_CHANGED_ACTION)
        dependencyProvider.resolve<Context>().registerReceiver(locationStateReceiver, intentFilter)

        // Get the location manager from the system.
        androidLocationManager = dependencyProvider.resolve<AndroidLocationManager>()

        // Connect to the google play services for using location services.
        googleLocationClient.initialize()

        // subscribe for location permission updates
        dependencyProvider.resolve<Messenger>().subscribe(this)

        if (!isAvailable) {
            logger.log(WARN, "Unable to provide location updates as" + serviceUnavailableMessage)

            return
        }

        // Register for location updates.
        RequestLocationUpdates()

        dependencyProvider.resolve<Messenger>().publish(LocationProvidersChangedMessage())
    }

    /**
     * Stops a service's background processes.
     */
    override fun stopService() {
        logger.log(DEBUG, "stopService()")

        dependencyProvider.resolve<Context>().unregisterReceiver(locationStateReceiver)

        dependencyProvider.resolve<Messenger>().unsubscribeToAll(this)

        // Disconnect from google play services.
        googleLocationClient.uninitialize()

        if (!isAvailable) {
            logger.log(WARN, "Cannot stop service as" + serviceUnavailableMessage)
            serviceStarted = false
            return
        }

        // Unregister location updates.
        CancelLocationUpdates()

        serviceStarted = false

        dependencyProvider.resolve<Messenger>().publish(LocationProvidersChangedMessage())
    }

    /**
     * Gets the current location coordinates, or null if the information cannot be obtained.
     */
    override val currentLocation: Location?
        get() {

            if (!isAvailable) {
                logger.log(WARN, "Unable to obtain location information as" + serviceUnavailableMessage)
            } else {
                return googleLocationClient.currentLocation
            }

            return null
        }

    /**
     * Gets whether the location service is available.
     */
    override val isAvailable: Boolean
        get() {
            val isLocationModeOn = locationSettings.isLocationModeOn
            val permissionGranted = permissionManager.checkPermission(*permissions)

            return serviceStarted && permissionGranted && isLocationModeOn
        }

    /**
     * Enables location services for the phone
     */
    override fun enableLocationServices(activity: Activity) {
        googleLocationClient.enableLocationServices(activity)
    }

    /**
     * This method performs a location lookup on address, and returns the results in locationLookup callback.
     */
    override fun locationLookup(address: String, locationLookupCallback: LocationCallback) {

        val permissionGranted = permissionManager.checkPermission(*permissions)

        val lookupAsyncTask = object : AsyncTask<Void, Void, LocationInfo>() {
            override fun doInBackground(vararg voids: Void): LocationInfo? {
                var locationInfo: LocationInfo? = null

                if (!(serviceStarted && permissionGranted)) {
                    logger.log(WARN, "Unable to lookup location information as" + serviceUnavailableMessage)
                } else {
                    val geocoder: Geocoder = dependencyProvider.resolve()

                    try {
                        val addresses = geocoder.getFromLocationName(address, 1)

                        if (addresses != null && addresses.size > 0) {
                            val returnedAddress = addresses[0]
                            locationInfo = LocationInfo(returnedAddress.latitude, returnedAddress.longitude, returnedAddress.getAddressLine(0), returnedAddress.locality, returnedAddress.adminArea, returnedAddress.countryName)
                        }
                    } catch (ex: IOException) {
                        logger.logException(ERROR,"Exception during address lookup", ex)
                    }

                }

                return locationInfo
            }

            override fun onPostExecute(locationInfo: LocationInfo?) {
                super.onPostExecute(locationInfo)
                locationLookupCallback.locationLookupCompleted(locationInfo)
            }
        }

        lookupAsyncTask.execute()
    }

    /**
     * This method performs a reverse location lookup on latitude and longitude, and returns the results in reverseLocationLookup callback.
     */
    override fun reverseLocationLookup(latitude: Double, longitude: Double, reverseLocationLookupCallback: LocationCallback) {

        val permissionGranted = permissionManager.checkPermission(*permissions)

        val reverseLookupAsyncTask = object : AsyncTask<Void, Void, LocationInfo>() {
            override fun doInBackground(vararg voids: Void): LocationInfo? {
                var locationInfo: LocationInfo? = null

                if (!(serviceStarted && permissionGranted)) {
                    logger.log(ERROR, "Unable to lookup address information as" + serviceUnavailableMessage)
                } else {
                    val geocoder: Geocoder = dependencyProvider.resolve()

                    try {
                        val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                        if (addresses != null && addresses.size > 0) {
                            val address = addresses[0]
                            locationInfo = LocationInfo(address.latitude, address.longitude, address.getAddressLine(0), address.locality, address.adminArea, address.countryName)
                        }
                    } catch (ex: IOException) {
                        logger.logException(ERROR, "Exception during reverse address lookup", ex)
                    }

                }

                return locationInfo
            }

            override fun onPostExecute(locationInfo: LocationInfo?) {
                super.onPostExecute(locationInfo)
                reverseLocationLookupCallback.locationLookupCompleted(locationInfo)
            }
        }

        reverseLookupAsyncTask.execute()
    }

    /*
     * This method is part of the location listener interface and is invoked when the device location is changed
     */
    override fun onLocationChanged(location: Location) {
        locationServiceDelegate?.currentLocationUpdated(location.latitude, location.longitude)
    }

    /*
     * This method is part of the location listener interface and is invoked when the provider status has changed.
     */
    override fun onStatusChanged(provider: String, status: Int, bundle: Bundle) {
        logger.log(INFO, String.format("Provider %s status changed to %d.", provider, status))
    }

    /*
     * This method is part of the location listener interface and is invoked when the provider is enabled by the user.
     */
    override fun onProviderEnabled(provider: String) {
        logger.log(INFO, String.format("Provider %s enabled.", provider))
    }

    /*
     * This method is part of the location listener interface and is invoked when the provider is disabled by the user.
     */
    override fun onProviderDisabled(provider: String) {
        logger.log(INFO, String.format("Provider %s disabled.", provider))
    }

    /*
     * This method requests the location manager for location updates
     */
    private fun RequestLocationUpdates() {

        val permissionGranted = permissionManager.checkPermission(*permissions)
        if (!permissionGranted || !serviceStarted) {
            logger.log(WARN, "Unable to subscribe for location updates as" + serviceUnavailableMessage)
            return
        }

        // The minimum time (in milliseconds) the system will wait until checking if the location changed
        val minLocationUpdateTimeInMilliSec: Long = 10000
        // The minimum distance (in meters) traveled before being notified
        val minLocationUpdateDistance = 50f

        // Define the criteria to use
        val criteria = Criteria()
        criteria.powerRequirement = Criteria.POWER_LOW
        criteria.accuracy = Criteria.ACCURACY_COARSE
        criteria.isAltitudeRequired = false
        criteria.isBearingRequired = false
        criteria.isCostAllowed = true
        criteria.isSpeedRequired = false

        // Get the best provider from the criteria specified, and false to say it can turn the provider on if it isn't already
        androidLocationManager?.let { locationManger ->
            val bestProvider = locationManger.getBestProvider(criteria, false)

            // Request location updates
            locationManger.requestLocationUpdates(bestProvider, minLocationUpdateTimeInMilliSec, minLocationUpdateDistance, this)
        }
    }

    /*
     * This method informs the location manager that location updates are no longer required
     */
    private fun CancelLocationUpdates() {

        if (!isAvailable) {
            logger.log(WARN, "Unable to unsubscribe from location updates" + serviceUnavailableMessage)
            return
        }

        androidLocationManager?.removeUpdates(this)
    }

    /**
     * This method is the handler for the notification received when permissions are updated
     * @param message - The permission update message containing collection of permissions updated
     */
    @Suppress("unused")
    @Subscribe
    fun onPermissionUpdatedMessage(message: PermissionUpdateMessage) {
        if (message.hasAnyPermission(*permissions)) {
            val permissionGranted = permissionManager.checkPermission(*permissions)
            if (serviceStarted && permissionGranted) {
                RequestLocationUpdates()
            }
        }
    }

    /**
     * The broadcast receiver that will intents indicating that the list of location providers has changed.
     */
    private inner class LocationStateReceiver : BroadcastReceiver() {
        /**
         * This method is called when the BroadcastReceiver is receiving an Intent
         * broadcast.

         * @param context The Context in which the receiver is running.
         * *
         * @param intent  The Intent being received.
         */
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (action == AndroidLocationManager.PROVIDERS_CHANGED_ACTION) {
                val messenger = DependencyProvider.default.resolve<Messenger>()
                messenger.publish(LocationProvidersChangedMessage())
            }
        }
    }
}
