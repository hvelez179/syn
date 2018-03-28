//
// AnalyzedDataProviderTests.kt
// teva_analysis
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.analysis.model

import com.nhaarman.mockito_kotlin.*
import com.teva.analysis.messages.HistoryUpdatedMessage
import com.teva.analysis.messages.UpdateAnalysisDataMessage
import com.teva.analysis.model.datamonitors.SummaryMessageQueue
import com.teva.analysis.utils.HistoryDataMatcher.matchesHistoryDayList
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.devices.dataquery.InhaleEventDataQuery
import com.teva.devices.entities.InhaleEvent
import com.teva.medication.dataquery.MedicationDataQuery
import com.teva.notifications.models.NotificationManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.threeten.bp.*
import org.threeten.bp.temporal.ChronoUnit.DAYS
import java.util.*

/**
 * This class defines unit tests for the AnalyzedDataProviderImpl class.
 */

class AnalyzedDataProviderImplTests {
    val dependencyProvider: DependencyProvider = DependencyProvider.default
    val messenger: Messenger = mock()
    val timeService: TimeService = mock()
    val historyCollator: HistoryCollator = mock()

    var notificationManager: NotificationManager = mock()

    @Before
    fun setup() {
        DependencyProvider.default.unregisterAll()


        dependencyProvider.register(Messenger::class, messenger)
        dependencyProvider.register(TimeService::class, timeService)

        dependencyProvider.register(SummaryMessageQueue::class, mock<SummaryMessageQueue>())

        whenever(timeService.today()).thenReturn(LocalDate.of(2020, 1, 31))
        whenever(timeService.now()).thenReturn(Instant.from(ZonedDateTime.of(2020, 1, 31, 10, 10, 10, 0, GMT_ZONE_ID)))
        dependencyProvider.register(HistoryCollator::class, historyCollator)
        dependencyProvider.register(NotificationManager::class, notificationManager)

        dependencyProvider.register(InhaleEventDataQuery::class, mock<InhaleEventDataQuery>())
        dependencyProvider.register(MedicationDataQuery::class, mock<MedicationDataQuery>())
    }

    @Test
    fun testGetHistoryObtainsProperlyCollatedDataFromHistoryCollatorAndReturnsIt() {
        // create expectations
        val expectedHistoryDays = createMockHistoryDays(timeService.today().minusDays(20), timeService.today())
        whenever(historyCollator.getHistory(any(), any())).thenReturn(expectedHistoryDays)

        // perform operation
        val analyzedDataProvider = AnalyzedDataProviderImpl(dependencyProvider, DAYS_TO_CACHE)
        val historyDays = analyzedDataProvider.getHistory(timeService.today().minusDays(20), timeService.today())

        // test expectations
        // verify that the returned history matches the expected values.
        assertThat(historyDays, matchesHistoryDayList(expectedHistoryDays))
    }

    @Test
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun testGetHistoryForCachedDateRangeReturnsDataFromCache() {
        // create expectations
        val cacheInterval = DAYS_TO_CACHE
        val expectedHistoryDays = createMockHistoryDays(timeService.today().minusDays((cacheInterval - 1).toLong()), timeService.today())
        whenever(historyCollator.getHistory(any(), any())).thenReturn(expectedHistoryDays)

        // perform operation
        val analyzedDataProvider = AnalyzedDataProviderImpl(dependencyProvider, DAYS_TO_CACHE)

        // test expectations
        // verify that a call to the database is made during initialization to load cached data.
        verify(historyCollator, times(1)).getHistory(any(), any())

        // verify that the cache has the history for the specified number of days.
        val clazz = analyzedDataProvider.javaClass
        val cachedHistoryField = clazz.getDeclaredField("cachedHistory")
        cachedHistoryField.isAccessible = true
        val cache = cachedHistoryField.get(analyzedDataProvider) as List<HistoryDay>
        assertEquals(cacheInterval.toLong(), getNumberOfHistoryDaysInCache(cache).toLong())

        // retrieve history for the cached date range.
        val historyDays = analyzedDataProvider.getHistory(timeService.today().minusDays((cacheInterval - 1).toLong()), timeService.today())

        // test expectations
        // verify that no additional calls are made to the history collator to retrieve history.
        verify(historyCollator, times(1)).getHistory(any(), any())

        // verify that the retrieved values match.
        assertThat(historyDays, matchesHistoryDayList(expectedHistoryDays))
    }

