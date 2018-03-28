//
// WalkthroughFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view.popup.walkthrough

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.teva.common.services.analytics.enumerations.AnalyticsScreen

import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.view.popup.DashboardPopupFragment
import com.teva.respiratoryapp.activity.viewmodel.dashboard.DashboardStateViewModel
import com.teva.respiratoryapp.activity.viewmodel.popup.walkthrough.WalkthroughViewModel

/**
 * This class represents the fragment which displays the walkthrough screens.
 */
class WalkthroughFragment : DashboardPopupFragment() {

    private var walkthrough: Walkthrough? = null

    init {
        screen = AnalyticsScreen.Walkthrough()
    }

    /**
     * Creates the content view for the walkthrough screens.
     *
     * @param inflater The LayoutInflater for the fragment.
     * @return A view to be added as the content of the walkthrough screen.
     */
    override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(walkthrough!!.layoutId, container, false)
    }

    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        if (fragmentArguments != null) {
            walkthrough = Walkthrough.values()[fragmentArguments.getInt(WALKTHROUGH_SCREEN_TYPE_BUNDLE_KEY)]
            val displayHowToUseTheApp = fragmentArguments.getBoolean(DISPLAY_HOW_TO_USE_THE_APP_BUNDLE_KEY)
            viewModel = WalkthroughViewModel(dependencyProvider!!, walkthrough!!, displayHowToUseTheApp)
        }
    }

    /**
     * The DashboardStateViewModel to be used for displaying the
     * background of the popup. The base implementation returns the actual
     * DashboardStateViewModel from the dependency provider. For walkthrough screens
     * related to tracker this method is overridden to provide the inhale event count
     * to be displayed in the background.
     */
    override val dashboardStateViewModel: DashboardStateViewModel
        get() {
            val INHALE_EVENTS_NONE = 0

            val dashboardStateViewModel = DashboardStateViewModel(dependencyProvider!!)

            when (walkthrough) {
                Walkthrough.DEVICES_2 -> dashboardStateViewModel.isDevicesCritical = true
                else -> dashboardStateViewModel.setInhalesToday(INHALE_EVENTS_NONE)
            }

            return dashboardStateViewModel
        }

    companion object {

        private val WALKTHROUGH_SCREEN_TYPE_BUNDLE_KEY = "WalkthroughScreenType"
        private val DISPLAY_HOW_TO_USE_THE_APP_BUNDLE_KEY = "DisplayHowToUseTheApp"

        /**
         * Creates an arguments bundle for Walkthrough fragments.
         *
         * @param walkthrough           - The type of the walkthrough.
         * @param displayHowToUseTheApp - flag to indicate if the fragment is part of walkthrough or how to use the app.
         * @return A Bundle to be used as the fragment's arguments.
         */
        fun createArguments(walkthrough: Walkthrough, displayHowToUseTheApp: Boolean): Bundle {
            val bundle = Bundle()
            bundle.putInt(WALKTHROUGH_SCREEN_TYPE_BUNDLE_KEY, walkthrough.ordinal)
            bundle.putBoolean(DISPLAY_HOW_TO_USE_THE_APP_BUNDLE_KEY, displayHowToUseTheApp)
            return bundle
        }
    }
}
