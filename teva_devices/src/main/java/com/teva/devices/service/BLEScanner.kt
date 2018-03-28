//
// BLEScanner.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.service

import android.Manifest
import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.AsyncTask
import android.os.Build
import android.os.ParcelUuid
import android.support.annotation.BinderThread
import android.support.annotation.MainThread
import com.teva.common.messages.PermissionUpdateMessage
import com.teva.common.services.PermissionManager
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.*
import com.teva.common.utilities.MessageHandler
import com.teva.common.utilities.Messenger
import org.greenrobot.eventbus.Subscribe
import java.util.*
import android.bluetooth.le.ScanCallback as AndroidScanCallback

/**
 * Scanner implementation that uses the pre-lollipop Bluetooth APIs to scan for devices.
 * Manually implements the filters.
 */
@MainThread
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
internal class BLEScanner(private val dependencyProvider: DependencyProvider) : Scanner, MessageHandler.MessageListener {
    private val logger = Logger(BLEScanner::class)
    private val handler: MessageHandler = MessageHandler(this)
    private val permissionManager: PermissionManager = dependencyProvider.resolve()

    private val filters = ArrayList<AdvertisementFilter>()
    private val coalescingCache = HashSet<String>()

    private val lock = Any()

    private var advertisementCallback: AdvertisementCallback? = null

    private val scanCallback = ScanCallback()

    private val protocolFactory: ProtocolFactory = dependencyProvider.resolve()

    private var inForeground: Boolean = false
    private var isEnabled: Boolean = false

    private val advertisementList = ArrayList<Pair<ConnectionInfo, BluetoothDevice>>()

    private var currentConnectInfoList: List<ConnectionInfo>? = null

    /**
     * Sets the list of devices the scanner should search for.
     */
    override fun setConnectionInfo(connectionInfoList: List<ConnectionInfo>) {

        if( currentConnectInfoList != null && connectionInfoList.isNotEmpty()) {
            var allItemsFound = true

            if(currentConnectInfoList?.size != connectionInfoList.size) {
                allItemsFound = false
            } else {
                for (info in connectionInfoList) {
                    if (!currentConnectInfoList!!.any { obj -> obj.serialNumber == info.serialNumber }) {
                        allItemsFound = false
                        break
                    }
                }
            }

            // if the new list is a subset of the current scan list, return without restarting the scan.
            // Some of the medication dispensers might require a new advertisement due the two-minute interval
            // expiration so clear the coalescing cache so that the advertisements are forwarded again.
            if(allItemsFound) {
                coalescingCache.clear()
                logger.log(INFO, "All connection info objects are included in current scan. New scan will not be started.")
                return
            }
        }

        if(connectionInfoList.isEmpty()) {
            logger.log(INFO, "No connection info objects in the new list.")
        }

        synchronized(lock) {
            currentConnectInfoList = connectionInfoList
            filters.clear()
            val protocolMap = protocolFactory.createProtocols(connectionInfoList)

            for (connectionInfo in connectionInfoList) {
                protocolMap[connectionInfo.protocolType]?.let { protocol ->
                    filters.add(protocol.createFilter(connectionInfo))
                }
            }
        }

        applyScanMode()
    }

    /**
     * Sets a value indicating whether the app is in the foreground and should use a higher
     * power scanning cycle.
     */
    override fun setInForeground(newInForeground: Boolean) {
        logger.log(DEBUG, "setInForeground(): " + newInForeground)
        inForeground = newInForeground

        applyScanMode()
    }

    /**
     * Starts scanning for Bluetooth peripherals that match the specified services.
     */
    override fun startScanning() {
        logger.log(VERBOSE, "startScanning()")
        if (!isEnabled) {
            isEnabled = true
            applyScanMode()

            dependencyProvider.resolve<Messenger>().subscribe(this)
        }
    }

    /**
     * Stops scanning for Bluetooth peripherals.
     */
    override fun stopScanning() {
        logger.log(VERBOSE, "stopScanning()")
        if (isEnabled) {
            isEnabled = false
            applyScanMode()

            dependencyProvider.resolve<Messenger>().unsubscribeToAll(this)
        }
    }

    /**
     * Updates the scanning mode/state based on the filter list and inForground status
     */
    private fun applyScanMode() {
        logger.log(VERBOSE, "applyScanMode()")
        val hasPermission = permissionManager.checkPermission(*BluetoothPermissions)

        if (hasPermission) {
            val task = ScanSetupTask()
            task.execute()
        }
    }

    /**
     * Message handler for the PermissionUpdateMessage that is sent when
     * new permissions are granted.

     * @param message The message received.
     */
    @Subscribe
    fun onPermissionUpdatedMessage(message: PermissionUpdateMessage) {
        if (message.hasAnyPermission(*BluetoothPermissions)) {
            applyScanMode()
        }
    }

