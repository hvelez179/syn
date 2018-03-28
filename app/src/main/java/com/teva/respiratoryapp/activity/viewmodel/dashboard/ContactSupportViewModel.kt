/*
 *
 *  ContactSupportViewModel.kt
 *  app
 *
 *  Copyright © 2018 Teva. All rights reserved.
 *
 */

package com.teva.respiratoryapp.activity.viewmodel.dashboard

import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel

/**
 * This class represents the viewmodel for the contact customer support fragment.
 */
class ContactSupportViewModel(dependencyProvider: DependencyProvider) : FragmentViewModel(dependencyProvider) {
    val description = getString(R.string.menuServiceCenterDescription_text)

    fun onContactSupportClicked() {
        dependencyProvider.resolve<ContactSupportViewModel.Events>().onContactSupport()
    }

    interface Events {
        fun onContactSupport()
    }

}