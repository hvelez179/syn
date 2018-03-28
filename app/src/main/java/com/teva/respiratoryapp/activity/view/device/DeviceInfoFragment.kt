//
// DeviceInfoFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view.device

import android.os.Bundle
import android.view.View

import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.CheckBluetoothAndLocationEvents
import com.teva.respiratoryapp.activity.viewmodel.device.DeviceInfoViewModel
import com.teva.respiratoryapp.databinding.DeviceInfoFragmentBinding
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment

/**
 * Fragment class for the Device List screen.
 */
class DeviceInfoFragment : BaseFragment<DeviceInfoFragmentBinding, DeviceInfoViewModel>(R.layout.device_info_fragment) {

    private var mode: DeviceInfoViewModel.Mode? = null

    /**
     * Android lifecycle method called when the fragment is created.
     *
     * @param savedInstanceState The saved state of the fragment.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            // If savedInstanceState is null then this is the first display of the screen
            // and not a reconstitution from a saved state.  So tell the activity to check
            // the bluetooth and location permission and enables.
            dependencyProvider!!.resolve<CheckBluetoothAndLocationEvents>().checkBluetoothAndLocationStatus()
        }
    }

    /**
     * Implemented by derived fragments to configure the properties of the fragment.
     */
    override fun configureFragment() {
        super.configureFragment()

        menuId = R.menu.remove_menu
    }

    /**
     * Sets the ViewModel for the fragment.
     */
    override fun inject(fragmentArguments: Bundle?) {
        val serialNumber = fragmentArguments!!.getString(SERIAL_NUMBER_BUNDLE_KEY)
        mode = fragmentArguments.getSerializable(MODE_BUNDLE_KEY) as DeviceInfoViewModel.Mode

        viewModel = DeviceInfoViewModel(dependencyProvider!!, serialNumber!!, mode!!)
    }

    /**
     * Initializes the toolbar properties.
     */
    override fun initToolbar(rootView: View) {
        if (mode === DeviceInfoViewModel.Mode.EDIT) {
            toolbarTitle = localizationService?.getString(R.string.inhalerAppend_text)
        } else {
            toolbarTitle = localizationService?.getString(R.string.addDeviceScanInhalerTitle_text)
        }

        super.initToolbar(rootView)
        //toolbar?.setNavigationIcon(null)
    }

    companion object {
        val MODE_BUNDLE_KEY = "mode"
        val SERIAL_NUMBER_BUNDLE_KEY = "serialNumber"

        /**
         * Static method to create an arguments bundle for the fragment.
         *
         * @param serialNumber The serial number of the device to display.
         * *
         * @param mode         The reason this screen was displayed.
         */
        fun createFragmentArguments(serialNumber: String, mode: DeviceInfoViewModel.Mode): Bundle {
            val bundle = Bundle()
            bundle.putString(SERIAL_NUMBER_BUNDLE_KEY, serialNumber)
            bundle.putSerializable(MODE_BUNDLE_KEY, mode)

            return bundle
        }
    }
}
