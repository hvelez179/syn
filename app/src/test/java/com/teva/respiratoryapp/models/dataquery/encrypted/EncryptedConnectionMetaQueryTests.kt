//
// EncryptedConnectionMetaQueryTests.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import com.nhaarman.mockito_kotlin.*
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.devices.entities.ConnectionMeta
import com.teva.devices.enumerations.InhalerNameType
import com.teva.medication.enumerations.MedicationClassification
import com.teva.respiratoryapp.services.data.DataService
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.SortParameter
import com.teva.respiratoryapp.services.data.encrypteddata.entities.ConnectionMetaDataEncrypted
import com.teva.respiratoryapp.services.data.encrypteddata.entities.DeviceDataEncrypted
import com.teva.respiratoryapp.testutils.BaseTest
import com.teva.respiratoryapp.testutils.Entity
import com.teva.respiratoryapp.testutils.Model
import com.teva.respiratoryapp.testutils.ModelMatcher.matchesConnectionMeta
import com.teva.respiratoryapp.testutils.matches
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.threeten.bp.*
import java.util.*

/**
 * This class defines the unit tests for testing the EncryptedConnectionMetaQuery class.
 */
class EncryptedConnectionMetaQueryTests : BaseTest() {

    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var dataService: DataService
    private lateinit var messenger: Messenger
    private lateinit var timeService: TimeService

    /**
     * This method sets up the mocks for the classes and methods required for testing.
     */
    @Before
    fun setup() {
        dependencyProvider = DependencyProvider.default
        dependencyProvider.unregisterAll()

        dataService = mock()
        messenger = mock()
        timeService = mock()

        whenever(dataService.create(ConnectionMetaDataEncrypted::class.java)).thenAnswer { ConnectionMetaDataEncrypted() }

        dependencyProvider.register(Messenger::class, messenger)
        dependencyProvider.register(DataService::class, dataService)
        dependencyProvider.register(TimeService::class, timeService)
        dependencyProvider.register(EncryptedMedicationMapper::class, EncryptedMedicationMapper(dependencyProvider))
        dependencyProvider.register(EncryptedPrescriptionMapper::class, EncryptedPrescriptionMapper(dependencyProvider))
        dependencyProvider.register(EncryptedConnectionMetaMapper(dependencyProvider))

        whenever(timeService.now()).thenAnswer { Instant.now() }

        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val date = LocalDate.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val date2 = LocalDate.from(ZonedDateTime.of(2016, 12, 14, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)

        val mockDevice = Entity.Device(false, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, 1)
        mockDevice.primaryKeyId = 1
        val mockDeviceSearchCriteria = SearchCriteria("serialNumber = %@", mockDevice.serialNumber)
        val mockDeviceQueryInfo = QueryInfo(mockDeviceSearchCriteria)
        mockDeviceQueryInfo.count = 1

        whenever(dataService.fetchRequest(eq(DeviceDataEncrypted::class.java), argThat { matches(mockDeviceQueryInfo) })).thenAnswer {
            val arrayList = ArrayList<DeviceDataEncrypted>()
            arrayList.add(mockDevice)
            arrayList
        }

        whenever(dataService.fetchRequest(eq(DeviceDataEncrypted::class.java), isNull())).thenAnswer {
            val arrayList = ArrayList<DeviceDataEncrypted>()
            arrayList.add(mockDevice)
            arrayList
        }

        val mockConnectionMeta = Entity.ConnectionMeta(date, mockDevice)
        val queryInfo1 = QueryInfo(SearchCriteria("connectionDate = %@ AND device like %@", date, 1))
        val queryInfo2 = QueryInfo(SearchCriteria("connectionDate >= %@ AND connectionDate <= %@", date, date2), SortParameter("connectionDate", true))
        val queryInfo3 = QueryInfo(SearchCriteria("connectionDate = %@", date))
        whenever(dataService.fetchRequest(eq(ConnectionMetaDataEncrypted::class.java), argThat { matches(queryInfo1) })).thenAnswer {
            val arrayList = ArrayList<ConnectionMetaDataEncrypted>()
            arrayList.add(mockConnectionMeta)
            arrayList
        }
        whenever(dataService.fetchRequest(eq(ConnectionMetaDataEncrypted::class.java), argThat { matches(queryInfo2) })).thenAnswer {
            val arrayList = ArrayList<ConnectionMetaDataEncrypted>()
            arrayList.add(mockConnectionMeta)
            arrayList.add(mockConnectionMeta)
            arrayList
        }
        whenever(dataService.fetchRequest(eq(ConnectionMetaDataEncrypted::class.java), argThat { matches(queryInfo3) })).thenAnswer {
            val arrayList = ArrayList<ConnectionMetaDataEncrypted>()
            arrayList.add(mockConnectionMeta)
            arrayList
        }
    }

    @Test
    fun testInsertOneConnectionMetaIntoDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val date = LocalDate.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)

