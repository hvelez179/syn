//
// DHPUserProfileCloudService.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.services.userprofile

import com.teva.cloud.ConsentStatus
import com.teva.cloud.dataentities.CloudAppData
import com.teva.cloud.dataentities.UserProfile
import com.teva.cloud.extensions.addCommonAttributes
import com.teva.cloud.models.CloudSessionState
import com.teva.cloud.services.DHPRequestQueueManager
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.ERROR
import com.teva.utilities.utilities.Logger.Level.VERBOSE
import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes.Role.guardian
import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes.Role.patient
import com.teva.dhp.DataEntities.DHPDataTypes.Endpoints.*
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects.*
import com.teva.dhp.DataEntities.DHPDataTypes.GenericDHPRequest
import com.teva.dhp.DataEntities.DHPDataTypes.GenericDHPResponse
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPAddDependentProfileResponseBody
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPGetPatientAppListResponseBody
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPGetRelatedProfilesResponseBody
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPResponseBody
import com.teva.dhp.models.DHPAPIs
import com.teva.dhp.models.DHPSession
import org.threeten.bp.LocalDate

/**
 * This class implements the UserProfile management functionality at the service layer.
 */
class DHPUserProfileCloudService : UserProfileCloudService {

    /**
     * This property contains the list of profiles to return via getAllProfilesAsync.
     */
    private var profiles: MutableList<UserProfile> = ArrayList()

    /**
     * This property indicates whether the account is registered with the DHP; that is, the account owner or a dependent is registered as a patient.
     * If false, when a profile is made active, we must call the addProfile API.
     * If true and a new dependent is added, we must call the addAdditionalDependent API.
     */
    private var isAccountRegistered = false

    /**
     * This property indicates whether the account owner has the patient role. If the account has only been used thus far for dependent patients, this will be false.
     * If false, when the account owner is made active, we must call the opt-in API to provide the patient role.
     */
    private var isAccountOwnerAPatient = false

    /**
     * This property indicates whether the account owner has the guardian role. If the account has only been used thus far for the account owner patients, this will be false.
     */
    private var isAccountOwnerAGuardian = false

    override var didGetAllProfiles: ((success: Boolean, userProfiles: List<UserProfile>) -> Unit)? = null

    override var didSetProfile: ((success: Boolean, userProfile: UserProfile) -> Unit)? = null

    override var didGetUserAppList: ((success: Boolean, userAppList: List<CloudAppData>) -> Unit)? = null


    val logger = Logger("DHPUserProfileCloudService")

    /**
     * A container object used to track state between async calls.
     */
    private class UserProfileRequestQueueManager : DHPRequestQueueManager() {
        internal var partialProfileInfo: MutableList<UserProfile> = ArrayList()
    }

    /**
     * This property provides the ability to queue UserProfile Requests.
     */
    private var userProfileRequestQueue = UserProfileRequestQueueManager()

    private var timeService = DependencyProvider.default.resolve<TimeService>()

    /**
     * Starts a chain of async calls to get all the UserProfiles stored in the DHP, and determine what API needs to be called to set an active profile.
     */
    override fun getAllProfilesAsync() {
        logger.log(VERBOSE, "getAllProfilesAsync")

        profiles = ArrayList()
        isAccountRegistered = false
        isAccountOwnerAPatient = false
        isAccountOwnerAGuardian = false

        if(CloudSessionState.shared.consentData?.patientDOB == null) {
            didGetAllProfiles?.invoke(false, profiles)
            return
        }

        val profile = UserProfile()
        profile.firstName = CloudSessionState.shared.idHubFirstName
        profile.lastName = CloudSessionState.shared.idHubLastName
        profile.isAccountOwner = true
        profile.isActive = false
        profile.dateOfBirth = CloudSessionState.shared.consentData?.patientDOB
        profile.profileId = DHPSession.shared.federationId
        profile.created = timeService.now()
        profiles.add(profile)

        // getRolesForProfile
        val dhpRequest = createRequestGetRolesForProfile()
        add(dhpRequest)
    }

