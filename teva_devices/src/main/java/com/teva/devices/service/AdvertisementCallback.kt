//
// AdvertisementCallback.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.service

import android.bluetooth.BluetoothDevice
import android.support.annotation.UiThread

/**
 * The callback interface used by the Scanner to deliver advertisements to the DeviceService.
 */
@UiThread
interface AdvertisementCallback {
    /**
     * Callback received when an advertisement is received by the scanner and matched to
     * a ConnectionInfo

     * @param connectionInfo The ConnectionInfo of the matched device
     * *
     * @param device         The Bluetooth device that produced the advertisement
     */
    fun onAdvertisement(connectionInfo: ConnectionInfo, device: BluetoothDevice)
}
