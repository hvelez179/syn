//
// DeviceServiceCallback.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.service

/**
 * The Calback interface used by the DeviceService to deliver events to the DeviceManager.
 */
interface DeviceServiceCallback {

    /**
     * Indicates that a device has connected.
     */
    fun onConnected(connectionInfo: ConnectionInfo)

    /**
     * Indicates that a device has disconnected.
     */
    fun onDisconnected(connectionInfo: ConnectionInfo)

    /**
     * Indicates that device attributes or inhale events have been updated.
     */
    fun onUpdated(connectionInfo: ConnectionInfo, deviceInfo: DeviceInfo, events: List<InhaleEventInfo>)
}
