//
// DeviceManagerNotificationId.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.model

/**
 * This class contains unique identifiers associated with Device Manager notifications in Android.
 */
object DeviceManagerNotificationId {
    // Device Dosage
    val DEVICE_NEAR_EMPTY = "DeviceNearEmpty"
    val DEVICE_OVERUSE = "DeviceOveruse"

    // Connectivity
    val CONNECTIVITY_NOW_CONNECTED = "ConnectivityNowConnected"
    val CONNECTIVITY_DISCONNECTED_7_DAYS_OR_MORE = "ConnectivityDisconnected7DaysOrMore"

    // Inhalations Feedback
    val INHALATIONS_FEEDBACK_GOOD_INHALATION = "InhalationsFeedbackGoodInhalation"
    val INHALATIONS_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION = "InhalationsFeedbackTurnOffGoodInhalationNotification"
    val INHALATIONS_FEEDBACK_LOW_INHALATION = "InhalationsFeedbackLowInhalation"
    val INHALATIONS_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION_WITH_TIP = "InhalationsFeedbackTurnOffGoodInhalationNotificationWithTip"
    val INHALATIONS_FEEDBACK_NO_INHALATION = "InhalationsFeedbackNoInhalation"
    val INHALATIONS_FEEDBACK_HIGH_INHALATION = "InhalationsFeedbackHighInhalation"
    val INHALATIONS_FEEDBACK_EXHALATION = "InhalationsFeedbackExhalation"
    val INHALATIONS_FEEDBACK_SUBOPTIMAL_INHALATIONS = "InhalationsFeedbackSuboptimalInhalations"

    // System Error
    val SYSTEM_ERROR_DETECTED = "SystemErrorDetected"
}