    /**
     * This method provides the callback for getRolesForProfile DHP API call.
     * @param response: the response body
     */
    private fun getRolesForProfileCompleted(response: GenericDHPResponse<DHPResponseBody>) {

        // If get roles for profile succeeded, this account has been used before by a patient.
        // i.e. the add profile API has been called before, should not be called again.
        isAccountRegistered = response.success

        isAccountOwnerAPatient = response.body?.returnObjects?.toString()?.contains(patient.value) ?: false
        isAccountOwnerAGuardian = response.body?.returnObjects?.toString()?.contains(guardian.value) ?: false

        // If this is the first time using an account, the only options are the Account Owner or a New Dependent. Either will use the addProfile API.
        // If this is not the first time using an account, get the profiles for the account and find out if the account owner is already a patient.
        if (isAccountRegistered && isAccountOwnerAGuardian) {
            val dhpRequest = createRequestGetAllProfiles()
            add(dhpRequest)
        } else {
            didGetAllProfiles?.invoke(true, profiles)
        }
    }

    /**
     * This method provides the callback for getAllProfiles DHP API call.
     * @param response: the DHP response
     */
    private fun getRelatedProfilesCompleted(response: GenericDHPResponse<DHPGetRelatedProfilesResponseBody>) {

        if (response.success) {

            response.body?.profiles?.let { parsedProfiles ->

                for (dhpProfile in parsedProfiles) {
                    val profile = UserProfile()
                    profile.firstName = dhpProfile.firstName
                    profile.lastName = dhpProfile.lastName
                    profile.isAccountOwner = false
                    profile.isActive = false
                    profile.profileId = dhpProfile.profileId

                    profile.dateOfBirth = LocalDate.parse(dhpProfile.dob)
                    profiles.add(profile)
                }
            }
        }

        didGetAllProfiles?.invoke(response.success, profiles)
    }

    override fun setupProfileAsync(profile: UserProfile) {
        logger.log(VERBOSE, "setupProfileAsync")

        // Add the profile to the list of partial profiles.
        userProfileRequestQueue.partialProfileInfo.add(profile)

        if (!isAccountRegistered) {
            if (profile.isAccountOwner == true) {
                addProfileForAccountOwnerPatientAsync(profile)
            } else {
                addProfileForGuardianAndDependentPatientAsync(profile)
            }
        } else {
            if (profile.isAccountOwner == true) {

                if (!isAccountOwnerAPatient) {
                    // In this scenario, the account owner has set up a first device for a dependent
                    // and has the role of 'guardian'.
                    // At this point, we can assume they are setting up a second device
                    // with themselves as the 'patient' role.
                    addProfileForAccountOwnerPatientAsync(profile)
                } else {

                    val profileId = profile.profileId
                    val isAccountOwner = profile.isAccountOwner
                    if(profileId == null || isAccountOwner == null) {
                        logger.log(ERROR, "setupProfileAsync - Expected profileId to be set.")
                        return
                    }

                    // TODO: Get patient mobile app list. If ProAir is not in the list, upload mobile device API needs to include consent.
                    val invokingRole = if(isAccountOwner) patient else guardian
                    val dhpRequest = createRequestUploadMobileDevice(profile.profileId!!, invokingRole)
                    add(dhpRequest)
                }

            } else if(profile.profileId != null && profile.isAccountOwner != null) {
                // TODO: Get patient mobile app list. If ProAir is not in the list, upload mobile device API needs to include consent.
                val invokingRole = if(profile.isAccountOwner == true) patient else guardian
                val dhpRequest = createRequestUploadMobileDevice(profile.profileId!!, invokingRole)
                add(dhpRequest)
            } else {
                // Nil profileId indicates need to set up profile for new dependent.
                addAdditionalDependentProfileAsync(profile)
            }
        }
    }

