//
// InhalationDataMonitorTests.kt
// teva_analysis
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.analysis.model.datamonitors

import com.nhaarman.mockito_kotlin.*
import com.teva.analysis.entities.HistoryDose
import com.teva.analysis.entities.SummaryInfo
import com.teva.analysis.enumerations.SummaryTextId
import com.teva.analysis.messages.HistoryUpdatedMessage
import com.teva.analysis.model.AnalyzedDataProvider
import com.teva.analysis.model.HistoryDay
import com.teva.analysis.utils.Model
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.devices.DeviceManagerStringReplacementKey
import com.teva.devices.dataquery.DeviceDataQuery
import com.teva.devices.dataquery.InhaleEventDataQuery
import com.teva.devices.entities.Device
import com.teva.devices.entities.InhaleEvent
import com.teva.devices.enumerations.InhaleStatus
import com.teva.devices.enumerations.InhalerNameType
import com.teva.devices.model.DeviceManagerInhaleEventKey
import com.teva.devices.model.DeviceManagerNotificationId
import com.teva.medication.dataquery.MedicationDataQuery
import com.teva.medication.entities.Medication
import com.teva.medication.entities.Prescription
import com.teva.medication.enumerations.MedicationClassification
import com.teva.notifications.entities.ReminderSetting
import com.teva.notifications.enumerations.RepeatType
import com.teva.notifications.models.NotificationManager
import com.teva.userfeedback.entities.DailyUserFeeling
import com.teva.userfeedback.enumerations.UserFeeling
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.*
import java.util.*

/**
 * This class defines unit tests for the InhalationDataMonitor class.
 */
class InhalationDataMonitorTests {
    private val dependencyProvider: DependencyProvider = DependencyProvider.default
    private val timeService: TimeService = mock()
    private val today: LocalDate = LocalDate.of(2017, 1, 1)
    private var now: Instant? = null
    private val analyzedDataProvider: AnalyzedDataProvider = mock()
    private var device: Device? = null
    private val deviceDataQuery: DeviceDataQuery = mock()
    private val inhaleEventDataQuery: InhaleEventDataQuery = mock()
    private val notificationManager: NotificationManager = mock()
    private val medicationDataQuery: MedicationDataQuery = mock()
    private var medication: Medication? = null
    private val summaryMessageQueue: SummaryMessageQueue = mock()
    private var reminderSetting: ReminderSetting? = null
    private val messenger: Messenger = mock()

    private enum class SubOptimalInhalationType {
        NO_INHALATION,
        EXHALATION,
        ERROR
    }

    @Before
    fun setup() {

        DependencyProvider.default.unregisterAll()

        dependencyProvider.register(Messenger::class, messenger)

        now = Instant.from(ZonedDateTime.of(today, LocalTime.of(23, 0, 0), GMT_ZONE_ID))
        whenever(timeService.now()).thenReturn(now)
        whenever(timeService.today()).thenReturn(today)
        dependencyProvider.register(TimeService::class, timeService)

        val changedTime = Instant.from(ZonedDateTime.of(2017, 1, 1, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 1, 1)
        device = Model.Device(true, "", 60, expirationDate, "9.1", "2.3", InhalerNameType.WORK,
                changedTime.minusSeconds(4800), "", 10, "Inhalation Inc.", "work1", 58, "123454321",
                "i2n3h4a5l6e7", true, changedTime, "745750")
        whenever(deviceDataQuery.get(eq(device!!.serialNumber))).thenReturn(device)
        dependencyProvider.register(DeviceDataQuery::class, deviceDataQuery)

        reminderSetting = ReminderSetting(true, DeviceManagerNotificationId.INHALATIONS_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION, RepeatType.NONE, null)
        whenever(notificationManager.getReminderSettingByName(eq(DeviceManagerNotificationId.INHALATIONS_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION))).thenReturn(reminderSetting)
        dependencyProvider.register(NotificationManager::class, notificationManager)

        val medications = ArrayList<Medication>()
        medication = Model.Medication("745750", "ProAir1", "ProAir1", MedicationClassification.RELIEVER, 300, 300, 2, 100, 12)
        medications.add(medication!!)
        whenever(medicationDataQuery.getAll()).thenReturn(medications)
        dependencyProvider.register(MedicationDataQuery::class, medicationDataQuery)

        dependencyProvider.register(SummaryMessageQueue::class, summaryMessageQueue)
    }

    @Test
    fun testHistoryUpdateMessageWithoutInhaleEventTriggersOveruseIfRelieverEventsExceedThreshold() {
        // mock the history to have inhale events above overuse threshold.
        val inhaleEvents = ArrayList<InhaleEvent>()
        val historyDays = createOneDayHistory(today, true, false, inhaleEvents)
        whenever(analyzedDataProvider.getHistory(eq(today), eq(today))).thenReturn(historyDays)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        whenever(inhaleEventDataQuery.getLast(any(), eq(null))).thenReturn(inhaleEvents)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)

        // send a history updated message without any inhale events.
        val inhalationDataMonitor = InhalationDataMonitor(dependencyProvider)
        val historyUpdatedMessage = HistoryUpdatedMessage(ArrayList<Any>())
        inhalationDataMonitor.onHistoryUpdated(historyUpdatedMessage)

        // verify that an overuse summary message gets added in the summary message queue.
        val summaryInfoArgumentCaptor = argumentCaptor<SummaryInfo>()
        verify(summaryMessageQueue).addMessage(summaryInfoArgumentCaptor.capture())
        assertEquals(SummaryTextId.OVERUSE, summaryInfoArgumentCaptor.lastValue.id)
        assertEquals(medication!!.brandName, summaryInfoArgumentCaptor.lastValue.message!!["MedicationName"])
    }

    @Test
    fun testHistoryUpdateMessageWithInhaleEventAndOverUseHistoryTriggersOveruse() {
        // mock the history to have inhale events above overuse threshold.
        val inhaleEvents = ArrayList<InhaleEvent>()
        val historyDays = createOneDayHistory(today, true, false, inhaleEvents)
        whenever(analyzedDataProvider.getHistory(eq(today), eq(today))).thenReturn(historyDays)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        whenever(inhaleEventDataQuery.getLast(any(),eq(null))).thenReturn(inhaleEvents)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)

        // send a history updated message without any inhale events.
        val inhalationDataMonitor = InhalationDataMonitor(dependencyProvider)
        val changedObjects = ArrayList<Any>()
        changedObjects.add(inhaleEvents[0])
        val historyUpdatedMessage = HistoryUpdatedMessage(changedObjects)
        inhalationDataMonitor.onHistoryUpdated(historyUpdatedMessage)

