//
// Protocol.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.service

import android.bluetooth.BluetoothDevice
import android.support.annotation.MainThread
import android.support.annotation.UiThread

/**
 * Interface for Protocols that are responsible for indentifying and constructing
 * MedicalDispenser objects for different types of devices.
 */
@MainThread
internal interface Protocol {

    /**
     * Creates an AdvertisementFilter that can be used to identify a specific inhaler from
     * the data contained in it's Bluetooth advertisement.

     * @param connectionInfo The information necessary to identify and connect to an inhaler.
     */
    fun createFilter(connectionInfo: ConnectionInfo): AdvertisementFilter

    /**
     * Constructs an MedicalDispenser for the specified ConnectionInfo and BluetoothDevice.
     */
    fun createMedicalDispenser(connectionInfo: ConnectionInfo, bluetoothDevice: BluetoothDevice): MedicationDispenser?

    /**
     * Checks if the MedicationDispenser represents the specified BluetoothDevice.
     * @param medicationDispenser The MedicationDispenser to test.
     * *
     * @param bluetoothDevice The BluetoothDevice to test.
     * *
     * @return True if the BluetoothDevice is the one that the MedicationDispenser represents, false otherwise.
     */
    fun isSameDevice(medicationDispenser: MedicationDispenser, bluetoothDevice: BluetoothDevice): Boolean
}
