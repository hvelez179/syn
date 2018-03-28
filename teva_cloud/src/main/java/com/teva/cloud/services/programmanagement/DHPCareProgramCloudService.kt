//
// DHPCareProgramCloudService.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.services.programmanagement

import com.teva.cloud.dataentities.CloudAppData
import com.teva.cloud.dataentities.ProgramData
import com.teva.cloud.extensions.*
import com.teva.cloud.models.CloudSessionState
import com.teva.cloud.models.programmanagement.InvitationDetails
import com.teva.cloud.services.DHPRequestQueueManager
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.*
import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.Endpoints.DHPAcceptInvitation
import com.teva.dhp.DataEntities.DHPDataTypes.Endpoints.DHPGetInvitationDetails
import com.teva.dhp.DataEntities.DHPDataTypes.Endpoints.DHPGetUserProgramAppList
import com.teva.dhp.DataEntities.DHPDataTypes.Endpoints.DHPOptOutDataSharing
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects.*
import com.teva.dhp.DataEntities.DHPDataTypes.GenericDHPRequest
import com.teva.dhp.DataEntities.DHPDataTypes.GenericDHPResponse
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPGetInvitationDetailResponseBody
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPGetUserProgramAppListResponseBody
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPResponseBody
import com.teva.dhp.models.DHPAPIs
import com.teva.dhp.models.DHPSession
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId


/**
 * This class implements the CareProgramCloudService interface and executes the request
 * using the DHP Manager.
 */

class DHPCareProgramCloudService : CareProgramCloudService {

    // callback methods
    override var didGetUserProgramList: ((errorCode: CareProgramErrorCode, errorDetails: List<CareProgramErrorDetail>?, programs: List<ProgramData>) -> Unit)? = null
    override var didGetInvitationDetails: ((errorCode: CareProgramErrorCode, errorDetails: List<CareProgramErrorDetail>?, invitationDetails: InvitationDetails) -> Unit)? = null
    override var didAcceptInvitation: ((errorCode: CareProgramErrorCode, errorDetails: List<CareProgramErrorDetail>?, invitationDetails: InvitationDetails?) -> Unit)? = null
    override var didLeaveProgram: ((errorCode: CareProgramErrorCode, errorDetails: List<CareProgramErrorDetail>?, programId: String) -> Unit)? = null

    /**
     * This property is the Logger used to log to the console.
     */
    private var logger = Logger("DHPCareProgramCloudService")

    /**
     * This property provides the ability to queue DHP Requests.
     */
    data class CareProgramRequestQueueManager(var invitationDetailsToAccept: InvitationDetails? = null, var programIdToOptOut: String? = null): DHPRequestQueueManager()
    private var dhpRequestQueueManager = CareProgramRequestQueueManager()

    /**
     * This property contains the invitation details from the last Get Invitatation Details API call.
     * Check that invitationCode property is not empty to determine if its payload is valid.
     */
    private var lastInvitationDetails = InvitationDetails()

    // Public Methods

    override fun getUserProgramListAsync() {
        createGetUserProgramAppListRequest()
    }

    /**
     * This method creates a Get Program Details API request, and executes or queues the request.
     * @param invitationCode: This parameter is the invitation code to use to get Program details.
     */
    override fun getInvitationDetailsAsync(invitationCode: String) {
        createGetInvitationDetailsRequest(invitationCode)
    }

    /**
     * This method creates an Accept Invitation API request, and executes or queues the request.
     * @param invitationCode: This parameter is the invitation code used to enroll in a Program.
     */
    override fun acceptInvitationAsync(invitationDetails: InvitationDetails) {
        createAcceptInvitationRequest(invitationDetails)
    }

    /**
     * This method creates a Consent Opt Out API request, and executes or queues the request.
     * @param programId: This parameter is the Program to leave.
     */
    override fun leaveProgramAsync(programId: String) {
        createConsentOptOutRequest(programId)
    }

    // DHP Request Callbacks

