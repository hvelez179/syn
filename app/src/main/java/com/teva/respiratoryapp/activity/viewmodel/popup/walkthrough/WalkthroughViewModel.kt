//
// WalkthroughViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.popup.walkthrough

import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.view.popup.walkthrough.Walkthrough
import com.teva.respiratoryapp.activity.viewmodel.popup.DashboardPopupViewModel
import com.teva.respiratoryapp.activity.viewmodel.popup.PopupColor

/**
 * This class is the view model for the walkthrough screens.
 *
 * @param dependencyProvider    The dependency injection mechanism.
 * @param walkthrough           - the type of the walkthrough screen.
 * @param displayHowToUseTheApp - indicates if the walkthrough screens are displayed
 *                                in "how to use the app" mode or the "walkthrough" mode.
 */
class WalkthroughViewModel(dependencyProvider: DependencyProvider,
                           walkthrough: Walkthrough,
                           private val displayHowToUseTheApp: Boolean)
    : DashboardPopupViewModel(dependencyProvider) {

    /**
     * This flag indicates if back button should be disabled for the current screen.
     */
    private var disallowNavigatingBack: Boolean = false

    /**
     * The type of the next screen to be displayed.
     */
    var nextScreen: Walkthrough? = null
        private set

    /**
     * The events interface implementation from the dependency provider.
     */
    private val events: Events

    /**
     * Events produced by the viewmodel to request actions by the activity.
     */
    interface Events {
        /**
         * Handler for the next button click.
         *
         * @param walkthrough - the next screen to be displayed.
         */
        fun onNext(walkthrough: Walkthrough)

        /**
         * Handler for the ScanInhaler button click.
         */
        fun onScanInhaler()

        /**
         * Handler for completion of the walkthrough either
         * by clicking the close button or choosing to scan
         * inhalers later.
         */
        fun onDone()
    }

    init {
        popupColor = PopupColor.GRAY
        disallowNavigatingBack = false
        initializeWalkthroughScreen(walkthrough)
        events = dependencyProvider.resolve<Events>()
        headerBarVisible = false
    }

    /**
     * This method initializes the walkthrough screen by enabling or disabling buttons
     * and setting the next screen information.
     *
     * @param walkthrough - the type of the walkthrough screen.
     */
    private fun initializeWalkthroughScreen(walkthrough: Walkthrough) {
        isCloseButtonVisible = displayHowToUseTheApp
        arrowState = walkthrough.arrowState
        buttonState = walkthrough.buttonState
        buttonsDimmed = true

        isBackButtonVisible = walkthrough.isBackButtonEnabled
        isNextButtonVisible = walkthrough.isNextButtonEnabled
        nextScreen = walkthrough.nextScreen

        if (walkthrough == Walkthrough.INHALER_READY && displayHowToUseTheApp) {
            isBackButtonVisible = false
        }

        if (walkthrough == Walkthrough.SECURITY && displayHowToUseTheApp) {
            nextScreen = null
            isNextButtonVisible = false
        }

        if (walkthrough == Walkthrough.GET_STARTED) {
            buttonText = getString(R.string.scanDeviceNow_text)
            hyperlinkText = getString(R.string.scanDeviceLater_text)
            disallowNavigatingBack = true
        }
    }

    /**
     * This method handles the back button click.
     */
    override fun onBackButtonClicked() {
        onBackPressed()
    }

    /**
     * Method called when the hardware back button is pressed.
     */
    override fun onBackPressed() {
        if (!disallowNavigatingBack) {
            super.onBackPressed()
        }
    }

    /**
     * This method handles the next button click.
     */
    override fun onNextButtonClicked() {
        nextScreen?.let {
            events.onNext(it)
        }
    }

    /**
     * This method handles the click of the button displayed on the walkthrough screens,
     * currently the "Scan Inhaler" button.
     */
    override fun onButton() {
        events.onScanInhaler()
    }

    /**
     * This method handles the click of the hyperlink displayed on the walkthrough screens,
     * currently the "Scan inhalers later" hyperlink.
     */
    override fun onHyperlink() {
        events.onDone()
    }

    /**
     * This method handles closing of the walkthrough screens using the close button.
     */
    override fun onClose() {
        events.onDone()
    }
}
