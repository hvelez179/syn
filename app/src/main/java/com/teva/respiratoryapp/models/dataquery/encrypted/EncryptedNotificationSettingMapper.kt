//
// EncryptedNotificationSettingMapper.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import com.teva.notifications.entities.ReminderSetting
import com.teva.notifications.enumerations.RepeatType
import com.teva.respiratoryapp.models.dataquery.generic.DataMapper
import com.teva.respiratoryapp.services.data.encrypteddata.entities.NotificationSettingDataEncrypted
import org.threeten.bp.LocalTime

/**
 * This is the mapper class for mapping between notification setting entities and models and vice-versa.
 */

class EncryptedNotificationSettingMapper : DataMapper<ReminderSetting, NotificationSettingDataEncrypted> {

    /**
     * Maps a data model object to a managed entity.
     *
     * @param source      The model object.
     * @param destination The managed entity.
     */
    override fun toManagedEntity(source: ReminderSetting, destination: NotificationSettingDataEncrypted) {
        destination.name = source.name!!
        destination.isEnabled = if (source.isEnabled) 1 else 0
        destination.repeatType = source.repeatType.ordinal
        destination.serverTimeOffset = source.serverTimeOffset
        destination.hasChanged = if(source.hasChanged) 1 else 0
        destination.changedTime = source.changeTime
        if (source.timeOfDay != null) {
            destination.repeatTypeData = source.timeOfDay!!.toSecondOfDay()
        } else {
            destination.repeatTypeData = 0
        }
    }

    /**
     * Maps a managed entity to a data model object.
     *
     * @param source      The managed entity.
     * @param destination The model object.
     */
    override fun toModelObject(source: NotificationSettingDataEncrypted, destination: ReminderSetting) {
        destination.name = source.name
        destination.isEnabled = source.isEnabled > 0
        destination.repeatType = RepeatType.fromOrdinal(source.repeatType)
        destination.timeOfDay = LocalTime.ofSecondOfDay(source.repeatTypeData.toLong())
        destination.serverTimeOffset = source.serverTimeOffset
        destination.hasChanged = source.hasChanged == 1
        destination.changeTime = source.changedTime
    }

    /**
     * Called before mapping objects to allow the mapper to optimize by initializing common data.
     */
    override fun preMap(toModel: Boolean) {

    }

    /**
     * Called after mapping objects to allow the mapper to release common data.
     */
    override fun postMap() {

    }
}
