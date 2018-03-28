//
// EncryptedDailyAirQualityMapper.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import com.teva.environment.entities.DailyAirQuality
import com.teva.environment.enumerations.AirQuality
import com.teva.respiratoryapp.models.dataquery.generic.DataMapper
import com.teva.respiratoryapp.services.data.encrypteddata.entities.DailyAirQualityDataEncrypted

/**
 * This is the mapper class for mapping between daily air quality entities and daily air quality models and vice-versa
 */
class EncryptedDailyAirQualityMapper : DataMapper<DailyAirQuality, DailyAirQualityDataEncrypted> {

    /**
     * Maps a data model object to a managed entity.
     *
     * @param source      The model object.
     * @param destination The managed entity.
     */
    override fun toManagedEntity(source: DailyAirQuality, destination: DailyAirQualityDataEncrypted) {
        destination.airQuality = source.airQuality.ordinal
        destination.airQualityIndex = source.airQualityIndex
        destination.date = source.date
    }

    /**
     * Maps a managed entity to a data model object.
     *
     * @param source      The managed entity.
     * @param destination The model object.
     */
    override fun toModelObject(source: DailyAirQualityDataEncrypted, destination: DailyAirQuality) {
        destination.airQuality = AirQuality.fromOrdinal(source.airQuality)
        destination.airQualityIndex = source.airQualityIndex
        destination.date = source.date
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
