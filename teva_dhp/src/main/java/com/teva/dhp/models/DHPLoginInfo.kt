//
// DHPLoginInfo.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.models

class DHPLoginInfo {
    /**
     * This property is the Client ID to pass to Identity Hub.
     */
    var clientId: String = ""

    /**
     * This property is the base registration URL for Identity Hub.
     */
    var identityHubRegistrationURL: String = ""

    /**
     * This property is the base URL to login to Identity Hub.
     */
    var identityHubLoginURL: String = ""

    /**
     * This property is the URL to look for in the browser when OAuth login is successful.
     */
    var identityHubLoginSuccessRedirectURL: String = ""

    /**
     * This property is the base OAuth Authorization URL for Identity Hub.
     */
    var identityHubAuthorizationURL: String = ""

    /**
     * This property is the URL to look for in the browser when OAuth Authorization is successful.
     */
    var identityHubAuthorizationSuccessRedirectURL: String = ""

    /**
     * This property is the URL to refresh the Identity Hub token.
     */
    var identityHubRefreshURL: String = ""

    /**
     * This property is the URL to login to with the DHP.
     */

    var dhpLoginURL: String = ""

    /**
     * This property is the base URL for the DHP API.
     */
    var DHPAPIURL: String = ""
}