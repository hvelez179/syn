//
// DailyUserFeelingDataEncrypted.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.services.data.encrypteddata.entities

import com.teva.respiratoryapp.services.data.encrypteddata.EncryptedEntity

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

/**
 * This class represents daily user feeling information stored in the database.
 */
class DailyUserFeelingDataEncrypted : EncryptedEntity() {
    /**
     * The daily user feeling date from the schema map.
     */
    var date: LocalDate?
        get() = getLocalDateProperty("date")
        set(date) = setLocalDateProperty("date", date)

    /**
     * The daily user feeling time from the schema map.
     */
    var time: Instant?
        get() = getInstantProperty("time")
        set(time) = setInstantProperty("time", time)

    /**
     * The user feeling value from the schema map.
     */
    var userFeeling: Int
        get() = getIntProperty("userFeeling")
        set(userFeeling) {
            schemaMap.put("userFeeling", userFeeling)
        }

    /**
     * This property is the difference in seconds between the real server time and the local device time setting.
     */
    var serverTimeOffset: Int?
        get() = getNullableIntProperty("serverTimeOffset")
        set(newValue) {
            schemaMap.put("serverTimeOffset", newValue)
        }
}
