//
//
// IntroViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//
//

package com.teva.respiratoryapp.activity.viewmodel.dashboard

import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel

/**
 * This class is the view model for the Value Proposition Intro screen.
 */
class IntroViewModel(dependencyProvider: DependencyProvider) : FragmentViewModel(dependencyProvider) {

    /**
     * Click handler for the "Next" ("Let's GO") button.
     */
    fun onNext()
    {
        dependencyProvider.resolve<Events>().onNext()
    }

    /**
     * Events produced by the ViewModel to request actions by the Activity.
     */
    interface Events {
        /**
         * Requests to navigate to the next screen.
         */
        fun onNext()
    }

}