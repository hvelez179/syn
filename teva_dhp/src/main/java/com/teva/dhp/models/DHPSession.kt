//
// DHPSession.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.models

class DHPSession {
    companion object {
        var shared = DHPSession()
    }

    /**
     * This property is the nonce used in the Identity Hub login URL to log in.
     * It is set internally when DHPManager.getLoginURL() is called, or
     * it can be set explicitly if the consumer creates the login URL seperately.
     */
    var identityHubLoginNonce: String = ""

    /**
     * This property is ID Token from Identity Hub.
     */
    var identityHubIDToken: String = ""

    /**
     * This property is Get Profile URL for Identity Hub.
     */
    var identityHubProfileURL: String = ""

    /**
     * This property stores the profile information returned by Identity Hub.
     */
    var identityHubProfile: Map<String, Any>? = mapOf<String, Any>()

    /**
     * This property is Access Token for Identity Hub.
     */
    var identityHubAccessToken: String = ""

    /**
     * This property is Refresh Token for Identity Hub.
     */
    var identityHubRefreshToken: String = ""

    /**
     * This property is Access Token for the DHP.
     */
    var dhpAccessToken: String = ""

    /**
     * This property is Refresh Token for the DHP.
     */
    var dhpRefreshToken: String = ""

    /**
     * This property is user's username, returned via their Identity Hub profile.
     */
    var username: String = ""

    /**
     * This property is user's federation ID, returned via their Identity Hub profile.
     */
    var federationId: String = ""

    /**
     * This property is user's clinical pseudo name ID, returned via their Identity Hub profile.
    This property is only set if isClinical.
     */
    var clinicalPseudoName: String = ""

    fun clear() {
        username = ""
        identityHubIDToken = ""
        identityHubProfile = null
        identityHubLoginNonce = ""
        identityHubProfileURL = ""
        identityHubAccessToken = ""
        identityHubRefreshToken = ""
        dhpAccessToken = ""
        dhpRefreshToken = ""
        federationId = ""
        clinicalPseudoName = ""
    }
}