//
// ConsentFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view.setup

import android.os.Bundle
import com.teva.common.services.analytics.enumerations.AnalyticsScreen

import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.view.TextEntryFragment
import com.teva.respiratoryapp.activity.viewmodel.setup.ConsentViewModel
import com.teva.respiratoryapp.databinding.ConsentFragmentBinding

/**
 * This class provides the UI for the consent screen.
 */
class ConsentFragment
    : TextEntryFragment<ConsentFragmentBinding, ConsentViewModel>(R.layout.consent_fragment) {

    init {
        screen = AnalyticsScreen.Dashboard()
    }

    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = ConsentViewModel(dependencyProvider!!)
    }

    /**
     * Implemented by derived fragments to configure the properties of the fragment.
     */
    override fun configureFragment() {
        setSaveViewModelState(true)
    }
}
