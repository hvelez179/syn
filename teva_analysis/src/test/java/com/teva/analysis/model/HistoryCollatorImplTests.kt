//
// HistoryCollatorImplTests.kt
// teva_analysis
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.analysis.model

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.teva.analysis.utils.HistoryTestDataCreator
import com.teva.analysis.utils.HistoryDataMatcher.matchesHistoryDayList
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.devices.dataquery.ConnectionMetaDataQuery
import com.teva.devices.dataquery.InhaleEventDataQuery
import com.teva.devices.entities.InhaleEvent
import com.teva.medication.dataquery.MedicationDataQuery
import com.teva.medication.dataquery.PrescriptionDataQuery
import com.teva.medication.entities.Medication
import com.teva.medication.entities.Prescription
import com.teva.userfeedback.dataquery.DailyUserFeelingDataQuery
import com.teva.userfeedback.entities.DailyUserFeeling
import com.teva.userfeedback.model.UserFeelingManager
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.threeten.bp.*
import java.util.*

/**
 * This class defines unit tests for the HistoryCollatorImpl class.
 */
class HistoryCollatorImplTests {

    val dependencyProvider: DependencyProvider = DependencyProvider.default
    val messenger: Messenger = mock()
    val medicationDataQuery: MedicationDataQuery = mock()
    val dataQuery: InhaleEventDataQuery = mock()
    val timeService: TimeService = mock()
    val analyzedDataProvider: AnalyzedDataProvider = mock()
    val prescriptionDataQuery: PrescriptionDataQuery = mock()
    val dailyUserFeelingDataQuery: DailyUserFeelingDataQuery = mock()
    val connectionMetaDataQuery: ConnectionMetaDataQuery = mock()
    val userFeelingManager: UserFeelingManager = mock()


    val prescriptionHistory = ArrayList<Prescription>()
    val dailyUserFeelingHistory = HashMap<LocalDate, DailyUserFeeling>()
    val connectionMetaHistory = HashMap<LocalDate, Int>()
    val relieverInhaleEventHistory = ArrayList<InhaleEvent>()
    val medicationHistory = ArrayList<Medication>()

    @Before
    fun setup() {
        DependencyProvider.default.unregisterAll()

        dependencyProvider.register(Messenger::class, messenger)
        dependencyProvider.register(MedicationDataQuery::class, medicationDataQuery)
        dependencyProvider.register(InhaleEventDataQuery::class, dataQuery)
        dependencyProvider.register(TimeService::class, timeService)
        dependencyProvider.register(AnalyzedDataProvider::class, analyzedDataProvider)
        dependencyProvider.register(PrescriptionDataQuery::class, prescriptionDataQuery)
        dependencyProvider.register(DailyUserFeelingDataQuery::class, dailyUserFeelingDataQuery)
        dependencyProvider.register(ConnectionMetaDataQuery::class, connectionMetaDataQuery)
        dependencyProvider.register(UserFeelingManager::class, userFeelingManager)
        whenever(timeService.today()).thenReturn(LocalDate.of(2020, 1, 31))
        whenever(timeService.now()).thenReturn(Instant.from(ZonedDateTime.of(2020, 1, 31, 10, 10, 10, 0, GMT_ZONE_ID)))
    }

    @Test
    fun testGetHistoryReturnsProperlyCollatedHistoryData() {
        // create expectations
        val expectedHistoryDays = HistoryTestDataCreator.createHistoricalDataAndReturnExpectedCollatedHistory(
                timeService.today(), timeService.now(),
                prescriptionHistory, dailyUserFeelingHistory,
                connectionMetaHistory, relieverInhaleEventHistory,
                medicationHistory)

        //initialize test data
        whenever(dataQuery.get(any<LocalDate>(), any<LocalDate>())).thenReturn(relieverInhaleEventHistory)
        whenever(prescriptionDataQuery.getAll()).thenReturn(prescriptionHistory)
        whenever(medicationDataQuery.getAll()).thenReturn(medicationHistory)
        whenever(dailyUserFeelingDataQuery.get(any(), any())).thenReturn(dailyUserFeelingHistory)
        whenever(userFeelingManager.getUserFeelingHistoryFromDate(any(), any())).thenReturn(dailyUserFeelingHistory)
        whenever(connectionMetaDataQuery.get(any<LocalDate>(), any<LocalDate>())).thenReturn(connectionMetaHistory)

        // perform operation
        val historyCollator = HistoryCollatorImpl(dependencyProvider)
        val historyDays = historyCollator.getHistory(timeService.today().minusDays(20), timeService.today())

        // test expectations
        assertThat(historyDays, matchesHistoryDayList(expectedHistoryDays))
    }

    companion object {

        private val GMT_ZONE_ID = ZoneId.ofOffset("GMT", ZoneOffset.UTC)
    }
}