        val device = Entity.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, 1)
        device.primaryKeyId = 1
        val medication = Entity.Medication(true, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        medication.primaryKeyId = 1
        device.medication = medication
        val connectionMeta = Entity.ConnectionMeta(date, device)

        val connectionMetaModel = Model.ConnectionMeta(date, device.serialNumber)

        // create expectations
        val expectedEntities = ArrayList<ConnectionMetaDataEncrypted>()
        expectedEntities.add(connectionMeta)

        val expectedSearchCriteria = ArrayList<SearchCriteria>()
        expectedSearchCriteria.add(SearchCriteria("connectionDate = %@ AND device = %@", connectionMetaModel.connectionDate, device.primaryKeyId))

        // perform operation
        val query = EncryptedConnectionMetaQuery(dependencyProvider)
        query.insert(connectionMetaModel)

        // test expectations
        verify(dataService).save(eq(ConnectionMetaDataEncrypted::class.java), argThat { matches(expectedEntities) }, argThat { matches(expectedSearchCriteria) })
    }

    @Test
    fun testQueryConnectionCountForSingleDateFromDatabase() {

        //initialize test data
        val date = LocalDate.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))

        // create expectations
        val expectedSearchCriteria = SearchCriteria("connectionDate = %@", date)

        // perform operation
        val query = EncryptedConnectionMetaQuery(dependencyProvider)
        val count = query.get(date)

        // test expectations
        val expectedQueryInfo = QueryInfo(expectedSearchCriteria)

        verify(dataService).fetchRequest(eq(ConnectionMetaDataEncrypted::class.java), argThat { matches(expectedQueryInfo) })
        assertEquals(count.toLong(), 1)
    }

    @Test
    fun testQueryConnectionCountForSingleInvalidDateFromDatabase() {

        //initialize test data
        val date = LocalDate.from(ZonedDateTime.of(2018, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))

        // create expectations
        val expectedSearchCriteria = SearchCriteria("connectionDate = %@", date)

        // perform operation
        val query = EncryptedConnectionMetaQuery(dependencyProvider)
        val count = query.get(date)

        // test expectations
        val expectedQueryInfo = QueryInfo(expectedSearchCriteria)

        verify(dataService).fetchRequest(eq(ConnectionMetaDataEncrypted::class.java), argThat { matches(expectedQueryInfo) })
        assertEquals(count.toLong(), 0)
    }

    @Test
    fun testQueryConnectionMetaForDeviceAndDateFromDatabase() {

        //initialize test data
        val time = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val date = LocalDate.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)

        val medicationModel = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, time)
        val deviceModel = Model.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, time.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, time, "745750")
        deviceModel.medication = medicationModel
        val connectionMetaModel = Model.ConnectionMeta(date, deviceModel.serialNumber)

        // create expectations
        val expectedSearchCriteria = SearchCriteria("connectionDate = %@ AND device like %@", date, 1)

        // perform operation
        val query = EncryptedConnectionMetaQuery(dependencyProvider)
        val connectionMeta = query.get(deviceModel, date)

        // test expectations
        val expectedQueryInfo = QueryInfo(expectedSearchCriteria)

        verify(dataService).fetchRequest(eq(ConnectionMetaDataEncrypted::class.java), argThat { matches(expectedQueryInfo) })
        assertThat<ConnectionMeta>(connectionMeta, matchesConnectionMeta(connectionMetaModel))
    }

    @Test
    fun testQueryConnectionMetaForDeviceAndInvalidDateFromDatabase() {

        //initialize test data
        val time = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)

        val medicationModel = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, time)
        val deviceModel = Model.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, time.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, time, "745750")
        deviceModel.medication = medicationModel

        // create expectations
        val invalidDate = LocalDate.from(ZonedDateTime.of(2018, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expectedSearchCriteria = SearchCriteria("connectionDate = %@ AND device like %@", invalidDate, 1)

        // perform operation
        val query = EncryptedConnectionMetaQuery(dependencyProvider)
        val connectionMeta = query.get(deviceModel, invalidDate)

        // test expectations
        val expectedQueryInfo = QueryInfo(expectedSearchCriteria)

        verify(dataService).fetchRequest(eq(ConnectionMetaDataEncrypted::class.java), argThat { matches(expectedQueryInfo) })
        assertNull(connectionMeta)
    }

    @Test
    fun testQueryConnectionCountForDateRangeFromDatabase() {

        //initialize test data
        val date = LocalDate.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val date2 = LocalDate.from(ZonedDateTime.of(2016, 12, 14, 11, 10, 9, 0, GMT_ZONE_ID))

        // create expectations
        val expectedSearchCriteria = SearchCriteria("connectionDate >= %@ AND connectionDate <= %@", date, date2)

        // perform operation
        val query = EncryptedConnectionMetaQuery(dependencyProvider)
        val returnedCounts = query.get(date, date2)

        // test expectations
        val expectedQueryInfo = QueryInfo(expectedSearchCriteria, SortParameter("connectionDate", true))

        verify(dataService).fetchRequest(eq(ConnectionMetaDataEncrypted::class.java), argThat { matches(expectedQueryInfo) })
        assertEquals(returnedCounts.size.toLong(), 1)
        assertEquals(returnedCounts[date], 2)
    }

    companion object {

        private val GMT_ZONE_ID = ZoneId.ofOffset("GMT", ZoneOffset.UTC)
    }
}
