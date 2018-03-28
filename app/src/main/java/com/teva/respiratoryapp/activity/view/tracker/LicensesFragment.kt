//
// LicensesFragment.kt
// app
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.activity.view.tracker

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import com.teva.common.services.analytics.enumerations.AnalyticsScreen
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.dashboard.LicensesViewModel
import com.teva.respiratoryapp.databinding.LicensesFragmentBinding
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment

/**
 * This class represents the licenses screen.
 */
class LicensesFragment : BaseFragment<LicensesFragmentBinding, LicensesViewModel>(R.layout.licenses_fragment) {

    init {
        screen = AnalyticsScreen.ThirdPartyLicenses()
    }

    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments - The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = LicensesViewModel(dependencyProvider!!)
    }

    /**
     * This method is called when the view is created. This method loads the terms of use document.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val webView = findViewById(R.id.webview) as WebView?
        webView!!.loadUrl("file:///android_asset/noticeOfThirdPartyLicenses.html")
    }

    /**
     * This method sets the toolbar title for the fragment.
     */
    override fun configureFragment() {
        super.configureFragment()
        toolbarTitle = localizationService!!.getString(R.string.licensesLink_text)
    }
}