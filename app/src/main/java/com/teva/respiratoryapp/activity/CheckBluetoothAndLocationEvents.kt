//
// CheckBluetoothAndLocationEvents.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity

/**
 * Interface implemented by the DashboardActivity to receive requests from the UI to re-check
 * the Bluetooth and Location permissions and enables.
 */
interface CheckBluetoothAndLocationEvents {
    /**
     * Requests that the main activity check that Bluetooth and Location permissions are
     * granted and the services are enabled.
     */
    fun checkBluetoothAndLocationStatus()
}
