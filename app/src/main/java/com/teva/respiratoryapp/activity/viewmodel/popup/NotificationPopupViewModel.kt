//
// NotificationPopupViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.popup

import android.databinding.Bindable
import com.teva.utilities.services.DependencyProvider
import com.teva.devices.model.DeviceManagerNotificationId
import com.teva.notifications.models.NotificationManager
import com.teva.notifications.services.notification.NotificationInfo
import com.teva.respiratoryapp.activity.viewmodel.SupportEvents
import com.teva.respiratoryapp.activity.viewmodel.dashboard.ContactSupportViewModel
import com.teva.respiratoryapp.models.notification.HyperlinkAction
import com.teva.respiratoryapp.models.notification.NotificationCategories
import com.teva.respiratoryapp.models.notification.NotificationCategory

/**
 * Viewmodel class for the notification popups.
 *
 * @param dependencyProvider The dependency injection mechanism.
 * @param notificationInfo The notification being displayed.
 */
class NotificationPopupViewModel(dependencyProvider: DependencyProvider,
                                 private val notificationInfo: NotificationInfo)
    : DashboardPopupViewModel(dependencyProvider) {

    /**
     * A value indicating whether the notification should be turned off.
     */
    @get:Bindable
    var isTurnOff: Boolean = false

    /**
     * The header text of the notification.
     */
    @get:Bindable
    var headerText: String? = null
        private set

    /**
     * The body text of the notification.
     */
    @get:Bindable
    var bodyText: String? = null
        private set

    /**
     * This field indicates the action to be taken when the  user clicks on a
     * hyperlink in the notification.
     */
    var hyperlinkAction: HyperlinkAction = HyperlinkAction.SHOW_FRAGMENT
        private set

    val notificationCategory: NotificationCategory

    init {

        notificationCategory = NotificationCategories.findCategory(notificationInfo)

        popupColor = notificationCategory.popupColor
        headerBarVisible = notificationCategory.headerBarVisible
        arrowState = notificationCategory.popupDashboardButton
        buttonState = PopupDashboardButton.ALL
        buttonsDimmed = false

        isCloseButtonVisible = notificationCategory.hasClose

        val dataMap = notificationInfo.notificationData

        if (notificationCategory.headerStringId != 0) {
            headerText = getString(notificationCategory.headerStringId, dataMap)
        }

        if (notificationCategory.bodyStringId != 0) {
            bodyText = getString(notificationCategory.bodyStringId, dataMap)
        }

        if (notificationCategory.buttonStringId != 0) {
            buttonText = getString(notificationCategory.buttonStringId, dataMap)
        }

        if (notificationCategory.hyperlinkStringId != 0) {
            hyperlinkText = getString(notificationCategory.hyperlinkStringId, dataMap)
        }

        hyperlinkAction = notificationCategory.hyperlinkAction

        updateOption()
    }

    /**
     * Gets a value indicating whether the header text is visible.
     */
    val isHeaderVisible: Boolean
        @Bindable
        get() = headerText != null

    /**
     * Gets a value indicating whether the body text is visible.
     */
    val isBodyVisible: Boolean
        @Bindable
        get() = bodyText != null

    /**
     * Loads the turnOff option from the Application Settings.
     */
    private fun updateOption() {
        val categoryId = notificationInfo.categoryId

        if (categoryId == DeviceManagerNotificationId.INHALATIONS_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION
                || categoryId == DeviceManagerNotificationId.INHALATIONS_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION_WITH_TIP) {

            val notificationManager = dependencyProvider.resolve<NotificationManager>()
            val settingName = DeviceManagerNotificationId.INHALATIONS_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION
            isTurnOff = !notificationManager.getReminderSettingByName(settingName)!!.isEnabled
        }
    }

    /**
     * Saves the turnOff option to the ApplicationSettings.
     */
    private fun saveOption() {
        val categoryId = notificationInfo.categoryId

        if (categoryId == DeviceManagerNotificationId.INHALATIONS_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION
                || categoryId == DeviceManagerNotificationId.INHALATIONS_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION_WITH_TIP) {
            if (isTurnOff) {
                val notificationManager = dependencyProvider.resolve<NotificationManager>()
                val settingName = DeviceManagerNotificationId.INHALATIONS_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION
                notificationManager.disableNotification(settingName)
            }
        }
    }

    /**
     * Click handler for the close button.
     */
    override fun onClose() {
        saveOption()
        dependencyProvider.resolve<Events>().onClose(notificationInfo)
    }

    /**
     * Click handler for the action button.
     */
    override fun onButton() {
        saveOption()
        dependencyProvider.resolve<Events>().onButton(notificationInfo)
    }

    /**
     * Click handler for the hyperlink.
     */
    override fun onHyperlink() {
        saveOption()
        if(hyperlinkAction == HyperlinkAction.SHOW_FRAGMENT) {
            dependencyProvider.resolve(Events::class.java).onHyperlink(notificationInfo)
        } else {
            dependencyProvider.resolve<ContactSupportViewModel.Events>().onContactSupport()
        }
    }

    /**
     * Click handler for the hyperlink in the notification body.
     */
    fun onBodyHyperlink() {
        saveOption()
        dependencyProvider.resolve<SupportEvents>().onSupport()
    }

    /**
     * Events produced by the viewmodel to request actions by the activity.
     */
    interface Events {
        /**
         * Indicates that the close button was clicked.
         */
        fun onClose(notificationInfo: NotificationInfo)

        /**
         * Indicates that the main button was clicked.
         */
        fun onButton(notificationInfo: NotificationInfo)

        /**
         * Indicates that the hyperlink was clicked.
         */
        fun onHyperlink(notificationInfo: NotificationInfo)
    }
}
