//
// DHPSyncCloudService.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.services.sync

import com.teva.cloud.dataentities.UserAccount
import com.teva.cloud.dataentities.UserProfile
import com.teva.cloud.dataquery.UserAccountQuery
import com.teva.cloud.extensions.*
import com.teva.cloud.models.CloudConstants
import com.teva.cloud.models.CloudSessionState
import com.teva.cloud.services.CloudObjectContainer
import com.teva.cloud.services.UserPreferenceSettings
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.ERROR
import com.teva.utilities.utilities.Logger.Level.VERBOSE
import com.teva.devices.entities.Device
import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.Endpoints.DHPDataSynch
import com.teva.dhp.DataEntities.DHPDataTypes.Endpoints.DHPGetMedicalDevices
import com.teva.dhp.DataEntities.DHPDataTypes.Endpoints.DHPGetPatientPrescriptions
import com.teva.dhp.DataEntities.DHPDataTypes.Endpoints.DHPRetrieval
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects.DHPMedicalDevicesRequest
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects.DHPProgramMedicalListRequest
import com.teva.dhp.DataEntities.DHPDataTypes.GenericDHPRequest
import com.teva.dhp.DataEntities.DHPDataTypes.GenericDHPResponse
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPGetMedicalDevicesResponseBody
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPGetPatientPrescriptionsResponseBody
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPResponseBody
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPRetrievalResponseBody
import com.teva.dhp.DataEntities.DHPDataTypes.SyncedObject
import com.teva.dhp.models.DHPAPIs
import com.teva.dhp.models.DHPManager
import com.teva.dhp.models.DHPSession
import com.teva.medication.entities.Prescription
import org.threeten.bp.Instant


/**
 * This class implements the SyncCloudService interface to upload and download data in the cloud.
 */
internal class DHPSyncCloudService : SyncCloudService {

    /**
     * This property is used to access the public interface of the Teva_DHP project.
     */
    private val dhpManager: DHPManager = DependencyProvider.default.resolve()

    /**
     * This property is used to get and save the UserAccount stored in the database.
     */
    private val userAccountQuery  = DependencyProvider.default.resolve<UserAccountQuery>()

    /**
     * This property is used to get the current time for the app.
     */
    private val timeService: TimeService = DependencyProvider.default.resolve()

    /**
     * This property indicates whether a sync has completed before.
     */
    override var isFirstSync: Boolean = lastInhalerSyncTime == null || lastNonInhalerSyncTime == null

    /**
     * This property is the Logger used to log to the console.
     */
    private val logger = Logger("DHPSyncCloudService")



    /**
     * This property is used to get and save the ConsentData stored in the database.
     */
    //private val consentDataQuery: ConsentDataQuery = DependencyProvider.default.resolve()



    /**
     * This property contains the objects to be uploaded to the cloud.
     */
    private var objectsToUpload: MutableList<SyncedObject> = ArrayList()

    /**
     * This property contains the objects downloaded from the cloud.
     */
    private var downloadedObjects = CloudObjectContainer()

    /**
     * This property tracks the maximum number of objects to cache and continue retrieving data before calling back.
     * Once the downloadedObjects count exceeds this threshold, send the delegate what has been downloaded so far, even if
     * the server indicates there is more data to download.
     */
    private var downloadObjectCountThreshold = 100

    /**
     * This property tracks the number of times the initial download has been attempted when it fails.
     */
    private var initialDownloadRetryAttempts = 0

    /**
     * This property contains the prescriptions and devices downloaded from the cloud via downloadPrescriptionsAndDevicesAsync.
     */
    private var downloadedPrescriptionsAndDevices = CloudObjectContainer()

    override var didDownload: ((success: Boolean, data: CloudObjectContainer, moreDataToDownloadExists: Boolean, didProcessDownloadedData: (() -> Unit)?) -> Unit)? = null
    override var didUpload: ((success: Boolean) -> Unit)? = null

