//
// ScanIntroViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.device

import android.databinding.Bindable

import com.teva.respiratoryapp.BR
import com.teva.respiratoryapp.activity.model.InhalerRegistrationCommonState
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel
import com.teva.utilities.services.DependencyProvider

/**
 * ViewModel for the Scan Device Intro screen
 *
 * @param dependencyProvider Dependency Injection object.
 */
class ScanIntroViewModel(dependencyProvider: DependencyProvider)
    : FragmentViewModel(dependencyProvider), InhalerRegistrationCommonState.Listener {

    private val state: InhalerRegistrationCommonState = dependencyProvider.resolve()

    /**
     * A value indicating whether the common state has a device object.
     */
    @get:Bindable
    var hasDevice: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.hasDevice)
            }
        }

    /**
     * Method called by the BaseFragment when the fragment's onStop() lifecycle method is called.
     */
    override fun onStop() {
        super.onStop()

        state.removeListener(this)
    }

    /**
     * Method called by the BaseFragment when the fragment's onStart() lifecycle method is called.
     */
    override fun onStart() {
        super.onStart()

        state.addListener(this)
        hasDevice = state.isDeviceLoaded
    }

    /**
     * Touch handler for the Start Scanning button.
     */
    fun startScanning() {
        dependencyProvider.resolve<IntroEvents>().onStartScan()
    }

    /**
     * InhalerRegistrationCommonState.Listener method called when the Device property changes.
     */
    override fun onDeviceLoaded() {
        hasDevice = state.isDeviceLoaded
    }

    /**
     * Events produced by the ViewModel to request actions by the Activity.
     */
    interface IntroEvents {
        /**
         * Requests to navigate to the Scan Inhaler screen.
         */
        fun onStartScan()
    }
}
