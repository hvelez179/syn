package com.teva.respiratoryapp.models.dataquery.encrypted

import com.nhaarman.mockito_kotlin.*
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.devices.entities.InhaleEvent
import com.teva.devices.enumerations.InhalerNameType
import com.teva.medication.enumerations.MedicationClassification
import com.teva.respiratoryapp.services.data.DataService
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.SortParameter
import com.teva.respiratoryapp.services.data.encrypteddata.entities.DeviceDataEncrypted
import com.teva.respiratoryapp.services.data.encrypteddata.entities.InhaleEventDataEncrypted
import com.teva.respiratoryapp.services.data.encrypteddata.entities.MedicationDataEncrypted
import com.teva.respiratoryapp.testutils.*

import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

import java.util.ArrayList

import com.teva.respiratoryapp.testutils.ModelMatcher.matchesInhaleEvent
import com.teva.respiratoryapp.testutils.ModelMatcher.matchesInhaleEventList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue

class EncryptedInhaleEventQueryTests : BaseTest() {

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

        whenever(dataService.create(InhaleEventDataEncrypted::class.java)).thenAnswer { InhaleEventDataEncrypted() }

        dependencyProvider.register(Messenger::class, messenger)
        dependencyProvider.register(DataService::class, dataService)
        dependencyProvider.register(TimeService::class, timeService)
        dependencyProvider.register(EncryptedMedicationMapper(dependencyProvider))
        dependencyProvider.register(EncryptedDeviceMapper(dependencyProvider))
        dependencyProvider.register(EncryptedInhaleEventMapper(dependencyProvider))

        whenever(timeService.now()).thenAnswer { Instant.now() }

        val event1Time = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val event2Time = event1Time.plusSeconds(10800)
        val expirationDate = LocalDate.of(2017, 12, 1)

        //mock retrieval of medication from database
        val mockInhaleEvent = Entity.InhaleEvent(1, event1Time, 0, 10, 10, 5, 3, 1, 15,
                1, "Cartridge1", 12, 2, 1, 1, event1Time, "123454321", 1)
        val mockInhaleEvent2 = Entity.InhaleEvent(2, event2Time, 0, 8, 8, 5, 4, 1, 14,
                1, "Cartridge1", 13, 3, 1, 1, event2Time, "123454321", 1)

        val mockInhaleEventQueryInfo = QueryInfo(SearchCriteria("device = %@ AND eventUID = %@", mockInhaleEvent.device!!.primaryKeyId, mockInhaleEvent.eventUID))
        mockInhaleEventQueryInfo.count = 1

        val mockInhaleEventQueryInfo2 = QueryInfo(SearchCriteria("device = %@ AND eventUID = %@", mockInhaleEvent2.device!!.primaryKeyId, mockInhaleEvent2.eventUID))
        mockInhaleEventQueryInfo2.count = 1

        whenever(dataService.fetchRequest(eq(InhaleEventDataEncrypted::class.java), argThat{matches(mockInhaleEventQueryInfo)})).thenAnswer {
            val arrayList = ArrayList<InhaleEventDataEncrypted>()
            arrayList.add(mockInhaleEvent)
            arrayList
        }

        whenever(dataService.fetchRequest(eq(InhaleEventDataEncrypted::class.java), argThat{matches(mockInhaleEventQueryInfo2)})).thenAnswer {
            val arrayList = ArrayList<InhaleEventDataEncrypted>()
            arrayList.add(mockInhaleEvent2)
            arrayList
        }

        val mockInhaleEventQueryInfo3 = QueryInfo(SearchCriteria("date >= %@ AND date <= %@", LocalDate.of(2016, 12, 13), LocalDate.of(2016, 12, 13)))
        mockInhaleEventQueryInfo3.count = 1
        whenever(dataService.fetchRequest(eq(InhaleEventDataEncrypted::class.java), argThat{matches(mockInhaleEventQueryInfo3)})).thenAnswer {
            val arrayList = ArrayList<InhaleEventDataEncrypted>()
            arrayList.add(mockInhaleEvent)
            arrayList.add(mockInhaleEvent2)
            arrayList
        }

        val mockInhaleEventQueryInfo4 = QueryInfo(SearchCriteria("device = %@", mockInhaleEvent2.device!!.primaryKeyId))
        whenever(dataService.fetchRequest(eq(InhaleEventDataEncrypted::class.java), argThat{matches(mockInhaleEventQueryInfo4)})).thenAnswer {
            val arrayList = ArrayList<InhaleEventDataEncrypted>()
            arrayList.add(mockInhaleEvent2)
            arrayList.add(mockInhaleEvent)
            arrayList
        }

