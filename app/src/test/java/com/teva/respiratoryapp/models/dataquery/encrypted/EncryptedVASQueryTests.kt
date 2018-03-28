//
// EncryptedVASQueryTests.java
// app
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import com.nhaarman.mockito_kotlin.*
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.respiratoryapp.services.data.DataService
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.entities.DailyUserFeelingDataEncrypted
import com.teva.respiratoryapp.testutils.BaseTest
import com.teva.respiratoryapp.testutils.Entity
import com.teva.respiratoryapp.testutils.Model
import com.teva.respiratoryapp.testutils.matches
import com.teva.userfeedback.entities.DailyUserFeeling
import com.teva.userfeedback.enumerations.UserFeeling
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.threeten.bp.*
import java.util.*

class EncryptedVASQueryTests : BaseTest() {

    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var dataService: DataService
    private lateinit var messenger: Messenger
    private lateinit var timeService: TimeService

    @Before
    fun setup() {

        dependencyProvider = DependencyProvider.default
        dataService = mock()
        messenger = mock()
        timeService = mock()

        whenever(dataService.create(DailyUserFeelingDataEncrypted::class.java)).thenAnswer {
            DailyUserFeelingDataEncrypted()
        }

        dependencyProvider = DependencyProvider.default
        dependencyProvider.register(Messenger::class, messenger)
        dependencyProvider.register(DataService::class, dataService)
        dependencyProvider.register(TimeService::class, timeService)
        dependencyProvider.register(EncryptedVASMapper())

        whenever(timeService.now()).thenAnswer {
            Instant.ofEpochSecond(1491226442L)
        }

        val eventTime1 = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val eventTime2 = Instant.from(ZonedDateTime.of(2016, 12, 14, 11, 10, 9, 0, GMT_ZONE_ID))
        val eventTime3 = Instant.from(ZonedDateTime.of(2016, 12, 15, 11, 10, 9, 0, GMT_ZONE_ID))
        val eventDate1 = LocalDate.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val eventDate2 = LocalDate.from(ZonedDateTime.of(2016, 12, 14, 11, 10, 9, 0, GMT_ZONE_ID))
        val eventDate3 = LocalDate.from(ZonedDateTime.of(2016, 12, 15, 11, 10, 9, 0, GMT_ZONE_ID))

        val userFeeling1 = Entity.DailyUserFeeling(eventDate1, eventTime1, 1, 1, eventTime1)
        val userFeeling2 = Entity.DailyUserFeeling(eventDate2, eventTime2, 2, 1, eventTime2)
        val userFeeling3 = Entity.DailyUserFeeling(eventDate3, eventTime3, 1, 1, eventTime3)

        val mockUserFeelingQueryInfo = QueryInfo(SearchCriteria("date >= %@ AND date <= %@", eventDate1, eventDate3))
        mockUserFeelingQueryInfo.count = 1

        whenever(dataService.fetchRequest(eq(DailyUserFeelingDataEncrypted::class.java), argThat { matches(mockUserFeelingQueryInfo) })).thenAnswer {
            val arrayList = ArrayList<DailyUserFeelingDataEncrypted>()
            arrayList.add(userFeeling1)
            arrayList.add(userFeeling2)
            arrayList.add(userFeeling3)
            return@thenAnswer arrayList
        }

        val mockUserFeelingQueryInfo1 = QueryInfo(SearchCriteria("date = %@", eventDate1))
        mockUserFeelingQueryInfo1.count = 1

        whenever(dataService.fetchRequest(eq(DailyUserFeelingDataEncrypted::class.java), argThat { matches(mockUserFeelingQueryInfo1) })).thenAnswer {
            val arrayList = ArrayList<DailyUserFeelingDataEncrypted>()
            arrayList.add(userFeeling1)
            return@thenAnswer arrayList
        }
    }

