//
// DeviceInfo.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.service

import org.threeten.bp.LocalDate

/**
 * The information representing and read from a Medication Dispenser device.
 *
 * @property serialNumber The serial number of the device.
 * @property manufacturerName The name of the device manufacturer.
 * @property hardwareRevision The hardware revision
 * @property softwareRevision The software revision
 * @property dosesTaken The number of doses that have been taken.
 * @property lastRecordId The id of the last record read from the device.
 * @property lotCode The lot code.
 * @property dateCode The date code
 * @property expirationDate The expiration date of the device.
 */
data class DeviceInfo(
        var serialNumber: String? = null,
        var manufacturerName: String? = null,
        var hardwareRevision: String? = null,
        var softwareRevision: String? = null,
        var dosesTaken: Int = 0,
        var lastRecordId: Int = 0,
        var lotCode: String? = null,
        var dateCode: String? = null,
        var expirationDate: LocalDate? = null)
