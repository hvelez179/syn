//
// NotificationType.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.notification

/**
 * The types of notifications displayed in the notification shade or as popups within the application.
 * The value of the notifications is also used as its relative priority.  A lower priority value indicates
 * a higher priority.
 *
 * @param priority The priority of the NotificationType
 */
enum class NotificationType(val priority: Int) {
    // Common
    NONE(0),

    // High Priority Inhalation-Triggered Notifications
    // Inhalations Feedback
    INHALATION_FEEDBACK_GOOD_INHALATION(101),
    INHALATION_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION(102),
    INHALATION_FEEDBACK_LOW_INHALATION(103),
    INHALATION_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION_WITH_TIP(104),
    INHALATION_FEEDBACK_NO_INHALATION(105),
    INHALATION_FEEDBACK_HIGH_INHALATION(106),
    INHALATION_FEEDBACK_EXHALATION(107),

    // Lower Priority Inhalation-Triggered Notifications
    INHALATION_FEEDBACK_SUBOPTIMAL_INHALATIONS(110),
    OVERUSE(111),
    NEAR_EMPTY(112),
    SYSTEM_ERROR_DETECTED(113),

    // Other even lower priority notifications
    INHALER_REGISTERED(114),
    CONNECTIVITY_NOW_CONNECTED(115),
    CONNECTIVITY_DISCONNECTED_7_DAYS_OR_MORE(116),

    // Cloud Manager
    NO_CLOUD_SYNC_FOR_14_DAYS(403),

    // ENVIRONMENT
    DAILY_ENVIRONMENTAL_REMINDER(504),

    // User Feeling
    ASK_USER_FEELING(701),
    FEELING_GOOD(702),
    FEELING_MODERATE(703),
    FEELING_BAD(704),

    // Engagement Booster
    CREATE_USER_REPORT(801),
    DAILY_SELF_ASSESSMENT_TOOL(802),
    ENVIRONMENT(803),
    TRACKING(804),

    // Emancipation
    EMANCIPATION_IN_7_DAYS(1001),
    EMANCIPATION_TOMORROW(1002),
    EMANCIPATED(1003);

    /**
     * Gets a value indicating whether this NotificationType is an Inhalation Feedback notification type.
     */
    val isInhalationFeedback: Boolean
        get() = InhalationFeedbackTypes.contains(this)

    /**
     * Gets a value indicating whether this NotificationType is an Inhalation Triggered notification type.
     */
    val isInhalationTriggered: Boolean
        get() = InhalationTriggeredTypes.contains(this)

    /**
     * Gets a value indicating whether this NotificationType is an Engagement Booster notification type.
     */
    val isEnagagementBooster: Boolean
        get() = EngagementBoosterTypes.contains(this)

    /**
     * Gets a value indicating whether this NotificationType is a User Feeling type.
     */
    val isAskUserFeelingGroup: Boolean
        get() = UserFeelingTypes.contains(this)

    companion object {

        /**
         * The set of notifications types that are Inhalation Feedback types.
         */
        private val InhalationFeedbackTypes = setOf(INHALATION_FEEDBACK_GOOD_INHALATION, INHALATION_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION, INHALATION_FEEDBACK_LOW_INHALATION, INHALATION_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION_WITH_TIP, INHALATION_FEEDBACK_NO_INHALATION, INHALATION_FEEDBACK_HIGH_INHALATION, INHALATION_FEEDBACK_EXHALATION)

        /**
         * The set of notification types that are Inhalation Triggered types.
         */
        private val InhalationTriggeredTypes = setOf(INHALATION_FEEDBACK_GOOD_INHALATION, INHALATION_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION, INHALATION_FEEDBACK_LOW_INHALATION, INHALATION_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION_WITH_TIP, INHALATION_FEEDBACK_NO_INHALATION, INHALATION_FEEDBACK_HIGH_INHALATION, INHALATION_FEEDBACK_EXHALATION, INHALATION_FEEDBACK_SUBOPTIMAL_INHALATIONS, OVERUSE, NEAR_EMPTY)

        /**
         * The set of notification types that are Engagement Booster types.
         */
        private val EngagementBoosterTypes = setOf(CREATE_USER_REPORT, DAILY_SELF_ASSESSMENT_TOOL, ENVIRONMENT, TRACKING)

        /**
         * The set of notification types that are User Feeling types.
         */
        private val UserFeelingTypes = setOf(ASK_USER_FEELING, FEELING_GOOD, FEELING_MODERATE, FEELING_BAD)

        /**
         * This function determines if a new notification of a specified type should replace
         * and existing notification of specified type.
         *
         * @param newNotificationType - the type of the new notification.
         * @param existingNotificationType - the type of the existing notification.
         */
        fun shouldNotificationReplaceExisting(newNotificationType: NotificationType, existingNotificationType: NotificationType?): Boolean {
            if(existingNotificationType == null) {
                return false
            }

            var replaceExisting: Boolean = false

            if(newNotificationType.isInhalationFeedback &&
                    existingNotificationType.isInhalationFeedback) {
                // replace inhalation feedback messages
                replaceExisting = true
            } else if(newNotificationType == NotificationType.CONNECTIVITY_NOW_CONNECTED &&
                    existingNotificationType == NotificationType.CONNECTIVITY_NOW_CONNECTED) {
                // do not replace the first connection messages as they could be from
                // multiple inhalers.
                replaceExisting = false
            } else if(newNotificationType == existingNotificationType) {
                replaceExisting = true
            }
            return replaceExisting
        }

        /**
         * This function determines if an existing notification is of higher priority
         * than a new notification. It allows the DSA to appear on top of the environment
         * reminder.
         *
         * @param newNotificationType - the type of the new notification.
         * @param existingNotificationType - the type of the existing notification.
         */
        fun isExistingNotificationOfHigherPriority(newNotificationType: NotificationType, existingNotificationType: NotificationType?): Boolean {
            if(existingNotificationType == null) {
                return false
            }

            if(existingNotificationType == DAILY_ENVIRONMENTAL_REMINDER &&
                    newNotificationType == ASK_USER_FEELING) {
                // allow the DSA reminder to appear above the environment reminder
                return false
            }

            // return based on priority values
            return existingNotificationType.priority < newNotificationType.priority
        }
    }
}