    /**
     * This method is the callback for the Get Invitation Details DHP API call.
     * It parses the JSON, and invokes the corresponding delegate method.
     * @param response: Contains the response body, message codes and indication of success.
     */
    private fun getInvitationDetailsCompleted(response: GenericDHPResponse<DHPGetInvitationDetailResponseBody>) {

        var invitationDetails = InvitationDetails()

        var errorCode = CareProgramErrorCode.UNKNOWN
        var errorDetails: List<CareProgramErrorDetail>? = null

        if (response.body?.responseMessageCode != null) {
            errorCode = CareProgramErrorCode.fromDHPResponseCode(response.body?.responseMessageCode!!)
        }

        if (response.success) {

            if (response.body?.responseMessageCode != null) {
                errorCode = CareProgramErrorCode.fromDHPResponseCode(response.body?.responseMessageCode!!)
            }

            errorDetails = CareProgramErrorDetail.toList(response.body?.errorDetails)

            val invitationResource = (response.body)?.invitationResource
            val programResource = (response.body)?.programResource
            if (invitationResource != null && programResource != null) {

                if (invitationResource != null) {

                    invitationDetails.parseInvitationDetails(invitationResource)
                    invitationDetails.parseInvitationProgramDetails(programResource)

                    if (invitationDetails.invitationStatus == "accepted") {
                        didGetInvitationDetails?.invoke(CareProgramErrorCode.INVITATION_ALREADY_ACCEPTED, errorDetails, invitationDetails)
                        return
                    }
                    val expirationDate = instantFromGMTString(invitationDetails.expirationDateGMT)
                    if (expirationDate == null) {
                        didGetInvitationDetails?.invoke(CareProgramErrorCode.EXPIRED_INVITATION_CODE, errorDetails, invitationDetails)
                        return
                    }
                    if (Instant.now().isAfter(expirationDate) || Instant.now().equals(expirationDate)) {
                        didGetInvitationDetails?.invoke(CareProgramErrorCode.EXPIRED_INVITATION_CODE, errorDetails, invitationDetails)
                        return
                    }

                } else {
                    logger.log(ERROR, "getInvitationDetailsCompleted failed to parse.")
                    errorCode = CareProgramErrorCode.GET_INVITATION_DETAILS_REQUEST_FAILED
                }
            } else {
                logger.log(ERROR, "getInvitationDetailsCompleted received failure: $response")
                errorCode = CareProgramErrorCode.GET_INVITATION_DETAILS_REQUEST_FAILED
            }
        }

        didGetInvitationDetails?.invoke(
                errorCode,
                errorDetails,
                invitationDetails)
    }

    /**
     * This method is the callback for the Get User Program App List DHP API call.
     * It parses the JSON, and invokes the corresponding delegate method.
     * @param response: Contains the response body, message codes and indication of success.
     */
    private fun getUserProgramAppListCompleted(response: GenericDHPResponse<DHPGetUserProgramAppListResponseBody>) {

        var programList = ArrayList<ProgramData>()

        if (response.body?.responseMessageCode == null){
            didGetUserProgramList?.invoke(CareProgramErrorCode.NO_INTERNET_CONNECTION, null, programList)
        }

        var errorCode = CareProgramErrorCode.UNKNOWN
        var errorDetails: List<CareProgramErrorDetail>? = null

        if (response.success) {

            if (response.body?.responseMessageCode != null) {
                errorCode = CareProgramErrorCode.fromDHPResponseCode(response.body?.responseMessageCode!!)
            }

            errorDetails = CareProgramErrorDetail.toList(response.body?.errorDetails)

            response.body?.getUserProgramAppListReturnObject?.programGroups?.let { programGroups ->

                for (group in programGroups) {

                    group.program_id?.let { programId ->

                        // If programId is not already in cached list, add it.
                        if (!programList.any { it.programId == programId }) {

                            // Convert object from JSON ReturnObject to object used by App.
                            var consentedAppData: ArrayList<CloudAppData>? = null
                            group.AppList?.let { appList ->

                                consentedAppData = ArrayList<CloudAppData>()

                                for (app in appList) {

                                    val consentedApp = CloudAppData(
                                            app.appName ?: "",
                                            app.appVersionNumber ?: "")

                                    consentedAppData?.add(consentedApp)
                                }
                            }

                            val programData = ProgramData(
                                    group.programName ?: "",
                                    group.program_id ?: "",
                                    CloudSessionState.shared.activeProfileID,
                                    consentedAppData,
                                    group.invitationCode ?: "")

                            programList.add(programData)
                        }
                    }
                }
            }
        }

        didGetUserProgramList?.invoke(errorCode, errorDetails, programList)
    }

    /**
     * This method is the callback for the Accept Invitation DHP API call.
     * It parses the JSON, and invokes the corresponding delegate method.
     * @param response: Contains the response body, message codes and indication of success.
     */
    private fun acceptInvitationCompleted(response: GenericDHPResponse<DHPResponseBody>) {

        var careProgramErrorCode = CareProgramErrorCode.UNKNOWN
        if (response.body?.responseMessageCode != null) {
            careProgramErrorCode = CareProgramErrorCode.fromDHPResponseCode(response.body?.responseMessageCode!!)
        }

        val errorDetails = CareProgramErrorDetail.toList(response.body?.errorDetails)

        didAcceptInvitation?.invoke(careProgramErrorCode, errorDetails, dhpRequestQueueManager.invitationDetailsToAccept)
        dhpRequestQueueManager.invitationDetailsToAccept = null
    }

