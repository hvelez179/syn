///
// DeviceInfoViewModelTest.java
// app
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.respiratoryapp.activity.viewmodel.device

import com.nhaarman.mockito_kotlin.*
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.common.utilities.Messenger
import com.teva.devices.entities.Device
import com.teva.devices.messages.UpdateDeviceMessage
import com.teva.devices.model.DeviceQuery
import com.teva.medication.entities.Medication
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.common.messages.ModelUpdateType
import com.teva.respiratoryapp.common.messages.ModelUpdatedMessage
import com.teva.respiratoryapp.mocks.MockedLocalizationService
import com.teva.respiratoryapp.services.alert.AlertButton
import com.teva.respiratoryapp.services.alert.SystemAlertManager
import com.teva.respiratoryapp.testutils.BaseTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Duration
import java.util.*
import com.teva.respiratoryapp.testutils.mocks.HandlerHelper

class DeviceInfoViewModelTest : BaseTest() {

    private lateinit var messenger: Messenger
    private lateinit var device: Device
    private lateinit var deviceQuery: DeviceQuery
    private lateinit var systemAlertManager: SystemAlertManager
    private lateinit var infoEvents: DeviceInfoViewModel.InfoEvents

    private lateinit var dependencyProvider: DependencyProvider

    @Before
    @Throws(Exception::class)
    fun setUp() {
        dependencyProvider = DependencyProvider.default

        messenger = mock()
        dependencyProvider.register(Messenger::class, messenger)

        val medication = Medication()
        medication.brandName = BRANDNAME
        device = Device()
        device.serialNumber = SERIAL_NUMBER
        device.nickname = NICKNAME
        device.medication = medication
        device.disconnectedTimeSpan = Duration.ZERO

        deviceQuery = mock()
        whenever(deviceQuery.get(SERIAL_NUMBER)).thenReturn(device)
        dependencyProvider.register(DeviceQuery::class, deviceQuery)

        // initialize localization service
        val localizationService = MockedLocalizationService()
        dependencyProvider.register(LocalizationService::class, localizationService)
        localizationService.add(R.string.deviceStateConnected_text, CONNECTED)
        localizationService.add(R.string.deviceStateSearching_text, LAST_CONNECTED_LESS_THAN_24_HOURS)
        localizationService.add(R.string.deviceStateDisconnected_text, LAST_CONNECTED_DAYS)
        localizationService.add(R.string.deviceStateDisconnected_one_day_text, LAST_CONNECTED_DAY)
        localizationService.add(R.string.removeDeviceConfirmation_text, QUERY_DELETE_INHALER)

        systemAlertManager = mock()
        dependencyProvider.register(SystemAlertManager::class, systemAlertManager)

        infoEvents = mock()
        dependencyProvider.register(DeviceInfoViewModel.InfoEvents::class, infoEvents)
    }

    @Test
    fun testOnStartSubscribesToMessenger() {
        val viewModel = DeviceInfoViewModel(dependencyProvider, SERIAL_NUMBER, DeviceInfoViewModel.Mode.ADD)

        viewModel.onStart()
        verify(messenger).subscribe(viewModel)
    }

    @Test
    fun testOnStopUnsubscribesFromMessenger() {
        val viewModel = DeviceInfoViewModel(dependencyProvider, SERIAL_NUMBER, DeviceInfoViewModel.Mode.ADD)

        viewModel.onStop()
        verify<Messenger>(messenger).unsubscribeToAll(viewModel)
    }

    @Test
    fun testPropertiesAreUpdatedWithConnectedDevice() {
        val viewModel = DeviceInfoViewModel(dependencyProvider, SERIAL_NUMBER, DeviceInfoViewModel.Mode.ADD)

        device.isConnected = true
        device.disconnectedTimeSpan = SEARCHING_DURATION

        viewModel.onStart()

        assertEquals(NICKNAME, viewModel.nickname)
        assertEquals(BRANDNAME, viewModel.medicationName)
        assertEquals(CONNECTED, viewModel.status)
    }

    @Test
    fun testPropertiesAreUpdatedWithDeviceDisconnectedLessThan24Hours() {
        val viewModel = DeviceInfoViewModel(dependencyProvider, SERIAL_NUMBER, DeviceInfoViewModel.Mode.ADD)

        device.isConnected = false
        device.doseCount = 200
        device.remainingDoseCount = 180
        device.disconnectedTimeSpan = SEARCHING_DURATION

        viewModel.onStart()

        assertEquals(NICKNAME, viewModel.nickname)
        assertEquals(BRANDNAME, viewModel.medicationName)
        assertEquals(LAST_CONNECTED_LESS_THAN_24_HOURS, viewModel.status)
    }

