//
// DeviceDataMonitorTests.kt
// teva_analysis
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.analysis.model.datamonitors

import com.nhaarman.mockito_kotlin.*
import com.teva.analysis.entities.SummaryInfo
import com.teva.analysis.enumerations.SummaryTextId
import com.teva.analysis.messages.UpdateAnalysisDataMessage
import com.teva.analysis.utils.Model
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.devices.DeviceManagerStringReplacementKey
import com.teva.devices.entities.Device
import com.teva.devices.entities.InhaleEvent
import com.teva.devices.enumerations.InhalerNameType
import com.teva.devices.messages.DeviceUpdatedMessage
import com.teva.devices.model.DeviceManagerInhaleEventKey
import com.teva.devices.model.DeviceManagerNotificationId
import com.teva.devices.model.DeviceQuery
import com.teva.medication.enumerations.MedicationClassification
import com.teva.notifications.models.NotificationManager

import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

import java.util.ArrayList

import org.junit.Assert.assertEquals

/**
 * This class defines unit tests for the DeviceDataMonitor class.
 */
class DeviceDataMonitorTests {

    private val dependencyProvider: DependencyProvider = DependencyProvider.default
    private var device: Device? = null
    private val notificationManager: NotificationManager = mock()
    private val deviceQuery: DeviceQuery = mock()
    private val summaryMessageQueue: SummaryMessageQueue = mock()
    private val messenger: Messenger = mock()

//    @Captor
//    private val mapArgumentCaptor: ArgumentCaptor<Map<String, Any>>? = null

