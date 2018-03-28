//
// InhaleEventDataEncrypted.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.services.data.encrypteddata.entities

import com.teva.respiratoryapp.services.data.encrypteddata.EncryptedEntity

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

/**
 * This class represents the database record for an Inhale Event.
 */
class InhaleEventDataEncrypted : EncryptedEntity() {

    /**
     * The unique ID for inhalation event.
     */
    var eventUID: Int
        get() = getIntProperty("eventUID")
        set(eventUID) {
            schemaMap.put("eventUID", eventUID)
        }

    /**
     * The peak calculated flow in units of 100ml/minute.
     */
    var inhalePeak: Int
        get() = getIntProperty("inhalePeak")
        set(inhalePeak) {
            schemaMap.put("inhalePeak", inhalePeak)
        }

    /**
     * The time delta from inhale event to peak inhale in (ms).
     */
    var inhaleTimeToPeak: Int
        get() = getIntProperty("inhaleTimeToPeak")
        set(inhaleTimeToPeak) {
            schemaMap.put("inhaleTimeToPeak", inhaleTimeToPeak)
        }

    /**
     * The duration of inhalation in milliseconds.
     */
    var duration: Int
        get() = getIntProperty("duration")
        set(duration) {
            schemaMap.put("duration", duration)
        }

    /**
     * The time delta from open event to inhale event in units of 0.1 seconds.
     */
    var inhaleEventTime: Int
        get() = getIntProperty("inhaleEventTime")
        set(eventTime) {
            schemaMap.put("inhaleEventTime", eventTime)
        }

    /**
     * The normalized date of inhalation event.
     */
    var eventTime: Instant?
        get() = getInstantProperty("eventTime")
        set(eventTime) = setInstantProperty("eventTime", eventTime)

    /**
     * The timezone offset stored in minutes
     */
    var timezoneOffset: Int
        get() = getIntProperty("timeZoneOffset")
        set(timezoneOffset) {
            schemaMap.put("timeZoneOffset", timezoneOffset)
        }

    /**
     * A value indicating whether the inhalation is valid.
     */
    var isValidInhale: Int
        get() = getIntProperty("isValidInhale")
        set(isValidInhale) {
            schemaMap.put("isValidInhale", isValidInhale)
        }

    /**
     * The status flags of the inhalation.
     */
    var status: Int
        get() = getIntProperty("status")
        set(status) {
            schemaMap.put("status", status)
        }

    /**
     * The time delta in seconds from device open event to device closed event.
     */
    var closeTime: Int
        get() = getIntProperty("closeTime")
        set(closeTime) {
            schemaMap.put("closeTime", closeTime)
        }

    /**
     * The local date of the inhale event.
     */
    var date: LocalDate?
        get() = getLocalDateProperty("date")
        set(date) = setLocalDateProperty("date", date)

    /**
     * The id of a removable drug cartridge if the device supports it.
     */
    var cartridgeUID: String
        get() = getStringProperty("cartridgeUID")
        set(cartridgeUID) {
            schemaMap.put("cartridgeUID", cartridgeUID)
        }

    /**
     * The unique ID for this dose. This allows multiple inhalation events to be associated
     * with a single dose.
     */
    var doseId: Int
        get() = getIntProperty("doseId")
        set(doseId) {
            schemaMap.put("doseId", doseId)
        }

    /**
     * The duration of detected flow above upper threshold.
     */
    var upperThresholdDuration: Int
        get() = getIntProperty("upperThresholdDuration")
        set(upperThresholdDuration) {
            schemaMap.put("upperThresholdDuration", upperThresholdDuration)
        }

    /**
     * Time delta from open event to the detection of the flow above upper threshold.
     */
    var upperThresholdTime: Int
        get() = getIntProperty("upperThresholdTime")
        set(upperThresholdTime) {
            schemaMap.put("upperThresholdTime", upperThresholdTime)
        }

    /**
     * This property is the difference in seconds between the real server time and the local device time setting.
     */
    var serverTimeOffset: Int?
        get() = getNullableIntProperty("serverTimeOffset")
        set(newValue) {
            schemaMap.put("serverTimeOffset", newValue)
        }

    /**
     * The device that owns the inhalations.
     */
    var device: DeviceDataEncrypted?
        get() {
            val innerValue = schemaMap["device"]

            if (innerValue is DeviceDataEncrypted) {
                return innerValue
            } else if (innerValue is Int) {
                val device = DeviceDataEncrypted()
                device.primaryKeyId = innerValue
                return device
            }

            return null
        }
        set(device) {
            var primaryKey: Int? = null

            if (device != null) {
                primaryKey = device.primaryKeyId
            }

            schemaMap.put("device", primaryKey)
        }
}
