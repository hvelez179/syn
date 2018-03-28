//
//  DHPConstants.kt
//  Teva_DHP
//
//  Copyright © 2017 Teva. All rights reserved.
//

package com.teva.dhp.models

/**
 Constant values used in the DHP project.
 */
internal class DHPConstants {

    companion object {
        /**
        *The URL to get Identity Hub public key data from.
        */
	    internal val salesForceKeysUrl: String = "https://login.salesforce.com/id/keys"

        /**
        *The Identity Hub response type.
        */
	    internal val responseType = "token id_token refresh_token"

        /**
        *The Identity Hub scope.
        */
	    internal val scope = "openid refresh_token"

        /**
        *The Identity Hub display type.
        */
	    internal val displayType = "touch"

		/**
		The Identity Hub prompt.
		 */
		internal val prompt = "login consent"

        /**
        *The max objects that can be sent in the upload API.
        */
	    internal val maxUploadObjects = 100

        /**
        *The tag to use in storing the Identity Hub public key in the keychain.
        */
	    internal val identityHubPublicKeyTag = "IdentityHubPublicKey"

        /**
        *Messages describing possible responses from the DHP.
        */
	    internal val dhpMessageSuccess = "Success with Warning"
	    internal val dhpMessagePayloadFailedValidation = "Payload failed validation"
	    internal val dhpMessageConsentAlreadyOptedIn = "Consent has already consented to syncing with the cloud"
	    internal val dhpMessagePatientAlreadyRegistered = "Patient has already been registered with DHP"
	    internal val dhpMessageWeatherPartiallyProcessed = "Some weather details could not be retrieved"
    }
}
