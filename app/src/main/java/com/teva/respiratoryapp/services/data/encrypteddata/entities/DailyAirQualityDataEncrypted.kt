//
// DailyAirQualityDataEncrypted.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.services.data.encrypteddata.entities

import com.teva.respiratoryapp.services.data.encrypteddata.EncryptedEntity

import org.threeten.bp.LocalDate

/**
 * This class represents daily air quality information stored in the database.
 */

class DailyAirQualityDataEncrypted : EncryptedEntity() {

    /**
     * The airQualityIndex from the schema map.
     */
    var airQualityIndex: Int
        get() = getIntProperty("airQualityIndex")
        set(airQualityIndex) {
            schemaMap.put("airQualityIndex", airQualityIndex)
        }

    /**
     * The airQuality from the schema map.
     */
    var airQuality: Int
        get() = getIntProperty("airQuality")
        set(airQuality) {
            schemaMap.put("airQuality", airQuality)
        }

    /**
     * The date associated with the daily air quality from the schema map.
     */
    var date: LocalDate?
        get() = getLocalDateProperty("date")
        set(date) = setLocalDateProperty("date", date)
}
