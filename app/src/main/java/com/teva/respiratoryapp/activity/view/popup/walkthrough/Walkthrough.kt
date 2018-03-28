//
// Walkthrough.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view.popup.walkthrough

import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.popup.PopupDashboardButton

/**
 * This enumeration lists the screens in the walkthrough.
 * @property layoutId The layout id for the walkthrough screen.
 * @property nextScreen The next walkthrough screen to be displayed.
 * @property isBackButtonEnabled The flag to enable or disable the back button on the walkthrough screen.
 * @property isNextButtonEnabled The flag to enable or disable the next button on the walkthrough screen.
 * @property dashboardButton The dashboard button to be highlighted when the screen is displayed.
 */

enum class Walkthrough(
        val layoutId: Int,
        val nextScreen: Walkthrough? = null,
        val isBackButtonEnabled: Boolean = true,
        val isNextButtonEnabled: Boolean = true,
        val arrowState: PopupDashboardButton = PopupDashboardButton.NONE,
        val buttonState: PopupDashboardButton = PopupDashboardButton.NONE) {


    GET_STARTED(layoutId = R.layout.walkthrough_get_started,
            isBackButtonEnabled = false,
            isNextButtonEnabled = false,
            arrowState = PopupDashboardButton.NONE,
            buttonState = PopupDashboardButton.ALL),

    SECURITY(layoutId = R.layout.walkthrough_security,
            nextScreen = GET_STARTED,
            arrowState = PopupDashboardButton.NONE,
            buttonState = PopupDashboardButton.ALL),

    REPORT(layoutId = R.layout.walkthrough_report,
            nextScreen = SECURITY,
            arrowState = PopupDashboardButton.REPORT,
            buttonState = PopupDashboardButton.REPORT),

    DSA(layoutId = R.layout.walkthrough_dsa,
            nextScreen = REPORT,
            arrowState = PopupDashboardButton.DSA,
            buttonState = PopupDashboardButton.DSA),

    SUPPORT(layoutId = R.layout.walkthrough_support,
            nextScreen = DSA,
            arrowState = PopupDashboardButton.SUPPORT,
            buttonState = PopupDashboardButton.SUPPORT),

    DEVICES_2(layoutId = R.layout.walkthrough_devices_2,
            nextScreen = SUPPORT,
            arrowState = PopupDashboardButton.DEVICES,
            buttonState = PopupDashboardButton.DEVICES),

    DEVICES_1(layoutId = R.layout.walkthrough_devices,
            nextScreen = DEVICES_2,
            arrowState = PopupDashboardButton.DEVICES,
            buttonState = PopupDashboardButton.DEVICES),

    ENVIRONMENT(layoutId = R.layout.walkthrough_environment,
            nextScreen = DEVICES_1,
            arrowState = PopupDashboardButton.ENVIRONMENT,
            buttonState = PopupDashboardButton.ENVIRONMENT),

    INHALE_EVENTS(layoutId = R.layout.walkthrough_inhale_events,
            nextScreen = ENVIRONMENT,
            arrowState = PopupDashboardButton.EVENTS,
            buttonState = PopupDashboardButton.EVENTS),

    INHALER_READY(layoutId = R.layout.walkthrough_inhaler_ready,
            nextScreen = INHALE_EVENTS,
            arrowState = PopupDashboardButton.NONE,
            buttonState = PopupDashboardButton.NONE),

    WELCOME(layoutId = R.layout.walkthrough_introduction,
            nextScreen = INHALER_READY,
            isBackButtonEnabled = false,
            arrowState = PopupDashboardButton.NONE,
            buttonState = PopupDashboardButton.NONE),

}