        val mockInhaleEventQueryInfo5 = QueryInfo(null, SortParameter("date", false), SortParameter("timezoneOffset", false))
        mockInhaleEventQueryInfo5.count = 2
        whenever(dataService.fetchRequest(eq(InhaleEventDataEncrypted::class.java), argThat{matches(mockInhaleEventQueryInfo5)})).thenAnswer {
            val arrayList = ArrayList<InhaleEventDataEncrypted>()
            arrayList.add(mockInhaleEvent2)
            arrayList.add(mockInhaleEvent)
            arrayList
        }

        val mockInhaleEventQueryInfo6 = QueryInfo(null, SortParameter("date", true), SortParameter("timezoneOffset", true))
        mockInhaleEventQueryInfo5.count = 1
        whenever(dataService.fetchRequest(eq(InhaleEventDataEncrypted::class.java), argThat{matches(mockInhaleEventQueryInfo6)})).thenAnswer {
            val arrayList = ArrayList<InhaleEventDataEncrypted>()
            arrayList.add(mockInhaleEvent)
            arrayList.add(mockInhaleEvent2)
            arrayList
        }

        //mock retrieval of device from database
        val mockDevice = Entity.Device(false, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, event1Time.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, event1Time, 1)
        mockDevice.primaryKeyId = 1
        val mockDeviceSearchCriteria = SearchCriteria("serialNumber = %@", mockDevice.serialNumber)
        val mockDeviceQueryInfo = QueryInfo(mockDeviceSearchCriteria)
        mockDeviceQueryInfo.count = 1

        whenever(dataService.fetchRequest(eq(DeviceDataEncrypted::class.java), argThat{matches(mockDeviceQueryInfo)})).thenAnswer {
            val arrayList = ArrayList<DeviceDataEncrypted>()
            arrayList.add(mockDevice)
            arrayList
        }

        whenever(dataService.fetchRequest(eq(DeviceDataEncrypted::class.java), isNull())).thenAnswer {
            val arrayList = ArrayList<DeviceDataEncrypted>()
            arrayList.add(mockDevice)
            arrayList
        }

        val mockDeviceQueryInfo2 = QueryInfo(SearchCriteria("Z_PK = %@", mockDevice.primaryKeyId))
        mockDeviceQueryInfo2.count = 1

        whenever(dataService.fetchRequest(eq(DeviceDataEncrypted::class.java), argThat{matches(mockDeviceQueryInfo2)})).thenAnswer {
            val arrayList = ArrayList<DeviceDataEncrypted>()
            arrayList.add(mockDevice)
            arrayList
        }

        val mockMedication = Entity.Medication(false, "745750", "ProAir", "HFA", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, event1Time)
        mockMedication.primaryKeyId = 1
        whenever(dataService.fetchRequest(eq(MedicationDataEncrypted::class.java), isNull())).thenAnswer {
            val arrayList = ArrayList<MedicationDataEncrypted>()
            arrayList.add(mockMedication)
            arrayList
        }

        val mockMedicationQueryInfo = QueryInfo(SearchCriteria("Z_PK = %@", mockMedication.primaryKeyId))
        mockMedicationQueryInfo.count = 1

