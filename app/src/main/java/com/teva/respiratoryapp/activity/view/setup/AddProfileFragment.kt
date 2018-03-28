//
// AddProfileFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view.setup

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.teva.cloud.dataentities.UserProfile
import com.teva.common.services.analytics.enumerations.AnalyticsScreen
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.dashboard.ContactSupportViewModel
import com.teva.respiratoryapp.activity.viewmodel.setup.AddProfileViewModel
import com.teva.respiratoryapp.databinding.AddProfileFragmentBinding
import com.teva.respiratoryapp.databinding.ConsentFragmentBinding
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment
import kotlinx.android.synthetic.main.add_profile_fragment.*

/**
 * Fragment class for the Add Profile screen.
 */
class AddProfileFragment
    : BaseFragment<AddProfileFragmentBinding, AddProfileViewModel>(R.layout.add_profile_fragment) {

    init {
        screen = AnalyticsScreen.AddDependent()
    }

    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = AddProfileViewModel(dependencyProvider!!, fragmentArguments?.getSerializable(EXISTING_PROFILES_BUNDLE_KEY) as java.util.ArrayList<UserProfile>)
    }

    /**
     * Initializes the toolbar properties.
     */
    override fun initToolbar(rootView: View) {
        super.initToolbar(rootView)

        attachScrollBehavior(scrollView)
    }

    /**
     * Lifetime method called after the fragment's view has been created.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        dob.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                viewModel?.onEditorActionButton()
                return@OnEditorActionListener true
            }
            false
        })
    }

    companion object {

        private val EXISTING_PROFILES_BUNDLE_KEY = "ExistingProfiles"

        /**
         * Creates an arguments bundle for the AddProfile fragment.
         *
         * @param walkthrough           - The type of the walkthrough.
         * @param displayHowToUseTheApp - flag to indicate if the fragment is part of walkthrough or how to use the app.
         * @return A Bundle to be used as the fragment's arguments.
         */
        fun createArguments(existingProfiles: ArrayList<UserProfile>): Bundle {
            val bundle = Bundle()
            bundle.putSerializable(EXISTING_PROFILES_BUNDLE_KEY, existingProfiles)
            return bundle
        }
    }

}