    @Test
    fun testPropertiesAreUpdatedWithDeviceDisconnectedLessThan48Hours() {
        val viewModel = DeviceInfoViewModel(dependencyProvider, SERIAL_NUMBER, DeviceInfoViewModel.Mode.ADD)

        device.isConnected = false
        device.doseCount = 200
        device.remainingDoseCount = 180
        device.disconnectedTimeSpan = OUT_OF_RANGE_DURATION

        viewModel.onStart()

        assertEquals(NICKNAME, viewModel.nickname)
        assertEquals(BRANDNAME, viewModel.medicationName)
        assertEquals(LAST_CONNECTED_DAY_EXPECTED, viewModel.status)
    }

    @Test
    fun testPropertiesAreUpdatedWithDeviceDisconnectedMoreThan48Hours() {
        val viewModel = DeviceInfoViewModel(dependencyProvider, SERIAL_NUMBER, DeviceInfoViewModel.Mode.ADD)

        device.isConnected = false
        device.doseCount = 200
        device.remainingDoseCount = 180
        device.disconnectedTimeSpan = MULTI_DAY_DURATION

        viewModel.onStart()

        assertEquals(NICKNAME, viewModel.nickname)
        assertEquals(BRANDNAME, viewModel.medicationName)
        assertEquals(LAST_CONNECTED_DAYS_EXPECTED, viewModel.status)
    }

    @Test
    fun testModelUpdatedMessageUpdatesDeviceWhenCurrentDeviceChanged() {
        val viewModel = DeviceInfoViewModel(dependencyProvider, SERIAL_NUMBER, DeviceInfoViewModel.Mode.ADD)

        device.isConnected = false
        device.disconnectedTimeSpan = MULTI_DAY_DURATION
        device.doseCount = 200
        device.remainingDoseCount = 180

        val messageDevice = Device()
        messageDevice.serialNumber = SERIAL_NUMBER
        val objectsUpdated = ArrayList<Any>()
        objectsUpdated.add(messageDevice)

        val message = ModelUpdatedMessage(ModelUpdateType.ENTITIES, objectsUpdated)
        viewModel.onModelUpdated(message)

        assertEquals(NICKNAME, viewModel.nickname)
        assertEquals(BRANDNAME, viewModel.medicationName)
        assertEquals(LAST_CONNECTED_DAYS_EXPECTED, viewModel.status)
    }

    @Test
    fun testModelUpdatedMessageDoesNotUpdateDeviceWhenCurrentDeviceNotInMessage() {
        val viewModel = DeviceInfoViewModel(dependencyProvider, SERIAL_NUMBER, DeviceInfoViewModel.Mode.ADD)

        device.isConnected = false
        device.disconnectedTimeSpan = MULTI_DAY_DURATION

        val messageDevice = Device()
        messageDevice.serialNumber = OTHER_SERIAL_NUMBER
        val objectsUpdated = ArrayList<Any>()
        objectsUpdated.add(messageDevice)

        val message = ModelUpdatedMessage(ModelUpdateType.ENTITIES, objectsUpdated)
        viewModel.onModelUpdated(message)

        assertNull(viewModel.nickname)
        assertNull(viewModel.medicationName)
        assertNull(viewModel.status)
    }

    @Test
    fun testUpdateDeviceMessageUpdatesDevice() {
        val viewModel = DeviceInfoViewModel(dependencyProvider, SERIAL_NUMBER, DeviceInfoViewModel.Mode.ADD)

        device.isConnected = false
        device.disconnectedTimeSpan = MULTI_DAY_DURATION
        device.doseCount = 200
        device.remainingDoseCount = 180

        val messageDevice = Device()
        messageDevice.serialNumber = SERIAL_NUMBER
        val objectsUpdated = ArrayList<Any>()
        objectsUpdated.add(messageDevice)

        val message = UpdateDeviceMessage()
        viewModel.onUpdateDeviceMessage(message)

        assertEquals(NICKNAME, viewModel.nickname)
        assertEquals(BRANDNAME, viewModel.medicationName)
        assertEquals(LAST_CONNECTED_DAYS_EXPECTED, viewModel.status)
    }

    @Test
    fun testRemoveInhalerButtonClickRemovesInhalerAndFiresDoneEventWithDeletedEnum() {
        // mock device query to indicate that there are still active devices registered.
        whenever(deviceQuery.hasActiveDevices()).thenReturn(true)

        val viewModel = DeviceInfoViewModel(dependencyProvider, SERIAL_NUMBER, DeviceInfoViewModel.Mode.ADD)

        // load the device info
        viewModel.onStart()

        // simulate a Remove Inhaler button click.
        viewModel.onRemoveInhalerClicked()

        // verify that an alert is displayed
        val listenerArgumentCaptor = argumentCaptor<(AlertButton)->Unit>()
        verify<SystemAlertManager>(systemAlertManager).showQuery(
                id = isNull(),
                message = eq(QUERY_DELETE_INHALER),
                messageId = isNull(),
                title = isNull(),
                titleId = isNull(),
                primaryButtonTextId = eq(R.string.yes_text),
                secondaryButtonTextId = eq(R.string.no_text),
                onClick = listenerArgumentCaptor.capture())

        // simulate an OK click on the alert.
        val listener = listenerArgumentCaptor.lastValue
        listener(AlertButton.PRIMARY)

        HandlerHelper.loopHandler()



        // verify device is marked as deleted
        verify(deviceQuery).markAsDeleted(device)

        // verify the screen indicates that it is done.
        verify(infoEvents).onDone(DeviceInfoViewModel.Mode.DELETED)
    }

