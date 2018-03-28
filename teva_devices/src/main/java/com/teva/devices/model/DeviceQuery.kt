//
// DeviceQuery.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.model

import android.support.annotation.WorkerThread

import com.teva.devices.dataquery.DeviceDataQuery
import com.teva.devices.entities.Device

/**
 * Classes conforming to this interface allows access methods to the device data.
 */
@WorkerThread
interface DeviceQuery : DeviceDataQuery {

    /**
     * A value indicating whether the database contains device objects.
     */
    fun getHasDevices(): Boolean

    /**
     * The number of connected devices.
     */
    fun getConnectedDeviceCount(): Int

    /**
     * Marks a device as inactive in the database.
     */
    fun removeDevice(device: Device)

    /**
     * Marks a previous inactive device as active.
     */
    fun undoRemoveDevice(device: Device)
}
