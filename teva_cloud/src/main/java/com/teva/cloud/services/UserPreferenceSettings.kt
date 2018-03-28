//
// UserPreferenceSettings.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.services

//import com.teva.cloud.extensions.fromJsonObject
//import com.teva.cloud.extensions.toDHPType
//import com.teva.cloud.extensions.toJsonObject
import com.teva.cloud.extensions.*
import com.teva.cloud.models.CloudSessionState
import com.teva.cloud.utilities.DateTimeConversionUtil
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects.DHPUserPreferenceSettings
import com.teva.notifications.entities.ReminderSetting
import org.threeten.bp.Instant


/**
 * This extension provides conversion of a UserPreferenceSettings wrapper to/from JSON
 */
class UserPreferenceSettings {

    var recurringReminderSettings = ArrayList<ReminderSetting>()
    var changeTime = Instant.now()

    internal fun toDHPType(): DHPUserPreferenceSettings {

        val obj = DHPUserPreferenceSettings()

        var changeTime = Instant.ofEpochSecond(0)
        var serverTimeOffset: Int? = null
        for (setting in recurringReminderSettings) {

            if (changeTime < setting.changeTime) {
                changeTime = setting.changeTime
                serverTimeOffset = setting.serverTimeOffset
            }
        }

        obj.objectName = obj.dhpObjectName
        obj.sourceTime_GMT = changeTime.toGMTString(false)
        obj.sourceTime_TZ = DateTimeConversionUtil.getGMTTimezoneOffsetString(Instant.now())
        obj.serverTimeOffset = serverTimeOffset?.toServerTimeOffsetString()
        obj.externalEntityID = CloudSessionState.shared.activeProfileID

        obj.setting = recurringReminderSettings.map { it.toDHPType() }

        return obj
    }

    companion object {

        internal fun fromDHPType(obj: DHPUserPreferenceSettings): UserPreferenceSettings
        {
            val userPreferenceSettings = UserPreferenceSettings()
            userPreferenceSettings.changeTime = instantFromGMTString(obj.sourceTime_GMT.fromStringOrUnknown())

            obj.setting?.let { settings ->
                userPreferenceSettings.recurringReminderSettings = ArrayList()
                userPreferenceSettings.recurringReminderSettings.addAll(
                        settings.map { it.fromDHPType(userPreferenceSettings.changeTime)})
            }
            return userPreferenceSettings
        }
    }

}