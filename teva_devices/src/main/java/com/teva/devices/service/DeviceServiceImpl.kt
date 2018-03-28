//
// DeviceServiceImpl.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.service

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.annotation.UiThread
import com.teva.common.messages.SystemMonitorMessage


import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.*
import com.teva.common.utilities.Messenger
import com.teva.devices.enumerations.DeviceActivity
import com.teva.devices.messages.BluetoothStateChangedMessage

import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet

/**
 * The concrete implementation of the DeviceService.
 */
@UiThread
class DeviceServiceImpl(private val dependencyProvider: DependencyProvider)
    : DeviceService, AdvertisementCallback, MedicationDispenserCallback {

    private val logger = Logger(DeviceServiceImpl::class)
    private val scanner: Scanner = dependencyProvider.resolve()
    private val protocolFactory: ProtocolFactory = dependencyProvider.resolve()
    private val medicationDispenserMap = HashMap<String, MedicationDispenser>()
    private var connectionInfoList: List<ConnectionInfo>? = null
    private var callback: DeviceServiceCallback? = null
    private var isEnabled: Boolean = false
    private var isStarted: Boolean = false
    private val connectedDevices = HashSet<String>()

    private val bluetoothStateReceiver = BluetoothStateReceiver()

    init {
        isEnabled = isBluetoothEnabled

        scanner.setAdvertisementCallback(this)
    }

    /**
     * Starts the device service
     */
    override fun start() {
        logger.log(VERBOSE, "start()")
        isStarted = true
        if (isEnabled) {
            scanner.startScanning()
        }

        // Start the broadcast receiver that monitors for changes to the Bluetooth enable state
        val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        dependencyProvider.resolve<Context>().registerReceiver(bluetoothStateReceiver, intentFilter)

    }

    /**
     * Shuts down the device service
     */
    override fun stop() {
        logger.log(VERBOSE, "stop()")
        isStarted = false
        scanner.stopScanning()

        disconnectAllDevices()

        // Stop the broadcast receiver that monitors for changes to the Bluetooth enable state
        dependencyProvider.resolve<Context>().unregisterReceiver(bluetoothStateReceiver)
    }

    /**
     * Returns true if the bluetooth radio is on, false otherwise
     */
    override val isBluetoothEnabled: Boolean
        get() {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

            return bluetoothAdapter != null && bluetoothAdapter.isEnabled
        }

    /**
     * Method called by the broadcast receiver when the Bluetooth adapter state changes.
     *
     * @param isEnabled True if the Bluetooth adapter is enabled, false otherwise.
     */
    private fun onBluetoothStateChanged(isEnabled: Boolean) {
        logger.log(VERBOSE, "onBluetoothStateChanged()")
        this.isEnabled = isEnabled

        if (isEnabled && isStarted) {
            scanner.startScanning()
        } else {
            disconnectAllDevices()
            scanner.stopScanning()
        }
    }

    /**
     * Indicates that the app is running in the foreground.
     */
    override fun setInForeground(inForeground: Boolean) {
        scanner.setInForeground(inForeground)
    }

    /**
     * Sets the list of devices to be connected to.
     */
    override fun setConnectionInfo(connectionInfoList: List<ConnectionInfo>) {
        this.connectionInfoList = connectionInfoList
        syncConnectionList()
    }

    /**
     * Sets the callback that commuicates device connections and updates.
     */
    override fun setCallback(callback: DeviceServiceCallback) {
        this.callback = callback
    }

    /**
     * Callback method where advertisements from the scanner are delivered.
     */
    override fun onAdvertisement(connectionInfo: ConnectionInfo, device: BluetoothDevice) {
        logger.log(DEBUG, "onAdvertisement(): " + connectionInfo.serialNumber)

        // make sure aren't already connected to or in the process of connecting to
        // an inhaler with the same serial number.
        if (!medicationDispenserMap.containsKey(connectionInfo.serialNumber)) {

            // The simulator takes some time to disconnect when the user changes the serial
            // number of the simulator.  So to guard against trying to connect to it again
            // while we are still connected it, we need to check if the list of connected
            // MedicationDispensers contains an entry with the same physical bluetooth device.
            // If we find it, then we will simply ignore this advertisement.  When the device
            // eventually disconnects, the scanning will be restarted and we will see the
            // advertisement again.
            var alreadyConnectedToSimulator = false
            for (medicationDispenser in medicationDispenserMap.values) {
                // Use the ConnectionInfo to get the protocol object for the current device.
                val currentConnectionInfo = medicationDispenser.connectionInfo
                val currentProtocol = protocolFactory.createProtocol(currentConnectionInfo.protocolType)

                // Use the protocol object to determine if the current MedicationDispenser
                // represents the same physical bluetooth device as the new advertisement.
                if (currentProtocol.isSameDevice(medicationDispenser, device)) {
                    logger.log(DEBUG, "Already connected to simulator ${currentConnectionInfo.serialNumber}, " +
                            "so wait for it to disconnect before connecting to ${connectionInfo.serialNumber}")
                    alreadyConnectedToSimulator = true
                    break
                }
            }

            // Only process this advertisement if we have ensured that it's not from
            // a bluetooth device that we are already connected to.
            if (!alreadyConnectedToSimulator) {
                val protocol = protocolFactory.createProtocol(connectionInfo.protocolType)

                val medicationDispenser = protocol.createMedicalDispenser(connectionInfo, device)
                if (medicationDispenser != null) {
                    medicationDispenserMap.put(connectionInfo.serialNumber, medicationDispenser)
                    medicationDispenser.setCallback(this)
                    medicationDispenser.connect()
                }
            }
        }
    }

    /**
     * Callback method from the MedicationDispenser device indicating that it has connected.
     */
    override fun onConnected(connectionInfo: ConnectionInfo) {
        logger.log(INFO, "OnConnected: " + connectionInfo.serialNumber)

        if (callback != null) {
            callback!!.onConnected(connectionInfo)
        }

        connectedDevices.add(connectionInfo.serialNumber)
        syncConnectionList()
        dependencyProvider.resolve<Messenger>().post(SystemMonitorMessage(DeviceActivity.Pairing(true, connectedDevices.size)))
    }

    /**
     * Callback method from the MedicationDispenser device indicating that it has disconnected.
     */
    override fun onDisconnected(connectionInfo: ConnectionInfo) {
        logger.log(INFO, "onDisconnected: " + connectionInfo.serialNumber)
        medicationDispenserMap.remove(connectionInfo.serialNumber)

        if (callback != null) {
            callback!!.onDisconnected(connectionInfo)
        }

        connectedDevices.remove(connectionInfo.serialNumber)
        syncConnectionList()
        //Todo - check the possibility of getting the failed to connect status
        dependencyProvider.resolve<Messenger>().post(SystemMonitorMessage(DeviceActivity.Pairing(false, connectedDevices.size)))
    }

    /**
     * Callback method from the MedicationDispenser device indicating that it's attributes or
     * inhale list has been updated.
     */
    override fun onUpdated(connectionInfo: ConnectionInfo, deviceInfo: DeviceInfo, events: List<InhaleEventInfo>) {
        logger.log(INFO, "onUpdated: ${connectionInfo.serialNumber} dosesTaken: ${deviceInfo.dosesTaken} events.size(): ${events.size}")

        if (callback != null) {
            callback!!.onUpdated(connectionInfo, deviceInfo, events)
        }
    }

    /**
     * Disconnects all of the currently connected devices.
     */
    private fun disconnectAllDevices() {
        val keys = ArrayList(medicationDispenserMap.keys)
        for (serialNumber in keys) {
            val medicationDispenser = medicationDispenserMap[serialNumber]
            medicationDispenser?.disconnect()

            if(medicationDispenser != null) {
                callback?.onDisconnected(medicationDispenser.connectionInfo)
            }
        }

        medicationDispenserMap.clear()
    }

    /**
     * Disconnects any devices that are not contained in the connectionInfoList and
     * updates the scanner with a list with the currently connected devices removed.
     */
    private fun syncConnectionList() {
        // Look for connected dispensers that are no longer in the ConnectionInfo list
        val connectionInfoSerialNumbers = HashSet<String>()
        for (connectionInfo in connectionInfoList!!) {
            connectionInfoSerialNumbers.add(connectionInfo.serialNumber)
        }

        val keys = ArrayList(medicationDispenserMap.keys)
        for (serialNumber in keys) {
            if (!connectionInfoSerialNumbers.contains(serialNumber)) {
                // connected dispenser is not in the current ConnectionInfo list, so disconnect
                medicationDispenserMap[serialNumber]?.disconnect()
                medicationDispenserMap.remove(serialNumber)
            }
        }

        val notAllDevicesConnected: Boolean = connectionInfoList?.any { !connectedDevices.contains(it.serialNumber)} ?: false

        // if all inhalers are connected, send the empty list else send the complete inhaler list
        scanner.setConnectionInfo( if(notAllDevicesConnected) connectionInfoList!! else ArrayList<ConnectionInfo>())
    }

    /**
     * The broadcast receiver that will receive the alarm intents from the OS.
     */
    private inner class BluetoothStateReceiver : BroadcastReceiver() {
        /**
         * This method is called when the BroadcastReceiver is receiving an Intent
         * broadcast.
         *
         * @param context The Context in which the receiver is running.
         * @param intent  The Intent being received.
         */
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val messenger = DependencyProvider.default.resolve<Messenger>()
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        onBluetoothStateChanged(false)
                        messenger.publish(BluetoothStateChangedMessage(false))
                    }

                    BluetoothAdapter.STATE_ON -> {
                        onBluetoothStateChanged(true)
                        messenger.publish(BluetoothStateChangedMessage(true))
                    }
                }
            }
        }
    }
}