    private var cachedLastInhalerSyncTime: Instant? = null
    private var lastInhalerSyncTime: Instant?
        get() { return getUserAccount().lastInhalerSyncTime }
        set(newValue) {
            val userAccount = getUserAccount()
            userAccount.lastInhalerSyncTime = newValue
            saveUserAccount(userAccount)
        }


    private var cachedLastNonInhalerSyncTime: Instant? = null
    private var lastNonInhalerSyncTime: Instant?
        get() { return getUserAccount().lastNonInhalerSyncTime }
        set(newValue) {
            val userAccount = getUserAccount()
            userAccount.lastNonInhalerSyncTime = newValue
            saveUserAccount(userAccount)
        }

    override fun uploadAsync(data: CloudObjectContainer) {
        logger.log(VERBOSE, "uploadAsync()")

        objectsToUpload.clear()

        objectsToUpload.addAll(data.prescriptions.map { it.toDHPType() })
        objectsToUpload.addAll(data.devices.map { it.toDHPType() })
        objectsToUpload.addAll(data.inhaleEvents.map { it.toDHPType() })
        objectsToUpload.addAll(data.dsas.map { it.toDHPType() })
        objectsToUpload.addAll(data.profiles.map { it.toDHPType(forUpload = true) })

        if (!data.settings.isEmpty()) {
            val userPreferenceSettings = UserPreferenceSettings()
            userPreferenceSettings.recurringReminderSettings.addAll(data.settings)
            objectsToUpload.add(userPreferenceSettings.toDHPType())
        }

        if (!objectsToUpload.isEmpty()) {
            uploadToDHPAsync()
        } else {
            didUpload?.invoke(true)
        }
    }

    override fun downloadPrescriptionsAndDevicesAsync() {
        logger.log(VERBOSE, "downloadPrescriptionsAndDevicesAsync")

        downloadedPrescriptionsAndDevices = CloudObjectContainer()
        downloadPrescriptionsFromDHPAsync()
    }

    override fun downloadAsync() {
        logger.log(VERBOSE, "downloadAsync()")
        downloadObjectCountThreshold = CloudConstants.downloadObjectCountThreshold
        downloadFromDHPAsync(false)
    }

    // Private methods

    /**
     * This method builds the DHP upload payload and executes the request.
     * It limits the number of objects uploaded based on the DHPManager's maxUploadObjects property.
     */
    private fun uploadToDHPAsync() {

        logger.log(VERBOSE, "uploadToDHPAsync()")

        var objs = ArrayList<Any>()
        val api = DHPAPIs.syncDataUpload

        try {
            val lastIndex = if(objectsToUpload.lastIndex > dhpManager.maxUploadObjects - 1) dhpManager.maxUploadObjects - 1 else objectsToUpload.lastIndex
            for (obj in objectsToUpload.subList(0, lastIndex + 1)) {
                obj.addCommonAttributes(api.messageId, withSourceTime = false)
                objs.add(obj)
            }
        } catch(e: Exception) {
            logger.log(ERROR, "error adding common attributes", e)
        }

        val payload = DHPDataSynch()
        payload.invokingExternalEntityID = DHPSession.shared.federationId
        payload.invokingRole = CloudSessionState.shared.activeInvokingRole

        if(payload.invokingRole == DHPCodes.Role.guardian) {
            payload.patientExternalEntityID = CloudSessionState.shared.activeProfileID
        }

        payload.apiExecutionMode = DHPCodes.ApiExecutionMode.asynchronous
        payload.objects = objs

        if (CloudSessionState.shared.isClinical) {
            payload.patientStudyHashKey = CloudSessionState.shared.studyHashKey
        }

        var upload = GenericDHPRequest(api, payload, DHPResponseBody::class, this::uploadToDHPCompleted)
        upload.prependObjectName = true

        dhpManager.executeAsync(upload, null)
    }

