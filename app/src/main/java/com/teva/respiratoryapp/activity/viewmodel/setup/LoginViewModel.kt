//
// LoginViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.setup

import android.os.Handler
import com.teva.cloud.enumerations.LoginResult
import com.teva.cloud.messages.SyncCloudMessage
import com.teva.cloud.models.CloudManager
import com.teva.cloud.models.WebLoginManager
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.common.utilities.Messenger
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.LoadingEvents
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel
import com.teva.respiratoryapp.services.alert.SystemAlertManager
import java.net.URL


class LoginViewModel(dependencyProvider: DependencyProvider, private val loginFailedCallback: () -> Unit)
    : FragmentViewModel(dependencyProvider) {

    private val webLoginManager = dependencyProvider.resolve<WebLoginManager>()
    private val cloudManager = dependencyProvider.resolve<CloudManager>()
    private val handler = Handler()

    private var isInitialSetup: Boolean = true
        get() = !cloudManager.isInitialSetupCompleted

    fun beginLogin(): String {
        // get this from WebLoginManager
        return webLoginManager.getLoginURL().toString()
    }

    fun processRequestUrl(url: String): ProcessRequestUrlResult {
        logger.log(Logger.Level.VERBOSE, "processRequestURL $url")

        if (webLoginManager.isLoginSuccessRedirectURL(URL(url))) {
            return ProcessRequestUrlResult(false, webLoginManager.getAuthorizationURL().toString())
        } else if (webLoginManager.isAuthorizationSuccessRedirectURL(URL(url))) {
            webLoginManager.completeLoginAsync(URL(url), this::loginCompleted)
            return ProcessRequestUrlResult(false, null)
        }

        dependencyProvider?.resolve<LoadingEvents>()?.showLoadingIndicator()
        return ProcessRequestUrlResult(true, null)

    }

    /**
     * This method is the callback for web login manager's completeLogin.
     * @param success: did the login succeed
     */
    private fun loginCompleted(result: LoginResult)
    {
        handler.post({
            if (result == LoginResult.SUCCESS) {
                dependencyProvider.resolve<Events>().loginComplete()
            } else {
                dependencyProvider?.resolve<LoadingEvents>()?.hideLoadingIndicator()
                //Todo: fix the error message after the UX team provides the text.
                dependencyProvider.resolve<SystemAlertManager>().showAlert(
                        id = LoginViewModel.LOGIN_FAILED_ALERT_ID,
                        messageId = when(result) { LoginResult.FAILURE -> R.string.cloudLoginFailed_text
                            LoginResult.INCORRECT_ACCOUNT -> R.string.cloudLoginIncorrectAccount_text
                            else -> null
                        },
                        titleId = R.string.cloudLoginFailedTitle_text,
                        primaryButtonTextId = R.string.ok_text,
                        secondaryButtonTextId = R.string.contact_teva_support
                )
                loginFailedCallback()
            }
        })

        if(!isInitialSetup) {
            dependencyProvider.resolve<Messenger>().post(SyncCloudMessage())
        }
    }



    data class ProcessRequestUrlResult(val shouldLoadUrl: Boolean, val redirectUrl: String?)

    interface Events {
        fun loginComplete()
    }

    companion object {
        val LOGIN_FAILED_ALERT_ID = "LoginFailedAlert"
    }
}