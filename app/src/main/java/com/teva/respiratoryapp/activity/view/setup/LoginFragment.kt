//
// LoginFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view.setup

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.teva.common.messages.SystemMonitorMessage
import com.teva.common.services.analytics.enumerations.AnalyticsScreen
import com.teva.common.utilities.Messenger
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.LoadingEvents
import com.teva.respiratoryapp.activity.viewmodel.setup.LoginViewModel
import com.teva.respiratoryapp.common.AppSystemMonitorActivity
import com.teva.respiratoryapp.databinding.LoginFragmentBinding
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment
import kotlinx.android.synthetic.main.login_fragment.*
import java.net.URL


class LoginFragment : BaseFragment<LoginFragmentBinding, LoginViewModel>(R.layout.login_fragment) {

    var isRegistering = false

    override var isLogoutable = false

    init {
        screen = AnalyticsScreen.Login()
    }

    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = LoginViewModel(dependencyProvider!!, this::loadLoginPage)
    }

    /**
     * Implemented by derived fragments to configure the properties of the fragment.
     */
    override fun configureFragment() {
        super.configureFragment()

        dependencyProvider?.resolve<LoadingEvents>()?.showLoadingIndicator()
    }

    /**
     * Called by the Fragment class after the view has been created.
     *
     * @param view The new view for the fragment.
     * @param savedInstanceState The saved instance state of the fragment.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachScrollBehavior(webView, getDimension(R.dimen.toolbar_title_threshold))

        webView.webViewClient = LoginWebViewClient()
        webView.settings.javaScriptEnabled = true

        loadLoginPage()
    }

    private fun loadLoginPage() {
        webView.visibility = View.VISIBLE
        val loginUrl = viewModel!!.beginLogin()
        webView.loadUrl(loginUrl)
    }

    /**
     * WebViewClient object that receives events from the WebView
     */
    private inner class LoginWebViewClient: WebViewClient() {
        @Suppress("DEPRECATION", "OverridingDeprecatedMember")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {

            if (url != null) {
                val result = viewModel!!.processRequestUrl(url)

                if (!result.shouldLoadUrl) {

                    if (result.redirectUrl == null) {
                        // URL is the Login success URL and login has begun. We are waiting for a login complete message now.
                        isRegistering = true
                        view?.visibility = View.GONE
                    } else {
                        view?.loadUrl(result.redirectUrl)
                    }


                } else {
                    return false
                }
            }

            return true
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

            if (isLoginOrRegistrationOrForgotPasswordUrl(url)) {
                dependencyProvider?.resolve<LoadingEvents>()?.hideLoadingIndicator()
            }
        }

        private fun isLoginOrRegistrationOrForgotPasswordUrl(url: String?): Boolean {

            //Todo - this implementation might have to change.
            val registrationPath = "/RegisterTEV"
            val forgotPasswordPath = "/ForgotPasswordTEV"

            if(url.isNullOrEmpty()) {
                return false
            }

            var matchesUrl = false

            val loginUrlString = viewModel!!.beginLogin()

            val loginUrl = URL(loginUrlString)
            val currentUrl = URL(url)
            var activity: AppSystemMonitorActivity? = null

            if(loginUrl.host.toLowerCase() == currentUrl.host.toLowerCase()) {
                if(loginUrl.path.toLowerCase() == currentUrl.path.toLowerCase()) {
                    activity = AppSystemMonitorActivity.LoginPageLoaded()
                    matchesUrl = true
                } else if(registrationPath.toLowerCase() == currentUrl.path.toLowerCase()) {
                    activity = AppSystemMonitorActivity.RegistrationPageLoaded()
                    matchesUrl = true
                } else if(forgotPasswordPath.toLowerCase() == currentUrl.path.toLowerCase()) {
                    activity = AppSystemMonitorActivity.ForgotPasswordPageLoaded()
                    matchesUrl = true
                }
            }

            if(matchesUrl) {
                dependencyProvider?.resolve<Messenger>()?.post(SystemMonitorMessage(activity!!))
            }

            return matchesUrl
        }
    }
}