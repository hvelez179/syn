//
// CareProgramErrorCode.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.services.programmanagement

import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService


/**
 * This enum contains error codes associated with Care Program.
 * noError: Request was successful
 * invalidInvitationCode: Request invitation code is not valid format, or does not exist.
 * invalidProgramName: Requested program name does not exist.
 * invalidProfile: Requested profile does not exist.
 */
enum class CareProgramErrorCode {
    NO_ERROR,
    NO_INTERNET_CONNECTION,
    INVALID_INVITATION_CODE,
    EXPIRED_INVITATION_CODE,
    INVITATION_ALREADY_ACCEPTED,
    INVALID_PROGRAM_NAME,
    INVALID_PROFILE,
    ALREADY_ENROLLED_IN_PROGRAM,
    APP_NOT_SUPPORTED_BY_PROGRAM,
    GET_INVITATION_DETAILS_REQUEST_FAILED,
    API_REQUEST_FAILED,
    API_REQUEST_FAILED_PAYLOAD_VALIDATION,
    UNKNOWN;

    /**
     * This will take the ErrorCode and return the appropriate string to return for error messages
     */
    fun toString(errorDetails: List<CareProgramErrorDetail>?) : String? {
        var returnValue: String?
        val localizationService: LocalizationService = DependencyProvider.default.resolve()
        if (this == CareProgramErrorCode.NO_ERROR) {
            returnValue = null
        } else if (CareProgramErrorCode.errorCodeToTextResourceMap[this] != null) {
            returnValue = localizationService.getString(CareProgramErrorCode.errorCodeToTextResourceMap[this]!!)
        } else {
            returnValue = localizationService.getString("addCareProgramUnknownError_text")
        }
        return returnValue
    }

    companion object {

        val errorCodeToTextResourceMap: Map<CareProgramErrorCode, String> = mapOf(
                CareProgramErrorCode.API_REQUEST_FAILED to "apiRequestFailedError_text",
                CareProgramErrorCode.API_REQUEST_FAILED_PAYLOAD_VALIDATION to "apiRequestFailedPayloadValidationError_text",
                CareProgramErrorCode.EXPIRED_INVITATION_CODE to "addCareProgramInvitationCodeExpired_text",
                CareProgramErrorCode.INVITATION_ALREADY_ACCEPTED to "addCareProgramInvitationCodeAlreadyAccepted_text",
                CareProgramErrorCode.GET_INVITATION_DETAILS_REQUEST_FAILED to "addCareProgramInvitationCodeError_text",
                CareProgramErrorCode.INVALID_INVITATION_CODE to "addCareProgramInvitationCodeError_text",
                CareProgramErrorCode.INVALID_PROGRAM_NAME to "invalidProgramNameError_text",
                CareProgramErrorCode.INVALID_PROFILE to "invalidProfileError_text",
                CareProgramErrorCode.ALREADY_ENROLLED_IN_PROGRAM to "alreadyAcceptedSimilarInvitation_text",
                CareProgramErrorCode.APP_NOT_SUPPORTED_BY_PROGRAM to "addCareProgramInvitationCodeError_text",
                CareProgramErrorCode.UNKNOWN to "addCareProgramUnknownError_text")

        /**
         * Creates a CareProgramErrorCode value from the Response Message Code from a DHP API response.
         *
         * @param dhpResponseCode: Response Message Code from a DHP API response.
         * @return: a CareProgramErrorCode corresponding to the response code.
         */
        fun fromDHPResponseCode(dhpResponseCode: String): CareProgramErrorCode {
            return when (dhpResponseCode) {
            // Request processed successfully
                "110" -> CareProgramErrorCode.NO_ERROR

            // Failure -> The request could not be processed.
                "112" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. Payload failed validation
                "113" -> CareProgramErrorCode.API_REQUEST_FAILED_PAYLOAD_VALIDATION

            // Identifier included in the payload is missing or blank
                "114" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Source Time or System Time falls outside the consented date
                "115" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Payload rejected as the requestor is Opted out
                "116" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. Consent is already in Opt-In state
                "117" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. Consent is already in Opt-Out state
                "118" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. Existing Consent information could not be retrieved
                "119" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. Error encountered while checking Consent
                "120" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Exception encountered during Message Processing" ->  Contact Support
                "121" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Failed parsing the payload or Undefined Error encountered" ->  Contact Support
                "122" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request did not include any resource to upload
                "123" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. Requestor is not active
                "124" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. Error encountered while updating Consent
                "125" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Invalid format used in the payload for source time or source time zone
                "126" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Error encountered while retrieving the Identifier
                "127" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. No active Pre-registration data found against the Payload
                "128" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. Duplicate Registration requested
                "129" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. Patient register failed
                "130" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. Mobile Device registration failed
                "131" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request was partially processed. Some weather details could not be retrieved based the input request.
                "132" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. No weather details were retrieved based on the input request.
                "133" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. Requester is not yet Registered
                "134" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. Call to DHP API failed. Contact Support
                "135" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. Call to FHIR API failed. Contact Support
                "136" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. Identifier included in the payload is invalid
                "137" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Application details in the input request is configured for Study and Site that does not match Patient's profile
                "138" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. Invitation code included in payload is invalid.
                "139" -> CareProgramErrorCode.INVALID_INVITATION_CODE

            // The timestamp in the input payload is greater than current timestamp
                "140" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. No Mobile Device/App found for this UUID.
                "141" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. The UUID details in the payload do not match with all the resources
                "142" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. Payload failed mandatory security checks.
                "143" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. Requestor's role is invalid or missing from the payload.
                "144" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Success - Request processed successfully
                "200" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Matching Document was found
                "201" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Matching Document was not found
                "202" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Warning - Request could not be processed.
                "300" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Error - Request could not be processed
                "400" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. Patient Identifier is Invalid
                "401" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. Call to FHIR API Failed
                "402" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. The App details in the payload do not match or does not correspond to a commercial application
                "403" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Request could not be processed. The App details in the payload do not match or does not correspond to a clinical study application
                "404" -> CareProgramErrorCode.API_REQUEST_FAILED

            // Failure" ->  The request is not processed
                "406" -> CareProgramErrorCode.API_REQUEST_FAILED

                else -> CareProgramErrorCode.UNKNOWN
            }
        }
    }
}