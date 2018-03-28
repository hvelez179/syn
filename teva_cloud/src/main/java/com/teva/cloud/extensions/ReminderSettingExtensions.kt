//
// ReminderSettingExtensions.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.extensions

import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects.DHPSetting
import com.teva.notifications.entities.ReminderSetting
import com.teva.notifications.enumerations.RepeatType
import org.threeten.bp.Instant
import org.threeten.bp.LocalTime

/**
 * These extension methods provide conversion of a ReminderSetting to/from JSON
 */

fun ReminderSetting.toDHPType(): DHPSetting {

    val obj = DHPSetting()

    obj.settingName = name
    obj.settingValue = isEnabled.toString()
    obj.settingDataType = "boolean"

    return obj
}

internal fun DHPSetting.fromDHPType(changeTime: Instant): ReminderSetting {

    val setting = ReminderSetting()

    setting.name = this.settingName.fromStringOrUnknown()
    setting.isEnabled = (this.settingValue ?: "false").toBoolean()
    setting.repeatType = RepeatType.ONCE_PER_DAY
    setting.timeOfDay = LocalTime.now()
    setting.changeTime = changeTime

    return setting
}