    @Test
    fun testInsertOneDailyUserFeelingEntryIntoDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val dailyUserFeelingDate = LocalDate.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))

        val userFeeling = Entity.DailyUserFeeling(dailyUserFeelingDate, changedTime, 1, 1, changedTime)

        val dailyUserFeelingModel = Model.DailyUserFeeling(dailyUserFeelingDate, changedTime, UserFeeling.GOOD, true, changedTime)

        // create expectations
        val expectedEntities = ArrayList<DailyUserFeelingDataEncrypted>()
        expectedEntities.add(userFeeling)

        val expectedSearchCriteria = ArrayList<SearchCriteria>()
        expectedSearchCriteria.add(SearchCriteria("date = %@", dailyUserFeelingModel.date!!))

        // perform operation
        val query = EncryptedVASQuery(dependencyProvider)
        query.insert(dailyUserFeelingModel)

        // test expectations
        verify(dataService).save(eq(DailyUserFeelingDataEncrypted::class.java), argThat { matches(expectedEntities) }, argThat { matches(expectedSearchCriteria) })
    }

    @Test
    fun testGetDailyUserFeelingByDateFromDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val dailyUserFeelingDate = LocalDate.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expectedUserFeeling = Model.DailyUserFeeling(dailyUserFeelingDate, changedTime, UserFeeling.GOOD, true, changedTime)

        // create expectations
        val queryInfo = QueryInfo(SearchCriteria("date = %@", dailyUserFeelingDate))

        // perform operation
        val query = EncryptedVASQuery(dependencyProvider)
        val returnedUserFeeling = query.get(dailyUserFeelingDate)

        // test expectations

        verify(dataService).fetchRequest(eq(DailyUserFeelingDataEncrypted::class.java), argThat { matches(queryInfo) })
        assertTrue(returnedUserFeeling!!.matches(expectedUserFeeling))
    }

    @Test
    fun testGetDailyUserFeelingWithinDateRangeFromDatabase() {

        //initialize test data
        val eventDate1 = LocalDate.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val eventDate2 = LocalDate.from(ZonedDateTime.of(2016, 12, 14, 11, 10, 9, 0, GMT_ZONE_ID))
        val eventDate3 = LocalDate.from(ZonedDateTime.of(2016, 12, 15, 11, 10, 9, 0, GMT_ZONE_ID))
        val eventTime1 = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val eventTime2 = Instant.from(ZonedDateTime.of(2016, 12, 14, 11, 10, 9, 0, GMT_ZONE_ID))
        val eventTime3 = Instant.from(ZonedDateTime.of(2016, 12, 15, 11, 10, 9, 0, GMT_ZONE_ID))

        val userFeeling1 = Model.DailyUserFeeling(eventDate1, eventTime1, UserFeeling.GOOD, true, eventTime1)
        val userFeeling2 = Model.DailyUserFeeling(eventDate2, eventTime2, UserFeeling.POOR, true, eventTime2)
        val userFeeling3 = Model.DailyUserFeeling(eventDate3, eventTime3, UserFeeling.GOOD, true, eventTime3)

        val expectedUserFeelings = HashMap<LocalDate, DailyUserFeeling>()
        expectedUserFeelings.put(eventDate1, userFeeling1)
        expectedUserFeelings.put(eventDate2, userFeeling2)
        expectedUserFeelings.put(eventDate3, userFeeling3)


        // create expectations
        val queryInfo = QueryInfo(SearchCriteria("date >= %@ AND date <= %@", eventDate1, eventDate3))

        // perform operation
        val query = EncryptedVASQuery(dependencyProvider)
        val returnedUserFeelings = query.get(eventDate1, eventDate3)

        // test expectations

        verify(dataService).fetchRequest(eq(DailyUserFeelingDataEncrypted::class.java), argThat { matches(queryInfo) })
        assertTrue(returnedUserFeelings.matches(expectedUserFeelings))
    }

    @Test
    fun testGetDailyUserFeelingForInvalidDateFromDatabase() {

        //initialize test data
        val dailyUserFeelingDate = LocalDate.from(ZonedDateTime.of(2017, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))

        // create expectations
        val queryInfo = QueryInfo(SearchCriteria("date = %@", dailyUserFeelingDate))

        // perform operation
        val query = EncryptedVASQuery(dependencyProvider)
        val returnedUserFeeling = query.get(dailyUserFeelingDate)

        // test expectations

        verify(dataService).fetchRequest(eq(DailyUserFeelingDataEncrypted::class.java), argThat { matches(queryInfo) })
        assertNull(returnedUserFeeling)
    }

    @Test
    fun testGetDailyUserFeelingForInvalidDateRangeFromDatabase() {

        //initialize test data
        val startDate = LocalDate.from(ZonedDateTime.of(2017, 12, 1, 11, 10, 9, 0, GMT_ZONE_ID))
        val endDate = LocalDate.from(ZonedDateTime.of(2017, 12, 30, 11, 10, 9, 0, GMT_ZONE_ID))

        // create expectations
        val queryInfo = QueryInfo(SearchCriteria("date >= %@ AND date <= %@", startDate, endDate))

        // perform operation
        val query = EncryptedVASQuery(dependencyProvider)
        val returnedUserFeelings = query.get(startDate, endDate)

        // test expectations

        verify(dataService).fetchRequest(eq(DailyUserFeelingDataEncrypted::class.java), argThat { matches(queryInfo) })
        assertEquals(0, returnedUserFeelings.size.toLong())
    }

    companion object {

        private val GMT_ZONE_ID = ZoneId.ofOffset("GMT", ZoneOffset.UTC)
    }
}
