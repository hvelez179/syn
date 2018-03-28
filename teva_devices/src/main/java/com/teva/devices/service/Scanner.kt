//
// Scanner.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.service

import android.support.annotation.UiThread

/**
 * Interface for objects that scan bluetooth advertisements to discover MedicalDispensers.
 */
@UiThread
internal interface Scanner {

    /**
     * Sets the list of devices the scanner should search for.
     */
    fun setConnectionInfo(connectionInfoList: List<ConnectionInfo>)

    /**
     * Sets a value indicating whether the app is in the foreground and should use a higher
     * power scanning cycle.
     */
    fun setInForeground(inForeground: Boolean)

    /**
     * Sets the advertisement callback for the scanner.
     */
    fun setAdvertisementCallback(advertisementCallback: AdvertisementCallback)

    /**
     * Starts scanning for Bluetooth peripherals that match the specified services.
     */
    fun startScanning()

    /**
     * Stops scanning for Bluetooth peripherals.
     */
    fun stopScanning()
}
