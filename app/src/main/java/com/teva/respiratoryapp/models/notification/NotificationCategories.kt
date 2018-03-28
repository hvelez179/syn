package com.teva.respiratoryapp.models.notification


import com.teva.cloud.models.CloudManagerNotificationId
import com.teva.devices.model.DeviceManagerNotificationId
import com.teva.environment.models.EnvironmentNotificationId
import com.teva.notifications.services.notification.NotificationInfo
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.view.EnvironmentFragment
import com.teva.respiratoryapp.activity.view.InstructionsForUseFragment
import com.teva.respiratoryapp.activity.view.SupportFragment
import com.teva.respiratoryapp.activity.view.popup.DsaPopup
import com.teva.respiratoryapp.activity.view.popup.NotificationPopup
import com.teva.respiratoryapp.activity.view.tracker.ReportFragment
import com.teva.respiratoryapp.activity.viewmodel.popup.PopupColor
import com.teva.respiratoryapp.activity.viewmodel.popup.PopupDashboardButton
import com.teva.respiratoryapp.models.engagementbooster.EngagementBoosterNotificationId
import com.teva.userfeedback.model.DSANotificationId

/**
 * This class contains the collection of NotificationCategory objects for the AsthmaApp notifications.
 */
object NotificationCategories {
    internal val categories: Map<String, NotificationCategory> = createCategories().associateBy { it.categoryId }