    /**
     * This method is the callback for the Consent Opt Out DHP API call.
     * It parses the JSON, and invokes the corresponding delegate method.
     * @param response: Contains the response body, message codes and indication of success.
     */
    private fun consentOptOutCompleted(response: GenericDHPResponse<DHPResponseBody>) {

        var careProgramErrorCode = CareProgramErrorCode.UNKNOWN
        if (response.body?.responseMessageCode != null) {
            careProgramErrorCode = CareProgramErrorCode.fromDHPResponseCode(response.body?.responseMessageCode!!)
        }

        val errorDetails = CareProgramErrorDetail.toList(response.body?.errorDetails)

        didLeaveProgram?.invoke(careProgramErrorCode, errorDetails, dhpRequestQueueManager.programIdToOptOut ?: "") // dhpRequestQueueManager.programIdToOptOut
        dhpRequestQueueManager.programIdToOptOut = null
    }


    // Private Request Creator Methods

    /**
     * Build the payload for getInvitationDetails API from the DHP.
     * @param invitationCode: This parameter is the invitation code to use to get Program details.
     */
    private fun createGetInvitationDetailsRequest(invitationCode: String) {

        logger.log(VERBOSE, "createGetInvitationDetailsRequest")

        // Clear previous response.
        lastInvitationDetails = InvitationDetails()

        val api = DHPAPIs.getInvitationDetails
        val payload = DHPGetInvitationDetails()
        payload.invokingRole = CloudSessionState.shared.activeInvokingRole
        payload.invitationCode = invitationCode
        payload.apiExecutionMode = DHPCodes.ApiExecutionMode.synchronous

        if (payload.invokingRole == DHPCodes.Role.guardian) {
            payload.invokingExternalEntityID = DHPSession.shared.federationId
            payload.patientExternalEntityID = CloudSessionState.shared.activeProfileID
        } else {
            payload.invokingExternalEntityID = CloudSessionState.shared.activeProfileID
        }

        payload.invitationDetails = createInvitationDetails(api.messageId)

        var getInvitationDetailsRequest = GenericDHPRequest(api, payload, DHPGetInvitationDetailResponseBody::class, this::getInvitationDetailsCompleted)
        getInvitationDetailsRequest.prependObjectName = true
        add(getInvitationDetailsRequest)

    }

    /**
     * Build the payload for Get User Program App List API from the DHP.
     */
    private fun createGetUserProgramAppListRequest() {

        logger.log(VERBOSE, "createGetUserProgramAppListRequest")

        val api = DHPAPIs.getUserProgramAppList

        val payload = DHPGetUserProgramAppList()

        payload.invokingRole = CloudSessionState.shared.activeInvokingRole

        // If invokingRole is Guardian set invokingExternalEntityID to FederationId, otherwise to dependent's external entity ID.
        if (payload.invokingRole == DHPCodes.Role.guardian) {

            payload.invokingExternalEntityID = DHPSession.shared.federationId

            // When a Guardian is calling this API on behalf of a patient, an additional field
            // "patientExternalEntityID" needs to be included in the payload.
            payload.patientExternalEntityID = CloudSessionState.shared.activeProfileID
        } else {

            payload.invokingExternalEntityID = CloudSessionState.shared.activeProfileID
        }

        payload.apiExecutionMode = DHPCodes.ApiExecutionMode.synchronous
        payload.getProgramAppList = createDHPGetProgramAppList(api.messageId)

        var getUserProgramAppListRequest = GenericDHPRequest(api, payload, DHPGetUserProgramAppListResponseBody::class, this::getUserProgramAppListCompleted)
        getUserProgramAppListRequest.prependObjectName = true

        add(getUserProgramAppListRequest)
    }

    /**
     * Creates a DHPGetProgramAppList object for the getUserProgramAppList payload.
     * @param messageID: the message ID of the request.
     * @return DHPGetProgramAppList
     */
    private fun createDHPGetProgramAppList(messageId: String): DHPGetProgramAppList {

        val dhpGetProgramAppList = DHPGetProgramAppList()

        dhpGetProgramAppList.externalEntityID = CloudSessionState.shared.activeProfileID

        // This API is patient-centric.
        dhpGetProgramAppList.role = DHPCodes.Role.patient

        dhpGetProgramAppList.addCommonAttributes(messageId, withSourceTime = true)

        // nil out the common attributes that are not used by this API.
        dhpGetProgramAppList.UUID = null
        dhpGetProgramAppList.dataEntryClassification = null

        return dhpGetProgramAppList
    }

