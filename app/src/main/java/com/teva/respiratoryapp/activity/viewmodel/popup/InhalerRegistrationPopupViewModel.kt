//
// InhalerRegistrationPopupViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.popup


import com.teva.respiratoryapp.R
import com.teva.utilities.services.DependencyProvider

/**
 * Viewmodel for the Inhaler Registration popup.
 *
 * @param dependencyProvider The dependency injection mechanism.
 */
class InhalerRegistrationPopupViewModel(dependencyProvider: DependencyProvider)
    : DashboardPopupViewModel(dependencyProvider) {
    init {

        popupColor = PopupColor.GRAY
        buttonText = getString(R.string.ok_text)
        isCloseButtonVisible = true
        arrowState = PopupDashboardButton.DEVICES
        buttonState = PopupDashboardButton.ALL
        buttonsDimmed = false
    }
}
