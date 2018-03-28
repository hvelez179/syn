//
// AboutTheAppFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view

import android.os.Bundle
import android.view.View
import com.teva.common.services.analytics.enumerations.AnalyticsScreen

import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.dashboard.AboutAppViewModel
import com.teva.respiratoryapp.databinding.AboutAppFragmentBinding
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment

/**
 * This class represents the "About the app" screen.
 */
class AboutTheAppFragment : BaseFragment<AboutAppFragmentBinding, AboutAppViewModel>(R.layout.about_app_fragment) {

    init {
        screen = AnalyticsScreen.About()
    }

    /**
     * Implemented by derived fragments to configure the properties of the fragment.
     */
    override fun configureFragment() {
        super.configureFragment()
        toolbarTitle = localizationService!!.getString(R.string.menuAboutTitle_text)
    }

    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = AboutAppViewModel(dependencyProvider!!)
    }

    /**
     * Called by the base class after the view is created.
     *
     * @param view The view that was created
     * @param savedInstanceState The saved instance state of the fragment.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachScrollBehavior(view.findViewById<View>(R.id.item_list))
    }
}
