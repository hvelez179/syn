//
// CareProgramErrorDetail.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.services.programmanagement


/**
 * This enum contains enhanced error codes associated with DHP error code "113".
 */
enum class CareProgramErrorDetail {

    ALREADY_ACCEPTED_INVITATION_CODE,
    ALREADY_ACCEPTED_SIMILAR_INVITATION,
    MISSING_MANDATORY_FIELD,
    EXPIRED_INVITATION_CODE,
    INVALID_VALUE_FOR_FIELD,
    UNKNOWN;

    companion object {
        fun fromErrorDetailString(errorDetailString: String): CareProgramErrorDetail {
            return when (errorDetailString) {
            // Accept invitation request is being sent when the invitation is already Accepted. Returned by ->
            //    M018 – Accept Invitation
                "113–001" -> ALREADY_ACCEPTED_INVITATION_CODE

            // Missing mandatory invitation code.
            //    M017 – Add Invitation
            //    M018 – Accept Invitation
            //    M019 – Remove Invitation
                "113–002" -> MISSING_MANDATORY_FIELD

            // Missing mandatory apiExecutionMode. Returned by ->
            //    All APIs
                "113–003" -> MISSING_MANDATORY_FIELD

            // Missing Mandatory resource in the payload. Returned by ->
            // TBD As of Sprint 7. TODO -> bjk Check Mobile API doc table 5.2.
                "113–004" -> MISSING_MANDATORY_FIELD

            // Recipient has already accepted similar invitation earlier. Returned by ->
            //    M017 – Add Invitation
                "113–005" -> ALREADY_ACCEPTED_SIMILAR_INVITATION

            // Invitation has already expired. Returned by ->
            //    M018 – Accept Invitation
            //    M019 – Remove Invitation
                "113–006" -> EXPIRED_INVITATION_CODE

            // Missing mandatory programId. Returned by ->
            //    M017 – Add Invitation
            //    M018 – Accept Invitation
            //    M019 – Remove Invitation
                "113–007" -> MISSING_MANDATORY_FIELD

            // Missing mandatory dataEntryClassification. Returned by ->
            //    All Data Ingestion APIs.
                "113–008" -> MISSING_MANDATORY_FIELD

            // Invalid value for field apiExecutionMode. Returned by ->
            //    All APIs.
                "113–009" -> INVALID_VALUE_FOR_FIELD

            // Invalid value for field dataEntryClassification. Returned by ->
            //    All Data Ingestion APIs.
                "113–010" -> INVALID_VALUE_FOR_FIELD

            // Unknown error detail
                else -> UNKNOWN
            }
        }

        /**
         * Creates a list of CareProgramErrorDetail enum cases corresponding to the errorDetails received from the DHP.
         * @param errorDetails: List of errorDetails received from a DHP API response.
         * @return: List of CareProgramErrorDetail enum cases corresponding to the errorDetails.
         */
        fun toList(errorDetails: List<String>?): List<CareProgramErrorDetail>? {
            val errorDetails = errorDetails
            if (errorDetails != null) {
                var careProgramErrorDetails: MutableList<CareProgramErrorDetail> = mutableListOf()
                for (errorDetail in errorDetails) {
                    val careProgramErrorDetail = fromErrorDetailString(errorDetail)
                    if (careProgramErrorDetail != null) {
                        if (!careProgramErrorDetails.any { it == careProgramErrorDetail }) {
                            careProgramErrorDetails.add(careProgramErrorDetail)
                        }
                    } else {
//                        val logger = Logger("Teva_Cloud")
                        val errorString = "Unknown DHP enhanced error code ${errorDetail}"
//                        logger.log(LogLevel.error, "${errorString}")
                        careProgramErrorDetails.add(CareProgramErrorDetail.UNKNOWN)
                    }
                }
                return careProgramErrorDetails.toList()
            }
            return null
        }
    }
}