//
// AlertViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel

import android.databinding.Bindable
import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.BR
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel
import com.teva.respiratoryapp.services.alert.AlertButton


class AlertViewModel(
        dependencyProvider: DependencyProvider,
        val id: String?,
        val title: String?,
        val message: String?,
        val confirmation: String?,
        val primaryAction: String?,
        val secondaryAction: String?,
        val onClick: ((AlertButton)->Boolean)?,
        val imageId: Int? = null,
        val imageTextId: Int? = null,
        val onImageClick: (() -> Unit)? = null)
    : FragmentViewModel(dependencyProvider) {

    /**
     * Property used to hold the state of the confirm checkbox.
     */
    @get:Bindable
    @get:JvmName("getConfirmed")
    var isConfirmed: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.confirmed)
        }

    /**
     * Click handler for the primary action button.
     */
    fun onPrimaryAction() {

        if (onClick?.invoke(AlertButton.PRIMARY) != false) {
            onClose()
        }
    }

    /**
     * Click handler for the secondary action button.
     */
    fun onSecondaryAction() {

        if (onClick?.invoke(AlertButton.SECONDARY) != false) {
            onClose()
        }
    }

    /**
     * Click handler for the close button.
     */
    fun onClose() {
        onBackPressed()
    }

    /**
     * Click handler for the image or associated text click event.
     */
    fun onImageClicked() {
        onClose()
        onImageClick?.invoke()
    }
}

