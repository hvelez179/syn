//
// DHPSyncCloudServiceTests.kt
// teva_cloud
//
// Copyright © 2018 Teva. All rights reserved.
//

package com.teva.cloud.services.sync

import android.content.SharedPreferences
import com.nhaarman.mockito_kotlin.*
import com.teva.cloud.dataquery.UserAccountQuery
import com.teva.cloud.models.CloudSessionState
import com.teva.cloud.models.userprofile.UserProfileManager
import com.teva.cloud.services.CloudObjectContainer
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.devices.entities.Device
import com.teva.devices.entities.InhaleEvent
import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.Endpoints.DHPDataSynch
import com.teva.dhp.DataEntities.DHPDataTypes.Endpoints.DHPGetMedicalDevices
import com.teva.dhp.DataEntities.DHPDataTypes.Endpoints.DHPGetPatientPrescriptions
import com.teva.dhp.DataEntities.DHPDataTypes.Endpoints.DHPRetrieval
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects.*
import com.teva.dhp.DataEntities.DHPDataTypes.GenericDHPRequest
import com.teva.dhp.DataEntities.DHPDataTypes.GenericDHPResponse
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPGetPatientPrescriptionsResponseBody
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPResponseBody
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPRetrievalResponseBody
import com.teva.dhp.models.DHPAPIs
import com.teva.dhp.models.DHPManager
import com.teva.medication.dataquery.MedicationDataQuery
import com.teva.medication.entities.Medication
import com.teva.medication.entities.Prescription
import com.teva.medication.enumerations.MedicationClassification
import com.teva.notifications.entities.ReminderSetting
import com.teva.notifications.enumerations.RepeatType
import com.teva.userfeedback.entities.DailyUserFeeling
import com.teva.userfeedback.enumerations.UserFeeling
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.LocalTime


/**
 * This class contains unit tests for the DHPSyncCloudService class.
 */
class DHPSyncCloudServiceTests {
    private var dependencyProvider: DependencyProvider = DependencyProvider.default
    private val dhpManager: DHPManager = mock()
    private val userAccountQuery: UserAccountQuery = mock()
    private val timeService: TimeService = mock()
    private val userProfileManager: UserProfileManager = mock()
    private val sharedPreferences: SharedPreferences = mock()
    private val sharedPreferencesEditor: SharedPreferences.Editor = mock()
    private val medicationDataQuery: MedicationDataQuery = mock()

    @Before
    @Throws(Exception::class)
    fun setup() {
        whenever(dhpManager.maxUploadObjects).thenReturn(100)
        dependencyProvider.register(DHPManager::class, dhpManager)
        dependencyProvider.register(UserAccountQuery::class, userAccountQuery)
        whenever(timeService.now()).thenReturn(Instant.ofEpochMilli(1516025834194))
        dependencyProvider.register(TimeService::class, timeService)
        dependencyProvider.register(UserProfileManager::class, userProfileManager)
        whenever(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor)
        dependencyProvider.register(SharedPreferences::class, sharedPreferences)
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        DependencyProvider.default.unregisterAll()
    }

    @Test
    fun testThatUploadAsyncInvokesTheCallbackIfThereIsNoDataToUpload() {
        // arrange
        val uploadData = CloudObjectContainer()
        CloudSessionState.shared.activeProfileID = "FED1234"
        val callback: (success: Boolean) -> Unit = mock()

        // act
        val syncService = DHPSyncCloudService()
        syncService.didUpload = callback
        syncService.uploadAsync(uploadData)

        // assert
        verify(callback)(true)
    }

