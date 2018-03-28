package com.teva.respiratoryapp.models.dataquery.encrypted

import com.nhaarman.mockito_kotlin.*
import com.teva.common.services.ServerTimeService
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.devices.entities.Device
import com.teva.devices.enumerations.InhalerNameType
import com.teva.medication.enumerations.MedicationClassification
import com.teva.respiratoryapp.services.data.DataService
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.SortParameter
import com.teva.respiratoryapp.services.data.encrypteddata.entities.DeviceDataEncrypted
import com.teva.respiratoryapp.services.data.encrypteddata.entities.MedicationDataEncrypted
import com.teva.respiratoryapp.testutils.BaseTest
import com.teva.respiratoryapp.testutils.Entity
import com.teva.respiratoryapp.testutils.Model
import com.teva.respiratoryapp.testutils.ModelMatcher.matchesDevice
import com.teva.respiratoryapp.testutils.ModelMatcher.matchesDeviceList
import com.teva.respiratoryapp.testutils.matches
import junit.framework.Assert.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.threeten.bp.*
import java.util.*

class EncryptedDeviceQueryTests : BaseTest() {

    internal lateinit var dependencyProvider: DependencyProvider
    internal lateinit var dataService: DataService
    internal lateinit var messenger: Messenger
    internal lateinit var timeService: TimeService
    internal lateinit var serverTimeService: ServerTimeService

    @Before
    fun setup() {
        dependencyProvider = DependencyProvider.default
        dataService = mock()
        messenger = mock()
        timeService = mock()
        serverTimeService = mock()

        dataService = mock()

        whenever(dataService.create(DeviceDataEncrypted::class.java)).thenAnswer { DeviceDataEncrypted() }

        messenger = mock()
        timeService = mock()

        //dependencyProvider = new DependencyProvider();
        dependencyProvider = DependencyProvider.default
        dependencyProvider.register(Messenger::class, messenger)
        dependencyProvider.register(DataService::class, dataService)
        dependencyProvider.register(TimeService::class, timeService)
        dependencyProvider.register(ServerTimeService::class, serverTimeService)
        dependencyProvider.register(EncryptedMedicationMapper::class, EncryptedMedicationMapper(dependencyProvider))
        dependencyProvider.register(EncryptedPrescriptionMapper::class, EncryptedPrescriptionMapper(dependencyProvider))
        dependencyProvider.register(EncryptedMedicationMapper(dependencyProvider))
        dependencyProvider.register(EncryptedPrescriptionMapper(dependencyProvider))
        dependencyProvider.register(EncryptedDeviceMapper(dependencyProvider))

        whenever(timeService.now()).thenAnswer { Instant.now() }

        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)

        //mock retrieval of medication from database
        val mockMedication = Entity.Medication(true, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        mockMedication.primaryKeyId = 1
        val mockMedication2 = Entity.Medication(true, "745752", "DuoResp Spiromax", "Budesonide / Formoterol", MedicationClassification.CONTROLLER, 2, 3, 4, 5, 6, true, changedTime)
        mockMedication2.primaryKeyId = 2

        val mockMedicationQueryInfo = QueryInfo(SearchCriteria("drugUID = %@", mockMedication.drugUID))
        mockMedicationQueryInfo.count = 1

        val mockMedicationQueryInfo2 = QueryInfo(SearchCriteria("drugUID = %@", mockMedication2.drugUID))
        mockMedicationQueryInfo2.count = 1

        whenever(dataService.fetchRequest(eq(MedicationDataEncrypted::class.java), argThat { matches(mockMedicationQueryInfo) })).thenAnswer {
            val arrayList = ArrayList<MedicationDataEncrypted>()
            arrayList.add(mockMedication)
            arrayList
        }

        whenever(dataService.fetchRequest(eq(MedicationDataEncrypted::class.java), argThat { matches(mockMedicationQueryInfo2) })).thenAnswer {
            val arrayList = ArrayList<MedicationDataEncrypted>()
            arrayList.add(mockMedication2)
            arrayList
        }

        val mockmedicationQueryInfo3 = QueryInfo(SearchCriteria("Z_PK = %@", mockMedication.primaryKeyId))
        mockmedicationQueryInfo3.count = 1

        whenever(dataService.fetchRequest(eq(MedicationDataEncrypted::class.java), argThat { matches(mockmedicationQueryInfo3) })).thenAnswer {
            val arrayList = ArrayList<MedicationDataEncrypted>()
            arrayList.add(mockMedication)
            arrayList
        }

        //mock retrieval of device from database
        val mockDevice = Entity.Device(false, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, 1)
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
    }

