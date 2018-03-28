///
// TrackerViewModelTest.java
// app
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.respiratoryapp.activity.viewmodel.tracker

import android.view.MenuItem
import com.nhaarman.mockito_kotlin.*
import com.teva.analysis.messages.HistoryUpdatedMessage
import com.teva.analysis.model.AnalyzedDataProvider
import com.teva.analysis.model.HistoryDay
import com.teva.cloud.models.userprofile.UserProfileManager
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.common.utilities.Messenger
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.common.DateTimeLocalization
import com.teva.respiratoryapp.mocks.MockedLocalizationService
import com.teva.respiratoryapp.models.ApplicationSettings
import com.teva.respiratoryapp.services.alert.SystemAlertManager
import com.teva.respiratoryapp.testutils.BaseTest
import com.teva.respiratoryapp.testutils.mocks.AsyncTaskHelper
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit
import java.util.*

class TrackerViewModelTest : BaseTest() {

    private val WEEKS_TO_CACHE = 5
    private val DAYS_IN_WEEK = 7
    private val BUFFER_PAGES = 2

    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var messenger: Messenger
    private lateinit var timeService: TimeService
    private lateinit var analyzedDataProvider: AnalyzedDataProvider
    private lateinit var applicationSettings: ApplicationSettings
    private lateinit var localizationService: MockedLocalizationService

    private lateinit var userProfileManager: UserProfileManager

    private var positionOffsetForIdentification: Int = 0

    @Before
    fun Setup() {
        positionOffsetForIdentification = 1

        dependencyProvider = DependencyProvider.default

        userProfileManager = mock()
        dependencyProvider.register(UserProfileManager::class, userProfileManager)

        messenger = mock()
        dependencyProvider.register(Messenger::class, messenger)

        timeService = mock()
        whenever(timeService.today()).thenReturn(TODAY)

        dependencyProvider.register(TimeService::class, timeService)

        createAnalyzedDataProviderMocks()

        localizationService = MockedLocalizationService()
        dependencyProvider.register(LocalizationService::class, localizationService)

        dependencyProvider.register(DateTimeLocalization::class, mock<DateTimeLocalization>())
    }

    private fun createAnalyzedDataProviderMocks() {

        // Create AnalyzedDataProvider mock
        analyzedDataProvider = mock()
        whenever(analyzedDataProvider.trackingStartDate).thenReturn(TRACKING_START_DATE)

        whenever(analyzedDataProvider.getHistory(any(), any())).thenAnswer { invocation ->
            val startDate = invocation.arguments[0] as LocalDate
            var date = invocation.arguments[1] as LocalDate

            val history = ArrayList<HistoryDay>()
            while (!date.isBefore(startDate)) {
                val day = HistoryDay(date)

                // use ConnectedInhalerCount as identifier of mocked day in tests.
                // set it to (1 + position) from today, not the cache start
                val position = positionOffsetForIdentification + date.until(TODAY, ChronoUnit.DAYS).toInt()
                day.connectedInhalerCount = position

                history.add(day)
                date = date.minusDays(1)
            }

            history
        }

        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
    }


    @Test
    @Throws(Exception::class)
    fun testThatOnStartSubscribesToMessenger() {
        val viewModel = TrackerViewModel(dependencyProvider)

        viewModel.onStart()

        verify(messenger).subscribe(viewModel)
    }

    @Test
    @Throws(Exception::class)
    fun testThatOnStopUnsubscribesFromMessenger() {
        val viewModel = TrackerViewModel(dependencyProvider)

        viewModel.onStop()

        verify(messenger).unsubscribeToAll(viewModel)
    }

    @Test
    @Throws(Exception::class)
    fun testThatTrackerItemsAreRecycled() {
        AsyncTaskHelper.beginTaskQueue()

        val viewModel = TrackerViewModel(dependencyProvider)

        AsyncTaskHelper.executeQueuedTasks()

        // Get a tracker item
        val trackerItem = viewModel.getTrackerItem(0)

        // Recycle the trackerItem
        viewModel.recycleTrackerItem(trackerItem)

        // Get another tracker item
        val nextTrackerItem = viewModel.getTrackerItem(1)

        // verify that the recycled tracker item was returned by the next call
        assertEquals(trackerItem, nextTrackerItem)
    }

