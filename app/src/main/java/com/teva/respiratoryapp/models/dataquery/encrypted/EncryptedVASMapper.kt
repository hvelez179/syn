//
// EncryptedVASMapper.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.encrypted


import com.teva.respiratoryapp.models.dataquery.generic.DataMapper
import com.teva.respiratoryapp.services.data.encrypteddata.entities.DailyUserFeelingDataEncrypted
import com.teva.userfeedback.entities.DailyUserFeeling
import com.teva.userfeedback.enumerations.UserFeeling

/**
 * This is the mapper class for mapping between DailyUserFeeling entities and DailyUserFeeling models and vice-versa
 */
class EncryptedVASMapper : DataMapper<DailyUserFeeling, DailyUserFeelingDataEncrypted> {

    /**
     * Maps a data model object ot a managed entity.
     *
     * @param source      The model object.
     * @param destination The managed entity.
     */
    override fun toManagedEntity(source: DailyUserFeeling, destination: DailyUserFeelingDataEncrypted) {
        destination.time = source.time
        destination.date = source.date
        destination.userFeeling = source.userFeeling.ordinal
        destination.hasChanged = if (source.hasChanged) 1 else 0
        destination.changedTime = source.changeTime
        destination.serverTimeOffset = source.serverTimeOffset
    }

    /**
     * Maps a managed entity to a data model object.
     *
     * @param source      The managed entity.
     * @param destination The model object.
     */
    override fun toModelObject(source: DailyUserFeelingDataEncrypted, destination: DailyUserFeeling) {
        destination.time = source.time
        destination.date = source.date
        destination.userFeeling = UserFeeling.fromOrdinal(source.userFeeling)
        destination.hasChanged = source.hasChanged == 1
        destination.changeTime = source.changedTime
        destination.serverTimeOffset = source.serverTimeOffset
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
