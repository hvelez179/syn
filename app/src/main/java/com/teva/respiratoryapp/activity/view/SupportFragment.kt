//
// SupportFragment.kt
// app
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.activity.view

import android.os.Bundle
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.dashboard.SupportViewModel
import com.teva.respiratoryapp.databinding.SupportFragmentBinding
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment

/**
 * This class represents the support page.
 */
class SupportFragment : BaseFragment<SupportFragmentBinding, SupportViewModel>(R.layout.support_fragment) {
    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = SupportViewModel(dependencyProvider!!)
    }

    /**
     * Implemented by derived fragments to configure the properties of the fragment.
     */
    override fun configureFragment() {
        super.configureFragment()
        toolbarTitle = localizationService!!.getString(R.string.menuServiceCenterItem_text)
    }
}