    /**
     * This method creates a Get Patient App List API request, and executes or queues the request.
     * This API is used to get Mobile App list for a Patient.
     */
    override fun getUserAppListAsync(profile: UserProfile) {
        createGetPatientAppListRequest(profile,  { response ->

            val appList: MutableList<CloudAppData> = ArrayList()
            val patientApps = (response.body)?.patientApps

            if (response.success && patientApps != null)  {

                for (returnObj in patientApps) {

                    val appName = returnObj.appName ?: "Unknown App name"
                    val appVersionNumber = returnObj.appVersionNumber ?: "Unknown App version"

                    val userAppInfo = CloudAppData(appName, appVersionNumber)

                    appList.add(userAppInfo)
                }
            }

            didGetUserAppList?.invoke(response.success, appList)
        })
    }

    /**
     * Build the payload for Get Patient App List API from the DHP.
     */
    private fun createGetPatientAppListRequest(profile: UserProfile, callback: (response: GenericDHPResponse<DHPGetPatientAppListResponseBody>) -> Unit) {

        logger.log(VERBOSE, "createGetPatientAppListRequest")

        val api = DHPAPIs.getPatientAppList
        val payload = DHPGetPatientAppList()
        payload.invokingExternalEntityID = DHPSession.shared.federationId
        payload.invokingRole = if(profile.isAccountOwner == true) patient else guardian

        if (payload.invokingRole == guardian) {
            payload.patientExternalEntityID = profile.profileId
        }

        payload.apiExecutionMode = DHPCodes.ApiExecutionMode.synchronous

        val dhpGetAppList = DHPGetAppList()
        dhpGetAppList.externalEntityID = profile.profileId
        dhpGetAppList.role = patient
        dhpGetAppList.addCommonAttributes(DHPAPIs.getPatientAppList.messageId, null, false, true)
        payload.getAppList = dhpGetAppList

        val getUserAppListRequest = GenericDHPRequest(api, payload, DHPGetPatientAppListResponseBody::class, callback)
        getUserAppListRequest.prependObjectName = true

        add(getUserAppListRequest)
    }

    /**
     * Creates a patient profile for the account owner in the DHP. This call is async.
     * @param profile: the user profile to create.
     */
    private fun addProfileForAccountOwnerPatientAsync(profile: UserProfile) {

        val api = DHPAPIs.addProfile

        val payload = DHPRegisterProfile()
        payload.invokingExternalEntityID = DHPSession.shared.federationId
        payload.invokingRole = patient
        payload.apiExecutionMode = DHPCodes.ApiExecutionMode.synchronous

        // Build the payload
        payload.consent_info = createConsentInfo(api.messageId, "Active", externalEntityID = DHPSession.shared.federationId)
        payload.profile_info = profile.toDHPType(role = patient, messageId = api.messageId)

        payload.mobile_device_info = createMobileDeviceInfo(api.messageId, DHPSession.shared.federationId)

        val addProfile = GenericDHPRequest(api, payload, DHPResponseBody::class, this::setupProfileCompleted)
        addProfile.prependObjectName = true

        // Add the profile to the list of partial profiles.
        userProfileRequestQueue.partialProfileInfo.add(profile)
        add(addProfile)
    }

    /**
     * Creates a patient profile for a dependent and an a guardian profile for the account owner in the DHP. This call is async.
     * @param profile: the user profile to create.
     */
    private fun addProfileForGuardianAndDependentPatientAsync(profile: UserProfile) {

        val api = DHPAPIs.addProfile

        val payload = DHPRegisterProfile()
        payload.invokingExternalEntityID = DHPSession.shared.federationId
        payload.invokingRole = guardian
        payload.apiExecutionMode = DHPCodes.ApiExecutionMode.synchronous

        // Build the payload
        payload.consent_info = createConsentInfo(api.messageId, "Active")
        payload.profile_info = profile.toDHPType(role = patient, messageId = api.messageId)
        payload.guardian_info = (profiles.firstOrNull { it -> it.isAccountOwner == true} ?: UserProfile()).toDHPType(guardian, api.messageId)

        payload.mobile_device_info = createMobileDeviceInfo(api.messageId, DHPSession.shared.federationId)

        val addProfile = GenericDHPRequest(api, payload, DHPAddDependentProfileResponseBody::class, this::addDependentProfileCompleted)
        addProfile.prependObjectName = true

        add(addProfile)
    }

