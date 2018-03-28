//
// WebLoginManagerImpl.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.models

import com.teva.cloud.enumerations.LoginResult
import com.teva.utilities.services.DependencyProvider
import com.teva.dhp.models.DHPManager
import java.net.URL


/**
 * The concrete implementation of WebLoginManager for Identity Hub.
 */

class WebLoginManagerImpl: WebLoginManager {

    private val dhpManager = DependencyProvider.default.resolve<DHPManager>()

    override fun getLoginURL(): URL {
        return dhpManager.getLoginURL()
    }

    override fun getRegistrationURL(): URL {
        return dhpManager.getRegistrationURL()
    }

    override fun isLoginSuccessRedirectURL(url: URL): Boolean {
        return dhpManager.isLoginSuccessRedirectURL(url)
    }

    override fun completeLoginAsync(loginSuccessRedirectURL: URL, callback: (LoginResult) -> Unit) {
        return dhpManager.completeLoginAsync(loginSuccessRedirectURL, {dhpResult ->
            val result = when(dhpResult) {
                com.teva.dhp.enumerations.LoginResult.SUCCESS -> LoginResult.SUCCESS
                com.teva.dhp.enumerations.LoginResult.FAILURE -> LoginResult.FAILURE
                com.teva.dhp.enumerations.LoginResult.INCORRECT_ACCOUNT -> LoginResult.INCORRECT_ACCOUNT
            }

            //DependencyProvider.default.resolve<Messenger>().post(SystemMonitorMessage(CloudActivity.identityHubLogin(result == LoginResult.SUCCESS)))
            callback(result)
        })
    }

    override fun getAuthorizationURL(): URL {
        return dhpManager.getAuthorizationURL()
    }

    override fun isAuthorizationSuccessRedirectURL(url: URL): Boolean {
        return dhpManager.isAuthorizationSuccessRedirectURL(url)
    }

}