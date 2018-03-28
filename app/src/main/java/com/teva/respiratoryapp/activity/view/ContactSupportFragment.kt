/*
 *
 *  ContactSupportFragment.kt
 *  app
 *
 *  Copyright © 2018 Teva. All rights reserved.
 *
 */

package com.teva.respiratoryapp.activity.view

import android.os.Bundle
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.dashboard.ContactSupportViewModel
import com.teva.respiratoryapp.databinding.ContactSupportFragmentBinding
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment

/**
 * This class represents the contact customer support fragment.
 */
class ContactSupportFragment : BaseFragment<ContactSupportFragmentBinding, ContactSupportViewModel>(R.layout.contact_support_fragment) {
    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = ContactSupportViewModel(dependencyProvider!!)
    }

    /**
     * Implemented by derived fragments to configure the properties of the fragment.
     */
    override fun configureFragment() {
        super.configureFragment()
        toolbarTitle = getString(R.string.menuServiceCenterCall_text)
    }

}