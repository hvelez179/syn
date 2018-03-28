//
// ReportViewModelTest.java
// app
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.activity.viewmodel.tracker

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.teva.analysis.entities.HistoryDose
import com.teva.analysis.extensions.enumerations.InhalationEffort
import com.teva.analysis.model.AnalyzedDataProvider
import com.teva.analysis.model.HistoryDay
import com.teva.cloud.dataentities.UserProfile
import com.teva.cloud.models.userprofile.UserProfileManager
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.common.utilities.Messenger
import com.teva.devices.entities.InhaleEvent
import com.teva.respiratoryapp.common.DateTimeLocalization
import com.teva.respiratoryapp.mocks.MockedLocalizationService
import com.teva.respiratoryapp.testutils.mocks.AsyncTaskHelper
import com.teva.userfeedback.dataquery.DailyUserFeelingDataQuery
import com.teva.userfeedback.entities.DailyUserFeeling
import com.teva.userfeedback.enumerations.UserFeeling
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.threeten.bp.*
import java.util.*

/**
 * This class defines unit tests for the ReportViewModel class.
 */
class ReportViewModelTest {
    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var timeService: TimeService
    private lateinit var dateTimeLocalization: DateTimeLocalization
    private lateinit var analyzedDataProvider: AnalyzedDataProvider
    private lateinit var localizationService: MockedLocalizationService
    private lateinit var dailyUserFeelingDataQuery: DailyUserFeelingDataQuery
    private lateinit var expectedDailyInhaleEvents: HashMap<LocalDate, HashMap<InhalationEffort, Int>>
    private lateinit var expectedWeeklyInhaleEvents: HashMap<Int, HashMap<InhalationEffort, Int>>
    private lateinit var expectedDsaHistory: HashMap<LocalDate, DailyUserFeeling>
    private lateinit var userProfileManager: UserProfileManager

    private var currentLocalTime = LocalTime.of(16, 0)
    private var currentLocalTimePlusOneHour = LocalTime.of(17, 0)
    private var currentDate = LocalDate.of(2017, 3, 11)
    private var NUMBER_OF_DAYS_FOR_DAILY_SUMMARY = 30
    private var NUMBER_OF_WEEKS_FOR_WEEKLY_SUMMARY = 12

    private val profile = UserProfile(firstName = "John Smith", dateOfBirth = LocalDate.of(2002, 11, 15))

    @Before
    fun setup() {
        dependencyProvider = DependencyProvider.default
        dependencyProvider.unregisterAll()

        dependencyProvider.register(ZoneId::class, ZoneOffset.ofTotalSeconds(0))
        timeService = mock()
        whenever(timeService.localTime()).thenReturn(currentLocalTime)

        dateTimeLocalization = mock()
        analyzedDataProvider = mock()
        dailyUserFeelingDataQuery = mock()
        whenever(timeService.today()).thenReturn(currentDate)
        whenever(analyzedDataProvider.getHistory(any<LocalDate>(), any<LocalDate>())).thenReturn(ArrayList<HistoryDay>())
        dependencyProvider.register(TimeService::class, timeService)
        dependencyProvider.register(DateTimeLocalization::class, dateTimeLocalization)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        dependencyProvider.register(Messenger::class, mock<Messenger>())
        dependencyProvider.register(DailyUserFeelingDataQuery::class, dailyUserFeelingDataQuery)

        localizationService = MockedLocalizationService()
        dependencyProvider.register(LocalizationService::class, localizationService)

        userProfileManager = mock()
        whenever(userProfileManager.getActive()).thenReturn(profile)
        dependencyProvider.register(UserProfileManager::class, userProfileManager)

    }

    @Test
    fun testReportViewModelReturnsTheDateRangeForDailyInhaleEventSummaryGraphCorrectly() {
        val startDate = LocalDate.of(2017, 2, 10)
        val startDateString = "February 10, 2017"
        val endDateString = "March 11, 2017"
        val expectedDateRangeString = startDateString + " - " + endDateString
        whenever(dateTimeLocalization.toShortMonthDayYear(eq(startDate))).thenReturn(startDateString)
        whenever(dateTimeLocalization.toShortMonthDayYear(eq(currentDate))).thenReturn(endDateString)
        dependencyProvider.register(DateTimeLocalization::class, dateTimeLocalization)

        AsyncTaskHelper.beginTaskQueue()

        val reportViewModel = ReportViewModel(dependencyProvider)
        reportViewModel.onStart()

        AsyncTaskHelper.executeQueuedTasks()

        // retrieve the date range for daily summary report.
        val userReportDateTimeRange = reportViewModel.dailySummaryDateRange

        // verify that the date range includes 30 days from current date.
        assertEquals(expectedDateRangeString, userReportDateTimeRange)
    }


