//
// ScanIntroFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view.device


import android.os.Bundle
import com.teva.common.services.analytics.enumerations.AnalyticsScreen

import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.device.ScanIntroViewModel
import com.teva.respiratoryapp.databinding.ScanIntroFragmentBinding
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment

/**
 * Fragment class for the Device List screen.
 */
class ScanIntroFragment : BaseFragment<ScanIntroFragmentBinding, ScanIntroViewModel>(R.layout.scan_intro_fragment) {

    init {
        screen = AnalyticsScreen.ScanningInstructions()
    }

    /**
     * Implemented by derived fragments to configure the properties of the fragment.
     */
    override fun configureFragment() {
        super.configureFragment()

        toolbarTitle = localizationService!!.getString(R.string.addDeviceScanInhalerTitle_text)
    }

    /**
     * Sets the ViewModel for the fragment.
     */
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = ScanIntroViewModel(dependencyProvider!!)
    }
}
