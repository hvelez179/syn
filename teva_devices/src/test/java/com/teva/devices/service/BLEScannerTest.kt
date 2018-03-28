////
//// BLEScannerTest.java
//// teva_devices
////
//// Copyright (c) 2017 Teva. All rights reserved.
////
//
//package com.teva.devices.service
//
//import android.Manifest
//import android.annotation.TargetApi
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothDevice
//import android.bluetooth.BluetoothManager
//import android.bluetooth.le.*
//import android.bluetooth.le.ScanSettings.CALLBACK_TYPE_FIRST_MATCH
//import android.os.Build
//import android.os.ParcelUuid
//import android.util.SparseArray
//import com.nhaarman.mockito_kotlin.*
//import com.teva.common.messages.PermissionUpdateMessage
//import com.teva.common.services.PermissionManager
//import com.teva.utilities.services.DependencyProvider
//import com.teva.common.utilities.Messenger
//import com.teva.devices.mocks.HandlerHelper
//import junit.framework.Assert.assertEquals
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.powermock.api.mockito.PowerMockito
//import org.powermock.core.classloader.annotations.PrepareForTest
//import org.powermock.modules.junit4.PowerMockRunner
//import java.util.*
//
///**
// * This class defines the unit tests for the BLEScanner class
// */
//@TargetApi(Build.VERSION_CODES.LOLLIPOP)
//@RunWith(PowerMockRunner::class)
//@PrepareForTest(BluetoothAdapter::class, BLEScanner::class)
//class BLEScannerTest {
//    private val SERIAL_NUMBER2 = "12345678902"
//
//    private var dependencyProvider: DependencyProvider = DependencyProvider.default
//    private var permissionManager: PermissionManager = mock()
//    private var bluetoothManager: BluetoothManager = mock()
//    private var bluetoothAdapter: BluetoothAdapter = mock()
//    private var bluetoothLeScanner: BluetoothLeScanner = mock()
//    private val scanCallback: ScanCallback? = null
//    private var messenger: Messenger = mock()
//    private var specificScanFilterBuilder: ScanFilter.Builder = mock()
//    private var genericScanFilterBuilder: ScanFilter.Builder = mock()
//    private var specificScanFilter: ScanFilter = mock()
//    private var genericScanFilter: ScanFilter = mock()
//    private var specificParcelUuid: ParcelUuid = mock()
//    private var genericParcelUuid: ParcelUuid = mock()
//
//    private var scanSettingsBuilder: ScanSettings.Builder = mock()
//    private var scanSettings: ScanSettings = mock()
//
////    @Captor
////    internal var scanFiltersArgumentCaptor: ArgumentCaptor<List<ScanFilter>>? = null
//
//    internal var connectionInfoList: List<ConnectionInfo> = createTwoConnectionInfoObjects()
//    internal var bluetoothDeviceList: List<BluetoothDevice> = createTwoBluetoothDevices()
//    internal var scanResultList: List<ScanResult> = createTwoScanResults()
//
//    /**
//     * This method sets up the mock classes and methods required for test execution.
//
//     * @throws Exception -  an exception is thrown if constructor mocking fails.
//     */
//    @Before
//    @Throws(Exception::class)
//    fun setup() {
//        DependencyProvider.default.unregisterAll()
//        HandlerHelper.clearQueue()
//
//        dependencyProvider.register(PermissionManager::class, permissionManager)
//        dependencyProvider.register(BluetoothManager::class, bluetoothManager)
//        dependencyProvider.register(Messenger::class, messenger)
//        dependencyProvider.register(ProtocolFactory::class, ProtocolFactoryImpl(dependencyProvider))
//
//        whenever(bluetoothAdapter.bluetoothLeScanner).thenReturn(bluetoothLeScanner)
//        whenever(bluetoothAdapter.isEnabled).thenReturn(true)
//        whenever(bluetoothManager.adapter).thenReturn(bluetoothAdapter)
//        whenever(permissionManager.checkPermission(any())).thenReturn(true)
//        PowerMockito.whenNew(ParcelUuid::class.java).withArguments(eq(InhalerProtocol.InhalerServiceUUID)).thenReturn(specificParcelUuid)
//        PowerMockito.whenNew(ParcelUuid::class.java).withArguments(eq(InhalerProtocol.SimulatorMarkerServiceUUID)).thenReturn(genericParcelUuid)
//        PowerMockito.whenNew(ScanFilter.Builder::class.java).withNoArguments().thenReturn(specificScanFilterBuilder)
//        whenever(specificScanFilterBuilder.setServiceUuid(eq(specificParcelUuid))).thenReturn(specificScanFilterBuilder)
//        whenever(specificScanFilterBuilder.setServiceUuid(eq(genericParcelUuid))).thenReturn(genericScanFilterBuilder)
//        whenever(specificScanFilterBuilder.build()).thenReturn(specificScanFilter)
//        whenever(genericScanFilterBuilder.build()).thenReturn(genericScanFilter)
//        PowerMockito.mockStatic(BluetoothAdapter::class.java)
//        whenever(BluetoothAdapter.getDefaultAdapter()).thenReturn(bluetoothAdapter)
//        PowerMockito.whenNew(ScanSettings.Builder::class.java).withNoArguments().thenReturn(scanSettingsBuilder)
//        whenever(scanSettingsBuilder.setScanMode(any())).thenReturn(scanSettingsBuilder)
//        whenever(scanSettingsBuilder.setReportDelay(any())).thenReturn(scanSettingsBuilder)
//        whenever(scanSettingsBuilder.build()).thenReturn(scanSettings)
//    }
//
//    @Test
//    fun testScanningStartedAndStoppedWithAppropriateFilters() {
//
//        // create expectations
//        val expectedScanFilterCount = 2
//
//        //perform operation
//        val bleScanner = BLEScanner(dependencyProvider)
//
//        val scanConnectionInfoList = ArrayList<ConnectionInfo>()
//        scanConnectionInfoList.add(connectionInfoList[0])
//
//        // call startScanning
//        bleScanner.startScanning()
//
//        // call setConnectionInfo with a couple of ConnectionInfo objects
//        bleScanner.setConnectionInfo(scanConnectionInfoList)
//
//        // test expectations
//
//        val scanFiltersArgumentCaptor = argumentCaptor<List<ScanFilter>>()
//
//        // verify BluetoothLEScanner.startScan() called with correct filters
//        //    A generic Simulator filter
//        //    1 for each ConnectionInfo that checks manufacturer data
//        verify(bluetoothLeScanner).startScan(scanFiltersArgumentCaptor.capture(), any(), any())
//        assertEquals(scanFiltersArgumentCaptor.lastValue.size, expectedScanFilterCount)
//        assertEquals(scanFiltersArgumentCaptor.lastValue[0], specificScanFilter)
//        assertEquals(scanFiltersArgumentCaptor.lastValue[1], genericScanFilter)
//
//        // perform operation
//
//        // call setConnectionInfo with empty list.
//        scanConnectionInfoList.clear()
//        bleScanner.setConnectionInfo(scanConnectionInfoList)
//
//        // test expectations
//        // verify BluetoothLEScanner.stopScan() called.
//        verify(bluetoothLeScanner, atLeastOnce()).stopScan(any())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun testGenericFiltersCreatedWhenManyConnectionInfosAreProvided() {
//
//        // create expectations
//        val expectedScanFilterCount = 1
//        PowerMockito.whenNew(ParcelUuid::class.java).withArguments(eq(InhalerProtocol.InhalerServiceUUID)).thenReturn(genericParcelUuid)
//
//        // perform operation
//        val maxFiltersField = BLEScanner::class.java.getDeclaredField("MAX_FILTERS")
//        maxFiltersField.isAccessible = true
//        val MAX_FILTERS = maxFiltersField.getInt(null)
//        val serialNumber = SERIAL_NUMBER
//        val initialSerialNumber = java.lang.Long.parseLong(serialNumber)
//
//        val bleScanner = BLEScanner(dependencyProvider!!)
//
//        // call startScanning
//        bleScanner.startScanning()
//
//        // call setConnectionInfo with more than 6 ConnectionInfo
//        val scanConnectionInfoList = ArrayList<ConnectionInfo>()
//
//        for (loop in 0..MAX_FILTERS) {
//            val scanConnectionInfo = ConnectionInfo()
//            scanConnectionInfo.serialNumber = java.lang.Long.toString(initialSerialNumber + loop)
//            scanConnectionInfo.authenticationKey = AUTHENTICATION_KEY
//            scanConnectionInfo.lastRecordId = 0
//            scanConnectionInfo.protocolType = ProtocolType.Inhaler
//
//            scanConnectionInfoList.add(scanConnectionInfo)
//        }
//
//        bleScanner.setConnectionInfo(scanConnectionInfoList)
//
//        // test expectations
//
//        val scanFiltersArgumentCaptor = argumentCaptor<List<ScanFilter>>()
//
//        // verify BluetoothLEScanner.startScan called with 1 generic filter
//        verify(bluetoothLeScanner).startScan(scanFiltersArgumentCaptor.capture(), any(), any())
//        assertEquals(scanFiltersArgumentCaptor.lastValue.size, expectedScanFilterCount)
//        assertEquals(scanFiltersArgumentCaptor.lastValue[0], genericScanFilter)
//    }
//
//    @Test
//    fun testScanningStartedAndStoppedWhenScanningControlFunctionsCalled() {
//
//        // create expectations
//        val expectedScanFilterCount = 2
//
//        //perform operation
//        val bleScanner = BLEScanner(dependencyProvider!!)
//
//        // call setConnectionInfo with at least 1 ConnectionInfo
//
//        val scanConnectionInfoList = ArrayList<ConnectionInfo>()
//        scanConnectionInfoList.add(connectionInfoList[0])
//        bleScanner.setConnectionInfo(scanConnectionInfoList)
//
//        // verify that BluetoothLEScanner.startScan() not yet called.
//        verify<BluetoothLeScanner>(bluetoothLeScanner, never()).startScan(any(), any(), any())
//
//        // call startScanning()
//        bleScanner.startScanning()
//
//        // test expectations
//
//        val scanFiltersArgumentCaptor = argumentCaptor<List<ScanFilter>>()
//
//        // verify that BluetoothLEScanner.startScan() called() with appropriate filters
//        verify<BluetoothLeScanner>(bluetoothLeScanner).startScan(scanFiltersArgumentCaptor!!.capture(), any(), any())
//        assertEquals(scanFiltersArgumentCaptor.lastValue.size, expectedScanFilterCount)
//        assertEquals(scanFiltersArgumentCaptor.lastValue[0], specificScanFilter)
//        assertEquals(scanFiltersArgumentCaptor.lastValue[1], genericScanFilter)
//
//        // perform operation
//
//        // call stopScanning()
//        bleScanner.stopScanning()
//
//        // test expectations
//
//        // verify that BluetoothLEScanner.stopScan()
//        verify<BluetoothLeScanner>(bluetoothLeScanner, atLeastOnce()).stopScan(any())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun testAdvertisementsAreMatchedWithCorrectFilters() {
//
//        // setup test
//        val manufacturerId = InhalerProtocol.ManufacturerId
//
//        // call setConnectionInfo with 2 ConnectionInfo
//        val advertisementCallback: AdvertisementCallback = mock()
//        val bleScanner = BLEScanner(dependencyProvider!!)
//        bleScanner.setAdvertisementCallback(advertisementCallback)
//        bleScanner.setConnectionInfo(connectionInfoList)
//
//        // perform operation
//
//        // pass ScanResult to BLEScanner.ScanCallback.onScanResult()
//        //    bleScanner.scanCallback.onScanResult();
//
//        val callbackField = bleScanner.javaClass.getDeclaredField("scanCallback")
//        callbackField.isAccessible = true
//        val callbackObject = callbackField.get(bleScanner)
//        val callbackMethod = callbackObject.javaClass.getMethod("onScanResult", Int::class.javaPrimitiveType, ScanResult::class.java)
//        callbackMethod.isAccessible = true
//
//        callbackMethod.invoke(callbackObject, CALLBACK_TYPE_FIRST_MATCH, scanResultList[0])
//
//        // test expectations
//        // verify that onAdvertisement() is called with correct ConnectionInfo
//        HandlerHelper.loopHandler()
//        verify(advertisementCallback).onAdvertisement(eq(connectionInfoList[0]), eq(bluetoothDeviceList[0]))
//
//
//        //perform operation
//        // create ScanResult with simulator inhaler advertisement
//
//        // pass ScanResult to BLEScanner.ScanCallback.onScanResult()
//        callbackMethod.invoke(callbackObject, CALLBACK_TYPE_FIRST_MATCH, scanResultList[1])
//
//        HandlerHelper.loopHandler()
//
//        // test expectations
//        // verify that onAdvertisement() is called with correct ConnectionInfo
//        verify(advertisementCallback).onAdvertisement(eq(connectionInfoList[1]), eq(bluetoothDeviceList[1]))
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun testBatchAdvertisementsAreMatchedWithCorrectFilters() {
//
//        // setup test
//        // call setConnectionInfo with 2 ConnectionInfo
//        val advertisementCallback: AdvertisementCallback = mock()
//        val bleScanner = BLEScanner(dependencyProvider!!)
//        bleScanner.setAdvertisementCallback(advertisementCallback)
//
//        bleScanner.setConnectionInfo(connectionInfoList)
//
//        // perform operation
//
//        whenever(specificParcelUuid!!.uuid).thenReturn(UUID.fromString("f429de80-c342-11e4-9da5-0002a5d5c51b"))
//
//        // pass ScanResult to BLEScanner.ScanCallback.onScanResult()
//        //    bleScanner.scanCallback.onScanResult();
//
//        val callbackField = bleScanner.javaClass.getDeclaredField("scanCallback")
//        callbackField.isAccessible = true
//        val callbackObject = callbackField.get(bleScanner)
//        val callbackMethod = callbackObject.javaClass.getMethod("onBatchScanResults", List::class.java)
//        callbackMethod.isAccessible = true
//        callbackMethod.invoke(callbackObject, scanResultList)
//
//        // test expectations
//        // verify that onAdvertisement() is called with correct ConnectionInfo
//
//        HandlerHelper.loopHandler()
//        verify(advertisementCallback).onAdvertisement(eq(connectionInfoList[0]), eq(bluetoothDeviceList[0]))
//        verify(advertisementCallback).onAdvertisement(eq(connectionInfoList[1]), eq(bluetoothDeviceList[1]))
//    }
//
//    @Test
//    fun testScanningStartedIfPermissionsGrantedAfterConnectionInfo() {
//
//        // create expectations
//        val expectedScanFilterCount = 2
//        val permissionSet = HashSet<String>()
//        permissionSet.add(Manifest.permission.ACCESS_COARSE_LOCATION)
//        permissionSet.add(Manifest.permission.ACCESS_FINE_LOCATION)
//        val permissionUpdateMessage = PermissionUpdateMessage(permissionSet)
//
//        whenever(permissionManager.checkPermission(any())).thenReturn(false)
//
//        //perform operation
//        val bleScanner = BLEScanner(dependencyProvider!!)
//
//        val scanConnectionInfoList = ArrayList<ConnectionInfo>()
//        scanConnectionInfoList.add(connectionInfoList[0])
//
//        // call startScanning
//        bleScanner.startScanning()
//
//        // call setConnectionInfo with a couple of ConnectionInfo objects
//        bleScanner.setConnectionInfo(scanConnectionInfoList)
//
//        // test expectations
//        verify<BluetoothLeScanner>(bluetoothLeScanner, never()).startScan(any(), any(), any())
//
//        whenever(permissionManager.checkPermission(any())).thenReturn(true)
//        bleScanner.onPermissionUpdatedMessage(permissionUpdateMessage)
//
//        val scanFiltersArgumentCaptor = argumentCaptor<List<ScanFilter>>()
//
//        // verify BluetoothLEScanner.startScan() called with correct filters
//        //    A generic Simulator filter
//        //    1 for each ConnectionInfo that checks manufacturer data
//        verify<BluetoothLeScanner>(bluetoothLeScanner).startScan(scanFiltersArgumentCaptor.capture(), any(), any())
//        assertEquals(scanFiltersArgumentCaptor.lastValue.size, expectedScanFilterCount)
//        assertEquals(scanFiltersArgumentCaptor.lastValue[0], specificScanFilter)
//        assertEquals(scanFiltersArgumentCaptor.lastValue[1], genericScanFilter)
//    }
//
//    @Test
//    fun testRunningInForegroundStartsScanInLowLatencyMode() {
//
//        // create expectations
//        val expectedScanFilterCount = 2
//        //perform operation
//        val bleScanner = BLEScanner(dependencyProvider!!)
//
//        val scanConnectionInfoList = ArrayList<ConnectionInfo>()
//        scanConnectionInfoList.add(connectionInfoList[0])
//
//        // mark as running in foreground
//        bleScanner.setInForeground(true)
//
//        // call setConnectionInfo with a couple of ConnectionInfo objects
//        bleScanner.setConnectionInfo(scanConnectionInfoList)
//
//        // call startScanning
//        bleScanner.startScanning()
//
//        val scanFiltersArgumentCaptor = argumentCaptor<List<ScanFilter>>()
//
//        //test expectations
//        val scanModeArgumentCaptor = argumentCaptor<Int>()
//        verify(scanSettingsBuilder).setScanMode(scanModeArgumentCaptor.capture())
//        assertEquals(ScanSettings.SCAN_MODE_LOW_LATENCY, scanModeArgumentCaptor.lastValue as Int)
//        // verify BluetoothLEScanner.startScan() called with correct filters
//        //    A generic Simulator filter
//        //    1 for each ConnectionInfo that checks manufacturer data
//        verify(bluetoothLeScanner).startScan(scanFiltersArgumentCaptor.capture(), any(), any())
//        assertEquals(scanFiltersArgumentCaptor.lastValue.size, expectedScanFilterCount)
//        assertEquals(scanFiltersArgumentCaptor.lastValue[0], specificScanFilter)
//        assertEquals(scanFiltersArgumentCaptor.lastValue[1], genericScanFilter)
//    }
//
//    @Test
//    fun testRunningInBackgroundStartsScanInLowPowerMode() {
//
//        // create expectations
//        val expectedScanFilterCount = 2
//        //perform operation
//        val bleScanner = BLEScanner(dependencyProvider!!)
//
//        val scanConnectionInfoList = ArrayList<ConnectionInfo>()
//        scanConnectionInfoList.add(connectionInfoList[0])
//
//        // mark as running in foreground
//        bleScanner.setInForeground(false)
//
//        // call setConnectionInfo with a couple of ConnectionInfo objects
//        bleScanner.setConnectionInfo(scanConnectionInfoList)
//
//        // call startScanning
//        bleScanner.startScanning()
//
//        val scanFiltersArgumentCaptor = argumentCaptor<List<ScanFilter>>()
//
//        //test expectations
//        val scanModeArgumentCaptor = argumentCaptor<Int>()
//        verify(scanSettingsBuilder).setScanMode(scanModeArgumentCaptor.capture())
//        assertEquals(ScanSettings.SCAN_MODE_LOW_POWER, scanModeArgumentCaptor.lastValue as Int)
//        // verify BluetoothLEScanner.startScan() called with correct filters
//        //    A generic Simulator filter
//        //    1 for each ConnectionInfo that checks manufacturer data
//        verify<BluetoothLeScanner>(bluetoothLeScanner).startScan(scanFiltersArgumentCaptor!!.capture(), any(), any())
//        assertEquals(scanFiltersArgumentCaptor.lastValue.size, expectedScanFilterCount)
//        assertEquals(scanFiltersArgumentCaptor.lastValue[0], specificScanFilter)
//        assertEquals(scanFiltersArgumentCaptor.lastValue[1], genericScanFilter)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun testMultipleScanResultsDoNotTriggerMultipleCallbacks() {
//
//        // setup test
//        // call setConnectionInfo with 2 ConnectionInfo
//        val advertisementCallback: AdvertisementCallback = mock()
//        val bleScanner = BLEScanner(dependencyProvider!!)
//        bleScanner.setAdvertisementCallback(advertisementCallback)
//
//        val scanConnectionInfoList = ArrayList<ConnectionInfo>()
//        scanConnectionInfoList.add(connectionInfoList[0])
//        bleScanner.setConnectionInfo(scanConnectionInfoList)
//
//        // perform operation
//
//        // pass ScanResult to BLEScanner.ScanCallback.onScanResult()
//        //    bleScanner.scanCallback.onScanResult();
//
//        val callbackField = bleScanner.javaClass.getDeclaredField("scanCallback")
//        callbackField.isAccessible = true
//        val callbackObject = callbackField.get(bleScanner)
//        val callbackMethod = callbackObject.javaClass.getMethod("onScanResult", Int::class.javaPrimitiveType, ScanResult::class.java)
//        callbackMethod.isAccessible = true
//
//        callbackMethod.invoke(callbackObject, CALLBACK_TYPE_FIRST_MATCH, scanResultList[0])
//
//        // test expectations
//        // verify that onAdvertisement() is called with correct ConnectionInfo
//
//        HandlerHelper.loopHandler()
//        verify(advertisementCallback).onAdvertisement(eq(connectionInfoList[0]), eq(bluetoothDeviceList[0]))
//
//        // trigger the scan result again
//        callbackMethod.invoke(callbackObject, CALLBACK_TYPE_FIRST_MATCH, scanResultList[0])
//
//        HandlerHelper.loopHandler()
//        verify(advertisementCallback, atMost(1)).onAdvertisement(eq(connectionInfoList[0]), eq(bluetoothDeviceList[0]))
//
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun testUnmatchedFilterDoesNotTriggerCallback() {
//
//        // setup test
//        val SERIAL_NUMBER2 = "92345678902"
//        val AUTHENTICATION_KEY2 = "1234567890123456"
//
//        // call setConnectionInfo with 2 ConnectionInfo
//        val advertisementCallback: AdvertisementCallback = mock()
//        val bleScanner = BLEScanner(dependencyProvider!!)
//        bleScanner.setAdvertisementCallback(advertisementCallback)
//
//        val connectionInfo2 = ConnectionInfo()
//        connectionInfo2.serialNumber = SERIAL_NUMBER2
//        connectionInfo2.authenticationKey = AUTHENTICATION_KEY2
//        connectionInfo2.lastRecordId = 1
//        connectionInfo2.protocolType = ProtocolType.Inhaler
//
//        val scanConnectionInfoList = ArrayList<ConnectionInfo>()
//        scanConnectionInfoList.add(connectionInfo2)
//        bleScanner.setConnectionInfo(scanConnectionInfoList)
//
//        // perform operation
//
//        // pass ScanResult to BLEScanner.ScanCallback.onScanResult()
//        //    bleScanner.scanCallback.onScanResult();
//
//        val callbackField = bleScanner.javaClass.getDeclaredField("scanCallback")
//        callbackField.isAccessible = true
//        val callbackObject = callbackField.get(bleScanner)
//        val callbackMethod = callbackObject.javaClass.getMethod("onScanResult", Int::class.javaPrimitiveType, ScanResult::class.java)
//        callbackMethod.isAccessible = true
//
//        callbackMethod.invoke(callbackObject, CALLBACK_TYPE_FIRST_MATCH, scanResultList[0])
//
//        // test expectations
//        // verify that onAdvertisement() is called with correct ConnectionInfo
//
//        HandlerHelper.loopHandler()
//        verify(advertisementCallback, never()).onAdvertisement(eq(connectionInfoList[0]), eq(bluetoothDeviceList[0]))
//    }
//
//    @Test(expected = IllegalStateException::class)
//    fun testExceptionFromBluetoothLEScannerIsRethrown() {
//
//        // initialize test
//        doThrow(IllegalStateException("Exception from BluetoothLeScanner")).whenever(bluetoothLeScanner).startScan(any(), any(), any())
//
//        //perform operation
//        val bleScanner = BLEScanner(dependencyProvider)
//
//        val scanConnectionInfoList = ArrayList<ConnectionInfo>()
//        scanConnectionInfoList.add(connectionInfoList[0])
//
//        // call setConnectionInfo with a couple of ConnectionInfo objects
//        bleScanner.setConnectionInfo(scanConnectionInfoList)
//
//        // call startScanning
//        // will throw expected exception
//        bleScanner.startScanning()
//    }
//
//    /**
//     * This method creates a pair of connection info objects used in the tests.
//
//     * @return -  A list containing two connectionInfo objects.
//     */
//    private fun createTwoConnectionInfoObjects(): List<ConnectionInfo> {
//
//        val connectionInfo = ConnectionInfo()
//        connectionInfo.serialNumber = SERIAL_NUMBER
//        connectionInfo.authenticationKey = AUTHENTICATION_KEY
//        connectionInfo.lastRecordId = 0
//        connectionInfo.protocolType = ProtocolType.Inhaler
//
//        val connectionInfo2 = ConnectionInfo()
//        connectionInfo2.serialNumber = SERIAL_NUMBER2
//        connectionInfo2.authenticationKey = AUTHENTICATION_KEY
//        connectionInfo2.lastRecordId = 0
//        connectionInfo2.protocolType = ProtocolType.Inhaler
//
//        val connectionInfoList = ArrayList<ConnectionInfo>()
//        connectionInfoList.add(connectionInfo)
//        connectionInfoList.add(connectionInfo2)
//
//        return connectionInfoList
//    }
//
//    /**
//     * This method creates a pair of scan result objects used in the tests.
//
//     * @return -  A list containing two scan results.
//     */
//    @Throws(Exception::class)
//    private fun createTwoScanResults(): List<ScanResult> {
//
//        val scanResultList = ArrayList<ScanResult>()
//        val manufacturerId = InhalerProtocol.ManufacturerId
//
//        val parcelUuids = ArrayList<ParcelUuid>()
//        parcelUuids.add(specificParcelUuid)
//        val manufacturerSpecificData: SparseArray<ByteArray> = mock()
//        whenever(manufacturerSpecificData.keyAt(any())).thenReturn(manufacturerId)
//        whenever(manufacturerSpecificData.size()).thenReturn(1)
//        val manuFacturerData = byteArrayOf(2, 223.toByte(), 220.toByte(), 28, 53)
//        manufacturerSpecificData.append(manufacturerId, manuFacturerData)
//
//        val scanRecord: ScanRecord = mock()
//
//        whenever(scanRecord.deviceName).thenReturn(null)
//        whenever(scanRecord.serviceUuids).thenReturn(parcelUuids)
//        whenever(scanRecord.getManufacturerSpecificData(eq(manufacturerId))).thenReturn(manuFacturerData)
//        whenever(scanRecord.manufacturerSpecificData).thenReturn(manufacturerSpecificData)
//        whenever(scanRecord.bytes).thenReturn(manuFacturerData)
//        val scanResult: ScanResult = mock()
//        whenever(scanResult.scanRecord).thenReturn(scanRecord)
//        whenever(scanResult.device).thenReturn(bluetoothDeviceList[0])
//
//        whenever(specificParcelUuid.uuid).thenReturn(UUID.fromString("f429de80-c342-11e4-9da5-0002a5d5c51b"))
//        whenever(genericParcelUuid.uuid).thenReturn(UUID.fromString("000018ff-0000-1000-8000-00805f9b34fb"))
//
//        scanResultList.add(scanResult)
//
//        val scanRecord2: ScanRecord = mock()
//        val manufacturerSpecificData2: SparseArray<ByteArray> = mock()
//        whenever(manufacturerSpecificData2.keyAt(any())).thenReturn(manufacturerId)
//        whenever(manufacturerSpecificData2.size()).thenReturn(1)
//        val manuFacturerData2 = byteArrayOf(2, 223.toByte(), 220.toByte(), 28, 54)
//        manufacturerSpecificData2.append(manufacturerId, manuFacturerData2)
//
//        whenever(scanRecord2.deviceName).thenReturn("Sim:12345678902")
//        whenever(scanRecord2.serviceUuids).thenReturn(parcelUuids)
//        whenever(scanRecord2.getManufacturerSpecificData(eq(manufacturerId))).thenReturn(manuFacturerData2)
//        whenever(scanRecord2.manufacturerSpecificData).thenReturn(manufacturerSpecificData2)
//        whenever(scanRecord2.bytes).thenReturn(manuFacturerData2)
//        val scanResult2: ScanResult = mock()
//        whenever(scanResult2.scanRecord).thenReturn(scanRecord2)
//        whenever(scanResult2.device).thenReturn(bluetoothDeviceList[1])
//        scanResultList.add(scanResult2)
//
//        return scanResultList
//    }
//
//    /**
//     * This method creates a pair of blue tooth devices used in the tests.
//
//     * @return -  A list containing two bluetooth devices.
//     */
//    private fun createTwoBluetoothDevices(): List<BluetoothDevice> {
//        val bluetoothDevices = ArrayList<BluetoothDevice>()
//        val device: BluetoothDevice = mock()
//        val device2: BluetoothDevice = mock()
//        whenever(device.address).thenReturn("12345")
//        whenever(device2.address).thenReturn("67890")
//        bluetoothDevices.add(device)
//        bluetoothDevices.add(device2)
//        return bluetoothDevices
//    }
//
//    companion object {
//
//        private val SERIAL_NUMBER = "12345678901"
//        private val AUTHENTICATION_KEY = "1234567890123456"
//
//        fun setupClass() {}
//    }
//}
