//
// MedicationDispenser.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.service

/**
 * Interface for Medical Dispensers with the common methods
 */
interface MedicationDispenser {

    /**
     * The ConnectionInfo for the MedicationDispenser.
     */
    val connectionInfo: ConnectionInfo

    /**
     * Requests that the MedicationDispenser connect to the physical device.
     */
    fun connect()

    /**
     * Requests that the MedicationDispenser disconnect from the physical device.
     */
    fun disconnect()

    /**
     * Sets the callback interface used to relay events from the physical device.
     *
     * @param callback The callback used to inform the DeviceService when a device
     * is connected, disconnected, or updated.
     */
    fun setCallback(callback: MedicationDispenserCallback)

}
