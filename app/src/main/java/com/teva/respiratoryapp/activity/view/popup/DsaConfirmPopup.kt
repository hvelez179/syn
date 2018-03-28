//
// DsaConfirmPopup.kt
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
import com.teva.respiratoryapp.activity.viewmodel.popup.DsaConfirmPopupViewModel
import com.teva.respiratoryapp.activity.viewmodel.popup.DsaPopupViewModel
import com.teva.respiratoryapp.databinding.DsaConfirmPopupBinding
import com.teva.respiratoryapp.databinding.DsaPopupBinding
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment
import com.teva.userfeedback.enumerations.UserFeeling

class DsaConfirmPopup : BaseFragment<DsaConfirmPopupBinding, DsaConfirmPopupViewModel>(R.layout.dsa_confirm_popup) {

    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        val dsaInt = fragmentArguments!!.getInt(DSA_BUNDLE_KEY)
        val dsa = UserFeeling.fromOrdinal(dsaInt)
        viewModel = DsaConfirmPopupViewModel(dependencyProvider!!, dsa)
    }

    /**
     * Initializes the binding with the viewmodel. Can be overloaded by derived fragment classes
     * to perform additional initialization of the binding.
     *
     * @param binding The main fragment binding.
     */
    override fun initBinding(binding: DsaConfirmPopupBinding) {
        super.initBinding(binding)

        binding.popupBackground?.let { it.viewmodel = viewModel }
    }

    companion object {
        private val DSA_BUNDLE_KEY = "DSA"

        /**
         * Creates a fragment arguments bundle for the DSA Confirm Popup.
         * @param userFeeling The user feeling argument
         */
        fun createArguments(userFeeling: UserFeeling): Bundle {
            val bundle = Bundle()
            bundle.putInt(DSA_BUNDLE_KEY, userFeeling.ordinal)

            return bundle
        }

        /**
         * DSA to image id conversion method used by the data bindings in the view layout.
         *
         * @param dsa The daily self assessment value to convert
         * @return The id of the image to represent the DSA value.
         */
        @JvmStatic
        fun DSAToImageId(dsa: UserFeeling): Int {
            val imageId = when (dsa) {
                UserFeeling.GOOD -> R.drawable.ic_smiley_happy_green_large
                UserFeeling.POOR -> R.drawable.ic_smiley_ok_yellow_large
                UserFeeling.BAD -> R.drawable.ic_smiley_sad_red_large
                else -> android.R.color.transparent
            }

            return imageId
        }
    }
}
