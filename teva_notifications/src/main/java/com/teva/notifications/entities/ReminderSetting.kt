///
// ReminderSetting.kt
// teva_notifications
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.notifications.entities

import com.teva.common.entities.TrackedModelObject
import com.teva.notifications.enumerations.RepeatType

import org.threeten.bp.LocalTime

/**
 * This class is used to store reminders.
 *
 * @property isEnabled Indicates if this reminder is enabled.
 * @property name Unique name for the Reminder.
 * @property repeatType Addresses how often the reminder is repeated.
 * @property timeOfDay The offset from midnight when reminder is expected to fire.
 */
class ReminderSetting(
        var isEnabled: Boolean = false,
        var name: String? = null,
        var repeatType: RepeatType = RepeatType.NONE,
        var timeOfDay: LocalTime? = null
    ) : TrackedModelObject() {
    companion object {
        val jsonObjectName = "user_preference_settings"
    }
}
