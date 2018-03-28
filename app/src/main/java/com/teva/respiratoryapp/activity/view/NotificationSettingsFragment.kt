//
// NotificationSettingsFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view

import android.os.Bundle
import com.teva.common.services.analytics.enumerations.AnalyticsScreen

import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.notificationsettings.NotificationSettingsViewModel
import com.teva.respiratoryapp.databinding.NotificationSettingsFragmentBinding
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment

/**
 * This class is the fragment class which displays the Notification Settings user interface.
 */
class NotificationSettingsFragment
    : BaseFragment<NotificationSettingsFragmentBinding, NotificationSettingsViewModel>(R.layout.notification_settings_fragment) {

    init {
        screen = AnalyticsScreen.NotificationSettings()
    }

    /**
     * Implemented by derived fragments to configure the properties of the fragment.
     */
    override fun configureFragment() {
        super.configureFragment()
        toolbarTitle = localizationService!!.getString(R.string.menuSettingsItem_text)
    }

    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = NotificationSettingsViewModel(dependencyProvider!!)
    }

}
