///
// SystemAlertManagernager.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.respiratoryapp.services.alert

import android.app.Dialog
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel

/**
 * Provides a facility to display alert dialogs.
 */
interface SystemAlertManager {

    /**
     * Shows a custom dialog.
     *
     * @param id The id of the dialog that is used to determine the view to use.
     * @param viewModel The viewmodel to use for the dialog.
     */
    fun showDialog(id: String, viewModel: FragmentViewModel)

    /**
     * Shows an alert with a message and an OK button.
     *
     * @param message The message to display.
     * @param messageId The string id of the message to display.
     * @param title The alert title
     * @param titleId The string id of the alert title
     * @param secondaryButtonTextId The text of the positive button
     * @param secondaryButtonTextId The text of the negative button
     * @param onClick The button click callback.
     * @param onClickClose The button click callback with a boolean close return
     */
    fun showAlert(id: String? = null,
                  message: String? = null,
                  messageId: Int? = null,
                  title: String? = null,
                  titleId: Int? = null,
                  primaryButtonTextId: Int? = R.string.ok_text,
                  secondaryButtonTextId: Int? = null,
                  onClick: ((AlertButton)->Unit)? = null,
                  onClickClose: ((AlertButton)->Boolean)? = null,
                  imageId: Int? = null,
                  imageTextId: Int? = null,
                  onImageClick: (() -> Unit)? = null): Dialog

    /**
     * Shows a query alert with a message and YES and NO buttons.
     *
     * @param message The message to display.
     * @param messageId The string id of the message to display.
     * @param title The alert title
     * @param titleId The string id of the alert title
     * @param primaryButtonTextId The text of the positive button
     * @param secondaryButtonTextId The text of the negative button
     * @param onClick The button click callback.
     */
    fun showQuery(id: String? = null,
                  message: String? = null,
                  messageId: Int? = null,
                  title: String? = null,
                  titleId: Int? = null,
                  primaryButtonTextId: Int = R.string.yes_text,
                  secondaryButtonTextId: Int = R.string.no_text,
                  onClick: (AlertButton)->Unit = {}): Dialog

    /**
     * This method closes all displayed alerts.
     */
    fun closeAllAlerts()
}
