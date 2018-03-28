//
// InhaleEvent.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.entities

import com.teva.devices.enumerations.InhaleStatus
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import java.util.*

/**
 * Represents an inhalation event.
 *
 * @property inhaleEventTime Time delta from open event to inhale event in units of 0.1 seconds.
 * @property inhaleDuration Duration of inhalation in milliseconds.
 * @property inhalePeak Peak calculated flow in units of 100ml/minute.
 * @property inhaleTimeToPeak Time delta from inhale event to peak inhale in (ms).
 * @property inhaleVolume Calculated total inhale volume in ml.
 * @property status Flags indicate state of the inhale event.
 * @property isValidInhale A value indicating whether the inhalation was valid.
 * @property contextFlags Context flags indicating which optional features are implemented by the inhaler.
 * @property closeTime Time delta in seconds from device open event to device closed event.
 * @property doseId Unique ID for this dose. This allows multiple inhalation events to be associated with a single dose.
 * @property cartridgeUID Unique id for the drug cartridge.
 * @property upperThresholdTime Time delta from open event to the detection of the flow above upper threshold.
 * @property upperThresholdDuration Duration of detected flow above upper threshold.
 */
class InhaleEvent(var inhaleEventTime: Int = 0,
                  var inhaleDuration: Int = 0,
                  var inhalePeak: Int = 0,
                  var inhaleTimeToPeak: Int = 0,
                  var inhaleVolume: Int = 0,
                  var status: Int = 0,
                  var isValidInhale: Boolean = false,
                  var contextFlags: Int = 0,
                  var closeTime: Int = 0,
                  var doseId: Int = 0,
                  var cartridgeUID: String = "",
                  var upperThresholdTime: Int = 0,
                  var upperThresholdDuration: Int = 0) : DoseEvent() {

    /**
     * Peak calculated flow in units of 100ml/minute.
     * The peak inspiratory flow calculated from the inhale event data
     */
    val peakInspiratoryFlow: Int
        get() = inhalePeak

    /**
     * A value indicating whether the inhalation has any issue flags set.
     */
    val hasIssues: Boolean
        get() = status != 0

    /**
     * A unique id for the inhale event.
     */
    val uniqueId: String
        get() = "$deviceSerialNumber:$eventUID"

    /**
     * The set of issues of the inhalation.
     */
    val issues: EnumSet<InhaleStatus.InhaleStatusFlag>
        get() = InhaleStatus.getInhaleStatusFlags(status)

    /**
     * Gets a LocalDateTime for the event time.
     */
    val localEventTime: LocalDateTime
        get() {
            val inhaleEventZoneId = ZoneOffset.ofHoursMinutes(timezoneOffsetMinutes / MINUTES_PER_HOUR,
                    timezoneOffsetMinutes % MINUTES_PER_HOUR)

            return LocalDateTime.ofInstant(eventTime!!, inhaleEventZoneId)
        }

    companion object {
        val jsonObjectName = "medication_administration"
        val MINUTES_PER_HOUR = 60
    }
}
