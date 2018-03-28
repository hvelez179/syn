//
// AdvertisementFilter.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.service

import java.util.UUID

/**
 * Represents a filter that defines the advertisement data that will uniquely identify a device.
 *
 * @property serviceUUID The id of a BLE service that must be implemented by a matching device.
 * @property name The device name to search for or null to not filter by name.
 * @property manufacturerId The manufacturer id of the manufacturer specific data or null to not
 *                          filter by manufacturer specific data.
 * @property manufacturerData The manufacturer data buffer or null to not filter by manufacturer data.
 * @property connectionInfo The connection information containing a specific serial number to search
 *                          for, or null to match any serial number.
 */
class AdvertisementFilter(
    var serviceUUID: UUID? = null,
    var name: String? = null,
    var manufacturerId: Int? = null,
    var manufacturerData: ByteArray? = null,
    var connectionInfo: ConnectionInfo? = null)

