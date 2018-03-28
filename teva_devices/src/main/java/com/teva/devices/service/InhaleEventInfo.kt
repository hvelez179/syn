//
// InhaleEventInfo.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.service

/**
 * Represents the information specific to inhale events.
 *
 * @property inhaleStartOffset Time delta from open event to inhale event in units of 0.1 seconds.
 * @property inhaleDuration Duration of inhalation in milliseconds.
 * @property inhalePeak Peak calculated flow in units of 100ml/minute.
 * @property inhalePeakOffset Time delta from inhale event to peak inhale in (ms).
 * @property inhaleVolume Calculated total inhale volume in ml.
 * @property status Flags indicate state of the inhale event.
 * @property isValidInhale A value indicating whether the inhalation was valid.
 * @property contextFlags Context flags indicating which optional features are implemented by the inhaler.
 * @property closeOffset Time delta in seconds from device open event to device closed event.
 * @property doseId Unique ID for this dose. This allows multiple inhalation events to be associated with a single dose.
 * @property upperThresholdOffset Time delta from open event to the detection of the flow above upper threshold.
 * @property upperThresholdDuration Duration of detected flow above upper threshold.
 */
class InhaleEventInfo(var inhaleStartOffset: Int = 0,
                      var inhaleDuration: Int = 0,
                      var inhalePeak: Int = 0,
                      var inhalePeakOffset: Int = 0,
                      var inhaleVolume: Int = 0,
                      var status: Int = 0,
                      var isValidInhale: Boolean = false,
                      var contextFlags: Int = 0,
                      var closeOffset: Int = 0,
                      var doseId: Int = 0,
                      var upperThresholdOffset: Int = 0,
                      var upperThresholdDuration: Int = 0) : DoseEventInfo()