    /**
     * Adds an additional dependent profile to the account, which already has at least one patient profile. This call is async.
     * @param profile: the user profile to create.
     */
    private fun addAdditionalDependentProfileAsync(profile: UserProfile) {

        if (!isAccountOwnerAGuardian) {
            addProfileForGuardianAndDependentPatientAsync(profile)
            return
        }

        val api = DHPAPIs.addDependentPatient

        val payload = DHPAddDependentPatient()
        payload.invokingExternalEntityID = DHPSession.shared.federationId
        payload.invokingRole = guardian
        payload.apiExecutionMode = DHPCodes.ApiExecutionMode.synchronous

        // Build the payload
        payload.consent_info = createConsentInfo(api.messageId, "Active", externalEntityID = DHPSession.shared.federationId)
        payload.consent_info?.serverTimeOffset = "0"
        payload.profile_info = profile.toDHPType(patient, api.messageId)
        payload.profile_info?.serverTimeOffset = "0"
        payload.mobile_device_info = createMobileDeviceInfo(api.messageId, DHPSession.shared.federationId)
        payload.mobile_device_info?.serverTimeOffset = "0"

        val addProfile = GenericDHPRequest(api, payload, DHPAddDependentProfileResponseBody::class, this::addDependentProfileCompleted)
        addProfile.prependObjectName = true

        add(addProfile)
    }

    /**
     * This method provides the callback for the addProfile DHP API call.
     * @param response: the DHP response
     */
    private fun setupProfileCompleted(response: GenericDHPResponse<DHPResponseBody>) {

        val userProfile = userProfileRequestQueue.partialProfileInfo.removeAt(0)

        didSetProfile?.invoke(response.success, userProfile)
    }

    /**
     * This method provides the callback for the add dependent profile DHP API call.
     * @param response: the DHP response
     */
    private fun addDependentProfileCompleted(response: GenericDHPResponse<DHPAddDependentProfileResponseBody>) {

        val userProfile = userProfileRequestQueue.partialProfileInfo.removeAt(0)

        if (response.success) {

            userProfile.profileId = response.body?.externalEntityID
        }

        didSetProfile?.invoke(response.success, userProfile)
    }

    // Private Request Creator Methods

    /**
     * Build the payload for uploadMobileDevice DHP API.
     * @param profileId: Active profile's ID.
     */
    private fun createRequestUploadMobileDevice(profileId: String, invokingRole: DHPCodes.Role): GenericDHPRequest<DHPResponseBody> {

        val mobileDeviceInfo = createMobileDeviceInfo(DHPAPIs.addMobileDevice.messageId, profileId)
        mobileDeviceInfo.addCommonAttributes(DHPAPIs.addMobileDevice.messageId)
        mobileDeviceInfo.objectName = mobileDeviceInfo.dhpObjectName

        val payload = DHPAddMobileDevice()
        payload.invokingExternalEntityID = DHPSession.shared.federationId

        payload.invokingRole = invokingRole

        payload.apiExecutionMode = DHPCodes.ApiExecutionMode.synchronous
        if (payload.invokingRole == guardian) {
            payload.patientExternalEntityID = profileId
        }

        payload.mobile_device_info = mobileDeviceInfo

        val upload = GenericDHPRequest(DHPAPIs.addMobileDevice, payload, DHPResponseBody::class, this::setupProfileCompleted)
        upload.prependObjectName = true

        return upload
    }

    /**
     * Build the payload for getRolesForProfile DHP API.
     */
    private fun createRequestGetRolesForProfile(): GenericDHPRequest<DHPResponseBody> {

        val payload = DHPGetRolesForProfile()
        payload.invokingExternalEntityID = DHPSession.shared.federationId
        payload.apiExecutionMode = DHPCodes.ApiExecutionMode.synchronous

        payload.getRoles = DHPGetRolesRequest()
        payload.getRoles?.externalEntityID = DHPSession.shared.federationId
        payload.getRoles?.addCommonAttributes(DHPAPIs.getRolesForProfile.messageId, null, false, true)

        val getRolesForProfile = GenericDHPRequest(DHPAPIs.getRolesForProfile, payload, DHPResponseBody::class, this::getRolesForProfileCompleted)
        getRolesForProfile.prependObjectName = true

        return getRolesForProfile
    }

