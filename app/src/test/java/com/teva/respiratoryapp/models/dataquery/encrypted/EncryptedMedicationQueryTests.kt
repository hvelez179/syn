package com.teva.respiratoryapp.models.dataquery.encrypted

import android.content.res.AssetManager
import com.nhaarman.mockito_kotlin.*
import com.teva.common.services.ServerTimeService
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.medication.dataquery.PrescriptionDataQuery
import com.teva.medication.entities.Medication
import com.teva.medication.entities.Prescription
import com.teva.medication.enumerations.MedicationClassification
import com.teva.respiratoryapp.services.data.DataService
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.SortParameter
import com.teva.respiratoryapp.services.data.encrypteddata.entities.MedicationDataEncrypted
import com.teva.respiratoryapp.services.data.encrypteddata.entities.PrescriptionDataEncrypted
import com.teva.respiratoryapp.testutils.*

import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

import java.util.ArrayList

import com.teva.respiratoryapp.testutils.ModelMatcher.matchesMedication
import com.teva.respiratoryapp.testutils.ModelMatcher.matchesMedicationList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import java.io.ByteArrayInputStream

class EncryptedMedicationQueryTests : BaseTest() {

    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var dataService: DataService
    private lateinit var messenger: Messenger
    private lateinit var timeService: TimeService
    private lateinit var serverTimeService: ServerTimeService
    private val medicationSkuString: String = "[\n" +
            "  {\"productId\": \"\", \"drugUID\": \"745750\", \"initialDoseCount\":200, \"expirationPeriodInMonths\":6},\n" +
            "  {\"productId\": \"AAA030\", \"drugUID\": \"745750\", \"initialDoseCount\":30, \"expirationPeriodInMonths\":6}\n" +
            "]"
    private lateinit var assetmanager: AssetManager

