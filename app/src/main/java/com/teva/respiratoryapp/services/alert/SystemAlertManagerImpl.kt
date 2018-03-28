//
// SystemAlertManagerImpl.java
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.services.alert

import android.app.AlertDialog
import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.teva.common.services.analytics.AnalyticsService
import com.teva.common.services.analytics.enumerations.AnalyticsScreen
import com.teva.common.utilities.LocalizationService
import com.teva.utilities.utilities.Logger
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.view.Alert
import com.teva.respiratoryapp.activity.viewmodel.AlertViewModel
import com.teva.respiratoryapp.mvvmframework.ui.BaseActivity
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel
import kotlin.reflect.full.primaryConstructor

/**
 * Provides a facility to display alert dialogs.
 * @param activity The parent activity for the alert dialog.
 * @param alertConfigurationProvider The object used to determine the layout to use for a dialog.
 */

class SystemAlertManagerImpl(
        private val activity: BaseActivity,
        private val alertConfigurationProvider: AlertConfigurationProvider?)
    : SystemAlertManager {

    private val displayedDialogSet = HashSet<Dialog>()
    private val logger = Logger(SystemAlertManagerImpl::class)
    private val localizationService: LocalizationService = activity.dependencyProvider.resolve()
    private val analyticsService = activity.dependencyProvider.resolve<AnalyticsService>()


    /**
     * Shows a custom dialog.
     *
     * @param id The id of the dialog that is used to determine the view to use.
     * @param viewModel The viewmodel to use for the dialog.
     */
    override fun showDialog(id: String, viewModel: FragmentViewModel) {
        val config = alertConfigurationProvider?.getAlertConfiguration(id)

        val dialog = config?.dialogClass?.primaryConstructor?.call(activity, viewModel) as? Dialog
        if (dialog != null) {
            dialog.show()
            dialog.setOnDismissListener { displayedDialogSet.remove(dialog) }
            displayedDialogSet.add(dialog)
        } else {
            logger.log(Logger.Level.WARN, "showDialog($id) - no dialog class provided by fragment.")
        }
    }

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
    override fun showAlert(id: String?,
                           message: String?,
                           messageId: Int?,
                           title: String?,
                           titleId: Int?,
                           primaryButtonTextId: Int?,
                           secondaryButtonTextId: Int?,
                           onClick: ((AlertButton) -> Unit)?,
                           onClickClose: ((AlertButton) -> Boolean)?,
                           imageId: Int?,
                           imageTextId: Int?,
                           onImageClick: (() -> Unit)?): Dialog {

        val messageText = when {
            message != null -> message
            messageId != null -> localizationService.getString(messageId)
            else -> null
        }

        val titleText = when {
            title != null -> title
            titleId != null -> localizationService.getString(titleId)
            else -> null
        }

        val primaryButtonText = if (primaryButtonTextId != null) {
            localizationService.getString(primaryButtonTextId)
        } else {
            null
        }

        val secondaryButtonText = if (secondaryButtonTextId != null) {
            localizationService.getString(secondaryButtonTextId)
        } else {
            null
        }

        val clickHandler = onClickClose ?: {alertButton -> onClick?.invoke(alertButton); true }

        val dialog = createDialog(
                id, titleText, messageText, null,
                primaryButtonText, secondaryButtonText,
                clickHandler,
                imageId,
                imageTextId,
                onImageClick)

        if(titleText != null) {
            analyticsService.enterScreen(AnalyticsScreen.Alert(titleText).screenName)
        }

        dialog.show()

        dialog.setOnDismissListener {
            displayedDialogSet.remove(dialog)
            if(titleText != null) {
                analyticsService.leaveScreen(AnalyticsScreen.Alert(titleText).screenName)
            }
        }
        displayedDialogSet.add(dialog)

        return dialog
    }

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
    override fun showQuery(id: String?, message: String?, messageId: Int?, title: String?, titleId: Int?, primaryButtonTextId: Int, secondaryButtonTextId: Int, onClick: (AlertButton) -> Unit): Dialog {
        return showAlert(id, message, messageId,
                title, titleId, primaryButtonTextId,
                secondaryButtonTextId, onClick, null)
    }

    /**
     * Creates an alert dialog.
     *
     * @param id The id of the dialog
     * @param title The title of the dialog
     * @param message The message text of the dialog
     * @param confirmation The text of the confirmation checkbox
     * @param primaryAction The text of the primary action button
     * @param secondaryAction The text of the secondary action button
     * @param onClick The click callback function
     */
    private fun createDialog(id: String?,
                             title: String?,
                             message: String?,
                             confirmation: String?,
                             primaryAction: String?,
                             secondaryAction: String?,
                             onClick: ((AlertButton)->Boolean)?,
                             imageId: Int?,
                             imageTextId: Int?,
                             onImageClick: (() -> Unit)?): Dialog {

        val config = alertConfigurationProvider?.getAlertConfiguration(id) ?: defaultConfig


        val dialog = when (config.type) {
            AlertType.ALERT_DIALOG -> {
                val builder = AlertDialog.Builder(activity)
                        .setView(createCustomView(message, title))
                        .setCancelable(false)

                if (primaryAction != null) {
                    builder.setPositiveButton(primaryAction, { dialog, which ->
                        onClick?.invoke(AlertButton.PRIMARY)
                    })
                }

                if (secondaryAction != null) {
                    builder.setNegativeButton(secondaryAction, { dialog, which ->
                        onClick?.invoke(AlertButton.SECONDARY)
                    })
                }

                builder.create()
            }

            AlertType.FULL_SCREEN_WITH_IMAGE -> {
                val alert = Alert(activity, config.layoutId)
                alert.viewModel = AlertViewModel(alert.dependencyProvider, id, title,
                        message, confirmation, primaryAction, secondaryAction, onClick, imageId, imageTextId, onImageClick)

                alert.window.attributes.windowAnimations = R.style.FadeStyle

                alert
            }

            else -> {
                val alert = Alert(activity, config.layoutId)
                alert.viewModel = AlertViewModel(alert.dependencyProvider, id, title,
                        message, confirmation, primaryAction, secondaryAction, onClick)

                alert.window.attributes.windowAnimations = R.style.FadeStyle

                alert
            }
        }

        return dialog
    }

    /**
     * Creates a custom alert layout that allows for multi-line titles.
     *
     * @param message The message text.
     * @param title The title text.
     * @return The custom alert view.
     */
    private fun createCustomView(message: String?, title: String?): View {
        val view = LayoutInflater.from(activity).inflate(R.layout.alert_layout, null)

        val titleView = view.findViewById<TextView>(android.R.id.title)
        val messageView = view.findViewById<TextView>(android.R.id.message)

        if (title.isNullOrEmpty()) {
            titleView.visibility = View.GONE
        } else {
            titleView.text = title
        }

        if (message.isNullOrEmpty()) {
            messageView.visibility = View.GONE
        } else {
            messageView.text = message
        }

        return view
    }

    /**
     * This method closes all displayed alerts.
     */
    override fun closeAllAlerts() {
        displayedDialogSet.forEach { dialog -> dialog.dismiss() }
    }

    companion object {
        val defaultConfig = AlertConfiguration(AlertType.FULL_SCREEN, R.layout.alert_fragment)
    }

}
