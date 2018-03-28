//
// DsaConfirmPopupViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.popup

import com.teva.utilities.services.DependencyProvider
import com.teva.userfeedback.enumerations.UserFeeling

/**
 * Viewmodel for the DsaConfirmPopup fragment.
 *
 * @param dependencyProvider The dependency injection mechanism.
 * @property dsa The UserFeeling value
 */
class DsaConfirmPopupViewModel(dependencyProvider: DependencyProvider,
                               val dsa: UserFeeling) : DashboardPopupViewModel(dependencyProvider) {
    init {
        headerBarVisible = false
        arrowState = PopupDashboardButton.DSA
        buttonState = PopupDashboardButton.ALL
        buttonsDimmed = false
    }

    /**
     * Click handler for the action button.
     */
    override fun onButton() {
        onBackPressed()
    }
}
