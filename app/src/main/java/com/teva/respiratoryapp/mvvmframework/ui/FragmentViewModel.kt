//
// FragmentViewModel.java
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.mvvmframework.ui

import android.databinding.BaseObservable
import android.os.Bundle
import android.support.annotation.MainThread
import android.view.MenuItem

import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.utilities.utilities.Logger

import com.teva.utilities.utilities.Logger.Level.VERBOSE

/**
 * Base class for fragment ViewModels.
 *
 * @param dependencyProvider The dependency injection mechanism.
 */
@MainThread
open class FragmentViewModel(protected var dependencyProvider: DependencyProvider) : BaseObservable() {

    protected var logger: Logger = Logger(this.javaClass.simpleName)
    protected val localizationService: LocalizationService = dependencyProvider.resolve()

    /**
     * Back pressed callback to override the one found in the dependency provider.
     * Used for custom dialogs where the viewmodel is created before the dialog.
     */
    var onBackPressedCallback: (() -> Unit)? = null

    /**
     * Restores the current state of the fragment from a saved instance state.
     *
     * @param savedInstanceState The Bundle to restore the state from.
     */
    open fun restoreInstanceState(savedInstanceState: Bundle?) {
        logger.log(VERBOSE, "restoreInstanceState")
    }

    /**
     * Saves the current state of the fragment into a saved instance state.
     *
     * @param savedInstanceState The Bundle to save the state into.
     */
    open fun saveInstanceState(savedInstanceState: Bundle) {
        logger.log(VERBOSE, "saveInstanceState")
    }

    /**
     * Method called by the BaseFragment when the fragment's onCreate() lifecycle method is called.
     */
    fun onCreate() {}

    /**
     * Method called by the BaseFragment when the fragment's onDestroy() lifecycle method is called.
     */
    fun onDestroy() {}

    /**
     * Method called by the BaseFragment when the fragment's onStart() lifecycle method is called.
     */
    open fun onStart() {

    }

    /**
     * Method called by the BaseFragment when the fragment's onStop() lifecycle method is called.
     */
    open fun onStop() {

    }

    /**
     * Method called by the BaseFragment when the fragment's onResume() lifecycle method is called.
     */
    open fun onResume() {

    }

    /**
     * Method called by the BaseFragment when the fragment's onPause() lifecycle method is called.
     */
    open fun onPause() {

    }

    /**
     * Method called by the BaseFragment to deliver fragment results.
     */
    open fun onResult(result: Bundle) {

    }

    /**
     * Method called by the BaseFragment when a toolbar menu item is clicked.
     */
    open fun onMenuItem(item: MenuItem): Boolean {
        return false
    }

    /**
     * Method called by the BaseFragment when the toolbar navigation button is pressed.
     */
    fun onNavigation() {
        onBackPressed()
    }

    /**
     * Method called by the BaseFragment when the hardware back button is pressed.
     */
    open fun onBackPressed() {
        // use the override callback if it's been set, otherwise get the callback
        // from the dependency provider.
        if (onBackPressedCallback != null) {
            onBackPressedCallback!!()
        } else {
            dependencyProvider.resolve<NavigationEvents>().onBackPressed()
        }
    }

    /**
     * gets a localized string
     *
     * @param stringId The string id.
     * @return The localized string.
     */
    protected fun getString(stringId: Int): String {
        return localizationService.getString(stringId)
    }

    /**
     * gets a localized string
     *
     * @param stringId The string id.
     * @param stringReplacements The map of string replacements.
     * @return The localized string.
     */
    protected fun getString(stringId: Int, stringReplacements: Map<String, Any>?): String {
        return localizationService.getString(stringId, stringReplacements)
    }

    /**
     * Common Navigation events for viewmodels
     */
    interface NavigationEvents {
        /**
         * Requests to navigate to the previous screen.
         */
        fun onBackPressed(noAnimation: Boolean = false)
    }
}
