/*
 *
 *  EmancipatedMessageViewModel.kt
 *  app
 *
 *  Copyright © 2018 Teva. All rights reserved.
 *
 */

package com.teva.respiratoryapp.activity.viewmodel

import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel

/**
 * This class is the viewmodel for the emancipated message fragment.
 */
class EmancipatedMessageViewModel(dependencyProvider: DependencyProvider) : FragmentViewModel(dependencyProvider) {
    val title = getString(R.string.emancipatedTitle_text)
    val content = getString(R.string.emancipatedBackgroundMessage_text)

    /**
     * Handler for the support link click event.
     */
    fun onSupportClicked() {
        dependencyProvider.resolve<EmancipatedMessageViewModel.Events>().onSupportClicked()
    }

    /**
     * The handler for the back button press.
     */
    override fun onBackPressed() {
        // do not allow the user to navigate in the app if emancipated.
        return
    }

    interface Events {
        fun onSupportClicked()
    }
}