    @Test
    fun testInsertOneDeviceIntoDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)

        val device = Entity.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, 1)
        val medication = Entity.Medication(true, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        medication.primaryKeyId = 1
        device.medication = medication

        val medicationModel = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        val deviceModel = Model.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, "745750")
        deviceModel.medication = medicationModel

        // create expectations
        val expectedEntities = ArrayList<DeviceDataEncrypted>()
        expectedEntities.add(device)

        val expectedSearchCriteria = ArrayList<SearchCriteria>()
        expectedSearchCriteria.add(SearchCriteria("serialNumber = %@", device.serialNumber))

        // perform operation
        val query = EncryptedDeviceQuery(dependencyProvider)
        query.insert(deviceModel)

        // test expectations

        verify(dataService).save(eq(DeviceDataEncrypted::class.java), argThat { matches(expectedEntities) }, argThat { matches(expectedSearchCriteria) })
    }

    @Test
    fun testInsertMultipleDevicesIntoDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)

        val medication = Entity.Medication(true, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        medication.primaryKeyId = 1
        val medication2 = Entity.Medication(true, "745752", "DuoResp Spiromax", "Budesonide / Formoterol", MedicationClassification.CONTROLLER, 2, 3, 4, 5, 6, true, changedTime)
        medication2.primaryKeyId = 2

        val device = Entity.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, 1)
        val device2 = Entity.Device(true, "", 50, expirationDate, "8.1", "2.1",
                InhalerNameType.WORK, changedTime.minusSeconds(7200), "", 10, "Inhalation Inc.", "home1",
                38, "678909876", "i1n2h3a4l5e6", true, changedTime, 2)
        device.medication = medication
        device2.medication = medication2

        val medicationModel = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        val deviceModel = Model.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, "745750")
        deviceModel.medication = medicationModel
        val medicationModel2 = Model.Medication("745752", "DuoResp Spiromax", "Budesonide / Formoterol", MedicationClassification.CONTROLLER, 2, 3, 4, 5, 6, true, changedTime)
        val deviceModel2 = Model.Device(true, "", 50, expirationDate, "8.1", "2.1",
                InhalerNameType.WORK, changedTime.minusSeconds(7200), "", 10, "Inhalation Inc.", "home1",
                38, "678909876", "i1n2h3a4l5e6", true, changedTime, "745752")
        deviceModel2.medication = medicationModel2

        // create expectations
        val expectedEntities = ArrayList<DeviceDataEncrypted>()
        expectedEntities.add(device)
        expectedEntities.add(device2)

        val expectedSearchCriteria = ArrayList<SearchCriteria>()

        for (d in expectedEntities) {
            expectedSearchCriteria.add(SearchCriteria("serialNumber = %@", d.serialNumber))
        }

        // perform operation
        val query = EncryptedDeviceQuery(dependencyProvider)

        val modelList = ArrayList<Device>()
        modelList.add(deviceModel)
        modelList.add(deviceModel2)

        query.insert(modelList)

        // test expectations
        verify(dataService).save(eq(DeviceDataEncrypted::class.java), argThat { matches(expectedEntities) }, argThat { matches(expectedSearchCriteria) })
    }

    @Test
    fun testDeleteOneDeviceFromDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)

        val medicationModel = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        val deviceModel = Model.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, "745750")
        deviceModel.medication = medicationModel

        // create expectations
        val expectedQueryInfo = QueryInfo(SearchCriteria("serialNumber = %@", deviceModel.serialNumber))

        // perform operation
        val query = EncryptedDeviceQuery(dependencyProvider)
        query.delete(deviceModel)

        // test expectations
        verify(dataService).delete(eq(DeviceDataEncrypted::class.java), argThat { matches(expectedQueryInfo) })
    }


    @Test
    fun testUpdateOneDeviceInDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)

        val device = Entity.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                54, "123454321", "i1n2h3a4l5e6", true, changedTime, 1)
        val medication = Entity.Medication(true, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        medication.primaryKeyId = 1
        device.medication = medication

        val medicationModel = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        val deviceModel = Model.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, "745750")
        deviceModel.medication = medicationModel

        // create expectations
        val expectedEntities = ArrayList<DeviceDataEncrypted>()
        expectedEntities.add(device)

        val searchCriteria = SearchCriteria("serialNumber = %@", device.serialNumber)
        val expectedSearchCriteria = ArrayList<SearchCriteria>()
        expectedSearchCriteria.add(searchCriteria)

        // perform operation
        val query = EncryptedDeviceQuery(dependencyProvider)

        deviceModel.remainingDoseCount = 54
        val updateList = ArrayList<Device>()
        updateList.add(deviceModel)
        query.update(updateList)

        // test expectations
        verify(dataService).save(eq(DeviceDataEncrypted::class.java), argThat { matches(expectedEntities) }, argThat { matches(expectedSearchCriteria) })
    }


    @Test
    fun testQueryDeviceFromDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)
        val device = Entity.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "1234554321", "i1n2h3a4l5e6", true, changedTime, 1)
        val medication = Entity.Medication(true, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        medication.primaryKeyId = 1
        device.medication = medication

        val medicationModel = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        val deviceModel = Model.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "1234554321", "i1n2h3a4l5e6", true, changedTime, "745750")
        deviceModel.medication = medicationModel


        // create expectations
        val expectedQueryInfo = QueryInfo(SearchCriteria("serialNumber = %@", device.serialNumber))
        expectedQueryInfo.count = 1

        // perform operation
        val query = EncryptedDeviceQuery(dependencyProvider)

        whenever(dataService.fetchRequest(eq(DeviceDataEncrypted::class.java), any())).thenAnswer {
            val arrayList = ArrayList<DeviceDataEncrypted>()
            arrayList.add(device)
            arrayList
        }

        val medicationQueryInfo = QueryInfo(SearchCriteria("Z_PK = %@", medication.primaryKeyId))
        medicationQueryInfo.count = 1

        whenever(dataService.fetchRequest(eq(MedicationDataEncrypted::class.java), argThat { matches(medicationQueryInfo) })).thenAnswer {
            val arrayList = ArrayList<MedicationDataEncrypted>()
            arrayList.add(medication)
            arrayList
        }

        val returnedDevice = query.get(device.serialNumber)

        // test expectations
        verify(dataService).fetchRequest(eq(DeviceDataEncrypted::class.java), argThat { matches(expectedQueryInfo) })

        assertThat<Device>(returnedDevice, matchesDevice(deviceModel))
    }

    @Test
    fun testQueryNonexistentDeviceFromDatabase() {

        //initialize test data
        val serialNumber = "987654321"

        // create expectations
        val expectedQueryInfo = QueryInfo(SearchCriteria("serialNumber = %@", serialNumber))
        expectedQueryInfo.count = 1

        // perform operation
        val query = EncryptedDeviceQuery(dependencyProvider)

        whenever(dataService.fetchRequest(eq(DeviceDataEncrypted::class.java), argThat { matches(expectedQueryInfo) })).thenAnswer { ArrayList<DeviceDataEncrypted>() }

        val returnedDevice = query.get(serialNumber)

        // test expectations
        verify(dataService).fetchRequest(eq(DeviceDataEncrypted::class.java), argThat { matches(expectedQueryInfo) })

        assertNull(returnedDevice)
    }

    @Test
    fun testQueryDeviceFromCache() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)
        val device = Entity.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, 1)
        val medication = Entity.Medication(true, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        medication.primaryKeyId = 1
        device.medication = medication

        val medicationModel = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        val deviceModel = Model.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, "745750")
        deviceModel.medication = medicationModel

        // create expectations

        // perform operation
        val query = EncryptedDeviceQuery(dependencyProvider)
        val returnedDevice = query.get(device.serialNumber)

        // test expectations
        verify(dataService).fetchRequest(eq(DeviceDataEncrypted::class.java), isNull())
        assertThat<Device>(returnedDevice, matchesDevice(deviceModel))
    }

    @Test
    fun testVerifyDeviceWithNicknameExistsInDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)
        val device = Entity.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home2",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, 1)

        // create expectations
        val expectedSearchCriteria = SearchCriteria("nickname = %@", device.nickname)

        // perform operation
        val query = EncryptedDeviceQuery(dependencyProvider)

        //mock device retrieval
        whenever(dataService.getCount(eq(DeviceDataEncrypted::class.java), any())).thenReturn(1)

        val result = query.has(device.nickname)

        // test expectations
        verify(dataService).getCount(eq(DeviceDataEncrypted::class.java), argThat { matches(expectedSearchCriteria) })
        assertTrue(result)
    }

    @Test
    fun testVerifyDeviceWithNicknameExistsInCache() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)
        val device = Entity.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, 1)

        //mock device retrieval
        whenever(dataService.fetchRequest(eq(DeviceDataEncrypted::class.java), any())).thenAnswer {
            val arrayList = ArrayList<DeviceDataEncrypted>()
            arrayList.add(device)
            arrayList
        }

        // create expectations
        val expectedSearchCriteria = SearchCriteria("nickname = %@", device.nickname)

        // perform operation
        val query = EncryptedDeviceQuery(dependencyProvider)

        val result = query.has(device.nickname)

        // test expectations
        verify(dataService, never()).getCount(eq(DeviceDataEncrypted::class.java), argThat { matches(expectedSearchCriteria) })
        assertTrue(result)
    }

    @Test
    fun testMarkDeviceAsDeletedInDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)
        val device = Entity.Device(false, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, null, "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, 1)

        val medication = Entity.Medication(true, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        medication.primaryKeyId = 1
        device.medication = medication

        val medicationModel = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        val deviceModel = Model.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, "745750")
        deviceModel.medication = medicationModel

        // create expectations
        val expectedSearchCriteria = ArrayList<SearchCriteria>()
        expectedSearchCriteria.add(SearchCriteria("serialNumber = %@", device.serialNumber))

        val expectedEntities = ArrayList<DeviceDataEncrypted>()
        expectedEntities.add(device)

        // perform operation
        val query = EncryptedDeviceQuery(dependencyProvider)
        query.markAsDeleted(deviceModel)

        //the expected change time should match the actual time at which the device was updated
        //this is a tweak to match them
        device.changedTime = deviceModel.changeTime

        // test expectations
        verify(dataService).save(eq(DeviceDataEncrypted::class.java), argThat { matches(expectedEntities) }, argThat { matches(expectedSearchCriteria) })
    }

    @Test
    fun testMarkDeviceAsNotDeletedInDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)
        val device = Entity.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, 1)

        val medication = Entity.Medication(true, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        medication.primaryKeyId = 1
        device.medication = medication

        val medicationModel = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        val deviceModel = Model.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, "745750")
        deviceModel.medication = medicationModel

        // create expectations
        val expectedSearchCriteria = ArrayList<SearchCriteria>()
        expectedSearchCriteria.add(SearchCriteria("serialNumber = %@", device.serialNumber))

        val expectedEntities = ArrayList<DeviceDataEncrypted>()
        expectedEntities.add(device)

        // perform operation
        val query = EncryptedDeviceQuery(dependencyProvider)
        query.undoMarkAsDeleted(deviceModel)

        //the expected change time should match the actual time at which the device was updated
        //this is a tweak to match them
        device.changedTime = deviceModel.changeTime

        // test expectations
        verify(dataService).save(eq(DeviceDataEncrypted::class.java), argThat { matches(expectedEntities) }, argThat { matches(expectedSearchCriteria) })
    }

    @Test
    fun testGetDeviceCountFromDatabase() {

        // create expectations
        val searchCriteria = SearchCriteria("manufacturerName = %@", "Inhalation Inc.")

        // perform operation
        val query = EncryptedDeviceQuery(dependencyProvider)

        //mock data retrieval
        whenever(dataService.getCount(eq(DeviceDataEncrypted::class.java), any())).thenReturn(1)

        val count = query.getCount(searchCriteria)

        // test expectations
        verify(dataService).getCount(eq(DeviceDataEncrypted::class.java), argThat { matches(searchCriteria) })
        assertEquals(count, 1)
    }

    @Test
    fun testQueryAllActiveDevicesFromDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)

        val device = Entity.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, 1)
        val medication = Entity.Medication(true, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        medication.primaryKeyId = 1
        device.medication = medication

        val medicationModel = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        val deviceModel = Model.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, "745750")
        deviceModel.medication = medicationModel

        // create expectations
        val searchCriteria = SearchCriteria("isActive = %@", true)
        val deviceQueryInfo = QueryInfo(searchCriteria, SortParameter("lastConnection", false))

        val expectedDevices = ArrayList<Device>()
        expectedDevices.add(deviceModel)

        // perform operation
        val query = EncryptedDeviceQuery(dependencyProvider)

        //mock device retrieval
        whenever(dataService.fetchRequest(eq(DeviceDataEncrypted::class.java), argThat { matches(deviceQueryInfo) })).thenAnswer {
            val arrayList = ArrayList<DeviceDataEncrypted>()
            arrayList.add(device)
            arrayList
        }

        val activeDevices = query.getAllActive()

        // test expectations
        verify(dataService).fetchRequest(eq(DeviceDataEncrypted::class.java), argThat { matches(deviceQueryInfo) })
        assertThat(activeDevices, matchesDeviceList(expectedDevices))
    }

    @Test
    fun testVerifyActiveDevicesExistInDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)

        val device = Entity.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, 1)
        val medication = Entity.Medication(true, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        medication.primaryKeyId = 1
        device.medication = medication

        // create expectations
        val searchCriteria = SearchCriteria("isActive = %@", true)

        // perform operation
        val query = EncryptedDeviceQuery(dependencyProvider)

        //mock device retrieval
        whenever(dataService.getCount(eq(DeviceDataEncrypted::class.java), any())).thenReturn(1)

        val activeDevicesExist = query.hasActiveDevices()

        // test expectations
        verify(dataService).getCount(eq(DeviceDataEncrypted::class.java), argThat { matches(searchCriteria) })
        assertTrue(activeDevicesExist)
    }

    @Test
    fun testQueryLastActiveControllerFromDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)

        val device = Entity.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(7200), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, 2)
        val device1 = Entity.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                48, "67899876", "i6n7h8a9l0e1", true, changedTime, 2)
        val medication = Entity.Medication(true, "745752", "DuoResp Spiromax", "Budesonide / Formoterol", MedicationClassification.CONTROLLER, 2, 3, 4, 5, 6, true, changedTime)
        medication.primaryKeyId = 2
        device.medication = medication

        val controllerMedication = Model.Medication("745752", "DuoResp Spiromax", "Budesonide / Formoterol", MedicationClassification.CONTROLLER, 2, 3, 4, 5, 6, true, changedTime)
        val expectedController = Model.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                48, "67899876", "i6n7h8a9l0e1", true, changedTime, "745752")
        expectedController.medication = controllerMedication

        // create expectations
        val searchCriteria = SearchCriteria("isActive = %@", true)
        val deviceQueryInfo = QueryInfo(searchCriteria, SortParameter("lastConnection", false))
        val medicationSearchCriteria = SearchCriteria("Z_PK = %@", medication.primaryKeyId)
        val medicationQueryInfo = QueryInfo(medicationSearchCriteria)

        // perform operation
        val query = EncryptedDeviceQuery(dependencyProvider)

        //mock device retrieval
        whenever(dataService.fetchRequest(eq(DeviceDataEncrypted::class.java), argThat { matches(deviceQueryInfo) })).thenAnswer {
            val arrayList = ArrayList<DeviceDataEncrypted>()
            arrayList.add(device1)
            arrayList.add(device)
            arrayList
        }

        //mock medication retrieval
        whenever(dataService.fetchRequest(eq(MedicationDataEncrypted::class.java), argThat { matches(medicationQueryInfo) })).thenAnswer {
            val arrayList = ArrayList<MedicationDataEncrypted>()
            arrayList.add(medication)
            arrayList
        }

        val lastActiveController = query.lastConnectedActiveController()

        // test expectations
        verify(dataService).fetchRequest(eq(DeviceDataEncrypted::class.java), argThat { matches(deviceQueryInfo) })
        assertThat<Device>(lastActiveController, matchesDevice(expectedController))
    }

    @Test
    fun testQueryLastActiveRelieverFromDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)

        val device = Entity.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(7200), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, 1)
        val device1 = Entity.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                48, "67899876", "i6n7h8a9l0e1", true, changedTime, 1)
        val medication = Entity.Medication(true, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        medication.primaryKeyId = 1
        device.medication = medication

        val relieverMedication = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        val expectedReliever = Model.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                48, "67899876", "i6n7h8a9l0e1", true, changedTime, "745750")
        expectedReliever.medication = relieverMedication

        // create expectations
        val searchCriteria = SearchCriteria("isActive = %@", true)
        val deviceQueryInfo = QueryInfo(searchCriteria, SortParameter("lastConnection", false))
        val medicationSearchCriteria = SearchCriteria("Z_PK = %@", medication.primaryKeyId)
        val medicationQueryInfo = QueryInfo(medicationSearchCriteria)

        // perform operation
        val query = EncryptedDeviceQuery(dependencyProvider)

        //mock device retrieval
        whenever(dataService.fetchRequest(eq(DeviceDataEncrypted::class.java), argThat { matches(deviceQueryInfo) })).thenAnswer {
            val arrayList = ArrayList<DeviceDataEncrypted>()
            arrayList.add(device1)
            arrayList.add(device)
            arrayList
        }

        //mock medication retrieval
        whenever(dataService.fetchRequest(eq(MedicationDataEncrypted::class.java), argThat { matches(medicationQueryInfo) })).thenAnswer {
            val arrayList = ArrayList<MedicationDataEncrypted>()
            arrayList.add(medication)
            arrayList
        }

        val lastActiveReliever = query.lastConnectedActiveReliever()

        // test expectations
        verify(dataService).fetchRequest(eq(DeviceDataEncrypted::class.java), argThat { matches(deviceQueryInfo) })
        assertThat<Device>(lastActiveReliever, matchesDevice(expectedReliever))
    }

    @Test
    fun testQueryFirstDeviceFromDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)

        val device = Entity.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, 1)
        val medication = Entity.Medication(true, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        medication.primaryKeyId = 1
        device.medication = medication

        val medicationModel = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        val deviceModel = Model.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, "745750")
        deviceModel.medication = medicationModel

        // create expectations
        val expectedQueryInfo = QueryInfo(null)
        expectedQueryInfo.count = 1

        // perform operation
        val query = EncryptedDeviceQuery(dependencyProvider)


        whenever(dataService.fetchRequest(eq(DeviceDataEncrypted::class.java), argThat { matches(expectedQueryInfo) })).thenAnswer {
            val arrayList = ArrayList<DeviceDataEncrypted>()
            arrayList.add(device)
            arrayList
        }

        val returnedDevice = query.first

        // test expectations

        verify(dataService).fetchRequest(eq(DeviceDataEncrypted::class.java), argThat { matches(expectedQueryInfo) })
        assertThat<Device>(returnedDevice, matchesDevice(deviceModel))
    }

    @Test
    fun testVerifyIfDevicesExistInDatabase() {

        // create expectations
        whenever(dataService.getCount(eq(DeviceDataEncrypted::class.java), isNull())).thenReturn(1)

        // perform operation
        val query = EncryptedDeviceQuery(dependencyProvider)
        val devicesExist = query.hasData()

        // test expectations
        verify(dataService).getCount(eq(DeviceDataEncrypted::class.java), isNull())
        assertTrue(devicesExist)
    }

    @Test
    fun testInsertOrUpdateDeviceInDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)
        val device = Entity.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "111111111", "i1n2h3a4l5e6", true, changedTime, 1)

        val medication = Entity.Medication(true, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        medication.primaryKeyId = 1
        device.medication = medication

        val medicationModel = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        val deviceModel = Model.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "111111111", "i1n2h3a4l5e6", true, changedTime, "745750")
        deviceModel.medication = medicationModel

        // create expectations
        val expectedEntities = ArrayList<DeviceDataEncrypted>()
        expectedEntities.add(device)

        // perform operation
        val query = EncryptedDeviceQuery(dependencyProvider)

        query.insertOrUpdate(deviceModel, false)

        val searchCriteria = SearchCriteria("serialNumber = %@", device.serialNumber)
        val searchCriteriaList = ArrayList<SearchCriteria>()
        searchCriteriaList.add(searchCriteria)
        // test expectations
        verify(dataService).save(eq(DeviceDataEncrypted::class.java), argThat { matches(expectedEntities) }, argThat { matches(searchCriteriaList) })

    }

    @Test
    fun testQueryAllChangedDevicesInDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 13, 11, 10, 9, 0, GMT_ZONE_ID))
        val expirationDate = LocalDate.of(2017, 12, 1)

        val device = Entity.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, 1)
        val medication = Entity.Medication(true, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        medication.primaryKeyId = 1
        device.medication = medication

        val medicationModel = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        val deviceModel = Model.Device(true, "", 60, expirationDate, "9.1", "2.3",
                InhalerNameType.HOME, changedTime.minusSeconds(3600), "", 10, "Inhalation Inc.", "home1",
                58, "123454321", "i1n2h3a4l5e6", true, changedTime, "745750")
        deviceModel.medication = medicationModel

        //create expectations
        val queryInfo = QueryInfo(SearchCriteria("hasChanged = %@", true))

        whenever(dataService.fetchRequest(eq(DeviceDataEncrypted::class.java), argThat { matches(queryInfo) })).thenAnswer {
            val arrayList = ArrayList<DeviceDataEncrypted>()
            arrayList.add(device)
            arrayList
        }

        val expectedDevices = ArrayList<Device>()
        expectedDevices.add(deviceModel)

        // perform operation
        val query = EncryptedDeviceQuery(dependencyProvider)
        val devices = query.getAllChanged()

        // test expectations
        verify(dataService).fetchRequest(eq(DeviceDataEncrypted::class.java), argThat { matches(queryInfo) })
        assertThat(devices, matchesDeviceList(expectedDevices))
    }

    companion object {
        private val GMT_ZONE_ID = ZoneId.ofOffset("GMT", ZoneOffset.UTC)
    }


}
