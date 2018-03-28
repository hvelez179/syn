//
//  DHPAPIs.kt
//  Teva_Cloud
//
//  Copyright © 2017 Teva. All rights reserved.
//

package com.teva.dhp.models

/**
 Contains DHP API Message IDs and URIs.
 */
class DHPAPIs {

    class DHPAPI(var id: Int, var uri: String) {

        val messageId: String

        init {

            /**
             * Takes an integer and creates a message code understood by the DHP (e.g. 1 = M001).
             */
            val idstring = "00$id"
            this.messageId = "M${idstring.substring(idstring.length - 3)}"

            /**
             * Takes a DHP API name and prefixes it with the API path.
             */
            this.uri = "/dhp/api/v2/${uri}"
        }
    }
    companion object {
        /**
         *Prescription
         */
        val addMedicationOrder = DHPAPI(1, "patients/medicationOrder/add")

        /**
         *Inhalation event
         */
        val addMedicationAdministration = DHPAPI(2, "patients/medicalDevice/medicationAdministration/add")

        /**
         *Inhaler
         */
        val registerMedicalDevice = DHPAPI(3, "patients/medicalDevice/register")
        val consentOptIn = DHPAPI(4, "patients/optInDataSharing")
        val consentOptOut = DHPAPI(4, "patients/optOutDataSharing")
        val storeCoachingDetails = DHPAPI(5, "patients/coachingDetail")
        val saveSurveyResponse = DHPAPI(6, "patients/questionnaireResponse")
        val saveUserPreferences = DHPAPI(7, "patients/userPreferences")
        val updateMedicalDeviceDetails = DHPAPI(8, "patients/medicalDevice/update")

        /**
         *Inhalation event
         */
        val updateMedicationAdministration = DHPAPI(9, "patients/medicalDevice/medicationAdministration/update")
        val modifyPatientProfile = DHPAPI(10, "patients/update")

        /**
         *Syncronize Data between the mobile device and the WH platform - Data Upload
         */
        val syncDataUpload = DHPAPI(11, "patients/dataSynchronization")

        /**
         *Syncronize Data between the mobile device and the WH platform - Data Retrieval
         */
        val syncDataDownload = DHPAPI(13, "patients/retrieval")

        /**
         *Store and Retrieve Geo Location based Weather details
         */
        val weatherDetails = DHPAPI(14, "patients/weather")

        /**
         *Gets the current DHP server time, in UTC
         */
        val getServerTime = DHPAPI(16, "getServerTime")

        /**
         *Register a patient and a profile, with consent and mobile device info
         */
        val addProfile = DHPAPI(22, "profiles/addProfile")

        /**
        Register a patient and a profile, with consent and mobile device info
         */
        val getRolesForProfile = DHPAPI(29, "profiles/getRolesForProfile")

        /**
        Get a list of prescriptions for a patient
         */
        val getPrescriptions = DHPAPI(35, "patients/medicationOrder/getList")

        /**
        Get a list of medical devices for a patient
         */
        val getDevices = DHPAPI(36, "patients/medicalDevice/getList")

        /**
        Gets dependent profiles
         */
        val getRelatedProfilesList = DHPAPI(27, "profiles/getRelatedProfilesList")

        /**
         This API gets Invitation details from the DHP.
         */
        val getInvitationDetails = DHPAPI(21, "profiles/getInvitationDetails")

        /**
        This API gets User Program App List from the DHP.
         */
        val getUserProgramAppList = DHPAPI(24, "patients/getUserProgramAppList")

        /**
        This API gets Get Mobile app list for a Patient from the DHP.
         */
        val getPatientAppList = DHPAPI(30, "patients/getPatientAppList")

        /**
         This API accepts an invitation.
         */
        val acceptInvitation = DHPAPI(18, "profiles/acceptInvitation")

        /**
         API to upload mobile device info.
         */
        val addMobileDevice = DHPAPI(32, "patients/mobileDevice/add")

        /**
         API to add a dependent to a profile
         */
        val addDependentPatient = DHPAPI(39, "profiles/addDependentPatient")

    }
}