//
// PrivacyNoticeFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view

import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import com.teva.common.services.analytics.enumerations.AnalyticsScreen

import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.dashboard.PrivacyNoticeViewModel
import com.teva.respiratoryapp.databinding.PrivacyNoticeFragmentBinding
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment
import kotlinx.android.synthetic.main.privacy_notice_fragment.*

/**
 * This class displays the privacy notice.
 */
class PrivacyNoticeFragment : BaseFragment<PrivacyNoticeFragmentBinding, PrivacyNoticeViewModel>(R.layout.privacy_notice_fragment) {

    init {
        screen = AnalyticsScreen.PrivacyNotice()
    }

    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments - The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = PrivacyNoticeViewModel(dependencyProvider!!)
    }

    /**
     * This method is called when the view is created. This method loads the privacy notice document.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val webView = findViewById(R.id.webview) as WebView?
        webView!!.loadUrl("file:///android_asset/privacynotice.html")
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
        toolbarTitle = localizationService!!.getString(R.string.privacyNoticeLink_text)
    }

}
