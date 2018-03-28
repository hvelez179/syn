//
// Device.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.entities

import com.teva.common.entities.TrackedModelObject
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.devices.enumerations.InhalerNameType
import com.teva.medication.entities.Medication
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

/**
 * This class provides device information.
 *
 * @property serialNumber The serial number of the device.
 * @property authenticationKey The authentication key used to authentication the device.
 * @property medication The medication of the device.
 * @property nickname The device nickname.
 * @property manufacturerName The name of the device manufacturer.
 * @property hardwareRevision The hardware revision
 * @property softwareRevision The software revision
 * @property lotCode The lot code.
 * @property dateCode The date code
 * @property expirationDate The expiration date of the device.
 * @property lastRecordId The id of the last record read from the device.
 * @property lastConnection The time of the last activity with the device.
 * @property inhalerNameType The nickname type.
 * @property isActive A value indicating whether the device is currently active or has been deleted.
 * @property isConnected A value indicating whether the device is currently connected
 * @property disconnectedTimeSpan The length of time that has passed since the last time this device has been connected.
 */
class Device(var serialNumber: String = "",
             var authenticationKey: String = "",
             var medication: Medication? = null,
             var nickname: String = "",
             var manufacturerName: String = "",
             var hardwareRevision: String = "",
             var softwareRevision: String = "",
             var lotCode: String = "",
             var dateCode: String = "",
             var expirationDate: LocalDate? = null,
             var lastRecordId: Int = 0,
             var lastConnection: Instant? = null,
             var inhalerNameType: InhalerNameType = InhalerNameType.CUSTOM,
             var isActive: Boolean = true,
             var isConnected: Boolean = false,
             var disconnectedTimeSpan: Duration? = null) : TrackedModelObject() {

    /**
     * The initial dose capacity of the Device.
     */
    var doseCount = 0
        set(doseCount) {
            if (doseCount > 0) {
                field = doseCount
            }
        }

    /**
     * The number of doses remaining before the device is empty.
     */
    var remainingDoseCount = 0
        set(remainingDoseCount) {
            if (remainingDoseCount >= 0) {
                field = remainingDoseCount
            }
        }

    /**
     * A value indicating whether the device is empty.
     */
    val isEmpty: Boolean
        get() = this.remainingDoseCount == 0

    /**
     * The percentage of total doses that are remaining.
     */
    val remainingDosePercentage: Int
        get() = if (this.doseCount > 0) Math.ceil(100.0 * this.remainingDoseCount.toDouble() / this.doseCount.toDouble()).toInt() else 0

    /**
     * A value indicating whether the device is almost empty.
     */
    val isNearEmpty: Boolean
        get() = medication != null && remainingDoseCount <= medication!!.nearEmptyDoseCount

    /**
     * A value indicating whether the device is expired.
     */
    val isExpired: Boolean
        get() {
            val timeService: TimeService = DependencyProvider.default.resolve()
            val today = LocalDate.from(timeService.now())
            return expirationDate != null && expirationDate!!.isBefore(today)
        }

    /**
     * The number of doses that have been taken.
     */
    val dosesTaken: Int
        get() = if (this.doseCount > this.remainingDoseCount) this.doseCount - this.remainingDoseCount else 0

    companion object {
        val jsonObjectName = "medical_device_info"
        val minConnectionDateString = "2016-01-01T00:00:00"
    }
}
