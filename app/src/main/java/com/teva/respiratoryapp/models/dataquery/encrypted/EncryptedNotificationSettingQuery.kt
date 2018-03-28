//
// EncryptedNotificationSettingQuery.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import com.teva.utilities.services.DependencyProvider
import com.teva.notifications.entities.ReminderSetting
import com.teva.respiratoryapp.models.dataquery.generic.GenericNotificationSettingQuery

/**
 * The encrypted data query implementation for notification settings.
 * @param dependencyProvider -  the dependency provider.
 */

class EncryptedNotificationSettingQuery(dependencyProvider: DependencyProvider)
    : GenericNotificationSettingQuery(
        dependencyProvider,
        dependencyProvider.resolve<EncryptedNotificationSettingMapper>()) {

    /**
     * This method creates a ReminderSetting model object and returns it.

     * @return - a new ReminderSetting object.
     */
    override fun createModel(): ReminderSetting {
        return ReminderSetting()
    }
}
