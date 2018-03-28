////
//// DeviceServiceImplTest.java
//// teva_devices
////
//// Copyright (c) 2017 Teva. All rights reserved.
////
//
//package com.teva.devices.service
//
//import android.annotation.TargetApi
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothDevice
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import com.nhaarman.mockito_kotlin.*
//import com.teva.utilities.services.DependencyProvider
//import com.teva.common.utilities.Messenger
//import com.teva.devices.mocks.HandlerHelper
//import com.teva.devices.utils.Matchers.matchesConnectionInfoList
//import org.junit.Assert.assertEquals
//import org.junit.Assert.assertThat
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.powermock.api.mockito.PowerMockito
//import org.powermock.core.classloader.annotations.PrepareForTest
//import org.powermock.modules.junit4.PowerMockRunner
//import java.lang.reflect.InvocationTargetException
//import java.util.ArrayList
//
///**
// * This class defines unit tests for the DeviceServiceImpl class.
// */
//@TargetApi(Build.VERSION_CODES.LOLLIPOP)
//@RunWith(PowerMockRunner::class)
//@PrepareForTest(BluetoothAdapter::class, DeviceServiceImpl::class)
//class DeviceServiceImplTest {
//
//    private var bleScanner: Scanner = mock()
//    private var dependencyProvider: DependencyProvider = DependencyProvider.default
//    private var context: Context = mock()
//    private var bluetoothAdapter: BluetoothAdapter = mock()
//    private var protocolFactory: ProtocolFactory = mock()
//    private var protocol: Protocol = mock()
//    private var medicationDispenser: MedicationDispenser = mock()
//    private var deviceServiceCallback: DeviceServiceCallback = mock()
//    private var messenger: Messenger = mock()
//
//    /**
//     * This method sets up the mock classes and methods required for test execution.
//
//     * @throws Exception -  an exception is thrown if constructor mocking fails.
//     */
//    @Before
//    @PrepareForTest(BLEScanner::class, ProtocolFactory::class)
//    @Throws(Exception::class)
//    fun setup() {
//        DependencyProvider.default.unregisterAll()
//        HandlerHelper.clearQueue()
//
//        dependencyProvider.register(Scanner::class, bleScanner)
//        dependencyProvider.register(Context::class, context)
//        PowerMockito.mockStatic(BluetoothAdapter::class.java)
//        whenever(BluetoothAdapter.getDefaultAdapter()).thenReturn(bluetoothAdapter)
//        whenever(bluetoothAdapter.isEnabled).thenReturn(true)
//
//        dependencyProvider.register(ProtocolFactory::class, protocolFactory)
//        whenever(protocolFactory.createProtocol(any())).thenReturn(protocol)
//        whenever(protocol.createMedicalDispenser(any(), any())).thenReturn(medicationDispenser)
//        dependencyProvider.register(Messenger::class, messenger)
//    }
//
//    @Test
//    fun testStartScanStartsScanningOnBLEScanner() {
//
//        // perform operation
//        val deviceServiceImpl = DeviceServiceImpl(dependencyProvider)
//        deviceServiceImpl.start()
//
//        // test expectations
//        verify(bleScanner).startScanning()
//    }
//
//    @Test
//    fun testSetConnectionInfoPassesConnectionInformationToBLEScanner() {
//        // initialize test data
//        val connectionInfo = createConnectionInfo()
//        val connectionInfoList = ArrayList<ConnectionInfo>()
//        connectionInfoList.add(connectionInfo)
//
//        // perform operation
//        val deviceServiceImpl = DeviceServiceImpl(dependencyProvider)
//        deviceServiceImpl.setConnectionInfo(connectionInfoList)
//
//        // test expectations
//        verify(bleScanner).setConnectionInfo(eq(connectionInfoList))
//    }
//
//    @Test
//    fun testStopScanStopsScanningOnBLEScanner() {
//        // initialize test
//        val connectionInfo = createConnectionInfo()
//        val connectionInfoList = ArrayList<ConnectionInfo>()
//        connectionInfoList.add(connectionInfo)
//        val bluetoothDevice: BluetoothDevice = mock()
//
//        // perform operation
//        val deviceServiceImpl = DeviceServiceImpl(dependencyProvider)
//        deviceServiceImpl.setConnectionInfo(connectionInfoList)
//        deviceServiceImpl.start()
//        deviceServiceImpl.onAdvertisement(connectionInfo, bluetoothDevice)
//        deviceServiceImpl.stop()
//
//        // test expectations
//        verify(bleScanner).stopScanning()
//        // verify that connected devices are disconnected upon stopping the scan.
//        verify(medicationDispenser).disconnect()
//    }
//
//    @Test
//    fun testAppForegroundStatusIsNotifiedToBLEScanner() {
//        // initialize test
//        val booleanArgumentCaptor = argumentCaptor<Boolean>()
//
//        // perform operation
//        val deviceServiceImpl = DeviceServiceImpl(dependencyProvider)
//        deviceServiceImpl.setInForeground(true)
//        deviceServiceImpl.setInForeground(false)
//
//        // test expectations
//        verify(bleScanner, times(2)).setInForeground(booleanArgumentCaptor.capture())
//        val foregroundFlags = booleanArgumentCaptor.allValues
//        assertEquals(true, foregroundFlags[0])
//        assertEquals(false, foregroundFlags[1])
//    }
//
//    @Test
//    fun testAdvertisementTriggersConnectionToMedicationDispenser() {
//        // initialize test data
//        val connectionInfo = createConnectionInfo()
//        val connectionInfoList = ArrayList<ConnectionInfo>()
//        connectionInfoList.add(connectionInfo)
//        val bluetoothDevice: BluetoothDevice = mock()
//        val SERIAL_NUMBER2 = "92345678901"
//        val connectionInfo2 = ConnectionInfo()
//        connectionInfo2.serialNumber = SERIAL_NUMBER2
//        connectionInfo2.authenticationKey = AUTHENTICATION_KEY
//        connectionInfo2.lastRecordId = 0
//        connectionInfo2.protocolType = ProtocolType.Inhaler
//        val bluetoothDevice2: BluetoothDevice = mock()
//        connectionInfoList.add(connectionInfo2)
//
//        // perform operation
//        val deviceServiceImpl = DeviceServiceImpl(dependencyProvider)
//        deviceServiceImpl.setConnectionInfo(connectionInfoList)
//        deviceServiceImpl.start()
//
//        // send an advertisement.
//        whenever(protocol.createMedicalDispenser(eq(connectionInfo), eq(bluetoothDevice))).thenReturn(medicationDispenser)
//        whenever(medicationDispenser.connectionInfo).thenReturn(connectionInfo)
//        deviceServiceImpl.onAdvertisement(connectionInfo, bluetoothDevice)
//
//        // test expectations
//        // verify that the medication dispenser connect is invoked.
//        verify(medicationDispenser).connect()
//
//        // perform operation
//        val medicationDispenser2: MedicationDispenser = mock()
//        whenever(medicationDispenser2.connectionInfo).thenReturn(connectionInfo2)
//        whenever(protocol.createMedicalDispenser(eq(connectionInfo2), eq(bluetoothDevice2))).thenReturn(medicationDispenser2)
//        // send a second advertisement.
//        deviceServiceImpl.onAdvertisement(connectionInfo2, bluetoothDevice2)
//
//        // test expectations
//        // verify that the second medication dispenser connect is invoked.
//        verify(medicationDispenser2).connect()
//    }
//
//    @Test
//    fun testMultipleAdvertisementsDoNotTriggerMultipleConnections() {
//        // initialize test data
//        val connectionInfo = createConnectionInfo()
//        val connectionInfoList = ArrayList<ConnectionInfo>()
//        connectionInfoList.add(connectionInfo)
//        val bluetoothDevice: BluetoothDevice = mock()
//        whenever(medicationDispenser.connectionInfo).thenReturn(connectionInfo)
//
//        // perform operation
//        val deviceServiceImpl = DeviceServiceImpl(dependencyProvider)
//        deviceServiceImpl.setConnectionInfo(connectionInfoList)
//        deviceServiceImpl.start()
//
//        // send an advertisement.
//        deviceServiceImpl.onAdvertisement(connectionInfo, bluetoothDevice)
//
//        // send a second advertisement.
//        deviceServiceImpl.onAdvertisement(connectionInfo, bluetoothDevice)
//
//        // test expectations
//        // verify that the connect is invoked only once.
//        verify(medicationDispenser, times(1)).connect()
//    }
//
//
//    @Test
//    fun testConnectedDevicesNotIncludedInConnectionInfoSentToBLEScanner() {
//        // initialize test data
//        val connectionInfo = createConnectionInfo()
//        val bluetoothDevice: BluetoothDevice = mock()
//        whenever(medicationDispenser.connectionInfo).thenReturn(connectionInfo)
//
//        val SERIAL_NUMBER2 = "92345678901"
//        val connectionInfo2 = ConnectionInfo()
//        connectionInfo2.serialNumber = SERIAL_NUMBER2
//        connectionInfo2.authenticationKey = AUTHENTICATION_KEY
//        connectionInfo2.lastRecordId = 0
//        connectionInfo2.protocolType = ProtocolType.Inhaler
//
//        val connectionInfoList1 = ArrayList<ConnectionInfo>()
//        connectionInfoList1.add(connectionInfo)
//
//        val connectionInfoList2 = ArrayList<ConnectionInfo>()
//        connectionInfoList2.add(connectionInfo)
//        connectionInfoList2.add(connectionInfo2)
//
//        // create expectations
//        val expectedConnectionInfoList = ArrayList<ConnectionInfo>()
//        expectedConnectionInfoList.add(connectionInfo2)
//
//        // perform operation
//        val deviceServiceImpl = DeviceServiceImpl(dependencyProvider)
//        deviceServiceImpl.setConnectionInfo(connectionInfoList1)
//        deviceServiceImpl.start()
//
//        // send an advertisement to mark the device as connected.
//        deviceServiceImpl.onAdvertisement(connectionInfo, bluetoothDevice)
//
//        // set the connection info containing the already connected device and a new device
//        deviceServiceImpl.setConnectionInfo(connectionInfoList2)
//
//        val connectionInfoListArgumentCaptor = argumentCaptor<List<ConnectionInfo>>()
//
//        // test expectations
//        // verify that the connection info has been set twice on the BLE scanner.
//        verify(bleScanner, times(2)).setConnectionInfo(connectionInfoListArgumentCaptor.capture())
//        // verify that the second connection info list sent to the BLE scanner has only the new device.
//        assertThat(connectionInfoListArgumentCaptor.allValues[1], matchesConnectionInfoList(expectedConnectionInfoList))
//    }
//
//    @Test
//    fun testDeviceIsDisconnectedIfNotIncludedInConnectionInfo() {
//        // initialize test data
//        val connectionInfo = createConnectionInfo()
//        val bluetoothDevice: BluetoothDevice = mock()
//        whenever(medicationDispenser.connectionInfo).thenReturn(connectionInfo)
//
//        val SERIAL_NUMBER2 = "92345678901"
//        val connectionInfo2 = ConnectionInfo()
//        connectionInfo2.serialNumber = SERIAL_NUMBER2
//        connectionInfo2.authenticationKey = AUTHENTICATION_KEY
//        connectionInfo2.lastRecordId = 0
//        connectionInfo2.protocolType = ProtocolType.Inhaler
//
//        val connectionInfoList1 = ArrayList<ConnectionInfo>()
//        connectionInfoList1.add(connectionInfo)
//
//        val connectionInfoList2 = ArrayList<ConnectionInfo>()
//        connectionInfoList2.add(connectionInfo2)
//
//        // perform operation
//        val deviceServiceImpl = DeviceServiceImpl(dependencyProvider)
//        deviceServiceImpl.setConnectionInfo(connectionInfoList1)
//        deviceServiceImpl.start()
//
//        // send an advertisement to mark the device as connected.
//        deviceServiceImpl.onAdvertisement(connectionInfo, bluetoothDevice)
//
//        // set the connection info without the connected device.
//        deviceServiceImpl.setConnectionInfo(connectionInfoList2)
//
//        val connectionInfoListArgumentCaptor = argumentCaptor<List<ConnectionInfo>>()
//
//        // test expectations
//        // verify disconnect is invoked
//        verify(medicationDispenser).disconnect()
//        // verify that the connection info has been set twice on the BLE scanner.
//        verify(bleScanner, times(2)).setConnectionInfo(connectionInfoListArgumentCaptor.capture())
//        // verify that the second connection info list sent to the BLE scanner has only the new device.
//        assertThat(connectionInfoListArgumentCaptor.allValues[1], matchesConnectionInfoList(connectionInfoList2))
//    }
//
//    @Test
//    fun testOnConnectedNotificationExcludesDeviceFromBLEScannerConnectionInfo() {
//        // initialize test data
//        val connectionInfo = createConnectionInfo()
//        val bluetoothDevice: BluetoothDevice = mock()
//        val connectionInfoList = ArrayList<ConnectionInfo>()
//        connectionInfoList.add(connectionInfo)
//
//        // create expectations
//        val expectedConnectionInfoList = ArrayList<ConnectionInfo>()
//
//        // perform operation
//        val deviceServiceImpl = DeviceServiceImpl(dependencyProvider)
//        deviceServiceImpl.setConnectionInfo(connectionInfoList)
//        deviceServiceImpl.start()
//        deviceServiceImpl.setCallback(deviceServiceCallback)
//        deviceServiceImpl.onAdvertisement(connectionInfo, bluetoothDevice)
//
//        // test expectations
//        // verify that the connection info was called once.
//        verify(bleScanner).setConnectionInfo(eq(connectionInfoList))
//
//        // send the onConnected notification.
//        deviceServiceImpl.onConnected(connectionInfo)
//
//        // test expectations
//        // verify that the callback is invoked.
//        verify(deviceServiceCallback).onConnected(eq(connectionInfo))
//
//        val connectionInfoListArgumentCaptor = argumentCaptor<List<ConnectionInfo>>()
//
//        // verify that the connection info is called again after the onConnected message.
//        verify(bleScanner, times(2)).setConnectionInfo(connectionInfoListArgumentCaptor.capture())
//
//        // verify that the connection info sent after onConnected excludes the connected device.
//        assertThat(connectionInfoListArgumentCaptor.allValues[1], matchesConnectionInfoList(expectedConnectionInfoList))
//    }
//
//    @Test
//    fun testOnDisconnectedNotificationAddsDeviceToBLEScannerConnectionInfo() {
//        // initialize test data
//        val connectionInfo = createConnectionInfo()
//        val bluetoothDevice: BluetoothDevice = mock()
//        val connectionInfoList = ArrayList<ConnectionInfo>()
//        connectionInfoList.add(connectionInfo)
//
//        // create expectations
//        val expectedConnectionInfoList = ArrayList<ConnectionInfo>()
//
//        // perform operation
//        val deviceServiceImpl = DeviceServiceImpl(dependencyProvider)
//        deviceServiceImpl.setConnectionInfo(connectionInfoList)
//        deviceServiceImpl.start()
//        deviceServiceImpl.setCallback(deviceServiceCallback)
//        deviceServiceImpl.onAdvertisement(connectionInfo, bluetoothDevice)
//
//        // test expectations
//        // verify that the connection info was called once.
//        verify(bleScanner).setConnectionInfo(eq(connectionInfoList))
//
//        // perform operation
//        // send the onConnected notification.
//        deviceServiceImpl.onConnected(connectionInfo)
//
//        // send the onDisconnected notification.
//        deviceServiceImpl.onDisconnected(connectionInfo)
//
//        val connectionInfoListArgumentCaptor = argumentCaptor<List<ConnectionInfo>>()
//
//        // verify that the callback is invoked.
//        verify(deviceServiceCallback).onDisconnected(eq(connectionInfo))
//        // verify that the connection info was called thrice - initially, after OnConnected and after onDisconnected.
//        verify(bleScanner, times(3)).setConnectionInfo(connectionInfoListArgumentCaptor.capture())
//        // verify that the connection info sent after onConnected excludes the connected device.
//        assertThat(connectionInfoListArgumentCaptor.allValues[1], matchesConnectionInfoList(expectedConnectionInfoList))
//        // verify that the connection info sent after onDisconnected includes the disconnected device.
//        assertThat(connectionInfoListArgumentCaptor.allValues[2], matchesConnectionInfoList(connectionInfoList))
//    }
//
//    @Test
//    fun testOnUpdatedNotificationTriggersCallback() {
//        // initialize test data
//        val connectionInfo = createConnectionInfo()
//        val bluetoothDevice: BluetoothDevice = mock()
//        val connectionInfoList = ArrayList<ConnectionInfo>()
//        connectionInfoList.add(connectionInfo)
//        val deviceInfo = DeviceInfo()
//        val inhaleEventInfo = InhaleEventInfo()
//        val inhaleEventInfoList = ArrayList<InhaleEventInfo>()
//        inhaleEventInfoList.add(inhaleEventInfo)
//
//        // perform operation
//        val deviceServiceImpl = DeviceServiceImpl(dependencyProvider)
//        deviceServiceImpl.setCallback(deviceServiceCallback)
//        deviceServiceImpl.setConnectionInfo(connectionInfoList)
//        deviceServiceImpl.start()
//        deviceServiceImpl.onAdvertisement(connectionInfo, bluetoothDevice)
//
//        // send an onUpdated notification.
//        deviceServiceImpl.onUpdated(connectionInfo, deviceInfo, inhaleEventInfoList)
//
//        // test expectations
//        // verify that the callback is invoked with the appropriate data.
//        verify(deviceServiceCallback).onUpdated(eq(connectionInfo), eq(deviceInfo), eq(inhaleEventInfoList))
//    }
//
//    @Test
//    @Throws(NoSuchFieldException::class, NoSuchMethodException::class, IllegalAccessException::class, InvocationTargetException::class)
//    fun testBluetoothAdapterStateChangesStopAndStartScansCorrectly() {
//        // initialize test
//        // initially mark the bluetooth adapter as disabled.
//        whenever(bluetoothAdapter.isEnabled).thenReturn(false)
//
//        // perform operation
//        val deviceServiceImpl = DeviceServiceImpl(dependencyProvider)
//        deviceServiceImpl.start()
//
//        // test expectations
//        // verify that start scanning is not invoked on the BLE Scanner.
//        verify(bleScanner, never()).startScanning()
//
//        // create an intent for sending the bluetooth adapter state changed notification
//        val intent: Intent = mock()
//        whenever(intent.action).thenReturn(BluetoothAdapter.ACTION_STATE_CHANGED)
//        whenever(intent.getIntExtra(any<String>(), any<Int>())).thenReturn(BluetoothAdapter.STATE_ON)
//
//        // obtain reference to the bluetooth state change receiver using refection
//        val bluetoothReceiverField = deviceServiceImpl.javaClass.getDeclaredField("bluetoothStateReceiver")
//        bluetoothReceiverField.isAccessible = true
//        val onReceiveMethod = bluetoothReceiverField.type.getDeclaredMethod("onReceive", Context::class.java, Intent::class.java)
//        onReceiveMethod.isAccessible = true
//        val bluetoothStateReceiver = bluetoothReceiverField.get(deviceServiceImpl)
//
//        //perform operation
//        // send the bluetooth adapter state change notification with ON state.
//        onReceiveMethod.invoke(bluetoothStateReceiver, context, intent)
//
//        // test expectations
//        // verify that start scanning is invoked on the BLE Scanner.
//        verify(bleScanner).startScanning()
//
//        //perform operation
//        // send the bluetooth adapter state change notification with OFF state.
//        whenever(intent.getIntExtra(any<String>(), any<Int>())).thenReturn(BluetoothAdapter.STATE_OFF)
//        onReceiveMethod.invoke(bluetoothStateReceiver, context, intent)
//
//        // test expectations
//        // verify that stop scanning is invoked on the BLE scanner.
//        verify(bleScanner).stopScanning()
//    }
//
//    /**
//     * Creates a ConnectionInfo object to be used by the tests.
//
//     * @return - a ConnectionInfo object.
//     */
//    private fun createConnectionInfo(): ConnectionInfo {
//        val connectionInfo = ConnectionInfo()
//        connectionInfo.serialNumber = SERIAL_NUMBER
//        connectionInfo.authenticationKey = AUTHENTICATION_KEY
//        connectionInfo.lastRecordId = 0
//        connectionInfo.protocolType = ProtocolType.Inhaler
//        return connectionInfo
//    }
//
//    companion object {
//
//        private val SERIAL_NUMBER = "12345678901"
//        private val AUTHENTICATION_KEY = "1234567890123456"
//
//    }
//}