    @Test
    fun testThatUploadAsyncSendsRequestToTheDHPManagerWithTheCorrectPayload() {
        // arrange
        val uploadData = createUploadData()
        CloudSessionState.shared.activeProfileID = "FED1234"

        // act
        val syncService = DHPSyncCloudService()
        syncService.uploadAsync(uploadData)
        val genericRequestArgumentCaptor = argumentCaptor<GenericDHPRequest<*>>()

        // assert
        verify(dhpManager).executeAsync(genericRequestArgumentCaptor.capture(), isNull())
        val dhpRequest = genericRequestArgumentCaptor.firstValue
        assertEquals(DHPDataSynch::class.java, dhpRequest.payload.javaClass)
        assertEquals(uploadData.objectCount(), (dhpRequest.payload as DHPDataSynch).objects.count())
        assertEquals(DHPAPIs.syncDataUpload, dhpRequest.uri)
    }

    @Test
    fun testThatUploadCompletedCreatesAnotherUploadRequestIfThereIsMoreDataToUpload() {
        // arrange
        val maxUploadObjectCount = 3
        val uploadData = createUploadData()
        CloudSessionState.shared.activeProfileID = "FED1234"
        whenever(dhpManager.maxUploadObjects).thenReturn(maxUploadObjectCount)
        dependencyProvider.register(DHPManager::class, dhpManager)

        // act
        val syncService = DHPSyncCloudService()
        syncService.uploadAsync(uploadData)
        val uploadCompletedMethod = syncService.javaClass.getDeclaredMethod("uploadToDHPCompleted", GenericDHPResponse::class.java)
        uploadCompletedMethod.isAccessible = true
        val genericDHPResponse: GenericDHPResponse<DHPResponseBody> = mock()
        whenever(genericDHPResponse.success).thenReturn(true)
        uploadCompletedMethod.invoke(syncService, genericDHPResponse)
        val genericRequestArgumentCaptor = argumentCaptor<GenericDHPRequest<*>>()

        // assert
        verify(dhpManager, times(2)).executeAsync(genericRequestArgumentCaptor.capture(), isNull())
        val dhpRequest = genericRequestArgumentCaptor.firstValue
        assertEquals(maxUploadObjectCount, (dhpRequest.payload as DHPDataSynch).objects.count())

        val dhpRequest2 = genericRequestArgumentCaptor.secondValue
        assertEquals(uploadData.objectCount() - maxUploadObjectCount, (dhpRequest2.payload as DHPDataSynch).objects.count())

    }

    @Test
    fun testThatUploadCompletedInvokesCallbackIfThereIsNoMoreDataToUpload() {
        // arrange
        val uploadData = createUploadData()
        CloudSessionState.shared.activeProfileID = "FED1234"
        val callback: (success: Boolean) -> Unit = mock()

        // act
        val syncService = DHPSyncCloudService()
        syncService.didUpload = callback
        syncService.uploadAsync(uploadData)
        val uploadCompletedMethod = syncService.javaClass.getDeclaredMethod("uploadToDHPCompleted", GenericDHPResponse::class.java)
        uploadCompletedMethod.isAccessible = true
        val genericDHPResponse: GenericDHPResponse<DHPResponseBody> = mock()
        whenever(genericDHPResponse.success).thenReturn(true)
        uploadCompletedMethod.invoke(syncService, genericDHPResponse)

        // assert
        verify(callback)(true)
    }

    @Test
    fun testThatDownloadAsyncSendsRequestToTheDHPManagerWithTheCorrectPayload() {
        // arrange
        CloudSessionState.shared.activeProfileID = "FED1234"

        // act
        val syncService = DHPSyncCloudService()
        syncService.downloadAsync()
        val genericRequestArgumentCaptor = argumentCaptor<GenericDHPRequest<*>>()

        // assert
        verify(dhpManager).executeAsync(genericRequestArgumentCaptor.capture(), isNull())
        val dhpRequest = genericRequestArgumentCaptor.firstValue
        assertEquals(DHPRetrieval::class.java, dhpRequest.payload.javaClass)
        assertEquals(DHPAPIs.syncDataDownload, dhpRequest.uri)
    }