    /**
     * This method provides the callback for DHPManager.upload.
     * @param response: the DHP response.
     */
    private fun uploadToDHPCompleted(response: GenericDHPResponse<DHPResponseBody>) {

        logger.log(VERBOSE, "uploadToDHPCompleted - succeeded: ${response.success}   message: ${response.message}")

        if (response.success) {
            // Successfully uploaded first x objects, if there are more, upload again with next group and don't callback yet.
            if (objectsToUpload.size > dhpManager.maxUploadObjects) {
                objectsToUpload.subList(0, dhpManager.maxUploadObjects).clear()
                return uploadToDHPAsync()
            }
        }

        didUpload?.invoke(response.success)
    }

    /**
     * This method builds the DHP download payload and executes the request.
     *
     * @param appendData: should the data downloaded be appended to the downloadObjects in memory, or should this start a new download
     */
    private fun downloadFromDHPAsync(appendData: Boolean) {

        logger.log(VERBOSE, "downloadFromDHPAsync")

        if (!appendData) {
            downloadedObjects.removeAllData()
        }

        val lastInhalerSyncTimeValue = cachedLastInhalerSyncTime ?: lastInhalerSyncTime ?: Instant.ofEpochSecond(0)
        val lastNonInhalerSyncTimeValue = cachedLastNonInhalerSyncTime ?: lastNonInhalerSyncTime ?: Instant.ofEpochSecond(0)

        val api = DHPAPIs.syncDataDownload

        val payload = DHPRetrieval()

        payload.messageID = api.messageId
        payload.appVersionNumber = CloudSessionState.shared.appVersionNumber
        payload.appName = CloudSessionState.shared.appName
        payload.UUID = CloudSessionState.shared.mobileUUID
        payload.inhalerSynchTime_GMT = lastInhalerSyncTimeValue.toGMTString(true)
        payload.nonInhalerSynchTime_GMT = lastNonInhalerSyncTimeValue.toGMTString(true)
        payload.invokingExternalEntityID = DHPSession.shared.federationId
        payload.invokingRole = CloudSessionState.shared.activeInvokingRole

        if (payload.invokingRole == DHPCodes.Role.guardian) {
            payload.patientExternalEntityID = CloudSessionState.shared.activeProfileID
        }

        payload.username = DHPSession.shared.username
        payload.retrievalType = "DataSyncRetrieval"
        payload.apiExecutionMode = DHPCodes.ApiExecutionMode.synchronous

        val retrieval = GenericDHPRequest(api, payload, DHPRetrievalResponseBody::class, this::downloadFromDHPCompleted)
        dhpManager.executeAsync(retrieval, null)
    }

