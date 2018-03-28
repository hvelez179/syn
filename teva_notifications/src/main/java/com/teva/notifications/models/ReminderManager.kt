///
// ReminderManager.kt
// teva_notifications
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.notifications.models

import com.teva.notifications.entities.ReminderSetting

/**
 * This interface defines methods for managing reminders.
 */
interface ReminderManager {
    /**
     * This method returns the reminder setting.
     *
     * @return Returns the reminder setting.
     */
    val reminderSetting: ReminderSetting?

    /**
     * This method configures the reminder notification and saves the setting to the database.
     *
     * @param enabled This parameter indicates whether the reminder is enabled.
     */
    fun enableReminder(enabled: Boolean)

    /**
     * This method starts the reminder manager.
     */
    fun start()

    /**
     * This method starts the reminder manager, with the ability to set the reminder enabled status.
     *
     * @param enabled: whether the reminder is enabled
     */
    fun start(enabled: Boolean)
}
