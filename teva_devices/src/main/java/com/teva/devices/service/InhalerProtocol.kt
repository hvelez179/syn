//
// InhalerProtocol.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.service

import android.bluetooth.BluetoothDevice

import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.*

import java.util.UUID

/**
 * Protocol class for Inhalers that provides the filters needed to recognize inhalers from
 * Bluetooth advertisements as well as the ability to construct an Inhaler object.
 */
internal class InhalerProtocol(private val dependencyProvider: DependencyProvider) : Protocol {

    /**
     * Convert a serial number string into a 5-byte Big-Endian byte array.
     * The 11 digit serial numbers are represented in the Bluetooth advertisements
     * as a 5 byte integer in big-endian byte-order.
     */
    private fun serialNumberToByteArray(serialNumber: String, buf: ByteArray, startIndex: Int) {
        var number = java.lang.Long.parseLong(serialNumber)
        for (i in 4 downTo 0) {
            buf[startIndex + i] = (number and 0xff).toByte()
            number = number shr 8
        }
    }

    /**
     * Creates an AdvertisementFilter that can be used to identify a specific inhaler from
     * the data contained in it's Bluetooth advertisement.
     *
     * @param connectionInfo The information necessary to identify and connect to an inhaler.
     */
    override fun createFilter(connectionInfo: ConnectionInfo): AdvertisementFilter {

        // Build an AdvertisementFilter based on the connectionInfo object.
        val filter = AdvertisementFilter()
        filter.connectionInfo = connectionInfo

        filter.serviceUUID = InhalerServiceUUID

        // Name for inhaler simulators
        filter.name = "Sim:" + connectionInfo.serialNumber

        // Manufacturer Data for real inhalers
        filter.manufacturerId = ManufacturerId

        // convert the serial number into the byte array form that it will take in
        // a Bluetooth advertisement.
        val data = ByteArray(5)
        serialNumberToByteArray(connectionInfo.serialNumber, data, 0)
        filter.manufacturerData = data

        return filter
    }

    /**
     * Constructs an MedicalDispenser for the specified ConnectionInfo and BluetoothDevice.
     */
    override fun createMedicalDispenser(connectionInfo: ConnectionInfo, bluetoothDevice: BluetoothDevice): MedicationDispenser? {
        var inhaler: MedicationDispenserImpl? = null

        try {
            inhaler = MedicationDispenserImpl(dependencyProvider, connectionInfo, bluetoothDevice)
        } catch (e: Exception) {
            // This exception is handled by returning a null MedicationDispenser from this method.
            logger.logException(ERROR, "Exception while creating inhaler", e)
        }

        return inhaler
    }

    /**
     * Checks if the MedicationDispenser represents the specified BluetoothDevice.
     * @param medicationDispenser The MedicationDispenser to test.
     * @param bluetoothDevice The BluetoothDevice to test.
     * @return True if the BluetoothDevice is the one that the MedicationDispenser represents, false otherwise.
     */
    override fun isSameDevice(medicationDispenser: MedicationDispenser, bluetoothDevice: BluetoothDevice): Boolean {
        if (medicationDispenser is MedicationDispenserImpl) {

            return medicationDispenser.bluetoothDevice.address == bluetoothDevice.address
        }

        return false
    }

    companion object {
        private val logger = Logger(InhalerProtocol::class)

        val InhalerServiceUUID = UUID.fromString("f429de80-c342-11e4-9da5-0002a5d5c51b")
        val SimulatorMarkerServiceUUID = UUID.fromString("000018ff-0000-1000-8000-00805f9b34fb")
        val ManufacturerId = 0x02CE
    }
}
