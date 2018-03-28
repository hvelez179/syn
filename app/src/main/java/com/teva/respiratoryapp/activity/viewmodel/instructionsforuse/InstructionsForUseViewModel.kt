//
// InstructionsForUseViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.instructionsforuse

import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel

class InstructionsForUseViewModel(dependencyProvider: DependencyProvider)
    : FragmentViewModel(dependencyProvider) {

    // This field indicates the current page being displayed.
    var currentPage: Int = 0
        set(newValue) {
            field = newValue
            notifyChange()
        }

    /**
     * This function returns the alpha value to be used for the page indicator.
     * The indicator for current page uses alpha 1.0 and the indicators for other
     * pages use alpha 0.3.
     */
    fun getIndicatorAlpha(indicator: Int): Float {
        return if(indicator ==  currentPage) {
            1.0f
        } else {
            0.3f
        }
    }
}
