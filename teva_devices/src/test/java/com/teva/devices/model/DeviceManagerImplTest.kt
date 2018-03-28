//
// DeviceManagerImplTest.java
// teva_devices
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.devices.model

import android.annotation.TargetApi
import android.content.SharedPreferences
import android.os.Build
import com.nhaarman.mockito_kotlin.*
import com.teva.common.messages.AppForegroundMessage
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.devices.dataquery.ConnectionMetaDataQuery
import com.teva.devices.dataquery.DeviceDataQuery
import com.teva.devices.dataquery.InhaleEventDataQuery
import com.teva.devices.entities.Device
import com.teva.devices.entities.InhaleEvent
import com.teva.devices.enumerations.InhalerNameType
import com.teva.devices.messages.DeviceConnectedMessage
import com.teva.devices.messages.DeviceDisconnectedMessage
import com.teva.devices.messages.DeviceUpdatedMessage
import com.teva.devices.mocks.HandlerHelper
import com.teva.devices.model.utils.Model
import com.teva.devices.service.*
import com.teva.devices.utils.Matchers.matchesConnectionInfoList
import com.teva.devices.utils.Matchers.matchesDevice
import com.teva.devices.utils.Matchers.matchesInhaleEventList
import com.teva.notifications.models.NotificationManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.threeten.bp.*
import java.util.*