    /**
     * Creates a DHPInvitationDetails object for the getInvitationDetails payload.
     * @param messageId: the id for the message
     * @return DHPInvitationDetails
     */
    private fun createInvitationDetails(messageId: String): DHPInvitationDetails {

        val invitationDetails = DHPInvitationDetails()
        invitationDetails.externalEntityID = CloudSessionState.shared.activeProfileID
        invitationDetails.role = DHPCodes.Role.patient

        invitationDetails.addCommonAttributes(messageId, withSourceTime = true)

        invitationDetails.dataEntryClassification = null

        return invitationDetails
    }

    /**
     * Creates a DHPConsentInfo object that wraps the "consent_info" object for a DHP request payload.
     * @param messageID: the message ID of the request
     * @param status: the consent status
     * @return DHPConsentInfo
     */
    private fun createDHPConsentInfo(messageId: String, programId: String, status: String): DHPConsentInfo {
        val timeService: TimeService = DependencyProvider.default.resolve()
        val nowNormalizedDate = timeService.today()
        val nowGMTNormalizedDateString = nowNormalizedDate.atStartOfDay(ZoneId.of("GMT")).toInstant().toGMTString(false)

        val consentInfo = DHPConsentInfo()

        var termsAndConditions: String
        var privacyNotice: String
        var consentEnabledStatus: String
        var historicalDataIndicator = "false"

        if (status == "Active") {
            termsAndConditions = "termsAndConditions"
            privacyNotice = "privacyNotice"
            consentEnabledStatus = "true"
            consentInfo.consentStartDate = nowGMTNormalizedDateString
        } else {
            termsAndConditions = "termsAndConditions"
            privacyNotice = "privacyNotice"
            consentEnabledStatus = "false"
            consentInfo.consentEndDate = nowGMTNormalizedDateString
            historicalDataIndicator = "false"
        }

        consentInfo.addCommonAttributes(messageId, withSourceTime = true)
        consentInfo.externalEntityID = CloudSessionState.shared.activeProfileID
        consentInfo.role = DHPCodes.Role.patient
        consentInfo.username = DHPSession.shared.username
        consentInfo.programID = programId
        consentInfo.termsAndConditions = termsAndConditions
        consentInfo.privacyNotice = privacyNotice
        consentInfo.privacyNoticeReadIndicator = "true"
        consentInfo.status = status
        consentInfo.acceptIndicator = "true"
        consentInfo.historicalDataIndicator = historicalDataIndicator
        consentInfo.consentAuditableEventDate = nowGMTNormalizedDateString
        consentInfo.consentEnabledStatus = consentEnabledStatus

        val serverTimeOffsetString = (CloudSessionState.shared.serverTimeOffset?:0).toServerTimeOffsetString()
        consentInfo.serverTimeOffset = serverTimeOffsetString
        consentInfo.consentType = DHPCodes.ConsentType.shareDataStatement
        consentInfo.dataEntryClassification = DHPCodes.DataEntryClassification.manual

        return consentInfo

    }

    /**
     * Build the payload for Accept Invitation API from the DHP.
     * @param invitationDetails: This parameter is the invitation details used to enroll in a Program.
     */
    private fun createAcceptInvitationRequest(invitationDetails: InvitationDetails) {

        logger.log(VERBOSE, "createAcceptInvitationRequest")

        val api = DHPAPIs.acceptInvitation

        val payload = DHPAcceptInvitation()

        payload.invokingRole = CloudSessionState.shared.activeInvokingRole

        // If invokingRole is Guardian set invokingExternalEntityID to FederationId, otherwise to dependent's external entity ID.
        if (payload.invokingRole == DHPCodes.Role.guardian) {

            payload.invokingExternalEntityID = DHPSession.shared.federationId
            payload.patientExternalEntityID = CloudSessionState.shared.activeProfileID
        } else {

            payload.invokingExternalEntityID = CloudSessionState.shared.activeProfileID
        }

        payload.invitationCode = invitationDetails.invitationCode
        payload.apiExecutionMode = DHPCodes.ApiExecutionMode.synchronous

        payload.invitation = createDHPInvitation(
                api.messageId,
                invitationDetails.programId,
                invitationDetails.invitationType,
                invitationDetails.programSupportedUserApps)
        payload.consent_info = createDHPConsentInfo(api.messageId, invitationDetails.programId,"Active")

        var acceptInvitationRequest = GenericDHPRequest(api, payload, DHPResponseBody::class, this::acceptInvitationCompleted)
        acceptInvitationRequest.prependObjectName = true

        dhpRequestQueueManager.invitationDetailsToAccept = invitationDetails
        add(acceptInvitationRequest)
    }