    internal fun createCategories(): List<NotificationCategory> {

        return listOf(
                NotificationCategory(
                        categoryId = DSANotificationId.DSA_REMINDER,
                        notificationType = NotificationType.ASK_USER_FEELING,
                        fragmentClass = DsaPopup::class,
                        popupDashboardButton = PopupDashboardButton.DSA,
                        headerStringId = R.string.app_name,
                        bodyStringId = R.string.dailyUserFeelingReminderNotificationCenter_text,
                        buttonStringId = R.string.ok_text),

                // Device Dosage
                NotificationCategory(
                        categoryId = DeviceManagerNotificationId.DEVICE_NEAR_EMPTY,
                        notificationType = NotificationType.NEAR_EMPTY,
                        fragmentClass = NotificationPopup::class,
                        layoutId = R.layout.device_near_empty_popup,
                        popupColor = PopupColor.WHITE,
                        headerBarVisible = false,
                        popupDashboardButton = PopupDashboardButton.DEVICES,
                        bodyStringId = R.string.deviceNearEmpty_text,
                        buttonStringId = R.string.ok_text),

                NotificationCategory(
                        categoryId = DeviceManagerNotificationId.DEVICE_OVERUSE,
                        notificationType = NotificationType.OVERUSE,
                        fragmentClass = NotificationPopup::class,
                        layoutId = R.layout.text_header_popup_2,
                        showWhenFromNotification = false,
                        popupColor = PopupColor.RED,
                        headerBarVisible = true,
                        popupDashboardButton = PopupDashboardButton.EVENTS,
                        headerStringId = R.string.medicationOveruse_header_text,
                        bodyStringId = R.string.medicationOveruse_body_text,
                        buttonStringId = R.string.ok_text,
                        hyperlinkStringId = R.string.contact_teva_support,
                        hyperlinkAction = HyperlinkAction.SHOW_SUPPORT),

                // Connectivity
                NotificationCategory(
                        categoryId = DeviceManagerNotificationId.CONNECTIVITY_NOW_CONNECTED,
                        notificationType = NotificationType.CONNECTIVITY_NOW_CONNECTED,
                        fragmentClass = NotificationPopup::class,
                        popupDashboardButton = PopupDashboardButton.DEVICES,
                        bodyStringId = R.string.connectivityNowConnected_text,
                        buttonStringId = R.string.ok_text),

                NotificationCategory(
                        categoryId = DeviceManagerNotificationId.CONNECTIVITY_DISCONNECTED_7_DAYS_OR_MORE,
                        notificationType = NotificationType.CONNECTIVITY_DISCONNECTED_7_DAYS_OR_MORE,
                        fragmentClass = NotificationPopup::class,
                        popupDashboardButton = PopupDashboardButton.DEVICES,
                        bodyStringId = R.string.connectivityDisconnected7DaysOrMore_text,
                        buttonStringId = R.string.ok_text),

                // InhalationFeedback
                NotificationCategory(
                        categoryId = DeviceManagerNotificationId.INHALATIONS_FEEDBACK_GOOD_INHALATION,
                        notificationType = NotificationType.INHALATION_FEEDBACK_GOOD_INHALATION,
                        fragmentClass = NotificationPopup::class,
                        showWhenFromNotification = false,
                        popupColor = PopupColor.GREEN,
                        headerBarVisible = true,
                        popupDashboardButton = PopupDashboardButton.EVENTS,
                        headerStringId = R.string.inhalationsFeedbackGoodInhalation_text,
                        buttonStringId = R.string.ok_text,
                        hyperlinkStringId = R.string.menuInstructionsForUseTitle_text,
                        hyperlinkNavigationClass = InstructionsForUseFragment::class),

                NotificationCategory(
                        categoryId = DeviceManagerNotificationId.INHALATIONS_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION,
                        notificationType = NotificationType.INHALATION_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION,
                        fragmentClass = NotificationPopup::class,
                        layoutId = R.layout.text_header_with_option_popup,
                        showWhenFromNotification = false,
                        popupColor = PopupColor.GREEN,
                        headerBarVisible = true,
                        popupDashboardButton = PopupDashboardButton.EVENTS,
                        headerStringId = R.string.inhalationsFeedbackGoodInhalation_text,
                        buttonStringId = R.string.ok_text,
                        hyperlinkStringId = R.string.menuInstructionsForUseTitle_text,
                        hyperlinkNavigationClass = InstructionsForUseFragment::class),

                NotificationCategory(
                        categoryId = DeviceManagerNotificationId.INHALATIONS_FEEDBACK_LOW_INHALATION,
                        notificationType = NotificationType.INHALATION_FEEDBACK_LOW_INHALATION,
                        fragmentClass = NotificationPopup::class,
                        showWhenFromNotification = false,
                        popupColor = PopupColor.GREEN,
                        headerBarVisible = true,
                        popupDashboardButton = PopupDashboardButton.EVENTS,
                        headerStringId = R.string.inhalationsFeedbackGoodInhalation_text,
                        bodyStringId = R.string.inhalationsFeedbackLowInhalation_text,
                        buttonStringId = R.string.ok_text,
                        hyperlinkStringId = R.string.menuInstructionsForUseTitle_text,
                        hyperlinkNavigationClass = InstructionsForUseFragment::class),

                NotificationCategory(
                        categoryId = DeviceManagerNotificationId.INHALATIONS_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION_WITH_TIP,
                        notificationType = NotificationType.INHALATION_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION_WITH_TIP,
                        fragmentClass = NotificationPopup::class,
                        layoutId = R.layout.text_header_with_option_popup,
                        showWhenFromNotification = false,
                        popupColor = PopupColor.GREEN,
                        headerBarVisible = true,
                        popupDashboardButton = PopupDashboardButton.EVENTS,
                        headerStringId = R.string.inhalationsFeedbackGoodInhalation_text,
                        bodyStringId = R.string.inhalationsFeedbackLowInhalation_text,
                        buttonStringId = R.string.ok_text,
                        hyperlinkStringId = R.string.menuInstructionsForUseTitle_text,
                        hyperlinkNavigationClass = InstructionsForUseFragment::class),

                NotificationCategory(
                        categoryId = DeviceManagerNotificationId.INHALATIONS_FEEDBACK_NO_INHALATION,
                        notificationType = NotificationType.INHALATION_FEEDBACK_NO_INHALATION,
                        fragmentClass = NotificationPopup::class,
                        showWhenFromNotification = false,
                        popupColor = PopupColor.RED,
                        headerBarVisible = true,
                        popupDashboardButton = PopupDashboardButton.EVENTS,
                        headerStringId = R.string.inhalationsFeedbackNoInhalation_text,
                        bodyStringId = R.string.inhalationsFeedbackNoInhalation_part2_text,
                        buttonStringId = R.string.ok_text,
                        hyperlinkStringId = R.string.menuInstructionsForUseTitle_text,
                        hyperlinkNavigationClass = InstructionsForUseFragment::class),

                NotificationCategory(
                        categoryId = DeviceManagerNotificationId.INHALATIONS_FEEDBACK_HIGH_INHALATION,
                        notificationType = NotificationType.INHALATION_FEEDBACK_HIGH_INHALATION,
                        fragmentClass = NotificationPopup::class,
                        layoutId = R.layout.blocked_vent_popup,
                        showWhenFromNotification = false,
                        popupColor = PopupColor.RED,
                        headerBarVisible = false,
                        popupDashboardButton = PopupDashboardButton.EVENTS,
                        headerStringId = R.string.inhalation_error_feedback_text,
                        bodyStringId = R.string.inhalationsFeedbackHighInhalation_text,
                        buttonStringId = R.string.ok_text,
                        hyperlinkStringId = R.string.menuInstructionsForUseTitle_text,
                        hyperlinkNavigationClass = InstructionsForUseFragment::class),

                NotificationCategory(
                        categoryId = DeviceManagerNotificationId.INHALATIONS_FEEDBACK_EXHALATION,
                        notificationType = NotificationType.INHALATION_FEEDBACK_EXHALATION,
                        fragmentClass = NotificationPopup::class,
                        showWhenFromNotification = false,
                        popupColor = PopupColor.RED,
                        headerBarVisible = true,
                        popupDashboardButton = PopupDashboardButton.EVENTS,
                        headerStringId = R.string.inhalationsFeedbackExhalation_text,
                        bodyStringId = R.string.inhalationsFeedbackExhalation_part2_text,
                        buttonStringId = R.string.ok_text,
                        hyperlinkStringId = R.string.menuInstructionsForUseTitle_text,
                        hyperlinkNavigationClass = InstructionsForUseFragment::class),

                NotificationCategory(
                        categoryId = DeviceManagerNotificationId.INHALATIONS_FEEDBACK_SUBOPTIMAL_INHALATIONS,
                        notificationType = NotificationType.INHALATION_FEEDBACK_SUBOPTIMAL_INHALATIONS,
                        fragmentClass = NotificationPopup::class,
                        layoutId = R.layout.text_header_popup_2,
                        showWhenFromNotification = false,
                        popupColor = PopupColor.RED,
                        headerBarVisible = true,
                        popupDashboardButton = PopupDashboardButton.EVENTS,
                        headerStringId = R.string.inhalationsFeedbackSuboptimalInhalations_text,
                        bodyStringId = R.string.inhalationsFeedbackSuboptimalInhalations_part2_text,
                        buttonStringId = R.string.ok_text,
                        hyperlinkStringId = R.string.menuInstructionsForUseTitle_text,
                        hyperlinkNavigationClass = InstructionsForUseFragment::class),

                NotificationCategory(
                        categoryId = DeviceManagerNotificationId.SYSTEM_ERROR_DETECTED,
                        notificationType = NotificationType.SYSTEM_ERROR_DETECTED,
                        fragmentClass = NotificationPopup::class,
                        layoutId = R.layout.system_error_popup,
                        popupColor = PopupColor.RED,
                        headerBarVisible = true,
                        popupDashboardButton = PopupDashboardButton.NONE,
                        headerStringId = R.string.systemErrorDetected_text,
                        bodyStringId = R.string.systemErrorDetected_part2_text,
                        buttonStringId = R.string.ok_text,
                        hyperlinkStringId = R.string.contact_teva_support,
                        hyperlinkAction = HyperlinkAction.SHOW_SUPPORT),


                NotificationCategory(
                        categoryId = EngagementBoosterNotificationId.CREATE_USER_REPORT,
                        notificationType = NotificationType.CREATE_USER_REPORT,
                        fragmentClass = NotificationPopup::class,
                        layoutId = R.layout.engagement_booster_report,
                        popupDashboardButton = PopupDashboardButton.REPORT,
                        bodyStringId = R.string.engagementBoosterCreateUserReport_text,
                        buttonStringId = R.string.openUserReport_text,
                        hyperlinkStringId = R.string.dismiss_text,
                        buttonNavigationClass = ReportFragment::class),

                /**
                 *  When DSA is launched from the dashboard or the environment reminder
                 *  the stack tag is set so that the correct view is displayed after
                 *  closing the DSA confirmation view.
                 *  However, when launched from the engagement booster, the stack tag
                 *  information is not set resulting in a blank screen after closing the
                 *  DSA confirmation view. The stack tag is being added here to overcome
                 *  this issue.
                 */
                NotificationCategory(
                        categoryId = EngagementBoosterNotificationId.DAILY_SELF_ASSESSMENT_TOOL,
                        notificationType = NotificationType.DAILY_SELF_ASSESSMENT_TOOL,
                        fragmentClass = NotificationPopup::class,
                        layoutId = R.layout.engagement_booster_dsa,
                        popupDashboardButton = PopupDashboardButton.DSA,
                        bodyStringId = R.string.engagementBoosterDailySelfAssessmentTool_text,
                        buttonStringId = R.string.openDailySelfAssessmentTool_text,
                        hyperlinkStringId = R.string.dismiss_text,
                        buttonNavigationClass = DsaPopup::class),

                NotificationCategory(
                        categoryId = EngagementBoosterNotificationId.ENVIRONMENT,
                        notificationType = NotificationType.ENVIRONMENT,
                        fragmentClass = NotificationPopup::class,
                        layoutId = R.layout.engagement_booster_environment,
                        popupDashboardButton = PopupDashboardButton.ENVIRONMENT,
                        bodyStringId = R.string.engagementBoosterEnvironment_text,
                        buttonStringId = R.string.openEnvironment_text,
                        hyperlinkStringId = R.string.dismiss_text,
                        buttonNavigationClass = EnvironmentFragment::class),

                NotificationCategory(
                        categoryId = EngagementBoosterNotificationId.TRACKING,
                        notificationType = NotificationType.TRACKING,
                        fragmentClass = NotificationPopup::class,
                        layoutId = R.layout.engagement_booster_connectivity,
                        popupDashboardButton = PopupDashboardButton.DEVICES,
                        bodyStringId = R.string.engagementBoosterTracking_text,
                        buttonStringId = R.string.ok_text),

                NotificationCategory(
                        categoryId = EnvironmentNotificationId.DailyEnvironmentalReminder,
                        notificationType = NotificationType.DAILY_ENVIRONMENTAL_REMINDER,
                        fragmentClass = NotificationPopup::class,
                        layoutId = R.layout.engagement_booster_environment,
                        popupDashboardButton = PopupDashboardButton.ENVIRONMENT,
                        bodyStringId = R.string.dailyEnvironmentalNotificationBody_text,
                        notificationStringId = R.string.dailyEnvironmentalNotification_text,
                        buttonStringId = R.string.openEnvironment_text,
                        hyperlinkStringId = R.string.dismiss_text,
                        buttonNavigationClass = EnvironmentFragment::class),

                NotificationCategory(
                        categoryId = CloudManagerNotificationId.NO_CLOUD_SYNC_FOR_14_DAYS,
                        notificationType = NotificationType.NO_CLOUD_SYNC_FOR_14_DAYS,
                        fragmentClass = NotificationPopup::class,
                        layoutId = R.layout.text_header_popup,
                        popupDashboardButton = PopupDashboardButton.NONE,
                        bodyStringId = R.string.noCloudSyncFor14Days_text,
                        buttonStringId = R.string.ok_text),

                NotificationCategory(
                        categoryId = CloudManagerNotificationId.EMANCIPATION_IN_7_DAYS,
                        notificationType = NotificationType.EMANCIPATION_IN_7_DAYS,
                        fragmentClass = NotificationPopup::class,
                        popupDashboardButton = PopupDashboardButton.NONE,
                        headerStringId = R.string.emancipationIn7DaysTitle_text,
                        bodyStringId = R.string.emancipationIn7DaysBackgroundMessage_text,
                        buttonStringId = R.string.i_understand_text,
                        hyperlinkStringId = R.string.contact_teva_support,
                        hyperlinkNavigationClass = SupportFragment::class,
                        bodyImageSource = R.drawable.ic_share_data,
                        bodyImageTextId = R.string.downloadData_text,
                        imageFragmentClass = ReportFragment::class),

                NotificationCategory(
                        categoryId = CloudManagerNotificationId.EMANCIPATION_TOMORROW,
                        notificationType = NotificationType.EMANCIPATION_TOMORROW,
                        fragmentClass = NotificationPopup::class,
                        popupDashboardButton = PopupDashboardButton.NONE,
                        headerStringId = R.string.emancipationTomorrowTitle_text,
                        bodyStringId = R.string.emancipationTomorrowBackgroundMessage_text,
                        buttonStringId = R.string.i_understand_text,
                        hyperlinkStringId = R.string.contact_teva_support,
                        hyperlinkNavigationClass = SupportFragment::class,
                        bodyImageSource = R.drawable.ic_share_data,
                        bodyImageTextId = R.string.downloadData_text,
                        imageFragmentClass = ReportFragment::class),

                NotificationCategory(
                        categoryId = CloudManagerNotificationId.EMANCIPATED,
                        notificationType = NotificationType.EMANCIPATED,
                        fragmentClass = NotificationPopup::class,
                        popupDashboardButton = PopupDashboardButton.NONE,
                        headerStringId = R.string.emancipatedTitle_text,
                        bodyStringId = R.string.emancipatedBackgroundMessage_text,
                        hyperlinkStringId = R.string.contact_teva_support,
                        hyperlinkNavigationClass = SupportFragment::class)
        )
    }

    /**
     * Retrieves the NotificationCategory for the specified category id.
     */
    fun findCategory(id: String): NotificationCategory {
        return categories[id]!!
    }

    /**
     * Retrieves the NotificationCategory for the specified NotificationData.
     */
    fun findCategory(notificationInfo: NotificationInfo): NotificationCategory {
        return categories[notificationInfo.categoryId]!!
    }

}