    @Test
    fun testReportViewModelReturnsCorrectDateAndTimeForDisplayOnThePrintableReport() {
        val expectedCurrentDisplayDate = "March 11, 2017"
        val expectedCurrentDisplayTime = "4:00 PM"
        whenever(dateTimeLocalization.toShortMonthDayYear(eq(currentDate))).thenReturn(expectedCurrentDisplayDate)
        whenever(dateTimeLocalization.toShortTime(eq(currentLocalTime))).thenReturn(expectedCurrentDisplayTime)
        dependencyProvider.register(DateTimeLocalization::class, dateTimeLocalization)

        AsyncTaskHelper.beginTaskQueue()

        val reportViewModel = ReportViewModel(dependencyProvider)
        reportViewModel.onStart()

        AsyncTaskHelper.executeQueuedTasks()

        val currentDisplayDate = reportViewModel.currentDisplayDate
        val currentDisplayTime = reportViewModel.currentDisplayTime

        // verify that the report date and time are correct.
        assertEquals(expectedCurrentDisplayDate, currentDisplayDate)
        assertEquals(expectedCurrentDisplayTime, currentDisplayTime)
    }

    @Test
    fun testReportViewModelReturnsCorrectDateWiseInhaleEventsForDisplayInDailySummaryGraph() {
        // create mocked inhale event data.
        createMockHistoryDataForInhaleEvents()

        AsyncTaskHelper.beginTaskQueue()

        val reportViewModel = ReportViewModel(dependencyProvider)
        reportViewModel.onStart()

        AsyncTaskHelper.executeQueuedTasks()

        // retrieve inhalation information for daily summary report.
        val returnedInhaleEvents = reportViewModel.getDayWiseInhaleEvents()
        val reportDate = reportViewModel.currentDate

        assertEquals(currentDate, reportDate)

        assertEquals(expectedDailyInhaleEvents.keys.size.toLong(), returnedInhaleEvents.keys.size.toLong())

        // verify that retrieved information matches expected information.
        for (date in returnedInhaleEvents.keys) {
            val expectedMap = expectedDailyInhaleEvents[date]
            val returnedMap = returnedInhaleEvents[date]

            assertEquals(expectedMap?.size?.toLong(), returnedMap?.size?.toLong())

            for (inhalationEffort in expectedMap?.keys!!) {
                assertEquals(expectedMap[inhalationEffort], returnedMap?.get(inhalationEffort))
            }
        }
    }

    @Test
    fun testReportViewModelReturnsCorrectWeekWiseInhaleEventsForDisplayInDailySummaryGraph() {
        // create mocked inhale event data.
        createMockHistoryDataForInhaleEvents()

        AsyncTaskHelper.beginTaskQueue()

        val reportViewModel = ReportViewModel(dependencyProvider)
        reportViewModel.onStart()

        AsyncTaskHelper.executeQueuedTasks()

        // retrieve inhalation information for weekly summary report.
        val returnedInhaleEvents = reportViewModel.getWeekWiseInhaleEvents()
        val reportDate = reportViewModel.currentDate

        assertEquals(currentDate, reportDate)

        assertEquals(expectedWeeklyInhaleEvents.keys.size.toLong(), returnedInhaleEvents.keys.size.toLong())

        // verify that retrieved information matches expected information.
        for (week in returnedInhaleEvents.keys) {
            val expectedMap = expectedWeeklyInhaleEvents[week]
            val returnedMap = returnedInhaleEvents[week]

            assertEquals(expectedMap?.size?.toLong(), returnedMap?.events?.size?.toLong())

            for (inhalationEffort in expectedMap?.keys!!) {
                assertEquals(expectedMap[inhalationEffort], returnedMap?.events?.get(inhalationEffort))
            }
        }
    }

    @Test
    fun testReportViewModelReturnsCorrectDateRangeForWeeklySummary() {
        val startDate = LocalDate.of(2016, 12, 18)
        val startDateString = "December 18, 2016"
        val endDateString = "March 11, 2017"
        val expectedDateRangeString = startDateString + " - " + endDateString
        whenever(dateTimeLocalization.toShortMonthDayYear(eq(startDate))).thenReturn(startDateString)
        whenever(dateTimeLocalization.toShortMonthDayYear(eq(currentDate))).thenReturn(endDateString)
        dependencyProvider.register(DateTimeLocalization::class, dateTimeLocalization)

        AsyncTaskHelper.beginTaskQueue()

        val reportViewModel = ReportViewModel(dependencyProvider)
        reportViewModel.onStart()

        AsyncTaskHelper.executeQueuedTasks()
        // retrieve the date range for weekly summary report.
        val weeklyReportDateRange = reportViewModel.weeklySummaryDateRange

        // verify that the returned date range includes 12 weeks from the current date.
        assertEquals(expectedDateRangeString, weeklyReportDateRange)
    }

