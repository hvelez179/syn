///
// DailyReportViewModelTest.java
// app
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.respiratoryapp.activity.viewmodel.tracker

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.teva.analysis.entities.HistoryDose
import com.teva.analysis.messages.HistoryUpdatedMessage
import com.teva.analysis.model.AnalyzedDataProvider
import com.teva.analysis.model.HistoryDay
import com.teva.cloud.models.userprofile.UserProfileManager
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.common.utilities.Messenger
import com.teva.devices.dataquery.DeviceDataQuery
import com.teva.devices.entities.Device
import com.teva.devices.entities.InhaleEvent
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.common.DateTimeLocalization
import com.teva.respiratoryapp.mocks.MockedLocalizationService
import com.teva.respiratoryapp.mvvmframework.ui.FragmentListViewModel
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel
import com.teva.respiratoryapp.testutils.BaseTest
import com.teva.respiratoryapp.testutils.mocks.AsyncTaskHelper
import com.teva.userfeedback.entities.DailyUserFeeling
import com.teva.userfeedback.enumerations.UserFeeling
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.*
import java.util.*

class DailyReportViewModelTest : BaseTest() {

    private lateinit var dependencyProvider: DependencyProvider

    private lateinit var defaultZoneOffset: ZoneOffset
    private lateinit var userFeelingTime: Instant
    private lateinit var eventTime1: Instant
    private lateinit var eventTime2: Instant
    private lateinit var eventTime3: Instant
    private lateinit var eventTime4: Instant
    private lateinit var eventTime5: Instant
    private lateinit var eventTime6: Instant

    private lateinit var messenger: Messenger
    private lateinit var historyDay: HistoryDay

    private lateinit var userProfileManager: UserProfileManager

    @Before
    fun setup() {
        dependencyProvider = DependencyProvider.default
        dependencyProvider.unregisterAll()
        
        dependencyProvider.register(ZoneId::class, ZoneOffset.ofTotalSeconds(0))

        defaultZoneOffset = ZoneOffset.ofTotalSeconds(0)
        userFeelingTime = ZonedDateTime.of(2017, 2, 17, 10, 35, 0, 0, defaultZoneOffset).toInstant()
        eventTime1 = ZonedDateTime.of(2017, 2, 17, 8, 10, 0, 0, defaultZoneOffset).toInstant()
        eventTime2 = ZonedDateTime.of(2017, 2, 17, 9, 25, 0, 0, defaultZoneOffset).toInstant()
        eventTime3 = ZonedDateTime.of(2017, 2, 17, 13, 40, 0, 0, defaultZoneOffset).toInstant()
        eventTime4 = ZonedDateTime.of(2017, 2, 17, 15, 0, 0, 0, defaultZoneOffset).toInstant()
        eventTime5 = ZonedDateTime.of(2017, 2, 17, 16, 10, 0, 0, defaultZoneOffset).toInstant()
        eventTime6 = ZonedDateTime.of(2017, 2, 17, 17, 32, 0, 0, defaultZoneOffset).toInstant()

        userProfileManager = mock()
        dependencyProvider.register(UserProfileManager::class, userProfileManager)

        val localizationService = MockedLocalizationService()
        localizationService.add(R.string.trackerInhaleEventsToday_zero_text, TRACKER_ZERO_INHALATIONS_TODAY)
        localizationService.add(R.string.trackerInhaleEventsToday_one_text, TRACKER_ONE_INHALATION_TODAY)
        localizationService.add(R.string.trackerInhaleEventsToday_text, TRACKER_MANY_INHALATIONS_TODAY)
        localizationService.add(R.string.trackerConnectedInhalersToday_zero_text, TRACKER_ZERO_CONNECTED_INHALERS_TODAY)
        localizationService.add(R.string.trackerConnectedInhalersToday_one_text, TRACKER_ONE_CONNECTED_INHALERS_TODAY)
        localizationService.add(R.string.trackerConnectedInhalersToday_text, TRACKER_MANY_CONNECTED_INHALERS_TODAY)

        dependencyProvider.register(LocalizationService::class, localizationService)

        messenger = mock()
        dependencyProvider.register(Messenger::class, messenger)

        historyDay = HistoryDay(TODAY)

        historyDay.dailyUserFeeling = DailyUserFeeling(userFeelingTime, UserFeeling.GOOD)

        historyDay.relieverDoses.add(createDose(eventTime1))
        historyDay.relieverDoses.add(createDose(eventTime2))
        historyDay.invalidDoses.add(createDose(eventTime3))
        historyDay.invalidDoses.add(createDose(eventTime4))
        historyDay.systemErrorDoses.add(createDose(eventTime5))
        historyDay.systemErrorDoses.add(createDose(eventTime6))

        val history = ArrayList<HistoryDay>()
        history.add(historyDay)

        val analyzedDataProvider: AnalyzedDataProvider = mock()
        whenever(analyzedDataProvider.getHistory(TODAY, TODAY)).thenReturn(history)

        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)