    @Test
    fun testThatDownloadPrescriptionsAndDevicesAsyncSendsRequestToTheDHPManagerToDownloadPrescriptions() {
        // arrange
        CloudSessionState.shared.activeProfileID = "FED1234"

        // act
        val syncService = DHPSyncCloudService()
        syncService.downloadPrescriptionsAndDevicesAsync()
        val genericRequestArgumentCaptor = argumentCaptor<GenericDHPRequest<*>>()

        // assert
        verify(dhpManager).executeAsync(genericRequestArgumentCaptor.capture(), isNull())
        val dhpRequest = genericRequestArgumentCaptor.firstValue
        assertEquals(DHPGetPatientPrescriptions::class.java, dhpRequest.payload.javaClass)
        assertEquals(DHPAPIs.getPrescriptions, dhpRequest.uri)
    }

    @Test
    fun testThatCallbackIsInvokedWhenPrescriptionDownloadFails() {
        // arrange
        CloudSessionState.shared.activeProfileID = "FED1234"
        val callback: (success: Boolean, data: CloudObjectContainer, moreDataToDownloadExists: Boolean, didProcessDownloadedData: (() -> Unit)?) -> Unit = mock()

        // act
        val syncService = DHPSyncCloudService()
        syncService.didDownload = callback
        val downloadPrescriptionsCompletedMethod = syncService.javaClass.getDeclaredMethod("downloadPrescriptionsFromDHPCompleted", GenericDHPResponse::class.java)
        downloadPrescriptionsCompletedMethod.isAccessible = true
        val genericDHPResponse: GenericDHPResponse<DHPGetPatientPrescriptionsResponseBody> = mock()
        whenever(genericDHPResponse.success).thenReturn(false)
        downloadPrescriptionsCompletedMethod.invoke(syncService, genericDHPResponse)
        val cloudObjectContainerArgumentCaptor = argumentCaptor<CloudObjectContainer>()

        // assert
        verify(callback)(eq(false), cloudObjectContainerArgumentCaptor.capture(), eq(true), isNull())
        assertEquals(0, cloudObjectContainerArgumentCaptor.firstValue.objectCount())
    }

    @Test
    fun testThatDownloadDevicesSendsRequestToTheDHPManagerWithCorrectPayload() {
        // arrange
        CloudSessionState.shared.activeProfileID = "FED1234"

        // act
        val syncService = DHPSyncCloudService()
        val downloadDevicesMethod = syncService.javaClass.getDeclaredMethod("downloadDevicesFromDHPAsync")
        downloadDevicesMethod.isAccessible = true
        downloadDevicesMethod.invoke(syncService)
        val genericRequestArgumentCaptor = argumentCaptor<GenericDHPRequest<*>>()

        // assert
        verify(dhpManager).executeAsync(genericRequestArgumentCaptor.capture(), isNull())
        val dhpRequest = genericRequestArgumentCaptor.firstValue
        assertEquals(DHPGetMedicalDevices::class.java, dhpRequest.payload.javaClass)
        assertEquals(DHPAPIs.getDevices, dhpRequest.uri)
    }

    @Test
    fun testThatCallbackIsInvokedWhenDeviceDownloadFails() {
        // arrange
        CloudSessionState.shared.activeProfileID = "FED1234"
        val callback: (success: Boolean, data: CloudObjectContainer, moreDataToDownloadExists: Boolean, didProcessDownloadedData: (() -> Unit)?) -> Unit = mock()

        // act
        val syncService = DHPSyncCloudService()
        syncService.didDownload = callback
        val downloadDevicesCompletedMethod = syncService.javaClass.getDeclaredMethod("downloadDevicesFromDHPCompleted", GenericDHPResponse::class.java)
        downloadDevicesCompletedMethod.isAccessible = true
        val genericDHPResponse: GenericDHPResponse<DHPGetPatientPrescriptionsResponseBody> = mock()
        whenever(genericDHPResponse.success).thenReturn(false)
        downloadDevicesCompletedMethod.invoke(syncService, genericDHPResponse)
        val cloudObjectContainerArgumentCaptor = argumentCaptor<CloudObjectContainer>()

        // assert
        verify(callback)(eq(false), cloudObjectContainerArgumentCaptor.capture(), eq(true), isNull())
        assertEquals(0, cloudObjectContainerArgumentCaptor.firstValue.objectCount())
    }