    /**
     * This method provides the callback for DHPManager.upload.
     *
     * @param response: the DHP response.
     */
    private fun downloadFromDHPCompleted(response: GenericDHPResponse<DHPRetrievalResponseBody>) {

        logger.log(VERBOSE, "downloadFromDHPCompleted - succeeded: ${response.success}   message: ${response.message}")

        var moreDataToDownloadExists = false

        // TODO: deal with errors better.  Reset sync time? Ignore and move on?
        if (response.success) {

            val lastInhalerSyncTimeGMT = response.body?.inhalerSynchTime_GMT
            val lastNonInhalerSyncTimeGMT = response.body?.nonInhalerSynchTime_GMT
            val additionalDocumentsExist = response.body?.additionalDocumentsExist

            if (lastInhalerSyncTimeGMT != null && lastNonInhalerSyncTimeGMT != null && additionalDocumentsExist != null) {

                response.body?.prescriptionMedicationOrders?.let { prescriptionMedicationOrders ->
                    downloadedObjects.prescriptions.addAll(prescriptionMedicationOrders.map { it.fromDHPType() })
                }

                response.body?.medicalDevices?.let { medicalDevices ->
                    val devices = medicalDevices.map({it.fromDHPType()}).filterNotNull()
                    downloadedObjects.devices.addAll(devices)
                }

                response.body?.medicationAdministrations?.let { medicationAdministrations ->
                    val inhaleEvents = medicationAdministrations.map { it.fromDHPType() }.filterNotNull()
                    downloadedObjects.inhaleEvents.addAll(inhaleEvents)
                }

                response.body?.questionnaireResponses?.let { questionnaireResponses ->
                    val dsas = questionnaireResponses.map { it.fromDHPType()}.filterNotNull()
                    downloadedObjects.dsas.addAll(dsas)
                }

                response.body?.userPreferenceSettings?.let { userPreferenceSettings ->
                    userPreferenceSettings.firstOrNull()?.let { userPreferenceSetting ->
                        downloadedObjects.settings.addAll(UserPreferenceSettings.fromDHPType(userPreferenceSetting).recurringReminderSettings)
                    }
                }

                response.body?.profiles?.let { dhpProfiles ->
                    val userProfiles = dhpProfiles.map { UserProfile.fromDHPType(it)}.filterNotNull()
                    downloadedObjects.profiles.addAll(userProfiles)
                }

                cachedLastInhalerSyncTime = instantFromGMTString(lastInhalerSyncTimeGMT) ?: lastInhalerSyncTime ?: Instant.ofEpochSecond(0)
                cachedLastNonInhalerSyncTime = instantFromGMTString(lastNonInhalerSyncTimeGMT) ?: lastNonInhalerSyncTime ?: Instant.ofEpochSecond(0)

                val downloadMore = (additionalDocumentsExist == "TRUE")
                val hasExceededDownloadObjectCountThreshold = downloadedObjects.objectCount() >= downloadObjectCountThreshold

                if (downloadMore && !hasExceededDownloadObjectCountThreshold) {
                    // There is more to download from the server, download again with the new token, and don't callback yet.
                    return downloadFromDHPAsync(true)
                } else {
                    moreDataToDownloadExists = downloadMore && hasExceededDownloadObjectCountThreshold
                }
            }
        }

        didDownload?.invoke(response.success, downloadedObjects, moreDataToDownloadExists, {
            // Only update the last sync times when the manager is done merging the data, in case the app is killed before doing so.
            lastInhalerSyncTime = cachedLastInhalerSyncTime
            lastNonInhalerSyncTime = cachedLastNonInhalerSyncTime
            cachedLastInhalerSyncTime = null
            cachedLastNonInhalerSyncTime = null

            logger.log(VERBOSE, "updated lastInhalerSyncTime:${lastInhalerSyncTime ?: Instant.ofEpochSecond(0).toGMTString(true)}")
            logger.log(VERBOSE, "updated lastNonInhalerSyncTime: ${lastNonInhalerSyncTime ?: Instant.ofEpochSecond(0).toGMTString(true)}")
        })
    }

    /**
     * This method builds the DHP get prescriptions payload and executes the request.
     */
    private fun downloadPrescriptionsFromDHPAsync() {

        logger.log(VERBOSE, "downloadPrescriptionsFromDHPAsync")

        val payload = DHPGetPatientPrescriptions()
        payload.invokingExternalEntityID = DHPSession.shared.federationId
        payload.invokingRole = CloudSessionState.shared.activeInvokingRole
        payload.apiExecutionMode = DHPCodes.ApiExecutionMode.synchronous

        if (payload.invokingRole == DHPCodes.Role.guardian) {
            payload.patientExternalEntityID = CloudSessionState.shared.activeProfileID
        }

        payload.program_medical_list = DHPProgramMedicalListRequest()
        payload.program_medical_list?.externalEntityID = CloudSessionState.shared.activeProfileID
        payload.program_medical_list?.addCommonAttributes(DHPAPIs.getPrescriptions.messageId, null)

        if (CloudSessionState.shared.isClinical) {
            payload.patientStudyHashKey = CloudSessionState.shared.studyHashKey
        }

        val getPrescriptions = GenericDHPRequest(DHPAPIs.getPrescriptions, payload, DHPGetPatientPrescriptionsResponseBody::class, this::downloadPrescriptionsFromDHPCompleted)
        getPrescriptions.prependObjectName = true

        dhpManager.executeAsync(getPrescriptions, null)
    }

