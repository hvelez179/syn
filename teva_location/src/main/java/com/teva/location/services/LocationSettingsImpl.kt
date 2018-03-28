///
// LocationSettingsImpl.kt
// teva_location
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.location.services

import android.content.Context
import android.provider.Settings

import com.teva.utilities.services.DependencyProvider

/**
 * Abstraction around the system location settings.
 */
class LocationSettingsImpl(dependencyProvider: DependencyProvider) : LocationSettings {
    private val context: Context = dependencyProvider.resolve<Context>()

    /**
     * Checks if the location setting is turned on.
     */
    override val isLocationModeOn: Boolean
        get() {
            val locationMode: Int

            try {
                locationMode = Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
            } catch (e: Settings.SettingNotFoundException) {
                return false
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF
        }
}