    @Test
    fun testRemoveLastInhalerButtonClickRemovesInhalerAndFiresDoneEventWithDeletedEnum() {
        // mock device query to indicate that there no more active inhalers registered.
        whenever(deviceQuery.hasActiveDevices()).thenReturn(false)

        val viewModel = DeviceInfoViewModel(dependencyProvider, SERIAL_NUMBER, DeviceInfoViewModel.Mode.ADD)

        // load the device info
        viewModel.onStart()

        // simulate a Remove Inhaler button click.
        viewModel.onRemoveInhalerClicked()

        // verify that an alert is displayed
        val listenerArgumentCaptor = argumentCaptor<(AlertButton)->Unit>()
        verify(systemAlertManager).showQuery(
                id = isNull(),
                message = eq(QUERY_DELETE_INHALER),
                messageId = isNull(),
                title = isNull(),
                titleId = isNull(),
                primaryButtonTextId = eq(R.string.yes_text),
                secondaryButtonTextId = eq(R.string.no_text),
                onClick = listenerArgumentCaptor.capture())

        // simulate an OK click on the alert.
        val listener = listenerArgumentCaptor.lastValue
        listener(AlertButton.PRIMARY)

        HandlerHelper.loopHandler()

        // verify device is marked as deleted
        verify(deviceQuery).markAsDeleted(device)

        // verify the screen indicates that it is done.
        verify(infoEvents).onDone(DeviceInfoViewModel.Mode.DELETED_LAST)
    }

    @Test
    fun testRemoveInhalerButtonClickDisplaysAlertAndCancelsRemoveWhenNegativeButtonIsPressed() {
        val viewModel = DeviceInfoViewModel(dependencyProvider, SERIAL_NUMBER, DeviceInfoViewModel.Mode.ADD)

        // load the device info
        viewModel.onStart()

        // simulate a Remove Inhaler button click.
        viewModel.onRemoveInhalerClicked()

        // verify that an alert is displayed
        val listenerArgumentCaptor = argumentCaptor<(AlertButton)->Unit>()
        verify(systemAlertManager).showQuery(
                id = isNull(),
                message = eq(QUERY_DELETE_INHALER),
                messageId = isNull(),
                title = isNull(),
                titleId = isNull(),
                primaryButtonTextId = eq(R.string.yes_text),
                secondaryButtonTextId = eq(R.string.no_text),
                onClick = listenerArgumentCaptor.capture())

        // simulate an OK click on the alert.
        val listener = listenerArgumentCaptor.lastValue
        listener(AlertButton.SECONDARY)

        // verify device is marked as deleted
        verify(deviceQuery, never()).markAsDeleted(device)

        // verify the screen indicates that it is done.
        verify(infoEvents, never()).onDone(any())
    }

    @Test
    fun testOnBackPressedMethodFiresDoneEvent() {
        val viewModel = DeviceInfoViewModel(dependencyProvider, SERIAL_NUMBER, DeviceInfoViewModel.Mode.ADD)

        viewModel.onBackPressed()

        verify(infoEvents).onDone(DeviceInfoViewModel.Mode.ADD)
    }

    @Test
    fun testOnEditNameFiresOnEditDeviceNameEvent() {
        val viewModel = DeviceInfoViewModel(dependencyProvider, SERIAL_NUMBER, DeviceInfoViewModel.Mode.ADD)

        viewModel.onEditName()

        verify(infoEvents).onEditDeviceName(SERIAL_NUMBER)
    }

    companion object {
        private val SERIAL_NUMBER = "12345678901"
        private val OTHER_SERIAL_NUMBER = "12345678902"
        private val NICKNAME = "nickname"
        private val BRANDNAME = "brandname"
        private val SEARCHING_DURATION = Duration.ofHours(4)
        private val OUT_OF_RANGE_DURATION = Duration.ofHours(30)
        private val MULTI_DAY_DURATION = Duration.ofHours(80)

        private val CONNECTED = "connected"
        private val LAST_CONNECTED_LESS_THAN_24_HOURS = "last_connected_less_than_24_hours"
        private val LAST_CONNECTED_DAY = "last_connected_day \$Days$"
        private val LAST_CONNECTED_DAYS = "last_connected_days \$Days$"
        private val QUERY_DELETE_INHALER = "query_delete_inhaler"
        private val LAST_CONNECTED_DAY_EXPECTED = "last_connected_day 1"

        private val LAST_CONNECTED_DAYS_EXPECTED = "last_connected_days 3"
    }
}