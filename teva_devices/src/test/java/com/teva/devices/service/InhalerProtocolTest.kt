//
// InhalerProtocolTest.java
// teva_devices
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.devices.service


import android.bluetooth.BluetoothDevice
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.devices.utils.Matchers.matchesAdvertisementFilter
import com.teva.devices.utils.Matchers.matchesMedicationDispenser
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * This class defines the unit tests for the InhalerProtocol class.
 */
class InhalerProtocolTest {

    internal var dependencyProvider: DependencyProvider = DependencyProvider.default

    /**
     * This method sets up the mocks of the classes and methods required for test execution.
     */
    @Before
    fun setup() {
        dependencyProvider.unregisterAll()

        dependencyProvider.register(TimeService::class, mock<TimeService>())
    }

    @Test
    @Throws(Exception::class)
    fun testMedicationDispenserIsCreatedCorrectly() {
        // initialize test data
        val bluetoothDevice: BluetoothDevice = mock()
        val connectionInfo = createConnectionInfo()

        // create expectations
        val expectedDispenser = MedicationDispenserImpl(dependencyProvider, connectionInfo, bluetoothDevice)

        // perform operation
        val inhalerProtocol = InhalerProtocol(dependencyProvider)
        val returnedDispenser = inhalerProtocol.createMedicalDispenser(connectionInfo, bluetoothDevice) as MedicationDispenserImpl?

        // test expectations
        // verify that the created medical dispenser matches the expected one.
        assertThat<MedicationDispenserImpl>(returnedDispenser, matchesMedicationDispenser(expectedDispenser))
    }

    @Test
    fun testAdvertisementFilterIsCreatedCorrectly() {
        // initialize test data
        val connectionInfo = createConnectionInfo()

        // create expectations
        val expectedAdvertisementFilter = AdvertisementFilter()
        expectedAdvertisementFilter.serviceUUID = InhalerProtocol.InhalerServiceUUID
        expectedAdvertisementFilter.connectionInfo = connectionInfo
        expectedAdvertisementFilter.manufacturerId = InhalerProtocol.ManufacturerId
        expectedAdvertisementFilter.manufacturerData = byteArrayOf(2, 223.toByte(), 220.toByte(), 28, 53)
        expectedAdvertisementFilter.name = "Sim:" + SERIAL_NUMBER

        // perform operation
        val inhalerProtocol = InhalerProtocol(dependencyProvider)
        val returnedAdvertisementFilter = inhalerProtocol.createFilter(connectionInfo)

        // test expectations
        // verify that the created advertisement filter matches the expected one.
        assertThat(returnedAdvertisementFilter, matchesAdvertisementFilter(expectedAdvertisementFilter))
    }

    @Test
    @Throws(Exception::class)
    fun testComparisonOfDevicesWorksCorrectly() {
        // initialize test data
        val connectionInfo = createConnectionInfo()
        val bluetoothDevice1: BluetoothDevice = mock()
        val dispenser = MedicationDispenserImpl(dependencyProvider, connectionInfo, bluetoothDevice1)
        val bluetoothDevice2: BluetoothDevice = mock()

        // create expectations
        whenever(bluetoothDevice1.address).thenReturn("1")
        whenever(bluetoothDevice2.address).thenReturn("2")

        // perform operation
        val inhalerProtocol = InhalerProtocol(dependencyProvider)
        // compare with the first device.
        var sameDevice = inhalerProtocol.isSameDevice(dispenser, bluetoothDevice1)

        // test expectations
        // verify that devices match.
        assertTrue(sameDevice)

        // perform operation
        // compare with the second device.
        sameDevice = inhalerProtocol.isSameDevice(dispenser, bluetoothDevice2)

        // test expectations
        // verify that the devices do not match.
        assertFalse(sameDevice)

        // perform operation
        // compare with the a MedicationDispenser mock.
        val mockDispenser: MedicationDispenser = mock()
        sameDevice = inhalerProtocol.isSameDevice(mockDispenser, bluetoothDevice2)

        // test expectations
        // verify that the devices do not match.
        assertFalse(sameDevice)
    }

    /**
     * This method creates a ConnectionInfo object to be used by tests.

     * @return - a ConnectionInfo object.
     */
    private fun createConnectionInfo(): ConnectionInfo {
        val connectionInfo = ConnectionInfo()
        connectionInfo.serialNumber = SERIAL_NUMBER
        connectionInfo.authenticationKey = AUTHENTICATION_KEY
        connectionInfo.lastRecordId = 0
        connectionInfo.protocolType = ProtocolType.Inhaler
        return connectionInfo
    }

    companion object {

        private val SERIAL_NUMBER = "12345678901"
        private val AUTHENTICATION_KEY = "1234567890123456"
    }
}
