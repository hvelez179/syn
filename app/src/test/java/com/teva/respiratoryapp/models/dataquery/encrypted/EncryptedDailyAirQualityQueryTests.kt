//
// EncryptedDailyAirQualityQueryTests.java
// app
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import com.nhaarman.mockito_kotlin.*
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.environment.entities.DailyAirQuality
import com.teva.environment.enumerations.AirQuality
import com.teva.respiratoryapp.services.data.DataService
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.entities.DailyAirQualityDataEncrypted
import com.teva.respiratoryapp.testutils.*
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

import java.util.ArrayList
import java.util.HashMap

/**
 * This class defines the unit tests for the EncryptedDailyAirQualityQuery class.
 */
class EncryptedDailyAirQualityQueryTests : BaseTest() {

    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var dataService: DataService
    private lateinit var messenger: Messenger
    private lateinit var timeService: TimeService

    /**
     * This method sets up the mock classes and methods needed for the test execution.
     */
    @Before
    fun setup() {
        dependencyProvider = DependencyProvider.default
        dependencyProvider.unregisterAll()

        dataService = mock()

        whenever(dataService.create(DailyAirQualityDataEncrypted::class.java)).thenAnswer { DailyAirQualityDataEncrypted() }

        messenger = mock()
        timeService = mock()

        dependencyProvider.register(Messenger::class, messenger)
        dependencyProvider.register(DataService::class, dataService)
        dependencyProvider.register(TimeService::class, timeService)
        dependencyProvider.register(EncryptedDailyAirQualityMapper())

        whenever(timeService.now()).thenAnswer { Instant.now() }

        val date1 = LocalDate.from(ZonedDateTime.of(2016, 12, 23, 11, 10, 9, 0, GMT_ZONE_ID))
        val date2 = LocalDate.from(ZonedDateTime.of(2016, 12, 24, 11, 10, 9, 0, GMT_ZONE_ID))

        val expectedSearchCriteria1 = SearchCriteria("date = %@", date1)
        val expectedSearchCriteria2 = SearchCriteria("date >= %@ AND date <= %@", date1, date2)

        val queryInfo1 = QueryInfo(expectedSearchCriteria1)

        val queryInfo2 = QueryInfo(expectedSearchCriteria2)

        val dailyAirQuality1 = Entity.DailyAirQuality(100, 1, date1)
        val dailyAirQuality2 = Entity.DailyAirQuality(120, 2, date2)


        whenever<List<DailyAirQualityDataEncrypted>>(dataService.fetchRequest(eq(DailyAirQualityDataEncrypted::class.java), argThat{matches(queryInfo1)})).thenAnswer {
            val dailyAirQualityEntries = ArrayList<DailyAirQualityDataEncrypted>()
            dailyAirQualityEntries.add(dailyAirQuality1)
            dailyAirQualityEntries
        }

        whenever<List<DailyAirQualityDataEncrypted>>(dataService.fetchRequest(eq(DailyAirQualityDataEncrypted::class.java), argThat{matches(queryInfo2)})).thenAnswer {
            val dailyAirQualityEntries = ArrayList<DailyAirQualityDataEncrypted>()
            dailyAirQualityEntries.add(dailyAirQuality1)
            dailyAirQualityEntries.add(dailyAirQuality2)
            dailyAirQualityEntries
        }

    }

    @Test
    fun testInsertOneDailyAirQualityEntryIntoDatabase() {

        //initialize test data
        val date = LocalDate.from(ZonedDateTime.of(2016, 12, 23, 11, 10, 9, 0, GMT_ZONE_ID))

        val dailyAirQuality = Entity.DailyAirQuality(100, 1, date)
        val dailyAirQualityModel = Model.DailyAirQuality(100, AirQuality.GOOD, date)

        // create expectations
        val expectedEntities = ArrayList<DailyAirQualityDataEncrypted>()
        expectedEntities.add(dailyAirQuality)

        val expectedSearchCriteria = ArrayList<SearchCriteria>()
        expectedSearchCriteria.add(SearchCriteria("date = %@", dailyAirQuality.date))

        // perform operation
        val query = EncryptedDailyAirQualityQuery(dependencyProvider)
        query.insert(dailyAirQualityModel)

        // test expectations

        verify(dataService).save(eq(DailyAirQualityDataEncrypted::class.java), argThat{matches(expectedEntities)}, argThat{matches(expectedSearchCriteria)})
    }