    @Test
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun testGetHistoryOutsideCachedDateRangeRetrievesHistoryFromHistoryCollator() {
        // create expectations
        val cacheInterval = DAYS_TO_CACHE
        val mockHistoryDays = createMockHistoryDays(timeService.today().minusDays((cacheInterval - 1).toLong()), timeService.today())
        whenever(historyCollator.getHistory(any(), any())).thenReturn(mockHistoryDays)

        // perform operation
        val analyzedDataProvider = AnalyzedDataProviderImpl(dependencyProvider, DAYS_TO_CACHE)

        // test expectations
        // verify that a call to the database is made during initialization to load cached data.
        verify(historyCollator, times(1)).getHistory(any(), any())

        // verify that the cache has the history for the specified number of days.
        val clazz = analyzedDataProvider.javaClass
        val cachedHistoryField = clazz.getDeclaredField("cachedHistory")
        cachedHistoryField.isAccessible = true
        val cache = cachedHistoryField.get(analyzedDataProvider) as List<HistoryDay>
        assertEquals(cacheInterval.toLong(), getNumberOfHistoryDaysInCache(cache).toLong())

        // retrieve history for a date range such that part of the range coincides with the cached history.
        val expectedHistoryDays = createMockHistoryDays(timeService.today().minusDays(20), timeService.today())
        whenever(historyCollator.getHistory(any(), any())).thenReturn(expectedHistoryDays)

        val historyDays = analyzedDataProvider.getHistory(timeService.today().minusDays(20), timeService.today())

        // test expectations
        // verify that a call is made to the history collator to retrieve history.
        verify(historyCollator, times(2)).getHistory(any(), any())

        // verify that the retrieved values match.
        assertThat(historyDays, matchesHistoryDayList(expectedHistoryDays))
    }

    @Test
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun testGetHistoryForCachedDateRangeReturnsDataFromDatabaseIfCacheIsInvalid() {
        // create expectations
        val cacheInterval = DAYS_TO_CACHE
        val mockHistoryDays = createMockHistoryDays(timeService.today().minusDays((cacheInterval - 1).toLong()), timeService.today())
        whenever(historyCollator.getHistory(any(), any())).thenReturn(mockHistoryDays)

        // perform operation
        val analyzedDataProvider = AnalyzedDataProviderImpl(dependencyProvider, DAYS_TO_CACHE)

        // test expectations
        // verify that a call to the history collator is made during initialization to fill the cache.
        verify(historyCollator, times(1)).getHistory(any(), any())

        // retrieve history for the cached date range.
        var historyDays = analyzedDataProvider.getHistory(timeService.today().minusDays(cacheInterval.toLong()), timeService.today())

        // verify that no call is made to history collator and data is retrieved from cache..
        verify(historyCollator, times(1)).getHistory(any(), any())
        // verify that the returned values match the expected values.
        assertThat(historyDays, matchesHistoryDayList(mockHistoryDays))

        // send an update data message to mark the cache as invalid.
        val inhaleEvent = InhaleEvent()
        val changedObjects = ArrayList<Any>()
        changedObjects.add(inhaleEvent)
        analyzedDataProvider.onUpdateAnalysisData(UpdateAnalysisDataMessage(changedObjects))

        // retrieve history for the cached date range again.
        historyDays = analyzedDataProvider.getHistory(timeService.today().minusDays(cacheInterval.toLong()), timeService.today())

        // test expectations
        // verify that a call is made to the history collator to retrieve the data.
        verify(historyCollator, times(2)).getHistory(any(), any())
        // verify that the returned values match the expected values.
        assertThat(historyDays, matchesHistoryDayList(mockHistoryDays))

        // verify that the history update message was sent with the changed objects
        // passed in the UpdateAnalysisDataMessage.
        val historyUpdatedMessageArgumentCaptor = argumentCaptor<HistoryUpdatedMessage>()
        verify(messenger, times(2)).publish(historyUpdatedMessageArgumentCaptor.capture())
        assertEquals(historyUpdatedMessageArgumentCaptor.allValues[1].objectsChanged,
                changedObjects)
    }

    /**
     * This method creates mock history data for the passed date range with empty history.
     * @param startDate - the start date for the history
     * *
     * @param endDate - the end date for the history
     * *
     * @return - mock history data with empty history for each day in the range.
     */
    private fun createMockHistoryDays(startDate: LocalDate, endDate: LocalDate): List<HistoryDay> {
        val historyDays = ArrayList<HistoryDay>()
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            val day = HistoryDay(currentDate)
            historyDays.add(day)
            currentDate = currentDate.plusDays(1)
        }
        return historyDays
    }

    /**
     * This method returns the number of days in the cache.
     * @param cache - The cache.
     * *
     * @return - the number of days for which history data is cached.
     */
    private fun getNumberOfHistoryDaysInCache(cache: List<HistoryDay>): Int {
        var numberOfCachedDays = 0
        var oldestDate: LocalDate? = null
        var newestDate: LocalDate? = null

        for (day in cache) {
            val historyDate = day.day
            if (oldestDate == null || oldestDate.isAfter(historyDate)) {
                oldestDate = historyDate
            }
            if (newestDate == null || newestDate.isBefore(historyDate)) {
                newestDate = historyDate
            }
        }

        if (oldestDate != null && newestDate != null) {
            // add one to include the start and end dates.
            numberOfCachedDays = DAYS.between(oldestDate, newestDate).toInt() + 1
        }

        return numberOfCachedDays
    }

    companion object {

        private val GMT_ZONE_ID = ZoneId.ofOffset("GMT", ZoneOffset.UTC)
        private val DAYS_TO_CACHE = 7
    }
}