    @Before
    fun setup() {

        DependencyProvider.default.unregisterAll()

        dependencyProvider.register(SummaryMessageQueue::class, summaryMessageQueue)

        val changedTime = Instant.from(ZonedDateTime.of(2017, 1, 1, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 1, 1)
        dependencyProvider.register(Messenger::class, messenger)
        device = Model.Device(true, "", 60, expirationDate, "9.1", "2.3", InhalerNameType.WORK,
                changedTime.minusSeconds(4800), "", 10, "Inhalation Inc.", "work1", 58, "125434521",
                "i2n3h4a5l6e7", true, changedTime, "745750")
        val medication = Model.Medication("745750", "ProAir1", "ProAir1", MedicationClassification.RELIEVER, 300, 300, 2, 100, 12)
        //medication.lowDosePercentage = 10
        device!!.medication = medication
        device!!.isConnected = true
        dependencyProvider.register(NotificationManager::class, notificationManager)
    }

    @Test
    fun testDeviceUpdatedMessageDoesNotTriggerLowDoseNotificationIfDosePercentageIsNotLow() {
        // set the remaining dose count higher the low dose percentage
        device!!.remainingDoseCount = 40

        // create a device updated message with an inhale event
        val currentDate = LocalDate.of(2017, 1, 2)
        val currentTime = LocalTime.of(10, 0, 0)
        val eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val inhaleEvent = Model.InhaleEvent(1, eventTime, 0, 23, 1105, 610, 440, 0, 4,
                2, "Cartridge4", 0, 0, true, "123454321")
        val inhaleEventList = ArrayList<InhaleEvent>()
        inhaleEventList.add(inhaleEvent)
        val deviceUpdatedMessage = DeviceUpdatedMessage(device!!, inhaleEventList)

        // send the device updated message to the device data monitor.
        val deviceDataMonitor = DeviceDataMonitor(dependencyProvider)
        deviceDataMonitor.onDeviceUpdated(deviceUpdatedMessage)

        // verify that the near empty notification is not set.
        verify(notificationManager, never()).setNotification(any(), any())
    }

    @Test
    fun testDeviceUpdatedMessageTriggersLowDoseNotificationIfDosePercentageIsLow() {
        // set the remaining dose count lower than the low dose percentage
        device!!.remainingDoseCount = 5

        // create a device updated message with an inhale event.
        val currentDate = LocalDate.of(2017, 1, 2)
        val currentTime = LocalTime.of(10, 0, 0)
        val eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val inhaleEvent = Model.InhaleEvent(1, eventTime, 0, 23, 1105, 610, 440, 0, 4,
                2, "Cartridge4", 0, 0, true, "123454321")
        val inhaleEventList = ArrayList<InhaleEvent>()
        inhaleEventList.add(inhaleEvent)
        val deviceUpdatedMessage = DeviceUpdatedMessage(device!!, inhaleEventList)

        // send the device updated message to the device data monitor
        val deviceDataMonitor = DeviceDataMonitor(dependencyProvider)
        deviceDataMonitor.onDeviceUpdated(deviceUpdatedMessage)

        val mapArgumentCaptor = argumentCaptor<Map<String, Any>>()

        // verify that the near empty notification is set.
        verify(notificationManager).setNotification(eq(DeviceManagerNotificationId.DEVICE_NEAR_EMPTY), mapArgumentCaptor.capture())

        // verify that the notification has the inhale event id, device serial number
        // and device nick name in the notification data.
        assertEquals(inhaleEvent.uniqueId, mapArgumentCaptor.lastValue[DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID])
        assertEquals(device!!.serialNumber, mapArgumentCaptor.lastValue[DeviceManagerInhaleEventKey.DEVICE_ID])
        assertEquals(device!!.nickname, mapArgumentCaptor.lastValue[DeviceManagerStringReplacementKey.NAME])
    }

    @Test
    fun testDeviceUpdatedMessageTriggersLowDoseNotificationEvenIfThereAreNoInhaleEvents() {
        // set the remaining dose count lower than the low dose percentage
        device!!.remainingDoseCount = 5

        // create a device updated message with an inhale event.
        val deviceUpdatedMessage = DeviceUpdatedMessage(device!!, ArrayList<InhaleEvent>())

        // send the device updated message to the device data monitor
        val deviceDataMonitor = DeviceDataMonitor(dependencyProvider)
        deviceDataMonitor.onDeviceUpdated(deviceUpdatedMessage)

        val mapArgumentCaptor = argumentCaptor<Map<String, Any>>()
        
        // verify that the near empty notification is set.
        verify(notificationManager).setNotification(eq(DeviceManagerNotificationId.DEVICE_NEAR_EMPTY), mapArgumentCaptor.capture())

        // verify that the notification has the inhale event id, device serial number
        // and device nick name in the notification data.
        assertEquals(device!!.serialNumber, mapArgumentCaptor.lastValue[DeviceManagerInhaleEventKey.DEVICE_ID])
        assertEquals(device!!.nickname, mapArgumentCaptor.lastValue[DeviceManagerStringReplacementKey.NAME])
    }

    @Test
    fun testUpdatedAnalysisDataMessageAddsNoInhalersSummaryMessageIfThereAreNoActiveInhalers() {
        // return an empty list of active devices from device query.
        whenever(deviceQuery.getAllActive()).thenReturn(ArrayList<Device>())
        dependencyProvider.register(DeviceQuery::class, deviceQuery)

        // create an update analysis data message.
        val updateAnalysisDataMessage = UpdateAnalysisDataMessage(ArrayList<Any>())

        // send the device updated message to the device data monitor
        val deviceDataMonitor = DeviceDataMonitor(dependencyProvider)
        deviceDataMonitor.onUpdateAnalysisData(updateAnalysisDataMessage)

        // verify that add message is invoked on the SummaryMessageQueue
        // for No Inhalers message
        val summaryInfoArgumentCaptor = argumentCaptor<SummaryInfo>()
        verify(summaryMessageQueue).addMessage(summaryInfoArgumentCaptor.capture())
        assertEquals(SummaryTextId.NO_INHALERS, summaryInfoArgumentCaptor.lastValue.id)
    }

    @Test
    fun testUpdatedAnalysisDataMessageRemovesNoInhalersSummaryMessageIfThereAreActiveInhalers() {
        // return an empty list of active devices from device query.
        whenever(deviceQuery.getAllActive()).thenReturn(ArrayList<Device>())
        dependencyProvider.register(DeviceQuery::class, deviceQuery)

        // create an update analysis data message.
        var updateAnalysisDataMessage = UpdateAnalysisDataMessage(ArrayList<Any>())

        // send the device updated message to the device data monitor
        val deviceDataMonitor = DeviceDataMonitor(dependencyProvider)
        deviceDataMonitor.onUpdateAnalysisData(updateAnalysisDataMessage)

        // verify that add message is invoked on the SummaryMessageQueue
        // for No Inhalers message
        val summaryInfoArgumentCaptor = argumentCaptor<SummaryInfo>()
        verify(summaryMessageQueue).addMessage(summaryInfoArgumentCaptor.capture())
        assertEquals(SummaryTextId.NO_INHALERS, summaryInfoArgumentCaptor.lastValue.id)

        // return a non-empty list of active devices from device query
        val deviceList = ArrayList<Device>()
        deviceList.add(device!!)
        whenever(deviceQuery.getAllActive()).thenReturn(deviceList)
        dependencyProvider.register(DeviceQuery::class, deviceQuery)

        // create an update analysis data message.
        updateAnalysisDataMessage = UpdateAnalysisDataMessage(ArrayList<Any>())

        // send the device updated message to the device data monitor
        deviceDataMonitor.onUpdateAnalysisData(updateAnalysisDataMessage)

        // verify that remove message is invoked on the SummaryMessageQueue
        // for No Inhalers message
        verify(summaryMessageQueue, atLeast(1)).removeMessage(summaryInfoArgumentCaptor.capture())
        assertEquals(SummaryTextId.NO_INHALERS, summaryInfoArgumentCaptor.allValues[0].id)
    }

    @Test
    fun testUpdatedAnalysisDataMessageAddsEmptyInhalersSummaryMessageIfThereAreActiveInhalersWithLowRemainingDose() {
        // return an empty list of active devices from device query.
        val deviceList = ArrayList<Device>()
        deviceList.add(device!!)
        whenever(deviceQuery.getAllActive()).thenReturn(deviceList)
        dependencyProvider.register(DeviceQuery::class, deviceQuery)
        device!!.remainingDoseCount = 4

        // create an update analysis data message.
        val updateAnalysisDataMessage = UpdateAnalysisDataMessage(ArrayList<Any>())

        // send the device updated message to the device data monitor
        val deviceDataMonitor = DeviceDataMonitor(dependencyProvider)
        deviceDataMonitor.onUpdateAnalysisData(updateAnalysisDataMessage)

        // verify that add message is invoked on the SummaryMessageQueue
        // for No Inhalers message
        val summaryInfoArgumentCaptor = argumentCaptor<SummaryInfo>()
        verify(summaryMessageQueue).addMessage(summaryInfoArgumentCaptor.capture())
        assertEquals(SummaryTextId.EMPTY_INHALER, summaryInfoArgumentCaptor.lastValue.id)
        assertEquals(device!!.nickname, summaryInfoArgumentCaptor.lastValue.message!!["Name"])
    }

    @Test
    fun testUpdatedAnalysisDataMessageRemovesEmptyInhalersSummaryMessageIfThereAreActiveInhalersWithoutLowRemainingDose() {
        // return an empty list of active devices from device query.
        val deviceList = ArrayList<Device>()
        deviceList.add(device!!)
        whenever(deviceQuery.getAllActive()).thenReturn(deviceList)
        dependencyProvider.register(DeviceQuery::class, deviceQuery)
        device!!.remainingDoseCount = 4

        // create an update analysis data message.
        var updateAnalysisDataMessage = UpdateAnalysisDataMessage(ArrayList<Any>())

        // send the device updated message to the device data monitor
        val deviceDataMonitor = DeviceDataMonitor(dependencyProvider)
        deviceDataMonitor.onUpdateAnalysisData(updateAnalysisDataMessage)

        // create a new device
        val changedTime = Instant.from(ZonedDateTime.of(2017, 1, 1, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 1, 1)
        val newDevice = Model.Device(true, "", 60, expirationDate, "9.1", "2.3", InhalerNameType.WORK,
                changedTime.minusSeconds(4800), "", 10, "Inhalation Inc.", "work1", 58, "125434521",
                "i2n3h4a5l6e7", true, changedTime, "745750")
        val medication = Model.Medication("745750", "ProAir1", "ProAir1", MedicationClassification.RELIEVER, 300, 300, 2, 100, 12)
        //medication.lowDosePercentage = 10
        newDevice.medication = medication
        newDevice.isConnected = true

        // return the new device as part of the active devices from the device query
        val newDeviceList = ArrayList<Device>()
        newDeviceList.add(newDevice)
        whenever(deviceQuery.getAllActive()).thenReturn(newDeviceList)
        dependencyProvider.register(DeviceQuery::class, deviceQuery)
        newDevice.remainingDoseCount = 58

        // create an update analysis data message.
        updateAnalysisDataMessage = UpdateAnalysisDataMessage(ArrayList<Any>())

        // send the device updated message to the device data monitor
        deviceDataMonitor.onUpdateAnalysisData(updateAnalysisDataMessage)

        // verify that remove message is invoked on the SummaryMessageQueue
        // for No Inhalers and Empty Inhalers message
        val summaryInfoArgumentCaptor = argumentCaptor<SummaryInfo>()
        verify(summaryMessageQueue, atLeast(4)).removeMessage(summaryInfoArgumentCaptor.capture())
        assertEquals(SummaryTextId.EMPTY_INHALER, summaryInfoArgumentCaptor.allValues[3].id)
    }

    companion object {

        private val GMT_ZONE_ID = ZoneId.ofOffset("GMT", ZoneOffset.UTC)
    }
}