    @Test
    fun testQueryDailyAirQualityForGivenDateFromDatabase() {

        //initialize test data
        val date = LocalDate.from(ZonedDateTime.of(2016, 12, 23, 11, 10, 9, 0, GMT_ZONE_ID))
        val dailyAirQualityModel = Model.DailyAirQuality(100, AirQuality.GOOD, date)

        // create expectations
        val expectedSearchCriteria = SearchCriteria("date = %@", dailyAirQualityModel.date)

        // perform operation
        val query = EncryptedDailyAirQualityQuery(dependencyProvider)
        val returnedAirQuality = query.get(date)

        // test expectations
        val queryInfo = QueryInfo(expectedSearchCriteria)

        verify(dataService).fetchRequest(eq(DailyAirQualityDataEncrypted::class.java), argThat{matches(queryInfo)})
        assertTrue(returnedAirQuality!!.matches(dailyAirQualityModel))
    }

    @Test
    fun testQueryDailyAirQualityForInvalidDateFromDatabase() {

        //initialize test data
        val date = LocalDate.from(ZonedDateTime.of(2017, 12, 23, 11, 10, 9, 0, GMT_ZONE_ID))

        // create expectations
        val expectedSearchCriteria = SearchCriteria("date = %@", date)

        // perform operation
        val query = EncryptedDailyAirQualityQuery(dependencyProvider)
        val returnedAirQuality = query.get(date)

        // test expectations
        val queryInfo = QueryInfo(expectedSearchCriteria)

        verify(dataService).fetchRequest(eq(DailyAirQualityDataEncrypted::class.java), argThat{matches(queryInfo)})
        assertNull(returnedAirQuality)
    }

    @Test
    fun testQueryDailyAirQualityForADateRangeFromDatabase() {

        //initialize test data
        val date = LocalDate.from(ZonedDateTime.of(2016, 12, 23, 11, 10, 9, 0, GMT_ZONE_ID))
        val date2 = LocalDate.from(ZonedDateTime.of(2016, 12, 24, 11, 10, 9, 0, GMT_ZONE_ID))

        val dailyAirQualityModel = Model.DailyAirQuality(100, AirQuality.GOOD, date)
        val dailyAirQualityModel2 = Model.DailyAirQuality(120, AirQuality.MODERATE, date2)


        // create expectations
        val expectedDailyAirQualityEntries = HashMap<LocalDate, DailyAirQuality>()
        expectedDailyAirQualityEntries.put(date, dailyAirQualityModel)
        expectedDailyAirQualityEntries.put(date2, dailyAirQualityModel2)

        val expectedSearchCriteria = SearchCriteria("date >= %@ AND date <= %@", date, date2)

        // perform operation
        val query = EncryptedDailyAirQualityQuery(dependencyProvider)
        val returnedAirQualityEntries = query.get(date, date2)

        // test expectations
        val queryInfo = QueryInfo(expectedSearchCriteria)

        verify(dataService).fetchRequest(eq(DailyAirQualityDataEncrypted::class.java), argThat{matches(queryInfo)})
        assertTrue(returnedAirQualityEntries.matchesAirQuality(expectedDailyAirQualityEntries))
    }

    @Test
    fun testQueryDailyAirQualityForAnInvalidDateRangeFromDatabase() {

        //initialize test data
        val date = LocalDate.from(ZonedDateTime.of(2018, 12, 23, 11, 10, 9, 0, GMT_ZONE_ID))
        val date2 = LocalDate.from(ZonedDateTime.of(2018, 12, 24, 11, 10, 9, 0, GMT_ZONE_ID))

        // create expectations

        val expectedSearchCriteria = SearchCriteria("date >= %@ AND date <= %@", date, date2)

        // perform operation
        val query = EncryptedDailyAirQualityQuery(dependencyProvider)
        val returnedAirQualityEntries = query.get(date, date2)

        // test expectations
        val queryInfo = QueryInfo(expectedSearchCriteria)

        verify(dataService).fetchRequest(eq(DailyAirQualityDataEncrypted::class.java), argThat{matches(queryInfo)})
        assertEquals(0, returnedAirQualityEntries.size.toLong())
    }

    companion object {

        private val GMT_ZONE_ID = ZoneId.ofOffset("GMT", ZoneOffset.UTC)
    }
}
