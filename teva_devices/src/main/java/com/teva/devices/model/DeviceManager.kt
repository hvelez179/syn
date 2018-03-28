//
// DeviceManager.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.model

/**
 * Interface for the DeviceManager.
 */
interface DeviceManager {

    /**
     * Starts the DeviceManager processing
     */
    fun start()

    /**
     * Shuts down the DeviceManager processing
     */
    fun stop()

    /**
     * Restarts the device manager, the device service, and updates the list of devices to scan for.
     */
    fun restart()

    /**
     * Returns true if the bluetooth radio is on, false otherwise
     */
    val isBluetoothEnabled: Boolean
}
