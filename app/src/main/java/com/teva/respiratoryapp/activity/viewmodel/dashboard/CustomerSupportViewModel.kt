/*
 *
 *  CustomerSupportViewModel.kt
 *  app
 *
 *  Copyright © 2018 Teva. All rights reserved.
 *
 */

package com.teva.respiratoryapp.activity.viewmodel.dashboard

import android.webkit.WebView
import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel

/**
 * This class is the viewmodel for the Customer Support Fragment.
 */
class CustomerSupportViewModel(dependencyProvider: DependencyProvider) : FragmentViewModel(dependencyProvider) {
    // This member represents the webview control in the fragment.
    var webView: WebView? = null

    /**
     * This method refreshes the web page displayed in the webview.
     */
    fun onRefreshWebPage() {
        webView?.reload()
    }

    /**
     * This method closes the customer support fragment by navigating back.
     */
    fun onDone() {
        onNavigation()
    }
}