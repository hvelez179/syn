//
// WebLoginManager.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.models

import com.teva.cloud.enumerations.LoginResult
import java.net.URL


/**
 * This interface is used by an OAuth web view to provide login logic.
 */
interface WebLoginManager {
    /**
     * This method returns the Web Login URL.
     */
    fun getLoginURL(): URL

    /**
     * This method returns the Web Registration URL.
     */
    fun getRegistrationURL(): URL

    /**
     * This method checks whether a URL is the OAuth success URL.
     * @param url: The URL to check.
     * @return: true or false
     */
    fun isLoginSuccessRedirectURL(url: URL): Boolean

    /**
     * This method returns the OAuth Authorization URL.
     */
    fun getAuthorizationURL(): URL

    /**
     * This method checks whether a URL is the OAuth authorization success URL.
     * @param url: The URL to check.
     * @return: true or false
     */
    fun isAuthorizationSuccessRedirectURL(url: URL): Boolean

    /**
     * This method takes an OAuth Success Redirect URL, parses the values from query string,
     * and completes the rest of the login process.
     * @param loginSuccessRedirectURL: the OAuth success URL with query string.
     * @param callback: a method to call on the consumer to indicate whether all steps succeeded or login failed.
     */
    fun completeLoginAsync(loginSuccessRedirectURL: URL, callback: (LoginResult) -> Unit)
}