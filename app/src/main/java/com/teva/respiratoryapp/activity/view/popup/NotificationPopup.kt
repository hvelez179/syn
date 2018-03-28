//
// NotificationPopup.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view.popup

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.teva.common.services.analytics.enumerations.AnalyticsScreen
import com.teva.common.utilities.splitWordsSeparatedByUnderscoresAndCapitalize
import com.teva.devices.model.DeviceManagerNotificationId
import com.teva.notifications.services.notification.NotificationInfo
import com.teva.respiratoryapp.BR
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.popup.NotificationPopupViewModel
import com.teva.respiratoryapp.models.notification.NotificationCategories

/**
 * Base fragment class for Notification popups.
 */
class NotificationPopup : DashboardPopupFragment() {

    /**
     * Gets the NotificationData for this notification fragment.
     */
    var notificationInfo: NotificationInfo? = null
        private set

    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        if (fragmentArguments != null) {
            notificationInfo = getNotificationDataFromArguments(fragmentArguments)
            val notificationCategory = NotificationCategories.findCategory(notificationInfo?.categoryId!!)
            screen = AnalyticsScreen.Notification(notificationCategory.notificationType.toString().splitWordsSeparatedByUnderscoresAndCapitalize())
        }

        viewModel = NotificationPopupViewModel(dependencyProvider!!, notificationInfo!!)
    }

    /**
     * Creates the content view for this popup.
     *
     * @param inflater           The LayoutInflator for the fragment.
     * @param container
     */
    override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup): View {
        val viewModel = viewModel as NotificationPopupViewModel?

        val categoryId = notificationInfo?.categoryId

        val layoutId = viewModel?.notificationCategory?.layoutId ?: R.layout.text_header_popup

//        if (categoryId == DeviceManagerNotificationId.INHALATIONS_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION
//                || categoryId == DeviceManagerNotificationId.INHALATIONS_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION_WITH_TIP) {
//            layoutId = R.layout.text_header_with_option_popup
//        } else if (viewModel!!.headerImageId == 0) {
//            layoutId = R.layout.text_header_popup
//        } else {
//            if (viewModel.headerText.isNullOrEmpty()) {
//                layoutId = R.layout.image_header_popup
//            } else {
//                layoutId = R.layout.text_and_image_header_popup
//            }
//        }

        val binding = DataBindingUtil.inflate<ViewDataBinding>(inflater, layoutId, container, false)
        binding.setVariable(BR.viewmodel, viewModel)

        return binding.root
    }

    companion object {
        val NOTIFICATION_BUNDLE_KEY = "notification"

        /**
         * Creates an arguments bundle for Notification fragments.
         *
         * @param data The NotificationData to be represented by the fragment.
         * @return A Bundle to be used as the fragment's arguments.
         */
        fun createArguments(data: NotificationInfo): Bundle {
            val bundle = Bundle()
            bundle.putParcelable(NOTIFICATION_BUNDLE_KEY, data)

            return bundle
        }

        /**
         * Parses an arguments Bundle and extracts the NotificationData object.
         *
         * @param bundle The arguments bundle.
         * @return The NotificationData contained within the Bundle.
         */
        fun getNotificationDataFromArguments(bundle: Bundle): NotificationInfo? {
            val notificationInfo = bundle.getParcelable<NotificationInfo>(NOTIFICATION_BUNDLE_KEY)

            return notificationInfo
        }
    }
}
