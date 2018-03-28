//
// CloudConstants.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.models

import com.teva.dhp.models.DHPLoginInfo

/**
 * The cloud constants.
 */
object CloudConstants {
    /** Maccabi
    val identityHubRegistrationURL: String = "https://test-mtl.cs80.force.com/RegisterTEV?siteid=SID-01234&lang=en_US"
    val identityHubLoginURL: String = "https://test-mtl.cs80.force.com/LoginTEV?siteid=SID-01234&lang=en_US"
    val identityHubLoginSuccessRedirectURL: String = "https://test.salesforce.com/services/oauth2/registration"
    val identityHubAuthorizationURL: String = "https://test-mtl.cs80.force.com/services/apexrest/identity/oauth2/authorize/SID-01234"
    val identityHubAuthorizationSuccessRedirectURL: String = "https://test.salesforce.com/services/oauth2/success"
    val identityHubRefreshURL: String = "https://test-mtl.cs80.force.com/services/oauth2/token"
    val clientId: String = "3MVG9rKhT8ocoxGk1EnFKTfvMvXwscKar0My7xjvhxXydqlGUIoJThi_ZontE69oagtCsUkFtUj2DF.S9zFAP"
     **/
    /** GxP Dev Commercial **/

    // This is the URL of the registration web page for the configured ID Hub site ID.
    val identityHubRegistrationURL: String = "https://sit-mtl.cs85.force.com/RegisterTEV?siteid=SID-15890&lang=en_US"

    // This is the URL of the login web page for the configured ID Hub site ID.
    val identityHubLoginURL: String = "https://sit-mtl.cs85.force.com/LoginTEV?siteid=SID-15890&lang=en_US"

    // This is the URL Identity Hub will redirect to after the user is successfully authenticated.
    // It is configured for the ID Hub site ID and can be set to anything we specify.
    // When this URL is reached, that app need to redirect to the OAuth Authorization URL to get tokens.
    val identityHubLoginSuccessRedirectURL: String = "https://test.salesforce.com/services/oauth2/registration"

    // This is the OAuth Authorization BASE URL for the configured ID Hub site ID.
    // In order to function properly, it must be appended with the expected query string parameters.
    val identityHubAuthorizationURL: String = "https://sit-mtl.cs85.force.com/services/apexrest/identity/oauth2/authorize/SID-15890"

    // This is the URL the Identity Hub OAuth Authorization URL will redirect to if the user has access to the client application provided.
    // This URL is provided as a query string parameter along with the identityHubAuthorizationURL.
    // When the browser redirects to this this success URL, it will include the tokens we need in the query string.
    val identityHubAuthorizationSuccessRedirectURL: String = "https://test.salesforce.com/services/oauth2/success"

    // This URL is used to refresh the Identity Hub access token using the Identity Hub Refresh token.
    val identityHubRefreshURL: String = "https://sit-mtl.cs85.force.com/services/oauth2/token"

    // This is the client ID, representing the DHP to Identity Hub.
    val clientId: String = "3MVG9X0_oZyBSzHr1dCvkIxSNs69CbrBv5hKh_Do1Y2vYw4Q1Nx2EQ4TuviI.05e3aou5IYh1GGXVe7Tx9S_d"

    // This is the URL used to log in with the DHP by providing the clientID and Identity Hub ID token.
//    val dhpLoginURL: String = "https://api-dev.dhp.ehealth.teva:9444/oauth/token"
    val dhpLoginURL: String = "https://api-int.dhp.ehealth.teva:9443/oauth/token"

    // This is the Base URL of the DHP API.
//    val dhpAPIURL: String = "https://api-dev.dhp.ehealth.teva:8444"
    val dhpAPIURL: String = "https://api-int.dhp.ehealth.teva:8443"

    // Cloud based DHP Simulator
    //val dhpAPIURL: String = "https://dhpsimulator.herokuapp.com"

    // Syncro network DHP Simulator (this may not always be running)
    //val dhpAPIURL: String = "http://10.10.0.164:8080"

    /** GxP QA
    val clientId: String = "3MVG9rKhT8ocoxGk1EnFKTfvMvUL0LFaFTeIuY6Mdb3LslIhfNHU8xZNHQgFEvZBAJ11wchH7QLV7c9IdeEsg"
     */

    /**
     * Container for DHP Login Items
     */
    var loginInfo =  DHPLoginInfo()

    init {
        initializeLoginInfo()
    }

    private fun initializeLoginInfo() {
        loginInfo.clientId = CloudConstants.clientId
        loginInfo.identityHubRegistrationURL = CloudConstants.identityHubRegistrationURL
        loginInfo.identityHubLoginURL = CloudConstants.identityHubLoginURL
        loginInfo.identityHubLoginSuccessRedirectURL = CloudConstants.identityHubLoginSuccessRedirectURL
        loginInfo.identityHubAuthorizationURL = CloudConstants.identityHubAuthorizationURL
        loginInfo.identityHubAuthorizationSuccessRedirectURL = CloudConstants.identityHubAuthorizationSuccessRedirectURL
        loginInfo.identityHubRefreshURL = CloudConstants.identityHubRefreshURL
        loginInfo.dhpLoginURL = CloudConstants.dhpLoginURL
        loginInfo.DHPAPIURL = CloudConstants.dhpAPIURL
    }

    val commercialAppName: String = "eProAir"
    val commercialAppVersionNumber: String = "1.0"
    val clinicalAppName: String = "TevaPTPS"
    val clinicalAppVersionNumber: String = "1.0"

    val tevaAppInstallationUUIDKey = "tevaAppInstallationUUID"

    val lastSuccessfulSyncDateKey = "lastSuccessfulSyncDate"
    val lastFailedSyncDateKey = "lastFailedSyncDate"

    /**
     * The initial download on a 2nd device may fail because the new device has not been asyncronously processed by DHP yet.
     * If the download fails, we will wait a couple seconds, then try again, up to a certain number of attempts.
     * The error returned is not specific to this issue, so it's possible something else caused the failure, and we should not retry indefinitely.
     */
    val maxInitialDownloadRetryAttempts = 10

    /**
     * The number of objects to download from the CloudService before merging cached objects into the database.
     */
    val downloadObjectCountThreshold: Int = 150

    /**
     * A time interval to wait between an upload and a download.
     */
    val initialDownloadRetryDelayInSeconds: Long = 3L

    /**
     * A time interval applied to upload objects to account for minor time differences between client and server.
     * If the source time sent to the server is a second ahead of the server's time, the object will not be accepted.
     */
    val sourceTimeOffset: Long = -2L

    /**
     * The max acceptable serverTimeOffset, in seconds. If a serverTimeOffset is determined and is greater than this,
     * the user will be notified via a banner message.
     */
    val maxAcceptableOffsetInSeconds: Int = 60

    /**
     * The offset value to store in the database when the offset can never be determined.
     */
    val unknownOffsetValue: Int = 999999999

    /**
     * This is the value added to server time to ensure that valid upload data does not get excluded.
     */
    val acceptableServerTimeDifferenceForUpload: Long = 5
}