//
// MedicationDispenserCallback.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.service

import android.support.annotation.MainThread

/**
 * Callback interface used by the MedicationDispenser to inform the DeviceService when a device
 * is connected, disconnected, or updated.
 */
@MainThread
interface MedicationDispenserCallback {
    /**
     * Method called when the MedicalDispenser has connected to the device.
     * @param connectionInfo The information describing the device to connect to.
     */
    fun onConnected(connectionInfo: ConnectionInfo)

    /**
     * Method called when the MedicalDispenser has disconnected from the device.
     * @param connectionInfo The information describing the device to connect to.
     */
    fun onDisconnected(connectionInfo: ConnectionInfo)

    /**
     * Method called when the MedicalDispenser has received updated data from the device.
     *
     * @param connectionInfo The information describing the device to connect to.
     * @param deviceInfo The device information retrieved from the device.
     * @param events The list of events read from the device.
     */
    fun onUpdated(connectionInfo: ConnectionInfo, deviceInfo: DeviceInfo, events: List<InhaleEventInfo>)
}