    @Test
    fun testThatCallbackIsInvokedWhenDownloadFails() {
        // arrange
        CloudSessionState.shared.activeProfileID = "FED1234"
        val callback: (success: Boolean, data: CloudObjectContainer, moreDataToDownloadExists: Boolean, didProcessDownloadedData: (() -> Unit)?) -> Unit = mock()

        // act
        val syncService = DHPSyncCloudService()
        syncService.didDownload = callback
        val downloadCompletedMethod = syncService.javaClass.getDeclaredMethod("downloadFromDHPCompleted", GenericDHPResponse::class.java)
        downloadCompletedMethod.isAccessible = true
        val genericDHPResponse: GenericDHPResponse<DHPGetPatientPrescriptionsResponseBody> = mock()
        whenever(genericDHPResponse.success).thenReturn(false)
        downloadCompletedMethod.invoke(syncService, genericDHPResponse)
        val cloudObjectContainerArgumentCaptor = argumentCaptor<CloudObjectContainer>()

        // assert
        verify(callback)(eq(false), cloudObjectContainerArgumentCaptor.capture(), eq(false), argumentCaptor<(() -> Unit)>().capture())
        assertEquals(0, cloudObjectContainerArgumentCaptor.firstValue.objectCount())
    }

    @Test
    fun testThatDownloadedDataIsSavedInTheDatabaseWhenDownloadSucceeds() {
        // arrange
        CloudSessionState.shared.activeProfileID = "FED1234"
        val responseBody = createDownloadData()
        val callback: (success: Boolean, data: CloudObjectContainer, moreDataToDownloadExists: Boolean, didProcessDownloadedData: (() -> Unit)?) -> Unit = mock()
        val medication = Medication("12345", "BrandName", "GenericName", MedicationClassification.RELIEVER, 13, 6, 6, 200, 12)
        whenever(medicationDataQuery.get(any<String>())).thenReturn(medication)
        dependencyProvider.register(MedicationDataQuery::class, medicationDataQuery)

        // act
        val syncService = DHPSyncCloudService()
        syncService.didDownload = callback
        val downloadCompletedMethod = syncService.javaClass.getDeclaredMethod("downloadFromDHPCompleted", GenericDHPResponse::class.java)
        downloadCompletedMethod.isAccessible = true
        val genericDHPResponse: GenericDHPResponse<DHPRetrievalResponseBody> = mock()
        whenever(genericDHPResponse.success).thenReturn(true)
        whenever(genericDHPResponse.body).thenReturn(responseBody)
        downloadCompletedMethod.invoke(syncService, genericDHPResponse)
        val cloudObjectContainerArgumentCaptor = argumentCaptor<CloudObjectContainer>()

        // assert
        verify(callback)(eq(true), cloudObjectContainerArgumentCaptor.capture(), eq(false), argumentCaptor<(() -> Unit)>().capture())
        assertEquals(6, cloudObjectContainerArgumentCaptor.firstValue.objectCount())
    }

    private fun createUploadData(): CloudObjectContainer {
        val medication = Medication("12345", "BrandName", "GenericName", MedicationClassification.RELIEVER, 2, 4, 6, 200, 12, null, null)
        val data = CloudObjectContainer()
        data.prescriptions.add(Prescription(1, 1, Instant.ofEpochMilli(1516025804194), medication))
        data.devices.add(Device("9876543210", "12121212", medication, "Work", "Manufacturer", "1", "1"))
        data.inhaleEvents.add(InhaleEvent(1,1, 1, 1, 1, 1, true))
        data.dsas.add(DailyUserFeeling(Instant.ofEpochMilli(1516025804194), UserFeeling.GOOD))
        val setting = ReminderSetting(true, "EnvironmentalReminder", RepeatType.ONCE_PER_DAY, LocalTime.of(8, 30))
        setting.changeTime = Instant.ofEpochMilli(1516025804194)
        data.settings.add(setting)
        return data
    }

