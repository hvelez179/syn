//
// DsaPopup.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view.popup

import android.os.Bundle
import com.teva.common.services.analytics.enumerations.AnalyticsScreen

import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.popup.DsaPopupViewModel
import com.teva.respiratoryapp.databinding.DsaPopupBinding
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment
import com.teva.userfeedback.enumerations.UserFeeling
import org.threeten.bp.Instant

class DsaPopup : BaseFragment<DsaPopupBinding, DsaPopupViewModel>(R.layout.dsa_popup) {

    init {
        screen = AnalyticsScreen.DailySelfAssessment()
    }

    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = DsaPopupViewModel(dependencyProvider!!, fragmentArguments?.getSerializable(DSA_BUNDLE_KEY) as Instant)
    }

    /**
     * Initializes the binding with the viewmodel. Can be overloaded by derived fragment classes
     * to perform additional initialization of the binding.
     *
     * @param binding The main fragment binding.
     */
    override fun initBinding(binding: DsaPopupBinding) {
        super.initBinding(binding)

        binding.popupBackground?.let { it.viewmodel = viewModel }
    }

    companion object {
        private val DSA_BUNDLE_KEY = "DSA_POPUP"

        /**
         * Creates a fragment arguments bundle for the DSA Confirm Popup.
         * @param userFeeling The user feeling argument
         */
        fun createArguments(now: Instant): Bundle {
            val bundle = Bundle()
            bundle.putSerializable(DSA_BUNDLE_KEY, now)
            return bundle
        }
    }
}
