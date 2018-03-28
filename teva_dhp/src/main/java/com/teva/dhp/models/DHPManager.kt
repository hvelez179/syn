//
//  DHPManager.kt
//  Teva_DHP
//
//  Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.models

import com.teva.dhp.DataEntities.DHPDataTypes.GenericDHPRequest
import com.teva.dhp.DataEntities.DHPDataTypes.GenericDHPRequestWithOldCallback
import com.teva.dhp.enumerations.LoginResult
import com.teva.dhp.enumerations.LoginState
import java.net.URL
import org.threeten.bp.Duration

/**
 The DHPManager is the main public interface of the Teva_DHP project, 
 used to login, logout, manage session data, and make all DHP API calls.
 */
interface DHPManager {

    /**
     This property is the DHPDelegate provided by the consumer.
     */
    var delegate: DHPDelegate?

    /**
     This property is the current LoginState of the DHPManager.
     */
    var loginState: LoginState

    /**
     The property returns the maximum number of objects to include in the data syncronization upload API payload.
     */
    var maxUploadObjects: Int

    var loginInfo: DHPLoginInfo

    var apiRequestMetricsCallback : ((String, Boolean, String, Duration) -> Unit)?

    /**
     This method initializes the loginState based on the properties provided by the DHPDelegate.
     It will callback to the delegate via the loggedIn(), failLogin(), or failConsent() methods as needed.
     If this is not called before executeRequest, it will be called on the first executeRequest.
     */
    fun initialize()

    /**
    This method executes a DHP API call.
    - Parameters:
    - request: a container object that encapsulates the request.
     */
    fun executeAsync(dhpRequest: GenericDHPRequest<*>, callback : ((Boolean, String, String?) -> Unit)?)

    /**
     This method is used to build an Identity Hub Registration URL using the base URL and optional query parameters.
     */
    fun getRegistrationURL(): URL

    /**
    This method is used to build an Identity Hub Login URL using the base URL and required query parameters.
     */
    fun getLoginURL(): URL

    /**
     This method checks whether a URL is the identityHubLoginSuccessRedirectURL.
     - Parameter url: The URL to check.
     - Returns: true or false
     */
    fun isLoginSuccessRedirectURL(url: URL): Boolean

    /**
    This method is used to build an Identity Hub OAuth Authorization URL using the base URL and required query parameters.
     */
    fun getAuthorizationURL() : URL

    /**
    This method checks whether a URL is the identityHubAuthorizationSuccessRedirectURL.
    - Parameter url: The URL to check.
    - Returns: true or false
     */
    fun isAuthorizationSuccessRedirectURL(url: URL) : Boolean

    /**
    This method takes an Identity Hub Authorization Success Redirect URL, parses the values from query string,
    and completes the rest of the DHP login. It validates the ID Token, gets the Identity Hub Profile,
    logs in with the DHP, registers the patient, and if necessary ensures the user is opted in.
    - Parameters:
    - authorizationSuccessRedirectURL: the OAuth success URL with query string.
    - callback: a method to call on the consumer to indicate whether all steps succeeded or login failed.
     */
    fun completeLoginAsync(authorizationSuccessRedirectURL: URL, callback:  (LoginResult) -> Unit)

    fun logOut()
}
