package com.teva.respiratoryapp.services.alert

import com.teva.respiratoryapp.activity.view.Alert
import kotlin.reflect.KClass

/**
 * Interface used to retrieve the layout id to use for an alert.
 */
interface AlertConfigurationProvider {
    /**
     * Gets the layout id for an alert.
     *
     * @param id The id of the dialog
     * @return The layout id to use or null to use the default layout.
     */
    fun getAlertConfiguration(id: String?): AlertConfiguration?
}

/**
 * Types of alert dialogs
 */
enum class AlertType {
    ALERT_DIALOG,
    CUSTOM_DIALOG,
    FULL_SCREEN,
    FULL_SCREEN_WITH_IMAGE
}

/**
 * Describes the configuration of an alert.
 *
 * @property type The alert type
 * @property layoutId The layout id for full screen alerts.
 */
data class AlertConfiguration(
        val type: AlertType,
        val layoutId: Int = 0,
        val dialogClass: KClass<*> = Alert::class)