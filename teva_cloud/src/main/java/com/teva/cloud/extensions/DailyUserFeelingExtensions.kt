//
// DailyUserFeelingExtensions.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.extensions

import com.teva.cloud.models.CloudSessionState
import com.teva.common.utilities.toInstant
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects.DHPQuestionnaireResponse
import com.teva.userfeedback.entities.DailyUserFeeling
import com.teva.userfeedback.enumerations.UserFeeling
import org.threeten.bp.LocalDateTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset


internal fun DailyUserFeeling.toDHPType(): DHPQuestionnaireResponse {

    val obj = DHPQuestionnaireResponse()

    obj.text = DailyUserFeeling.questionnaireText
    obj.answerValue = this.userFeeling.ordinal.toString()
    obj.date = date?.toInstant()?.toGMTString(false)
    obj.objectName = obj.dhpObjectName
    obj.externalEntityID = CloudSessionState.shared.activeProfileID
    obj.sourceTime_GMT = time?.toGMTString(false)
    obj.sourceTime_TZ = time?.toGMTOffset()
    obj.serverTimeOffset = serverTimeOffset?.toServerTimeOffsetString()

    return obj
}

internal fun DHPQuestionnaireResponse.fromDHPType(): DailyUserFeeling? {

    val dailyUserFeeling = DailyUserFeeling()

    if(this.text != DailyUserFeeling.questionnaireText) {
        return null
    }

    dailyUserFeeling.userFeeling = UserFeeling.fromOrdinal(this.answerValue!!.toInt())
    dailyUserFeeling.date = localDateFromGMTString(this.date.fromStringOrUnknown())
    val dateString = this.sourceTime_GMT.fromStringOrUnknown()
    var timezoneOffsetString = this.sourceTime_TZ.fromStringOrUnknown()
    if(timezoneOffsetString.contains("GMT")) {
        timezoneOffsetString = timezoneOffsetString.substring(3, timezoneOffsetString.length)
    }
    val dateTime = LocalDateTime.parse(dateString)
    val offsetDateTime = OffsetDateTime.of(dateTime, ZoneOffset.of(timezoneOffsetString) )
    dailyUserFeeling.time = offsetDateTime.toInstant()
    dailyUserFeeling.changeTime = offsetDateTime.toInstant()
    dailyUserFeeling.serverTimeOffset = this.serverTimeOffset.fromServerTimeOffsetString()

    return dailyUserFeeling
}