    @Test
    fun testReportViewModelReturnsDsaDataObtainedFromDailyUserFeelingDataQuery() {
        createMockDSAData()

        AsyncTaskHelper.beginTaskQueue()

        val reportViewModel = ReportViewModel(dependencyProvider)
        reportViewModel.onStart()

        AsyncTaskHelper.executeQueuedTasks()

        // retrieve DSA for the DSA report.
        val returnedDsa = reportViewModel.dayWiseDSA
        assertEquals(expectedDsaHistory.size.toLong(), returnedDsa.size.toLong())

        // verify that the returned DSA matches expected DSA.
        for (date in returnedDsa.keys) {
            assertEquals(expectedDsaHistory[date]?.userFeeling, returnedDsa[date]?.userFeeling)
        }
    }

    /**
     * This method creates mock data for inhalaton events for the weekly and daily summary reports
     */
    private fun createMockHistoryDataForInhaleEvents() {
        val GMT_ZONE_ID = ZoneId.ofOffset("GMT", ZoneOffset.UTC)

        whenever(analyzedDataProvider.getHistory(any<LocalDate>(), any<LocalDate>())).thenAnswer { invocation ->
            val startDate = invocation.arguments[0] as LocalDate
            var endDate = invocation.arguments[1] as LocalDate

            expectedDailyInhaleEvents = HashMap<LocalDate, HashMap<InhalationEffort, Int>>()
            expectedWeeklyInhaleEvents = HashMap<Int, HashMap<InhalationEffort, Int>>()

            var dayCount = 0
            var weekCount = 0

            val history = ArrayList<HistoryDay>()
            while (!endDate.isBefore(startDate)) {
                val eventTime = Instant.from(ZonedDateTime.of(endDate, currentLocalTime, GMT_ZONE_ID))
                val eventTime2 = Instant.from(ZonedDateTime.of(endDate, currentLocalTimePlusOneHour, GMT_ZONE_ID))
                val day = HistoryDay(endDate)

                val inhaleEvent = InhaleEvent()
                inhaleEvent.eventTime = eventTime
                inhaleEvent.isValidInhale = true
                inhaleEvent.inhalePeak = 460
                val inhaleEvent2 = InhaleEvent()
                inhaleEvent2.eventTime = eventTime2
                inhaleEvent2.isValidInhale = true
                inhaleEvent2.inhalePeak = 2100
                val inhaleEvents = ArrayList<InhaleEvent>()
                inhaleEvents.add(inhaleEvent)
                inhaleEvents.add(inhaleEvent2)

                val relieverInhaleEvents = ArrayList<InhaleEvent>()
                relieverInhaleEvents.add(inhaleEvent)
                val relieverDose = HistoryDose("742750", relieverInhaleEvents)
                day.relieverDoses.add(relieverDose)

                val invalidInhaleEvents = ArrayList<InhaleEvent>()
                invalidInhaleEvents.add(inhaleEvent2)
                val invalidDose = HistoryDose("742750", invalidInhaleEvents)
                day.invalidDoses.add(invalidDose)

                history.add(day)
                if (dayCount < NUMBER_OF_DAYS_FOR_DAILY_SUMMARY) {
                    val dailyInhalationEffortMap = HashMap<InhalationEffort, Int>()
                    dailyInhalationEffortMap.put(InhalationEffort.GOOD_INHALATION, 1)
                    dailyInhalationEffortMap.put(InhalationEffort.ERROR, 1)
                    expectedDailyInhaleEvents.put(endDate, dailyInhalationEffortMap)
                }
                dayCount++

                if (dayCount % 7 == 0) {
                    val weeklyInhalationEffortMap = HashMap<InhalationEffort, Int>()
                    weeklyInhalationEffortMap.put(InhalationEffort.GOOD_INHALATION, 7)
                    weeklyInhalationEffortMap.put(InhalationEffort.ERROR, 7)
                    expectedWeeklyInhaleEvents.put(weekCount, weeklyInhalationEffortMap)
                    weekCount++
                }

                endDate = endDate.minusDays(1)
            }

            history
        }

        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
    }

    /**
     * This method creates mock data for dsa reports
     */
    private fun createMockDSAData() {
        whenever(dailyUserFeelingDataQuery.get(any<LocalDate>(), any<LocalDate>())).thenAnswer { invocation ->
            val startDate = invocation.arguments[0] as LocalDate
            var endDate = invocation.arguments[1] as LocalDate

            expectedDsaHistory = HashMap<LocalDate, DailyUserFeeling>()
            var userFeeling = UserFeeling.GOOD
            while (!endDate.isBefore(startDate)) {
                expectedDsaHistory.put(endDate, DailyUserFeeling(endDate.atStartOfDay().toInstant(ZoneOffset.UTC), userFeeling))
                when (userFeeling) {
                    UserFeeling.BAD -> userFeeling = UserFeeling.POOR
                    UserFeeling.POOR -> userFeeling = UserFeeling.GOOD
                    UserFeeling.GOOD -> userFeeling = UserFeeling.BAD
                }
                endDate = endDate.minusDays(1)
            }

            expectedDsaHistory
        }

        dependencyProvider.register(DailyUserFeelingDataQuery::class, dailyUserFeelingDataQuery)
    }
}
