//
// DailyUserFeeling.kt
// teva_userfeedback
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.userfeedback.entities

import com.teva.common.entities.TrackedModelObject
import com.teva.userfeedback.enumerations.UserFeeling

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

/**
 * This class provides Daily User Feeling information.
 *
 * @property time The time at which the user feeling was entered.
 * @property userFeeling The user feeling entered by the user.
 */
class DailyUserFeeling(var time: Instant? = null, var userFeeling: UserFeeling = UserFeeling.UNKNOWN) : TrackedModelObject() {

    /**
     * This property is the date associated with the Daily User Feeling.
     * It is used as a key to save, and lookup the DailyUserFeeling in the database and cloud.
     */
    var date: LocalDate? = null

    init {
        if (time != null) {
            val zonedUserFeelingTime = ZonedDateTime.ofInstant(time, ZoneId.systemDefault())
            date = LocalDate.from(zonedUserFeelingTime)
        }
    }

    companion object {
        val questionnaireText = "dailyFeeling"
        val jsonObjectName = "questionnaire_response"
    }
}
