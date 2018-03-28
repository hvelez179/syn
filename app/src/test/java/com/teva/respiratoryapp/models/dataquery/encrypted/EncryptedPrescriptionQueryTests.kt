package com.teva.respiratoryapp.models.dataquery.encrypted

import com.nhaarman.mockito_kotlin.*
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.medication.entities.Prescription
import com.teva.medication.enumerations.MedicationClassification
import com.teva.respiratoryapp.services.data.DataService
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.entities.MedicationDataEncrypted
import com.teva.respiratoryapp.services.data.encrypteddata.entities.PrescriptionDataEncrypted
import com.teva.respiratoryapp.testutils.BaseTest
import com.teva.respiratoryapp.testutils.Entity
import com.teva.respiratoryapp.testutils.Model
import com.teva.respiratoryapp.testutils.matches
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import java.util.*

class EncryptedPrescriptionQueryTests : BaseTest() {

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

        whenever(dataService.create(PrescriptionDataEncrypted::class.java)).thenAnswer { PrescriptionDataEncrypted() }

        dependencyProvider.register(Messenger::class, messenger)
        dependencyProvider.register(DataService::class, dataService)
        dependencyProvider.register(TimeService::class, timeService)
        dependencyProvider.register(EncryptedMedicationMapper(dependencyProvider))
        dependencyProvider.register(EncryptedPrescriptionMapper(dependencyProvider))

        whenever(timeService.now()).thenAnswer { Instant.now() }