    /**
     * Build the payload for getAllProfiles API from the DHP.
     */
    private fun createRequestGetAllProfiles(): GenericDHPRequest<DHPGetRelatedProfilesResponseBody> {

        val payload = DHPGetRelatedProfiles()
        payload.invokingExternalEntityID = DHPSession.shared.federationId
        payload.invokingRole = guardian
        payload.apiExecutionMode = DHPCodes.ApiExecutionMode.synchronous

        payload.getRelatedProfiles = DHPGetRelatedProfilesRequest()
        payload.getRelatedProfiles?.externalEntityID = DHPSession.shared.federationId
        payload.getRelatedProfiles?.role = guardian
        payload.getRelatedProfiles?.targetRole = listOf(patient.value)
        payload.getRelatedProfiles?.addCommonAttributes(DHPAPIs.getRelatedProfilesList.messageId)

        val getAllProfiles = GenericDHPRequest(DHPAPIs.getRelatedProfilesList, payload, DHPGetRelatedProfilesResponseBody::class, this::getRelatedProfilesCompleted)
        getAllProfiles.prependObjectName = true

        return getAllProfiles
    }

    // Private Helper Methods

    /**
     * This method adds the the passed-in DHP Request to the DHPRequestQueueManager.
     * The DHPRequestQueueManager executes the request if there are no outstanding requests.
     * Otherwise, the request is enqueued.
     * @param dhpRequest: This parameter contains the DHP API request data.
     */
    private fun add(dhpRequest: GenericDHPRequest<*>) {
        userProfileRequestQueue.add(dhpRequest)
    }

    /**
     * Creates a DHPConsentInfo object that wraps the "consent_info" object.
     * @param messageId: the message ID of the request.
     * @param status: the consent status.
     * @param externalEntityID: the external entity ID.
     */
    private fun createConsentInfo(messageId: String, status: String, externalEntityID: String? = null): DHPConsentInfo {

        val consentInfo = DHPConsentInfo()
        val consentData = CloudSessionState.shared.consentData
        val hasConsented = (status == ConsentStatus.ACTIVE.status).toString()
        val now = CloudSessionState.shared.serverTime ?: timeService.now()

        consentInfo.status = status
        consentInfo.acceptIndicator = "true"
        consentInfo.consentEnabledStatus = hasConsented
        consentInfo.historicalDataIndicator = "true"
        consentInfo.privacyNoticeReadIndicator = "true"
        consentInfo.termsAndConditions = consentData?.termsAndConditions ?: ""
        consentInfo.privacyNotice = consentData?.privacyNotice ?: ""
        consentInfo.consentAuditableEventDate = CloudSessionState.shared.dateFormatter.format(consentData?.created ?: now)
        consentInfo.consentStartDate = CloudSessionState.shared.dateFormatter.format(consentData?.consentStartDate?.atStartOfDay() ?: now)
        consentInfo.dataEntryClassification = DHPCodes.DataEntryClassification.manual
        consentInfo.consentType = DHPCodes.ConsentType.cloudConsent
        consentInfo.externalEntityID = externalEntityID

        consentInfo.addCommonAttributes(messageId)

        return consentInfo
    }

    private fun createMobileDeviceInfo(messageId: String, externalEntityID: String): DHPMobileDeviceInfo {

        val mobileDeviceInfo = DHPMobileDeviceInfo()
        mobileDeviceInfo.externalEntityID = externalEntityID
        mobileDeviceInfo.serialNumber = CloudSessionState.shared.mobileUUID
        mobileDeviceInfo.serverTimeOffset = "0"

        mobileDeviceInfo.addCommonAttributes(messageId)

        return mobileDeviceInfo
    }
}