        val deviceList = ArrayList<Device>()
        val device = Device()
        device.serialNumber = SERIAL_NUMBER
        deviceList.add(device)

        val deviceDataQuery: DeviceDataQuery = mock()
        whenever(deviceDataQuery.getAll()).thenReturn(deviceList)

        dependencyProvider.register(DeviceDataQuery::class, deviceDataQuery)

        val dateTimeLocalization: DateTimeLocalization = mock()
        whenever(dateTimeLocalization.toShortMonthDayYear(TODAY)).thenReturn(FORMATTED_LOCAL_DATE)

        dependencyProvider.register(DateTimeLocalization::class, dateTimeLocalization)
    }

    private fun createDose(eventTime: Instant): HistoryDose {
        val inhaleEvent = InhaleEvent()
        val zoneOffsetMinutes = ZoneOffset.from(ZonedDateTime.ofInstant(eventTime, defaultZoneOffset)).totalSeconds / SECONDS_PER_MINUTE

        inhaleEvent.eventTime = eventTime
        inhaleEvent.timezoneOffsetMinutes = zoneOffsetMinutes

        inhaleEvent.deviceSerialNumber = SERIAL_NUMBER

        val list = ArrayList<InhaleEvent>()
        list.add(inhaleEvent)
        return HistoryDose(DRUG_ID, list)
    }

    @Test
    fun testThatCloseHandlerCallsOnBackPressed() {
        val viewModel = DailyReportViewModel(dependencyProvider, TODAY)

        val navigationEvents: FragmentViewModel.NavigationEvents = mock()
        dependencyProvider.register(FragmentViewModel.NavigationEvents::class, navigationEvents)

        viewModel.onClose()

        verify(navigationEvents).onBackPressed()
    }

    @Test
    @Throws(Exception::class)
    fun testThatOnStartSubscribesToMessenger() {
        AsyncTaskHelper.beginTaskQueue()

        val viewModel = DailyReportViewModel(dependencyProvider, TODAY)

        viewModel.onStart()

        verify(messenger).subscribe(viewModel)
    }

    @Test
    @Throws(Exception::class)
    fun testThatOnStopUnsubscribesFromMessenger() {
        val viewModel = DailyReportViewModel(dependencyProvider, TODAY)

        viewModel.onStop()

        verify(messenger).unsubscribeToAll(viewModel)
    }

    private fun LocalTimeFromInstant(instant: Instant): LocalTime {
        val ldt = LocalDateTime.ofInstant(instant, defaultZoneOffset)

        return ldt.toLocalTime()
    }

    @Test
    fun testThatReportItemsAreLoaded() {
        AsyncTaskHelper.beginTaskQueue()

        val viewModel = DailyReportViewModel(dependencyProvider, TODAY)

        val listener: FragmentListViewModel.ListChangedListener = mock()
        viewModel.listChangedListener = listener

        viewModel.onHistoryUpdated(HistoryUpdatedMessage(ArrayList<Any>()))

        AsyncTaskHelper.executeQueuedTasks()

        val items = viewModel.items

        // Verify the fragment is notified.
        verify(listener).onListChanged()

        // Verify the count
        assertEquals(6, items!!.size.toLong())

        // Verify the times of the items to verify they are in the correct order.
        assertEquals(LocalTimeFromInstant(eventTime6), items[0].time)
        assertEquals(LocalTimeFromInstant(eventTime5), items[1].time)
        assertEquals(LocalTimeFromInstant(eventTime4), items[2].time)
        assertEquals(LocalTimeFromInstant(eventTime3), items[3].time)
        assertEquals(LocalTimeFromInstant(eventTime2), items[4].time)
        assertEquals(LocalTimeFromInstant(eventTime1), items[5].time)
    }

    @Test
    fun testThatFormattedDateReturnsCorrectValue() {
        val viewModel = DailyReportViewModel(dependencyProvider, TODAY)

        assertEquals(FORMATTED_LOCAL_DATE, viewModel.formattedDate)
    }

    @Test
    fun testThatEventsTodayReturnsZeroFormOfMessage() {
        AsyncTaskHelper.beginTaskQueue()

        val viewModel = DailyReportViewModel(dependencyProvider, TODAY)

        historyDay.invalidDoses.clear()
        historyDay.relieverDoses.clear()
        historyDay.systemErrorDoses.clear()

        viewModel.onHistoryUpdated(HistoryUpdatedMessage(ArrayList<Any>()))

        AsyncTaskHelper.executeQueuedTasks()

        assertEquals(TRACKER_ZERO_INHALATIONS_TODAY, viewModel.eventsToday)
    }

    @Test
    fun testThatEventsTodayReturnsOneFormOfMessage() {
        AsyncTaskHelper.beginTaskQueue()

        val viewModel = DailyReportViewModel(dependencyProvider, TODAY)

        historyDay.invalidDoses.clear()
        historyDay.relieverDoses.removeAt(0)
        historyDay.systemErrorDoses.clear()

        viewModel.onHistoryUpdated(HistoryUpdatedMessage(ArrayList<Any>()))

        AsyncTaskHelper.executeQueuedTasks()

        assertEquals(TRACKER_ONE_INHALATION_TODAY, viewModel.eventsToday)
    }

    @Test
    fun testThatEventsTodayReturnsManyFormOfMessage() {
        AsyncTaskHelper.beginTaskQueue()

        val viewModel = DailyReportViewModel(dependencyProvider, TODAY)

        viewModel.onHistoryUpdated(HistoryUpdatedMessage(ArrayList<Any>()))

        AsyncTaskHelper.executeQueuedTasks()

        assertEquals(EXPECTED_TRACKER_MANY_INHALATIONS_TODAY, viewModel.eventsToday)
    }

    @Test
    fun testThatConnectedInhalersReturnsZeroFormOfMessage() {
        AsyncTaskHelper.beginTaskQueue()

        val viewModel = DailyReportViewModel(dependencyProvider, TODAY)

        historyDay.connectedInhalerCount = 0

        viewModel.onHistoryUpdated(HistoryUpdatedMessage(ArrayList<Any>()))

        AsyncTaskHelper.executeQueuedTasks()

        assertEquals(TRACKER_ZERO_CONNECTED_INHALERS_TODAY, viewModel.connectedInhalers)
    }

    @Test
    fun testThatConnectedInhalersReturnsOneFormOfMessage() {
        AsyncTaskHelper.beginTaskQueue()

        val viewModel = DailyReportViewModel(dependencyProvider, TODAY)

        historyDay.connectedInhalerCount = 1

        viewModel.onHistoryUpdated(HistoryUpdatedMessage(ArrayList<Any>()))

        AsyncTaskHelper.executeQueuedTasks()

        assertEquals(TRACKER_ONE_CONNECTED_INHALERS_TODAY, viewModel.connectedInhalers)
    }

    @Test
    fun testThatConnectedInhalersReturnsManyFormOfMessage() {
        AsyncTaskHelper.beginTaskQueue()

        val viewModel = DailyReportViewModel(dependencyProvider, TODAY)

        historyDay.connectedInhalerCount = 2

        viewModel.onHistoryUpdated(HistoryUpdatedMessage(ArrayList<Any>()))

        AsyncTaskHelper.executeQueuedTasks()

        assertEquals(EXPECTED_TRACKER_MANY_CONNECTED_INHALERS_TODAY, viewModel.connectedInhalers)
    }

    companion object {
        private val SECONDS_PER_MINUTE = 60

        private val DRUG_ID = "745750"

        private val TODAY = LocalDate.of(2017, 2, 17)
        private val FORMATTED_LOCAL_DATE = "Feb 17, 2017"

        private val TRACKER_ZERO_INHALATIONS_TODAY = "0 Inhalation events today"
        private val TRACKER_ONE_INHALATION_TODAY = "1 Inhalation event today"
        private val TRACKER_MANY_INHALATIONS_TODAY = "\$embeddedValue$ Inhalation events today"

        private val TRACKER_ZERO_CONNECTED_INHALERS_TODAY = "0 inhalers connected"
        private val TRACKER_ONE_CONNECTED_INHALERS_TODAY = "1 inhaler connected"
        private val TRACKER_MANY_CONNECTED_INHALERS_TODAY = "\$embeddedValue$ inhalers connected"

        private val EXPECTED_TRACKER_MANY_CONNECTED_INHALERS_TODAY = "2 inhalers connected"
        private val EXPECTED_TRACKER_MANY_INHALATIONS_TODAY = "4 Inhalation events today"
        private val SERIAL_NUMBER = "12345678901"
    }

}