        //mock medication data and prescription retrieval
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 12, 11, 10, 9, 0, GMT_ZONE_ID))

        val mockMedication = Entity.Medication(true, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        mockMedication.primaryKeyId = 1

        val mockMedicationQueryInfo = QueryInfo(SearchCriteria("drugUID = %@", mockMedication.drugUID))
        mockMedicationQueryInfo.count = 1

        whenever(dataService.fetchRequest(eq(MedicationDataEncrypted::class.java), argThat { matches(mockMedicationQueryInfo) })).thenAnswer {
            val arrayList = ArrayList<MedicationDataEncrypted>()
            arrayList.add(mockMedication)
            arrayList
        }

        whenever(dataService.fetchRequest(eq(MedicationDataEncrypted::class.java), isNull())).thenAnswer {
            val arrayList = ArrayList<MedicationDataEncrypted>()
            arrayList.add(mockMedication)
            arrayList
        }

        val mockMedication2 = Entity.Medication(true, "745752", "DuoResp Spiromax", "Budesonide / Formoterol", MedicationClassification.CONTROLLER, 2, 3, 4, 5, 6, true, changedTime)
        mockMedication2.primaryKeyId = 2

        val mockMedicationQueryInfo2 = QueryInfo(SearchCriteria("drugUID = %@", mockMedication2.drugUID))
        mockMedicationQueryInfo2.count = 1
        whenever(dataService.fetchRequest(eq(MedicationDataEncrypted::class.java), argThat { matches(mockMedicationQueryInfo2) })).thenAnswer {
            val arrayList = ArrayList<MedicationDataEncrypted>()
            arrayList.add(mockMedication2)
            arrayList
        }

        val mockPrescription = Entity.Prescription(true, 1, 2, changedTime, 1, true, changedTime)
        mockPrescription.medication = mockMedication

        val mockPrescriptionQueryInfo = QueryInfo(SearchCriteria("medication.drugUID = %@ AND prescriptionDate = %@", mockMedication.drugUID, changedTime))
        mockPrescriptionQueryInfo.count = 1

        whenever(dataService.fetchRequest(eq(PrescriptionDataEncrypted::class.java), argThat {matches(mockPrescriptionQueryInfo)})).thenAnswer {
            val arrayList = ArrayList<PrescriptionDataEncrypted>()
            arrayList.add(mockPrescription)
            arrayList
        }
    }

    @Test
    fun testInsertOnePrescriptionIntoDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 12, 11, 10, 9, 0, GMT_ZONE_ID))

        val prescription = Entity.Prescription(true, 1, 2, changedTime, 1, true, changedTime)
        val medication = Entity.Medication(true, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        medication.primaryKeyId = 1
        prescription.medication = medication

        val medicationModel = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        val prescriptionModel = Model.Prescription(1, 2, changedTime, medicationModel.drugUID, true, changedTime)
        prescriptionModel.medication = medicationModel

        // create expectations
        val expectedEntities = ArrayList<PrescriptionDataEncrypted>()
        expectedEntities.add(prescription)

        val expectedSearchCriteria = ArrayList<SearchCriteria>()
        expectedSearchCriteria.add(SearchCriteria("medication = %@ AND prescriptionDate = %@", medication.primaryKeyId, changedTime.epochSecond))

        // perform operation
        val query = EncryptedPrescriptionQuery(dependencyProvider)
        query.insert(prescriptionModel)

        // test expectations
        verify(dataService).save(eq(PrescriptionDataEncrypted::class.java), argThat { matches(expectedEntities) }, argThat {matches(expectedSearchCriteria)})
    }

    @Test
    fun testInsertMultiplePrescriptionsIntoDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 12, 11, 10, 9, 0, GMT_ZONE_ID))

        val medication = Entity.Medication(true, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        medication.primaryKeyId = 1
        val medication2 = Entity.Medication(true, "745752", "DuoResp Spiromax", "Budesonide / Formoterol", MedicationClassification.CONTROLLER, 2, 3, 4, 5, 6, true, changedTime)
        medication2.primaryKeyId = 2

        val prescription = Entity.Prescription(true, 1, 2, changedTime, 1, true, changedTime)
        val prescription2 = Entity.Prescription(true, 1, 3, changedTime, 2, true, changedTime)
        prescription.medication = medication
        prescription2.medication = medication2

        val medicationModel = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        val prescriptionModel = Model.Prescription(1, 2, changedTime, medicationModel.drugUID, true, changedTime)
        prescriptionModel.medication = medicationModel
        val medicationModel2 = Model.Medication("745752", "DuoResp Spiromax", "Budesonide / Formoterol", MedicationClassification.CONTROLLER, 2, 3, 4, 5, 6, true, changedTime)
        val prescriptionModel2 = Model.Prescription(1, 3, changedTime, medicationModel2.drugUID, true, changedTime)
        prescriptionModel2.medication = medicationModel2

        val modelList = ArrayList<Prescription>()
        modelList.add(prescriptionModel)
        modelList.add(prescriptionModel2)

        // create expectations
        val expectedEntities = ArrayList<PrescriptionDataEncrypted>()
        expectedEntities.add(prescription)
        expectedEntities.add(prescription2)

        val expectedSearchCriteria = ArrayList<SearchCriteria>()
        expectedSearchCriteria.add(SearchCriteria("medication = %@ AND prescriptionDate = %@", medication.primaryKeyId, changedTime.epochSecond))
        expectedSearchCriteria.add(SearchCriteria("medication = %@ AND prescriptionDate = %@", medication2.primaryKeyId, changedTime.epochSecond))

        // perform operation
        val query = EncryptedPrescriptionQuery(dependencyProvider)
        query.insert(modelList)

        // test expectations
        verify(dataService).save(eq(PrescriptionDataEncrypted::class.java), argThat {matches(expectedEntities)}, argThat{matches(expectedSearchCriteria)})
    }

    @Test
    fun testDeleteOnePrescriptionFromDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 12, 11, 10, 9, 0, GMT_ZONE_ID))

        val prescription = Entity.Prescription(true, 1, 2, changedTime, 1, true, changedTime)
        val medication = Entity.Medication(true, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        medication.primaryKeyId = 1
        prescription.medication = medication

        val medicationModel = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        val prescriptionModel = Model.Prescription(1, 2, changedTime, medicationModel.drugUID, true, changedTime)
        prescriptionModel.medication = medicationModel

        // create expectations
        val expectedEntities = ArrayList<PrescriptionDataEncrypted>()
        expectedEntities.add(prescription)

        val expectedQueryInfo = QueryInfo(SearchCriteria("medication = %@ AND prescriptionDate = %@", medication.primaryKeyId, changedTime.epochSecond))

        // perform operation
        val query = EncryptedPrescriptionQuery(dependencyProvider)
        query.delete(prescriptionModel)

        // test expectations
        verify(dataService).delete(eq(PrescriptionDataEncrypted::class.java), argThat {matches(expectedQueryInfo)})
    }

    @Test
    fun testUpdateOnePrescriptionInDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 12, 11, 10, 9, 0, GMT_ZONE_ID))

        val prescription = Entity.Prescription(true, 2, 2, changedTime, 1, true, changedTime)
        val medication = Entity.Medication(true, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        medication.primaryKeyId = 1
        prescription.medication = medication

        val medicationModel = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        val prescriptionModel = Model.Prescription(1, 2, changedTime, medicationModel.drugUID, true, changedTime)
        prescriptionModel.medication = medicationModel

        prescriptionModel.inhalesPerDose = 2
        val updateList = ArrayList<Prescription>()
        updateList.add(prescriptionModel)

        whenever(dataService.fetchRequest(eq(PrescriptionDataEncrypted::class.java), any())).thenReturn(listOf(prescription))

        // create expectations
        val expectedEntities = ArrayList<PrescriptionDataEncrypted>()
        expectedEntities.add(prescription)

        val searchCriteria = SearchCriteria("medication = %@ AND prescriptionDate = %@", medication.primaryKeyId, changedTime.epochSecond)
        val expectedSearchCriteria = ArrayList<SearchCriteria>()
        expectedSearchCriteria.add(searchCriteria)

        // perform operation
        val query = EncryptedPrescriptionQuery(dependencyProvider)
        query.update(updateList)

        // test expectations
        verify(dataService).save(eq(PrescriptionDataEncrypted::class.java), argThat {matches(expectedEntities)}, argThat{matches(expectedSearchCriteria)})
    }

    @Test
    fun testQueryPrescriptionFromDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 12, 11, 10, 9, 0, GMT_ZONE_ID))
        val medicationModel = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        val prescriptionModel = Model.Prescription(1, 2, changedTime, medicationModel.drugUID, true, changedTime)
        prescriptionModel.medication = medicationModel
        val expectedPrescriptions = ArrayList<Prescription>()
        expectedPrescriptions.add(prescriptionModel)

        // create expectations
        val expectedQueryInfo = QueryInfo(SearchCriteria("medication.drugUID = %@ AND prescriptionDate = %@", "745750", changedTime))
        expectedQueryInfo.count = 1

        // perform operation
        val query = EncryptedPrescriptionQuery(dependencyProvider)
        val prescriptions = query.readBasedOnQuery(expectedQueryInfo)

        // test expectations
        verify(dataService).fetchRequest(eq(PrescriptionDataEncrypted::class.java), argThat{matches(expectedQueryInfo)})
        assertTrue(prescriptions.matches(expectedPrescriptions))
    }

    @Test
    fun testGetPrescriptionCountFromDatabase() {

        // create expectations
        val searchCriteria = SearchCriteria("medication = %@", "1")

        // perform operation
        val query = EncryptedPrescriptionQuery(dependencyProvider)

        //mock device retrieval
        whenever(dataService.getCount(eq(PrescriptionDataEncrypted::class.java), any())).thenReturn(1)

        val count = query.getCount(searchCriteria)

        // test expectations
        verify(dataService).getCount(eq(PrescriptionDataEncrypted::class.java), argThat{matches(searchCriteria)})
        assertEquals(count.toLong(), 1)
    }

    @Test
    fun testQueryFirstPrescriptionFromDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 12, 11, 10, 9, 0, GMT_ZONE_ID))
        val medicationModel = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        val prescriptionModel = Model.Prescription(1, 2, changedTime, medicationModel.drugUID, true, changedTime)
        prescriptionModel.medication = medicationModel

        val mockMedication = Entity.Medication(true, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        mockMedication.primaryKeyId = 1
        val mockPrescription = Entity.Prescription(true, 1, 2, changedTime, 1, true, changedTime)
        mockPrescription.medication = mockMedication

        // create expectations
        val expectedQueryInfo = QueryInfo(null)
        expectedQueryInfo.count = 1

        // perform operation
        val query = EncryptedPrescriptionQuery(dependencyProvider)

        whenever(dataService.fetchRequest(eq(PrescriptionDataEncrypted::class.java), any())).thenAnswer {
            val arrayList = ArrayList<PrescriptionDataEncrypted>()
            arrayList.add(mockPrescription)
            arrayList
        }

        val prescription = query.first

        // test expectations
        verify(dataService).fetchRequest(eq(PrescriptionDataEncrypted::class.java), argThat{matches(expectedQueryInfo)})
        assertTrue(prescription!!.matches(prescriptionModel))
    }

    @Test
    fun testVerifyIfPrescriptionsExistInDatabase() {

        // create expectations
        val expectedQueryInfo = QueryInfo(null)
        expectedQueryInfo.count = 1

        // perform operation
        val query = EncryptedPrescriptionQuery(dependencyProvider)

        //mock device retrieval
        whenever(dataService.getCount(eq(PrescriptionDataEncrypted::class.java), isNull())).thenReturn(1)

        val prescriptionsExist = query.hasData()

        // test expectations
        verify(dataService).getCount(eq(PrescriptionDataEncrypted::class.java), isNull())
        assertTrue(prescriptionsExist)
    }

    @Test
    fun testInsertOrUpdatePrescriptionInDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 12, 11, 10, 9, 0, GMT_ZONE_ID))

        val expectedPrescription = Entity.Prescription(true, 1, 2, changedTime, 1, true, changedTime)
        val medication = Entity.Medication(true, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        medication.primaryKeyId = 1
        expectedPrescription.medication = medication

        val medicationModel = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        val prescriptionModel = Model.Prescription(1, 2, changedTime, medicationModel.drugUID, true, changedTime)
        prescriptionModel.medication = medicationModel

        // create expectations
        val expectedEntities = ArrayList<PrescriptionDataEncrypted>()
        expectedEntities.add(expectedPrescription)

        // perform operation
        val query = EncryptedPrescriptionQuery(dependencyProvider)

        query.insertOrUpdate(prescriptionModel, false)

        val searchCriteria = SearchCriteria("medication = %@ AND prescriptionDate = %@", medication.primaryKeyId, prescriptionModel.prescriptionDate?.epochSecond)
        val searchCriteriaList = ArrayList<SearchCriteria>()
        searchCriteriaList.add(searchCriteria)

        // test expectations
        verify(dataService).save(eq(PrescriptionDataEncrypted::class.java), argThat{matches(expectedEntities)}, argThat{matches(searchCriteriaList)})

    }

    @Test
    fun testFindPrescriptionInDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 12, 11, 10, 9, 0, GMT_ZONE_ID))

        val medicationModel = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        val prescriptionModel = Model.Prescription(1, 2, changedTime, medicationModel.drugUID, true, changedTime)
        prescriptionModel.medication = medicationModel

        val medicationEntity = Entity.Medication(false, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        medicationEntity.primaryKeyId = 1

        // perform operation
        val query = EncryptedPrescriptionQuery(dependencyProvider)

        //mock device retrieval
        whenever(dataService.fetchRequest(eq(MedicationDataEncrypted::class.java), any())).thenReturn(listOf(medicationEntity))
        whenever(dataService.getCount(eq(PrescriptionDataEncrypted::class.java), any())).thenReturn(1)

        val prescriptionFound = query.has(prescriptionModel)

        val searchCriteria = SearchCriteria("medication = %@ AND prescriptionDate = %@", medicationEntity.primaryKeyId, prescriptionModel.prescriptionDate?.epochSecond)

        // test expectations
        verify(dataService).getCount(eq(PrescriptionDataEncrypted::class.java), argThat{matches(searchCriteria)})
        assertTrue(prescriptionFound)
    }

    companion object {

        private val GMT_ZONE_ID = ZoneId.ofOffset("GMT", ZoneOffset.UTC)
    }
}
