//
// GenericNotificationSettingQuery.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.generic

import com.teva.utilities.services.DependencyProvider
import com.teva.notifications.dataquery.ReminderDataQuery
import com.teva.notifications.entities.ReminderSetting
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.entities.NotificationSettingDataEncrypted

/**
 * An instance of this class allows access to the notification setting data.
 *
 * @param dependencyProvider - the dependency provider.
 */
abstract class GenericNotificationSettingQuery(
        dependencyProvider: DependencyProvider,
        mapper: DataMapper<ReminderSetting, NotificationSettingDataEncrypted>)
    : GenericQueryBaseForTrackedModels<ReminderSetting, NotificationSettingDataEncrypted>(
        dependencyProvider, NotificationSettingDataEncrypted::class.java, mapper),
        ReminderDataQuery {

    /**
     * This method returns a unique search criteria for searching a notification.
     *
     * @param model - the ReminderSetting object used for defining the search criteria.
     * @return - the Search criteria to be used for searching.
     */
    override fun uniqueSearchCriteria(model: ReminderSetting): SearchCriteria {
        return SearchCriteria("name = %@", model.name!!)
    }

    /**
     * This method is used by query classes which use cache for returning data.
     * Since this class does not use the cache, we do not do anything here.
     */
    override fun resetCache() {}

    /**
     * Gets the Reminder data with the given name.
     *
     * @param name - the name of the reminder to be returned.
     * @return - the reminder with the specified name
     */
    override operator fun get(name: String): ReminderSetting? {
        val queryInfo = QueryInfo(SearchCriteria("name = %@", name))
        val reminders = readBasedOnQuery(queryInfo)

        if (reminders.isNotEmpty()) {
            return reminders[0]
        }

        return null
    }

    /**
     * Gets all the enabled Reminder Settings.
     *
     * @return - a list of all reminders currently enabled.
     */
    override val allEnabled: List<ReminderSetting>
        get() {
            val queryInfo = QueryInfo(SearchCriteria("enabled = %@", 1))
            return readBasedOnQuery(queryInfo)
        }

    /**
     * Checks if there is a reminder setting with the given name.
     *
     * @return - true if there is a reminder with the specified name, else false.
     */
    override fun hasData(name: String): Boolean {
        return getCount(SearchCriteria("name = %@", name)) > 0
    }
}