    @Before
    fun setup() {
        dependencyProvider = DependencyProvider.default
        dataService = mock()
        messenger = mock()
        timeService = mock()
        serverTimeService = mock()

        assetmanager = mock()
        whenever(assetmanager.open(eq("medicationsku.json"))).thenReturn(ByteArrayInputStream( medicationSkuString.toByteArray() ))
        dependencyProvider.register(AssetManager::class, assetmanager)

        whenever(dataService.shouldReloadInitialData).thenReturn(false)
        whenever(dataService.create(MedicationDataEncrypted::class.java)).thenAnswer { MedicationDataEncrypted() }
        whenever(dataService.create(PrescriptionDataEncrypted::class.java)).thenAnswer { PrescriptionDataEncrypted() }

        //dependencyProvider = new DependencyProvider();
        dependencyProvider.register(Messenger::class, messenger)
        dependencyProvider.register(DataService::class, dataService)
        dependencyProvider.register(TimeService::class, timeService)
        dependencyProvider.register(ServerTimeService::class, serverTimeService)
        dependencyProvider.register(EncryptedPrescriptionMapper(dependencyProvider))
        dependencyProvider.register(EncryptedMedicationMapper(dependencyProvider))
        dependencyProvider.register(PrescriptionDataQuery::class, EncryptedPrescriptionQuery(dependencyProvider))

        whenever(timeService.now()).thenAnswer { Instant.now() }

        //mock medication retrieval
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 8, 11, 10, 9, 0, GMT_ZONE_ID))

        val mockMedication = Entity.Medication(false, "745750", "ProAir", "HFA", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        val mockMedication2 = Entity.Medication(true, "745752", "DuoResp Spiromax", "Budesonide / Formoterol", MedicationClassification.CONTROLLER, 2, 3, 4, 5, 6, true, changedTime)
        mockMedication.primaryKeyId = 1
        mockMedication2.primaryKeyId = 2
        whenever(dataService.fetchRequest(eq(MedicationDataEncrypted::class.java), isNull())).thenAnswer {
            val arrayList = ArrayList<MedicationDataEncrypted>()
            arrayList.add(mockMedication)
            arrayList.add(mockMedication2)
            arrayList
        }

        whenever(dataService.fetchRequest(eq(MedicationDataEncrypted::class.java), any())).thenAnswer {
            val arrayList = ArrayList<MedicationDataEncrypted>()
            arrayList.add(mockMedication)
            arrayList.add(mockMedication2)
            arrayList
        }

        val mockPrescription = Entity.Prescription(true, 1, 2, changedTime, 1, true, changedTime)
        mockPrescription.medication = mockMedication
        val mockPrescription2 = Entity.Prescription(true, 2, 2, changedTime, 2, true, changedTime)
        mockPrescription.medication = mockMedication2

        whenever(dataService.fetchRequest(eq(PrescriptionDataEncrypted::class.java), isNull())).thenAnswer {
            val arrayList = ArrayList<PrescriptionDataEncrypted>()
            arrayList.add(mockPrescription)
            arrayList.add(mockPrescription2)
            arrayList
        }
    }

    @Test
    fun testInsertOneMedicationIntoDatabase() {
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 8, 11, 10, 9, 0, GMT_ZONE_ID))

        // create expectations
        val expectedEntities = ArrayList<MedicationDataEncrypted>()
        expectedEntities.add(Entity.Medication(true, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime))

        val expectedSearchCriteria = ArrayList<SearchCriteria>()
        expectedSearchCriteria.add(SearchCriteria("drugUID = %@", expectedEntities[0].drugUID))

        // perform operation
        val query = EncryptedMedicationQuery(dependencyProvider)

        val medication = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)

        whenever(assetmanager.open(eq("medicationsku.json"))).thenReturn(ByteArrayInputStream( medicationSkuString.toByteArray() ))
        dependencyProvider.register(AssetManager::class, assetmanager)

        query.insert(medication)

        // test expectations

        verify(dataService).save(eq(MedicationDataEncrypted::class.java),
                argThat{ matches(expectedEntities) },
                argThat { matches(expectedSearchCriteria) })
    }

    @Test
    fun testInsertSeveralMedicationsIntoDatabase() {
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 8, 11, 10, 9, 0, GMT_ZONE_ID))

        // create expectations
        val expectedEntities = ArrayList<MedicationDataEncrypted>()
        expectedEntities.add(Entity.Medication(true, "745750", "ProAir", "Albuterol", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime))
        expectedEntities.add(Entity.Medication(true, "745752", "DuoResp Spiromax", "Budesonide / Formoterol", MedicationClassification.CONTROLLER, 2, 3, 4, 5, 6, true, changedTime))

        val expectedSearchCriteria = ArrayList<SearchCriteria>()
        for (entity in expectedEntities) {
            expectedSearchCriteria.add(SearchCriteria("drugUID = %@", entity.drugUID))
        }

        // perform operation
        val query = EncryptedMedicationQuery(dependencyProvider)

        val modelList = ArrayList<Medication>()
        modelList.add(Model.Medication("745750", "ProAir", "Albuterol", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime))
        modelList.add(Model.Medication("745752", "DuoResp Spiromax", "Budesonide / Formoterol", MedicationClassification.CONTROLLER, 2, 3, 4, 5, 6, true, changedTime))

        whenever(assetmanager.open(eq("medicationsku.json"))).thenReturn(ByteArrayInputStream( medicationSkuString.toByteArray() ))
        dependencyProvider.register(AssetManager::class, assetmanager)

        query.insert(modelList, true)

        // test expectations
        expectedEntities[0].changedTime = modelList[0].changeTime
        expectedEntities[1].changedTime = modelList[1].changeTime

        verify(dataService).save(eq(MedicationDataEncrypted::class.java), argThat{matches(expectedEntities)}, argThat{matches(expectedSearchCriteria)})
    }

    @Test
    fun testDeleteOneMedicationFromDatabase() {

        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 9, 11, 10, 9, 0, GMT_ZONE_ID))

        // create expectations
        val expectedQueryInfo = QueryInfo(SearchCriteria("drugUID = %@", "745750"))

        // perform operation
        val query = EncryptedMedicationQuery(dependencyProvider)

        val medication = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)

        whenever(assetmanager.open(eq("medicationsku.json"))).thenReturn(ByteArrayInputStream( medicationSkuString.toByteArray() ))
        dependencyProvider.register(AssetManager::class, assetmanager)

        query.delete(medication)

        // test expectations

        verify(dataService).delete(eq(MedicationDataEncrypted::class.java), argThat{matches(expectedQueryInfo)})
    }

    @Test
    fun testUpdateOneMedicationInDatabase() {

        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 9, 11, 10, 9, 0, GMT_ZONE_ID))

        // create expectations
        val expectedEntities = ArrayList<MedicationDataEncrypted>()
        expectedEntities.add(Entity.Medication(false, "745750", "ProAir", "HFA", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime))

        val expectedSearchCriteria = ArrayList<SearchCriteria>()
        for (entity in expectedEntities) {
            expectedSearchCriteria.add(SearchCriteria("drugUID = %@", entity.drugUID))
        }

        val expectedQueryInfo = QueryInfo(SearchCriteria("drugUID = %@", "745750"))
        expectedQueryInfo.count = 1

        whenever(dataService.fetchRequest(eq(MedicationDataEncrypted::class.java), argThat{matches(expectedQueryInfo)})).thenAnswer {
            val arrayList = ArrayList<MedicationDataEncrypted>()
            arrayList.add(MedicationDataEncrypted())
            arrayList
        }

        // perform operation
        val query = EncryptedMedicationQuery(dependencyProvider)

        val updateList = ArrayList<Medication>()
        updateList.add(Model.Medication("745750", "ProAir", "HFA", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime))

        whenever(assetmanager.open(eq("medicationsku.json"))).thenReturn(ByteArrayInputStream( medicationSkuString.toByteArray() ))
        dependencyProvider.register(AssetManager::class, assetmanager)

        query.update(updateList, true)

        // test expectations
        expectedEntities[0].changedTime = updateList[0].changeTime

        verify(dataService).save(eq(MedicationDataEncrypted::class.java), argThat{matches(expectedEntities)}, argThat{matches(expectedSearchCriteria)})
    }

    @Test
    fun testQueryMedicationUsingDrugUIDFromDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 8, 11, 10, 9, 0, GMT_ZONE_ID))
        val expectedMedication = Model.Medication("745750", "ProAir", "HFA", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        expectedMedication.prescriptions = ArrayList<Prescription>()

        // create expectations
        val sortParameter = SortParameter("genericName", true)

        val queryInfo = QueryInfo(SearchCriteria("drugUID = %@", "745750"), sortParameter)
        queryInfo.count = 1


        // perform operation
        val query = EncryptedMedicationQuery(dependencyProvider)
        val returnedMedication = query["745750"]

        // test expectations
        verify(dataService, never()).fetchRequest(eq(MedicationDataEncrypted::class.java), argThat{matches(queryInfo)})
        assertThat<Medication>(returnedMedication, matchesMedication(expectedMedication))
    }

    @Test
    fun testQueryMedicationsBasedOnClassFromDatabase() {

        //initialize  test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 8, 11, 10, 9, 0, GMT_ZONE_ID))
        val expectedMedication = Model.Medication("745750", "ProAir", "HFA", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        expectedMedication.prescriptions = ArrayList<Prescription>()

        val expectedMedications = ArrayList<Medication>()
        expectedMedications.add(expectedMedication)

        // create expectations
        val sortParameter = SortParameter("genericName", true)

        val queryInfo = QueryInfo(SearchCriteria("medicationClassification = %@", MedicationClassification.RELIEVER), sortParameter)
        queryInfo.count = 1


        // perform operation
        val query = EncryptedMedicationQuery(dependencyProvider)
        val returnedMedications = query[MedicationClassification.RELIEVER]

        // test expectations
        verify(dataService, never()).fetchRequest(eq(MedicationDataEncrypted::class.java), argThat{matches(queryInfo)})
        assertThat(returnedMedications, matchesMedicationList(expectedMedications))
    }

    @Test
    fun testQueryFirstMedicationBasedOnClassFromDatabase() {

        //initialize  test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 8, 11, 10, 9, 0, GMT_ZONE_ID))
        val expectedMedication = Model.Medication("745750", "ProAir", "HFA", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        expectedMedication.prescriptions = ArrayList<Prescription>()

        // create expectations
        val sortParameter = SortParameter("genericName", true)

        val queryInfo = QueryInfo(SearchCriteria("medicationClassification = %@", MedicationClassification.RELIEVER), sortParameter)
        queryInfo.count = 1


        // perform operation
        val query = EncryptedMedicationQuery(dependencyProvider)
        val returnedMedication = query.getFirst(MedicationClassification.RELIEVER)

        // test expectations
        verify(dataService, never()).fetchRequest(eq(MedicationDataEncrypted::class.java), argThat{matches(queryInfo)})
        assertThat<Medication>(returnedMedication, matchesMedication(expectedMedication))
    }


    @Test
    fun testQueryUpdatePrescriptionInDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 8, 11, 10, 9, 0, GMT_ZONE_ID))

        val medicationModel = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        val prescriptionModel = Model.Prescription(1, 2, changedTime, medicationModel.drugUID, true, changedTime)
        prescriptionModel.medication = medicationModel
        val expectedPrescription = Entity.Prescription(true, 1, 2, changedTime, 1, true, changedTime)
        val expectedMedication = Entity.Medication(true, "745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        expectedMedication.primaryKeyId = 1
        expectedPrescription.medication = expectedMedication

        // create expectations
        val searchCriteria = SearchCriteria("medication = %@ AND prescriptionDate = %@", expectedMedication.primaryKeyId, prescriptionModel.prescriptionDate?.epochSecond)
        val searchCriteriaList = ArrayList<SearchCriteria>()
        searchCriteriaList.add(searchCriteria)


        // perform operation
        val query = EncryptedMedicationQuery(dependencyProvider)
        whenever(assetmanager.open(eq("medicationsku.json"))).thenReturn(ByteArrayInputStream( medicationSkuString.toByteArray() ))
        dependencyProvider.register(AssetManager::class, assetmanager)
        query.update(prescriptionModel)

        expectedPrescription.changedTime = prescriptionModel.changeTime
        val expectedEntities = ArrayList<PrescriptionDataEncrypted>()
        expectedEntities.add(expectedPrescription)

        // test expectations
        verify(dataService).save(eq(PrescriptionDataEncrypted::class.java), argThat{matches(expectedEntities)}, argThat{matches(searchCriteriaList)})
    }

    @Test
    fun testGetMedicationCountFromDatabase() {

        // create expectations
        val searchCriteria = SearchCriteria("brandName = %@", "ProAir")

        // perform operation
        val query = EncryptedMedicationQuery(dependencyProvider)

        //mock device retrieval
        whenever(dataService.getCount(eq(MedicationDataEncrypted::class.java), any())).thenReturn(1)

        val count = query.getCount(searchCriteria)

        // test expectations
        verify(dataService).getCount(eq(MedicationDataEncrypted::class.java), argThat{matches(searchCriteria)})
        assertEquals(count.toLong(), 1)
    }

    @Test
    fun testQueryEarliestPrescriptionDateInDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 8, 11, 10, 9, 0, GMT_ZONE_ID))

        val prescription = Entity.Prescription(true, 1, 2, changedTime, 1, true, changedTime)

        val medicationModel = Model.Medication("745750", "ProAir", "GenericName", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        val prescriptionModel = Model.Prescription(1, 2, changedTime, medicationModel.drugUID, true, changedTime)
        prescriptionModel.medication = medicationModel

        // create expectations
        val sortParameter = SortParameter("prescriptionDate", true)

        val queryInfo = QueryInfo(null, sortParameter)
        queryInfo.count = 1

        // perform operation
        val query = EncryptedMedicationQuery(dependencyProvider)

        whenever(dataService.fetchRequest(eq(PrescriptionDataEncrypted::class.java), argThat{matches(queryInfo)})).thenAnswer {
            val arrayList = ArrayList<PrescriptionDataEncrypted>()
            arrayList.add(prescription)
            arrayList
        }

        val firstPrescription = query.earliestPrescriptionDate

        // test expectations
        verify(dataService).fetchRequest(eq(PrescriptionDataEncrypted::class.java), argThat{matches(queryInfo)})
        assertEquals(firstPrescription, changedTime)
    }

    @Test
    fun testVerifyIfMedicationsOfSpecifiedClassExistInDatabase() {

        //initialize test data
        val medicationClassification = MedicationClassification.CONTROLLER

        // create expectations
        val queryInfo = QueryInfo(SearchCriteria("medicationClassification = %@", medicationClassification))

        // perform operation
        val query = EncryptedMedicationQuery(dependencyProvider)

        val medicationClassificationDataExists = query.hasData(MedicationClassification.CONTROLLER)

        // test expectations
        assertEquals(medicationClassificationDataExists, true)
        verify(dataService, never()).fetchRequest(eq(MedicationDataEncrypted::class.java), argThat{matches(queryInfo)})
    }

    @Test
    fun testQueryFirstMedicationFromDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 8, 11, 10, 9, 0, GMT_ZONE_ID))
        val expectedMedication = Model.Medication("745750", "ProAir", "HFA", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)
        expectedMedication.prescriptions = ArrayList<Prescription>()

        // create expectations
        val expectedQueryInfo = QueryInfo(null)
        expectedQueryInfo.count = 1

        // perform operation
        val query = EncryptedMedicationQuery(dependencyProvider)
        val returnedMedication = query.first

        // test expectations
        verify(dataService, atLeastOnce()).fetchRequest(eq(MedicationDataEncrypted::class.java), argThat{matches(expectedQueryInfo)})
        assertThat<Medication>(returnedMedication, matchesMedication(expectedMedication))
    }

    @Test
    fun testVerifyIfMedicationsExistInDatabase() {

        // create expectations

        // perform operation
        val query = EncryptedMedicationQuery(dependencyProvider)

        //mock device retrieval
        whenever(dataService.getCount(eq(MedicationDataEncrypted::class.java), isNull())).thenReturn(1)

        val medicationsExist = query.hasData()

        // test expectations
        verify(dataService).getCount(eq(MedicationDataEncrypted::class.java), isNull())
        assertTrue(medicationsExist)
    }

    @Test
    fun testVerifyResetChangeFlagUpdatesMedicationInDatabase() {

        //initialize test data
        val changedTime = Instant.from(ZonedDateTime.of(2016, 12, 12, 11, 10, 9, 0, GMT_ZONE_ID))

        val expectedEntities = ArrayList<MedicationDataEncrypted>()
        expectedEntities.add(Entity.Medication(true, "745750", "ProAir", "Albuterol", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime))

        val expectedSearchCriteria = ArrayList<SearchCriteria>()
        for (entity in expectedEntities) {
            expectedSearchCriteria.add(SearchCriteria("drugUID = %@", entity.drugUID))
        }

        val medication = Model.Medication("745750", "ProAir", "Albuterol", MedicationClassification.RELIEVER, 1, 2, 3, 4, 5, true, changedTime)

        // perform operation
        val query = EncryptedMedicationQuery(dependencyProvider)

        whenever(assetmanager.open(eq("medicationsku.json"))).thenReturn(ByteArrayInputStream( medicationSkuString.toByteArray() ))
        dependencyProvider.register(AssetManager::class, assetmanager)

        query.resetChangedFlag(medication, true)

        val searchCriteria = SearchCriteria("drugUID = %@", medication.drugUID)
        val searchCriteriaList = ArrayList<SearchCriteria>()
        searchCriteriaList.add(searchCriteria)
        // test expectations
        verify(dataService).save(eq(MedicationDataEncrypted::class.java), argThat{matches(expectedEntities)}, argThat{matches(searchCriteriaList)})

    }

    companion object {
        private val GMT_ZONE_ID = ZoneId.ofOffset("GMT", ZoneOffset.UTC)
    }

}