    /**
     * Sets the advertisement callback for the scanner.
     */
    override fun setAdvertisementCallback(advertisementCallback: AdvertisementCallback) {
        this.advertisementCallback = advertisementCallback
    }

    /**
     * Callback used by the MessageHandler to report a posted message.
     */
    override fun onMessage(message: Int) {
        if (message == ADVERTISEMENT_MESSAGE && advertisementCallback != null) {
            val adverts = ArrayList<Pair<ConnectionInfo, BluetoothDevice>>()

            synchronized(lock) {
                adverts.addAll(advertisementList)
                advertisementList.clear()
            }

            for (pair in adverts) {
                advertisementCallback!!.onAdvertisement(pair.first, pair.second)
            }
        }
    }

    /**
     * AsyncTask to start and stop scanning because calling startLeScan() can
     * sometimes take a second to complete.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private inner class ScanSetupTask : AsyncTask<Void, Void, Void>() {
        /**
         * Method that runs on the worker thread
         */
        @TargetApi(Build.VERSION_CODES.M)
        override fun doInBackground(vararg params: Void): Void? {
            val scanFilters = ArrayList<ScanFilter>()

            // only create filters if the scanner is enabled.
            if (isEnabled) {
                synchronized(lock) {
                    // clear the coalescing cache that filters out duplicate advertisements
                    coalescingCache.clear()

                    if (filters.size > 0) {
                        if (filters.size <= MAX_FILTERS) {
                            // build ScanFilter objects from the AdvertismentFilter objects
                            for (filter in filters) {
                                val builder = ScanFilter.Builder()
                                        .setServiceUuid(ParcelUuid(filter.serviceUUID))

                                if (filter.manufacturerData != null) {
                                    builder.setManufacturerData(filter.manufacturerId!!, filter.manufacturerData)
                                }
                                scanFilters.add(builder.build())
                            }

                            // set up filter for all Simulator devices
                            val simulatorFilter = ScanFilter.Builder()
                                    .setServiceUuid(ParcelUuid(InhalerProtocol.InhalerServiceUUID))
                                    .setServiceUuid(ParcelUuid(InhalerProtocol.SimulatorMarkerServiceUUID))
                                    .build()
                            scanFilters.add(simulatorFilter)
                        } else {
                            // too many filters, so just add a single filter for all devices with the inhaler service.
                            val filter = ScanFilter.Builder()
                                    .setServiceUuid(ParcelUuid(InhalerProtocol.InhalerServiceUUID))
                                    .build()
                            scanFilters.add(filter)
                        }
                    }
                }
            }

            val bluetoothManager: BluetoothManager = dependencyProvider.resolve()
            val bluetoothLeScanner = bluetoothManager.adapter.bluetoothLeScanner

            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter.isEnabled && bluetoothLeScanner != null) {
                try {
                    if (scanFilters.size > 0) {
                        // We have scan filters, so start scanning
                        var settingsBuilder = ScanSettings.Builder()
                                .setScanMode(if (inForeground) ScanSettings.SCAN_MODE_LOW_LATENCY else ScanSettings.SCAN_MODE_BALANCED)
                                .setReportDelay(SCAN_REPORT_DELAY)

                        settingsBuilder = settingsBuilder.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)

                        logger.log(DEBUG, "ScanSetupTask: Starting scan with %d filters", scanFilters.size)
                        // Change in scan settings(example - low power to low latency mode) does not
                        // take effect until the scan is stopped and restarted. So stop the scan and start it.
                        bluetoothLeScanner.stopScan(scanCallback)
                        bluetoothLeScanner.startScan(scanFilters, settingsBuilder.build(), scanCallback)
                    } else {
                        // No scan filters, so stop scanning
                        logger.log(DEBUG, "ScanSetupTask: No filters, stoping scan", scanFilters.size)
                        bluetoothLeScanner.stopScan(scanCallback)
                    }
                } catch (ex: IllegalStateException) {
                    logger.log(WARN, "Exception while starting or stopping the scanner")

                    // The BluetoothLEScanner might throw an exception if the bluetooth radio is
                    // not enabled. Even though the code checks the current state before calling,
                    // the exception could still occur.  However, it's possible that this
                    // exception could be caused by some unknown condition not relating to
                    // the bluetooth state.
                    //
                    // So, if we now check if the bluetooth is enabled and it isn't, then
                    // we can assume that was the cause of the exception, and we can just ignore
                    // it, because we would have skipped starting the scan if isBluetoothEnabled()
                    // had returned true before we called startScan() or stopScan().  Other
                    // parts of the app will notify hte user that the bluetooth is not enabled,
                    // and the DeviceServiceImpl will restart scanning when the bluetooth is
                    // re-enabled.
                    //
                    // But, if the bluetooth is enabled, then something else happened and we will
                    // rethrow the exception.
                    if (bluetoothAdapter.isEnabled) {
                        throw ex
                    }
                }

            }

            return null
        }
    }

    /**
     * Callback interface for the BluetoothLeScanner
     */
    @BinderThread
    private inner class ScanCallback : AndroidScanCallback() {
        /**
         * Called when an advertisement is received.
         */
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            processScanResult(result)
        }

        /**
         * Called when advertisements are received in batch mode.
         */
        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)

            for (scanResult in results) {
                processScanResult(scanResult)
            }
        }

        /**
         * Called when scanning fails.
         */
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)

            if (errorCode == AndroidScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED) {
                logger.log(ERROR, "onScanFailed: Scanning failed because one of the" +
                        " requested features is unsupported - errorCode = " + errorCode)
            }
        }

        /**
         * Processes a ScanResult and matches it to a ConnectionInfo.
         */
        private fun processScanResult(scanResult: ScanResult) {

            val scanRecord = scanResult.scanRecord
            if (scanRecord != null) {
                // check if we've seen this device already during the current scan cycle.
                val device = scanResult.device
                val cacheKey = if (scanRecord.deviceName != null)
                    device.address + ":" + scanRecord.deviceName
                else
                    device.address

                synchronized(lock) {
                    if (coalescingCache.contains(cacheKey)) {
                        logger.log(VERBOSE, "Filtering out advertisement for device that has already been seen. $cacheKey")
                        return
                    }

                    coalescingCache.add(cacheKey)
                }

                // build an Advertisement object that will be used by the Protocol to match
                // the scan record to a ConnectionInfo object.

                val name = scanRecord.deviceName
                val serviceUUIDs = HashSet<UUID>()

                for (parcelUuid in scanRecord.serviceUuids) {
                    serviceUUIDs.add(parcelUuid.uuid)
                }

                var manufacturerId: Int? = null
                var manufacturerData: ByteArray? = null
                if (scanRecord.manufacturerSpecificData.size() > 0) {
                    manufacturerId = scanRecord.manufacturerSpecificData.keyAt(0)
                    manufacturerData = scanRecord.getManufacturerSpecificData(manufacturerId)
                }

                // Debug logging of the advertisement
                // Check the log level first to avoid spending time
                // building a log string if the DEBUG level isn't enabled
                if (logger.isEnabled(Logger.Level.DEBUG)) {
                    val message = StringBuilder()
                    message.append("AdvertisementReceived:  ")
                            .append(Logger.toHexString(scanRecord.bytes, 0, scanRecord.bytes.size))
                            .append("\n")

                    for (uuid in serviceUUIDs) {
                        message.append(uuid.toString())
                                .append(" ")
                    }

                    if (name != null) {
                        message.append(name)
                                .append(" ")
                    }

                    if (manufacturerData != null) {
                        message.append(Integer.toHexString(manufacturerId!!))
                                .append(" ")
                                .append(Logger.toHexString(manufacturerData))
                    }

                    logger.log(DEBUG, message.toString())
                }

                var matchedFilter: AdvertisementFilter? = null
                synchronized(lock) {
                    // We got this far because one of the filters was matched by the OS.
                    // Now find the AdvertisementFilter that was matched.
                    for (filter in filters) {
                        val matchesServiceId = serviceUUIDs.contains(filter.serviceUUID)

                        val matchesManufacturerId = filter.manufacturerId == null || filter.manufacturerId == manufacturerId

                        val matchesManufacturerData = matchesManufacturerId
                                && manufacturerData != null
                                && Arrays.equals(manufacturerData, filter.manufacturerData)

                        val matchesName = name != null && name == filter.name

                        if (matchesServiceId && (matchesManufacturerData || matchesName)) {

                            logger.log(DEBUG, "Matched filter")
                            matchedFilter = filter
                            break
                        }
                    }
                }

                if (matchedFilter != null) {
                    val matchedConnectionInfo = matchedFilter!!.connectionInfo
                    logger.log(DEBUG, "Found device advertisement: " + matchedConnectionInfo!!.serialNumber)

                    synchronized(lock) {
                        advertisementList.add(Pair(matchedConnectionInfo, device))
                    }
                    handler.sendMessageIfNotQueued(ADVERTISEMENT_MESSAGE)
                } else {
                    logger.log(WARN, "Failed to match a recognized advertisement to a filter")
                }
            }
        }
    }

    companion object {
        private val ADVERTISEMENT_MESSAGE = 1

        private val MAX_FILTERS = 6

        private val SCAN_REPORT_DELAY: Long = 0

        private val BluetoothPermissions = arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    }
}
