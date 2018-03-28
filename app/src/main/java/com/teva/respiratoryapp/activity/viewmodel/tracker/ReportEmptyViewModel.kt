package com.teva.respiratoryapp.activity.viewmodel.tracker

import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel

/**
 * Viewmodel for the screen shown when a report is requested and no report data exists.
 */
class ReportEmptyViewModel(dependencyProvider: DependencyProvider)
    : FragmentViewModel(dependencyProvider) {

    /**
     * Click handler for the CTA button.
     */
    fun onButton() {
        dependencyProvider.resolve<Events>().onAddInhaler()
    }

    interface Events {
        /**
         * Request to add an inhaler.
         */
        fun onAddInhaler()
    }

}