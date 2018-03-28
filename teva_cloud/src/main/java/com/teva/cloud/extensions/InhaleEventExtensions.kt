//
// InhaleEventExtensions.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.extensions

import com.teva.cloud.models.CloudSessionState
import com.teva.devices.entities.InhaleEvent
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects.DHPMedicationAdministration
import org.threeten.bp.Instant

/**
 * These extension methods provide conversion of a InhaleEvent to/from JSON
 */

fun stringOrEmpty(str: String): String {
    return if(str == "Unknown") "" else str
}



fun InhaleEvent.toDHPType(): DHPMedicationAdministration {

    val obj = DHPMedicationAdministration()

    obj.eventID = uniqueId.toStringOrUnknown()
    obj.deviceSerialNumber = deviceSerialNumber.toStringOrUnknown()
    obj.medicationEventUID = eventUID.toString()
    obj.medicationEventTime = eventTime?.epochSecond.toString()
    obj.medicationEventTime_TZ = timezoneOffsetMinutes.toGMTOffset()
    obj.medicationStartOffset = (inhaleEventTime).toString()
    obj.medicationDuration = inhaleDuration.toString()
    obj.medicationDurationUOM = "milliseconds"
    obj.medicationPeakFlow = ((inhalePeak)*100).toString()
    obj.medicationPeakFlowUOM = "ml/minute"
    obj.medicationPeakOffset = inhaleTimeToPeak.toString()
    obj.medicationPeakOffsetUOM = "milliseconds"
    obj.medicationVolume = inhaleVolume.toString()
    obj.medicationVolumeUOM = "ml"
    obj.medicationEventDuration = closeTime.toString()
    obj.medicationEventDurationUOM = "seconds"
    obj.doseID = doseId.toString()
    obj.cartridgeID = cartridgeUID.toStringOrUnknown()
    obj.drugID = drugUID.toStringOrUnknown()
    obj.upperThresholdOffset = upperThresholdTime.toString()
    obj.upperThresholdOffsetUOM = "milliseconds"
    obj.upperThresholdDuration = upperThresholdDuration.toString()
    obj.upperThresholdDurationUOM = "milliseconds"
    obj.isInvalidMedication = (!isValidInhale).toString()
    obj.objectName = obj.dhpObjectName
    obj.externalEntityID = CloudSessionState.shared.activeProfileID

    obj.status = status.toString()

    obj.sourceTime_GMT = eventTime?.toGMTString(false)
    obj.sourceTime_TZ = timezoneOffsetMinutes.toGMTOffset()
    obj.serverTimeOffset = serverTimeOffset?.toServerTimeOffsetString()

    return obj
}

internal fun DHPMedicationAdministration.fromDHPType(): InhaleEvent? {

    val inhaleEvent = InhaleEvent()

    if(this.medicationEventTime == null) {
        return null
    }

    inhaleEvent.deviceSerialNumber = this.deviceSerialNumber.fromStringOrUnknown()
    inhaleEvent.eventUID = (this.medicationEventUID ?: "0").toInt()
    inhaleEvent.eventTime = Instant.ofEpochSecond((this.medicationEventTime ?: "0").toLong())
    inhaleEvent.inhaleEventTime = ((this.medicationStartOffset ?: "0").toInt() / 100)
    inhaleEvent.inhaleDuration = ((this.medicationDuration ?: "0").toInt())
    inhaleEvent.inhalePeak = ((this.medicationPeakFlow ?: "0").toInt() / 100)
    inhaleEvent.inhaleTimeToPeak = (this.medicationPeakOffset ?: "0").toInt()
    inhaleEvent.inhaleVolume = (this.medicationVolume ?: "0").toInt()
    inhaleEvent.closeTime = (this.medicationEventDuration ?: "0").toInt()
    inhaleEvent.status = (this.status ?: "0").toInt()
    inhaleEvent.isValidInhale = (this.isInvalidMedication ?: "true").toBoolean() == false
    inhaleEvent.doseId = (this.doseID ?: "0").toInt()
    inhaleEvent.cartridgeUID = this.cartridgeID.fromStringOrUnknown()
    inhaleEvent.drugUID = this.drugID.fromStringOrUnknown()
    inhaleEvent.upperThresholdTime = (this.upperThresholdOffset ?: "0").toInt()
    inhaleEvent.upperThresholdDuration = (this.upperThresholdDuration ?: "0").toInt()
    inhaleEvent.timezoneOffsetMinutes = Int.fromGMTOffset(this.sourceTime_TZ.fromStringOrUnknown())
    inhaleEvent.changeTime = instantFromGMTString(this.sourceTime_GMT.fromStringOrUnknown())
    inhaleEvent.serverTimeOffset = this.serverTimeOffset.fromServerTimeOffsetString()

    return inhaleEvent
}

