//
// InhaleEventInfoExtensions.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.model

import com.teva.devices.entities.InhaleEvent
import com.teva.devices.service.InhaleEventInfo

/**
 * Converts an InhaleEventInfo object into an InhaleEvent object.
 */
fun InhaleEventInfo.toInhaleEvent(deviceSerialNumber: String,
                                  drugUID: String): InhaleEvent {
    val inhaleEvent = InhaleEvent()
    inhaleEvent.deviceSerialNumber = deviceSerialNumber
    inhaleEvent.drugUID = drugUID
    inhaleEvent.eventTime = eventTime
    inhaleEvent.timezoneOffsetMinutes = timezoneOffsetMinutes
    inhaleEvent.eventUID = eventUID
    inhaleEvent.inhaleEventTime = inhaleStartOffset
    inhaleEvent.inhaleDuration = inhaleDuration
    inhaleEvent.inhalePeak = inhalePeak
    inhaleEvent.inhaleTimeToPeak = inhalePeakOffset
    inhaleEvent.inhaleVolume = inhaleVolume
    inhaleEvent.contextFlags = contextFlags
    inhaleEvent.closeTime = closeOffset
    inhaleEvent.status = status
    inhaleEvent.isValidInhale = isValidInhale
    inhaleEvent.doseId = doseId
    inhaleEvent.upperThresholdTime = upperThresholdOffset
    inhaleEvent.upperThresholdDuration = upperThresholdDuration

    return inhaleEvent
}