        whenever(dataService.fetchRequest(eq(MedicationDataEncrypted::class.java), argThat{matches(mockMedicationQueryInfo)})).thenAnswer {
            val arrayList = ArrayList<MedicationDataEncrypted>()
            arrayList.add(mockMedication)
            arrayList
        }

    }

    @Test
    fun testInsertOneInhaleEventIntoDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)

        val device = Entity.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, 1)
        device.primaryKeyId = 1
        val inhaleEvent = Entity.InhaleEvent(1, changedTime, 0, 10, 10, 5, 3, 1, 15,
                1, "Cartridge1", 12, 2, 1, 1, changedTime, "123454321", 1)
        inhaleEvent.device = device

        val inhaleEventModel = Model.InhaleEvent(1, changedTime, 0, 10, 10, 5, 3, 1, 15,
                1, "Cartridge1", 12, 2, true, true, changedTime, "123454321")
        val deviceModel = Model.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, "745750")
        inhaleEventModel.deviceSerialNumber = deviceModel.serialNumber

        // create expectations
        val expectedEntities = ArrayList<InhaleEventDataEncrypted>()
        expectedEntities.add(inhaleEvent)

        val expectedSearchCriteria = ArrayList<SearchCriteria>()
        expectedSearchCriteria.add(SearchCriteria("device = %@ AND eventUID = %@", device.primaryKeyId, inhaleEventModel.eventUID))

        // perform operation
        val query = EncryptedInhaleEventQuery(dependencyProvider)
        query.insert(inhaleEventModel)

        // test expectations

        verify(dataService).save(eq(InhaleEventDataEncrypted::class.java), argThat{matches(expectedEntities)}, argThat{matches(expectedSearchCriteria)})
    }

    @Test
    fun testGetOneInhaleEventFromDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)
        val eventUID = 1
        val devicePrimaryKey = 1

        val deviceModel = Model.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, "745750")

        val inhaleEventModel = Model.InhaleEvent(1, changedTime, 0, 10, 10, 5, 3, 1, 15,
                1, "Cartridge1", 12, 2, true, true, changedTime, "123454321")
        inhaleEventModel.deviceSerialNumber = deviceModel.serialNumber
        inhaleEventModel.drugUID = deviceModel.medication!!.drugUID

        // create expectations
        val queryInfo = QueryInfo(SearchCriteria("device = %@ AND eventUID = %@", devicePrimaryKey, eventUID))

        // perform operation
        val query = EncryptedInhaleEventQuery(dependencyProvider)
        val returnedInhaleEvent = query.get(eventUID, deviceModel)

        // test expectations
        verify(dataService).fetchRequest(eq(InhaleEventDataEncrypted::class.java), argThat{matches(queryInfo)})
        assertThat<InhaleEvent>(returnedInhaleEvent, matchesInhaleEvent(inhaleEventModel))
    }

    @Test
    fun testGetInhaleEventsWithinDateRangeFromDatabase() {

        //initialize test data
        val startDate = LocalDate.of(2016, 12, 13)
        val endDate = LocalDate.of(2016, 12, 13)

        val event1Time = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val event2Time = event1Time.plusSeconds(10800)
        val expirationDate = LocalDate.of(2017, 12, 1)
        val eventUID = 1
        val devicePrimaryKey = 1

        val deviceModel = Model.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, event1Time.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, event1Time, "745750")

        val inhaleEventModel = Model.InhaleEvent(1, event1Time, 0, 10, 10, 5, 3, 1, 15,
                1, "Cartridge1", 12, 2, true, true, event1Time, "123454321")
        inhaleEventModel.deviceSerialNumber = deviceModel.serialNumber
        inhaleEventModel.drugUID = deviceModel.medication!!.drugUID

        val inhaleEventModel2 = Model.InhaleEvent(2, event2Time, 0, 8, 8, 5, 4, 1, 14,
                1, "Cartridge1", 13, 3, true, true, event2Time, "123454321")
        inhaleEventModel2.deviceSerialNumber = deviceModel.serialNumber
        inhaleEventModel2.drugUID = deviceModel.medication!!.drugUID

        val expectedInhaleEvents = ArrayList<InhaleEvent>()
        expectedInhaleEvents.add(inhaleEventModel)
        expectedInhaleEvents.add(inhaleEventModel2)

        // create expectations
        val queryInfo = QueryInfo(SearchCriteria("date >= %@ AND date <= %@", startDate, endDate))

        // perform operation
        val query = EncryptedInhaleEventQuery(dependencyProvider)
        val returnedInhaleEvents = query.get(LocalDate.of(2016, 12, 13), LocalDate.of(2016, 12, 13))

        // test expectations
        verify(dataService).fetchRequest(eq(InhaleEventDataEncrypted::class.java), argThat{matches(queryInfo)})
        assertThat(returnedInhaleEvents, matchesInhaleEventList(expectedInhaleEvents))
    }

    @Test
    fun testGetCountOfInhaleEventsWithinDateRangeFromDatabase() {

        //initialize test data
        val startDate = LocalDate.of(2016, 12, 13)
        val endDate = LocalDate.of(2016, 12, 13)

        // create expectations
        val searchCriteria = SearchCriteria("date >= %@ AND date <= %@", startDate, endDate)

        // perform operation
        val query = EncryptedInhaleEventQuery(dependencyProvider)

        whenever(dataService.getCount(eq(InhaleEventDataEncrypted::class.java), any())).thenReturn(2)
        val count = query.getCount(LocalDate.of(2016, 12, 13), LocalDate.of(2016, 12, 13))

        // test expectations
        verify(dataService).getCount(eq(InhaleEventDataEncrypted::class.java), argThat{matches(searchCriteria)})
        assertEquals(count.toLong(), 2)
    }

    @Test
    fun testGetInhaleEventsForDeviceFromDatabase() {

        //initialize test data
        val event1Time = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val event2Time = event1Time.plusSeconds(10800)
        val expirationDate = LocalDate.of(2017, 12, 1)
        val devicePrimaryKey = 1

        val deviceModel = Model.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, event1Time.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, event1Time, "745750")

        val inhaleEventModel = Model.InhaleEvent(1, event1Time, 0, 10, 10, 5, 3, 1, 15,
                1, "Cartridge1", 12, 2, true, true, event1Time, "123454321")
        inhaleEventModel.deviceSerialNumber = deviceModel.serialNumber
        inhaleEventModel.drugUID = deviceModel.medication!!.drugUID

        val inhaleEventModel2 = Model.InhaleEvent(2, event2Time, 0, 8, 8, 5, 4, 1, 14,
                1, "Cartridge1", 13, 3, true, true, event2Time, "123454321")
        inhaleEventModel2.deviceSerialNumber = deviceModel.serialNumber
        inhaleEventModel2.drugUID = deviceModel.medication!!.drugUID

        val expectedInhaleEvents = ArrayList<InhaleEvent>()
        expectedInhaleEvents.add(inhaleEventModel2)
        expectedInhaleEvents.add(inhaleEventModel)

        // create expectations
        val queryInfo = QueryInfo(SearchCriteria("device = %@", devicePrimaryKey))

        // perform operation
        val query = EncryptedInhaleEventQuery(dependencyProvider)
        val returnedInhaleEvents = query.get(deviceModel)

        // test expectations
        verify(dataService).fetchRequest(eq(InhaleEventDataEncrypted::class.java), argThat{matches(queryInfo)})
        assertThat(returnedInhaleEvents, matchesInhaleEventList(expectedInhaleEvents))
    }

    @Test
    fun testVerifyIfDeviceHasInhaleEventsInDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)
        val devicePrimaryKey = 1

        val deviceModel = Model.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, "745750")

        // create expectations
        val searchCriteria = SearchCriteria("device = %@", devicePrimaryKey)

        // perform operation
        val query = EncryptedInhaleEventQuery(dependencyProvider)

        whenever(dataService.getCount(eq(InhaleEventDataEncrypted::class.java), any())).thenReturn(2)

        val inhaleEventsFound = query.hasData(deviceModel)

        // test expectations
        verify(dataService).getCount(eq(InhaleEventDataEncrypted::class.java), argThat{matches(searchCriteria)})
        assertTrue(inhaleEventsFound)
    }

    @Test
    fun testGetLastNInhaleEventsFromDatabase() {

        //initialize test data
        val event1Time = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val event2Time = event1Time.plusSeconds(10800)
        val expirationDate = LocalDate.of(2017, 12, 1)
        val devicePrimaryKey = 1

        val deviceModel = Model.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, event1Time.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, event1Time, "745750")

        val inhaleEventModel = Model.InhaleEvent(1, event1Time, 0, 10, 10, 5, 3, 1, 15,
                1, "Cartridge1", 12, 2, true, true, event1Time, "123454321")
        inhaleEventModel.deviceSerialNumber = deviceModel.serialNumber
        inhaleEventModel.drugUID = deviceModel.medication!!.drugUID

        val inhaleEventModel2 = Model.InhaleEvent(2, event2Time, 0, 8, 8, 5, 4, 1, 14,
                1, "Cartridge1", 13, 3, true, true, event2Time, "123454321")
        inhaleEventModel2.deviceSerialNumber = deviceModel.serialNumber
        inhaleEventModel2.drugUID = deviceModel.medication!!.drugUID

        val expectedInhaleEvents = ArrayList<InhaleEvent>()
        expectedInhaleEvents.add(inhaleEventModel2)
        expectedInhaleEvents.add(inhaleEventModel)

        // create expectations
        val queryInfo = QueryInfo(null, SortParameter("date", false), SortParameter("timezoneOffset", false))
        queryInfo.count = 2

        // perform operation
        val query = EncryptedInhaleEventQuery(dependencyProvider)
        val returnedInhaleEvents = query.getLast(2)

        // test expectations
        verify(dataService).fetchRequest(eq(InhaleEventDataEncrypted::class.java), argThat{matches(queryInfo)})
        assertThat(returnedInhaleEvents, matchesInhaleEventList(expectedInhaleEvents))
    }

    @Test
    fun testGetEarliestInhaleEventDateFromDatabase() {

        //initialize test data
        val expectedEventTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))

        // create expectations
        val queryInfo = QueryInfo(null, SortParameter("date", true), SortParameter("timezoneOffset", true))
        queryInfo.count = 1

        // perform operation
        val query = EncryptedInhaleEventQuery(dependencyProvider)
        val earliestEventTime = query.getEarliestInhaleEventDate()

        // test expectations
        verify(dataService).fetchRequest(eq(InhaleEventDataEncrypted::class.java), argThat{matches(queryInfo)})
        assertEquals(earliestEventTime, expectedEventTime)
    }

    companion object {

        private val GMT_ZONE_ID = ZoneId.ofOffset("GMT", ZoneOffset.UTC)
    }
}
