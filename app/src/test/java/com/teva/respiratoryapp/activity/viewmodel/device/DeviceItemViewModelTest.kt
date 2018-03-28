//
// DeviceItemViewModelTest.java
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.device

import android.databinding.Observable
import com.nhaarman.mockito_kotlin.*
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.common.utilities.Messenger
import com.teva.devices.dataquery.DeviceDataQuery
import com.teva.devices.entities.Device
import com.teva.devices.model.DeviceQuery
import com.teva.medication.entities.Medication
import com.teva.respiratoryapp.BR
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.mocks.MockedLocalizationService
import com.teva.respiratoryapp.services.alert.SystemAlertManager
import com.teva.respiratoryapp.testutils.BaseTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant

class DeviceItemViewModelTest : BaseTest() {

    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var messenger: Messenger
    private lateinit var systemAlertManager: SystemAlertManager
    private lateinit var deviceQuery: DeviceQuery
    private lateinit var deviceListEvents: DeviceListViewModel.DeviceListEvents
    private lateinit var device: Device
    private lateinit var deviceEvents: DeviceListViewModel.DeviceListEvents

    @Before
    fun setup() {
        dependencyProvider = DependencyProvider.default

        messenger = mock()
        dependencyProvider.register(Messenger::class, messenger)

        systemAlertManager = mock()
        dependencyProvider.register(SystemAlertManager::class, systemAlertManager)

        val medication = Medication()
        //medication.lowDosePercentage = 20

        device = Device()
        device.serialNumber = "1"
        device.medication = medication
        device.doseCount = 200
        device.remainingDoseCount = 180

        deviceQuery = mock()

        dependencyProvider.register(DeviceDataQuery::class, deviceQuery)
        dependencyProvider.register(DeviceQuery::class, deviceQuery)

        deviceListEvents = mock()
        dependencyProvider.register(DeviceListViewModel.DeviceListEvents::class, deviceListEvents)

        val localizationService = MockedLocalizationService()
        localizationService.add(R.string.deviceStateConnected_text, CONNECTED)
        localizationService.add(R.string.deviceListStartTracking_text, NEVER_CONNECTED)
        localizationService.add(R.string.deviceStateSearching_text, LAST_CONNECTED_LESS_THAN_24_HOURS)
        localizationService.add(R.string.deviceStateDisconnected_text, LAST_CONNECTED_DAYS)
        localizationService.add(R.string.deviceStateDisconnected_one_day_text, LAST_CONNECTED_DAY)
        localizationService.add(R.string.removeDeviceConfirmation_text, QUERY_DELETE_INHALER)

        dependencyProvider.register(LocalizationService::class, localizationService)

        val timeService: TimeService = mock()
        whenever(timeService.now()).thenReturn(NOW)

        dependencyProvider.register(TimeService::class, timeService)
    }

    @Test
    fun testDeviceStateReturnsNearEmpty() {
        val viewModel = DeviceItemViewModel(dependencyProvider)

        device.remainingDoseCount = 10

        viewModel.setItem(device)

        val state = viewModel.state

        assertEquals(DeviceItemState.NEAR_EMPTY, state)
    }

    @Test
    fun testDeviceStateReturnsConnected() {
        val viewModel = DeviceItemViewModel(dependencyProvider)

        device.isConnected = true

        viewModel.setItem(device)

        val state = viewModel.state

        assertEquals(DeviceItemState.CONNECTED, state)
    }

    @Test
    fun testDeviceStateReturnsDisconnected() {
        val viewModel = DeviceItemViewModel(dependencyProvider)

        device.isConnected = false

        viewModel.setItem(device)

        val state = viewModel.state

        assertEquals(DeviceItemState.DISCONNECTED, state)
    }

    @Test
    fun testStatusMessageReturnsConnected() {
        val viewModel = DeviceItemViewModel(dependencyProvider)
        device.isConnected = true
        viewModel.setItem(device)

        assertEquals(CONNECTED, viewModel.statusMessage)
    }

    @Test
    fun testStatusMessageReturnsNeverConnected() {
        val viewModel = DeviceItemViewModel(dependencyProvider)
        device.remainingDoseCount = device.doseCount
        viewModel.setItem(device)

        assertEquals(NEVER_CONNECTED, viewModel.statusMessage)
    }

    @Test
    fun testStatusMessageReturnsDisconnectedLessThan24Hours() {
        val viewModel = DeviceItemViewModel(dependencyProvider)
        device.lastConnection = NOW_MINUS_1_HOUR
        viewModel.setItem(device)

        val statusMessage = viewModel.statusMessage
        assertEquals(LAST_CONNECTED_LESS_THAN_24_HOURS, statusMessage)
    }

    @Test
    fun testStatusMessageReturnsDisconnectedMoreThan24Hours() {
        val viewModel = DeviceItemViewModel(dependencyProvider)
        device.lastConnection = NOW_MINUS_25_HOUR
        device.doseCount = 200
        device.remainingDoseCount = 180
        viewModel.setItem(device)

        val expectedMessage = "last_connected_day 1"

        val statusMessage = viewModel.statusMessage
        assertEquals(expectedMessage, statusMessage)
    }

    @Test
    fun testSetItemCallsNotifyChange() {
        val viewModel = DeviceItemViewModel(dependencyProvider)

        val callback: Observable.OnPropertyChangedCallback = mock()
        viewModel.addOnPropertyChangedCallback(callback)

        viewModel.setItem(device)

        verify(callback).onPropertyChanged(viewModel, BR._all)
    }

    @Test
    fun testThatOnShowInfoInvokesMethodOnDeviceListEventsToDisplayDeviceInfo() {
        deviceEvents = mock()
        dependencyProvider.register(DeviceListViewModel.DeviceListEvents::class, deviceEvents)
        val viewModel = DeviceItemViewModel(dependencyProvider)
        viewModel.setItem(device)
        viewModel.onShowInfo()

        verify(deviceEvents).showDeviceInfo(device)
    }

    companion object {
        private val QUERY_DELETE_INHALER = "query_delete_inhaler %s"
        private val CONNECTED = "connected"
        private val NEVER_CONNECTED = "never_connected"
        private val LAST_CONNECTED_LESS_THAN_24_HOURS = "last_connected_less_than_24_hours"
        private val LAST_CONNECTED_DAYS = "last_connected_days \$Days$"
        private val LAST_CONNECTED_DAY = "last_connected_day \$Days$"
        private val NOW = Instant.ofEpochSecond(1491226442L)
        private val NOW_MINUS_1_HOUR = Instant.ofEpochSecond(1491222842L)
        private val NOW_MINUS_25_HOUR = Instant.ofEpochSecond(1491119162L)
    }
}