//
// DeviceQueryTests.java
// teva_devices
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.devices.model

import android.annotation.TargetApi
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.nhaarman.mockito_kotlin.*
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.devices.dataquery.ConnectionMetaDataQuery
import com.teva.devices.dataquery.DeviceDataQuery
import com.teva.devices.dataquery.InhaleEventDataQuery
import com.teva.devices.entities.Device
import com.teva.devices.entities.InhaleEvent
import com.teva.devices.enumerations.InhalerNameType
import com.teva.devices.mocks.HandlerHelper
import com.teva.devices.model.utils.Model
import com.teva.devices.service.ConnectionInfo
import com.teva.devices.service.DeviceService
import com.teva.devices.service.ProtocolType
import com.teva.devices.utils.Matchers.matchesConnectionInfoList
import com.teva.devices.utils.Matchers.matchesDevice
import com.teva.devices.utils.Matchers.matchesDeviceList
import com.teva.notifications.models.NotificationManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.threeten.bp.*
import java.util.*

/**
 * This class defines unit tests for the DeviceQueryImpl class.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class DeviceQueryTests {

    private var dependencyProvider: DependencyProvider = DependencyProvider.default
    private var messenger: Messenger = mock()
    private var deviceDataQuery: DeviceDataQuery = mock()
    private var deviceService: DeviceService = mock()
    private var activeDevices: MutableList<Device> = mock()
    private var timeService: TimeService = mock()
    private var connectionMetaDataQuery: ConnectionMetaDataQuery = mock()
    private var inhaleEventDataQuery: InhaleEventDataQuery = mock()

    private var connectionInfoList: List<ConnectionInfo> = mock()
    private var context: Context = mock()
    private var notificationManager: NotificationManager = mock()
    internal val GMT_ZONE_ID = ZoneId.ofOffset("GMT", ZoneOffset.UTC)

    private var connectionInfo: ConnectionInfo? = null

//    @Captor
//    private val connectionInfoListArgumentCaptor: ArgumentCaptor<List<ConnectionInfo>>? = null

    /**
     * This method sets up the mock classes and methods required for test execution.

     * @throws Exception -  an exception is thrown if constructor mocking fails.
     */
    @Before
    @Throws(Exception::class)
    fun setup() {
        val GMT_ZONE_ID = ZoneId.ofOffset("GMT", ZoneOffset.UTC)

        DependencyProvider.default.unregisterAll()
        HandlerHelper.clearQueue()

        dependencyProvider.register(Messenger::class, messenger)
        dependencyProvider.register(TimeService::class, timeService)
        whenever(timeService.now()).thenReturn(Instant.now())
        whenever(timeService.today()).thenReturn(LocalDate.of(2017, 1, 13))
        dependencyProvider.register(DeviceService::class, deviceService)
        dependencyProvider.register(Context::class, context)
        dependencyProvider.register(ConnectionMetaDataQuery::class, connectionMetaDataQuery)
        dependencyProvider.register(NotificationManager::class, notificationManager)
        doReturn(ArrayList<InhaleEvent>()).whenever(inhaleEventDataQuery).get(any<Device>())
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)
        val sharedPreferencesEditor = mock<SharedPreferences.Editor>()
        val sharedPreferences = mock<SharedPreferences>()
        whenever(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor)
        dependencyProvider.register(SharedPreferences::class, sharedPreferences)

        val changedTime = Instant.from(ZonedDateTime.of(2017, 1, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)
        activeDevices = ArrayList<Device>()
        val device1 = Model.Device(true, "", 60, expirationDate, "9.1", "2.3", InhalerNameType.HOME,
                changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1", 58, "123454321",
                "i1n2h3a4l5e6", true, changedTime, "745750")
        val device2 = Model.Device(true, "", 60, expirationDate, "9.1", "2.3", InhalerNameType.WORK,
                changedTime.minusSeconds(4800), "", 10, "Inhalation Inc.", "work1", 58, "125434521",
                "i2n3h4a5l6e7", true, changedTime, "745750")
        activeDevices.add(device1)
        activeDevices.add(device2)

        connectionInfoList = createConnectionInfoForTwoDevices()
        connectionInfo = connectionInfoList[0]

        whenever(deviceDataQuery.getAllActive()).thenReturn(activeDevices)
        whenever(deviceDataQuery.getAll()).thenReturn(activeDevices)
        whenever(deviceDataQuery.get(eq(device1.serialNumber))).thenReturn(device1)
        whenever(deviceDataQuery.hasData()).thenReturn(true)
        whenever(deviceDataQuery.lastConnectedActiveReliever()).thenReturn(device1)
    }

    @Test
    fun testDeviceQueryHasDevicesDelegatesToDeviceDataQuery() {

        //initialize test data
        whenever(deviceDataQuery.hasData()).thenReturn(false)

        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        var returnedValue = deviceManager.deviceQuery.getHasDevices()

        // test expectations
        // verify that the hasData method on device data query is invoked
        // and the response is returned correctly
        verify(deviceDataQuery).hasData()
        assertFalse(returnedValue)

        whenever(deviceDataQuery.hasData()).thenReturn(true)
        returnedValue = deviceManager.deviceQuery.getHasDevices()

        assertTrue(returnedValue)
    }

    @Test
    fun testGetConnectedDevicesCountReturnsTheCorrectValue() {

        // create expectations
        val connectedDeviceInfo = connectionInfoList[0]

        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        deviceManager.start()
        // send connect notification for one device.
        deviceManager.onConnected(connectedDeviceInfo)
        var connectedDevicesCount = deviceManager.deviceQuery.getConnectedDeviceCount()

        // test expectations
        // verify that after a connect notification the connected device count is 1.
        assertEquals(1, connectedDevicesCount.toLong())

        // send disconnect notification for the connected device.
        deviceManager.onDisconnected(connectedDeviceInfo)
        connectedDevicesCount = deviceManager.deviceQuery.getConnectedDeviceCount()

        //verify that after a disconnect notification the connected device count is 0.
        assertEquals(0, connectedDevicesCount.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testRemoveDeviceMarksTheDeviceAsDeleted() {
        // create expectations
        val updatedConnectionInfoList = ArrayList<ConnectionInfo>()
        updatedConnectionInfoList.add(connectionInfoList[1])

        val updatedActiveDevices = ArrayList<Device>()
        updatedActiveDevices.add(activeDevices[1])

        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        deviceManager.start()
        whenever(deviceDataQuery.getAllActive()).thenReturn(updatedActiveDevices)
        deviceManager.deviceQuery.removeDevice(activeDevices[0])
        HandlerHelper.loopHandler(2)

        val connectionInfoListArgumentCaptor = argumentCaptor<List<ConnectionInfo>>()

        // test expectations
        // verify that the mark as deleted method is called.
        verify(deviceDataQuery).markAsDeleted(eq(activeDevices[0]))
        verify(deviceService, times(2)).setConnectionInfo(connectionInfoListArgumentCaptor.capture())

        // verify that the connection info is set without the removed device.
        assertThat(connectionInfoListArgumentCaptor.allValues[1], matchesConnectionInfoList(updatedConnectionInfoList))
    }

    @Test
    fun testUndoRemoveDeviceMarksDeviceAsNotDeleted() {

        // create expectations
        val updatedActiveDevices = ArrayList<Device>()
        updatedActiveDevices.add(activeDevices[1])

        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        deviceManager.start()
        whenever(deviceDataQuery.getAllActive()).thenReturn(updatedActiveDevices)
        deviceManager.deviceQuery.removeDevice(activeDevices[0])
        HandlerHelper.loopHandler()
        whenever(deviceDataQuery.getAllActive()).thenReturn(activeDevices)
        deviceManager.deviceQuery.undoRemoveDevice(activeDevices[0])
        HandlerHelper.loopHandler(2)

        val connectionInfoListArgumentCaptor = argumentCaptor<List<ConnectionInfo>>()

        // test expectations
        // verify that the undo mark as deleted method is called.
        verify(deviceDataQuery).undoMarkAsDeleted(eq(activeDevices[0]))
        verify(deviceService, times(3)).setConnectionInfo(connectionInfoListArgumentCaptor.capture())

        // verify that the connection info is set with the re-activated device.
        assertThat(connectionInfoListArgumentCaptor.allValues[2], matchesConnectionInfoList(connectionInfoList))
    }

    @Test
    fun testInsertDeviceSetsTheCorrectConnectionInfo() {

        // create expectations
        val changedTime = Instant.from(ZonedDateTime.of(2017, 1, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)
        val newDevice = Model.Device(true, "", 70, expirationDate, "9.1", "2.3", InhalerNameType.HOME,
                changedTime.minusSeconds(2400), "", 10, "Inhalation Inc.", "home1", 68, "567898765",
                "i3n4h5a6l7e8", true, changedTime, "745750")

        val updatedActiveDevices = ArrayList<Device>()
        updatedActiveDevices.addAll(activeDevices)
        updatedActiveDevices.add(newDevice)

        val newConnectionInfo = ConnectionInfo()
        newConnectionInfo.serialNumber = newDevice.serialNumber
        newConnectionInfo.authenticationKey = newDevice.authenticationKey
        newConnectionInfo.lastRecordId = newDevice.lastRecordId
        newConnectionInfo.protocolType = ProtocolType.Inhaler

        val updatedConnectionInfoList = ArrayList<ConnectionInfo>()
        updatedConnectionInfoList.addAll(connectionInfoList)
        updatedConnectionInfoList.add(newConnectionInfo)


        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        deviceManager.start()
        whenever(deviceDataQuery.getAllActive()).thenReturn(updatedActiveDevices)
        deviceManager.deviceQuery.insert(newDevice, true)
        HandlerHelper.loopHandler(2)

        val connectionInfoListArgumentCaptor = argumentCaptor<List<ConnectionInfo>>()

        // test expectations
        //verify that the insert method on device data query is invoked to add the device to the database.
        verify(deviceDataQuery).insert(eq(newDevice), eq(true))
        verify(deviceService, times(2)).setConnectionInfo(connectionInfoListArgumentCaptor.capture())

        // verify that the connection info is set with the newly added device.
        assertThat(connectionInfoListArgumentCaptor.allValues[1], matchesConnectionInfoList(updatedConnectionInfoList))
    }

    @Test
    fun testInsertDevicesSetsTheCorrectConnectionInfo() {

        // create expectations
        val emptyDeviceList = ArrayList<Device>()

        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        whenever(deviceDataQuery.getAllActive()).thenReturn(emptyDeviceList)
        deviceManager.start()
        whenever(deviceDataQuery.getAllActive()).thenReturn(activeDevices)
        deviceManager.deviceQuery.insert(activeDevices, true)
        HandlerHelper.loopHandler(2)

        val connectionInfoListArgumentCaptor = argumentCaptor<List<ConnectionInfo>>()

        // test expectations
        //verify that the insert method on device data query is invoked to add the device to the database.
        verify(deviceDataQuery).insert(eq(activeDevices), eq(true))
        verify(deviceService, times(2)).setConnectionInfo(connectionInfoListArgumentCaptor.capture())

        // verify that the connection info is set with the newly added devices.
        assertThat(connectionInfoListArgumentCaptor.allValues[1], matchesConnectionInfoList(connectionInfoList))
    }

    @Test
    fun testUpdateDevicesSetsTheCorrectConnectionInfo() {

        // create expectations
        connectionInfoList[0].authenticationKey = "i6n5h4a3l2e1"

        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        deviceManager.start()
        activeDevices[0].authenticationKey = "i6n5h4a3l2e1"
        whenever(deviceDataQuery.getAllActive()).thenReturn(activeDevices)
        deviceManager.deviceQuery.update(activeDevices, true)
        HandlerHelper.loopHandler(2)

        val connectionInfoListArgumentCaptor = argumentCaptor<List<ConnectionInfo>>()

        // test expectations
        //verify that the update method on device data query is invoked to update devices in the database.
        verify(deviceDataQuery).update(eq(activeDevices), eq(true))
        verify(deviceService, times(2)).setConnectionInfo(connectionInfoListArgumentCaptor.capture())

        // verify that the connection info is set with the updated device information.
        assertThat(connectionInfoListArgumentCaptor.allValues[1], matchesConnectionInfoList(connectionInfoList))
    }

    @Test
    fun testInsertOrUpdateDeviceSetsTheCorrectConnectionInfo() {

        // create expectations
        val changedTime = Instant.from(ZonedDateTime.of(2017, 1, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)
        val newDevice = Model.Device(true, "", 70, expirationDate, "9.1", "2.3", InhalerNameType.HOME,
                changedTime.minusSeconds(2400), "", 10, "Inhalation Inc.", "home1", 68, "567898765",
                "i3n4h5a6l7e8", true, changedTime, "745750")

        val updatedActiveDevices = ArrayList<Device>()
        updatedActiveDevices.addAll(activeDevices)
        updatedActiveDevices.add(newDevice)

        val newConnectionInfo = ConnectionInfo()
        newConnectionInfo.serialNumber = newDevice.serialNumber
        newConnectionInfo.authenticationKey = newDevice.authenticationKey
        newConnectionInfo.lastRecordId = newDevice.lastRecordId
        newConnectionInfo.protocolType = ProtocolType.Inhaler

        val updatedConnectionInfoList = ArrayList<ConnectionInfo>()
        updatedConnectionInfoList.addAll(connectionInfoList)
        updatedConnectionInfoList.add(newConnectionInfo)

        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        deviceManager.start()
        whenever(deviceDataQuery.getAllActive()).thenReturn(updatedActiveDevices)
        deviceManager.deviceQuery.insertOrUpdate(newDevice, true)
        HandlerHelper.loopHandler(2)

        val connectionInfoListArgumentCaptor = argumentCaptor<List<ConnectionInfo>>()

        // test expectations
        // verify that insertOrUpdate is invoked on the device data query to insert or update
        // the device in the database.
        verify(deviceDataQuery).insertOrUpdate(eq(newDevice), eq(true))
        verify(deviceService, times(2)).setConnectionInfo(connectionInfoListArgumentCaptor.capture())

        // verify that the connection info is set with the updated/inserted device information.
        assertThat(connectionInfoListArgumentCaptor.allValues[1], matchesConnectionInfoList(updatedConnectionInfoList))
    }

    @Test
    fun testDeleteDeviceSetsTheCorrectConnectionInfo() {

        // create expectations
        val updatedConnectionInfoList = ArrayList<ConnectionInfo>()
        updatedConnectionInfoList.add(connectionInfoList[1])

        val updatedActiveDevices = ArrayList<Device>()
        updatedActiveDevices.add(activeDevices[1])

        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        deviceManager.start()
        whenever(deviceDataQuery.getAllActive()).thenReturn(updatedActiveDevices)
        deviceManager.deviceQuery.delete(activeDevices[0])
        HandlerHelper.loopHandler(2)

        val connectionInfoListArgumentCaptor = argumentCaptor<List<ConnectionInfo>>()

        // test expectations
        // verify that delete is invoked on the device data query to delete the device from the database.
        verify(deviceDataQuery).delete(eq(activeDevices[0]))
        verify(deviceService, times(2)).setConnectionInfo(connectionInfoListArgumentCaptor.capture())

        // verify that the connection info is set without the deleted device information.
        assertThat(connectionInfoListArgumentCaptor.allValues[1], matchesConnectionInfoList(updatedConnectionInfoList))
    }

    @Test
    fun testResetDeviceChangedFlagDoesNotSetConnectionInfo() {

        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        deviceManager.deviceQuery.resetChangedFlag(activeDevices[0], false)

        // test expectations
        // verify that resetChangedFlag is invoked on the device data query to update the device in the database.
        verify(deviceDataQuery).resetChangedFlag(eq(activeDevices[0]), eq(false))

        // verify that the connection info is not set when changed flag is reset.
        verify(deviceService, never()).setConnectionInfo(any())
    }

    @Test
    fun testGetAllDevicesReturnsDeviceInformationAugmentedWithConnectionStatus() {

        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)

        // send a connect notification for the first device in our list of devices
        deviceManager.onConnected(connectionInfo!!)
        val allDevices = deviceManager.deviceQuery.getAll()

        // test expectations
        // verify that the getAll method of device data query is invoked to retrieve all the devices.
        verify(deviceDataQuery).getAll()

        // verify that the devices are augmented with connection information
        // the first device in the returned devices is marked as connected.
        assertTrue(allDevices[0].isConnected)

        // the second device in the returned devices is marked as not connected.
        assertFalse(allDevices[1].isConnected)
    }

    @Test
    fun testIfDeviceQueryHasDataRetrievesInformationFromDeviceDataQuery() {
        // create expectations
        whenever(deviceDataQuery.hasData()).thenReturn(false)

        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        var returnedValue = deviceManager.deviceQuery.hasData()

        // test expectations
        // verify that the hasData method of the device data query is invoked.
        verify(deviceDataQuery).hasData()
        // verify that the value received from device data query is returned.
        assertFalse(returnedValue)

        // perform operation
        whenever(deviceDataQuery.hasData()).thenReturn(true)
        returnedValue = deviceManager.deviceQuery.hasData()

        // test expectations
        // verify that the hasData method of the device data query is invoked twice.
        verify(deviceDataQuery, times(2)).hasData()
        // verify that the value received from device data query is returned.
        assertTrue(returnedValue)
    }

    @Test
    fun testGetLastActiveControllerAndRelieverRetrieveInformationFromDeviceDataQuery() {
        // create expectations
        val changedTime = Instant.from(ZonedDateTime.of(2017, 1, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)
        val controller = Model.Device(true, "", 60, expirationDate, "9.1", "2.3", InhalerNameType.HOME,
                changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1", 58, "123454321",
                "i1n2h3a4l5e6", true, changedTime, "745750")
        val reliever = Model.Device(true, "", 60, expirationDate, "9.1", "2.3", InhalerNameType.WORK,
                changedTime.minusSeconds(4800), "", 10, "Inhalation Inc.", "work1", 58, "125434521",
                "i2n3h4a5l6e7", true, changedTime, "745752")

        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        doReturn(controller).whenever(deviceDataQuery).lastConnectedActiveController()
        doReturn(reliever).whenever(deviceDataQuery).lastConnectedActiveReliever()

        val returnedController = deviceManager.deviceQuery.lastConnectedActiveController()
        val returnedReliever = deviceManager.deviceQuery.lastConnectedActiveReliever()

        // test expectations
        //verify that the methods on the device data query are invoked.
        verify(deviceDataQuery).lastConnectedActiveController()
        verify(deviceDataQuery).lastConnectedActiveReliever()
        // verify that the values received from the device data query are returned correctly.
        assertThat<Device>(returnedController, matchesDevice(controller))
        assertThat<Device>(returnedReliever, matchesDevice(reliever))
    }

    @Test
    fun testGetDeviceBySerialNumberReturnsDeviceInformationAugmentedWithConnectionStatus() {
        // create expectations
        val connectedDeviceInfo = connectionInfoList[0]

        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        deviceManager.start()
        // send a connect notification for the device we are about to retrieve.
        deviceManager.onConnected(connectedDeviceInfo)
        val device = deviceManager.deviceQuery.get(activeDevices[0].serialNumber)

        // test expectations
        // verify that the get method of the device data query is invoked to retrieve device information.
        verify(deviceDataQuery, atLeastOnce()).get(eq(activeDevices[0].serialNumber))

        // verify that the retrieved device information is  correct.
        assertEquals(activeDevices[0], device)

        // verify that the device is augmented with the connection information.
        assertTrue(device!!.isConnected)
    }

    @Test
    fun testGetAllChangedDevicesReturnsDeviceInformationAugmentedWithConnectionStatus() {
        //initialize test data
        val changedDevices = ArrayList<Device>()
        changedDevices.add(activeDevices[0])
        changedDevices.add(activeDevices[1])
        whenever(deviceDataQuery.getAllChanged()).thenReturn(changedDevices)
        val connectedDeviceInfo = connectionInfoList[0]

        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        deviceManager.start()
        // send a connect notification for the first changed device.
        deviceManager.onConnected(connectedDeviceInfo)
        val returnedChangedDevices = deviceManager.deviceQuery.getAllChanged()

        // test expectations
        // verify that the getAllChanged method of device data query is onvoked to obtain the changed devices.
        verify(deviceDataQuery).getAllChanged()
        // verify that the list of changed devices obtained is correct.
        assertThat<List<Device>>(changedDevices, matchesDeviceList(returnedChangedDevices))
        // verify that the returned changed devices are augmented with the connection status.
        // verify that the first device is marked as connected.
        assertTrue(changedDevices[0].isConnected)
        // verify that the second device is marked as not connected.
        assertFalse(changedDevices[1].isConnected)
    }

    @Test
    fun testDeviceQueryHasActiveDevicesRetrievesInformationFromDeviceDataQuery() {
        // create expectations
        whenever(deviceDataQuery.hasActiveDevices()).thenReturn(false)

        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        var returnedValue = deviceManager.deviceQuery.hasActiveDevices()

        // test expectations
        // verify that the hasActiveDevices method of the device data query is invoked.
        verify(deviceDataQuery).hasActiveDevices()
        // verify that the value received from device data query is returned correctly.
        assertFalse(returnedValue)

        // perform operation
        whenever(deviceDataQuery.hasActiveDevices()).thenReturn(true)
        returnedValue = deviceManager.deviceQuery.hasActiveDevices()

        // test expectations
        // verify that the hasActiveDevices method of the device data query is invoked twice.
        verify(deviceDataQuery, times(2)).hasActiveDevices()
        // verify that the value received from device data query is returned correctly.
        assertTrue(returnedValue)
    }

    @Test
    fun testMarkDeviceAsDeletedSetsTheCorrectConnectionInfo() {
        // create expectations
        val updatedConnectionInfoList = ArrayList<ConnectionInfo>()
        updatedConnectionInfoList.add(connectionInfoList[1])

        val updatedActiveDevices = ArrayList<Device>()
        updatedActiveDevices.add(activeDevices[1])

        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        deviceManager.start()
        whenever(deviceDataQuery.getAllActive()).thenReturn(updatedActiveDevices)
        deviceManager.deviceQuery.markAsDeleted(activeDevices[0])
        HandlerHelper.loopHandler(2)

        val connectionInfoListArgumentCaptor = argumentCaptor<List<ConnectionInfo>>()

        // test expectations
        // verify that the device is marked as inactive.
        assertEquals(false, activeDevices[0].isActive)
        // verify that the update method of the device data query is invoked to set the
        // device information in the database.
        verify(deviceDataQuery).update(eq(activeDevices[0]), eq(true))
        // verify that the connection info is set twice, one for start and the second
        // for the markAsDelete
        verify(deviceService, times(2)).setConnectionInfo(connectionInfoListArgumentCaptor.capture())

        // verify that the connection info is set the second time without the deleted device information.
        assertThat(connectionInfoListArgumentCaptor.allValues[1], matchesConnectionInfoList(updatedConnectionInfoList))
    }

    @Test
    fun testUndoMarkDeviceAsDeletedSetsTheCorrectConnectionInfo() {
        // create expectations
        val updatedActiveDevices = ArrayList<Device>()
        updatedActiveDevices.add(activeDevices[1])

        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        deviceManager.start()
        whenever(deviceDataQuery.getAllActive()).thenReturn(updatedActiveDevices)
        deviceManager.deviceQuery.markAsDeleted(activeDevices[0])
        HandlerHelper.loopHandler()
        whenever(deviceDataQuery.getAllActive()).thenReturn(activeDevices)
        deviceManager.deviceQuery.undoMarkAsDeleted(activeDevices[0])
        HandlerHelper.loopHandler(2)

        val connectionInfoListArgumentCaptor = argumentCaptor<List<ConnectionInfo>>()

        // test expectations
        // verify that the device is marked as active.
        assertEquals(true, activeDevices[0].isActive)
        // verify that the upadte method of device data query is invoked twice to save the
        // device information in database - for markAsDelete and undoMarkAsDelete
        verify(deviceDataQuery, times(2)).update(eq(activeDevices[0]), eq(true))

        // verify that the connection info is set thrice - for start, markAsDelete and undoMarkAsDelete.
        verify(deviceService, times(3)).setConnectionInfo(connectionInfoListArgumentCaptor.capture())

        // verify that the connection info is set with the re-activated device the third time.
        assertThat(connectionInfoListArgumentCaptor.allValues[2], matchesConnectionInfoList(connectionInfoList))
    }

    @Test
    fun testDeviceQueryHasDeviceWithNicknameRetrievesInformationFromDeviceDataQuery() {
        // create expectations
        whenever(deviceDataQuery.has(eq(activeDevices[0].nickname))).thenReturn(true)

        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        var returnedValue = deviceManager.deviceQuery.has(activeDevices[0].nickname)

        // test expectations
        // verify that the has method of device data query is invoked to retrieve device information.
        verify(deviceDataQuery).has(eq(activeDevices[0].nickname))
        // verify that the value received from the device data query is returned correctly.
        assertTrue(returnedValue)

        // perform operation
        returnedValue = deviceManager.deviceQuery.has("NonExistentDevice")

        // test expectations
        // verify that the has method of device data query is invoked to retrieve device information.
        verify(deviceDataQuery).has(eq("NonExistentDevice"))
        // verify that the value received from the device data query is returned correctly.
        assertFalse(returnedValue)
    }

    /**
     * This is a helper method which creates a list containing two connection info objects.

     * @return - a list containing two connection info objects.
     */
    private fun createConnectionInfoForTwoDevices(): List<ConnectionInfo> {
        val changedTime = Instant.from(ZonedDateTime.of(2017, 1, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)
        val device1 = Model.Device(true, "", 60, expirationDate, "9.1", "2.3", InhalerNameType.HOME,
                changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1", 58, "123454321",
                "i1n2h3a4l5e6", true, changedTime, "745750")
        val device2 = Model.Device(true, "", 60, expirationDate, "9.1", "2.3", InhalerNameType.WORK,
                changedTime.minusSeconds(4800), "", 10, "Inhalation Inc.", "work1", 58, "125434521",
                "i2n3h4a5l6e7", true, changedTime, "745750")

        val connectionInfo1 = ConnectionInfo()
        connectionInfo1.serialNumber = device1.serialNumber
        connectionInfo1.authenticationKey = device1.authenticationKey
        connectionInfo1.lastRecordId = device1.lastRecordId
        connectionInfo1.protocolType = ProtocolType.Inhaler

        val connectionInfo2 = ConnectionInfo()
        connectionInfo2.serialNumber = device2.serialNumber
        connectionInfo2.authenticationKey = device2.authenticationKey
        connectionInfo2.lastRecordId = device2.lastRecordId
        connectionInfo2.protocolType = ProtocolType.Inhaler

        val connectionInfoList = ArrayList<ConnectionInfo>()
        connectionInfoList.add(connectionInfo1)
        connectionInfoList.add(connectionInfo2)

        return connectionInfoList
    }
}
