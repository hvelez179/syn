//
// DashboardPopupViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.popup

import android.databinding.Bindable
import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel

/**
 * Base class for popup viewmodels that provides properties to show marker triangles pointing to dashboard wdigets.
 *
 * @param dependencyProvider The dependency injection mechanism.
 */
open class DashboardPopupViewModel(dependencyProvider: DependencyProvider)
    : FragmentViewModel(dependencyProvider) {

    /**
     * A value indicating whether the close button is visible.
     */
    @get:Bindable
    var isCloseButtonVisible: Boolean = false

    /**
     * The text of the main button.
     */
    @get:Bindable
    var buttonText: String? = null

    /**
     * The text of the hyperlink.
     */
    @get:Bindable
    var hyperlinkText: String? = null

    /**
     * An enumeration indicating the color of the popup and it's main button.
     */
    var popupColor: PopupColor? = null
    var headerBarVisible = true

    var arrowState = PopupDashboardButton.ALL
    var buttonState = PopupDashboardButton.ALL
    var buttonsDimmed = false

    /**
     * A value indicating if back button is visible.
     */
    @get:Bindable
    var isBackButtonVisible: Boolean = false
        protected set
    /**
     * A value indicating if next button is visible.
     */
    @get:Bindable
    var isNextButtonVisible: Boolean = false
        protected set

    init {
        isBackButtonVisible = false
        isNextButtonVisible = false
    }

    /**
     * Handler for the back button click.
     * Override in derived class to provide functionality.
     */
    open fun onBackButtonClicked() {}

    /**
     * Handler for the next button click.
     * Override in derived class to provide functionality.
     */
    open fun onNextButtonClicked() {}

    /**
     * Gets a value indicating whether the main button is visible.
     */
    val isButtonVisible: Boolean
        @Bindable
        get() = buttonText != null

    /**
     * Gets a value indicating whether the hyperlink is visible.
     */
    val isHyperlinkVisible: Boolean
        @Bindable
        get() = hyperlinkText != null

    /**
     * Click handler for the action button.
     */
    open fun onButton() {
        onBackPressed()
    }

    /**
     * Click handler for the hyperlink.
     */
    open fun onHyperlink() {}

    /**
     * Click handler for the close button.
     */
    open fun onClose() {
        onBackPressed()
    }
}
