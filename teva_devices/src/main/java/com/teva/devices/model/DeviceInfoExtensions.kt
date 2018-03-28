//
// DeviceInfoExtensions.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.model

import com.teva.devices.entities.Device
import com.teva.devices.service.DeviceInfo

/**
 * Updates a Device object with the information contained in a DeviceInfo object.
 *
 * @param device The Device object ot update.
 */
fun DeviceInfo.updateDevice(device: Device) {
    // The device service won't read the static attributes such as the ManufacturerName
    // if it knows they've already been read from the device. If it did not read them,
    // they will be null.

    manufacturerName?.let { device.manufacturerName = it }
    softwareRevision?.let { device.softwareRevision = it }
    hardwareRevision?.let { device.hardwareRevision = it }
    dateCode?.let { device.dateCode = it }
    lotCode?.let { device.lotCode = it }

    device.lastRecordId = lastRecordId
    device.remainingDoseCount = Math.max(0, device.doseCount - dosesTaken)
}
