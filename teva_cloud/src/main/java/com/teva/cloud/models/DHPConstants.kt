//
// DHPConstants.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.models


/**
 * Constant values used for the DHP.
 */
object DHPConstants {
    /**
     * Constant keys to use in JSON payloads to the DHP.
     */
    object JSONKeys {

        val FEDERATION_ID = "federationID"
        val ROLE = "role"
        val ROLE_OF_ACCEPTOR = "roleOfAcceptor"
        val ROLE_OF_SENDER = "roleOfSender"
        val INVOKING_EXTERNAL_ENTITY_ID = "invokingExternalEntityID"
        val INVOKING_ROLE = "invokingRole"
        val ACCEPTOR_EXTERNAL_ENTITY_ID = "acceptorExternalEntityID"
        val EXTERNAL_ENTITY_ID = "externalEntityID"
        val INVITATION_CODE = "invitationCode"
        val USER_NAME = "username"
        val PATIENT_STUDY_HASHKEY = "patientStudyHashKey"
        val PATIENT_PSEUDONAME = "patientPseudoName"
        val MESSAGE_ID = "messageID"
        val APP_NAME = "appName"
        val APP_VERSION_NUMBER = "appVersionNumber"
        val MOBILE_UUID = "UUID"
        val CREATED_DATE_GMT = "createdDate_GMT"
        val CREATED_DATE_TZ = "createdDate_TZ"
        val ACCEPTED_DATE_GMT = "acceptedDate_GMT"
        val ACCEPTED_DATE_TZ = "acceptedDate_TZ"
        val EXPIRATION_DATE_GMT = "expirationDate_GMT"
        val EXPIRATION_DATE_TZ = "expirationDate_TZ"
        val SERVER_TIME = "serverTime_GMT"
        val SOURCE_TIME = "sourceTime_GMT"
        val SOURCE_TIMEZONE = "sourceTime_TZ"
        val INHALER_SYNC_TIME = "inhalerSynchTime_GMT"
        val NON_INHALER_SYNC_TIME = "nonInhalerSynchTime_GMT"
        val OBJECT_NAME = "objectName"
        val DOCUMENT_STATUS = "documentStatus"
        val MOBILE_DEVICE_TIME_OFFSET = "mobileDeviceTimeOffset"
        val SERVER_TIME_OFFSET = "serverTimeOffset"
        val API_EXECUTION_MODE = "apiExecutionMode"
        val RETURN_OBJECTS = "returnObjects"

        val DATA_ENTRY_CLASSIFICATION = "dataEntryClassification"
        val DATA_ENTRY_CLASSIFICATION_AUTOMATIC = "automatic"
        val DATA_ENTRY_CLASSIFICATION_MANUAL = "manual"

        val INVITATION = "invitation"
        val INVITATION_DETAILS = "invitationDetails"
        val INVITATION_TYPE = "invitationType"

        val PROFILE_ID = "profileId"
        val PROGRAM_ID = "programID"
        val EMAIL_ID = "emailID"
    }

    /**
    The URL to get Identity Hub public key data from.
     */
    val salesForceKeysUrl: String = "https://login.salesforce.com/id/keys"

    /**
    The Identity Hub response type.
     */
    val responseType = "token id_token refresh_token"

    /**
    The Identity Hub scope.
     */
    val scope = "openid refresh_token"

    /**
    The Identity Hub display type.
     */
    val displayType = "touch"

    /**
    The Identity Hub prompt.
     */
    val prompt = "login consent"

    /**
    The max objects that can be sent in the upload API.
     */
    val maxUploadObjects = 100

    /**
    The tag to use in storing the Identity Hub public key in the keychain.
     */
    val identityHubPublicKeyTag = "IdentityHubPublicKey"

    /**
    Messages describing possible responses from the DHP.
     */
    val dhpMessageSuccess = "Success with Warning"
    val dhpMessagePayloadFailedValidation = "Payload failed validation"
    val dhpMessageConsentAlreadyOptedIn = "Consent has already consented to syncing with the cloud"
    val dhpMessagePatientAlreadyRegistered = "Patient has already been registered with DHP"
    val dhpMessageWeatherPartiallyProcessed = "Some weather details could not be retrieved"
}