//
// TermsOfUseFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import com.teva.common.services.analytics.enumerations.AnalyticsScreen

import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.dashboard.TermsOfUseViewModel
import com.teva.respiratoryapp.databinding.TermsOfUseFragmentBinding
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment

/**
 * This fragment displays the terms of use.
 */
class TermsOfUseFragment : BaseFragment<TermsOfUseFragmentBinding, TermsOfUseViewModel>(R.layout.terms_of_use_fragment) {

    init {
        screen = AnalyticsScreen.TermsOfUse()
    }

    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments - The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = TermsOfUseViewModel(dependencyProvider!!)
    }

    /**
     * This method is called when the view is created. This method loads the terms of use document.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val webView = findViewById(R.id.webview) as WebView?
        webView!!.loadUrl("file:///android_asset/termsofuse.html")
    }

    /**
     * Initializes the toolbar properties.
     */
    override fun initToolbar(rootView: View) {
        super.initToolbar(rootView)

        attachScrollBehavior(rootView.findViewById(R.id.webview), getDimension(R.dimen.toolbar_title_threshold))
    }

    /**
     * This method sets the toolbar title for the fragment.
     */
    override fun configureFragment() {
        super.configureFragment()
        toolbarTitle = localizationService!!.getString(R.string.termsOfUseLink_text)
    }
}