        // verify that an overuse summary message gets added in the summary message queue.
        val summaryInfoArgumentCaptor = argumentCaptor<SummaryInfo>()
        verify(summaryMessageQueue).addMessage(summaryInfoArgumentCaptor.capture())
        assertEquals(SummaryTextId.OVERUSE, summaryInfoArgumentCaptor.lastValue.id)
        assertEquals(medication!!.brandName, summaryInfoArgumentCaptor.lastValue.message!!["MedicationName"])
    }

    @Test
    fun testHistoryUpdateMessageWithoutInhaleEventTriggersOveruseIfRelieverEventsAndInvalidEventsExceedThreshold() {
        // mock the history to have a combination of inhale events and
        // invalid events above overuse threshold.
        val inhaleEvents = ArrayList<InhaleEvent>()
        val historyDays = createOneDayHistory(today, true, true, inhaleEvents)
        whenever(analyzedDataProvider.getHistory(eq(today), eq(today))).thenReturn(historyDays)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        whenever(inhaleEventDataQuery.getLast(any(), eq(null))).thenReturn(inhaleEvents)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)

        // send a history updated message without any inhale events.
        val inhalationDataMonitor = InhalationDataMonitor(dependencyProvider)
        val historyUpdatedMessage = HistoryUpdatedMessage(ArrayList<Any>())
        inhalationDataMonitor.onHistoryUpdated(historyUpdatedMessage)

        // verify that an overuse summary message gets added in the summary message queue.
        val summaryInfoArgumentCaptor = argumentCaptor<SummaryInfo>()
        verify(summaryMessageQueue).addMessage(summaryInfoArgumentCaptor.capture())
        assertEquals(SummaryTextId.OVERUSE, summaryInfoArgumentCaptor.lastValue.id)
        assertEquals(medication!!.brandName, summaryInfoArgumentCaptor.lastValue.message!!["MedicationName"])
    }

    @Test
    fun testHistoryUpdateMessageWithoutInhaleEventDoesNotTriggerOveruseIfRelieverEventsDoNotExceedThreshold() {
        // mock the history to have inhale events below overuse threshold.
        val inhaleEvents = ArrayList<InhaleEvent>()
        val historyDays = createOneDayHistory(today, false, false, inhaleEvents)
        whenever(analyzedDataProvider.getHistory(eq(today), eq(today))).thenReturn(historyDays)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        whenever(inhaleEventDataQuery.getLast(any(),eq(null))).thenReturn(inhaleEvents)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)

        // send a history updated message without any inhale events.
        val inhalationDataMonitor = InhalationDataMonitor(dependencyProvider)
        val historyUpdatedMessage = HistoryUpdatedMessage(ArrayList<Any>())
        inhalationDataMonitor.onHistoryUpdated(historyUpdatedMessage)

        // verify that overuse summary message is not added in the summary message queue.
        verify(summaryMessageQueue, never()).addMessage(any())
    }

    @Test
    fun testHistoryUpdateMessageWithoutInhaleEventDoesNotTriggerOveruseIfRelieverEventsAndInvalidEventsDoNotExceedThreshold() {
        // mock the history to have a combination of inhale events and invalid events below overuse threshold.
        val inhaleEvents = ArrayList<InhaleEvent>()
        val historyDays = createOneDayHistory(today, false, true, inhaleEvents)
        whenever(analyzedDataProvider.getHistory(eq(today), eq(today))).thenReturn(historyDays)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        whenever(inhaleEventDataQuery.getLast(any(), any())).thenReturn(inhaleEvents)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)

        // send a history updated message without any inhale events.
        val inhalationDataMonitor = InhalationDataMonitor(dependencyProvider)
        val historyUpdatedMessage = HistoryUpdatedMessage(ArrayList<Any>())
        inhalationDataMonitor.onHistoryUpdated(historyUpdatedMessage)

        // verify that overuse summary message is not added in the summary message queue.
        verify(summaryMessageQueue, never()).addMessage(any())
    }

    @Test
    fun testHistoryUpdateMessageWithGoodInhaleEventTriggersGoodInhaleNotificationIfEnabled() {
        // create an inhale event with good inhalation.
        val inhaleEvents = ArrayList<InhaleEvent>()

        val currentDate = today
        val currentTime = LocalTime.of(22, 55, 0)
        val eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val event = Model.InhaleEvent(1, eventTime, 0, 22, 1100, 470, 0, 32, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        event.drugUID = "745750"

        inhaleEvents.add(event)

        val historyDays = ArrayList<HistoryDay>()
        whenever(analyzedDataProvider.getHistory(eq(today), eq(today))).thenReturn(historyDays)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        whenever(inhaleEventDataQuery.getLast(any(), eq(null))).thenReturn(inhaleEvents)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)

        // send a history updated message with the inhale event.
        val changedObjects = ArrayList<Any>()
        changedObjects.add(event)
        val inhalationDataMonitor = InhalationDataMonitor(dependencyProvider)
        val historyUpdatedMessage = HistoryUpdatedMessage(changedObjects)
        inhalationDataMonitor.onHistoryUpdated(historyUpdatedMessage)

        // verify that good inhalation notification is set.
        val stringArgumentCaptor = argumentCaptor<String>()
        val notificationDataArgumentCaptor = argumentCaptor<Map<String, Any>>()
        verify(notificationManager).setNotification(stringArgumentCaptor.capture(), notificationDataArgumentCaptor.capture())
        assertEquals(DeviceManagerNotificationId.INHALATIONS_FEEDBACK_GOOD_INHALATION, stringArgumentCaptor.lastValue)
        assertEquals(event.uniqueId, notificationDataArgumentCaptor.lastValue[DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID])
    }

    @Test
    fun testHistoryUpdateMessageWithLowInhaleEventTriggersLowInhaleNotificationIfEnabled() {
        // create an inhale event with low inhalation.
        val inhaleEvents = ArrayList<InhaleEvent>()

        val currentDate = today
        val currentTime = LocalTime.of(22, 55, 0)
        val eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val event = Model.InhaleEvent(1, eventTime, 0, 22, 1100, 320, 0, 32, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        event.drugUID = "745750"

        inhaleEvents.add(event)

        val historyDays = ArrayList<HistoryDay>()
        whenever(analyzedDataProvider.getHistory(eq(today), eq(today))).thenReturn(historyDays)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        whenever(inhaleEventDataQuery.getLast(any(),eq(null))).thenReturn(inhaleEvents)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)

        // send a history updated message with the inhale event.
        val changedObjects = ArrayList<Any>()
        changedObjects.add(event)
        val inhalationDataMonitor = InhalationDataMonitor(dependencyProvider)
        val historyUpdatedMessage = HistoryUpdatedMessage(changedObjects)
        inhalationDataMonitor.onHistoryUpdated(historyUpdatedMessage)

        // verify that low inhalation notification is set.
        val stringArgumentCaptor = argumentCaptor<String>()
        val notificationDataArgumentCaptor = argumentCaptor<Map<String, Any>>()
        verify(notificationManager).setNotification(stringArgumentCaptor.capture(), notificationDataArgumentCaptor.capture())
        assertEquals(DeviceManagerNotificationId.INHALATIONS_FEEDBACK_LOW_INHALATION, stringArgumentCaptor.lastValue)
        assertEquals(event.uniqueId, notificationDataArgumentCaptor.lastValue[DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID])
    }

    @Test
    fun testHistoryUpdateMessageWithGoodInhaleEventDoesNotTriggerGoodInhaleNotificationIfDisabled() {
        // create an inhale event with good inhalation.
        val inhaleEvents = ArrayList<InhaleEvent>()

        val currentDate = today
        val currentTime = LocalTime.of(22, 55, 0)
        val eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val event = Model.InhaleEvent(1, eventTime, 0, 22, 1100, 470, 0, 32, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        event.drugUID = "745750"

        inhaleEvents.add(event)

        // mark the good inhalation reminder as disabled.
        reminderSetting!!.isEnabled = false
        whenever(notificationManager.getReminderSettingByName(eq(DeviceManagerNotificationId.INHALATIONS_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION))).thenReturn(reminderSetting)
        dependencyProvider.register(NotificationManager::class, notificationManager)

        val historyDays = ArrayList<HistoryDay>()
        whenever(analyzedDataProvider.getHistory(eq(today), eq(today))).thenReturn(historyDays)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        whenever(inhaleEventDataQuery.getLast(any(),eq(null))).thenReturn(inhaleEvents)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)

        // send a history updated message with the inhale event.
        val changedObjects = ArrayList<Any>()
        changedObjects.add(event)
        val inhalationDataMonitor = InhalationDataMonitor(dependencyProvider)
        val historyUpdatedMessage = HistoryUpdatedMessage(changedObjects)
        inhalationDataMonitor.onHistoryUpdated(historyUpdatedMessage)

        // verify that good inhalation notification is not set.
        verify(notificationManager, never()).setNotification(any(), any())
    }

    @Test
    fun testHistoryUpdateMessageWithLowInhaleEventDoesNotTriggerLowInhaleNotificationIfDisabled() {
        // create an inhale event with low inhalation.
        val inhaleEvents = ArrayList<InhaleEvent>()

        val currentDate = today
        val currentTime = LocalTime.of(22, 55, 0)
        val eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val event = Model.InhaleEvent(1, eventTime, 0, 22, 1100, 320, 0, 32, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        event.drugUID = "745750"

        inhaleEvents.add(event)

        // mark the good inhalation reminder as disabled.
        reminderSetting!!.isEnabled = false
        whenever(notificationManager.getReminderSettingByName(eq(DeviceManagerNotificationId.INHALATIONS_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION))).thenReturn(reminderSetting)
        dependencyProvider.register(NotificationManager::class, notificationManager)

        val historyDays = ArrayList<HistoryDay>()
        whenever(analyzedDataProvider.getHistory(eq(today), eq(today))).thenReturn(historyDays)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        whenever(inhaleEventDataQuery.getLast(any(),eq(null))).thenReturn(inhaleEvents)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)

        // send a history updated message with the inhale event.
        val changedObjects = ArrayList<Any>()
        changedObjects.add(event)
        val inhalationDataMonitor = InhalationDataMonitor(dependencyProvider)
        val historyUpdatedMessage = HistoryUpdatedMessage(changedObjects)
        inhalationDataMonitor.onHistoryUpdated(historyUpdatedMessage)

        // verify that low inhalation notification is not set.
        verify(notificationManager, never()).setNotification(any(), any())
    }

    @Test
    fun testHistoryUpdateMessageWithGoodInhaleEventFollowingAcceptableInhalationsTriggersGoodInhaleNotificationWithTurnOffOptionIfEnabled() {
        // create consecutive acceptable inhale events with good latest inhalation.
        val currentDate = today
        val currentTime = LocalTime.of(22, 55, 0)
        val eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))


        val inhaleEvents = createConsecutiveAcceptableInhalations(eventTime, false)

        val historyDays = ArrayList<HistoryDay>()
        whenever(analyzedDataProvider.getHistory(eq(today), eq(today))).thenReturn(historyDays)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        whenever(inhaleEventDataQuery.getLast(any(),eq(null))).thenReturn(inhaleEvents)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)

        // send a history updated message with the inhale event.
        val changedObjects = ArrayList<Any>()
        changedObjects.add(inhaleEvents[0])
        val inhalationDataMonitor = InhalationDataMonitor(dependencyProvider)
        val historyUpdatedMessage = HistoryUpdatedMessage(changedObjects)
        inhalationDataMonitor.onHistoryUpdated(historyUpdatedMessage)

        // verify that good inhalation notification is set.
        val stringArgumentCaptor = argumentCaptor<String>()
        val notificationDataArgumentCaptor = argumentCaptor<Map<String, Any>>()
        verify(notificationManager).setNotification(stringArgumentCaptor.capture(), notificationDataArgumentCaptor.capture())
        assertEquals(DeviceManagerNotificationId.INHALATIONS_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION, stringArgumentCaptor.lastValue)
        assertEquals(inhaleEvents[0].uniqueId, notificationDataArgumentCaptor.lastValue[DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID])
    }

    @Test
    fun testHistoryUpdateMessageWithLowInhaleEventFollowingAcceptableInhalationsTriggersGoodInhaleNotificationWithTipIfEnabled() {
        // create consecutive acceptable inhale events with low latest inhalation.
        val currentDate = today
        val currentTime = LocalTime.of(22, 55, 0)
        val eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))


        val inhaleEvents = createConsecutiveAcceptableInhalations(eventTime, true)

        val historyDays = ArrayList<HistoryDay>()
        whenever(analyzedDataProvider.getHistory(eq(today), eq(today))).thenReturn(historyDays)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        whenever(inhaleEventDataQuery.getLast(any(), eq(null))).thenReturn(inhaleEvents)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)

        // send a history updated message with the inhale event.
        val changedObjects = ArrayList<Any>()
        changedObjects.add(inhaleEvents[0])
        val inhalationDataMonitor = InhalationDataMonitor(dependencyProvider)
        val historyUpdatedMessage = HistoryUpdatedMessage(changedObjects)
        inhalationDataMonitor.onHistoryUpdated(historyUpdatedMessage)

        // verify that low inhalation notification is set.
        val stringArgumentCaptor = argumentCaptor<String>()
        val notificationDataArgumentCaptor = argumentCaptor<Map<String, Any>>()
        verify(notificationManager).setNotification(stringArgumentCaptor.capture(), notificationDataArgumentCaptor.capture())
        assertEquals(DeviceManagerNotificationId.INHALATIONS_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION_WITH_TIP, stringArgumentCaptor.lastValue)
        assertEquals(inhaleEvents[0].uniqueId, notificationDataArgumentCaptor.lastValue[DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID])
    }

    @Test
    fun testHistoryUpdateMessageWithNoInhalationTriggersNoInhaleNotification() {
        // create an inhale event with no inhalation.
        val inhaleEvents = ArrayList<InhaleEvent>()

        val currentDate = today
        val currentTime = LocalTime.of(22, 55, 0)
        val eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val event = Model.InhaleEvent(1, eventTime, 0, 22, 1100, 200, 0, 32, 6,
                1, "Cartridge4", 0, 0, false, "123454321")
        event.drugUID = "745750"

        inhaleEvents.add(event)

        val historyDays = ArrayList<HistoryDay>()
        whenever(analyzedDataProvider.getHistory(eq(today), eq(today))).thenReturn(historyDays)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        whenever(inhaleEventDataQuery.getLast(any(),eq(null))).thenReturn(inhaleEvents)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)

        // send a history updated message with the inhale event.
        val changedObjects = ArrayList<Any>()
        changedObjects.add(event)
        val inhalationDataMonitor = InhalationDataMonitor(dependencyProvider)
        val historyUpdatedMessage = HistoryUpdatedMessage(changedObjects)
        inhalationDataMonitor.onHistoryUpdated(historyUpdatedMessage)

        // verify that no inhalation notification is set.
        val stringArgumentCaptor = argumentCaptor<String>()
        val notificationDataArgumentCaptor = argumentCaptor<Map<String, Any>>()
        verify(notificationManager).setNotification(stringArgumentCaptor.capture(), notificationDataArgumentCaptor.capture())
        assertEquals(DeviceManagerNotificationId.INHALATIONS_FEEDBACK_NO_INHALATION, stringArgumentCaptor.lastValue)
        assertEquals(event.uniqueId, notificationDataArgumentCaptor.lastValue[DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID])
    }

    @Test
    fun testHistoryUpdateMessageWithNoInhalationFollowingMaxSuboptimalInhalationsTriggersSuboptimalInhalationsNotification() {
        // create multiple suboptimal inhalations with No inhalation as the latest event.
        val currentDate = today
        val currentTime = LocalTime.of(22, 55, 0)
        val eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val inhaleEvents = createConsecutiveSuboptimalInhalations(eventTime, SubOptimalInhalationType.NO_INHALATION)

        val historyDays = ArrayList<HistoryDay>()
        whenever(analyzedDataProvider.getHistory(eq(today), eq(today))).thenReturn(historyDays)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        whenever(inhaleEventDataQuery.getLast(any(), eq(null))).thenReturn(inhaleEvents)
        whenever(inhaleEventDataQuery.getLast(any(), eq(InhaleStatus.SystemErrorStatuses))).thenReturn(inhaleEvents)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)

        // send a history updated message with the latest inhalation event.
        val changedObjects = ArrayList<Any>()
        changedObjects.add(inhaleEvents[0])
        val inhalationDataMonitor = InhalationDataMonitor(dependencyProvider)
        val historyUpdatedMessage = HistoryUpdatedMessage(changedObjects)
        inhalationDataMonitor.onHistoryUpdated(historyUpdatedMessage)

        // verify that no inhalation notification is set and suboptimal inhalations notification is set.
        val stringArgumentCaptor = argumentCaptor<String>()
        val notificationDataArgumentCaptor = argumentCaptor<Map<String, Any>>()
        verify(notificationManager, times(2)).setNotification(stringArgumentCaptor.capture(), notificationDataArgumentCaptor.capture())
        assertEquals(DeviceManagerNotificationId.INHALATIONS_FEEDBACK_NO_INHALATION, stringArgumentCaptor.allValues[1])
        assertEquals(inhaleEvents[0].uniqueId, notificationDataArgumentCaptor.allValues[1][DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID])

        assertEquals(DeviceManagerNotificationId.INHALATIONS_FEEDBACK_SUBOPTIMAL_INHALATIONS, stringArgumentCaptor.allValues[0])
        assertEquals(inhaleEvents[0].uniqueId, notificationDataArgumentCaptor.allValues[0][DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID])
        assertEquals(6, notificationDataArgumentCaptor.allValues[0][DeviceManagerStringReplacementKey.MAX_UNSUCCESSFUL_INHALE_EVENTS])
        assertEquals(12, notificationDataArgumentCaptor.allValues[0][DeviceManagerStringReplacementKey.MAX_EVENTS_TO_CHECK_FOR_UNSUCCESSFUL_INHALATION_EVENTS])
    }

    @Test
    fun testHistoryUpdateMessageWithNoInhalationFollowingLessThanMaxSuboptimalInhalationsDoesNotTriggerSuboptimalInhalationsNotification() {
        // create multiple suboptimal inhalations with No inhalation as the latest event.
        val currentDate = today
        val currentTime = LocalTime.of(22, 55, 0)
        val eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        // use five suboptimal inhalations and one good inhalation
        val inhaleEvents = createConsecutiveSuboptimalInhalations(eventTime, SubOptimalInhalationType.NO_INHALATION).subList(0, 5)

        val goodInhaleEvent = Model.InhaleEvent(1, eventTime.minusSeconds(36000), 0, 22, 1100, 330, 0, 32, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        goodInhaleEvent.drugUID = "745750"
        inhaleEvents.add(goodInhaleEvent)

        val historyDays = ArrayList<HistoryDay>()
        whenever(analyzedDataProvider.getHistory(eq(today), eq(today))).thenReturn(historyDays)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        whenever(inhaleEventDataQuery.getLast(any(),eq(null))).thenReturn(inhaleEvents)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)

        // send a history updated message with the latest inhalation event.
        val changedObjects = ArrayList<Any>()
        changedObjects.add(inhaleEvents[0])
        val inhalationDataMonitor = InhalationDataMonitor(dependencyProvider)
        val historyUpdatedMessage = HistoryUpdatedMessage(changedObjects)
        inhalationDataMonitor.onHistoryUpdated(historyUpdatedMessage)

        // verify that no inhalation notification is set and suboptimal inhalations notification is not set.
        val stringArgumentCaptor = argumentCaptor<String>()
        val notificationDataArgumentCaptor = argumentCaptor<Map<String, Any>>()
        verify(notificationManager, times(1)).setNotification(stringArgumentCaptor.capture(), notificationDataArgumentCaptor.capture())
        assertEquals(DeviceManagerNotificationId.INHALATIONS_FEEDBACK_NO_INHALATION, stringArgumentCaptor.lastValue)
        assertEquals(inhaleEvents[0].uniqueId, notificationDataArgumentCaptor.lastValue[DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID])
    }

    @Test
    fun testHistoryUpdateMessageWithExhalationTriggersExhalationNotification() {
        // create an inhale event with exhalation.
        val inhaleEvents = ArrayList<InhaleEvent>()

        val currentDate = today
        val currentTime = LocalTime.of(22, 55, 0)
        val eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val event = Model.InhaleEvent(1, eventTime, 0, 22, 1100, 500, 0, 8, 6,
                1, "Cartridge4", 0, 0, false, "123454321")
        event.drugUID = "745750"

        inhaleEvents.add(event)

        val historyDays = ArrayList<HistoryDay>()
        whenever(analyzedDataProvider.getHistory(eq(today), eq(today))).thenReturn(historyDays)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        whenever(inhaleEventDataQuery.getLast(any(),eq(null))).thenReturn(inhaleEvents)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)

        // send a history updated message with the exhalation event.
        val changedObjects = ArrayList<Any>()
        changedObjects.add(event)
        val inhalationDataMonitor = InhalationDataMonitor(dependencyProvider)
        val historyUpdatedMessage = HistoryUpdatedMessage(changedObjects)
        inhalationDataMonitor.onHistoryUpdated(historyUpdatedMessage)

        // verify that exhalation notification is set.
        val stringArgumentCaptor = argumentCaptor<String>()
        val notificationDataArgumentCaptor = argumentCaptor<Map<String, Any>>()
        verify(notificationManager).setNotification(stringArgumentCaptor.capture(), notificationDataArgumentCaptor.capture())
        assertEquals(DeviceManagerNotificationId.INHALATIONS_FEEDBACK_EXHALATION, stringArgumentCaptor.lastValue)
        assertEquals(event.uniqueId, notificationDataArgumentCaptor.lastValue[DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID])
    }

    @Test
    fun testHistoryUpdateMessageWithExhalationFollowingSuboptimalInhalationsTriggersSuboptimalInhalationsNotification() {
        // create multiple suboptimal inhalations with Exhalation as the latest inhale event.
        val currentDate = today
        val currentTime = LocalTime.of(22, 55, 0)
        val eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val inhaleEvents = createConsecutiveSuboptimalInhalations(eventTime, SubOptimalInhalationType.EXHALATION)

        val historyDays = ArrayList<HistoryDay>()
        whenever(analyzedDataProvider.getHistory(eq(today), eq(today))).thenReturn(historyDays)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        whenever(inhaleEventDataQuery.getLast(any(), eq(null))).thenReturn(inhaleEvents)
        whenever(inhaleEventDataQuery.getLast(any(), eq(InhaleStatus.SystemErrorStatuses))).thenReturn(inhaleEvents)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)

        // send a history updated message with the latest event.
        val changedObjects = ArrayList<Any>()
        changedObjects.add(inhaleEvents[0])
        val inhalationDataMonitor = InhalationDataMonitor(dependencyProvider)
        val historyUpdatedMessage = HistoryUpdatedMessage(changedObjects)
        inhalationDataMonitor.onHistoryUpdated(historyUpdatedMessage)

        // verify that exhalation notification is set and the suboptimal inhalations notification is sent.
        val stringArgumentCaptor = argumentCaptor<String>()
        val notificationDataArgumentCaptor = argumentCaptor<Map<String, Any>>()
        verify(notificationManager, times(2)).setNotification(stringArgumentCaptor.capture(), notificationDataArgumentCaptor.capture())
        assertEquals(DeviceManagerNotificationId.INHALATIONS_FEEDBACK_EXHALATION, stringArgumentCaptor.allValues[1])
        assertEquals(inhaleEvents[0].uniqueId, notificationDataArgumentCaptor.allValues[1][DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID])

        assertEquals(DeviceManagerNotificationId.INHALATIONS_FEEDBACK_SUBOPTIMAL_INHALATIONS, stringArgumentCaptor.allValues[0])
        assertEquals(inhaleEvents[0].uniqueId, notificationDataArgumentCaptor.allValues[0][DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID])
        assertEquals(6, notificationDataArgumentCaptor.allValues[0][DeviceManagerStringReplacementKey.MAX_UNSUCCESSFUL_INHALE_EVENTS])
        assertEquals(12, notificationDataArgumentCaptor.allValues[0][DeviceManagerStringReplacementKey.MAX_EVENTS_TO_CHECK_FOR_UNSUCCESSFUL_INHALATION_EVENTS])
    }

    @Test
    fun testHistoryUpdateMessageWithErrorTriggersHighInhalationNotification() {
        // create an inhale event with high inhalation.
        val inhaleEvents = ArrayList<InhaleEvent>()

        val currentDate = today
        val currentTime = LocalTime.of(22, 55, 0)
        val eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val event = Model.InhaleEvent(1, eventTime, 0, 22, 1100, 2500, 0, 0, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        event.drugUID = "745750"

        inhaleEvents.add(event)

        val historyDays = ArrayList<HistoryDay>()
        whenever(analyzedDataProvider.getHistory(eq(today), eq(today))).thenReturn(historyDays)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        whenever(inhaleEventDataQuery.getLast(any(), eq(null))).thenReturn(inhaleEvents)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)

        // send a history updated message with the high inhalation event.
        val changedObjects = ArrayList<Any>()
        changedObjects.add(event)
        val inhalationDataMonitor = InhalationDataMonitor(dependencyProvider)
        val historyUpdatedMessage = HistoryUpdatedMessage(changedObjects)
        inhalationDataMonitor.onHistoryUpdated(historyUpdatedMessage)

        // verify that high inhalation notification is set.
        val stringArgumentCaptor = argumentCaptor<String>()
        val notificationDataArgumentCaptor = argumentCaptor<Map<String, Any>>()
        verify(notificationManager).setNotification(stringArgumentCaptor.capture(), notificationDataArgumentCaptor.capture())
        assertEquals(DeviceManagerNotificationId.INHALATIONS_FEEDBACK_HIGH_INHALATION, stringArgumentCaptor.lastValue)
        assertEquals(event.uniqueId, notificationDataArgumentCaptor.lastValue[DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID])
    }

    @Test
    fun testHistoryUpdateMessageWithErrorFollowingSuboptimalInhalationsTriggersSuboptimalInhalationsNotification() {
        // create multiple suboptimal inhalations with high inhalation as the latest event.
        val currentDate = today
        val currentTime = LocalTime.of(22, 55, 0)
        val eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val inhaleEvents = createConsecutiveSuboptimalInhalations(eventTime, SubOptimalInhalationType.ERROR)

        val historyDays = ArrayList<HistoryDay>()
        whenever(analyzedDataProvider.getHistory(eq(today), eq(today))).thenReturn(historyDays)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        whenever(inhaleEventDataQuery.getLast(any(), eq(null))).thenReturn(inhaleEvents)
        whenever(inhaleEventDataQuery.getLast(any(), eq(InhaleStatus.SystemErrorStatuses))).thenReturn(inhaleEvents)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)

        // send a history updated message with the latest inhale event.
        val changedObjects = ArrayList<Any>()
        changedObjects.add(inhaleEvents[0])
        val inhalationDataMonitor = InhalationDataMonitor(dependencyProvider)
        val historyUpdatedMessage = HistoryUpdatedMessage(changedObjects)
        inhalationDataMonitor.onHistoryUpdated(historyUpdatedMessage)

        // verify that high inhalation notification is set and suboptimal inhalations notification is set.
        val stringArgumentCaptor = argumentCaptor<String>()
        val notificationDataArgumentCaptor = argumentCaptor<Map<String, Any>>()
        verify(notificationManager, times(2)).setNotification(stringArgumentCaptor.capture(), notificationDataArgumentCaptor.capture())
        assertEquals(DeviceManagerNotificationId.INHALATIONS_FEEDBACK_HIGH_INHALATION, stringArgumentCaptor.allValues[1])
        assertEquals(inhaleEvents[0].uniqueId, notificationDataArgumentCaptor.allValues[1][DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID])

        assertEquals(DeviceManagerNotificationId.INHALATIONS_FEEDBACK_SUBOPTIMAL_INHALATIONS, stringArgumentCaptor.allValues[0])
        assertEquals(inhaleEvents[0].uniqueId, notificationDataArgumentCaptor.allValues[0][DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID])
        assertEquals(6, notificationDataArgumentCaptor.allValues[0][DeviceManagerStringReplacementKey.MAX_UNSUCCESSFUL_INHALE_EVENTS])
        assertEquals(12, notificationDataArgumentCaptor.allValues[0][DeviceManagerStringReplacementKey.MAX_EVENTS_TO_CHECK_FOR_UNSUCCESSFUL_INHALATION_EVENTS])
    }

    @Test
    fun testHistoryUpdateMessageWithBadDataErrorTriggersSystemErrorNotificationWithCorrectErrorCode() {
        // create an inhale event with bad data.
        val inhaleEvents = ArrayList<InhaleEvent>()

        val currentDate = today
        val currentTime = LocalTime.of(22, 55, 0)
        val eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val event = Model.InhaleEvent(1, eventTime, 0, 24, 1111, 500, 200, 2, 8,
                1, "Cartridge3", 0, 0, false, "123454321")
        event.drugUID = "745750"

        inhaleEvents.add(event)

        val historyDays = ArrayList<HistoryDay>()
        whenever(analyzedDataProvider.getHistory(eq(today), eq(today))).thenReturn(historyDays)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        whenever(inhaleEventDataQuery.getLast(any(), eq(null))).thenReturn(inhaleEvents)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)

        // send a history updated message with the event.
        val changedObjects = ArrayList<Any>()
        changedObjects.add(event)
        val inhalationDataMonitor = InhalationDataMonitor(dependencyProvider)
        val historyUpdatedMessage = HistoryUpdatedMessage(changedObjects)
        inhalationDataMonitor.onHistoryUpdated(historyUpdatedMessage)

        // verify that system error notification is set.
        val stringArgumentCaptor = argumentCaptor<String>()
        val notificationDataArgumentCaptor = argumentCaptor<Map<String, Any>>()
        verify(notificationManager).setNotification(stringArgumentCaptor.capture(), notificationDataArgumentCaptor.capture())
        assertEquals(DeviceManagerNotificationId.SYSTEM_ERROR_DETECTED, stringArgumentCaptor.lastValue)
        assertEquals(event.uniqueId, notificationDataArgumentCaptor.lastValue[DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID])
        // verify that the notification includes the correct error code.
        assertEquals("1", notificationDataArgumentCaptor.lastValue[DeviceManagerStringReplacementKey.SYSTEM_ERROR_CODE])
    }

    @Test
    fun testHistoryUpdateMessageWithTimeStampErrorTriggersSystemErrorNotificationWithCorrectErrorCode() {
        // create an inhale event with time stamp error.
        val inhaleEvents = ArrayList<InhaleEvent>()

        val currentDate = today
        val currentTime = LocalTime.of(22, 55, 0)
        val eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val event = Model.InhaleEvent(1, eventTime, 0, 24, 1111, 500, 200, 16, 8,
                1, "Cartridge3", 0, 0, false, "123454321")
        event.drugUID = "745750"

        inhaleEvents.add(event)

        val historyDays = ArrayList<HistoryDay>()
        whenever(analyzedDataProvider.getHistory(eq(today), eq(today))).thenReturn(historyDays)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        whenever(inhaleEventDataQuery.getLast(any(),eq(null))).thenReturn(inhaleEvents)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)

        // send a history updated message with the event.
        val changedObjects = ArrayList<Any>()
        changedObjects.add(event)
        val inhalationDataMonitor = InhalationDataMonitor(dependencyProvider)
        val historyUpdatedMessage = HistoryUpdatedMessage(changedObjects)
        inhalationDataMonitor.onHistoryUpdated(historyUpdatedMessage)

        // verify that the system error notification is set.
        val stringArgumentCaptor = argumentCaptor<String>()
        val notificationDataArgumentCaptor = argumentCaptor<Map<String, Any>>()
        verify(notificationManager).setNotification(stringArgumentCaptor.capture(), notificationDataArgumentCaptor.capture())
        assertEquals(DeviceManagerNotificationId.SYSTEM_ERROR_DETECTED, stringArgumentCaptor.lastValue)
        assertEquals(event.uniqueId, notificationDataArgumentCaptor.lastValue[DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID])
        // verify that the notification includes the correct error code.
        assertEquals("2", notificationDataArgumentCaptor.lastValue[DeviceManagerStringReplacementKey.SYSTEM_ERROR_CODE])
    }

    @Test
    fun testHistoryUpdateMessageWithParameterErrorTriggersSystemErrorNotificationWithCorrectErrorCode() {
        // create an inhale event with parameter error.
        val inhaleEvents = ArrayList<InhaleEvent>()

        val currentDate = today
        val currentTime = LocalTime.of(22, 55, 0)
        val eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val event = Model.InhaleEvent(1, eventTime, 0, 24, 1111, 500, 200, 64, 8,
                1, "Cartridge3", 0, 0, false, "123454321")
        event.drugUID = "745750"

        inhaleEvents.add(event)

        val historyDays = ArrayList<HistoryDay>()
        whenever(analyzedDataProvider.getHistory(eq(today), eq(today))).thenReturn(historyDays)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        whenever(inhaleEventDataQuery.getLast(any(),eq(null))).thenReturn(inhaleEvents)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)

        // send a history updated message with the event.
        val changedObjects = ArrayList<Any>()
        changedObjects.add(event)
        val inhalationDataMonitor = InhalationDataMonitor(dependencyProvider)
        val historyUpdatedMessage = HistoryUpdatedMessage(changedObjects)
        inhalationDataMonitor.onHistoryUpdated(historyUpdatedMessage)

        // verify that system error notification is set.
        val stringArgumentCaptor = argumentCaptor<String>()
        val notificationDataArgumentCaptor = argumentCaptor<Map<String, Any>>()
        verify(notificationManager).setNotification(stringArgumentCaptor.capture(), notificationDataArgumentCaptor.capture())
        assertEquals(DeviceManagerNotificationId.SYSTEM_ERROR_DETECTED, stringArgumentCaptor.lastValue)
        assertEquals(event.uniqueId, notificationDataArgumentCaptor.lastValue[DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID])
        // verify that the notification includes the correct error code.
        assertEquals("3", notificationDataArgumentCaptor.lastValue[DeviceManagerStringReplacementKey.SYSTEM_ERROR_CODE])
    }

    @Test
    fun testHistoryUpdateMessageWithMultipleSystemErrorsTriggersSystemErrorNotificationWithCorrectErrorCode() {
        // create an inhale event with bad parameter, time stamp and parameter errors.
        val inhaleEvents = ArrayList<InhaleEvent>()

        val currentDate = today
        val currentTime = LocalTime.of(22, 55, 0)
        val eventTime = Instant.from(ZonedDateTime.of(currentDate, currentTime, GMT_ZONE_ID))
        val event = Model.InhaleEvent(1, eventTime, 0, 24, 1111, 500, 200, 82, 8,
                1, "Cartridge3", 0, 0, false, "123454321")
        event.drugUID = "745750"

        inhaleEvents.add(event)

        val historyDays = ArrayList<HistoryDay>()
        whenever(analyzedDataProvider.getHistory(eq(today), eq(today))).thenReturn(historyDays)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        whenever(inhaleEventDataQuery.getLast(any(),eq(null))).thenReturn(inhaleEvents)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)

        // send a history updated message with the event.
        val changedObjects = ArrayList<Any>()
        changedObjects.add(event)
        val inhalationDataMonitor = InhalationDataMonitor(dependencyProvider)
        val historyUpdatedMessage = HistoryUpdatedMessage(changedObjects)
        inhalationDataMonitor.onHistoryUpdated(historyUpdatedMessage)

        // verify that system error notification is set.
        val stringArgumentCaptor = argumentCaptor<String>()
        val notificationDataArgumentCaptor = argumentCaptor<Map<String, Any>>()
        verify(notificationManager).setNotification(stringArgumentCaptor.capture(), notificationDataArgumentCaptor.capture())
        assertEquals(DeviceManagerNotificationId.SYSTEM_ERROR_DETECTED, stringArgumentCaptor.lastValue)
        assertEquals(event.uniqueId, notificationDataArgumentCaptor.lastValue[DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID])
        val errorCodeString = notificationDataArgumentCaptor.lastValue[DeviceManagerStringReplacementKey.SYSTEM_ERROR_CODE] as String
        //verify that the error code string contains the correct error codes.
        assertTrue(errorCodeString.contains("1"))
        assertTrue(errorCodeString.contains("2"))
        assertTrue(errorCodeString.contains("3"))
    }

    /**
     * This method creates one day history with inhale events and returns the history
     * and inhale events. Based on parameters passed, the created inhale events are
     * created to simulate inhaler overuse and/or invalid events.
     *
     * @param date                 - the date for which history needs to be created.
     * @param simulateOveruse      - flag to indicate if created event number should exceed
     *                               the overuse threshold.
     * @param simulateInvalidDoses - flag to indicate if invalid doses should be included.
     * @param inhaleEvents-        the list to which the created inhale events are added.
     * @return - a history day with all the created events.
     */
    private fun createOneDayHistory(date: LocalDate, simulateOveruse: Boolean, simulateInvalidDoses: Boolean, inhaleEvents: MutableList<InhaleEvent>): List<HistoryDay> {
        var currentTime = LocalTime.of(1, 0, 0)
        var eventTime = Instant.from(ZonedDateTime.of(date, currentTime, GMT_ZONE_ID))
        val event1 = Model.InhaleEvent(1, eventTime, 0, 20, 1065, 500, 400, 0, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        event1.drugUID = "745750"

        currentTime = LocalTime.of(2, 0, 0)
        eventTime = Instant.from(ZonedDateTime.of(date, currentTime, GMT_ZONE_ID))
        val event2 = Model.InhaleEvent(1, eventTime, 0, 19, 965, 450, 410, 0, 4,
                2, "Cartridge4", 0, 0, true, "123454321")
        event2.drugUID = "745750"

        currentTime = LocalTime.of(4, 0, 0)
        eventTime = Instant.from(ZonedDateTime.of(date, currentTime, GMT_ZONE_ID))
        val event3 = if (simulateInvalidDoses)
            Model.InhaleEvent(1, eventTime, 0, 22, 1100, 90, 0, 32, 6,
                    1, "Cartridge4", 0, 0, false, "123454321")
        else
            Model.InhaleEvent(1, eventTime, 0, 21, 1165, 451, 390, 0, 6,
                    1, "Cartridge4", 0, 0, true, "123454321")
        event3.drugUID = "745750"

        currentTime = LocalTime.of(5, 0, 0)
        eventTime = Instant.from(ZonedDateTime.of(date, currentTime, GMT_ZONE_ID))
        val event4 = Model.InhaleEvent(1, eventTime, 0, 23, 1105, 610, 440, 0, 4,
                2, "Cartridge4", 0, 0, true, "123454321")
        event4.drugUID = "745750"

        currentTime = LocalTime.of(6, 30, 0)
        eventTime = Instant.from(ZonedDateTime.of(date, currentTime, GMT_ZONE_ID))
        val event5 = Model.InhaleEvent(1, eventTime, 0, 25, 1265, 550, 420, 0, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        event5.drugUID = "745750"

        currentTime = LocalTime.of(9, 45, 0)
        eventTime = Instant.from(ZonedDateTime.of(date, currentTime, GMT_ZONE_ID))
        val event6 = Model.InhaleEvent(1, eventTime, 0, 22, 1100, 330, 0, 32, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        event6.drugUID = "745750"

        currentTime = LocalTime.of(11, 0, 0)
        eventTime = Instant.from(ZonedDateTime.of(date, currentTime, GMT_ZONE_ID))
        val event7 = if (simulateInvalidDoses)
            Model.InhaleEvent(1, eventTime, 0, 26, 965, 582, 570, 4, 6,
                    1, "Cartridge3", 0, 0, false, "234565432")
        else
            Model.InhaleEvent(1, eventTime, 0, 20, 1065, 500, 400, 0, 6,
                    1, "Cartridge4", 0, 0, true, "123454321")
        event7.drugUID = "745750"

        currentTime = LocalTime.of(13, 0, 0)
        eventTime = Instant.from(ZonedDateTime.of(date, currentTime, GMT_ZONE_ID))
        val event8 = Model.InhaleEvent(1, eventTime, 0, 19, 965, 450, 410, 0, 4,
                2, "Cartridge4", 0, 0, true, "123454321")
        event8.drugUID = "745750"

        currentTime = LocalTime.of(15, 30, 0)
        eventTime = Instant.from(ZonedDateTime.of(date, currentTime, GMT_ZONE_ID))
        val event9 = if (simulateInvalidDoses)
            Model.InhaleEvent(1, eventTime, 0, 15, 935, 502, 439, 8, 6,
                    1, "Cartridge3", 0, 0, false, "234565432")
        else
            Model.InhaleEvent(1, eventTime, 0, 21, 1165, 451, 390, 0, 6,
                    1, "Cartridge4", 0, 0, true, "123454321")
        event9.drugUID = "745750"

        currentTime = LocalTime.of(17, 0, 0)
        eventTime = Instant.from(ZonedDateTime.of(date, currentTime, GMT_ZONE_ID))
        val event10 = Model.InhaleEvent(1, eventTime, 0, 23, 1105, 610, 440, 0, 4,
                2, "Cartridge4", 0, 0, true, "123454321")
        event10.drugUID = "745750"

        currentTime = LocalTime.of(18, 30, 0)
        eventTime = Instant.from(ZonedDateTime.of(date, currentTime, GMT_ZONE_ID))
        val event11 = Model.InhaleEvent(1, eventTime, 0, 25, 1265, 550, 420, 0, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        event11.drugUID = "745750"

        currentTime = LocalTime.of(20, 45, 0)
        eventTime = Instant.from(ZonedDateTime.of(date, currentTime, GMT_ZONE_ID))
        val event12 = Model.InhaleEvent(1, eventTime, 0, 22, 1100, 330, 0, 32, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        event12.drugUID = "745750"

        currentTime = LocalTime.of(23, 0, 0)
        eventTime = Instant.from(ZonedDateTime.of(date, currentTime, GMT_ZONE_ID))
        val event13 = Model.InhaleEvent(1, eventTime, 0, 19, 965, 450, 410, 0, 4,
                2, "Cartridge4", 0, 0, true, "123454321")
        event13.drugUID = "745750"

        val day = HistoryDay(date)
        val dose1 = HistoryDose("745750", listOf(event1))
        dose1.isReliever = true
        val dose2 = HistoryDose("745750", listOf(event2))
        dose2.isReliever = true
        val dose3 = HistoryDose("745750", listOf(event3))
        dose3.isReliever = true
        val dose4 = HistoryDose("745750", listOf(event4))
        dose4.isReliever = true
        val dose5 = HistoryDose("745750", listOf(event5))
        dose5.isReliever = true
        val dose6 = HistoryDose("745750", listOf(event6))
        dose6.isReliever = true
        val dose7 = HistoryDose("745750", listOf(event7))
        dose7.isReliever = true
        val dose8 = HistoryDose("745750", listOf(event8))
        dose8.isReliever = true
        val dose9 = HistoryDose("745750", listOf(event9))
        dose9.isReliever = true
        val dose10 = HistoryDose("745750", listOf(event10))
        dose10.isReliever = true
        val dose11 = HistoryDose("745750", listOf(event11))
        dose11.isReliever = true
        val dose12 = HistoryDose("745750", listOf(event12))
        dose12.isReliever = true
        val dose13 = HistoryDose("745750", listOf(event13))
        dose13.isReliever = true
        day.relieverDoses.add(dose1)
        day.relieverDoses.add(dose2)
        day.relieverDoses.add(dose4)
        day.relieverDoses.add(dose5)
        day.relieverDoses.add(dose6)
        day.relieverDoses.add(dose8)

        if (!simulateInvalidDoses) {
            day.relieverDoses.add(dose3)
            day.relieverDoses.add(dose7)
            day.relieverDoses.add(dose9)
        } else {
            day.invalidDoses.add(dose3)
            day.invalidDoses.add(dose7)
            day.invalidDoses.add(dose9)
        }

        day.relieverDoses.add(dose10)

        if (simulateOveruse) {
            day.relieverDoses.add(dose11)
            day.relieverDoses.add(dose12)
            day.relieverDoses.add(dose13)
        }
        val zonedDateTime = ZonedDateTime.of(date, LocalTime.of(0, 1, 0), GMT_ZONE_ID)
        day.dailyUserFeeling = DailyUserFeeling(Instant.from(zonedDateTime), UserFeeling.BAD)
        day.connectedInhalerCount = 1

        // Prescription data.
        val prescriptions = ArrayList<Prescription>()

        val prescription = Model.Prescription(1, 3, Instant.from(zonedDateTime), medication!!.drugUID)
        prescription.medication = medication
        prescriptions.add(prescription)
        day.prescriptions = prescriptions

        if (simulateOveruse) {
            inhaleEvents.add(event13)
            inhaleEvents.add(event12)
            inhaleEvents.add(event11)
        }

        inhaleEvents.add(event10)
        inhaleEvents.add(event9)
        inhaleEvents.add(event8)
        inhaleEvents.add(event7)
        inhaleEvents.add(event6)
        inhaleEvents.add(event5)
        inhaleEvents.add(event4)
        inhaleEvents.add(event3)
        inhaleEvents.add(event2)
        inhaleEvents.add(event1)

        val historyDays = ArrayList<HistoryDay>()
        historyDays.add(day)
        return historyDays
    }

    /**
     * This method creates consecutive acceptable inhalations.
     *
     * @param latestEventTime     - the latest event time.
     * @param lowLatestInhalation - this flag indicates if the latest inhalation should
     *                              be a low inhalation. if set to false, a good latest
     *                              inhalation is created.
     * @return - a list of created acceptable inhale events.
     */
    private fun createConsecutiveAcceptableInhalations(latestEventTime: Instant, lowLatestInhalation: Boolean): List<InhaleEvent> {
        var eventTime = latestEventTime
        val event1 = Model.InhaleEvent(1, eventTime, 0, 20, 1065, if (lowLatestInhalation) 350 else 480, 500, 0, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        event1.drugUID = "745750"

        eventTime = eventTime.minusSeconds(3600)
        val event2 = Model.InhaleEvent(1, eventTime, 0, 19, 965, 450, 410, 0, 4,
                2, "Cartridge4", 0, 0, true, "123454321")
        event2.drugUID = "745750"

        eventTime = eventTime.minusSeconds(3600)
        val event3 = Model.InhaleEvent(1, eventTime, 0, 21, 1165, 451, 390, 0, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        event3.drugUID = "745750"

        eventTime = eventTime.minusSeconds(3600)
        val event4 = Model.InhaleEvent(1, eventTime, 0, 23, 1105, 610, 440, 0, 4,
                2, "Cartridge4", 0, 0, true, "123454321")
        event4.drugUID = "745750"

        eventTime = eventTime.minusSeconds(3600)
        val event5 = Model.InhaleEvent(1, eventTime, 0, 25, 1265, 550, 420, 0, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        event5.drugUID = "745750"

        eventTime = eventTime.minusSeconds(3600)
        val event6 = Model.InhaleEvent(1, eventTime, 0, 22, 1100, 330, 0, 32, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        event6.drugUID = "745750"

        eventTime = eventTime.minusSeconds(3600)
        val event7 = Model.InhaleEvent(1, eventTime, 0, 20, 1065, 500, 400, 0, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        event7.drugUID = "745750"

        eventTime = eventTime.minusSeconds(3600)
        val event8 = Model.InhaleEvent(1, eventTime, 0, 19, 965, 450, 410, 0, 4,
                2, "Cartridge4", 0, 0, true, "123454321")
        event8.drugUID = "745750"

        eventTime = eventTime.minusSeconds(3600)
        val event9 = Model.InhaleEvent(1, eventTime, 0, 21, 1165, 451, 390, 0, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        event9.drugUID = "745750"

        eventTime = eventTime.minusSeconds(3600)
        val event10 = Model.InhaleEvent(1, eventTime, 0, 23, 1105, 610, 440, 0, 4,
                2, "Cartridge4", 0, 0, true, "123454321")
        event10.drugUID = "745750"

        eventTime = eventTime.minusSeconds(3600)
        val event11 = Model.InhaleEvent(1, eventTime, 0, 25, 1265, 550, 420, 0, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        event11.drugUID = "745750"

        eventTime = eventTime.minusSeconds(3600)
        val event12 = Model.InhaleEvent(1, eventTime, 0, 22, 1100, 330, 0, 32, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        event12.drugUID = "745750"


        val inhaleEvents = ArrayList<InhaleEvent>()
        inhaleEvents.add(event1)
        inhaleEvents.add(event2)
        inhaleEvents.add(event3)
        inhaleEvents.add(event4)
        inhaleEvents.add(event5)
        inhaleEvents.add(event6)
        inhaleEvents.add(event7)
        inhaleEvents.add(event8)
        inhaleEvents.add(event9)
        inhaleEvents.add(event10)
        inhaleEvents.add(event11)
        inhaleEvents.add(event12)

        return inhaleEvents
    }

    /**
     * This method creates consecutive suboptimal inhalations.
     *
     * @param latestEventTime      - the latest event time.
     * @param latestInhalationType - the latest suboptimal inhalation type.
     * @return - a list containing the suboptimal inhale events created.
     */
    private fun createConsecutiveSuboptimalInhalations(latestEventTime: Instant, latestInhalationType: SubOptimalInhalationType): MutableList<InhaleEvent> {
        var eventTime = latestEventTime
        var event1 = Model.InhaleEvent(1, eventTime, 0, 20, 1065, 30, 500, 0, 6,
                1, "Cartridge4", 0, 0, false, "123454321")

        if (latestInhalationType == SubOptimalInhalationType.EXHALATION) {
            event1 = Model.InhaleEvent(1, eventTime, 0, 20, 1065, 30, 439, 8, 6,
                    1, "Cartridge4", 0, 0, false, "123454321")
        } else if (latestInhalationType == SubOptimalInhalationType.ERROR) {
            event1 = Model.InhaleEvent(1, eventTime, 0, 20, 1065, 2570, 30, 0, 6,
                    1, "Cartridge4", 0, 0, true, "123454321")
        }
        event1.drugUID = "745750"

        eventTime = eventTime.minusSeconds(3600)
        val event2 = Model.InhaleEvent(1, eventTime, 0, 19, 965, 90, 410, 0, 4,
                2, "Cartridge4", 0, 0, false, "123454321")
        event2.drugUID = "745750"

        eventTime = eventTime.minusSeconds(3600)
        val event3 = Model.InhaleEvent(1, eventTime, 0, 15, 935, 502, 439, 8, 6,
                1, "Cartridge4", 0, 0, false, "123454321")
        event3.drugUID = "745750"

        eventTime = eventTime.minusSeconds(3600)
        val event4 = Model.InhaleEvent(1, eventTime, 0, 23, 1105, 110, 440, 0, 4,
                2, "Cartridge4", 0, 0, false, "123454321")
        event4.drugUID = "745750"

        eventTime = eventTime.minusSeconds(3600)
        val event5 = Model.InhaleEvent(1, eventTime, 0, 25, 1265, 40, 420, 0, 6,
                1, "Cartridge4", 0, 0, false, "123454321")
        event5.drugUID = "745750"

        eventTime = eventTime.minusSeconds(3600)
        val event6 = Model.InhaleEvent(1, eventTime, 0, 26, 965, 2270, 582, 0, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        event6.drugUID = "745750"

        eventTime = eventTime.minusSeconds(3600)
        val event7 = Model.InhaleEvent(1, eventTime, 0, 20, 1065, 30, 400, 0, 6,
                1, "Cartridge4", 0, 0, false, "123454321")
        event7.drugUID = "745750"

        eventTime = eventTime.minusSeconds(3600)
        val event8 = Model.InhaleEvent(1, eventTime, 0, 19, 965, 45, 410, 0, 4,
                2, "Cartridge4", 0, 0, false, "123454321")
        event8.drugUID = "745750"

        eventTime = eventTime.minusSeconds(3600)
        val event9 = Model.InhaleEvent(1, eventTime, 0, 21, 1165, 2390, 451, 0, 6,
                1, "Cartridge4", 0, 0, true, "123454321")
        event9.drugUID = "745750"

        eventTime = eventTime.minusSeconds(3600)
        val event10 = Model.InhaleEvent(1, eventTime, 0, 15, 935, 502, 439, 8, 6,
                1, "Cartridge4", 0, 0, false, "123454321")
        event10.drugUID = "745750"

        eventTime = eventTime.minusSeconds(3600)
        val event11 = Model.InhaleEvent(1, eventTime, 0, 25, 1265, 55, 420, 0, 6,
                1, "Cartridge4", 0, 0, false, "123454321")
        event11.drugUID = "745750"

        eventTime = eventTime.minusSeconds(3600)
        val event12 = Model.InhaleEvent(1, eventTime, 0, 22, 110, 330, 0, 32, 6,
                1, "Cartridge4", 0, 0, false, "123454321")
        event12.drugUID = "745750"


        val inhaleEvents = ArrayList<InhaleEvent>()
        inhaleEvents.add(event1)
        inhaleEvents.add(event2)
        inhaleEvents.add(event3)
        inhaleEvents.add(event4)
        inhaleEvents.add(event5)
        inhaleEvents.add(event6)
        inhaleEvents.add(event7)
        inhaleEvents.add(event8)
        inhaleEvents.add(event9)
        inhaleEvents.add(event10)
        inhaleEvents.add(event11)
        inhaleEvents.add(event12)

        return inhaleEvents
    }

    companion object {
        private val GMT_ZONE_ID = ZoneId.ofOffset("GMT", ZoneOffset.UTC)
    }
}
