//
// ConnectionInfo.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.service

/**
 * Data needed by the BluetoothService to discover and connect to an inhaler.
 *
 * @property serialNumber The serial number of the device to connect to.
 * @property authenticationKey The authentication key of the device to connect to.
 * @property protocolType The type of inhaler to connect to.
 * @property lastRecordId The id of the last inhalation event that was retrieved from the inhaler.
 */
class ConnectionInfo(
        var serialNumber: String = "",
        var authenticationKey: String = "",
        var protocolType: ProtocolType = ProtocolType.Inhaler,
        var lastRecordId: Int = 0)
