//
// DeviceManagerStringReplacementKey.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices

/**
 * This class contains dictionary keys used during string replacement.
 */

object DeviceManagerStringReplacementKey {
    /**
     * This constant represents the device nickname.
     */
    val NAME = "Name"

    /**
     * This constant represents the device medication name.
     */
    val MEDICATION_NAME = "MedicationName"

    /**
     * This constant is used to look up SystemErrorCode strings in notification userInfo.
     */
    val SYSTEM_ERROR_CODE = "SystemErrorCode"

    /**
     * This constant is used to look up the max number of events in the past to check for unsuccessful inhalation events.
     */
    val MAX_EVENTS_TO_CHECK_FOR_UNSUCCESSFUL_INHALATION_EVENTS = "MaxEventsToCheckForUnsuccessfulInhalationEvents"

    /**
     * This constant is used to look up the maximum number of unsuccessful inhalation events in a specified range of events.
     */
    val MAX_UNSUCCESSFUL_INHALE_EVENTS = "MaxUnsuccessfulInhaleEvents"
}