    /**
     * This method provides the callback for downloadPrescriptionsFromDHPAsync.
     * @param response: the DHP response
     */
    private fun downloadPrescriptionsFromDHPCompleted(response: GenericDHPResponse<DHPGetPatientPrescriptionsResponseBody>) {
        logger.log(VERBOSE, "downloadPrescriptionsFromDHPCompleted - succeeded: ${response.success} message: ${response.message}")

        if (!response.success || response.body?.prescriptionMedicationOrderResources == null) {
            didDownload?.invoke(false, downloadedPrescriptionsAndDevices, true, null)
            return
        }
        val prescriptions = response.body?.prescriptionMedicationOrderResources!!.filter { it != null && it.fromDHPResource() != null }?.map { it.fromDHPResource() as Prescription }
        downloadedPrescriptionsAndDevices.prescriptions.addAll(prescriptions)

        downloadDevicesFromDHPAsync()
    }

    /**
     * This method builds the DHP get devices payload and executes the request.
     */
    private fun downloadDevicesFromDHPAsync() {

        logger.log(VERBOSE, "downloadDevicesFromDHPAsync")

        val payload = DHPGetMedicalDevices()
        payload.invokingExternalEntityID = DHPSession.shared.federationId
        payload.invokingRole = CloudSessionState.shared.activeInvokingRole
        payload.apiExecutionMode = DHPCodes.ApiExecutionMode.synchronous

        if (payload.invokingRole == DHPCodes.Role.guardian) {
            payload.patientExternalEntityID = CloudSessionState.shared.activeProfileID
        }

        payload.getMedicalDevices = DHPMedicalDevicesRequest()
        payload.getMedicalDevices?.addCommonAttributes(DHPAPIs.getDevices.messageId, dataEntryClassification = null)

        if (CloudSessionState.shared.isClinical) {
            payload.patientStudyHashKey = CloudSessionState.shared.studyHashKey
        }

        val getDevices = GenericDHPRequest(DHPAPIs.getDevices, payload, DHPGetMedicalDevicesResponseBody::class, this::downloadDevicesFromDHPCompleted)
        getDevices.prependObjectName = true

        dhpManager.executeAsync(getDevices, null)
    }

    /**
     * This method provides the callback for downloadDevicesFromDHPAsync.
     * @param response: the DHP response
     */
    private fun downloadDevicesFromDHPCompleted(response: GenericDHPResponse<DHPGetMedicalDevicesResponseBody>) {

        logger.log(VERBOSE, "downloadDevicesFromDHPCompleted - succeeded: ${response.success}  message: ${response.message}")

        if (!response.success || response.body?.medicalDeviceResources == null) {
            didDownload?.invoke(false, downloadedPrescriptionsAndDevices, true, null)
            return
        }

        val devices = response.body?.medicalDeviceResources!!.filter { it.fromDHPResource() != null}.map { it.fromDHPResource() as Device }
        downloadedPrescriptionsAndDevices.devices.addAll(devices)

        didDownload?.invoke(response.success, downloadedPrescriptionsAndDevices, true, null)
    }

    /**
     * This method gets the UserAccount from the database.
     */
    private fun getUserAccount(): UserAccount {
        var obj = userAccountQuery.getUserAccount()

        if (obj == null) {
            obj = UserAccount()
            obj.created = timeService.now()
            userAccountQuery.insert(obj, false)
        }

        return obj
    }

    /**
     * This method saves the UserAccount object into the database.
     */
    private fun saveUserAccount(userAccount: UserAccount) {
        userAccountQuery.update(userAccount, true)
        logger.log(VERBOSE, "saveUserAccount: ${userAccount.federationId ?: ""}")
    }

}