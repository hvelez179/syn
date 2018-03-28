///
// ReminderDataQuery.kt
// teva_notifications
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.notifications.dataquery

import com.teva.common.dataquery.DataQueryForTrackedModels
import com.teva.notifications.entities.ReminderSetting

/**
 * Classes implementing this interface allow access to the reminder setting data store.
 */
interface ReminderDataQuery : DataQueryForTrackedModels<ReminderSetting> {

    /**
     * Gets the Reminder data with the given name.
     *
     * @param name The name of the reminder to be returned.
     * @return The reminder with the specified name
     */
    fun get(name: String): ReminderSetting?

    /**
     * Gets all the enabled Reminder Settings.
     *
     * @return A list of all reminders currently enabled.
     */
    val allEnabled: List<ReminderSetting>

    /**
     * Checks if there is a reminder setting with the given name.
     *
     * @return True if there is a reminder with the specified name, else false.
     */
    fun hasData(name: String): Boolean
}
