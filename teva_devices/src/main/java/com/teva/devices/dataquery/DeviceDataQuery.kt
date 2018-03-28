//
// DeviceDataQuery.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.dataquery

import android.support.annotation.WorkerThread

import com.teva.common.dataquery.DataQueryForTrackedModels
import com.teva.devices.entities.Device

@WorkerThread
interface DeviceDataQuery : DataQueryForTrackedModels<Device> {

    /**
     * Gets all active devices.
     */
    fun getAllActive(): List<Device>

    /**
     * Gets the device with the given serial number.
     */
    fun get(serialNumber: String): Device?

    /**
     * Checks if there are data in the data store
     */
    fun hasData(): Boolean

    /**
     * Gets the last connected active controller.
     */
    fun lastConnectedActiveController(): Device?

    /**
     * Gets the last connected active reliever.
     */
    fun lastConnectedActiveReliever(): Device?

    /**
     * Marks the device as deleted. This will de-activate and disconnect with the device.
     */
    fun markAsDeleted(device: Device)

    /**
     * Restore a previously de-activated device. Marks device.isActive = true
     */
    fun undoMarkAsDeleted(device: Device)

    /**
     * Get a device by nickname.
     */
    fun has(nickname: String): Boolean

    /**
     * Checks if there are active devices.
     */
    fun hasActiveDevices(): Boolean
}
