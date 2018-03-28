////////////////////////////////////////////////////////////////////////////////
// NotificationCategory.java
// app
//
// Copyright © 2017 Teva. All rights reserved
////////////////////////////////////////////////////////////////////////////////

package com.teva.respiratoryapp.models.notification

import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.popup.PopupColor
import com.teva.respiratoryapp.activity.viewmodel.popup.PopupDashboardButton
import kotlin.reflect.KClass

enum class HyperlinkAction {
    SHOW_FRAGMENT,
    SHOW_SUPPORT
}

/**
 * This class is the base NotificationCategory
 * @property categoryId               The id of the NotificationCategory.
 * @property notificationType         The type of the NotificationCategory.
 * @property fragmentClass The class of the fragment to display for this notification.
 * @property showWhenInForeground     Indicathes whether the notification should show a popup when the app is in the foreground.
 * @property showWhenFromNotification Indicates whether the notification should show a popup when the user clicks on the notification in the notification shade.
 * @property popupColor The color of the popup header and button
 * @property popupDashboardButton The dashboard button or widget to point to.
 * @property hasClose Indicates whether the popup should have a close button.
 * @property imageId The id of an image on the popup
 * @property headerStringId The header text string id
 * @property bodyStringId The body text string id
 * @property buttonStringId The button text string id
 * @property hyperlinkStringId The hyperlink text string id
 * @property buttonNavigationClass The fragment class that the button will navigate to
 * @property hyperlinkNavigationClass The fragment class that the hyperlink will navigate to
 * @property stackTag The stack tag to use for the popup
 */
class NotificationCategory(
        val categoryId: String,
        val notificationType: NotificationType,
        val fragmentClass: KClass<*>,
        val showWhenInForeground: Boolean = true,
        val showWhenFromNotification: Boolean = true,
        val popupColor: PopupColor = PopupColor.WHITE,
        val headerBarVisible: Boolean = false,
        val popupDashboardButton: PopupDashboardButton = PopupDashboardButton.NONE,
        val hasClose: Boolean = false,
        val layoutId: Int = R.layout.text_header_popup,
        val headerStringId: Int = 0,
        val bodyStringId: Int = 0,
        val notificationStringId: Int? = null,
        val buttonStringId: Int = 0,
        val hyperlinkStringId: Int = 0,
        val buttonNavigationClass: KClass<*>? = null,
        val hyperlinkNavigationClass: KClass<*>? = null,
        val stackTag: String? = null,
        val hyperlinkAction: HyperlinkAction = HyperlinkAction.SHOW_FRAGMENT,
        val bodyImageSource: Int? = null,
        val bodyImageTextId: Int? = null,
        val imageFragmentClass: KClass<*>? = null) {

    /**
     * Gets the priority of the notification category.
     */
    val priority: Int
        get() = notificationType.priority
}
