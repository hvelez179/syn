//
// SupportViewModel.kt
// app
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.activity.viewmodel.dashboard

import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.SupportEvents
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel

/**
 * This class is the view model class for the support fragment.
 */
class SupportViewModel(dependencyProvider: DependencyProvider) : FragmentViewModel(dependencyProvider) {

    var description : String
    private set

    init {
        description = dependencyProvider.resolve<LocalizationService>().getString(R.string.menuServiceCenterDescription_text)
    }

    /**
     * This function is invoked when user clicks the 'Contact Teva Support' button.
     */
    fun onContactSupport() {
        dependencyProvider.resolve<SupportEvents>().onSupport()
    }
}