    private fun createDownloadData(): DHPRetrievalResponseBody {
        val responseBody: DHPRetrievalResponseBody = mock()
        val prescriptionMedicationOrders = listOf(DHPPrescriptionMedicationOrder("prescription_medication_order", "12345", "1", "unit", "1", "unit", "2018-01-16T00:00:00", "prescription_medication_order", "0", "", "M013", "", "eProAir", "1.0", "2018-01-16T00:00:00", "GMT-05:00", DHPCodes.DataEntryClassification.manual, "FED1234"))
        val medicalDevices = listOf(DHPMedicalDeviceInfo("medical_device_info", "1234567890", "12121212", "12345", "Manufacturer", "1", "1", "", "", "2019-01-16", "200", "195", "5", "2018-01-16T13:00:00", "Home", "active", "medical_device_info", DHPCodes.DeviceClassification.smartInhaler.toString(), "reliever", "", "Home", "0", "M013", "", "eProAir", "1.0", "2018-01-16T00:00:00", "GMT-05:00", DHPCodes.DataEntryClassification.manual, "FED1234", ""))
        val medicationAdministrations = listOf(DHPMedicationAdministration("medication_administration", "1234567890:1", "1234567890", "1", "1516025804194", "GMT-05:00", "10000", "milliseconds", "15000", "milliseconds", "450", "ml/minute", "11000", "milliseconds", "5", "ml", "15", "seconds", "1", "", "12345", "1000", "milliseconds", "2000", "milliseconds", "FALSE", "1", "medication_administration", "0", "M013", "", "eProAir", "1.0", "2018-01-16T00:00:00", "GMT-05:00", DHPCodes.DataEntryClassification.manual, "FED1234", ""))
        val questionnaireResponses = listOf(DHPQuestionnaireResponse("questionnaire_response", "dailyFeeling", "1", "2018-01-16T8:00:00", "questionnaire_response", "0", "", "M013", "", "eProAir", "1.0", "2018-01-16T00:00:00", "GMT-05:00", DHPCodes.DataEntryClassification.manual, "FED1234"))
        val userPreferenceSettings = listOf(DHPUserPreferenceSettings(listOf(DHPSetting("setting", "dailyEnvironmentReminder", "boolean", "true", "", "2018-01-16T08:30:00")), "user_preference_settings", "0", "user_preference_settings", "M013", "", "eProAir", "1.0", "2018-01-16T00:00:00", "GMT-05:00", DHPCodes.DataEntryClassification.manual, ""))
        val userProfiles = listOf(DHPProfileInfo("profile_info", "FED1234", "user001@mail.com", "user001@mail.com", "user", "user", "male", "true", DHPCodes.Role.patient, "", "", "", "", "1970-01-01", "0", "M013", "", "eProAir", "1.0", "2018-01-16T00:00:00", "GMT-05:00", DHPCodes.DataEntryClassification.manual, "profile_info"))
        whenever(responseBody.prescriptionMedicationOrders).thenReturn(prescriptionMedicationOrders)
        whenever(responseBody.medicalDevices).thenReturn(medicalDevices)
        whenever(responseBody.medicationAdministrations).thenReturn(medicationAdministrations)
        whenever(responseBody.questionnaireResponses).thenReturn(questionnaireResponses)
        whenever(responseBody.userPreferenceSettings).thenReturn(userPreferenceSettings)
        whenever(responseBody.profiles).thenReturn(userProfiles)
        whenever(responseBody.inhalerSynchTime_GMT).thenReturn("2018-01-16T11:00:00")
        whenever(responseBody.nonInhalerSynchTime_GMT).thenReturn("2018-01-16T11:00:00")
        whenever(responseBody.additionalDocumentsExist).thenReturn("FALSE")
        return responseBody
    }
}