//
// SelectInhalerNameFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view.device


import android.os.Bundle
import android.support.v4.app.Fragment
import com.teva.common.services.analytics.enumerations.AnalyticsScreen

import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.model.InhalerRegistrationCommonState
import com.teva.respiratoryapp.activity.viewmodel.device.InhalerNameItem
import com.teva.respiratoryapp.activity.viewmodel.device.SelectInhalerNameViewModel
import com.teva.respiratoryapp.databinding.InhalerNameItemBinding
import com.teva.respiratoryapp.databinding.SelectInhalerNameFragmentBinding
import com.teva.respiratoryapp.mvvmframework.ui.ViewModelListFragment
import com.teva.devices.enumerations.InhalerNameType


/**
 * A simple [Fragment] subclass.
 */
class SelectInhalerNameFragment : ViewModelListFragment<SelectInhalerNameFragmentBinding, SelectInhalerNameViewModel, InhalerNameItemBinding, InhalerNameItem>(R.layout.select_inhaler_name_fragment, R.layout.inhaler_name_item) {

    /**
     * Implemented by derived fragments to configure the properties of the fragment.
     */
    override fun configureFragment() {
        super.configureFragment()
        toolbarTitle = localizationService!!.getString(R.string.deviceNameViewTitle_text)

        val commonState = dependencyProvider!!.resolve<InhalerRegistrationCommonState>()
        if (commonState.mode === InhalerRegistrationCommonState.Mode.Add) {
            //menuId = R.menu.next_menu
            screen = AnalyticsScreen.InhalerName()
        } else {
            screen = AnalyticsScreen.EditInhaler()
        }
    }

    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = SelectInhalerNameViewModel(dependencyProvider!!)
    }

    companion object {

        /**
         * Helper method used by data bindings to convert an InhalerNameType into a drawable resource id.
         *
         * @param inhalerNameType The InhalerNameType to convert.
         * @return The cooresponding drawable resource id.
         */
        @JvmStatic
        fun InhalerNameTypeToDrawableId(inhalerNameType: InhalerNameType): Int {
            val result = when (inhalerNameType) {
                InhalerNameType.HOME -> R.drawable.home_selector
                InhalerNameType.SPORTS -> R.drawable.sports_selector
                InhalerNameType.CARRY_WITH_ME -> R.drawable.travel_selector
                InhalerNameType.WORK -> R.drawable.work_selector
                InhalerNameType.CUSTOM -> R.drawable.plus_selector
                else -> 0
            }

            return result
        }
    }
}
