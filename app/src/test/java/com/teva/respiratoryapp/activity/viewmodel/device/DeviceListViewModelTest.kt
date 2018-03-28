package com.teva.respiratoryapp.activity.viewmodel.device

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.teva.common.utilities.LocalizationService
import com.teva.devices.messages.UpdateDeviceMessage
import com.teva.respiratoryapp.activity.viewmodel.device.DeviceListViewModel.DeviceListEvents
import com.teva.respiratoryapp.common.messages.ModelUpdateType
import com.teva.respiratoryapp.common.messages.ModelUpdatedMessage
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.devices.dataquery.DeviceDataQuery
import com.teva.devices.entities.Device
import com.teva.respiratoryapp.mocks.MockedLocalizationService
import com.teva.respiratoryapp.mvvmframework.ui.FragmentListViewModel
import com.teva.respiratoryapp.testutils.BaseTest

import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant

import java.util.ArrayList

import org.junit.Assert.assertEquals

class DeviceListViewModelTest : BaseTest() {

    private lateinit var localizationService: MockedLocalizationService
    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var messenger: Messenger
    private lateinit var deviceQuery: DeviceDataQuery
    private lateinit var deviceListEvents: DeviceListEvents
    private lateinit var device1: Device
    private lateinit var device2: Device
    private lateinit var device3: Device

    @Before
    fun setup() {
        dependencyProvider = DependencyProvider.default

        messenger = mock()
        dependencyProvider.register(Messenger::class, messenger)

        device1 = Device()
        device1.serialNumber = "1"
        device1.lastConnection = NOW.minusSeconds(10)
        device2 = Device()
        device2.serialNumber = "2"
        device2.lastConnection = NOW.minusSeconds(20)
        device3 = Device()
        device3.serialNumber = "3"
        device3.lastConnection = NOW.minusSeconds(30)

        deviceQuery = mock()
        dependencyProvider.register(DeviceDataQuery::class, deviceQuery)

        deviceListEvents = mock()
        dependencyProvider.register(DeviceListEvents::class, deviceListEvents)

        localizationService = MockedLocalizationService()
        dependencyProvider.register(LocalizationService::class, localizationService)
    }

    @Test
    fun testStartSubscribesToMessengerAndUpdatesList() {
        val activeDevices = ArrayList<Device>()
        activeDevices.add(device1)
        activeDevices.add(device2)
        whenever(deviceQuery.getAllActive()).thenReturn(activeDevices)

        val viewModel = DeviceListViewModel(dependencyProvider)

        viewModel.onStart()

        verify(messenger).subscribe(viewModel)
        verify(deviceQuery).getAllActive()

        val deviceList = viewModel.items
        assertEquals(device1.serialNumber, deviceList[0].serialNumber)
        assertEquals(device2.serialNumber, deviceList[1].serialNumber)
    }

    @Test
    fun testStopUnsubscribesFromMessenger() {
        val activeDevices = ArrayList<Device>()
        activeDevices.add(device1)
        whenever(deviceQuery.getAllActive()).thenReturn(activeDevices)

        val viewModel = DeviceListViewModel(dependencyProvider)

        viewModel.onStop()

        verify(messenger).unsubscribeToAll(viewModel)
    }

    @Test
    fun testOnModelUpdatedUpdatesItems() {
        val activeDevices = ArrayList<Device>()
        activeDevices.add(device3)
        whenever(deviceQuery.getAllActive()).thenReturn(activeDevices)

        val viewModel = DeviceListViewModel(dependencyProvider)

        val messageObjects = ArrayList<Any>()
        messageObjects.add(device3)
        val message = ModelUpdatedMessage(ModelUpdateType.ENTITIES, messageObjects)
        viewModel.onModelUpdatedMessage(message)

        verify(deviceQuery).getAllActive()

        val deviceList = viewModel.items
        assertEquals(device3.serialNumber, deviceList[0].serialNumber)
    }

    @Test
    fun testUpdateDeviceMessageUpdatesItems() {
        val activeDevices = ArrayList<Device>()
        activeDevices.add(device3)
        whenever(deviceQuery.getAllActive()).thenReturn(activeDevices)

        val listChangedListener: FragmentListViewModel.ListChangedListener = mock()

        val viewModel = DeviceListViewModel(dependencyProvider)
        viewModel.listChangedListener = listChangedListener

        val message = UpdateDeviceMessage()
        viewModel.onUpdateDeviceMessage(message)

        verify(listChangedListener).onListChanged()
    }


    @Test
    fun testAddInhalerFiresAddInhalerEvent() {
        val activeDevices = ArrayList<Device>()
        activeDevices.add(device1)
        whenever(deviceQuery.getAllActive()).thenReturn(activeDevices)

        val viewModel = DeviceListViewModel(dependencyProvider)

        viewModel.addInhaler()

        verify(deviceListEvents).addInhaler(false)
    }

    companion object {
        private val NOW = Instant.ofEpochSecond(1491226442L)
    }
}