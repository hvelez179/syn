//
// CustomDeviceNameFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view.device


import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.teva.common.services.analytics.enumerations.AnalyticsScreen
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.view.TextEntryFragment
import com.teva.respiratoryapp.activity.viewmodel.device.CustomDeviceNameViewModel
import com.teva.respiratoryapp.databinding.CustomDeviceNameFragmentBinding

/**
 * Fragment class for the Device List screen.
 */
class CustomDeviceNameFragment : TextEntryFragment<CustomDeviceNameFragmentBinding, CustomDeviceNameViewModel>(
        R.layout.custom_device_name_fragment) {

    init {
        screen = AnalyticsScreen.NameYourInhaler()
    }

    /**
     * Implemented by derived fragments to configure the properties of the fragment.
     */
    override fun configureFragment() {
        super.configureFragment()

        toolbarTitle = localizationService!!.getString(R.string.addDeviceCustomNicknameTitle_text)
        setSaveViewModelState(true)
        menuId = 0
    }

    /**
     * Sets the ViewModel for the fragment.
     */
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = CustomDeviceNameViewModel(dependencyProvider!!)
    }

    /**
     * Hides the keyboard on pause
     */
    override fun onPause() {
        super.onPause()

        val editText = view!!.findViewById<EditText>(R.id.editText)
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText!!.windowToken, 0)
    }


}