    /**
     * Creates a DHPInvitationDetails object for the getInvitationDetails payload.
     * @param messageID: the message ID of the request
     * @param programId: the ID of the program
     * @param invitationType: the type
     * @param consentedApps: list of the apps that have consented
     * @return DHPInvitation
     */
    private fun createDHPInvitation(messageId: String, programId: String, invitationType: String, consentedApps: List<CloudAppData>) : DHPInvitation {

        val invitation = DHPInvitation()

        // Common properties shared by all objects in FHIR
        invitation.addCommonAttributes(messageId = messageId, withSourceTime = true)

        // Invitation object properties
        invitation.emailID = DHPSession.shared.username
        invitation.acceptorExternalEntityID = CloudSessionState.shared.activeProfileID
        invitation.programID = programId
        invitation.invitationType = invitationType

        // This value should always be “used”
        invitation.status = DHPCodes.Status.used.value
        invitation.roleOfAcceptor = if (CloudSessionState.shared.activeInvokingRole == DHPCodes.Role.guardian) DHPCodes.RoleOfAcceptor.guardian else DHPCodes.RoleOfAcceptor.patient

        val timeService: TimeService = DependencyProvider.default.resolve()
        val now = timeService.now()
        val nowGMTString = now.toGMTString(false)
        val nowTimezeone = now.toGMTOffset()

        invitation.acceptedDate_GMT = nowGMTString
        invitation.acceptedDate_TZ = nowTimezeone

        val serverTimeOffsetString = (CloudSessionState.shared.serverTimeOffset?:0).toServerTimeOffsetString()

        invitation.serverTimeOffset = serverTimeOffsetString
        invitation.consentedAppDetails = consentedApps.map { ConsentedAppDetail(consentedAppName = it.appName, consentedAppVersionNumber = it.appVersionNumber) }


        return invitation
    }

    /**
     * Build the payload for Consent Opt Out request API from the DHP. This is used to leave a Program.
     * @param programId: This parameter is the Program to leave.
     */
    private fun createConsentOptOutRequest(programId: String) {

        val api = DHPAPIs.consentOptOut

        val payload = DHPOptOutDataSharing()
        payload.invokingExternalEntityID = DHPSession.shared.federationId
        payload.invokingRole = CloudSessionState.shared.activeInvokingRole
        if (payload.invokingRole == DHPCodes.Role.guardian) {
            payload.patientExternalEntityID = CloudSessionState.shared.activeProfileID
        }
        payload.apiExecutionMode = DHPCodes.ApiExecutionMode.synchronous
        payload.consent_info = createDHPConsentInfo(api.messageId, programId, "Withdrawn")

        var consentOptOutRequest = GenericDHPRequest(api, payload, DHPResponseBody::class, this::consentOptOutCompleted)
        consentOptOutRequest.prependObjectName = true

        dhpRequestQueueManager.programIdToOptOut = programId
        add(consentOptOutRequest)

    }

    // Private Helper Methods

    /**
     * This method adds the the passed-in DHP Request to the DHPRequestQueueManager.
     * The DHPRequestQueueManager executes the request if there are no outstanding requests.
     * Otherwise, the request is enqueued.
     * @param dhpRequest: This parameter contains the DHP API request data.
     */
    private fun add(dhpRequest: GenericDHPRequest<*>) {
        dhpRequestQueueManager.add(dhpRequest)
    }

    private inline fun <reified T: Any> getValue(map: Map<String, Any>, keyPath: String, key: String): T {

        val fullKeyPath = keyPath + key

        val valueOptional = map[fullKeyPath] ?: throw Exception ("Could not find $fullKeyPath in map .")

        val typedValue = valueOptional as? T ?: throw Exception("Could not cast $fullKeyPath as ${T::class.simpleName}")

        return typedValue
    }

}

// data classes

/**
This data class contains information about one App, including App name, and version number.
 */
data class PatientAppInfo(internal var appName: String = "", internal var appVersionNumber: String = "")

/**
This data class contains information about one App consented to share data with a specific Program.
 */
data class UserProgramApp(var appName: String = "", var appVersionNumber: String = "", var consentAcceptDateString: String = "")

/**
This data class contains the response fields from the Get User Program App List API request.
 */
data class UserProgramAppListItem(var programId: String = "", var programName: String = "", var invitationCode: String, var groupName: String = "", var active: Boolean = false, var groupReference: String = "", var appList: List<UserProgramApp>?)

