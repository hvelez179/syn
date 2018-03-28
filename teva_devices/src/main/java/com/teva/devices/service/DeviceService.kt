//
// DeviceService.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.service

import android.support.annotation.UiThread

/**
 * Device Service interface
 */
@UiThread
interface DeviceService {

    /**
     * Starts the device service
     */
    fun start()

    /**
     * Shuts down the device service
     */
    fun stop()

    /**
     * Notifies that DeviceService that the app is running in the foregroud.

     * @param inForeground True if the app is running in the foreground, false otherwise.
     */
    fun setInForeground(inForeground: Boolean)

    /**
     * Sets the list of devices to be connected to.
     */
    fun setConnectionInfo(connectionInfoList: List<ConnectionInfo>)

    /**
     * Sets the callback that commuicates device connections and updates.
     */
    fun setCallback(callback: DeviceServiceCallback)

    /**
     * Returns true if the bluetooth radio is on, false otherwise
     */
    val isBluetoothEnabled: Boolean
}