/**
 * This class defines the unit tests for the DeviceManagerImpl class.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class DeviceManagerImplTest {

    private var dependencyProvider: DependencyProvider = DependencyProvider.default
    private var messenger: Messenger = mock()
    private var deviceDataQuery: DeviceDataQuery = mock()
    private var deviceService: DeviceService = mock()
    private var timeService: TimeService = mock()
    internal val GMT_ZONE_ID = ZoneId.ofOffset("GMT", ZoneOffset.UTC)
    private var connectionMetaDataQuery: ConnectionMetaDataQuery = mock()
    private var notificationManager: NotificationManager = mock()
    private var inhaleEventDataQuery: InhaleEventDataQuery = mock()

    /**
     * This method sets up the mock classes and methods required for test execution.

     * @throws Exception -  an exception is thrown if constructor mocking fails.
     */
    @Before
    @Throws(Exception::class)
    fun setup() {
        MockitoAnnotations.initMocks(this)

        DependencyProvider.default.unregisterAll()
        HandlerHelper.clearQueue()

        dependencyProvider.register(Messenger::class, messenger)
        dependencyProvider.register(TimeService::class, timeService)
        whenever(timeService.now()).thenReturn(Instant.from(ZonedDateTime.of(2017, 1, 13, 11, 10, 9, 0, GMT_ZONE_ID)))
        whenever(timeService.today()).thenReturn(LocalDate.of(2017, 1, 13))
        dependencyProvider.register(DeviceService::class, deviceService)
        dependencyProvider.register(ConnectionMetaDataQuery::class, connectionMetaDataQuery)
        dependencyProvider.register(NotificationManager::class, notificationManager)
        whenever(inhaleEventDataQuery.get(any())).thenReturn(ArrayList<InhaleEvent>())
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)
        val sharedPreferencesEditor: SharedPreferences.Editor = mock()
        val sharedPreferences: SharedPreferences = mock()
        whenever(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor)
        dependencyProvider.register(SharedPreferences::class, sharedPreferences)

        val changedTime = Instant.from(ZonedDateTime.of(2017, 1, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)
        val activeDevices = ArrayList<Device>()
        val device1 = Model.Device(true, "", 60, expirationDate, "9.1", "2.3", InhalerNameType.HOME,
                changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1", 58, "123454321",
                "i1n2h3a4l5e6", true, changedTime, "745750")
        val device2 = Model.Device(true, "", 60, expirationDate, "9.1", "2.3", InhalerNameType.WORK,
                changedTime.minusSeconds(4800), "", 10, "Inhalation Inc.", "work1", 58, "125434521",
                "i2n3h4a5l6e7", true, changedTime, "745750")
        activeDevices.add(device1)
        activeDevices.add(device2)

        whenever(deviceDataQuery.getAllActive()).thenReturn(activeDevices)
        whenever(deviceDataQuery.getAll()).thenReturn(activeDevices)
        whenever(deviceDataQuery.get(eq(device1.serialNumber))).thenReturn(device1)
        whenever(deviceDataQuery.hasData()).thenReturn(true)
        whenever(deviceDataQuery.lastConnectedActiveReliever()).thenReturn(device1)
    }

    @Test
    fun testThatStartingDeviceManagerSetsConnectionInfoAndStartsDeviceService() {
        // create expectations
        val connectionInfoList = createConnectionInfoForTwoDevices()

        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        deviceManager.start()

        // test expectations

        // verify that the device service start method is invoked.
        verify(deviceService).start()
        verify(messenger).subscribe(eq(deviceManager))

        val connectionInfoListArgumentCaptor = argumentCaptor<List<ConnectionInfo>>()

        // verify that the correct connection info is set.
        verify(deviceService).setConnectionInfo(connectionInfoListArgumentCaptor.capture())
        assertThat(connectionInfoList, matchesConnectionInfoList(connectionInfoListArgumentCaptor.lastValue))
    }

    @Test
    fun testThatStoppingDeviceManagerStopsDeviceService() {
        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        deviceManager.stop()

        // test expectations

        // verify that the device service stop method is invoked.
        verify(deviceService).stop()
        verify(messenger).unsubscribeToAll(eq(deviceManager))
    }

    @Test
    fun testCheckingBluetoothStatusGetsDelegatedToDeviceService() {
        var bluetoothEnabled = false
        whenever(deviceService.isBluetoothEnabled).thenReturn(bluetoothEnabled)
        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        var returnedValue = deviceManager.isBluetoothEnabled

        // test expectations

        // verify that the isBluetoothEnabled method of the device service is invoked and the value
        // received from the device service is returned.
        verify(deviceService).isBluetoothEnabled
        assertEquals(bluetoothEnabled, returnedValue)

        bluetoothEnabled = true
        whenever(deviceService.isBluetoothEnabled).thenReturn(bluetoothEnabled)
        returnedValue = deviceManager.isBluetoothEnabled

        // verify that the value received from the device service is returned.
        assertEquals(bluetoothEnabled, returnedValue)
    }

    @Test
    fun testAppForegroundStatusIsNotifiedToDeviceService() {
        // initialize test data
        var appForegroundMessage = AppForegroundMessage(true)

        val booleanArgumentCaptor = argumentCaptor<Boolean>()

        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        deviceManager.onAppForeground(appForegroundMessage)

        appForegroundMessage = AppForegroundMessage(false)
        deviceManager.onAppForeground(appForegroundMessage)

        // test expectations
        // verify that the device service is notified of the app foreground status
        // with the correct values.
        verify(deviceService, times(2)).setInForeground(booleanArgumentCaptor.capture())
        val capturedValues = booleanArgumentCaptor.allValues
        assertTrue(capturedValues[0])
        assertFalse(capturedValues[1])
    }

    @Test
    fun testDeviceUpdateMessageSetsConnectionInfoOnTheDeviceService() {
        val DEVICE_UPDATED_MESSAGE_ID = 2

        // create expectations
        val connectionInfoList = createConnectionInfoForTwoDevices()

        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        deviceManager.start()
        deviceManager.onMessage(DEVICE_UPDATED_MESSAGE_ID)

        val connectionInfoListArgumentCaptor = argumentCaptor<List<ConnectionInfo>>()

        // test expectations
        // verify that the connection info is set twice, once during start and once for the update message
        // with the correct values.
        verify(deviceService, times(2)).setConnectionInfo(connectionInfoListArgumentCaptor.capture())
        assertThat(connectionInfoList, matchesConnectionInfoList(connectionInfoListArgumentCaptor.lastValue))
    }

    @Test
    fun testDeviceConnectNotificationTriggersACorrectDeviceConnectedMessage() {
        //initialize test data
        val inhaleEvent = InhaleEvent()
        val inhaleEvents = ArrayList<InhaleEvent>()
        inhaleEvents.add(inhaleEvent)
        val connectionInfo = createConnectionInfo()
        val connectionMetaDataQuery: ConnectionMetaDataQuery = mock()
        dependencyProvider.register(ConnectionMetaDataQuery::class, connectionMetaDataQuery)
        whenever(inhaleEventDataQuery.get(any())).thenReturn(inhaleEvents)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)


        // create expectations
        val changedTime = Instant.from(ZonedDateTime.of(2017, 1, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)
        val device = Model.Device(true, "", 60, expirationDate, "9.1", "2.3", InhalerNameType.HOME,
                changedTime, "", 10, "Inhalation Inc.", "home1", 58, "123454321",
                "i1n2h3a4l5e6", true, changedTime, "745750")
        val deviceConnectedMessageArgumentCaptor = argumentCaptor<DeviceConnectedMessage>()

        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        deviceManager.start()
        deviceManager.onConnected(connectionInfo)

        // test expectations
        // verify that the device connected message is posted with the correct device information.
        verify(messenger).post(deviceConnectedMessageArgumentCaptor.capture())
        assertThat(device, matchesDevice(deviceConnectedMessageArgumentCaptor.lastValue.device))
    }

    @Test
    fun testDeviceDisconnectNotificationTriggersACorrectDeviceDisconnectedMessage() {
        //initialize test data
        val connectionInfo = createConnectionInfo()
        val connectionMetaDataQuery: ConnectionMetaDataQuery = mock()
        dependencyProvider.register(ConnectionMetaDataQuery::class, connectionMetaDataQuery)

        // create expectations
        val changedTime = Instant.from(ZonedDateTime.of(2017, 1, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)
        val device = Model.Device(true, "", 60, expirationDate, "9.1", "2.3", InhalerNameType.HOME,
                changedTime, "", 10, "Inhalation Inc.", "home1", 58, "123454321",
                "i1n2h3a4l5e6", true, changedTime, "745750")

        val deviceDisconnectedMessageArgumentCaptor = argumentCaptor<DeviceDisconnectedMessage>()

        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        deviceManager.start()
        deviceManager.onDisconnected(connectionInfo)

        // test expectations
        //verify that the device disconnected message is posted with the correct device information
        deviceDisconnectedMessageArgumentCaptor.apply {
            verify(messenger).post(capture())
        }

        assertThat(device, matchesDevice(deviceDisconnectedMessageArgumentCaptor.lastValue.device))
    }

    @Test
    fun testDeviceUpdateNotificationTriggersACorrectDeviceUpdatedMessage() {

        // initialize test data
        val deviceInfo = createMockDeviceInfo(2, "Inhalation Inc.", "2.3", "9.1", "", "", 10)
        val deviceSerialNumber = "123454321"
        val drugUID = "745750"
        val connectionInfo = createConnectionInfo()

        val inhaleEventDataQuery: InhaleEventDataQuery = mock()
        whenever(inhaleEventDataQuery.get(any<Int>(), any())).thenReturn(null)
        doNothing().whenever(inhaleEventDataQuery).insert(any<List<InhaleEvent>>(), any())
        val connectionMetaDataQuery: ConnectionMetaDataQuery = mock()
        dependencyProvider.register(ConnectionMetaDataQuery::class, connectionMetaDataQuery)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)

        val inhaleEventInfoList = ArrayList<InhaleEventInfo>()

        val inhaleEventInfo1 = createInhaleEventInfo(1, timeService.now(), 2, 10, 4, 2, 40, 1, true)
        val inhaleEventInfo2 = createInhaleEventInfo(2, timeService.now(), 3, 12, 5, 3, 38, 1, true)

        inhaleEventInfoList.add(inhaleEventInfo1)
        inhaleEventInfoList.add(inhaleEventInfo2)

        // create expectations
        val inhaleEvent1 = inhaleEventInfo1.toInhaleEvent(deviceSerialNumber, drugUID)

        val inhaleEvent2 = inhaleEventInfo2.toInhaleEvent(deviceSerialNumber, drugUID)
        inhaleEvent2.deviceSerialNumber = deviceSerialNumber
        inhaleEvent2.drugUID = drugUID

        val inhaleEventList = ArrayList<InhaleEvent>()
        inhaleEventList.add(inhaleEvent1)
        inhaleEventList.add(inhaleEvent2)

        val changedTime = Instant.from(ZonedDateTime.of(2017, 1, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)

        val device = Model.Device(true, "", 60, expirationDate, "9.1", "2.3", InhalerNameType.HOME,
                changedTime, "", 10, "Inhalation Inc.", "home1", 58, "123454321",
                "i1n2h3a4l5e6", true, changedTime, "745750")

        val booleanArgumentCaptor = argumentCaptor<Boolean>()
        val deviceUpdatedMessageArgumentCaptor = argumentCaptor<DeviceUpdatedMessage>()

        // perform operation
        val deviceManager = DeviceManagerImpl(dependencyProvider, deviceDataQuery)
        deviceManager.start()
        deviceManager.onUpdated(connectionInfo, deviceInfo, inhaleEventInfoList)

        val inhaleEventListArgumentCaptor = argumentCaptor<List<InhaleEvent>>()

        // test expectations
        // verify that inhale events with correct data are inserted in the database
        verify(inhaleEventDataQuery).insert(inhaleEventListArgumentCaptor.capture(), booleanArgumentCaptor.capture())
        assertThat<List<InhaleEvent>>(inhaleEventList, matchesInhaleEventList(inhaleEventListArgumentCaptor.lastValue))

        //verify that the device updated message is posted with the correct device information
        verify(messenger).post(deviceUpdatedMessageArgumentCaptor.capture())
        assertThat(device, matchesDevice(deviceUpdatedMessageArgumentCaptor.lastValue.device))
    }

    /**
     * This is a helper method which create a list containing two connection info objects.

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

    /**
     * This method creates a ConnectionInfo object to be used by tests.
     * @return - a ConnectionInfo object.
     */
    private fun createConnectionInfo(): ConnectionInfo {
        val connectionInfo = ConnectionInfo()
        connectionInfo.serialNumber = "123454321"
        connectionInfo.authenticationKey = "i1n2h3a4l5e6"
        connectionInfo.lastRecordId = 10
        connectionInfo.protocolType = ProtocolType.Inhaler
        return connectionInfo
    }

    /**
     * This is a helper method which creates an InhaleEventInfo object with the given data.
     *
     * @param eventUID          - the inhale event uid.
     * @param eventTime         - the inhale event time.
     * @param inhaleStartOffset - inhalation start offset.
     * @param inhaleDuration    - inhalation duration.
     * @param inhalePeak        - inhalation peak.
     * @param inhalePeakOffset  - inhalation peak offset.
     * @param inhaleVolume      - inhaled volume.
     * @param status            - status.
     * @param isValidInhale     - flag to indicate if this was a valid inhale.
     * @return - the InhaleEventInfo object populated with the given data
     */
    private fun createInhaleEventInfo(eventUID: Int, eventTime: Instant, inhaleStartOffset: Int,
                                      inhaleDuration: Int, inhalePeak: Int, inhalePeakOffset: Int, inhaleVolume: Int,
                                      status: Int, isValidInhale: Boolean): InhaleEventInfo {
        val inhaleEventInfo = InhaleEventInfo()

        inhaleEventInfo.eventUID = eventUID
        inhaleEventInfo.eventTime = eventTime
        inhaleEventInfo.inhaleStartOffset = inhaleStartOffset
        inhaleEventInfo.inhaleDuration = inhaleDuration
        inhaleEventInfo.inhalePeak = inhalePeak
        inhaleEventInfo.inhalePeakOffset = inhalePeakOffset
        inhaleEventInfo.inhaleVolume = inhaleVolume
        inhaleEventInfo.status = status
        inhaleEventInfo.isValidInhale = isValidInhale

        return inhaleEventInfo
    }

    /**
     * This is a helper method which creates a mock device info object and populates it with the given data.
     *
     * @param dosesTaken       - doses taken.
     * @param manufacturerName - device manufacturer name.
     * @param softwareRevision - software revision of the device.
     * @param hardwareRevision - hardware revision of the device.
     * @param dateCode         - the date code.
     * @param lotCode          - the lot code.
     * @param lastRecordId     - the last record Id.
     * @return - mock device info object populated with the given data.
     */
    private fun createMockDeviceInfo(dosesTaken: Int, manufacturerName: String, softwareRevision: String,
                                     hardwareRevision: String, dateCode: String, lotCode: String, lastRecordId: Int): DeviceInfo {
        val deviceInfo = DeviceInfo()
        deviceInfo.dosesTaken = dosesTaken
        deviceInfo.manufacturerName = manufacturerName
        deviceInfo.softwareRevision = softwareRevision
        deviceInfo.hardwareRevision = hardwareRevision
        deviceInfo.dateCode = dateCode
        deviceInfo.lotCode = lotCode
        deviceInfo.lastRecordId = lastRecordId
        return deviceInfo
    }
}
