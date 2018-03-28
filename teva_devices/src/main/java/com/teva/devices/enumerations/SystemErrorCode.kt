//
// SystemErrorCode.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.enumerations

/**
 * Enumeration of system error codes
 */
enum class SystemErrorCode(val code: Int) {
    BAD_DATA(1),
    TIMESTAMP_ERROR(2),
    INHALER_PARAMETER_ERROR(3)
}
