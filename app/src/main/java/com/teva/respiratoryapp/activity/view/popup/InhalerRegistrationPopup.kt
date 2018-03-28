//
// InhalerRegistrationPopup.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view.popup


import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.popup.InhalerRegistrationPopupViewModel
import com.teva.respiratoryapp.databinding.InhalerRegistrationPopupBinding

/**
 * Fragment class for the Inhaler Registered popup.
 */
class InhalerRegistrationPopup : DashboardPopupFragment() {

    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = InhalerRegistrationPopupViewModel(dependencyProvider!!)
    }

    /**
     * Creates the content view for this popup.
     *
     * @param inflater The LayoutInflator for the fragment.
     * @return A view to be added as the content of the popup.
     */
    override fun onCreateContentView(inflater: LayoutInflater,
                                     container: ViewGroup): View {

        val binding = DataBindingUtil.inflate<InhalerRegistrationPopupBinding>(inflater, R.layout.inhaler_registration_popup, container, false)

        val contentViewModel = viewModel as InhalerRegistrationPopupViewModel?
        binding.viewmodel = contentViewModel

        return binding.root
    }

}