    @Test
    fun testThatTrackerItemCountReturnsCorrectValue() {
        AsyncTaskHelper.beginTaskQueue()

        val viewModel = TrackerViewModel(dependencyProvider)

        // while waiting for TrackingStartDate to be updated by DataTask,
        // getItemCount() should return 0.
        assertEquals(0, viewModel.itemCount.toLong())

        AsyncTaskHelper.executeQueuedTasks()

        // mocks were configured with today=2/17/2017 and trackingStartDate=12/1/2017
        val expectedDays = 1 + TRACKING_START_DATE.until(TODAY, ChronoUnit.DAYS).toInt()
        assertEquals(expectedDays.toLong(), viewModel.itemCount.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testCachingMechanism() {
        AsyncTaskHelper.beginTaskQueue()

        val viewModel = TrackerViewModel(dependencyProvider)

        AsyncTaskHelper.executeQueuedTasks()

        // get a tracker item for the target date.
        val targetPosition = TARGET_DATE.until(TODAY, ChronoUnit.DAYS).toInt()
        val trackerItem = viewModel.getTrackerItem(targetPosition)

        // verify that getTrackerItem() returns unloaded item when cache empty
        assertFalse(trackerItem.isLoaded)

        // execute background tasks that will fill the cache.
        AsyncTaskHelper.executeQueuedTasks()

        // verify that the tracker item was updated
        assertTrue(trackerItem.isLoaded)
        // The ConnectedInhalers property of the HistoryDay mocks is set to their position
        // in the tracker item list to identify the day when testing the caching.
        assertEquals((positionOffsetForIdentification + targetPosition).toLong(), trackerItem.connectedInhalers.toLong())

        // verify that proper cache pages are loaded by iterating through all of the days in the list
        // and if the tracker item returned is already loaded, then the day was cached.
        val targetWeek = (TARGET_DATE.toEpochDay() / DAYS_IN_WEEK).toInt()

        // 2 weeks before and 2 weeks after target week (total 5 weeks)
        val daysToCache = WEEKS_TO_CACHE * DAYS_IN_WEEK
        val cacheStartDate = LocalDate.ofEpochDay(((targetWeek - BUFFER_PAGES) * DAYS_IN_WEEK).toLong())

        val trackerItemList = ArrayList<TrackerItemViewModel>()
        // use the ConnectedInhalers property of the HistoryDay to identify if the correct HistoryDay
        // was loaded.
        for (index in 0..daysToCache - 1) {
            val date = cacheStartDate.plusDays(index.toLong())
            val position = date.until(TODAY, ChronoUnit.DAYS).toInt()

            val item = viewModel.getTrackerItem(position)
            assertTrue(item.isLoaded)
            assertEquals((positionOffsetForIdentification + position).toLong(), item.connectedInhalers.toLong())

            trackerItemList.add(item)
        }

        // Change identification offset and invalidate the cache
        positionOffsetForIdentification = 1000

        viewModel.onHistoryUpdated(HistoryUpdatedMessage(ArrayList<Any>()))

        AsyncTaskHelper.executeQueuedTasks()

        // use the ConnectedInhalers property of the HistoryDay to verify
        // that the cache was invalidated and reloaded.
        for (index in 0..daysToCache - 1) {
            val date = cacheStartDate.plusDays(index.toLong())
            val position = date.until(TODAY, ChronoUnit.DAYS).toInt()

            val item = trackerItemList[index]
            assertEquals((positionOffsetForIdentification + position).toLong(), item.connectedInhalers.toLong())
        }
    }

    @Test
    fun testUserReportEventIsNotRaisedAndAnAlertIsDisplayedIfNoDataIsAvailable() {
        val USER_REPORT_NO_DATA_MESSAGE = "USER_REPORT_NO_DATA_MESSAGE"
        val USER_REPORT_NO_DATA_TITLE = "USER_REPORT_NO_DATA_TITLE"

        val localizationService: LocalizationService = mock()
        whenever(localizationService.getString(eq(R.string.userReportNoData_text))).thenReturn(USER_REPORT_NO_DATA_MESSAGE)
        whenever(localizationService.getString(eq(R.string.userReportNoDataTitle_text))).thenReturn(USER_REPORT_NO_DATA_TITLE)
        dependencyProvider.register(LocalizationService::class, localizationService)

        val systemAlertManager: SystemAlertManager = mock()
        dependencyProvider.register(SystemAlertManager::class, systemAlertManager)

        analyzedDataProvider = mock()
        whenever(analyzedDataProvider.trackingStartDate).thenReturn(null)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)

        val trackerEvents: TrackerViewModel.TrackerEvents = mock()
        dependencyProvider.register(TrackerViewModel.TrackerEvents::class, trackerEvents)

        val menuItem: MenuItem = mock()
        whenever(menuItem.itemId).thenReturn(R.id.show_report)

        val trackerViewModel = TrackerViewModel(dependencyProvider)
        trackerViewModel.onMenuItem(menuItem)

        verify(trackerEvents, never()).onReport()
        verify(trackerEvents).onReportEmpty()
    }

    @Test
    fun testUserReportEventIsRaisedIfDataIsAvailable() {
        createAnalyzedDataProviderMocks()

        val trackerEvents: TrackerViewModel.TrackerEvents = mock()
        dependencyProvider.register(TrackerViewModel.TrackerEvents::class, trackerEvents)

        val menuItem: MenuItem = mock()
        whenever(menuItem.itemId).thenReturn(R.id.show_report)

        val trackerViewModel = TrackerViewModel(dependencyProvider)
        trackerViewModel.onMenuItem(menuItem)

        verify(trackerEvents).onReport()
    }

    companion object {
        private val TODAY = LocalDate.of(2017, 2, 17)
        private val TARGET_DATE = LocalDate.of(2017, 1, 5)
        private val TRACKING_START_DATE = LocalDate.of(2016, 12, 1)
    }
}